package com.jarvis.tts;

import android.app.Service;
import android.content.Intent;
import android.media.AudioAttributes;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.util.Log;
import java.io.File;

public class TtsPlayerService extends Service {
    private static final String TAG = "JarvisTTS";
    private MediaPlayer player;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) { stopSelf(); return START_NOT_STICKY; }

        String path = "/sdcard/tts.wav";
        Uri uri = intent.getData();
        if (uri != null && uri.getPath() != null) path = uri.getPath();

        Log.d(TAG, "Playing: " + path);

        if (player != null) { player.release(); player = null; }

        try {
            if (!new File(path).exists()) {
                Log.e(TAG, "File not found: " + path);
                stopSelf(); return START_NOT_STICKY;
            }
            player = new MediaPlayer();
            player.setAudioAttributes(new AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_ASSISTANCE_ACCESSIBILITY)
                .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                .build());
            player.setDataSource(path);
            player.prepare();
            player.setOnCompletionListener(mp -> {
                mp.release(); player = null; stopSelf();
            });
            player.setOnErrorListener((mp, what, extra) -> {
                Log.e(TAG, "Error: " + what); stopSelf(); return true;
            });
            player.start();
        } catch (Exception e) {
            Log.e(TAG, "Exception: " + e.getMessage()); stopSelf();
        }
        return START_NOT_STICKY;
    }

    @Override public IBinder onBind(Intent i) { return null; }

    @Override public void onDestroy() {
        if (player != null) { player.release(); player = null; }
        super.onDestroy();
    }
}
