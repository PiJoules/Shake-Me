package com.ShakeMe.shake;

import android.app.Activity;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener, OnSeekBarChangeListener{
    
    private SeekBar bar; // seekbar for controlling sensitivity
    private TextView sensitivity;
    
    // MediaPlayer controls playing the mp3
    private MediaPlayer mp;
    private boolean playing = false;
    
    // Display the gif in a webview for simplicity
    private WebView wv;
    
    // Stuff for detecting shakes
    private SensorManager sm;
    private Sensor accel;
    private float xAccel, yAccel, zAccel;
    private boolean initialized = false;
    private double NOISE = 5.0;
    private ArrayList<Double> previousNoise;
    private final int MAXNOISECOUNT = 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        
        bar = (SeekBar)findViewById(R.id.seekBar);
        bar.setOnSeekBarChangeListener(this);
        sensitivity = (TextView) findViewById(R.id.sensitivity);
        
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        
        mp = MediaPlayer.create(this, R.raw.batman_on_drugs);
        mp.setVolume(1.0f, 1.0f);
        
        // setup the webview
        wv = (WebView) findViewById(R.id.web);
        String html = "<html><body style = 'background:black;'><img src = 'file:///android_res/raw/batman.gif' style = 'width:100%;'></body></html>";
        wv.loadDataWithBaseURL(null, html, "text/html", "utf-8", null);
        wv.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
        wv.setVisibility(View.INVISIBLE);
                
        previousNoise = new ArrayList<Double>();
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
    
    private void playGif(){
        mp.start();
        mp.setLooping(true);
        wv.setVisibility(View.VISIBLE);
    }
    
    private void stopGif(){
        mp.pause();
        mp.seekTo(mp.getCurrentPosition());
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
            while (previousNoise.size() > MAXNOISECOUNT){
                previousNoise.remove(0);
            }
            if (previousNoise.size() == MAXNOISECOUNT && !playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT > NOISE){
                    playGif();
                    playing = true;
                }
            }
            else if (playing){
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
    
    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        sensitivity.setText("Sensitivity (" + progress + ")");
        NOISE = (10-progress);
    }
    
    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
        
    }
    
    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        
    }
}
