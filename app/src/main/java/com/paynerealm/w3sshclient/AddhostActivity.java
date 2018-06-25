package com.paynerealm.w3sshclient;

import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;

import com.paynerealm.w3sshclient.database.PrefDatabase;
import com.paynerealm.w3sshclient.models.Preference;
import com.rengwuxian.materialedittext.MaterialEditText;


public class AddhostActivity extends AppCompatActivity {
    private Preference pref;
    Preference addP = null;
    MaterialEditText edit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addhost);

        pref = getIntent().getParcelableExtra(Constants.PREFERENCE_PARCEABLE);

        if (pref != null) {
            fillForm();
        }

        AppCompatButton btn = findViewById(R.id.button_save);
        btn.setOnClickListener(v -> {
            edit = findViewById(R.id.connectionNameField);
            String name = edit.getText().toString();
            edit = findViewById(R.id.hostNameField);
            String host = edit.getText().toString();
            edit = findViewById(R.id.usernameField);
            String userName = edit.getText().toString();
            edit = findViewById(R.id.portField);
            String ports = null;
            if (!edit.getText().toString().equals(""))
                ports = edit.getText().toString();
            edit = findViewById(R.id.passwordField);
            String passwordOrKey = edit.getText().toString();

            if (ports != null) {
                if (!name.equals("") && !host.equals("") && !userName.equals("") && !ports.equals("") && !passwordOrKey.equals("")) {
                    Integer port = Integer.parseInt(ports);
                    addP = new Preference(passwordOrKey, name, host, userName, port);
                } else findError(name, host, userName, ports, passwordOrKey);
            }

            if (addP != null) {
                PrefDatabase.addPreference(addP, HomeActivity.sqLiteDatabase);
                finish();
            }
        });
    }


    private void fillForm() {
        MaterialEditText edit;
        edit = findViewById(R.id.hostNameField);
        edit.setText(pref.getHostName());
        edit = findViewById(R.id.usernameField);
        edit.setText(pref.getUsername());
        edit = findViewById(R.id.connectionNameField);
        edit.setText(pref.getName());
        edit = findViewById(R.id.passwordField);
        edit.setText(pref.getPassword());
        edit = findViewById(R.id.portField);
        edit.setText(String.valueOf(pref.getPort()));
    }

    private void findError(String name, String host, String userName, String ports, String passwordOrKey) {
        AlertDialog.Builder ab = new AlertDialog.Builder(AddhostActivity.this);
        ab.setTitle("Alert!!!");
        ab.setPositiveButton("OK", (dialog, which) -> dialog.dismiss());

        if (name.equals("")) {
            ab.setMessage("Please enter a Connection Name");
            ab.show();
        } else if (host.equals("")) {
            ab.setMessage("Please enter Host Name/IP");
            ab.show();
        } else if (userName.equals("")) {
            ab.setMessage("Please enter Username");
            ab.show();
        } else if (ports.equals("")) {
            ab.setMessage("Please enter Port No.(default 22):");
            ab.show();
        } else if (passwordOrKey.equals("")) {
            ab.setMessage("Please enter Password");
            ab.show();
        }
    }
}

