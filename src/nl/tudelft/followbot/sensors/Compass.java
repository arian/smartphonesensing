package nl.tudelft.followbot.sensors;

import android.hardware.SensorManager;

public class Compass extends Sensor {

	public Compass(SensorManager sManager) {
		super(sManager);
	}

	@Override
	protected int getSensorType() {
		return android.hardware.Sensor.TYPE_MAGNETIC_FIELD;
	}

}
