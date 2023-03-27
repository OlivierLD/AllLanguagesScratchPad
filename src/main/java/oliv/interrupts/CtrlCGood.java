package oliv.interrupts;

import java.util.Date;
import java.util.concurrent.atomic.AtomicBoolean;

public class CtrlCGood {
    /**
     * How to do it correctly
     */
    public static void main(String...args) {

        final Thread itsMe = Thread.currentThread();
        AtomicBoolean keepWorking = new AtomicBoolean(true);

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            System.out.println("\nOops! Trapped exit signal...");
            synchronized (itsMe) {
                // itsMe.notify();
                keepWorking.set(false);
                try {
                    itsMe.wait(); // Give the main thread time to finish...
                    System.out.println("... Gone");
                } catch (InterruptedException ie) {
                    ie.printStackTrace();
                }
            }
        }));
        System.out.println("Starting... Ctrl-C to stop.");
        try {
            synchronized (itsMe) {
                // itsMe.wait();
                while (keepWorking.get()) {
                    System.out.printf("Still at work, at %s...\n", new Date());
                    itsMe.wait(1_000L);
                }
            }
            System.out.println("Ok, ok! I'm leaving! (doing some cleanup first, 5s...)");
            // This stuff takes time
            Thread.sleep(5_000L);
            System.out.println("Done cleaning my stuff!");
        } catch (InterruptedException ie) {
            ie.printStackTrace();
        }
        System.out.println("Bye!");
        synchronized(itsMe) {
            itsMe.notify(); // Unlock the shutdown hook.
        }
        System.out.println("Everyone's done now.");
    }
}
