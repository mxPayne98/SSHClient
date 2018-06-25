package com.paynerealm.w3sshclient.sshutils;

import android.app.Activity;
import android.util.Log;

import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;
import com.paynerealm.w3sshclient.syncDialogs.AlertBox;
import com.paynerealm.w3sshclient.syncDialogs.AlertDialog;
import com.paynerealm.w3sshclient.syncDialogs.AlertInput;
import com.paynerealm.w3sshclient.syncDialogs.BlockingOnUIRunnable;


public class SessionUserInfo implements UserInfo, UIKeyboardInteractive {

    private String myPassword;
    private String myRsa;
    private final String myUser;
    private final String myHost;
    private final int myPort;

    private Activity parent;

    private final String log = "SessionUserInfo";

    private SshConnection connection;

    public SessionUserInfo(String host, String user, int port, Activity ac) {
        myHost = host;
        myUser = user;
        myPort = port;
        parent = ac;
        connection = null;
        myPassword = null;
    }

    public String getRsa() {
        return myRsa;
    }

    public boolean usingRSA() {
        return ((myPassword == null) && (myRsa != null));
    }

    public void setPassword(String pass) {
        myPassword = pass;
        myRsa = null;
    }

    public int getPort() {
        return myPort;
    }

    public void setConnectionHandler(SshConnection c) {
        connection = c;
    }

    @Override
    public String[] promptKeyboardInteractive(String destination, String name, String instruction,
                                              String[] prompt, boolean[] echo) {
        Log.d(log, "prompt keyboard");
        return null;
    }

    public String promptInput(String title, String promptMessage) {
        AlertInput alert = new AlertInput(parent, title, promptMessage);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();
        return alert._userResponse.responseString;
    }

    public void handleException(JSchException paramJSchException) {
        String error = paramJSchException.getMessage();

        if (paramJSchException.getMessage().contains("reject HostKey")) {
            error = "You have rejected the server's fingerprint";
        } else if (paramJSchException.getMessage().contains("UnknownHostKey")) {
            error = "Unknown Host Key";
        } else if (paramJSchException.getMessage().contains("HostKey has been changed")) {
            error = "Host key has been changed";
        } else if (paramJSchException.getMessage().contains("Auth fail")) {
            error = "authentication failed, check username/password";
        } else if (paramJSchException.getMessage().contains("Auth cancel")) {
            error = "Authentication has been cancelled";
        } else if (paramJSchException.getMessage().contains("socket is not established")) {
            error = "Unable to establish connection, check the host's IP and your connection.";
        } else if (paramJSchException.getMessage().contains("Too many authentication")) {
            error = "Too many incorrect authentications with this user";
        } else if (paramJSchException.getMessage().contains("Connection refused")) {
            error = "Connection refused";
        } else if (paramJSchException.getMessage().contains("Unable to resolve host") || paramJSchException.getMessage().contains("Network is unreachable")) {
            error = "Unable to establish connection with the host, retry?";
            if (connection != null) {
                AlertDialog alert = new AlertDialog(parent, error, this);
                BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
                actionRunnable.startOnUiAndWait();
                if (alert.getDialogResponse().responseBoolean) {
                    connection.connect();
                } else {
                    parent.finish();
                }
                return;
            } else {
                error = "Unable to establish connection with the host.";
            }
        }
        showMessage("Error", error);
    }

    //abstract methods
    @Override
    public String getPassphrase() {
        Log.d(log, "getPassphrase");
        //log here that we returned the password
        return myPassword;
    }

    public void showMessage(String title, String message) {
        AlertBox alert = new AlertBox(parent, title, message);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();
    }

    @Override
    public void showMessage(String message) {
        Log.d(log, "showMessage:" + message);
        showMessage("", message);
    }


    @Override
    //return true, password is retreived through getPassword()
    public boolean promptPassword(String message) {
        if (myPassword != null) {
            return true;
        }
        AlertInput alert = new AlertInput(parent, "Enter password", message);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();
        if (alert._userResponse.responseBoolean) {
            myPassword = alert._userResponse.responseString;
        }
        return alert._userResponse.responseBoolean;
    }

    @Override
    public boolean promptPassphrase(String message) {
        Log.d(log, "promptPassphrase" + message);
        // TODO
        return true;
    }

    @Override
    public boolean promptYesNo(String message) {
        AlertDialog alert = new AlertDialog(parent, message, this);
        BlockingOnUIRunnable actionRunnable = new BlockingOnUIRunnable(parent, alert);
        actionRunnable.startOnUiAndWait();

        return alert.getDialogResponse().responseBoolean;
    }

    public String getUser() {
        return myUser;
    }

    public String getHost() {
        return myHost;
    }

    public String getPassword() {
        return myPassword;
    }
}