package com.shake;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.TextView;
import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener{
    
    private MediaPlayer mp;
    private boolean playing = false;
    
    //private TextView xtv,ytv,ztv,noisetv;
    private TextView shake;
    //public GifDecoderView view;
    private WebView wv;
    private ImageView settings, play;
    ProgressDialog progress;
    
    private final int SEARCH_IDENTIFIER = 0;
    private final String SEARCH = "SEARCH";
    
    private SensorManager sm;
    private Sensor accel;
    private float xAccel, yAccel, zAccel;
    private boolean initialized = false, playButtonPressed = false;
    private final double NOISE = 9.0;
    private final double SMALLNOISE = NOISE/2;
    private ArrayList<Double> previousNoise;
    private final int MAXNOISECOUNT = 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle("Shake");
        
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        
        //view = new GifDecoderView(this);
        //setContentView(view);
        //view.hideGif();
        
        mp = MediaPlayer.create(this, R.raw.batman_on_drugs);
        //mp.setVolume(0, 0); // mute mp (for testing)
        
        // setup the webview
        wv = (WebView) findViewById(R.id.web);
        wv.getSettings().setJavaScriptEnabled(true);
        setFilePath("file:///sdcard/download/3009sw7sw7yu.gif");
        wv.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        wv.setVisibility(View.INVISIBLE);
        
        // setup the buttons
        settings = (ImageView) findViewById(R.id.settings);
        settings.setImageResource(R.raw.settings_gear);
        settings.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Intent fe = new Intent(getApplicationContext(), FileExplorer.class);
                startActivityForResult(fe, SEARCH_IDENTIFIER);
            }
        });
        play = (ImageView) findViewById(R.id.play);
        play.setImageResource(R.raw.play);
        play.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if (!playButtonPressed && !playing){
                    playGif();
                    playButtonPressed = true;
                }
                else if (playButtonPressed && !playing){
                    stopGif();
                    playButtonPressed = false;
                }
            }
        });
        
        
        shake = (TextView) findViewById(R.id.shake);
        
        previousNoise = new ArrayList<Double>();
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == SEARCH_IDENTIFIER && resultCode == Activity.RESULT_OK){
            String file = data.getStringExtra(SEARCH);
            if (file.endsWith(".gif")){
                setFilePath("file://" + file);
            }
            else if (file.endsWith(".mp3")) {
                mp = MediaPlayer.create(this, Uri.parse("file://" + file));
            }
        }
    }
    
    @Override
    protected void onResume(){
        super.onResume();
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    @Override
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
    }
    
    private void setFilePath(String path){
        String html = "<html><body style = 'background:black;'><img src = '" + path + "' style = 'width:100%;'></body></html>";
        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
    }
    
    private void playGif(){
        mp.start();
        mp.setLooping(true);
        //view.showGif();
        //setContentView(view);
        //setContentView(wv);
        wv.setVisibility(View.VISIBLE);
    }
    
    private void stopGif(){
        mp.pause();
        mp.seekTo(mp.getCurrentPosition());
        //view.hideGif();
        //setContentView(R.layout.main);
        wv.setVisibility(View.INVISIBLE);
    }

    public void onSensorChanged(SensorEvent event) {
        if (!initialized){
            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            initialized = true;
        }
        else {
            float dAX = xAccel - event.values[0];
            float dAY = yAccel - event.values[1];
            float dAZ = zAccel - event.values[2];
            double noiseVector = Math.sqrt(Math.pow(dAX,2)+Math.pow(dAY,2)+Math.pow(dAZ,2));
            
            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            previousNoise.add(noiseVector);
            if (previousNoise.size() > MAXNOISECOUNT){
                previousNoise.remove(0);
            }
            if (previousNoise.size() == MAXNOISECOUNT && !playing && !playButtonPressed){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT > NOISE){
                    playGif();
                    playing = true;
                }
            }
            else if (playing && !playButtonPressed){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT < NOISE){
                    if (mp.isPlaying()){
                        stopGif();
                    }
                    playing = false;
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }
}
