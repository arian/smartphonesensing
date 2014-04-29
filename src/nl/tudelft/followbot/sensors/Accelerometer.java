package nl.tudelft.followbot.sensors;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public class Accelerometer implements SensorEventListener {

	private final SensorManager mSensorManager;
	private final Sensor mSensor;

	public Accelerometer(SensorManager sManager) {
		mSensorManager = sManager;
		mSensor = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		if (mSensor != null) {
			Log.d("bla", "Don't have an accelerometer");
		}
	}

	public void resume() {
		if (mSensor == null) {
			return;
		}
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
	}

	public void pause() {
		if (mSensor == null) {
			return;
		}
		mSensorManager.unregisterListener(this, mSensor);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		Log.d("bla", event.values[0] + "," + event.values[1] + ","
				+ event.values[2]);
	}

}
