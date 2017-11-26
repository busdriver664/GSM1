package com.test.myapplication;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Instrumentation;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaRecorder;
import android.os.Environment;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.Settings;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;

import static android.content.Context.KEYGUARD_SERVICE;


/**
 * Created by Admin on 23.10.2017.
 */

public class CallReceiver extends BroadcastReceiver  {
    private PowerManager mPowerManager;
    private PowerManager.WakeLock mWakeLock;
    @Override
    public void onReceive(final Context context, Intent intent) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        final PhoneStateListener listener = new PhoneStateListener(){
            @Override
            public void onCallStateChanged(int state, String incomingNumber) {
                switch (state)
                {
                    case TelephonyManager.CALL_STATE_IDLE:
                        Toast.makeText(context.getApplicationContext(), "Call Ended..", Toast.LENGTH_LONG).show();
                        Log.i("stop", "Call Ended....");
                        break;
                    case TelephonyManager.CALL_STATE_OFFHOOK:
                        Toast.makeText(context.getApplicationContext(), "Call Picked..", Toast.LENGTH_LONG) .show();
                        Log.i("received", "Call Picked....");
                        //startRecording(incomingNumber);
                        /*PowerManager:
                        mPowerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
                        turnOffScreen();*/
                        AudioManager amanager=(AudioManager)context.getSystemService(Context.AUDIO_SERVICE);
                        amanager.setStreamMute(AudioManager.STREAM_NOTIFICATION, true);
                        amanager.setStreamMute(AudioManager.STREAM_ALARM, true);
                        amanager.setStreamMute(AudioManager.STREAM_MUSIC, true);
                        amanager.setStreamMute(AudioManager.STREAM_RING, true);
                        amanager.setStreamMute(AudioManager.STREAM_SYSTEM, true);
                    case TelephonyManager.CALL_STATE_RINGING:
                        // Вот здесь надо обрабатывать звонок
                        Toast.makeText(context.getApplicationContext(), "Call RingingBEOCH..." + incomingNumber,Toast.LENGTH_LONG).show();
                       autoPickCall(context);
                        /*try {
                            Runtime.getRuntime().exec("input keyevent " +
                                    Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));

                        } catch (IOException e) {

                        }*/
                        break;
                }

            }
        };
        telephonyManager.listen(listener, PhoneStateListener.LISTEN_CALL_STATE);
    }
    private void autoPickCall(final Context mContext) {
        new Thread(new Runnable() {

            @Override
            public void run() {
                try {
                    Runtime.getRuntime().exec("input keyevent " +
                            Integer.toString(KeyEvent.KEYCODE_HEADSETHOOK));
                    Log.v("", "GVSharma try");
                } catch (IOException e) {
                    // Runtime.exec(String) had an I/O problem, try to fall back
                    String enforcedPerm = "android.permission.CALL_PRIVILEGED";
                    Intent btnDown = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                            Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN,
                                    KeyEvent.KEYCODE_HEADSETHOOK));
                    Intent btnUp = new Intent(Intent.ACTION_MEDIA_BUTTON).putExtra(
                            Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP,
                                    KeyEvent.KEYCODE_HEADSETHOOK));

                    mContext.sendOrderedBroadcast(btnDown, enforcedPerm);
                    mContext.sendOrderedBroadcast(btnUp, enforcedPerm);
                    Log.v("", "GVSharma catch");
                }
            }

        }).start();
    }

    private void startRecording(String number) {
        String savePath = Environment.getExternalStorageDirectory().getAbsolutePath();
        savePath += "/Recorded";
        File file = new File(savePath);
        if (!file.exists()) {
            file.mkdir();
        }
        savePath += "/record_" + System.currentTimeMillis() + ".amr";
        MediaRecorder mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);  // если заменить VOICE_CALL на MIC то звук без проблем пишется с микрофона, прим. К.О. :)
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.AMR_NB);
        mRecorder.setOutputFile(savePath);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.DEFAULT);
        try {
            mRecorder.prepare();
        } catch (IOException e) {
        }
        mRecorder.start();
    }
    @TargetApi(21) //Suppress lint error for PROXIMITY_SCREEN_OFF_WAKE_LOCK
    public void turnOffScreen(){
        // turn off screen
        Log.v("ProximityActivity", "OFF!");
        mWakeLock = mPowerManager.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "tag");
        if (mWakeLock.isHeld())
            mWakeLock.release();
    }


}
