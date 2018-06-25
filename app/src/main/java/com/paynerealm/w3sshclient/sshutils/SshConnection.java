package com.paynerealm.w3sshclient.sshutils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.KeyPair;
import com.jcraft.jsch.Session;

public class SshConnection {
    // jsch objects
    private JSch jsch;
    protected Channel channel;
    protected Session session;
    private SessionUserInfo userInfo;
    protected CONNECTION_STATE state;

    protected enum CONNECTION_STATE {
        CONNECTED,
        CONNECTING,
        DISCONNECTED
    }

    // SessionUserInfo not connected to UI at this point
    public SshConnection(SessionUserInfo user) {
        jsch = new JSch();
        // channel created by child class, can be terminal or file transfer channel
        channel = null;
        userInfo = user;
        state = CONNECTION_STATE.DISCONNECTED;

        try {
            session = jsch.getSession(userInfo.getUser(), userInfo.getHost(), userInfo.getPort());
            session.setHostKeyRepository(new FingerPrintRepository(jsch));
            session.setServerAliveInterval(10000);
        } catch (Exception e) {
            session = null;
        }
    }

    private boolean setupSession() {
        if (session == null) {
            return false;
        }

        boolean ret = true;
        if (userInfo.usingRSA()) {
            try {
                // load(alias, private, public, passphrase)
                KeyPair keyPair = KeyPair.load(jsch, userInfo.getRsa(), null);
                if (!keyPair.isEncrypted()) {
                    jsch.addIdentity(userInfo.getHost(), keyPair.forSSHAgent(), null, null);
                } else {
                    String passphrase = userInfo.promptInput("RSA Encrypted", "Please enter passphrase for key");
                    keyPair.decrypt(passphrase);
                }
            } catch (JSchException e) {
                userInfo.handleException(e);
                ret = false;
            }
        }
        session.setUserInfo(userInfo);
        return ret;
    }

    public boolean connect() {
        return setupSession();
    }

    public void disconnect() {
        if (state != CONNECTION_STATE.DISCONNECTED) {
            if (channel != null) {
                channel.disconnect();
            }
            if (session != null) {
                session.disconnect();
            }
            state = CONNECTION_STATE.DISCONNECTED;
        }
    }


    public void disableHostChecking() {
        java.util.Properties config = new java.util.Properties();
        config.put("StrictHostKeyChecking", "no");
        session.setConfig(config);
    }


    public boolean isConnected() {
        return state != CONNECTION_STATE.DISCONNECTED;
    }

    public String getName() {
        return userInfo.getHost();
    }

    public Channel getChannel() {
        return channel;
    }


    protected SessionUserInfo getUserInfo() {
        return userInfo;
    }

    protected Session getSession() {
        return session;
    }
}