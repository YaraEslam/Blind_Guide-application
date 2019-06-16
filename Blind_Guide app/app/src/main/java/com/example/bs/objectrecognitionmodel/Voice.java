package com.example.bs.objectrecognitionmodel;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.speech.tts.TextToSpeech;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Layout;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;


import pl.droidsonroids.gif.GifDrawable;
import pl.droidsonroids.gif.GifImageView;

public class Voice extends AppCompatActivity {
    private TextToSpeech TTS;
    public static SpeechRecognizer SRec;
    private TextView txvResult;
    int voiceBool = 0;
    int text =0;

    public static int obstacle = 0, color=0, recognize=0;

    private static final String TAG = "OpenCV/Sample/voice";

    // to rotate the activity without starting from the begin
    @Override
    public void onConfigurationChanged(Configuration configuration){
        super.onConfigurationChanged(configuration);
        setContentView(R.layout.voice_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                SRec.startListening(intent);

            }
        });
        initializeSpeechRecognizer();
        change();
    }

    ////////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.voice_main);
        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                        RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
                intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
                intent.putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 1);
                SRec.startListening(intent);

            }
        });
        initializeTextToSpeech();
        initializeSpeechRecognizer();
        change();
    }

    private void change(){
        // Button of info
        TextView txt = (TextView) findViewById(R.id.infopop);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Voice.this, info.class));
            }
        });
        /////////

        // R GIF
        ImageView Gifr = (GifImageView) findViewById(R.id.gifr);
        Gifr.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ImageView gif2 = (GifImageView) findViewById(R.id.gif);
                //gif2.setImageResource(R.drawable.r1);
                Speak(" Now i can recognize objects .");
                recognize=1;
                if(voiceBool == 0) {
                    //Create a new intent to open the camera
                    Intent Main_intent = new Intent(Voice.this, MainActivity.class);
                    //start the new activity
                    startActivity(Main_intent);
                }
                else
                    voiceBool = 0;
            }
        });

        // D GIF
        ImageView Gifd = (GifImageView) findViewById(R.id.gifd);
        Gifd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //View gif = (View) findViewById(R.id.linergif);
                //gif.setImageResource(R.drawable.d1);
                Speak(" Now i can warning you from obstacle .");
                obstacle=1;
                if(voiceBool == 0) {
                    //Create a new intent to open the camera
                    Intent Main_intent = new Intent(Voice.this, MainActivity.class);
                    //start the new activity
                    startActivity(Main_intent);
                }
                else
                    voiceBool = 0;
            }
        });

        // C GIF
        ImageView Gifc = (GifImageView) findViewById(R.id.gifc);
        Gifc.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ImageView gif2 = (GifImageView) findViewById(R.id.gif);
                //gif2.setImageResource(R.drawable.c1);
                Speak(" Now i can recognize object's color .");
                color=1;
                if(voiceBool == 0) {
                    //Create a new intent to open the camera
                    Intent Main_intent = new Intent(Voice.this, MainActivity.class);
                    //start the new activity
                    startActivity(Main_intent);
                }
                else
                    voiceBool = 0;
            }
        });

    }

    public void getSpeechInput(/*View view*/){
        //Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        //intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault());
        initializeSpeechRecognizer();

       /* if (intent.resolveActivity(getPackageManager()) != null) {
            //startActivityForResult(intent, 10);
        } else {
            Toast.makeText(this, "Your Device Don't Support Speech Input", Toast.LENGTH_SHORT).show();
        }*/

    }

    @Override
    protected void onActivityResult(int requestCode ,int resultCode ,Intent data){
        super.onActivityResult(resultCode,resultCode,data);
        switch (resultCode) {
            case 10:
                if ( resultCode==RESULT_OK && data!= null ){
                    ArrayList<String> result =data.getStringArrayListExtra(
                            RecognizerIntent.EXTRA_RESULTS);
                    txvResult.setText(result.get(0));
                }
                break;
        }
    }

    public void initializeSpeechRecognizer() {
        color=0;
        recognize=0;
        obstacle=0;
        if (SpeechRecognizer.isRecognitionAvailable(this)){
            SRec=SpeechRecognizer.createSpeechRecognizer(this);
            SRec.setRecognitionListener(new RecognitionListener() {

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
                public void onResults(Bundle bundle) {
                    List<String > Results = bundle.getStringArrayList(
                            SpeechRecognizer.RESULTS_RECOGNITION);
                    ProcessResult(Results.get(0));
                }

                @Override
                public void onPartialResults(Bundle partialResults) {

                }

                @Override
                public void onEvent(int eventType, Bundle params) {

                }
            });
        }
    }

    private void ProcessResult(String command){
        command =command.toLowerCase();
        //distance
        if (command.contains("distance")) {
            //ImageView gif = (GifImageView) findViewById(R.id.gif);
            //gif.setImageResource(R.drawable.d1);
            Speak(" Now i can warning you from obstacle .");
            obstacle=1;
            if(voiceBool == 0) {
                //Create a new intent to open the camera
                Intent Main_intent = new Intent(Voice.this, MainActivity.class);
                //start the new activity
                startActivity(Main_intent);
            }
            else
                voiceBool = 0;
        }

        // recognize
        else if (command.contains("recognize")|| command.contains("recognise")) {
            //ImageView gif2 = (GifImageView) findViewById(R.id.gif);
            //gif2.setImageResource(R.drawable.r1);
            Speak(" Now i can recognize objects .");
            recognize=1;
            if(voiceBool == 0) {
                //Create a new intent to open the camera
                Intent Main_intent = new Intent(Voice.this, MainActivity.class);
                //start the new activity
                startActivity(Main_intent);
            }
            else
                voiceBool = 0;
        }

        //color
        else if (command.contains("color")||command.contains("colour") ) {
            //ImageView gif2 = (GifImageView) findViewById(R.id.gif);
            //gif2.setImageResource(R.drawable.c1);
            Speak(" Now i can recognize object's color .");
            color=1;
            if(voiceBool == 0) {
                //Create a new intent to open the camera
                Intent Main_intent = new Intent(Voice.this, MainActivity.class);
                //start the new activity
                startActivity(Main_intent);
            }
            else
                voiceBool = 0;
        }

        else
            Speak("select one of recognize , distance or color ");
    }


    private void initializeTextToSpeech(){
        TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (TTS.getEngines().size() ==0) {
                    Toast.makeText(Voice.this , "you haven't TTS engine" ,
                            Toast.LENGTH_LONG ).show();
                    finish();
                }
                else {
                    TTS.setLanguage(Locale.US);
                    Speak("select one of recognize , distance or color ");
                    //Speak("Hello , your new friend is ready , i hope your guide to be useful");
                }

            }
        });
    }

    private void Speak(String msg) {
        if(Build.VERSION.SDK_INT >=21){
            TTS.speak(msg,TextToSpeech.QUEUE_FLUSH ,null , null);
        }
        else {
            TTS.speak(msg,TextToSpeech.QUEUE_FLUSH ,null);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
       // getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        TTS.shutdown();
    }
}