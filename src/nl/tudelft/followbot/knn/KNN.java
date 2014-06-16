package nl.tudelft.followbot.knn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class KNN {

	private final ArrayList<FeatureVector> features = new ArrayList<FeatureVector>();

	private float[] min;
	private float[] max;

	public void add(FeatureVector feature) {
		min = min == null ? feature.getFeatures().clone() : feature.min(min);
		max = max == null ? feature.getFeatures().clone() : feature.max(max);
		features.add(feature);
	}

	public KNNClass classify(final FeatureVector feature, int n) {

		KNNClass maxClass = null;
		int i = 0;
		HashMap<KNNClass, Integer> score = new HashMap<KNNClass, Integer>();
		int max = 0;

		final float[] mn = this.min;
		final float[] mx = this.max;

		Collections.sort(features, new Comparator<FeatureVector>() {

			@Override
			public int compare(FeatureVector lhs, FeatureVector rhs) {
				float dist = lhs.distance(feature, mn, mx)
						- rhs.distance(feature, mn, mx);
				return dist < 0 ? -1 : dist > 0 ? 1 : 0;
			}

		});

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
}
