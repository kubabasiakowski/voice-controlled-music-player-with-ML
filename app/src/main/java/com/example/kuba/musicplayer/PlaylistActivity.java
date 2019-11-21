package com.example.kuba.musicplayer;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class PlaylistActivity extends AppCompatActivity implements RecognitionListener {

    private SharedPreferencesConfig preferencesConfig;
    DatabaseHelper dbHelper;
    ListView listViewPlaylists;
    ArrayList<Playlist> playlistList;
    PlaylistListAdapter adapter;

    private Intent recognizerIntent;
    private SpeechRecognizer mySpeechRecognizer = null;
    private static final int REQUEST_RECORD_PERMISSION = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_playlist);

        AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistActivity.this);
        View view = getLayoutInflater().inflate(R.layout.add_playlist_dialog, null);

        //inicjalizowanie elementow aktywnosci
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        listViewPlaylists = (ListView) findViewById(R.id.playlistListView);
        playlistList = new ArrayList<>();

        //ustawienia dla toolbara
        toolbar.setTitle("Listy odtwarzania");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());
        dbHelper = new DatabaseHelper(this);

        playlistList = dbHelper.selectPlaylists(preferencesConfig.readUserId());
        adapter = new PlaylistListAdapter(this, playlistList);
        listViewPlaylists.setAdapter(adapter);

        listViewPlaylists.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(getApplicationContext(), SongsOnPlaylistActivity.class);
                Playlist tempPlaylist = (Playlist) adapter.getItem(position);
                intent.putExtra("playlistId", tempPlaylist.getId());
                startActivity(intent);
            }
        });

    } //koniec onCreate -----------

    @Override
    protected void onStop() {
        super.onStop();
        if(mySpeechRecognizer!=null){
            mySpeechRecognizer.destroy();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case REQUEST_RECORD_PERMISSION:
                if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    mySpeechRecognizer.startListening(recognizerIntent);
                } else {
                    Toast.makeText(PlaylistActivity.this, "Niezbędne jest pozwolenie na nagrywanie dźwięku.", Toast.LENGTH_SHORT).show();
                }
        }
    }

    // rozpoznawanie mowy
    public void startSpeechRecognition(){
        mySpeechRecognizer = SpeechRecognizer.createSpeechRecognizer(this);
        Log.i("PlayerActivity", "isRecognitionAvailable: " + SpeechRecognizer.isRecognitionAvailable(this));
        mySpeechRecognizer.setRecognitionListener(this);
        recognizerIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, "en");
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS,1);
        recognizerIntent.putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 10);
        mySpeechRecognizer.startListening(recognizerIntent);
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
            case R.id.search:
                msg="Wypowiedz nazwę szukanej playlisty";
                startSpeechRecognition();
                break;
            case R.id.add:
                msg="Add";
                addPlaylist();
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

    //dodanie utworzonego menu do toolbara
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_mic_with_add,menu);
        return true;
    }

    //wylogowanie użytkownika
    public void userLogout(){
        preferencesConfig.writeLoginStatus(false);
        preferencesConfig.writeUserId(-1);
        Intent loginIntent = new Intent(PlaylistActivity.this, LoginActivity.class);
        PlaylistActivity.this.startActivity(loginIntent);
        finish();
    }

    public void addPlaylist(){
        final AlertDialog.Builder builder = new AlertDialog.Builder(PlaylistActivity.this);
        View view = getLayoutInflater().inflate(R.layout.add_playlist_dialog, null);
        final EditText playlistName = (EditText) view.findViewById(R.id.editTextPlaylistName);
        Button btnAddPlaylist = (Button) view.findViewById(R.id.btnAddPlaylist);

        builder.setView(view);
        final AlertDialog dialog = builder.create();

        btnAddPlaylist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!playlistName.getText().toString().isEmpty()){
                    dbHelper.addPlaylist(playlistName.getText().toString(), preferencesConfig.readUserId());
                    Toast.makeText(PlaylistActivity.this,"Playlista dodana", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                    finish();
                    startActivity(getIntent());
                }
                else{
                    Toast.makeText(PlaylistActivity.this,"Podaj nazwę playlisty", Toast.LENGTH_SHORT).show();
                }
            }
        });


        dialog.show();
    }

    @Override
    public void onReadyForSpeech(Bundle params) {

    }

    @Override
    public void onBeginningOfSpeech() {

    }

    @Override
    public void onRmsChanged(float rmsdB) {

    }

    @Override
    public void onBufferReceived(byte[] buffer) {

    }

    @Override
    public void onEndOfSpeech() {

    }

    @Override
    public void onError(int error) {

    }

    @Override
    public void onResults(Bundle results) {
        ArrayList<String> matches = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        Playlist tempPlaylist = dbHelper.getPlaylistByNameAndUserId(matches.get(0).toLowerCase(),preferencesConfig.readUserId());
        if(tempPlaylist != null){
            Intent intent = new Intent(getApplicationContext(), SongsOnPlaylistActivity.class);
            intent.putExtra("playlistId", tempPlaylist.getId());
            startActivity(intent);
        } else {
            Toast.makeText(PlaylistActivity.this, "Playlista o nazwie: '" + matches.get(0) + "' nie istnieje", Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onPartialResults(Bundle partialResults) {

    }

    @Override
    public void onEvent(int eventType, Bundle params) {

    }
}
