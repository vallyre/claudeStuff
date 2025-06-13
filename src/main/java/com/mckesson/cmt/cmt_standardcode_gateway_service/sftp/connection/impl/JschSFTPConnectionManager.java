package com.mckesson.cmt.cmt_standardcode_gateway_service.sftp.connection.impl;


import com.jcraft.jsch.*;
import com.mckesson.cmt.cmt_standardcode_gateway_service.sftp.connection.SFTPConnectionManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



public class JschSFTPConnectionManager implements SFTPConnectionManager {

    private static final Logger log = LoggerFactory.getLogger(JschSFTPConnectionManager.class);
    private final String host;
    private final String username;
    private final String password;

    public JschSFTPConnectionManager(String host, String username, String password) {
        if (host == null || host.trim().isEmpty()) {
            String errorMsg = "Host cannot be null or empty";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg); // Fatal: include message
        }
        if (username == null || username.trim().isEmpty()) {
            String errorMsg = "Username cannot be null or empty";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg); // Fatal: include message
        }
        if (password == null) {
            String errorMsg = "Password cannot be null";
            log.error(errorMsg);
            throw new IllegalArgumentException(errorMsg); // Fatal: include message
        }
        this.host = host.trim();
        this.username = username.trim();
        this.password = password;
    }

    @Override
    public Session connect() throws JSchException {
        try {
            JSch jsch = new JSch();
            Session session = jsch.getSession(username, host, 22);
            session.setPassword(password);
            session.setConfig("StrictHostKeyChecking", "no");
            session.connect();
            log.info("Connected to SFTP session successfully");
            return session;
        } catch (JSchException e) {
            String errorMsg = "Failed to connect to SFTP server " + e.getMessage();
            log.error(errorMsg);
            throw new JSchException(errorMsg, e); // Fatal: include detailed message
        }
    }

    @Override
    public ChannelSftp openChannel(Session session) throws JSchException {
        if (session == null || !session.isConnected()) {
            String errorMsg = "Cannot open SFTP channel: Session is null or not connected";
            log.error(errorMsg);
            throw new JSchException(errorMsg); // Fatal: include message
        }

        try {
            Channel channel = session.openChannel("sftp");
            ChannelSftp channelSftp = (ChannelSftp) channel;
            channelSftp.connect();
            log.info("SFTP channel opened successfully for session ");
            return channelSftp;
        } catch (JSchException e) {
            String errorMsg = "Failed to open SFTP channel for session " + e.getMessage();
            log.error(errorMsg);
            throw new JSchException(errorMsg, e); // Fatal: include detailed message
        }
    }

    @Override
    public void disconnect(Session session, ChannelSftp channelSftp) {
        // Disconnect channel
        if (channelSftp != null && channelSftp.isConnected()) {
            try {
                channelSftp.disconnect();
                log.info("SFTP channel disconnected successfully");
            } catch (Exception e) {
                String errorMsg = "Non-fatal error disconnecting SFTP channel: " + e.getMessage();
                log.warn(errorMsg);
                // Non-fatal: log only, no response modification here
            }
        } else {
            log.debug("SFTP channel already disconnected or null; skipping channel disconnect");
        }

        // Disconnect session
        if (session != null && session.isConnected()) {
            try {
                session.disconnect();
                log.info("SFTP session disconnected successfully");
            } catch (Exception e) {
                String errorMsg = "Non-fatal error disconnecting SFTP session: " + e.getMessage();
                log.warn(errorMsg);
                // Non-fatal: log only, no response modification here
            }
        } else {
            log.debug("SFTP session already disconnected or null; skipping session disconnect");
        }
    }

}
