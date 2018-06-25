package com.paynerealm.w3sshclient.asyncNetworkTasks;

import android.os.AsyncTask;
import android.util.Log;

import com.paynerealm.w3sshclient.sshutils.SshConnection;

public class SshConnectTask extends AsyncTask<SshConnection, Integer, Boolean> {
    private IConnectionNotifier handler;
    private SshConnection connection;

    private final String log = "SshConnectTask";

    public SshConnectTask(IConnectionNotifier caller) {
        handler = caller;
        connection = null;
    }

    protected Boolean doInBackground(SshConnection... connection) {
        Boolean ret = false;
        try {
            this.connection = connection[0];
            if (!this.connection.isConnected()) {
                ret = this.connection.connect();
            }
        } catch (Exception e) {
            Log.e(log, "doInBackground exception", e);
        }
        return ret;
    }

    protected void onPostExecute(Boolean result) {
        handler.connectionResult(result);
    }

}
