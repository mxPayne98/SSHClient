package com.paynerealm.w3sshclient.sshutils;

import android.util.Log;

import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.ChannelShell;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

public class ShellConnection extends SshConnection {
    private PipedInputStream ip;
    private PipedOutputStream op;

    private ChannelShell channelShell;
    private ChannelExec channelExec;

    private final String log = "ShellConnection";

    public ShellConnection(SessionUserInfo user) {
        super(user);
        op = new PipedOutputStream();
        ip = new PipedInputStream();
        channelShell = null;
        channelExec = null;

        // give user object, alerts user if they want to reconnect
        getUserInfo().setConnectionHandler(this);
    }


    /*public static void executeCommand(String command) {
        try {
            pin.write(command.getBytes());
            pin.write("\n".getBytes());
            pin.flush();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }*/

    public boolean connect() {
        boolean ret = super.connect();
        if (!ret) {
            Log.d(log, "failed to connect...");
            return ret;
        }
        try {
            Session session = super.getSession();
            if ((session != null) && (state == CONNECTION_STATE.DISCONNECTED)) {
                Log.d(log, "SSH shell Connecting...");
                state = CONNECTION_STATE.CONNECTING;
                session.connect(5000);

//                session.setConfig("PreferredAuthentications", "publickey,keyboard-interactive,password");

                channel = session.openChannel("exec");

                channel.setInputStream(ip, true);
                channel.setOutputStream(op, true);
                channel.connect(5000);

                channelExec = (ChannelExec) channel;

                state = CONNECTION_STATE.CONNECTED;

                Log.d(log, "SSH shell Connected");
                ret = true;
            }
        } catch (JSchException e) {
            Log.e(log, "Exception caught while initiating shell connection", e);
            ret = false;
            state = CONNECTION_STATE.DISCONNECTED;
            getUserInfo().handleException(e);
        }
        return ret;
    }

    @Override
    public void disconnect() {
        super.disconnect();
        //need to call finish on terminal session
    }

    public void setPtySize(int col, int row, int px, int py) {
        if (channelExec != null) {
            Log.d(log, "setPtySize");
            channelExec.setPtySize(col, row, px, py);
        }
    }

    public void setPtyType(String type) {
        if (channelExec != null) {
            Log.d(log, "setPtyType: " + type);
            channelExec.setEnv("TERM", type);
            channelExec.setPtyType(type);
        }
    }

    public void setPty(boolean bool) {
        if (channelExec != null) {
            channelExec.setPty(bool);
        }
    }

    public String executeCommand(String command) {
        String ret = null;
        if (state != CONNECTION_STATE.CONNECTED) {
            return null;
        }
        try {
            //open channel ready to send input

            Log.d("Execute : ", command);

            ((ChannelExec) getChannel()).setCommand(command);

            StringBuilder stringBuilder = new StringBuilder();
            String line;
            BufferedReader br = new BufferedReader(new InputStreamReader(new PipedInputStream()));

            while ((line = br.readLine()) != null) {
                stringBuilder.append(line).append("\n");// append newline
            }

            //in.close();
            br.close();

            if (stringBuilder.length() > 0) {
                Log.d("Execute : ", "Return");
                ret = stringBuilder.toString();
            } else {
                ret = "...\n";
            }
        } catch (Exception e) {
            Log.d(log, e.getMessage());
        }
        disconnect();
        return ret;
    }

    public PipedInputStream getInputStream() {
        return ip;
    }

    public PipedOutputStream getOutputStream() {
        return op;
    }
}
