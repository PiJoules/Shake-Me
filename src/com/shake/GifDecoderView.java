/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.shake;

import android.app.ProgressDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Handler;
import android.widget.ImageView;
import java.io.InputStream;

/**
 *
 * @author Pi_Joules
 */
public class GifDecoderView extends ImageView {
    
    private boolean mIsPlayingGif = false;
    private GifDecoder mGifDecoder;
    private Bitmap mTmpBitmap;
    final Handler mHandler = new Handler();
    final Runnable mUpdateResults = new Runnable() {
      public void run() {
         if (mTmpBitmap != null && !mTmpBitmap.isRecycled()) {
            GifDecoderView.this.setImageBitmap(mTmpBitmap);
         }
      }
    };
    private InputStream stream;
    //ProgressDialog progress;

    public GifDecoderView(Context context) {
        super(context);
        init(context.getResources().openRawResource(R.raw.batman));
        /*progress = new ProgressDialog(context);
        progress.setTitle("Loading");
        progress.setMessage("Please wait...");
        progress.show();*/
        playGif();
    }
    
    public void init(InputStream input){
        this.stream = input;
        System.out.println(input.toString());
    }
    
    public void playGif(){
        mGifDecoder = new GifDecoder();
        mGifDecoder.read(this.stream);
        mIsPlayingGif = true;
        
        //progress.dismiss();
        
        new Thread(new Runnable() {
                 public void run() {
                    final int n = mGifDecoder.getFrameCount();
                    final int ntimes = mGifDecoder.getLoopCount();
                    int repetitionCounter = 0;
                    do {
                      for (int i = 0; i < n; i++) {
                         mTmpBitmap = mGifDecoder.getFrame(i);
                         final int t = mGifDecoder.getDelay(i);
                         mHandler.post(mUpdateResults);
                         try {
                            Thread.sleep(t);
                         } catch (InterruptedException e) {
                            e.printStackTrace();
                         }
                      }
                      if(ntimes != 0) {
                         repetitionCounter ++;
                      }
                   } while (mIsPlayingGif && (repetitionCounter <= ntimes));
                }
             }).start();
    }
    
    public void hideGif(){
        this.setVisibility(INVISIBLE);
    }
    
    public void showGif(){
        this.setVisibility(VISIBLE);
    }
    
}
