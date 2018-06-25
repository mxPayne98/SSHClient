package com.paynerealm.w3sshclient.terminal;

import android.content.Context;
import android.util.AttributeSet;

import com.paynerealm.w3sshclient.sshutils.ShellConnection;

import jackpal.androidterm.emulatorview.EmulatorView;

public class TerminalView extends EmulatorView {
    private ShellConnection connection;
    private int textSize;
    private String type;

    public TerminalView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    //called when orientation of screen changes
    public void copyOld(TerminalView old) {
        connection = old.getConnection();
        textSize = old.getTextSize();
    }

    public void addConnection(ShellConnection c) {
        connection = c;
    }

    public ShellConnection getConnection() {
        return connection;
    }

    public int getTextSize() {
        return textSize;
    }

    public void refreshScreen() {
        super.updateSize(true);
        updatePTY();
    }

    public void reduceSize() {
        textSize--;
        this.setTextSize(textSize);
    }

    public void increaseSize() {
        textSize++;
        this.setTextSize(textSize);
    }

    @Override
    public void setTextSize(int size) {
        textSize = size;
        //setTextSize calls updateSize(true) for us, no need to call again, just setPtySize
        super.setTextSize(size);
        updatePTY();
    }

    @Override
    protected void onSizeChanged(int paramInt1, int paramInt2, int paramInt3, int paramInt4) {
        refreshScreen();
        super.onSizeChanged(paramInt1, paramInt2, paramInt3, paramInt4);
    }

    @Override
    public void setTermType(String type) {
        super.setTermType(type);
        this.type = type;
        connection.setPty(true);
    }

    private void updatePTY() {
        if (connection != null) {
            connection.setPtyType(type);
            connection.setPtySize(getVisibleColumns(), getVisibleRows(), getWidth(), getHeight());
        }
    }

}
