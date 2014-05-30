package nl.tudelft.followbot.sensors;

import java.util.Observable;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;

public class OrientationCalculator extends Observable implements SensorSink {

	float[] mGravity;
	float[] mGeomagnetic;
	float azimut;

	@Override
	public void push(SensorEvent event) {
		if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER)
			mGravity = event.values;
		if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD)
			mGeomagnetic = event.values;
		if (mGravity != null && mGeomagnetic != null) {
			float R[] = new float[9];
			float I[] = new float[9];
			boolean success = SensorManager.getRotationMatrix(R, I, mGravity,
					mGeomagnetic);
			if (success) {
				float orientation[] = new float[3];
				SensorManager.getOrientation(R, orientation);
				azimut = orientation[0]; // orientation contains:
											// azimut, pitch and roll
				setChanged();
				notifyObservers();
			}
		}
	}

	public float getAzimut() {
		return azimut;
	}
}
