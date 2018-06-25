/*
 * Copyright (c) 2017 Benjamin Raison
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.paynerealm.w3sshclient.old;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ScrollView;
import android.widget.TextView;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.paynerealm.w3sshclient.R;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.UnsupportedEncodingException;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    Session session;
    Output output;
    Channel channel;
    InputStream in = new PipedInputStream();
    PipedOutputStream pin;
    ScrollView sv;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        TextView textView = findViewById(R.id.txtOutput);
        EditText txtInput = findViewById(R.id.txtInput);
        sv = findViewById(R.id.scroll_view);
        Intent intent = getIntent();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(
                    getString(R.string.format_user, intent.getStringExtra("username"), intent.getStringExtra("server")));
        } else {
            Log.w(TAG, "SupportActionBar is null!");
        }
        ImageButton btnSend = findViewById(R.id.btnSend);
        btnSend.setOnClickListener(v -> {
            Log.d(TAG, "Clicked!: " + txtInput.getText().toString());
            if (!txtInput.getText().toString().trim().equals("")) {
                try {
                    pin.write(txtInput.getText().toString().trim().getBytes());
                    pin.write("\n".getBytes());
                    pin.flush();
                    txtInput.setText("");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        startConnection(intent, textView);
        sv.fullScroll(View.FOCUS_DOWN);
    }

    private void startConnection(Intent intent, TextView textView) {
        new Thread(() -> {
            JSch jsch = new JSch();
            output = new Output(textView, this);
            try {
                pin = new PipedOutputStream((PipedInputStream) in);
                session = jsch.getSession(intent.getStringExtra("username"), intent.getStringExtra("server"), 22);
                session.setConfig("StrictHostKeyChecking", "no");
                session.setPassword(intent.getStringExtra("password"));
                session.setOutputStream(output);
                session.connect();
                channel = session.openChannel("shell");
                channel.setInputStream(in);
                channel.setOutputStream(output);
                channel.connect();
                channel.start();
                if (session.isConnected()) {
                    Log.d(TAG, "Connected");
                }
            } catch (JSchException | IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    @Override
    public void onBackPressed() {
        channel.disconnect();
        session.disconnect();
        super.onBackPressed();
    }

}

class Output extends OutputStream {
    private TextView textView;
    private AppCompatActivity activity;
    private StringBuilder buffer = new StringBuilder(100);

    Output(TextView textView, AppCompatActivity activity) {
        this.textView = textView;
        this.activity = activity;
    }

    @Override
    public void write(int i) {
        buffer.append((char) i);
        if (buffer.length() > 99) {
//            String s = buffer.toString();
            try {
                byte[] ptext = buffer.toString().getBytes("UTF-8");
                String s = new String(ptext, "UTF-8");
                buffer = new StringBuilder(100);
                activity.runOnUiThread(() -> textView.append(s));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }

        }
    }
}