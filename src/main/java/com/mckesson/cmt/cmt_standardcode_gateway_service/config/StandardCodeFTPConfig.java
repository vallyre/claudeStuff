package com.mckesson.cmt.cmt_standardcode_gateway_service.config;

import org.springframework.context.annotation.Bean;
import com.mckesson.cmt.cmt_standardcode_gateway_service.file.handling.FileSystemHandler;
import com.mckesson.cmt.cmt_standardcode_gateway_service.file.handling.LocalFileSystemHandler;
import com.mckesson.cmt.cmt_standardcode_gateway_service.sftp.connection.SFTPConnectionManager;
import com.mckesson.cmt.cmt_standardcode_gateway_service.sftp.connection.impl.JschSFTPConnectionManager;

import org.springframework.beans.factory.annotation.Value;

//@Configuration
public class StandardCodeFTPConfig {

     @Bean
    public SFTPConnectionManager sftpConnectionManager(
            @Value("${standcode.gateway.host}") String host,
            @Value("${standcode.gateway.username}") String username,
            @Value("${standcode.gateway.password}") String password) {
        return new JschSFTPConnectionManager(host, username, password);
    }

    @Bean
    public FileSystemHandler fileSystemHandler(
            @Value("${standcode.gateway.localDirectory}") String localPath) {
        return new LocalFileSystemHandler(localPath);
    }

}
