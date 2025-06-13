package com.mckesson.cmt.cmt_standardcode_gateway_service.sftp.connection;


import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

/**
 * Interface for managing SFTP connections
 */
public interface SFTPConnectionManager {

    /**
     * Establishes a connection to the SFTP server and returns a session.
     *
     * @return the established JSch Session
     * @throws JSchException if there's an error connecting to the SFTP server
     */
    Session connect() throws JSchException;

    /**
     * Opens an SFTP channel using the provided session.
     *
     * @param session the JSch Session to use for opening the channel
     * @return the opened SFTP channel
     * @throws JSchException if there's an error opening the SFTP channel
     */
    ChannelSftp openChannel(Session session) throws JSchException;

    /**
     * Disconnects the provided session and SFTP channel.
     *
     * @param session     the JSch Session to disconnect
     * @param channelSftp the SFTP channel to disconnect
     */
    void disconnect(Session session, ChannelSftp channelSftp);
}