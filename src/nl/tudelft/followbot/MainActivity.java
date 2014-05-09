package nl.tudelft.followbot;

import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.sensors.Accelerometer;
import nl.tudelft.followbot.sensors.SensorSink;
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;

public class MainActivity extends Activity {

	private Accelerometer accel;

	private final String TAG = "FollowBot";

	private final DataStack<float[]> accelStack = new DataStack<float[]>(512);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		accel = new Accelerometer(
				(SensorManager) getSystemService(Context.SENSOR_SERVICE));

		accel.addListener(new SensorSink() {
			@Override
			public void push(SensorEvent event) {
				float[] values = event.values;
				Log.d(TAG, values[0] + "," + values[1] + "," + values[2]);
			}
		});

		accel.addListener(new SensorSink() {
			@Override
			public void push(SensorEvent event) {
				accelStack.push(event.values);
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		accel.resume();
	}

	@Override
	protected void onPause() {
		super.onPause();
		accel.pause();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

}
