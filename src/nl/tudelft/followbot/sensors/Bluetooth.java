package nl.tudelft.followbot.sensors;

import java.util.ArrayList;

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

	private final ArrayList<String> mArrayAdapter = new ArrayList<String>();

	BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

	// Create a BroadcastReceiver for ACTION_FOUND
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();
			if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				int rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI,
						Short.MIN_VALUE);
				Toast.makeText(activity.getApplicationContext(),
						"  RSSI: " + rssi + "dBm", Toast.LENGTH_SHORT).show();
				Log.d("BLUETOOTH", "  RSSI: " + rssi + "dBm");
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
		// Register the BroadcastReceiver
		IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
		activity.registerReceiver(mReceiver, filter);
	}

	public void destroy() {
		activity.unregisterReceiver(mReceiver);
	}

}
