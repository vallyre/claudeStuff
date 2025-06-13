package com.mckesson.cmt.cmt_standardcode_gateway_service.file.handling;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

/**
 * 
 */
public interface FileSystemHandler {

    Path prepareTargetDirectory(String folderName,String rootFolder) throws IOException;
    void saveFile(InputStream inputStream, Path destinationPath) throws IOException;
    void extractZipFiles(String zipFilePath, String localPath) throws IOException;
    Path prepareDirectory(String basePath, String targetDirName) throws IOException;

}
