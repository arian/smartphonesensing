package nl.tudelft.followbot.sensors;

import java.util.ArrayList;

import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.util.Log;

public abstract class Sensor implements SensorEventListener {

	protected final SensorManager mSensorManager;
	protected final android.hardware.Sensor mSensor;
	private final ArrayList<SensorSink> listeners = new ArrayList<SensorSink>();

	public Sensor(SensorManager sManager) {
		mSensorManager = sManager;
		mSensor = mSensorManager.getDefaultSensor(getSensorType());
		if (mSensor != null) {
			Log.d("bla", "Don't have a sensor");
		}
	}

	abstract protected int getSensorType();

	public Sensor addListener(SensorSink listener) {
		listeners.add(listener);
		return this;
	}

	public Sensor removeListener(SensorSink listener) {
		listeners.remove(listener);
		return this;
	}

	public Sensor resume() {
		if (mSensor == null) {
			return this;
		}
		mSensorManager.registerListener(this, mSensor,
				SensorManager.SENSOR_DELAY_NORMAL);
		return this;
	}

	public Sensor pause() {
		if (mSensor == null) {
			return this;
		}
		mSensorManager.unregisterListener(this, mSensor);
		return this;
	}

	@Override
	public void onAccuracyChanged(android.hardware.Sensor sensor, int accuracy) {
	}

	@Override
	public void onSensorChanged(SensorEvent event) {
		for (SensorSink sink : listeners) {
			sink.push(event);
		}
	}

}
