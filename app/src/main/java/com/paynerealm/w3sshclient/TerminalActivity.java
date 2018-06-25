package com.paynerealm.w3sshclient;

import android.app.FragmentManager;
import android.app.Service;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import com.paynerealm.w3sshclient.asyncNetworkTasks.IConnectionNotifier;
import com.paynerealm.w3sshclient.asyncNetworkTasks.SshConnectTask;
import com.paynerealm.w3sshclient.models.Preference;
import com.paynerealm.w3sshclient.sshutils.SessionUserInfo;
import com.paynerealm.w3sshclient.sshutils.ShellConnection;
import com.paynerealm.w3sshclient.sshutils.SshConnection;
import com.paynerealm.w3sshclient.terminal.TerminalSession;
import com.paynerealm.w3sshclient.terminal.TerminalView;


public class TerminalActivity extends AppCompatActivity implements IConnectionNotifier {

    private SshConnection connection;
    private boolean keyboardShown;

    private SharedPreferencesManager prefInstance;

    public static TerminalSession terminalSession;
    private TerminalView view;

    private RetainedTerminal retainedTerminal;
    final static String log = "TerminalActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_terminal);

        prefInstance = SharedPreferencesManager.getInstance();
        keyboardShown = false;
        LinearLayout commandLayout = findViewById(R.id.commandLayout);
        commandLayout.setVisibility(View.GONE);

        FragmentManager fm = getFragmentManager();
        String ter = "terminal";
        retainedTerminal = (RetainedTerminal) fm.findFragmentByTag(ter);

        if (retainedTerminal != null) {
            terminalSession = retainedTerminal.getTerminalSession();
            view = findViewById(R.id.emulatorView);
            view.copyOld(retainedTerminal.getTerminalView());
            connection = terminalSession.getConnection();

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            view.setDensity(metrics);

            view.attachSession(terminalSession);
            connectionResult(true);
        } else {
            view = findViewById(R.id.emulatorView);

            retainedTerminal = new RetainedTerminal();
            fm.beginTransaction().add(retainedTerminal, ter).commit();

            DisplayMetrics metrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(metrics);
            view.setDensity(metrics);

            Intent intent = getIntent();
            Preference p = intent.getParcelableExtra(Constants.PREFERENCE_PARCEABLE);

            SessionUserInfo user = new SessionUserInfo(p.getHostName(), p.getUsername(), p.getPort(), this);
            user.setPassword(p.getPassword());

            ShellConnection connection = null;

            connection = new ShellConnection(user);

            terminalSession = new TerminalSession(connection);
            view.attachSession(terminalSession);

            retainedTerminal.setTerminalSession(terminalSession);
            retainedTerminal.setTerminalView(view);

            this.connection = connection;
            //give the connection to the view so he can update Pty size on the fly
            view.addConnection(connection);

            //view.setUseCookedIME(true);

            prefInstance.setPreferencesonShellConnection(this.connection);
            prefInstance.setPreferencesTerminal(view);
            prefInstance.setPreferenceSession(terminalSession);

            SshConnectTask task = new SshConnectTask(this);
            task.execute(this.connection);
            /*EditText txtCommand = findViewById(R.id.txtCommand);
            ImageButton btnCommand = findViewById(R.id.btnCommand);
            btnCommand.setOnClickListener(v -> {
                Log.d("Command : ", "Clicked!: " + txtCommand.getText().toString());
                if (!txtCommand.getText().toString().trim().equals("")) {
                    executeCommand(txtCommand.getText().toString().trim());
                    txtCommand.setText("");
                }
            });*/
        }

        Button Ctrl = findViewById(R.id.Ctrl);
        setupControlButton(Ctrl);

        Button Tab = findViewById(R.id.tab);
        setupTabButton(Tab);

        Button Esc = findViewById(R.id.esc);
        setupEscButton(Esc);

        ImageButton leftButton = findViewById(R.id.leftButton);
        setupLeftButton(leftButton);

        ImageButton rightButton = findViewById(R.id.rightButton);
        setupRightButton(rightButton);

        ImageButton upButton = findViewById(R.id.upButton);
        setupUpButton(upButton);

        ImageButton keyboardButton = findViewById(R.id.keyboardButton);
        setupKeyboardButton(keyboardButton);


    }

    @Override
    public void onBackPressed() {
        if (!connection.isConnected()) {
            try {
                //kill the connecting thread
                if (connection != null) {
                    connection.disconnect();
                }
                super.onBackPressed();
            } catch (Exception e) {
                Log.d(log, "exception while disconnecting");
            }
        } else {
            AlertDialog.Builder ab = new AlertDialog.Builder(TerminalActivity.this);
            ab.setMessage("Are you sure you would like to disconnect?");
            ab.setTitle("Disconnect");
            ab.setPositiveButton("YES", (dialog, which) -> {
                connection.disconnect();
                finish();
            });
            ab.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
            ab.show();
        }
    }

    @Override
    public void connectionResult(boolean result) {
        if (!result) {
            setTitle("Error...");
            return;
        }
        setTitle(connection.getName());
        view.refreshScreen();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        boolean ret = true;
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            view.increaseSize();
        } else if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            view.reduceSize();
        } else {
            ret = super.onKeyDown(keyCode, event);
        }
        return ret;
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        boolean ret = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_VOLUME_UP:
                break;
            case KeyEvent.KEYCODE_VOLUME_DOWN:
                break;
            default:
                ret = super.onKeyUp(keyCode, event);
                break;
        }
        return ret;
    }

    private void setupControlButton(Button ctrl) {
        ctrl.setOnClickListener(v -> view.sendControlKey());
    }

    private void setupEscButton(Button esc) {
        esc.setOnClickListener(v -> terminalSession.write(Constants.ESC));
    }

    private void setupTabButton(Button tab) {
        tab.setOnClickListener(v -> terminalSession.write(Constants.TAB));
    }

    private void setupUpButton(ImageButton upButton) {
        upButton.setOnClickListener(v -> terminalSession.write(Constants.UP));
    }

    private void setupLeftButton(ImageButton leftButton) {
        leftButton.setOnClickListener(v -> terminalSession.write(Constants.LEFT));
    }

    private void setupRightButton(ImageButton rightButton) {
        rightButton.setOnClickListener(v -> terminalSession.write(Constants.RIGHT));
    }

    private void setupKeyboardButton(final ImageButton keyboard) {
        keyboard.setOnClickListener(v -> handleKeyboard());
    }

    private void handleKeyboard() {
        final InputMethodManager inputMethodManager = (InputMethodManager) this.getSystemService(Service.INPUT_METHOD_SERVICE);
        if (keyboardShown) {
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
            keyboardShown = false;
        } else {
            inputMethodManager.showSoftInput(view, 0);
            keyboardShown = true;
        }
        view.updateSize(true);
        view.refreshScreen();
    }
}
