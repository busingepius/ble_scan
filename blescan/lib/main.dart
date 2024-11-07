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
  static const platform = MethodChannel('bluetooth_channel');
  List<String> scannedDevices = [];
  bool isScanning = false;

  @override
  void initState() {
    super.initState();
    requestBluetoothPermissions();
  }

  Future<void> requestBluetoothPermissions() async {
    try {
      await platform.invokeMethod('requestBluetoothPermissions');
    } on PlatformException catch (e) {
      print("Failed to request Bluetooth permissions: '${e.message}'.");
    }
  }

  Future<void> startScan() async {
    try {
      await platform.invokeMethod('startScan');
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
      await platform.invokeMethod('stopScan');
      setState(() {
        isScanning = false;
      });
    } on PlatformException catch (e) {
      print("Failed to stop scan: '${e.message}'.");
    }
  }

  Future<void> getScanResults() async {
    try {
      final List<dynamic> results = await platform.invokeMethod('getScanResults');
      setState(() {
        scannedDevices = results.cast<String>();
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
              onPressed: isScanning ? stopScan : startScan,
              child: Text(isScanning ? 'Stop Scan' : 'Start Scan'),
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
