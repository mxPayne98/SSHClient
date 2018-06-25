package com.paynerealm.w3sshclient.sshutils;

import android.util.Log;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.HostKeyRepository;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.UserInfo;
import com.paynerealm.w3sshclient.HomeActivity;
import com.paynerealm.w3sshclient.database.PrefDatabase;
import com.paynerealm.w3sshclient.models.HostKeys;


public class FingerPrintRepository implements HostKeyRepository {
    private JSch parameter;
    static private final String log = "FingerPrintRepository";

    FingerPrintRepository(JSch jsch) {
        parameter = jsch;
    }

    //if the host port is 22, host: domain, otherwise host: [domain]:6021
    public int check(String host, byte[] key) {
        return PrefDatabase.fingercheck(host, key, HomeActivity.sqLiteDatabase);
    }

    public void add(HostKey hostkey, UserInfo ui) {
        HostKeys host = new HostKeys(hostkey.getHost(), hostkey.getFingerPrint(parameter), hostkey.getKey(), hostkey.getType());
        PrefDatabase.addHostkey(host, HomeActivity.sqLiteDatabase);
    }

    public void remove(String host, String type) {
    }

    public void remove(String host, String type, byte[] key) {
    }

    public String getKnownHostsRepositoryID() {
        Log.d(log, "getKnownHostsRepositoryID");
        return null;
    }

    public HostKey[] getHostKey() {
        Log.d(log, "getHostKey");
        return null;
    }

    public HostKey[] getHostKey(String host, String type) {
        HostKey[] arrayOfHostKey = new HostKey[1];
        HostKeys hostKeys = PrefDatabase.getHostKey(host, HomeActivity.sqLiteDatabase);
        try {
            arrayOfHostKey[arrayOfHostKey.length - 1] = new HostKey(hostKeys.getHostName(), hostKeys.getKey().getBytes());
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return arrayOfHostKey;
    }
}