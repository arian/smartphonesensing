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

	/**
	 * Distance to another point, scaled between min and max values
	 * 
	 * @param other
	 * @param min
	 * @param max
	 * @return
	 */
	public float distance(FeatureVector other, float[] min, float[] max) {
		float dist = 0;
		float[] sf = getNormalizedValues(min, max);
		float[] so = other.getNormalizedValues(min, max);
		int l = Math.min(sf.length, so.length);
		for (int i = 0; i < l; i++) {
			float a = sf[i] - so[i];
			dist += a * a;
		}
		return dist;
	}

	/**
	 * Get a new vectors with the maximum elements of this and other
	 * 
	 * @param other
	 * @return
	 */
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

	/**
	 * Get a new vectors with the minimum elements of this and other
	 * 
	 * @param other
	 * @return
	 */
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

	/**
	 * Create new array with values scaled between min and max
	 * 
	 * @param min
	 * @param max
	 * @return
	 */
	public float[] getNormalizedValues(float[] min, float[] max) {
		float[] f = new float[features.length];
		for (int i = 0; i < features.length; i++) {
			if (i < min.length && i < max.length) {
				float div = max[i] - min[i];
				if (div == 0) {
					div = 1;
				}
				f[i] = (features[i] - min[i]) / div;
			} else {
				f[i] = features[i];
			}
		}
		return f;
	}

}
