package nl.tudelft.followbot.bluetooth;

import java.io.File;

import nl.tudelft.followbot.LogFile;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;
import android.widget.Toast;

public class Bluetooth {

	private final Activity activity;

	private final int REQUEST_ENABLE_BT = 1;

	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				// Get the BluetoothDevice object from the Intent
				BluetoothDevice device = intent
						.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// If it's paired, get RSSI and restart discovery
				if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
					int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
							Short.MIN_VALUE);
					Log.d("BLUETOOTH", " RSSI: " + rssi + "dBm" + " Distance: "
							+ getRSSIDistance(rssi) + "m" + " Device: "
							+ device.getName());

					LogFile.appendLog(new File(context.getExternalCacheDir(),
							"logbt.csv"), String.format("%d,%d", rssi, 16));

					Toast.makeText(
							activity.getApplicationContext(),
							"RSSI: " + rssi + "dBm" + "\nDistance: "
									+ getRSSIDistance(rssi) + "m",
							Toast.LENGTH_SHORT).show();

					// restart discovery
					startBluetoothDiscovery();
				}
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED
					.equals(action)) {
				startBluetoothDiscovery();
			}
		}
	};

	public Bluetooth(Activity activity) {
		this.activity = activity;

		if (!mBluetoothAdapter.isEnabled()) {
			Intent enableBtIntent = new Intent(
					BluetoothAdapter.ACTION_REQUEST_ENABLE);
			activity.startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
		}
	}

	public void create() {
		Log.d("BLUETOOTH", "Registering BroadcastReceiver");
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		activity.registerReceiver(mReceiver, filter);

		filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		activity.registerReceiver(mReceiver, filter);

		startBluetoothDiscovery();
	}

	public void destroy() {
		stopBluetoothDiscovery();
		activity.unregisterReceiver(mReceiver);
	}

	public void startBluetoothDiscovery() {
		// cancel any prior BT device discovery
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}

		mBluetoothAdapter.startDiscovery();
	}

	public void stopBluetoothDiscovery() {
		if (mBluetoothAdapter.isDiscovering()) {
			mBluetoothAdapter.cancelDiscovery();
		}
	}

	public double getRSSIDistance(int rssi) {
		return Math.pow(10.0, (-44.0 - rssi) / 20.0);
	}
}