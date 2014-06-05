package nl.tudelft.followbot.calibration;

import java.util.Observable;

import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.sensors.LinearAccelerometer;
import nl.tudelft.followbot.sensors.SensorSink;
import nl.tudelft.followbot.timer.Periodical;
import android.hardware.SensorEvent;
import android.util.Log;

public class AccelerometerCalibration extends Observable {

	private final DataStack<float[]> data = new DataStack<float[]>(1500);

	Periodical periodical = new Periodical() {
		@Override
		public void run(long millis) {
			AccelerometerCalibration.this.end();
			Log.d("FollowBot", millis + "");
		}
	};

	SensorSink sensorListener = new SensorSink() {
		@Override
		public void push(SensorEvent event) {
			data.push(new float[] { event.timestamp, event.values[0],
					event.values[1], event.values[2] });
		}
	};

	private final LinearAccelerometer sensor;

	private boolean running = false;

	public AccelerometerCalibration(LinearAccelerometer snsr, int time) {
		sensor = snsr;
	}

	public void start() {
		if (running) {
			return;
		}
		periodical.delay(5000);
		sensor.addListener(sensorListener);
		running = true;
	}

	public void end() {
		if (!running) {
			return;
		}
		sensor.removeListener(sensorListener);
		setChanged();
		notifyObservers();
		running = true;
	}

	public DataStack<float[]> getData() {
		return data;
	}

}
