package nl.tudelft.followbot;

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

	private final String TAG = "FollowBot";

	private final CameraEstimator cameraEstimation = new CameraEstimator();

	private final DataStack<float[]> accelStack = new DataStack<float[]>(512);

	private LinearAccelerometer accel;
	private OrientationCalculator orienCalc;

	private final KNNClass standClass = new KNNClass("stand");
	private final KNNClass walkClass = new KNNClass("walk");
	private final KNN knn = new KNN();

	// PlotAChartEngine plotter;
	private ScatterPlotView plotView;

	private Filter filter;

	private float yaw;

	private final Periodical measurePeriodical = new Periodical() {

		private double pYaw = Double.MIN_VALUE;

		@Override
		public void run(long millis) {
			detectActivity();
			if (pYaw != Double.MIN_VALUE) {
				double diff = pYaw - yaw;
				filter.userRotate(diff);
			}
			pYaw = yaw;
		}
	};

	private final Periodical plotPeriodical = new Periodical() {
		@Override
		public void run(long millis) {
			plotView.plot(filter.getPositions());
		}
	};

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

		accel = new LinearAccelerometer(sm);
		accel.addListener(new SensorSink() {
			@Override
			public void push(SensorEvent event) {
				accelStack.push(new float[] { event.timestamp, event.values[0],
						event.values[1], event.values[2] });
			}
		});

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

		// plotter = new PlotAChartEngine(this, );

		plotView = new ScatterPlotView(this);

		filter = new Filter(100, 25);

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

		measurePeriodical.start(100);
		plotPeriodical.start(100);
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
			onClickCalibrate(standClass,
					getString(R.string.toast_stand_finished));
			break;
		case R.id.action_cal_walk:
			onClickCalibrate(walkClass, getString(R.string.toast_walk_finished));
			break;
		}
		return true;
	}

	private void onClickCalibrate(final KNNClass klass, final CharSequence msg) {
		final int calibrationTime = 4;
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
		detectActivity();
	}

	public void detectActivity() {
		FeatureVector feature = new FeatureVector(null,
				FeatureExtractor.extractFeaturesFromFloat4(accelStack));
		KNNClass klass = knn.classify(feature, 1);
		if (klass != null) { // if there was no calibration before
			Log.d(TAG, klass.getName());
			TextView tv = (TextView) findViewById(R.id.activity_output);
			tv.setText(klass.getName());
		}
	}
}
