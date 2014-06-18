package nl.tudelft.followbot.sensors;

import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class OrientationCalculator extends Observable implements SensorSink {

	float[] mGravity;
	float[] mGeomagnetic;

	float orientation[] = new float[3];

	private final Accelerometer accel;
	private final Compass compass;

	public OrientationCalculator(SensorManager sm) {
		accel = new Accelerometer(sm);
		compass = new Compass(sm);

		accel.addListener(this);
		compass.addListener(this);
	}

	public void resume() {
		accel.resume();
		compass.resume();
	}

	public void pause() {
		accel.pause();
		compass.pause();
	}

	/**
	 * @link http://stackoverflow.com/a/6804786/430730
	 */
	@Override
	public void push(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
			mGravity = event.values;
		} else if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
			mGeomagnetic = event.values;
		}

		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				SensorManager.getOrientation(R, orientation);

				setChanged();
				notifyObservers();
			}
		}
	}

	public float getYaw() {
		return orientation[0];
	}

	public float getRoll() {
		return orientation[1];
	}

	public float getPitch() {
		return orientation[2];
	}
}
