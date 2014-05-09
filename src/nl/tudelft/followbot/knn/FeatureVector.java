package nl.tudelft.followbot.knn;

public class FeatureVector {

	private final KNNClass clazz;
	private final float[] features;

	public FeatureVector(KNNClass cls, float[] ftrs) {
		clazz = cls;
		features = ftrs;
	}

	public KNNClass getKNNClass() {
		return clazz;
	}

	public float distance(FeatureVector other) {
		float dist = 0;
		float[] ofeatures = other.features;
		int l = Math.min(features.length, ofeatures.length);
		for (int i = 0; i < l; i++) {
			float a = features[i] - ofeatures[i];
			dist += a * a;
		}
		return dist;
	}

}
