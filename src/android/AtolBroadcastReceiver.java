package ru.keysoftware.ksm.cordova;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Bundle;
import android.os.Vibrator;

public class AtolBroadcastReceiver extends BroadcastReceiver {
    private static final String FNC1_PREFIX = "\u00e8";
    private Vibrator vibrator;
    private SoundPool soundpool = null;
    private int soundid;
    private String barcodeStr;
    private boolean isScaning = false;
    private BarcodeListener listener;

    public AtolBroadcastReceiver(Context context) {
        vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);

        soundpool = new SoundPool(1, AudioManager.STREAM_NOTIFICATION, 100); // MODE_RINGTONE
        soundid = soundpool.load("/etc/Scan_new.ogg", 1);
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        isScaning = false;
        soundpool.play(soundid, 1, 1, 0, 0, 1);
        vibrator.vibrate(100);

        System.out.println("intent:" + intent);
        System.out.println("extras:" + intent.getExtras());
        final Bundle bundle = intent.getExtras();
        for (final String key : bundle.keySet()) {
            System.out.println(key + ": " + bundle.get(key));
        }
        String barcode = intent.getStringExtra("EXTRA_BARCODE_DECODING_DATA");
        if (barcode != null && barcode.startsWith(FNC1_PREFIX)) {
            barcode = barcode.substring(1);
        }
        if (listener != null) {
            listener.onBarcodeScanned(barcode);
        }
    }

    public void addEventListener(BarcodeListener listener) {
        this.listener = listener;
    }
}
