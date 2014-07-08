package com.shake;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.widget.TextView;
import java.util.ArrayList;

public class MainActivity extends Activity implements SensorEventListener{
    
    private MediaPlayer mp;
    private boolean playing = false;
    
    //private TextView xtv,ytv,ztv,noisetv;
    public GifDecoderView view;
    ProgressDialog progress;
    
    private SensorManager sm;
    private Sensor accel;
    private float xAccel, yAccel, zAccel;
    private boolean initialized = false;
    private final double NOISE = 10.0;
    private ArrayList<Double> previousNoise;
    private final int MAXNOISECOUNT = 3;
    
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        //setContentView(R.layout.main);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setTitle("Shake");
        
        sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        accel = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
        
        view = new GifDecoderView(this);
        setContentView(view);
        view.hideGif();
        
        mp = MediaPlayer.create(this, R.raw.batman_on_drugs);
        
        /*xtv = (TextView) findViewById(R.id.xAccel);
        ytv = (TextView) findViewById(R.id.yAccel);
        ztv = (TextView) findViewById(R.id.zAccel);
        noisetv = (TextView) findViewById(R.id.noise);*/
        
        previousNoise = new ArrayList<Double>();
    }
    
    protected void onResume(){
        super.onResume();
        sm.registerListener(this, accel, SensorManager.SENSOR_DELAY_NORMAL);
    }
    
    protected void onPause() {
        super.onPause();
        sm.unregisterListener(this);
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
            
            /*xtv.setText("dx: " + dAX);
            ytv.setText("dy: " + dAY);
            ztv.setText("dz: " + dAZ);
            noisetv.setText("noise: " + noiseVector);*/
            
            xAccel = event.values[0];
            yAccel = event.values[1];
            zAccel = event.values[2];
            previousNoise.add(noiseVector);
            if (previousNoise.size() > MAXNOISECOUNT){
                previousNoise.remove(0);
            }
            if (previousNoise.size() == MAXNOISECOUNT && !playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT > NOISE){
                    mp.start();
                    mp.setLooping(true);
                    view.showGif();
                    playing = true;
                }
            }
            else if (playing){
                double sum = 0;
                for (int i = 0; i < MAXNOISECOUNT; i++) sum += previousNoise.get(i);
                if (sum/MAXNOISECOUNT < NOISE){
                    if (mp.isPlaying()){
                        mp.pause();
                        mp.seekTo(mp.getCurrentPosition());
                        view.hideGif();
                    }
                    playing = false;
                }
            }
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        
    }
}
