package com.paynerealm.w3sshclient.syncDialogs;

import android.app.Activity;

import com.paynerealm.w3sshclient.sshutils.SessionUserInfo;

public class AlertDialog implements BlockingOnUIRunnableListener {
    Activity _activityParent;
    String _message;
    DialogResponse _userResponse;

    public AlertDialog(Activity parent, String msg, SessionUserInfo handle) {
        _message = msg;
        _activityParent = parent;
        _userResponse = new DialogResponse();
    }

    public DialogResponse getDialogResponse() {
        return _userResponse;
    }

    public void onRunOnUIThread(final Runnable runnable) {
        android.app.AlertDialog alert = new android.app.AlertDialog.Builder(_activityParent).setMessage(_message)
                .setNegativeButton("No", (dialog, which) -> {
                    dialog.dismiss();
                    _userResponse.responseBoolean = false;
                    synchronized (runnable) {
                        runnable.notifyAll();
                    }
                })
                .setPositiveButton("Yes", (dialog, which) -> {
                    dialog.dismiss();
                    _userResponse.responseBoolean = true;
                    synchronized (runnable) {
                        runnable.notifyAll();
                    }
                })
                .create();
        alert.setCanceledOnTouchOutside(false);
        alert.show();
    }
}