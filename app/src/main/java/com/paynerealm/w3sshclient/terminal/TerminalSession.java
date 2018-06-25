package com.paynerealm.w3sshclient.terminal;


import com.paynerealm.w3sshclient.sshutils.ShellConnection;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import jackpal.androidterm.emulatorview.TermSession;

public class TerminalSession extends TermSession {
    private ShellConnection connection;

    public TerminalSession(ShellConnection connection) {
        this.connection = connection;
        PipedInputStream ip;
        PipedOutputStream op;
        try {
            ip = new PipedInputStream(connection.getOutputStream());
            op = new PipedOutputStream(connection.getInputStream());
            //setTerminalStuff Here
            setTermIn(ip);
            setTermOut(op);
        } catch (Exception e) {
            // Log.d(log, "Exception caught while creating jsch session" + e.getMessage());
        }
    }

    public ShellConnection getConnection() {
        return connection;
    }

    @Override //called when data is processed from the input stream
    public void processInput(byte[] buffer, int offset, int count) {

        super.processInput(buffer, offset, count);
    }
}
