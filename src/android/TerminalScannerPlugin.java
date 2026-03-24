package ru.keysoftware.ksm.cordova;

import android.content.Context;
import android.content.IntentFilter;
import android.device.ScanManager;
import android.util.Log;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;
import org.json.JSONArray;
import org.json.JSONException;

import java.util.Set;

public class TerminalScannerPlugin extends CordovaPlugin {
    private static final Set<String> UROVO_MANUFACTURERS = Set.of("UBX", "Urovo");
    private static final String LOG_TAG = "UrovoPDAPlugin";
    private static final String UROVO_SCAN_ACTION = "urovo.rcv.message";
    private static final String ATOL_SCAN_ACTION = "com.xcheng.scanner.action.BARCODE_DECODING_BROADCAST";

    private Context context;
    private ScanManager scanManager;
    private AtolBroadcastReceiver atolScanReceiver;
    private UrovoBroadcastReceiver urovoScanReceiver;

    @Override
    protected void pluginInitialize() {
        context = this.cordova.getActivity().getApplicationContext();
        atolScanReceiver = new AtolBroadcastReceiver(context);
        urovoScanReceiver = new UrovoBroadcastReceiver(context);
    }

    @Override
    public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
        if (action.equals("onBarcodeScanned")) {
            this.onBarcodeScanned(callbackContext);
            return true;
        } else if (action.equals("doScan")) {
            this.doScan(callbackContext);
            return true;
        }
        return false;
    }

    /**
     * Called when the system is about to start resuming a previous activity.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onPause(boolean multitasking) {
        if (scanManager != null) {
            scanManager.stopDecode();
        }
        context.unregisterReceiver(atolScanReceiver);
        context.unregisterReceiver(urovoScanReceiver);
    }

    /**
     * Called when the activity will start interacting with the user.
     *
     * @param multitasking Flag indicating if multitasking is turned on for app
     */
    @Override
    public void onResume(boolean multitasking) {
        initScan();
        IntentFilter urovoFilter = new IntentFilter();
        urovoFilter.addAction(UROVO_SCAN_ACTION);
        context.registerReceiver(urovoScanReceiver, urovoFilter);

        IntentFilter atolFilter = new IntentFilter();
        atolFilter.addAction(ATOL_SCAN_ACTION);
        context.registerReceiver(atolScanReceiver, atolFilter);
    }

    /**
     * Called when the activity is becoming visible to the user.
     */
    @Override
    public void onStart() {
    }

    private void initScan() {
        System.out.println("manufacturer: " + android.os.Build.MANUFACTURER);
        if (UROVO_MANUFACTURERS.contains(android.os.Build.MANUFACTURER)) {
            scanManager = new ScanManager();
            scanManager.openScanner();
            scanManager.switchOutputMode(0);
        }
    }

    private void doScan(final CallbackContext callbackContext) {
        try {
            scanManager.stopDecode();
            Thread.sleep(100);
            scanManager.startDecode();
            callbackContext.success();
        } catch (Exception e) {
            Log.e(LOG_TAG, e.getMessage());
            callbackContext.error(e.getLocalizedMessage());
        }
    }

    private void onBarcodeScanned(final CallbackContext callbackContext) {
        urovoScanReceiver.addEventListener(barcode -> {
            PluginResult result = new PluginResult(PluginResult.Status.OK, barcode);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        });
        atolScanReceiver.addEventListener(barcode -> {
            PluginResult result = new PluginResult(PluginResult.Status.OK, barcode);
            result.setKeepCallback(true);
            callbackContext.sendPluginResult(result);
        });
    }
}
