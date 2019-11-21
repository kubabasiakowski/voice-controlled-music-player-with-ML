package com.example.kuba.musicplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.support.multidex.MultiDex;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.AndroidCharacter;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    DatabaseHelper dbHelper;
    SharedPreferencesConfig preferencesConfig;

    TextView textViewHello;
    Button btnSongs,btnPlaylists,btnCommands;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        MultiDex.install(this);
        //inicjalizacja elementów aktywności
        textViewHello = (TextView) findViewById(R.id.textViewWitaj);
        btnSongs = (Button) findViewById(R.id.btnSongs);
        btnPlaylists = (Button) findViewById(R.id.btnPlaylists);
        btnCommands = (Button) findViewById(R.id.btnCommands);

        dbHelper = new DatabaseHelper(this);
        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());

        if(preferencesConfig.readLoginStatus()==false){
            Intent loginIntent = new Intent(MainActivity.this, LoginActivity.class);
            MainActivity.this.startActivity(loginIntent);
            finish();
        }

        //wczytanie zalogowanego uzytkownika
        user = dbHelper.selectUser(preferencesConfig.readUserId());

        textViewHello.setText("Witaj " + user.getName());

        btnSongs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent songsIntent = new Intent(MainActivity.this, SongListActivity.class);
                MainActivity.this.startActivity(songsIntent);
            }
        });

        btnPlaylists.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent playlistsIntent = new Intent(MainActivity.this, PlaylistActivity.class);
                MainActivity.this.startActivity(playlistsIntent);
            }
        });

        btnCommands.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent commandsIntent = new Intent(MainActivity.this, RecognizedComandsActivity.class);
                MainActivity.this.startActivity(commandsIntent);
            }
        });
    }

}


