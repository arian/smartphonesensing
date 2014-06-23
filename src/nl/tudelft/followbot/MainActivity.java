package nl.tudelft.followbot;

import ioio.lib.util.android.IOIOActivity;

import java.util.Observable;
import java.util.Observer;

import nl.tudelft.followbot.camera.CameraEstimator;
import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.data.FeatureExtractor;
import nl.tudelft.followbot.filters.particle.Filter;
import nl.tudelft.followbot.ioio.MotorController;
import nl.tudelft.followbot.knn.FeatureVector;
import nl.tudelft.followbot.plot.ScatterPlotView;
import nl.tudelft.followbot.sensors.LinearAccelerometer;
import nl.tudelft.followbot.sensors.OrientationCalculator;
import nl.tudelft.followbot.sensors.SensorSink;
import nl.tudelft.followbot.timer.Periodical;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.content.Context;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MainActivity extends IOIOActivity {
	/**
	 * Reference distance in [m] for the robot to track
	 */
	private static final double REFERENCE_DISTANCE = 0.5;
	/**
	 * Tolerance for robot reference tracking
	 */
	private static final double TOLERANCE_DISTANCE_TRACKING = 0.3;
	/**
	 * Tolerance for robot orientation angle tracking
	 */
	private static final double TOLERANCE_ORIENTATION_TRACKING = 0.1;
	/**
	 * Standard deviation of a camera distance measurement
	 */
	private static final double MEASURE_DISTANCE_NOISE = 0.2;
	/**
	 * std dev of the robot orientation measurement in rad
	 */
	private static final double MEASURE_ORIENTATION_NOISE = 1.5;
	/**
	 * Standard deviation of a camera heading measurement
	 */
	private static final double MEASURE_HEADING_NOISE = 0.05;
	/**
	 * std dev of the user orientation, which updates the particle filter
	 */
	private static final double USER_ROTATION_NOISE = 0.05; // radians
	/**
	 * when the knn classifier detects walking, this is the assumed walking
	 * speed
	 */
	private static final double USER_MOVE_SPEED = 0.4; // meters / second
	/**
	 * this is the standard deviation of the walking speed.
	 */
	private static final double USER_MOVE_NOISE = 0.05;
	/**
	 * this is the standard deviation of the standing speed.
	 */
	private static final double USER_STAND_SPEED = 0.0; // meters / second
	/**
	 * this is the standard deviation of the standing speed.
	 */
	private static final double USER_STAND_NOISE = 0.005;
	/**
	 * Number of particles in the filter
	 */
	private static final int FILTER_PARTICLES_COUNT = 1000;
	/**
	 * Initial radius in which the initial particles are distributed
	 */
	private static final double FILTER_PARTICLES_INITIAL_DISTANCE = 2; // meters
	/**
	 * Period the values are measured and controlled [ms]
	 */
	private static final int MEASURE_PERIOD = 250;
	/**
	 * Speed of the robot in [m/s]
	 */
	private static final float ROBOT_SPEED = 0.27f;
	/**
	 * Speed of the robot in [m/s]
	 */
	private static final float ROBOT_MOVE_NOISE = 0.02f;
	/**
	 * Tag for Log.d debug logs
	 */
	private final String TAG = "FollowBot";
	/**
	 * Camera Estimator communicates with the OpenCV binding which writes values
	 * to this object, so they can be read here
	 */
	private final CameraEstimator cameraEstimation = new CameraEstimator();
	/**
	 * Latest 128 acceleration data values
	 */
	private final DataStack<float[]> accelStack = new DataStack<float[]>(128);
	/**
	 * Acceleration sensor object. This uses the accelerometer. A callback is
	 * added that pushes values to the accelStack data stack
	 */
	private LinearAccelerometer accel;
	/**
	 * Combines acceleration and gravity sensor to calculate the orientation
	 * (yaw) of the phone. Listeners can be added
	 */
	private OrientationCalculator orienCalc;
	/**
	 * Last measured yaw / orientation
	 */
	private float yaw;
	/**
	 * Previous measured yaw
	 */
	private double pYaw = Double.MIN_VALUE;
	/**
	 * Activiy Monitor
	 */
	private ActivityMonitor activityMonitor;
	/**
	 * View element which is used to draw a scatter plot
	 */
	private ScatterPlotView plotView;
	/**
	 * Whether the KNN Classifier or Particle filter should be plotted
	 */
	private boolean plotKNN = false;
	/**
	 * The Particle Filter object
	 */
	private Filter filter;

	private boolean started = false;

	/**
	 * Creates a IOIO loop that interfaces with the IOIO board via Bluetooth and
	 * controls the motor speeds
	 */
	@Override
	protected MotorController createIOIOLooper() {
		return new MotorController();
	}

	/**
	 * Periodical task to measure values and to sense user activity.
	 */
	private final Periodical measurePeriodical = new Periodical() {
		@Override
		public void run(long millis) {
			controlRobot(millis);
			senseUserActivity(millis);
		}
	};

	/**
	 * Periodical task to plot the particle filter or the KNN data points
	 */
	private final Periodical plotPeriodical = new Periodical() {

		@Override
		public void run(long millis) {
			double[][] data = plotKNN ? activityMonitor.getKNNData() : filter
					.getPositions();
			plotView.plot(data);

			double distance = filter.getDistanceEstimate();
			double orientation = filter.getOrientationEstimate();

			TextView tv = (TextView) findViewById(R.id.robot_estimation);
			tv.setText(String
					.format("%3.2f m %3.2f rad", distance, orientation));

		}
	};

	/**
	 * OpenCV initialization
	 */
	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(
			this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS:
				// Load native library after(!) OpenCV initialization
				System.loadLibrary("object_tracking");
				cameraEstimation.enableCamera();
				break;
			default:
				super.onManagerConnected(status);
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

		// store latest acceleration data
		accel = new LinearAccelerometer(sm);
		accel.addListener(new SensorSink() {
			@Override
			public void push(SensorEvent event) {
				accelStack.push(new float[] { (float) (event.timestamp / 1e9),
						event.values[0], event.values[1], event.values[2] });
			}
		});

		// save current orientation/yaw
		orienCalc = new OrientationCalculator(sm);
		orienCalc.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				yaw = ((OrientationCalculator) observable).getYaw();
			}
		});

		// open new camera view
		cameraEstimation.openCameraView(
				(CameraBridgeViewBase) findViewById(R.id.surface_view), 480,
				360);

		// The partial filter
		filter = new Filter().fill(FILTER_PARTICLES_COUNT,
				FILTER_PARTICLES_INITIAL_DISTANCE);

		// activity monitor
		activityMonitor = new ActivityMonitor(this, accel);
		activityMonitor.loadFromFile();

		// plotter
		plotView = new ScatterPlotView(this);
		LinearLayout layout = (LinearLayout) findViewById(R.id.chart);
		layout.addView(plotView);
	}

	@Override
	protected void onResume() {
		super.onResume();

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

		if (accel != null) {
			accel.resume();
		}
		if (orienCalc != null) {
			orienCalc.resume();
		}
	}

	@Override
	protected void onPause() {
		super.onPause();

		cameraEstimation.disableCamera();

		if (accel != null) {
			accel.pause();
		}
		if (orienCalc != null) {
			orienCalc.pause();
		}

		measurePeriodical.end();
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		cameraEstimation.disableCamera();
		measurePeriodical.end();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	/**
	 * Actions for the menu
	 */
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_preview_rgba:
			cameraEstimation.setViewMode(CameraEstimator.VIEW_MODE_RGBA);
			break;
		case R.id.action_detection_threshold:
			cameraEstimation.setViewMode(CameraEstimator.VIEW_MODE_THRESH);
			break;
		case R.id.action_detection_rgba:
			cameraEstimation.setViewMode(CameraEstimator.VIEW_MODE_OD_RGBA);
			break;
		case R.id.action_cal_stand:
			activityMonitor.calibrateStandActivity();
			break;
		case R.id.action_cal_walk:
			activityMonitor.calibrateWalkActivity();
			break;
		case R.id.action_clear_cal:
			activityMonitor.clear();
		case R.id.action_save_cal:
			activityMonitor.saveToFile();
			break;
		case R.id.action_switch_plot:
			plotKNN = !plotKNN;
			break;
		}
		return true;
	}

	public void senseUserActivity(long millis) {
		/*
		 * Use the KNN Classifier to detect the activity (walking/standing) and
		 * update the particle filter accordingly
		 */
		FeatureVector feature = new FeatureVector(null,
				FeatureExtractor.extractFeaturesFromFloat4(accelStack));

		activityMonitor.detectActivity(feature);

		if (activityMonitor.isWalking() || activityMonitor.isStanding()) {
			TextView tv = (TextView) findViewById(R.id.activity_output);
			tv.setText(activityMonitor.getClassName());

			if (millis > 0 && activityMonitor.isWalking()) {
				Log.d(TAG, "" + (millis / 1000.0 * USER_MOVE_SPEED));
				filter.userMove(millis / 1000.0 * USER_MOVE_SPEED,
						USER_MOVE_NOISE);
				return;
			}
		}

		if (millis > 0) {
			filter.userMove(millis / 1000.0 * USER_STAND_SPEED,
					USER_STAND_NOISE);
		}

		/*
		 * Sense user rotate and apply it to particles only if user standing
		 */
		if (pYaw != Double.MIN_VALUE && activityMonitor.isStanding()) {
			double diff = pYaw - yaw;
			// applies to particle filter
			filter.userRotate(diff, USER_ROTATION_NOISE);
		}
		pYaw = yaw;
	}

	public void startStop(View view) {
		if (started) {
			measurePeriodical.end();
			plotPeriodical.end();
			MotorController.robotMove(MotorController.ROBOT_STOP);
			started = false;
		} else {
			measurePeriodical.start(MEASURE_PERIOD);
			plotPeriodical.start(100);
			started = true;
		}
	}

	/**
	 * Control robot based on obtained estimations from the particle filter
	 */
	public void controlRobot(long millis) {

		// if the robot is seen by the camera, then use measurements to
		// update the particle filter
		if (cameraEstimation.robotSeen()) {

			/*
			 * measure and resample
			 */
			double d = cameraEstimation.getDistance() / 100;
			double a = cameraEstimation.getOrientation();

			// camera distance measurement with x [m] deviation
			filter.distanceMeasurement(d, MEASURE_DISTANCE_NOISE);
			filter.resample();

			// camera orientation measurement with x [rad] deviation
			filter.orientationMeasurement(a, MEASURE_ORIENTATION_NOISE);
			filter.resample();

			filter.headingMeasurement(0, MEASURE_HEADING_NOISE);
			filter.resample();
		}

		// filtered values
		double fd = filter.getDistanceEstimate();
		double fa = filter.getOrientationEstimate();

		if (true || MotorController.ioioConnected) {
			((TextView) findViewById(R.id.ioio_status))
					.setText("IOIO CONNECTED");

			if (false && (Math.abs(fa) > TOLERANCE_ORIENTATION_TRACKING)) {
				// if the robot is pointing towards the right -> make it //
				// turn left;
				// otherwise -> make it turn right
				if (fa > 0) {
					// IOIO motor control (rotate robot)
					MotorController
							.robotMove(MotorController.ROBOT_ROTATE_LEFT);

					// Same as with moving forward: we know how much it
					// turns between samples -> we can update particles:
					// 10 degrees per sample
				} else {
					// IOIO motor control (rotate robot)
					MotorController
							.robotMove(MotorController.ROBOT_ROTATE_RIGHT);

					// Same as with moving forward: we know how much it
					// turns between samples -> we can update particles:
					// -10 degrees per sample
				}
			} else if (fd > REFERENCE_DISTANCE + TOLERANCE_DISTANCE_TRACKING) {

				// IOIO motor control (make robot go forward)
				MotorController.robotMove(MotorController.ROBOT_MOVE_FORWARD);

				// Knowing the traveled distance of the robot at each time
				// instant, we can update the particles in the filter
				// E.g.: if we sample every 100 ms -> robot moves 10 cm in
				// that time
				filter.robotMove(ROBOT_SPEED * millis / 1000.0,
						ROBOT_MOVE_NOISE);

				((TextView) findViewById(R.id.robot_action)).setText("FORWARD");

				// filter.robotMove(ROBOT_SPEED * millis / 1000.0, 0.05);
			} else {
				MotorController.robotMove(MotorController.ROBOT_STOP);
				((TextView) findViewById(R.id.robot_action)).setText("STOP");
				((TextView) findViewById(R.id.ioio_status))
						.setText("IOIO DISCONNECTED");
			}
		}
	}
}
