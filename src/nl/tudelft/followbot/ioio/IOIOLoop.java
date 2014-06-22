package nl.tudelft.followbot.ioio;

import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import android.util.Log;

public class IOIOLoop extends BaseIOIOLooper {
	public static float pwmDutyCycle1;
	public static float pwmDutyCycle2;
	public static float pwmDutyCycle3;
	public static float pwmDutyCycle4;

	private PwmOutput pwmOutput1;
	private PwmOutput pwmOutput2;
	private PwmOutput pwmOutput3;
	private PwmOutput pwmOutput4;

	@Override
	public void setup() throws ConnectionLostException {
		pwmOutput1 = ioio_.openPwmOutput(11, 100);
		pwmOutput2 = ioio_.openPwmOutput(12, 100);
		pwmOutput3 = ioio_.openPwmOutput(13, 100);
		pwmOutput4 = ioio_.openPwmOutput(14, 100);

		Log.d("IOIO", "IOIO setup complete");
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		pwmOutput1.setDutyCycle(pwmDutyCycle1);
		pwmOutput2.setDutyCycle(pwmDutyCycle2);
		pwmOutput3.setDutyCycle(pwmDutyCycle3);
		pwmOutput4.setDutyCycle(pwmDutyCycle4);

		Thread.sleep(10);
	}

	@Override
	public void disconnected() {
	}

}
