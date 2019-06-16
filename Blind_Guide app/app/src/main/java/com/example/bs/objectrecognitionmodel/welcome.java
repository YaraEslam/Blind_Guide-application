package com.example.bs.objectrecognitionmodel;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Locale;

import pl.droidsonroids.gif.GifImageView;

public class welcome extends Activity {

    private TextToSpeech TTS;

    @Override
    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
        setContentView(R.layout.activity_welcome);
        change();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        TTS = new TextToSpeech(this, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if (TTS.getEngines().size() == 0) {
                    Toast.makeText(welcome.this, "you haven't TTS engine",
                            Toast.LENGTH_LONG).show();
                    finish();
                } else {
                    TTS.setLanguage(Locale.US);
                    Speak("Hello , your new friend is ready , i hope your guide to be useful");
                }

            }
        });
        change();
    }

    private void Speak(String msg) {
        if(Build.VERSION.SDK_INT >=21){
            TTS.speak(msg,TextToSpeech.QUEUE_FLUSH ,null , null);
        }
        else {
            TTS.speak(msg,TextToSpeech.QUEUE_FLUSH ,null);
        }
    }

    private void change (){

        // Button of info
        TextView txt = (TextView) findViewById(R.id.infopop);
        txt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(welcome.this, info.class));
            }
        });
        /////////

        ImageView gif = (GifImageView) findViewById(R.id.gif);
        gif.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(welcome.this, Voice.class));

            }
        });
    }

}
