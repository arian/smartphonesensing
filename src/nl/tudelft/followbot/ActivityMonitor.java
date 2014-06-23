package nl.tudelft.followbot;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

import nl.tudelft.followbot.calibration.AccelerometerCalibration;
import nl.tudelft.followbot.data.DataStack;
import nl.tudelft.followbot.data.FeatureExtractor;
import nl.tudelft.followbot.knn.FeatureVector;
import nl.tudelft.followbot.knn.KNN;
import nl.tudelft.followbot.knn.KNNClass;
import nl.tudelft.followbot.sensors.Sensor;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class ActivityMonitor {

	private final String ACTIVITY_FILE = "activity.csv";

	private final String DEBUG_TAG = "ActivityMonitor";

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
	 * Current class
	 */
	private KNNClass klass;
	/**
	 * App Activity context
	 */
	private final Context ctx;
	/**
	 * Acceleration Sensor
	 */
	private final Sensor accel;

	public ActivityMonitor(Context ctx, Sensor accel) {
		this.ctx = ctx;
		this.accel = accel;
	}

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

	public boolean isWalking() {
		return (klass == walkClass);
	}

	public boolean isStanding() {
		return (klass == standClass);
	}

	public void calibrateStandActivity() {
		calibrateActivity(standClass,
				ctx.getString(R.string.toast_stand_finished));
	}

	public void calibrateWalkActivity() {
		calibrateActivity(walkClass,
				ctx.getString(R.string.toast_walk_finished));
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

		cal.addObserver(new Observer() {
			@Override
			public void update(Observable observable, Object data) {
				DataStack<float[]> ds = cal.getData();
				float[] d = FeatureExtractor.extractFeaturesFromFloat4(ds);

				LogFile.appendLog(new File(ctx.getExternalCacheDir(),
						"logs5.csv"), String.format("%f,%f,%d", d[0], d[1],
						klass == walkClass ? 1 : 0));

				FeatureVector feature = new FeatureVector(klass, d);
				knn.add(feature);
				Toast.makeText(ctx, msg, Toast.LENGTH_SHORT).show();
			}
		});
		cal.start();
	}

	public void detectActivity(FeatureVector feature) {
		// classify measured values, with KNN classifier. Take 3 points
		klass = knn.classify(feature, 3);
	}

	public String getClassName() {
		if (klass != null) {
			return klass.getName();
		}
		return null;
	}

	public float[] getNormalizedFeatureValues(FeatureVector feature) {
		float[] f = knn.normalizeFeature(feature);
		return f;
	}

	public void clear() {
		knn.clear();
	}

	public void loadFromFile() {
		File file = new File(ctx.getExternalFilesDir("act"), ACTIVITY_FILE);
		try {

			if (!file.exists()) {
				return;
			}

			BufferedReader buf = new BufferedReader(new FileReader(file));

			String line = null;
			while ((line = buf.readLine()) != null) {
				String[] sp = line.split(",");
				float[] f = new float[] { Float.parseFloat(sp[0]),
						Float.parseFloat(sp[1]) };
				KNNClass k = Integer.parseInt(sp[2]) == 1 ? walkClass
						: standClass;
				Log.d(DEBUG_TAG, String.format("%f,%f,%d", f[0], f[1],
						k == walkClass ? 1 : 0));
				FeatureVector fv = new FeatureVector(k, f);
				knn.add(fv);
			}

			buf.close();

		} catch (IOException e) {
			Log.e(DEBUG_TAG, e.getMessage() + "");
		}
	}

	public void saveToFile() {
		File file = new File(ctx.getExternalFilesDir("act"), ACTIVITY_FILE);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}

			FileWriter fw = new FileWriter(file, false);
			BufferedWriter buf = new BufferedWriter(fw);
			PrintWriter print = new PrintWriter(buf);

			ArrayList<FeatureVector> fvs = knn.getFeatures();
			for (FeatureVector fv : fvs) {
				float[] f = fv.getFeatures();
				print.printf("%f,%f,%d\n", f[0], f[1],
						fv.getKNNClass() == walkClass ? 1 : 0);
			}

			print.close();
		} catch (IOException e) {
			Log.e(DEBUG_TAG, e.getMessage());
		}
	}

}
