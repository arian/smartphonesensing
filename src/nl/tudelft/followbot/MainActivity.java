package nl.tudelft.followbot;

import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import nl.tudelft.followbot.calibration.AccelerometerCalibration;
import nl.tudelft.followbot.camera.CameraEstimator;
import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.data.FeatureExtractor;
import nl.tudelft.followbot.filters.particle.Filter;
import nl.tudelft.followbot.knn.FeatureVector;
import nl.tudelft.followbot.knn.KNN;
import nl.tudelft.followbot.knn.KNNClass;
import nl.tudelft.followbot.plot.ScatterPlotView;
import nl.tudelft.followbot.sensors.LinearAccelerometer;
import nl.tudelft.followbot.sensors.OrientationCalculator;
import nl.tudelft.followbot.sensors.SensorSink;
import nl.tudelft.followbot.timer.Periodical;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;

import android.app.Activity;
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
import android.widget.Toast;

public class MainActivity extends Activity {

	/**
	 * Standard deviation of a camera distance measurement
	 */
	private static final double MEASURE_DISTANCE_NOISE = 0.10;
	/**
	 * Standard deviation of a camera heading measurement
	 */
	private static final double MEASURE_HEADING_NOISE = 0.10;
	/**
	 * std dev of the user orientation, which updates the particle filter
	 */
	private static final double USER_ROTATION_NOISE = 0.05; // radians
	/**
	 * when the knn classifier detects walking, this is the assumed walking
	 * speed
	 */
	private static final double USER_MOVE_SPEED = 1.0; // meters / second
	/**
	 * this is the standard deviation of the walking speed.
	 */
	private static final double USER_MOVE_NOISE = 0.1;
	/**
	 * Number of particles in the filter
	 */
	private static final int FILTER_PARTICLES_COUNT = 100;
	/**
	 * Initial radius in which the initial particles are distributed
	 */
	private static final double FILTER_PARTICLES_INITIAL_DISTANCE = 2; // meters
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
	 * KNN Classifier
	 */
	private final KNN knn = new KNN();
	/**
	 * KNN Class object for the "stand" activity
	 */
	private final KNNClass standClass = new KNNClass("stand");
	/**
	 * KNN Class object for the "walk" activity
	 */
	private final KNNClass walkClass = new KNNClass("walk");
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
	/**
	 * Periodical task to measure values and to sense user activity.
	 */
	private final Periodical measurePeriodical = new Periodical() {
		@Override
		public void run(long millis) {
			measureRobot();
			senseUserActivity(millis);
		}
	};
	/**
	 * Periodical task to plot the particle filter or the KNN data points
	 */
	private final Periodical plotPeriodical = new Periodical() {

		public double[][] getKNNData() {
			float[][] fs = knn.getNormalizedFeatures();
			ArrayList<FeatureVector> fvs = knn.getFeatures();
			double[][] d = new double[3][fs.length];
			for (int i = 0; i < fs.length; i++) {
				float[] f = fs[i];
				d[0][i] = f[0];
				d[1][i] = f[1];
				d[2][i] = (fvs.get(i).getKNNClass() == walkClass) ? 1 : 0;
			}
			return d;
		}

		@Override
		public void run(long millis) {
			plotView.plot(plotKNN ? getKNNData() : filter.getPositions());
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
				// Log.i(TAG, "OpenCV loaded successfully");
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
				accelStack.push(new float[] { event.timestamp, event.values[0],
						event.values[1], event.values[2] });
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

		measurePeriodical.start(500);
		plotPeriodical.start(500);

		// // simulate heading measurement
		// new Periodical() {
		// @Override
		// public void run(long millis) {
		// filter.headingMeasurement(0.0, 0.05);
		// filter.resample();
		// }
		// }.delay(4000);
		//
		// // simulate distance measurement
		// new Periodical() {
		// @Override
		// public void run(long millis) {
		// filter.distanceMeasurement(6, 0.5);
		// filter.resample();
		// }
		// }.delay(6000);
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
			calibrateActivity(standClass,
					getString(R.string.toast_stand_finished));
			break;
		case R.id.action_cal_walk:
			calibrateActivity(walkClass,
					getString(R.string.toast_walk_finished));
			break;
		case R.id.action_clear_cal:
			knn.clear();
		case R.id.action_switch_plot:
			plotKNN = !plotKNN;
			break;
		}
		return true;
	}

	/**
	 * Add new data point to the activity detection KNN classifier
	 * 
	 * @param klass
	 * @param msg
	 */
	private void calibrateActivity(final KNNClass klass, final CharSequence msg) {
		final int calibrationTime = 4 * 1000;
		final AccelerometerCalibration cal = new AccelerometerCalibration(
				accel, calibrationTime);
		final Context context = getApplicationContext();

		cal.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				DataStack<float[]> ds = cal.getData();
				float[] d = FeatureExtractor.extractFeaturesFromFloat4(ds);
				FeatureVector feature = new FeatureVector(klass, d);
				knn.add(feature);
				Toast.makeText(context, msg, Toast.LENGTH_SHORT).show();
			}
		});
		cal.start();
	}

	public void onClickDetectActivity(View view) {
		detectActivity(0);
	}

	public void senseUserActivity(long millis) {
		detectActivity(millis);
		senseUserRotate();
	}

	/**
	 * Use the KNN Classifier to detect the activity (walking/standing) and
	 * update the particle filter accordingly
	 * 
	 * @param millis
	 */
	public void detectActivity(long millis) {
		FeatureVector feature = new FeatureVector(null,
				FeatureExtractor.extractFeaturesFromFloat4(accelStack));

		// classify measured values, with KNN classifier. Take 3 points
		KNNClass klass = knn.classify(feature, 3);

		if (klass != null) { // if there was calibration before
			TextView tv = (TextView) findViewById(R.id.activity_output);
			tv.setText(klass.getName());

			if (millis > 0 && klass == walkClass) {
				filter.userMove(1000 / millis * USER_MOVE_SPEED,
						USER_MOVE_NOISE);
			}
		}
	}

	/**
	 * Update particle filter for user rotations
	 */
	public void senseUserRotate() {
		if (pYaw != Double.MIN_VALUE) {
			double diff = pYaw - yaw;
			// applies to particle filter
			filter.userRotate(diff, USER_ROTATION_NOISE);
		}
		pYaw = yaw;
	}

	/**
	 * Read camera measurements to add weights to particles and resample
	 */
	public void measureRobot() {

		// if the robot is seen by the camera, then use measurements to
		// update the particle filter
		if (cameraEstimation.robotSeen()) {

			float d = cameraEstimation.getDistance();
			float a = cameraEstimation.getOrientation();

			Log.d(TAG, "-> " + d + " @ " + a);

			// camera distance measurement with x [m] deviation
			filter.distanceMeasurement(d, MEASURE_DISTANCE_NOISE);
			filter.resample();

			// camera orientation measurement with x [rad] deviation
			filter.orientationMeasurement(a, MEASURE_HEADING_NOISE);
			filter.resample();
		}
	}

}
