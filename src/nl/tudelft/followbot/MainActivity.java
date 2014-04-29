package nl.tudelft.followbot;

import nl.tudelft.followbot.sensors.Accelerometer;
import android.app.Activity;
import android.content.Context;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.Menu;

public class MainActivity extends Activity {

	private Accelerometer accel;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		accel = new Accelerometer(
				(SensorManager) getSystemService(Context.SENSOR_SERVICE));
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
