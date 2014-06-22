package nl.tudelft.followbot.calibration;

import java.util.Observable;

import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.sensors.Sensor;
import nl.tudelft.followbot.sensors.SensorSink;
import nl.tudelft.followbot.timer.Periodical;
import android.hardware.SensorEvent;

/**
 * Store data from the linear acceleration meter for a period of time. This data
 * can be used to extract features from it.
 */
public class AccelerometerCalibration extends Observable {

	private final DataStack<float[]> data = new DataStack<float[]>(1500);

	Periodical periodical = new Periodical() {
		@Override
		public void run(long millis) {
			AccelerometerCalibration.this.end();
		}
	};

	SensorSink sensorListener = new SensorSink() {
		@Override
		public void push(SensorEvent event) {
			data.push(new float[] { (event.timestamp / 1e9f), event.values[0],
					event.values[1], event.values[2] });
		}
	};

	private final Sensor sensor;
	private final int time;

	private boolean running = false;

	public AccelerometerCalibration(Sensor snsr, int t) {
		sensor = snsr;
		time = t;
	}

	public void start() {
		if (running) {
			return;
		}
		periodical.delay(time);
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
