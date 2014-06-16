package nl.tudelft.followbot.timer;

import android.os.Handler;

public class Periodical {

	long previousTime;
	long period;
	boolean delay;

	Handler timerHandler = new Handler();

	Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			long time = System.currentTimeMillis();
			Periodical.this.run(time - previousTime);
			previousTime = time;
			if (delay) {
				end();
			} else {
				timerHandler.postDelayed(timerRunnable, period);
			}
		}
	};

	public void run(long millis) {
	}

	public void start(long p, boolean d) {
		if (p < 0) {
			throw new IllegalArgumentException(
					"Period should be greater than zero");
		}
		period = p;
		delay = d;
		previousTime = System.currentTimeMillis();
		timerHandler.postDelayed(timerRunnable, d ? p : 0);
	}

	public void start(long p) {
		start(p, false);
	}

	public void delay(long p) {
		start(p, true);
	}

	public void end() {
		timerHandler.removeCallbacks(timerRunnable);
	}

}
