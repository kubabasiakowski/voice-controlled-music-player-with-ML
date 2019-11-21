package com.example.kuba.musicplayer;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.ListView;

import java.util.ArrayList;

import javax.crypto.Cipher;

public class RecognizedComandsActivity extends AppCompatActivity {

    private SharedPreferencesConfig preferencesConfig;
    ArrayList<Command> commands;
    RecognizedCommandsAdapter commandsAdapter;
    ListView commandsListView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recognized_comands);

        //inicjalizowanie elementow aktywnosci
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        commands = new ArrayList<>();
        commands.add(new Command("stop","Zatrzymanie odtwarzania"));
        commands.add(new Command("go","Wznowienie odtwarzania"));
        commands.add(new Command("forward","Przejście do następnego utworu"));
        commands.add(new Command("backward","Przejście do poprzedniego utworu"));
        commands.add(new Command("left","Cofnięcie odtwarzanego utworu o 10 sekund"));
        commands.add(new Command("right","Przyspieszenie odtwarzanego utworu o 10 sekund"));
        commands.add(new Command("off","Wyłączenie aplikacji"));

        commandsListView = (ListView) findViewById(R.id.commandsListView);

        commandsAdapter = new RecognizedCommandsAdapter(this, commands);
        commandsListView.setAdapter(commandsAdapter);

        //ustwaienia dla toolbara
        toolbar.setTitle("Lista poleceń głosowych");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());
    }

    //implementacja strzałki powrotu toolbara
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id== android.R.id.home){
            //konczy aktywnosc
            this.finish();
        }

        return super.onOptionsItemSelected(item);
    }
}
