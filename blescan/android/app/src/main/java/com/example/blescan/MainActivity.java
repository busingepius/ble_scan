package com.example.blescan;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;

import io.flutter.embedding.android.FlutterActivity;
import io.flutter.embedding.engine.FlutterEngine;
import io.flutter.plugin.common.MethodChannel;

public class MainActivity extends FlutterActivity {
    private static final String CHANNEL = "bluetooth_channel";
    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    private BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private List<String> scanResults = new ArrayList<>();
    private final Handler handler = new Handler(Looper.getMainLooper());

    // Stops scanning after 10 seconds
    private static final long SCAN_PERIOD = 10000;
    private boolean isScanning = false;

    @Override
    public void configureFlutterEngine(@NonNull FlutterEngine flutterEngine) {
        super.configureFlutterEngine(flutterEngine);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        bluetoothLeScanner = bluetoothAdapter.getBluetoothLeScanner();

        new MethodChannel(flutterEngine.getDartExecutor().getBinaryMessenger(), CHANNEL)
            .setMethodCallHandler((call, result) -> {
                switch (call.method) {
                    case "getPairedDevices":
                        result.success(getPairedDevices());
                        break;
                    case "enableBluetooth":
                        enableBluetooth();
                        result.success(null);
                        break;
                    case "requestBluetoothPermissions":
                        requestBluetoothPermissions();
                        result.success(null);
                        break;
                    case "startScan":
                        startScan();
                        result.success(null);
                        break;
                    case "stopScan":
                        stopScan();
                        result.success(null);
                        break;
                    case "getScanResults":
                        result.success(scanResults);
                        break;
                    default:
                        result.notImplemented();
                        break;
                }
            });
    }

    private List<String> getPairedDevices() {
        List<String> deviceList = new ArrayList<>();
        if (bluetoothAdapter != null && bluetoothAdapter.isEnabled()) {
            for (BluetoothDevice device : bluetoothAdapter.getBondedDevices()) {
                deviceList.add(device.getName() + " - " + device.getAddress());
            }
        }
        return deviceList;
    }

    private void enableBluetooth() {
        if (bluetoothAdapter != null && !bluetoothAdapter.isEnabled()) {
            bluetoothAdapter.enable();
        }
    }

    private void requestBluetoothPermissions() {
        List<String> permissionsList = new ArrayList<>();
        permissionsList.add(Manifest.permission.BLUETOOTH);
        permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            permissionsList.add(Manifest.permission.BLUETOOTH_SCAN);
            permissionsList.add(Manifest.permission.BLUETOOTH_CONNECT);
        }

        String[] permissionsArray = permissionsList.toArray(new String[0]);
        List<String> missingPermissions = new ArrayList<>();
        for (String permission : permissionsArray) {
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                missingPermissions.add(permission);
            }
        }

        if (!missingPermissions.isEmpty()) {
            ActivityCompat.requestPermissions(this, missingPermissions.toArray(new String[0]), REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            Toast.makeText(this, "Bluetooth permissions already granted", Toast.LENGTH_SHORT).show();
        }
    }

    // Start BLE scan
    private void startScan() {
        if (bluetoothLeScanner == null || isScanning) return;

        scanResults.clear();  // Clear previous scan results
        handler.postDelayed(this::stopScan, SCAN_PERIOD);  // Stop scan after a defined period
        bluetoothLeScanner.startScan(bleScanCallback);
        isScanning = true;
        Toast.makeText(this, "Scanning for BLE devices...", Toast.LENGTH_SHORT).show();
    }

    // Stop BLE scan
    private void stopScan() {
        if (bluetoothLeScanner == null || !isScanning) return;

        bluetoothLeScanner.stopScan(bleScanCallback);
        isScanning = false;
        Toast.makeText(this, "BLE scan stopped", Toast.LENGTH_SHORT).show();
    }

    // BLE Scan callback
    private final ScanCallback bleScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            BluetoothDevice device = result.getDevice();
            String deviceInfo = device.getName() + " - " + device.getAddress();
            if (!scanResults.contains(deviceInfo)) {
                scanResults.add(deviceInfo);
            }
        }

        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            for (ScanResult result : results) {
                BluetoothDevice device = result.getDevice();
                String deviceInfo = device.getName() + " - " + device.getAddress();
                if (!scanResults.contains(deviceInfo)) {
                    scanResults.add(deviceInfo);
                }
            }
        }

        @Override
        public void onScanFailed(int errorCode) {
            Toast.makeText(MainActivity.this, "BLE scan failed with error: " + errorCode, Toast.LENGTH_SHORT).show();
        }
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }

            if (allPermissionsGranted) {
                Toast.makeText(this, "Bluetooth permissions granted", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth permissions denied", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
