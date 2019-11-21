package com.example.kuba.musicplayer;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Checkable;
import android.widget.ListView;
import android.widget.Toast;

import java.io.File;
import java.util.ArrayList;

public class SongListActivity extends AppCompatActivity {

    String[] items;
    ListView songListView;
    private SharedPreferencesConfig preferencesConfig;
    DatabaseHelper dbHelper;
    ArrayList<Song> songList;
    SongListAdapter adapter;
    MediaPlayer mediaPlayer;
    public static final int MY_REQ_PERMISSION_EXT_STORAGE = 123;

    public static final int RUNTIME_PERMISSION_CODE = 7;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_song_list);

        //inicjalizowanie elementow aktywnosci
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        songListView = (ListView) findViewById(R.id.songListView);
        songList = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);
        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());
        mediaPlayer = new MediaPlayer();

        //ustwaienia dla toolbara
        toolbar.setTitle("Lista utworów");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ChcekPermission();
        loadSongs();

        adapter = new SongListAdapter(this, songList);
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                .putExtra("songs", songList)
                .putExtra("pos", position));

            }
        });

    }

    //implementacja opcji menu toolbara
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == android.R.id.home) {
            //konczy aktywnosc
            this.finish();
        }
        String msg = " ";
        switch (item.getItemId()) {
            case R.id.logout:
                msg = "Logout";
                userLogout();
                break;
        }
        if(msg != " ")
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    //dodanie utworzonego menu do toolbara
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_without_add, menu);
        return true;
    }

    //wylogowanie użytkownika
    public void userLogout() {
        preferencesConfig.writeLoginStatus(false);
        preferencesConfig.writeUserId(-1);
        Intent loginIntent = new Intent(SongListActivity.this, LoginActivity.class);
        SongListActivity.this.startActivity(loginIntent);
        finish();
    }


    //sprawdzenie zezwoleń
    private void ChcekPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, MY_REQ_PERMISSION_EXT_STORAGE);
                return;
            }
        } else {
            loadSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_REQ_PERMISSION_EXT_STORAGE:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    adapter = new SongListAdapter(this, songList);
                    songListView.setAdapter(adapter);
                } else {
                    Toast.makeText(this, "Aplikacja nie będzie działała poprawnie.", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    private void loadSongs() {
        Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String selection = MediaStore.Audio.Media.IS_MUSIC + "!=0";
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);

        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    String name = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
                    String artist = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.ARTIST));
                    String url = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.DATA));
                    String title = cursor.getString(cursor.getColumnIndex(MediaStore.Audio.Media.TITLE));

                    songList.add(new Song(title, url));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

}
