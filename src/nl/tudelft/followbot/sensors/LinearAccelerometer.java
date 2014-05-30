package nl.tudelft.followbot.sensors;

import android.hardware.SensorManager;

public class LinearAccelerometer extends Sensor {

	public LinearAccelerometer(SensorManager sManager) {
		super(sManager);
	}

	@Override
	protected int getSensorType() {
		return android.hardware.Sensor.TYPE_LINEAR_ACCELERATION;
	}

}
