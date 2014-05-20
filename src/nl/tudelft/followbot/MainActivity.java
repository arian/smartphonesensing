package nl.tudelft.followbot;

import java.util.Observable;
import java.util.Observer;

import nl.tudelft.followbot.calibration.AccelerometerCalibration;
import nl.tudelft.followbot.camera.CameraEstimator;
import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.data.FeatureExtractor;
import nl.tudelft.followbot.knn.FeatureVector;
import nl.tudelft.followbot.knn.KNN;
import nl.tudelft.followbot.knn.KNNClass;
import nl.tudelft.followbot.sensors.Accelerometer;
import nl.tudelft.followbot.sensors.SensorSink;

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
import android.widget.Button;

public class MainActivity extends Activity {
	private MenuItem mItemPreviewRGBA;
	private MenuItem mItemPreviewThresh;
	private MenuItem mItemPreviewOdRGBA;

	private final CameraEstimator cameraEstimation = new CameraEstimator();

	private Accelerometer accel;

	private FeatureVector standFeature;
	private FeatureVector walkFeature;
	private final KNNClass standClass = new KNNClass("stand");
	private final KNNClass walkClass = new KNNClass("walk");
	private final KNN knn = new KNN();

	private final String TAG = "FollowBot";

	private final DataStack<float[]> accelStack = new DataStack<float[]>(512);

	private final BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(
			this) {
		@Override
		public void onManagerConnected(int status) {
			switch (status) {
			case LoaderCallbackInterface.SUCCESS: {
				// Log.i(TAG, "OpenCV loaded successfully");

				// Load native library after(!) OpenCV initialization
				System.loadLibrary("object_tracking");

				cameraEstimation.enableCamera();
			}
				break;
			default: {
				super.onManagerConnected(status);
			}
				break;
			}
		}
	};

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);

		accel = new Accelerometer(
				(SensorManager) getSystemService(Context.SENSOR_SERVICE));

		accel.addListener(new SensorSink() {
			@Override
			public void push(SensorEvent event) {
				accelStack.push(new float[] { event.timestamp, event.values[0],
						event.values[1], event.values[2] });
			}
		});

		// open new camera view
		cameraEstimation.openCameraView(
				(CameraBridgeViewBase) findViewById(R.id.surface_view), 480,
				360);
	}

	@Override
	protected void onPause() {
		super.onPause();

		cameraEstimation.disableCamera();

		if (accel != null) {
			accel.pause();
		}
	}

	@Override
	protected void onResume() {
		super.onResume();

		OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_3, this,
				mLoaderCallback);

		if (accel != null) {
			accel.resume();
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		cameraEstimation.disableCamera();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);

		mItemPreviewRGBA = menu.add("Preview RGBA");
		mItemPreviewThresh = menu.add("Object Detection Threshold");
		mItemPreviewOdRGBA = menu.add("Object Detection RGBA");

		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		Log.i(TAG, "called onOptionsItemSelected; selected item: " + item);

		if (item == mItemPreviewRGBA) {
			cameraEstimation.setViewMode(CameraEstimator.VIEW_MODE_RGBA);
		} else if (item == mItemPreviewThresh) {
			cameraEstimation.setViewMode(CameraEstimator.VIEW_MODE_THRESH);
		} else if (item == mItemPreviewOdRGBA) {
			cameraEstimation.setViewMode(CameraEstimator.VIEW_MODE_OD_RGBA);
		}

		return true;
	}

	public void onClickCalStand(View view) {
		Button btn = (Button) view;
		final int calibrationTime = 4;
		final AccelerometerCalibration cal = new AccelerometerCalibration(
				accel, calibrationTime, btn);

		cal.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				standFeature = new FeatureVector(standClass, FeatureExtractor
						.extractFeaturesFromFloat4(cal.getData()));
				knn.add(standFeature);
			}
		});

		cal.start();
	}

	public void onClickCalWalk(View view) {
		Button btn = (Button) view;
		final int calibrationTime = 10;
		final AccelerometerCalibration cal = new AccelerometerCalibration(
				accel, calibrationTime, btn);

		cal.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				walkFeature = new FeatureVector(walkClass, FeatureExtractor
						.extractFeaturesFromFloat4(cal.getData()));
				knn.add(walkFeature);
			}
		});

		cal.start();
	}

	public void onClickDetectActivity(View view) {
		FeatureVector feature = new FeatureVector(null,
				FeatureExtractor.extractFeaturesFromFloat4(accelStack));
		KNNClass klass = knn.classify(feature, 1);
		Log.d(TAG, klass.getName());
	}
}
