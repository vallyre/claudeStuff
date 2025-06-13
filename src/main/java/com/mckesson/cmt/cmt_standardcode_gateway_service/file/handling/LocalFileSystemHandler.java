package com.mckesson.cmt.cmt_standardcode_gateway_service.file.handling;

import java.io.*;
import java.nio.file.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class LocalFileSystemHandler implements FileSystemHandler {

    private static final Logger log = LoggerFactory.getLogger(LocalFileSystemHandler.class);

    private final String baseLocalPath;

    public LocalFileSystemHandler(String baseLocalPath) {
        this.baseLocalPath = baseLocalPath;
        // Validate baseLocalPath on initialization
        try {
            Path basePath = Paths.get(baseLocalPath).normalize();
            if (!Files.exists(basePath)) {
                Files.createDirectories(basePath);
                log.info("Initialized base directory: {}", basePath);
            }
            if (!Files.isWritable(basePath)) {
                throw new IOException("Base directory is not writable: " + baseLocalPath);
            }
        } catch (IOException e) {
            log.error("Failed to initialize LocalFileSystemHandler with base path {}: {}", baseLocalPath,
                    e.getMessage());
            throw new IllegalArgumentException("Invalid base local path: " + baseLocalPath, e);
        }
    }

    public Path prepareTargetDirectory(String folderName, String rootFolder) throws IOException {
        Path dateDirectoryPath = Paths.get(baseLocalPath, rootFolder);

        try {
            if (!Files.exists(dateDirectoryPath)) {
                Files.createDirectories(dateDirectoryPath);
                log.info("Date directory created: {}", dateDirectoryPath);
            } else {
                log.info("Date directory already exists: {}", dateDirectoryPath);
            }

            Path folderDirectoryPath = Paths.get(dateDirectoryPath.toString(), folderName);
            if (!Files.exists(folderDirectoryPath)) {
                Files.createDirectories(folderDirectoryPath);
                log.info("Folder directory created: {}", folderDirectoryPath);
            } else {
                log.info("Folder directory already exists: {}", folderDirectoryPath);
            }

            return folderDirectoryPath;
        } catch (IOException e) {
            String errorMsg = "Failed to prepare target directory " + folderName + ": " + e.getMessage();
            log.error(errorMsg);
            throw new IOException(errorMsg, e); // Fatal: cannot proceed without directory
        }
    }

    public void saveFile(InputStream inputStream, Path destinationPath) throws IOException {
        try {
            Files.copy(inputStream, destinationPath, StandardCopyOption.REPLACE_EXISTING);
            log.info("File saved to: {}", destinationPath);
        } catch (IOException e) {
            String errorMsg = "Error saving file to " + destinationPath + ": " + e.getMessage();
            log.error(errorMsg);
            throw new IOException(errorMsg, e); // Fatal: cannot proceed if file save fails
        } finally {
            try {
                if (inputStream != null) {
                    inputStream.close();
                }
            } catch (IOException e) {
                log.warn("Failed to close input stream: {}", e.getMessage());
            }
        }
    }

    @Override
    public void extractZipFiles(String zipFilePath, String localPath) throws IOException {
        Path zipPath = Paths.get(zipFilePath);
        if (!Files.exists(zipPath)) {
            String errorMsg = "ZIP file does not exist: " + zipFilePath;
            log.error(errorMsg);
            throw new IOException(errorMsg); // Fatal: cannot extract non-existent file
        }

        String zipFileName = zipPath.getFileName().toString();
        String folderName = zipFileName.replace(".zip", "");
        Path destinationDir;
        try {
            destinationDir = prepareDirectory(localPath, folderName); // Prepare extraction directory
        } catch (IOException e) {
            String errorMsg = "Failed to prepare extraction directory for " + zipFilePath + ": " + e.getMessage();
            log.error(errorMsg);
            throw new IOException(errorMsg, e); // Fatal: cannot proceed without directory
        }

        try (ZipInputStream zis = new ZipInputStream(new FileInputStream(zipFilePath))) {
            ZipEntry zipEntry;
            while ((zipEntry = zis.getNextEntry()) != null) {
                Path extractedFilePath = destinationDir.resolve(zipEntry.getName()).normalize();
                File extractedFile = extractedFilePath.toFile();

                if (!extractedFilePath.startsWith(destinationDir)) {
                    String errorMsg = "Zip entry " + zipEntry.getName() + " attempts path traversal in " + zipFilePath;
                    log.error(errorMsg);
                    throw new IOException(errorMsg); // Fatal: security violation stops extraction
                }

                try {
                    if (zipEntry.isDirectory()) {
                        Files.createDirectories(extractedFilePath);
                        log.info("Directory created: {}", extractedFilePath);
                    } else {
                        Files.createDirectories(extractedFilePath.getParent());
                        try (FileOutputStream fos = new FileOutputStream(extractedFile)) {
                            byte[] buffer = new byte[1024];
                            int bytesRead;
                            while ((bytesRead = zis.read(buffer)) != -1) {
                                fos.write(buffer, 0, bytesRead);
                            }
                            log.info("Extracted: {}", extractedFilePath);
                        }
                    }
                } catch (IOException e) {
                    String errorMsg = "Failed to extract entry " + zipEntry.getName() + " from " + zipFilePath + ": "
                            + e.getMessage();
                    log.error(errorMsg);
                    // Non-fatal: log and continue with next entry
                    continue;
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            String errorMsg = "Error opening or reading ZIP file " + zipFilePath + ": " + e.getMessage();
            log.error(errorMsg);
            throw new IOException(errorMsg, e); // Fatal: cannot proceed if ZIP cannot be read
        }
    }

    @Override
    public Path prepareDirectory(String basePath, String targetDirName) throws IOException {
        Path targetPath = Paths.get(basePath, targetDirName).normalize();

        try {
            if (!Files.exists(targetPath)) {
                Files.createDirectories(targetPath);
                log.info("Target directory created: {}", targetPath);
            } else {
                log.info("Target directory already exists: {}", targetPath);
            }
            return targetPath;
        } catch (IOException e) {
            String errorMsg = "Failed to prepare directory " + targetPath + ": " + e.getMessage();
            log.error(errorMsg);
            throw new IOException(errorMsg, e); // Fatal: cannot proceed without directory
        }
    }
    }