package nl.tudelft.followbot.knn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class KNN {

	private final ArrayList<FeatureVector> features = new ArrayList<FeatureVector>();

	private float[] min;
	private float[] max;

	/**
	 * Add new learned data point
	 * 
	 * @param feature
	 */
	public void add(FeatureVector feature) {
		min = min == null ? feature.getFeatures().clone() : feature.min(min);
		max = max == null ? feature.getFeatures().clone() : feature.max(max);
		features.add(feature);
	}

	/**
	 * Clear all data points
	 */
	public void clear() {
		features.clear();
		min = max = null;
	}

	/**
	 * Classifiy a new feature vector with existing calibrated values
	 * 
	 * @param feature
	 * @param n
	 *            number of nearest points
	 * @return
	 */
	public KNNClass classify(final FeatureVector feature, int n) {

		KNNClass maxClass = null;
		int i = 0;
		HashMap<KNNClass, Integer> score = new HashMap<KNNClass, Integer>();
		int max = 0;

		final float[] mn = this.min;
		final float[] mx = this.max;

		// Sort by distance
		Collections.sort(features, new Comparator<FeatureVector>() {

			@Override
			public int compare(FeatureVector lhs, FeatureVector rhs) {
				float dist = lhs.distance(feature, mn, mx)
						- rhs.distance(feature, mn, mx);
				return dist < 0 ? -1 : dist > 0 ? 1 : 0;
			}

		});

		// count type of classes for the N nearest neighbors and store which
		// type occurs the most often
		for (FeatureVector nearFeature : features) {
			KNNClass nearClass = nearFeature.getKNNClass();
			Integer s = score.get(nearClass);
			if (s == null) {
				s = 0;
			}
			score.put(nearClass, ++s);
			if (s > max) {
				max = s;
				maxClass = nearClass;
			}
			if (i++ == n) {
				break;
			}
		}

		return maxClass;
	}

	public ArrayList<FeatureVector> getFeatures() {
		return features;
	}

	public float[][] getNormalizedFeatures() {
		float[][] d = new float[features.size()][2];
		for (int i = 0; i < features.size(); i++) {
			d[i] = features.get(i).getNormalizedValues(min, max);
		}
		return d;
	}

}
