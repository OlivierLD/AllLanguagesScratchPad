package http.httpserver;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Dedicated (meaning does only what it's designed to explicitly do, nothing generic) HTTP Server.
 * This is NOT J2EE Compliant, nor CGI, nor anything.
 *
 * This is the smallest server ever, as an example. It illustrates the level-0 basics.
 * Does valid HTTP Responses, in pure ASCII. Only ONE class involved.
 *
 * For HTTP Responses, see https://www.w3.org/Protocols/rfc2616/rfc2616-sec10.html
 *
 * To run it, try:
 *       curl --location --request GET 'http://localhost:9999/device-access?dev=01&status=off'
 *       curl --location --request GET 'http://localhost:9999/?file=package.minihttp.server.sh'
 * and then look in the code for details.
 *
 * To package and run it, use package.minihttp.server.sh
 * The only required jar is ~5kb big.
 */
public class StandaloneHTTPServer {
	private static boolean verbose = false;
	private boolean keepWorking = true;

	private boolean keepWorking() {
		return keepWorking;
	}

	private void keepWorking(boolean b) {
		keepWorking = b;
	}

	public StandaloneHTTPServer() {
	}

	private final static String VERBOSE_PRM_PREFIX = "--verbose:";

	public StandaloneHTTPServer(String... prms) {
		// Bind the server
		String machineName = "localhost";
		int port = 9999;

		machineName = System.getProperty("http.host", machineName);
		port = Integer.parseInt(System.getProperty("http.port", String.valueOf(port)));

		System.out.printf("HTTP Host:%s\n", machineName);
		System.out.printf("HTTP Port:%d\n", port);

		if (prms != null && prms.length > 0) {
			for (int i = 0; i < prms.length; i++) {
				if (prms[i].startsWith(VERBOSE_PRM_PREFIX)) {
					verbose = prms[i].substring(VERBOSE_PRM_PREFIX.length()).equalsIgnoreCase("y");
				}
			}
		}

		if (verbose) {
			System.out.println("Server running from [" + System.getProperty("user.dir") + "]");
		}
		// For the example: Start a Thread that does its own job...
		Thread dummyThread = new Thread(() -> {
			while (keepWorking()) {
				try {
					synchronized (this) {
						wait(1_000L);
					}
				} catch (InterruptedException ie) {
					System.out.println("==> Bing.");
				}
				// System.out.println("Boom!");
			}
			System.out.println("Told to get out.");
		});
		dummyThread.start();

		// Infinite loop
		try {
			ServerSocket ss = new ServerSocket(port);
			while (true) {
				Socket client = ss.accept();
				BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream()));
				PrintWriter out = new PrintWriter(new OutputStreamWriter(client.getOutputStream()));

				String line;
				while ((line = in.readLine()) != null) {
					if (line.length() == 0) {
						break;
					} else if (line.startsWith("POST /exit") || line.startsWith("GET /exit")) {
						System.out.println("Received an exit signal from REST request");
						synchronized (dummyThread) {
							keepWorking(false);
							dummyThread.notify();
						}
						try {
							Thread.sleep(1_000L);
						} // Just give it some time to stop...
						catch (InterruptedException ie) {
							// Boom!
						}
						System.exit(0); // That's tough...
					} else if (line.startsWith("POST /") || line.startsWith("GET /")) {
						manageRequest(line, out);
					} else {
						System.out.println("Un-Managed case:" + line);
					}
					if (verbose) {
						System.out.println("Read:[" + line + "]");
					}
				}
				out.flush();
				out.close();
				in.close();
				client.close();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	// Just a wrapper, really.
	private void manageRequest(String request, PrintWriter out) {
		out.println(generateContent(request));
	}

	// The actual skill of the server is here.
	private String generateContent(String request) {
		String str = ""; // "Content-Type: text/plain\r\n\r\n";
		if (verbose) {
			System.out.println("Managing request [" + request + "]");
		}
		String[] elements = request.split(" ");
		if (elements[0].equals("GET")) {
			String[] parts = elements[1].split("\\?");
			if (parts.length == 2 && parts[1].startsWith("file=")) {
				String fileName = parts[1].substring("file=".length());
				File data = new File(fileName);
				if (verbose) {
					System.out.println("Trying to read " + fileName);
				}
				if (data.exists()) {
					try {
						BufferedReader br = new BufferedReader(new FileReader(data));
						String line = "";
						String content = "";
						while (line != null) {
							line = br.readLine();
							if (line != null) {
								content += (line + "\n");
							}
						}
						br.close();
						str = "HTTP/1.1 200\r\n\r\n" + content;
					} catch (Exception ex) {
						ex.printStackTrace();
					}
				} else {
					str = String.format("HTTP/1.1 400\r\n\r\nThere is no parameter is this query. File %s not found.", fileName);
				}
			} else {
				if (request.startsWith("GET /device-access")) {
					System.out.println("--> " + request);
					String dev = "";
					String status = "";
					String[] params = parts[1].split("&");
					for (String nv : params) {
						String[] nvPair = nv.split("=");
						//        System.out.println(nvPair[0] + " = " + nvPair[1]);
						if (nvPair[0].equals("dev")) {
							dev = nvPair[1];
						} else if (nvPair[0].equals("status")) {
							status = nvPair[1];
						}
					}
					System.out.println("Setting [" + dev + "] to [" + status + "]");
					if (("01".equals(dev) || "02".equals(dev)) &&
							("on".equals(status) || "off".equals(status))) {
						str = String.format("HTTP/1.1 200\r\n\r\nGot it. Dev:%s, Status:%s\r\n", dev, status);
					} else {
						str = ("HTTP/1.1 400\r\n\r\nUnknown dev/status [" + dev + "/" + status + "]");
					}
				} else {
					str = ("HTTP/1.1 400\r\n\r\nUn-managed: " + request);
				}
			}
		} else {
			str = "HTTP/1.1 400\r\n\r\n- Not managed -";
		}
		return str;
	}

	/**
	 * @param args see usage
	 */
	public static void main(String... args) {
		System.out.println("Starting tiny dedicated server");
		System.out.println("Use [Ctrl] + [C] to stop it, or POST or GET the following request:");
		System.out.println("http://localhost:" + System.getProperty("http.port", "9999") + "/exit");
		System.out.println("Data are available at:");
		System.out.println("http://localhost:" + System.getProperty("http.port", "9999"));
		System.out.println("----------------------------------");
		if (isHelpRequired(args)) {
			System.out.println("Usage is: java " + new StandaloneHTTPServer().getClass().getName() + " prms");
			System.out.println("\twhere prms can be:");
			System.out.println("\t-?\tDisplay this message");
			System.out.println("\t-verbose=[y|n] - default is n");
			System.out.println("The following variables can be defined in the command line (before the class name):");
			System.out.println("\t-Dhttp.port=[port number]\tThe HTTP port to listen to, 9999 by default");
			System.out.println("\t-Dhttp.host=[hostname]   \tThe HTTP host to bind, localhost by default");
			System.out.println("Example:");
			System.out.println("java -Dhttp.port=6789 -Dhttp.host=localhost " + new StandaloneHTTPServer().getClass().getName());
			System.exit(0);
		}

		Runtime.getRuntime().addShutdownHook(new Thread(() -> {
			System.out.println("\nShutting down nicely...");
		}, "Shutdown Hook"));
		new StandaloneHTTPServer(args);
	}

	private static boolean isHelpRequired(String... args) {
		boolean ret = false;
		if (args != null) {
			for (int i = 0; i < args.length; i++) {
				if (args[i].equalsIgnoreCase("-H") ||
						args[i].equalsIgnoreCase("-HELP") ||
						args[i].equalsIgnoreCase("--HELP") ||
						args[i].equalsIgnoreCase("HELP") ||
						args[i].equals("?") ||
						args[i].equals("-?")) {
					ret = true;
					break;
				}
			}
		}
		return ret;
	}
}
