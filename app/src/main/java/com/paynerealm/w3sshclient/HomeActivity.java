package com.paynerealm.w3sshclient;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.paynerealm.w3sshclient.adapters.MyAdapter;
import com.paynerealm.w3sshclient.adapters.RecyclerItemClickListener;
import com.paynerealm.w3sshclient.database.PrefDatabase;
import com.paynerealm.w3sshclient.models.Preference;

import static com.paynerealm.w3sshclient.database.PrefDatabase.getPrefList;
import static com.paynerealm.w3sshclient.database.PrefDatabase.tablehost;
import static com.paynerealm.w3sshclient.database.PrefDatabase.tablepref;

public class HomeActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private RecyclerView.Adapter recyclerViewAdapter;
    private RecyclerView.LayoutManager layoutManager;
    public static SQLiteDatabase sqLiteDatabase;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        SharedPreferencesManager.init(this);
        SharedPreferencesManager.getInstance();

        sqLiteDatabase = openOrCreateDatabase(PrefDatabase.DATABASE_NAME, MODE_PRIVATE, null);
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + tablepref + "(connectionName VARCHAR,hostName VARCHAR,username VARCHAR,password VARCHAR,portNumber VARCHAR)");
        sqLiteDatabase.execSQL("CREATE TABLE IF NOT EXISTS " + tablehost + "(fingerprint VARCHAR,key VARCHAR,type VARCHAR,hostName VARCHAR)");

        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(view -> {
            Intent intent = new Intent(HomeActivity.this, AddhostActivity.class);
            startActivity(intent);
        });

        final Cursor cursor = sqLiteDatabase.rawQuery("select * from " + tablepref, null);

        //Configure RecyclerView
        recyclerView = findViewById(R.id.recycler_view_users);
        recyclerView.setHasFixedSize(true);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);

        //Configure custom Adapter
        recyclerViewAdapter = new MyAdapter(getPrefList(sqLiteDatabase), this);
        recyclerView.setAdapter(recyclerViewAdapter);
        recyclerView.addOnItemTouchListener(new RecyclerItemClickListener(this,
                new RecyclerItemClickListener.OnItemClickListener() {
                    //Short press to bring up user information editing interface
                    @Override
                    public void onItemClick(View view, int position) {
                        cursor.moveToPosition(position);
                        Preference preference = new Preference(
                                cursor.getString(cursor.getColumnIndex("password")),
                                cursor.getString(cursor.getColumnIndex("connectionName")),
                                cursor.getString(cursor.getColumnIndex("hostName")),
                                cursor.getString(cursor.getColumnIndex("username")),
                                cursor.getInt(cursor.getColumnIndex("portNumber"))
                        );
                        connectToPreference(preference);
                    }

                    @Override
                    public void onLongClick(View view, int position) {

                    }

                }));

    }

    private void connectToPreference(Preference p) {
        Intent intent = new Intent(this, TerminalActivity.class);
        intent.putExtra(Constants.PREFERENCE_PARCEABLE, (Parcelable) p);
        startActivity(intent);
    }


    @Override
    public void onBackPressed() {
        AlertDialog.Builder ab = new AlertDialog.Builder(HomeActivity.this);
        ab.setMessage("Are you sure you want to exit?");
        ab.setTitle("Exit");
        ab.setPositiveButton("Yes", (dialog, which) -> {
            finish();
            HomeActivity.super.onBackPressed();
        });
        ab.setNegativeButton("No", (dialog, which) -> dialog.dismiss());
        ab.show();
    }

}
