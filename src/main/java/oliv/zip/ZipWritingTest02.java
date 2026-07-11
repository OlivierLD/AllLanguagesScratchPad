package oliv.zip;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * This class generates a zip archive containing all the
 * files within constant ZIP_DIR.
 * For this example you'll need to put a few files in the
 * directory ZIP_DIR, and it will generate a zip archive
 * containing all those files in the location OUTPUT_ZIP.
 * Minimum Java version: 8
 */
public class ZipWritingTest02 {
    private final static Logger LOG = Logger.getLogger("Zip02"); // Logger
    public final static String DIR_TO_ZIP = "/Users/olivierlediouris/repos/AllLanguagesScratchPad/temp"; // Where to get the data from
    public static final String OUTPUT_ZIP = "./output.02.zip";  // Final result

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
        catch (IOException|ZipParsingException e) {
            LOG.log(Level.SEVERE, "Error while creating zip file.", e);
        }
        return null;
    }

    /**
     * Adds an extra file to the zip archive, copying in the created
     * date and a comment.
     * @param file file to be archived
     * @param zipStream archive to contain the file.
     */
    private void addToZipFile(Path file, ZipOutputStream zipStream, String pathInZip) {
        String inputFileName = file.toFile().getPath();
        try (FileInputStream inputStream = new FileInputStream(inputFileName)) {

            // create a new ZipEntry, which is basically another file
            // within the archive. We omit the path from the filename
            String entryName = pathInZip + "/" + file.toFile().getName();
            System.out.printf("Creating zip entry %s\n", entryName);
            ZipEntry entry = new ZipEntry(entryName);
            entry.setCreationTime(FileTime.fromMillis(file.toFile().lastModified()));
            entry.setComment("Created by OlivSoft, for tests.");
            zipStream.putNextEntry(entry);

            LOG.info("Generated new entry for: " + inputFileName);

            // Now we copy the existing file into the zip archive. To do
            // this we write into the zip stream, the call to putNextEntry
            // above prepared the stream, we now write the bytes for this
            // entry. For another source such as an in memory array, you'd
            // just change where you read the information from.
            byte[] readBuffer = new byte[2048];
            int amountRead;
            int written = 0;

            while ((amountRead = inputStream.read(readBuffer)) > 0) {
                zipStream.write(readBuffer, 0, amountRead);          // write into the file IN the zip.
                written += amountRead;
            }

            LOG.info("Stored " + written + " bytes to " + inputFileName);
        }
        catch (IOException e) {
            throw new ZipParsingException("Unable to process " + inputFileName, e);
        }
    }

    /**
     * Instantiate a new ZipWriter and provide the directory we want to compress.
     * @param args command line args not used
     */
    public static void main(String[] args) {
        ZipWritingTest02 zw = new ZipWritingTest02();
        final ZipOutputStream zipStream = zw.createZip(OUTPUT_ZIP);

        try {
            Path directory = Paths.get(DIR_TO_ZIP);
            DirectoryStream<Path> dirStream = Files.newDirectoryStream(directory);

            dirStream.forEach(path -> {
                System.out.printf("Adding %s\n", path);
                zw.addToZipFile(path, zipStream, "downOneLevel/downTwoLevels");
            });

            zipStream.close();

        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

    /**
     * We want to let a checked exception escape from a lambda that does not
     * allow exceptions. The only way I can see of doing this is to wrap the
     * exception in a RuntimeException. This is a somewhat unfortunate side
     * effect of lambda's being based off of interfaces.
     */
    private class ZipParsingException extends RuntimeException {
        public ZipParsingException(String reason, Exception inner) {
            super(reason, inner);
        }
    }
}