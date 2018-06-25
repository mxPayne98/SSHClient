package com.paynerealm.w3sshclient.database;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.jcraft.jsch.HostKey;
import com.jcraft.jsch.JSchException;
import com.paynerealm.w3sshclient.models.HostKeys;
import com.paynerealm.w3sshclient.models.Preference;


import java.util.ArrayList;

public class PrefDatabase {
    public static String DATABASE_NAME = "ssh_db";
    public static String tablepref = "Preference";
    public static String tablehost = "HostKeys";

    public static ArrayList<Preference> getPrefList(SQLiteDatabase db) {
        Cursor c = db.rawQuery("select * from " + tablepref, null);
        ArrayList<Preference> pref_lis = new ArrayList<>();
        c.moveToFirst();
        while (!c.isAfterLast()) {
            pref_lis.add(new Preference(
                    c.getString(c.getColumnIndex("password")),
                    c.getString(c.getColumnIndex("connectionName")),
                    c.getString(c.getColumnIndex("hostName")),
                    c.getString(c.getColumnIndex("username")),
                    c.getInt(c.getColumnIndex("portNumber"))
            ));
            c.moveToNext();
        }
        return pref_lis;
    }

    public static void addHostkey(HostKeys h, SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + tablehost + "(fingerprint,key,type,hostName) VALUES ('" + h.getFingerprint() + "','" + h.getKey() + "','" + h.getType() + "','" + h.getHostName() + "')");
    }

    public static void addPreference(Preference p, SQLiteDatabase db) {
        db.execSQL("INSERT INTO " + tablepref + "(connectionName,hostName,username,password,portNumber) VALUES ('" + p.getName() + "','" + p.getHostName() + "','" + p.getUsername() + "','" + p.getPassword() + "','" + p.getPort() + "')");
    }

    public static HostKeys getHostKey(String name, SQLiteDatabase db) {
        HostKeys hostKeys = null;
        Cursor cursor = db.rawQuery("select * from " + tablehost + " where hostName = '" + name + "'", null);
        while (cursor.moveToNext()) {
            hostKeys = new HostKeys(cursor.getString(3), cursor.getString(0), cursor.getString(1), cursor.getString(2));
        }
        return hostKeys;
    }

    public static int fingercheck(String name, byte[] key, SQLiteDatabase db) {
        try {

            HostKey JcHost = new HostKey(name, key);
            HostKeys hostKeys;

            Cursor cursor = db.rawQuery("select * from " + tablehost + " where hostName = '" + name + "'", null);
            while (cursor.moveToNext()) {
                hostKeys = new HostKeys(cursor.getString(3), cursor.getString(0), cursor.getString(1), cursor.getString(2));
                boolean res = JcHost.getKey().equals(hostKeys.getKey());
                if (res)
                    return 2;
                else return 0;
            }
        } catch (JSchException e) {
            e.printStackTrace();
        }
        return 1;
    }

    public static void deletePreference(Preference p, SQLiteDatabase db) {
        String name = p.getName();
        String hname = p.getHostName();
        db.execSQL("DELETE FROM " + tablepref + " where connectionName = '" + name + "' AND hostName = '" + hname + "'");
    }

    public static void clearHostKeysTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + tablehost);
    }

    public static void clearConnectionsTable(SQLiteDatabase db) {
        db.execSQL("DELETE FROM " + tablepref);
    }
}
