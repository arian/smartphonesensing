package nl.tudelft.followbot.knn;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;

public class KNN {

	private final ArrayList<FeatureVector> features = new ArrayList<FeatureVector>();

	public void add(FeatureVector feature) {
		features.add(feature);
	}

	public KNNClass classify(final FeatureVector feature, int n) {

		int i = 0;
		HashMap<KNNClass, Integer> score = new HashMap<KNNClass, Integer>();
		int max = 0;
		KNNClass maxClass = null;

		Collections.sort(features, new Comparator<FeatureVector>() {

			@Override
			public int compare(FeatureVector lhs, FeatureVector rhs) {
				float dist = lhs.distance(feature) - rhs.distance(feature);
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
}
