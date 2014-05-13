package nl.tudelft.followbot.calibration;

import java.util.Observable;

import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.sensors.Accelerometer;
import nl.tudelft.followbot.sensors.SensorSink;
import android.hardware.SensorEvent;
import android.os.Handler;
import android.util.Log;
import android.widget.Button;

public class AccelerometerCalibration extends Observable {

	private final DataStack<float[]> data = new DataStack<float[]>(500);

	Handler timerHandler = new Handler();

	Runnable timerRunnable = new Runnable() {

		@Override
		public void run() {
			long millis = System.currentTimeMillis() - startTime;
			int seconds = (int) (millis / 1000);
			button.setText(String.format("%d", timeToRun - seconds));
			if (seconds < timeToRun) {
				timerHandler.postDelayed(this, 500);
			} else {
				end();
			}
		}

	};

	SensorSink sensorListener = new SensorSink() {
		@Override
		public void push(SensorEvent event) {
			data.push(event.values);
		}
	};

	private long startTime;

	private CharSequence buttonText;

	private final Button button;

	private final Accelerometer sensor;

	private final int timeToRun;

	private boolean running = false;

	public AccelerometerCalibration(Accelerometer snsr, int time, Button btn) {
		sensor = snsr;
		timeToRun = time;
		button = btn;
	}

	public void start() {
		if (running) {
			return;
		}
		buttonText = button.getText();
		startTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerRunnable, 0);
		sensor.addListener(sensorListener);
		running = true;
	}

	public void end() {
		if (!running) {
			return;
		}
		timerHandler.removeCallbacks(timerRunnable);
		button.setText(buttonText);
		sensor.removeListener(sensorListener);
		Log.d("Calibration", data.getSize() + "");
		notifyObservers();
		running = true;
	}

	public DataStack<float[]> getData() {
		return data;
	}

}
