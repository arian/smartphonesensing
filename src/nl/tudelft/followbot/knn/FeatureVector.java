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

	public float[] getFeatures() {
		return features;
	}

	public float distance(FeatureVector other, float[] min, float[] max) {
		float dist = 0;
		float[] ofeatures = other.features;
		int l = Math.min(features.length, ofeatures.length);
		for (int i = 0; i < l; i++) {
			float div = max[i] - min[i];
			if (div == 0) {
				div = 1;
			}
			float x = features[i] - min[i];
			float y = ofeatures[i] - min[i];
			float a = x / div - y / div;
			dist += a * a;
		}
		return dist;
	}

	public float[] max(float[] other) {
		int lo = other.length;
		int lf = features.length;
		int n = Math.max(lo, lf);

		float[] m = new float[n];
		float mv = Float.NEGATIVE_INFINITY;

		for (int i = 0; i < n; i++) {
			float l = i < lo ? other[i] : mv;
			float r = i < lf ? features[i] : mv;
			m[i] = Math.max(l, r);
		}

		return m;
	}

	public float[] min(float[] other) {
		int lo = other.length;
		int lf = features.length;
		int n = Math.max(lo, lf);

		float[] m = new float[n];
		float mv = Float.POSITIVE_INFINITY;

		for (int i = 0; i < n; i++) {
			m[i] = Math.min(i < lo ? other[i] : mv, i < lf ? features[i] : mv);
		}

		return m;
	}

}
