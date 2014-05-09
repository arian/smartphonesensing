package nl.tudelft.followbot.sensors;

import android.hardware.SensorEvent;

public interface SensorSink {

	void push(SensorEvent event);

}
