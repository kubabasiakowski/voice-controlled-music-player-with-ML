package com.example.kuba.musicplayer;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.RequiresApi;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class SongsOnPlaylistActivity extends AppCompatActivity {

    ListView songsOnPlaylistListView;
    Intent intent;
    private SharedPreferencesConfig preferencesConfig;
    DatabaseHelper dbHelper;
    ArrayList<Song> songListFromDB, songListFromStorage;

    ArrayList<Integer> selectedItemsIndexList = new ArrayList<>();
    boolean[] checkedSongs;
    String[] songNames;
    String[] songPaths;

    SongsOnPlaylistAdapter adapter;
    SongListAdapter adapterDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_songs_on_playlist);

        //inicializacja
        songsOnPlaylistListView = (ListView)findViewById(R.id.songsOnPlaylistListView);
        songListFromDB = new ArrayList<>();
        songListFromStorage = new ArrayList<>();
        dbHelper = new DatabaseHelper(this);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());

        //pobranie wartości przeslanych z poprzedniej aktywności
        Bundle bundle = getIntent().getExtras();
        songListFromDB = dbHelper.selectSongs(bundle.getInt("playlistId"));
        adapter = new SongsOnPlaylistAdapter(this, songListFromDB);
        songsOnPlaylistListView.setAdapter(adapter);

        //ustawienia dla toolbara
        toolbar.setTitle(dbHelper.getPlaylistName(bundle.getInt("playlistId")));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ChcekPermission();
        loadSongs();

        checkedSongs = new boolean[songListFromStorage.size()];
        songNames = new String[songListFromStorage.size()];
        songNames = getSongNames(songListFromStorage);
        songPaths = new String[songListFromStorage.size()];
        songPaths = getSongPaths(songListFromStorage);

        songsOnPlaylistListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(new Intent(getApplicationContext(), PlayerActivity.class)
                        .putExtra("songs", songListFromDB)
                        .putExtra("pos", position));
            }
        });
    }

    //implementacja opcji menu toolbara
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id== android.R.id.home){
            //konczy aktywnosc
            this.finish();
        }
        String msg=" ";
        switch (item.getItemId()){
            case R.id.add:
                addSongs();
                break;
            case R.id.logout:
                msg="Logout";
                userLogout();
                break;
        }
        if(msg != " ")
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    public void addSongs(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(SongsOnPlaylistActivity.this);


        builder.setTitle("Wybierz utwory");
        builder.setMultiChoiceItems(songNames, checkedSongs, new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                if(isChecked) {
                    if (!selectedItemsIndexList.contains(which)) {
                        selectedItemsIndexList.add(which);
                    } else if (selectedItemsIndexList.contains(which)) {

                    }
                }
            }
        });
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Bundle bundle = getIntent().getExtras();
                Song song = new Song();
                for(int i = 0; i < selectedItemsIndexList.size() ; i++) {
                    //dodanie do bazy
                    song.setName(songNames[selectedItemsIndexList.get(i)]);
                    song.setPath(songPaths[selectedItemsIndexList.get(i)]);
                    dbHelper.addSongsToPlaylist(bundle.getInt("playlistId"),song);
                }
                finish();
                startActivity(getIntent());
            }
        });
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //dodanie utworzonego menu do toolbara
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main,menu);
        return true;
    }

    //wylogowanie użytkownika
    public void userLogout(){
        preferencesConfig.writeLoginStatus(false);
        preferencesConfig.writeUserId(-1);
        Intent loginIntent = new Intent(SongsOnPlaylistActivity.this, LoginActivity.class);
        SongsOnPlaylistActivity.this.startActivity(loginIntent);
        finish();
    }

    //sprawdzenie zezwoleń
    private void ChcekPermission() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 123);
                return;
            }
        } else {
            loadSongs();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 123:
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //adapterDialog = new SongListAdapter(this, songListFromStorage);
                    //songsOnDialogListView.setAdapter(adapterDialog);
                } else {
                    Toast.makeText(this, "Permission deined", Toast.LENGTH_SHORT).show();
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

                    songListFromStorage.add(new Song(title, url));

                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public String[] getSongNames(final ArrayList<Song> songList){
        int length = songList.size();
        String[] songNames = new String[length];
        for(int i = 0;i<length;i++){
            songNames[i] = songList.get(i).getName();
        }
        return songNames;
    }

    public String[] getSongPaths(final ArrayList<Song> songList){
        int length = songList.size();
        String[] paths = new String[length];
        for(int i = 0;i<length;i++){
            paths[i] = songList.get(i).getPath();
        }
        return paths;
    }
}
