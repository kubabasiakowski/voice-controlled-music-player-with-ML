package com.example.kuba.musicplayer;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.PorterDuff;
import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.support.v7.widget.Toolbar;
import android.widget.ToggleButton;

import org.tensorflow.contrib.android.TensorFlowInferenceInterface;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.locks.ReentrantLock;
//import org.tensorflow.lite.Interpreter;
//import org.tensorflow.lite.TensorFlowLite;

public class PlayerActivity extends AppCompatActivity {

    // Constants that control the behavior of the recognition code and model
    // settings. See the audio recognition tutorial for a detailed explanation of
    // all these, but you should customize them to match your training settings if
    // you are running your own model.
    private static final int SAMPLE_RATE = 16000;
    private static final int SAMPLE_DURATION_MS = 1000;
    private static final int RECORDING_LENGTH = (int) (SAMPLE_RATE * SAMPLE_DURATION_MS / 1000);
    private static final long AVERAGE_WINDOW_DURATION_MS = 500;
    private static final float DETECTION_THRESHOLD = 0.70f;
    private static final int SUPPRESSION_MS = 1500;
    private static final int MINIMUM_COUNT = 3;
    private static final long MINIMUM_TIME_BETWEEN_SAMPLES_MS = 30;
    private static final String LABEL_FILENAME = "file:///android_asset/labels.txt";
    private static final String MODEL_FILENAME = "file:///android_asset/frozen_conv_18000.pb";
    private static final String INPUT_DATA_NAME = "decoded_sample_data:0";
    private static final String SAMPLE_RATE_NAME = "decoded_sample_data:1";
    private static final String OUTPUT_SCORES_NAME = "labels_softmax";

    // UI elements.
    private static final int REQUEST_RECORD_AUDIO = 13;
    private static final String LOG_TAG = PlayerActivity.class.getSimpleName();

    // Working variables.
    short[] recordingBuffer = new short[RECORDING_LENGTH];
    int recordingOffset = 0;
    boolean shouldContinue = true;
    private Thread recordingThread;
    boolean shouldContinueRecognition = true;
    private Thread recognitionThread;
    private final ReentrantLock recordingBufferLock = new ReentrantLock();
    private TensorFlowInferenceInterface inferenceInterface;
    private List<String> labels = new ArrayList<String>();
    private List<String> displayedLabels = new ArrayList<>();
    private RecognizeCommands recognizeCommands = null;

    //interface elements
    TextView songTitleTextView, elapsedTimeTextView, remainingTimeTextView;
    Button btnNext, btnPrevious, btnPausePlay;
    SeekBar seekBar;
    Toolbar toolbar;
    private SharedPreferencesConfig preferencesConfig;
    ToggleButton tglBtnSpeek;

    static MediaPlayer mediaPlayer;
    int totalTime;
    int position;
    ArrayList<Song> songList;

    @SuppressLint("NewApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player);

        // Load the labels for the model, but only display those that don't start
        // with an underscore.
        String actualFilename = LABEL_FILENAME.split("file:///android_asset/")[1];
        Log.i(LOG_TAG, "Reading labels from: " + actualFilename);
        BufferedReader br = null;
        try {
            br = new BufferedReader(new InputStreamReader(getAssets().open(actualFilename)));
            String line;
            while ((line = br.readLine()) != null) {
                labels.add(line);
                if (line.charAt(0) != '_') {
                    displayedLabels.add(line.substring(0, 1).toUpperCase() + line.substring(1));
                }
            }
            br.close();
        } catch (IOException e) {
            throw new RuntimeException("Problem reading label file!", e);
        }

        //Inicializacja obiektu RecognizeCommands
        recognizeCommands =
                new RecognizeCommands(
                        labels,
                        AVERAGE_WINDOW_DURATION_MS,
                        DETECTION_THRESHOLD,
                        SUPPRESSION_MS,
                        MINIMUM_COUNT,
                        MINIMUM_TIME_BETWEEN_SAMPLES_MS);

        inferenceInterface = new TensorFlowInferenceInterface(getAssets(), MODEL_FILENAME);

        // Start the recording and recognition threads.
        requestMicrophonePermission();
        startRecording();
        startRecognition();

//        AudioManager myAudioManager = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
//        myAudioManager.setStreamMute(AudioManager.STREAM_MUSIC, true);

        //inicializacja elementów
        preferencesConfig = new SharedPreferencesConfig(getApplicationContext());
        songTitleTextView = (TextView) findViewById(R.id.songTitle);
        elapsedTimeTextView = (TextView) findViewById(R.id.elapsedTimeTextView);
        remainingTimeTextView = (TextView) findViewById(R.id.remainingTimeTextView);
        btnNext = (Button) findViewById(R.id.btnNext);
        btnPrevious = (Button) findViewById(R.id.btnPrevious);
        btnPausePlay = (Button) findViewById(R.id.btnPause);
        seekBar = (SeekBar) findViewById(R.id.seekBar);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        tglBtnSpeek = (ToggleButton) findViewById(R.id.toggleBtnSpeak);

        //ustawienia toolbara
        toolbar.setTitle("Odtwarzacz");
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //pobranie wartości z poprzedniej aktywności
        Intent i = getIntent();
        Bundle bundle = i.getExtras();
        songList = (ArrayList<Song>) bundle.get("songs");
        position = bundle.getInt("pos");

        String songTitle = songList.get(position).getName();
        songTitleTextView.setText(songTitle);
        final Uri uri = Uri.parse(songList.get(position).getPath());

        //odtwarzanie
        mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
        mediaPlayer.seekTo(0);
        totalTime = mediaPlayer.getDuration();
        mediaPlayer.start();

        //seekBar
        seekBar.setMax(mediaPlayer.getDuration());
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (fromUser) {
                    mediaPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //update seekBar /Thread
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (mediaPlayer != null) {
                    try {
                        Message msg = new Message();
                        msg.what = mediaPlayer.getCurrentPosition();
                        handler.sendMessage(msg);
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                    } catch (IllegalStateException e){
                        mediaPlayer.reset();
                        mediaPlayer.getCurrentPosition();
                    }
                }
            }
        }).start();


        btnPausePlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                seekBar.setMax(mediaPlayer.getDuration());

                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                    btnPausePlay.setBackgroundResource(R.drawable.ic_play);
                } else {
                    mediaPlayer.start();
                    btnPausePlay.setBackgroundResource(R.drawable.ic_pause);
                }
            }
        });

        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //zatrzymanie i zwolnienie odtwarzania
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();

                //zmiana pozycji listy
                position = (position + 1) % songList.size();
                //odswiezenie danych utworu (sciezka, nazwa, dlugosc)
                Uri uri = Uri.parse(songList.get(position).getPath().toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songTitleTextView.setText(songList.get(position).getName());
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
            }
        });

        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //zatrzymanie i zwolnienie odtwarzania
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();

                //zmiana pozycji listy
                position = ((position - 1) < 0) ? (songList.size() - 1) : (position - 1);
                //odswiezenie danych utworu (sciezka, nazwa, dlugosc)
                Uri uri = Uri.parse(songList.get(position).getPath().toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songTitleTextView.setText(songList.get(position).getName());
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
            }
        });

        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                mediaPlayer.stop();
                mediaPlayer.reset();
                mediaPlayer.release();
                //zmiana pozycji listy
                position = (position + 1) % songList.size();
                //odswiezenie danych utworu (sciezka, nazwa, dlugosc)
                Uri uri = Uri.parse(songList.get(position).getPath().toString());
                mediaPlayer = MediaPlayer.create(getApplicationContext(), uri);
                songTitleTextView.setText(songList.get(position).getName());
                seekBar.setMax(mediaPlayer.getDuration());

                mediaPlayer.start();
            }
        });
    }    //koniec onCreate ---------------------------

    private void requestMicrophonePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(
                    new String[] {android.Manifest.permission.RECORD_AUDIO}, REQUEST_RECORD_AUDIO);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == REQUEST_RECORD_AUDIO
                && grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startRecording();
            startRecognition();
        } else {
            Toast.makeText(this, "Aplikacja nie będzie działała poprawnie.", Toast.LENGTH_SHORT).show();
        }
    }

    public synchronized void startRecording() {
        if (recordingThread != null) {
            return;
        }
        shouldContinue = true;
        recordingThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                record();
                            }
                        });
        recordingThread.start();
    }

    public synchronized void stopRecording() {
        if (recordingThread == null) {
            return;
        }
        shouldContinue = false;
        recordingThread = null;
    }

    private void record() {
        android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_AUDIO);

        // Estimate the buffer size we'll need for this device.
        int bufferSize =
                AudioRecord.getMinBufferSize(
                        SAMPLE_RATE, AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT);
        if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
            bufferSize = SAMPLE_RATE * 2;
        }
        short[] audioBuffer = new short[bufferSize / 2];

        AudioRecord record =
                new AudioRecord(
                        MediaRecorder.AudioSource.DEFAULT,
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_IN_MONO,
                        AudioFormat.ENCODING_PCM_16BIT,
                        bufferSize);

        if (record.getState() != AudioRecord.STATE_INITIALIZED) {
            Log.e(LOG_TAG, "Audio Record can't initialize!");
            return;
        }

        record.startRecording();

        Log.v(LOG_TAG, "Start recording");

        // Loop, gathering audio data and copying it to a round-robin buffer.
        while (shouldContinue) {
            int numberRead = record.read(audioBuffer, 0, audioBuffer.length);
            int maxLength = recordingBuffer.length;
            int newRecordingOffset = recordingOffset + numberRead;
            int secondCopyLength = Math.max(0, newRecordingOffset - maxLength);
            int firstCopyLength = numberRead - secondCopyLength;
            // We store off all the data for the recognition thread to access. The ML
            // thread will copy out of this buffer into its own, while holding the
            // lock, so this should be thread safe.
            recordingBufferLock.lock();
            try {
                System.arraycopy(audioBuffer, 0, recordingBuffer, recordingOffset, firstCopyLength);
                System.arraycopy(audioBuffer, firstCopyLength, recordingBuffer, 0, secondCopyLength);
                recordingOffset = newRecordingOffset % maxLength;
            } finally {
                recordingBufferLock.unlock();
            }
        }

        record.stop();
        record.release();
    }

    public synchronized void startRecognition() {
        if (recognitionThread != null) {
            return;
        }
        shouldContinueRecognition = true;
        recognitionThread =
                new Thread(
                        new Runnable() {
                            @Override
                            public void run() {
                                recognize();
                            }
                        });
        recognitionThread.start();
    }

    public synchronized void stopRecognition() {
        if (recognitionThread == null) {
            return;
        }
        shouldContinueRecognition = false;
        recognitionThread = null;
    }

    private void recognize() {

        short[] inputBuffer = new short[RECORDING_LENGTH];
        float[] floatInputBuffer = new float[RECORDING_LENGTH];
        float[] outputScores = new float[labels.size()];
        String[] outputScoresNames = new String[] {OUTPUT_SCORES_NAME};
        int[] sampleRateList = new int[] {SAMPLE_RATE};

        // Loop, grabbing recorded data and running the recognition model on it.
        while (shouldContinueRecognition) {
            // The recording thread places data in this round-robin buffer, so lock to
            // make sure there's no writing happening and then copy it to our own
            // local version.
            recordingBufferLock.lock();
            try {
                int maxLength = recordingBuffer.length;
                int firstCopyLength = maxLength - recordingOffset;
                int secondCopyLength = recordingOffset;
                System.arraycopy(recordingBuffer, recordingOffset, inputBuffer, 0, firstCopyLength);
                System.arraycopy(recordingBuffer, 0, inputBuffer, firstCopyLength, secondCopyLength);
            } finally {
                recordingBufferLock.unlock();
            }

            // We need to feed in float values between -1.0f and 1.0f, so divide the
            // signed 16-bit inputs.
            for (int i = 0; i < RECORDING_LENGTH; ++i) {
                floatInputBuffer[i] = inputBuffer[i] / 32767.0f;
            }

            // Run the model.
            inferenceInterface.feed(SAMPLE_RATE_NAME, sampleRateList);
            inferenceInterface.feed(INPUT_DATA_NAME, floatInputBuffer, RECORDING_LENGTH, 1);
            inferenceInterface.run(outputScoresNames);
            inferenceInterface.fetch(OUTPUT_SCORES_NAME, outputScores);

            // Use the smoother to figure out if we've had a real recognition event.
            long currentTime = System.currentTimeMillis();
            final RecognizeCommands.RecognitionResult result =
                    recognizeCommands.processLatestResults(outputScores, currentTime);

            runOnUiThread(
                    new Runnable() {
                        @Override
                        public void run() {

                            boolean commandFound = false;
                            // If we do have a new command, highlight the right list entry.
                            if (!result.foundCommand.startsWith("_") && result.isNewCommand) {
                                int labelIndex = -1;
                                for (int i = 0; i < labels.size(); ++i) {
                                    if (labels.get(i).equals(result.foundCommand)) {
                                        labelIndex = i;
                                    }
                                }

                                switch (labelIndex - 2) {
                                    case 0: // stop
                                        if(mediaPlayer.isPlaying())
                                            btnPausePlay.callOnClick();
                                        commandFound=true;
                                        break;
                                    case 1: //go
                                        if(!mediaPlayer.isPlaying())
                                            btnPausePlay.callOnClick();
                                        commandFound=true;
                                        break;
                                    case 2: // forward
                                        btnNext.callOnClick();
                                        commandFound=true;
                                        break;
                                    case 3: //backward
                                        btnPrevious.callOnClick();
                                        commandFound=true;
                                        break;
                                    case 4: // left
                                        if(seekBar.getProgress() > 10000) {
                                            mediaPlayer.seekTo(seekBar.getProgress() - 10000);
                                            seekBar.setProgress(seekBar.getProgress() - 10000);
                                        }
                                        else {
                                            mediaPlayer.seekTo(0);
                                            seekBar.setProgress(0);
                                        }
                                        commandFound=true;
                                        break;
                                    case 5: // right
                                        if(seekBar.getProgress() < seekBar.getMax() - 10000) {
                                            mediaPlayer.seekTo(seekBar.getProgress() + 10000);
                                            seekBar.setProgress(seekBar.getProgress() + 10000);
                                        }
                                        else {
                                            btnNext.callOnClick();
                                        }
                                        commandFound=true;
                                        break;
                                    case 6: //off
                                        exitApplication();
                                        commandFound=true;
                                        break;
                                }

                                if (commandFound==true) {
                                    final String score = Math.round(result.score * 100) + "%";
                                    Toast.makeText(PlayerActivity.this, result.foundCommand + " " + score , Toast.LENGTH_SHORT).show();
                                }
                            }
                        }
                    });
            try {
                // We don't need to run too frequently, so snooze for a bit.
                Thread.sleep(MINIMUM_TIME_BETWEEN_SAMPLES_MS);
            } catch (InterruptedException e) {
                // Ignore
            }
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mediaPlayer.stop();
        mediaPlayer = null;
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            int currentPosition = msg.what;
            //update seekbar
            seekBar.setProgress(currentPosition);

            //update times
            String elapsedTime = createTime(currentPosition);
            String remainingTime = createTime(mediaPlayer.getDuration() - currentPosition);

            elapsedTimeTextView.setText(elapsedTime);
            remainingTimeTextView.setText("- "+ remainingTime);
        }
    };

    public String createTime(int time){
        String timeLabel;
        int min = time / 1000 / 60;
        int sec = time / 1000 % 60;
        timeLabel = min +":";
        if(sec < 10){
            timeLabel +="0";
        }
        timeLabel += sec;
        return timeLabel;
    }

    private void clearMediaPlayer(){
        mediaPlayer.stop();
        mediaPlayer.release();
        mediaPlayer = null;
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
            case R.id.speak:
                msg = "Możesz mówić";
                //startSpeechRecognition();
                break;
        }
        if(msg != " ")
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
        return super.onOptionsItemSelected(item);
    }

    //dodanie utworzonego menu do toolbara
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_with_mic, menu);
        return true;
    }

    //wylogowanie użytkownika
    public void userLogout() {
        preferencesConfig.writeLoginStatus(false);
        preferencesConfig.writeUserId(-1);
        Intent loginIntent = new Intent(PlayerActivity.this, LoginActivity.class);
        PlayerActivity.this.startActivity(loginIntent);
        finish();
    }

    private void exitApplication(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        }
        System.exit(0);
    }

}
