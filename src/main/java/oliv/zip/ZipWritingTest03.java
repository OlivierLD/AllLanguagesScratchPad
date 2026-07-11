package oliv.zip;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Write directly in a zip file.
 * Minimum Java version: 8
 */
public class ZipWritingTest03 {
    private final static Logger LOG = Logger.getLogger("Zip03"); // Logger
    public final static List<Map<String, List<String>>> DATA_TO_WRITE = Arrays.asList(
            Map.of("key01", Arrays.asList("Akeu", "Coucou", "Larigou")),
            Map.of("key02", Arrays.asList("Tagada", "Pouet", "Pouet")),
            Map.of("key03", Arrays.asList("Lorem", "ipsum", "dolor", "sit", "amet,", "consectetur", "adipiscing", "elit."))

    );
    public static final String OUTPUT_ZIP = "./output.03.zip";  // Final result

    /**
     * Create the zip, and return it.
     */
    private ZipOutputStream createZip(String zipName) {

        // the zip file name that we will create
        File zipFileName = Paths.get(zipName).toFile();

        try {
            ZipOutputStream zipStream = new ZipOutputStream(new FileOutputStream(zipFileName));
            LOG.info(String.format("Zip file %s created", zipName));
            return zipStream;
        }
        catch (IOException e) {
            LOG.log(Level.SEVERE, "Error while creating zip file.", e);
        }
        return null;
    }

    /**
     * Instantiate a new ZipWriter and provide the directory we want to compress.
     * @param args command line args not used
     */
    public static void main(String[] args) {
        ZipWritingTest03 zw = new ZipWritingTest03();
        final ZipOutputStream zipStream = zw.createZip(OUTPUT_ZIP);

        try {

            DATA_TO_WRITE.stream().forEach(
                 oneMap -> {
                     oneMap.keySet().stream().forEach(key -> {
                         System.out.printf("File %s\n", key);

                         String pathInZip = "LogRootLevel";
                         try {
                             String entryName = "/" + pathInZip + "/" + key + ".txt";
                             System.out.printf("Creating zip entry %s\n", entryName);
                             ZipEntry entry = new ZipEntry(entryName);
                             entry.setCreationTime(FileTime.fromMillis(System.currentTimeMillis()));
                             entry.setComment("Created by OlivSoft, for tests.");
                             zipStream.putNextEntry(entry);

                             LOG.info(String.format("Generated new entry for: %s", entryName));

                             List<String> records = oneMap.get(key);
                             records.stream().forEach(word -> {
                                 System.out.printf("\tWriting %s\n", word);
                                 String toWrite = word + "\n";
                                 try {
                                     zipStream.write(toWrite.getBytes(), 0, toWrite.length());
                                     zipStream.flush();
                                 } catch (IOException ex2) {
                                     ex2.printStackTrace();
                                 }
                             });
                         } catch (Exception ex) {
                             ex.printStackTrace();
                         }
                     });
                 }
            );

            zipStream.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }
}