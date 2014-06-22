package nl.tudelft.followbot.ioio;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.DigitalOutput.Spec.Mode;
import ioio.lib.api.PwmOutput;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import android.util.Log;

public class MotorController extends BaseIOIOLooper {
	// Inverted due to open-drain settings
	private static final float MOTOR_SPEED_FORWARD = (float) 0.2;
	private static final float MOTOR_SPEED_ROTATE = (float) 0.0;

	public static final int ROBOT_MOVE_FORWARD = 1;
	public static final int ROBOT_ROTATE_LEFT = 2;
	public static final int ROBOT_ROTATE_RIGHT = 3;

	private static float rightSideSpeed;
	private static float leftSideSpeed;

	private PwmOutput rightSide;
	private PwmOutput leftSide;

	@Override
	public void setup() throws ConnectionLostException {
		rightSide = ioio_.openPwmOutput(new DigitalOutput.Spec(12,
				Mode.OPEN_DRAIN), 1000);
		leftSide = ioio_.openPwmOutput(new DigitalOutput.Spec(13,
				Mode.OPEN_DRAIN), 1000);

		Log.d("IOIO", "IOIO setup complete");
	}

	@Override
	public void loop() throws ConnectionLostException, InterruptedException {
		rightSide.setDutyCycle(rightSideSpeed);
		leftSide.setDutyCycle(leftSideSpeed);

		Thread.sleep(10);
	}

	@Override
	public void disconnected() {
	}

	public static void robotMove(int movement) {
		if (movement == ROBOT_MOVE_FORWARD) {
			rightSideSpeed = MOTOR_SPEED_FORWARD;
			leftSideSpeed = MOTOR_SPEED_FORWARD;
		} else if (movement == ROBOT_ROTATE_LEFT) {
			rightSideSpeed = 1 - MOTOR_SPEED_ROTATE;
			leftSideSpeed = MOTOR_SPEED_ROTATE;
		} else if (movement == ROBOT_ROTATE_RIGHT) {
			rightSideSpeed = MOTOR_SPEED_ROTATE;
			leftSideSpeed = 1 - MOTOR_SPEED_ROTATE;
		}
	}
}
