import 'dart:async';
import 'package:flutter/material.dart';
import 'package:flutter/services.dart';

void main() {
  runApp(MyApp());
}

class MyApp extends StatelessWidget {
  @override
  Widget build(BuildContext context) {
    return MaterialApp(
      title: 'Bluetooth Example',
      theme: ThemeData(primarySwatch: Colors.blue),
      home: BluetoothPage(),
    );
  }
}

class BluetoothPage extends StatefulWidget {
  @override
  _BluetoothPageState createState() => _BluetoothPageState();
}

class _BluetoothPageState extends State<BluetoothPage> {
  static const methodChannel = MethodChannel('bluetooth_channel');
  List<String> scannedDevices = [];
  bool isScanning = false;

  @override
  void initState() {
    super.initState();
    requestBluetoothPermissions();
  }

  Future<void> requestBluetoothPermissions() async {
    try {
      await methodChannel.invokeMethod('requestBluetoothPermissions');
    } on PlatformException catch (e) {
      print("Failed to request Bluetooth permissions: '${e.message}'.");
    }
  }

  Future<void> enableBluetooth() async {
    try {
      await methodChannel.invokeMethod('enableBluetooth');
    } on PlatformException catch (e) {
      print('Failed to enable bluetooth: ${e.message}');
    }
  }

  Future<void> getPairedDevices() async {
    try {
      var pairedDevices =
          await methodChannel.invokeMethod('getPairedDevices');
      setState(() {
        scannedDevices = pairedDevices.cast<String>();
      });
    } on PlatformException catch (e) {
      print('Failed to enable bluetooth: ${e.message}');
    }
  }

  Future<void> startScan() async {
    try {
      await methodChannel.invokeMethod('startScan');
      setState(() {
        isScanning = true;
      });
      // Periodically update the scanned devices list
      Timer.periodic(Duration(seconds: 2), (timer) {
        if (!isScanning) {
          timer.cancel();
        } else {
          getScanResults();
        }
      });
    } on PlatformException catch (e) {
      print("Failed to start scan: '${e.message}'.");
    }
  }

  Future<void> stopScan() async {
    try {
      await methodChannel.invokeMethod('stopScan');
      setState(() {
        isScanning = false;
      });
    } on PlatformException catch (e) {
      print("Failed to stop scan: '${e.message}'.");
    }
  }

  Future<void> getScanResults() async {
    try {
      final List<dynamic> results =
          await methodChannel.invokeMethod('getScanResults');
      setState(() {
        scannedDevices = results.cast<String>();
        print(scannedDevices);
      });
    } on PlatformException catch (e) {
      print("Failed to get scan results: '${e.message}'.");
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(
        title: Text('Bluetooth Example'),
      ),
      body: Padding(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            ElevatedButton(
              onPressed: enableBluetooth,
              child: Text('Enable BLE'),
            ),
            ElevatedButton(
              onPressed: isScanning ? stopScan : startScan,
              child: Text(isScanning ? 'Stop Scan' : 'Start Scan'),
            ),
            ElevatedButton(
              onPressed: getPairedDevices,
              child: Text('GET PAIRED RESULTS'),
            ),
            ElevatedButton(
              onPressed: getScanResults,
              child: Text('GET SCAN RESULTS'),
            ),
            SizedBox(height: 20),
            Expanded(
              child: ListView.builder(
                itemCount: scannedDevices.length,
                itemBuilder: (context, index) {
                  return ListTile(
                    title: Text(scannedDevices[index]),
                  );
                },
              ),
            ),
          ],
        ),
      ),
    );
  }
}
