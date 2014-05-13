package nl.tudelft.followbot.data;

public class FeatureExtractor {

	private final DataStack<Float> data;

	public static FeatureExtractor fromFloat3(DataStack<float[]> data) {
		DataStack<Float> d = new DataStack<Float>(data.getSize());
		FeatureExtractor f = new FeatureExtractor(d);

		for (int i = 0; i < data.getSize(); i++) {
			float[] x = data.get(i);
			if (x.length >= 3) {
				d.push(x[0] * x[0] + x[1] * x[1] + x[2] * x[2]);
			}
		}

		return f;
	}

	public FeatureExtractor(DataStack<Float> d) {
		data = d;
	}

	public int zeroCrossings() {
		int counts = 0;
		float cur, prev = 0;
		for (int i = 0; i < data.getSize(); i++) {
			cur = data.get(i);
			if (cur * prev < 0) {
				counts++;
			}
			prev = cur;
		}
		return counts;
	}

	public float power() {
		float pwr = 0;
		float cur;
		for (int i = 0; i < data.getSize(); i++) {
			cur = data.get(i);
			pwr += cur * cur;
		}
		return pwr;
	}

	public float avgPower() {
		return power() / data.getSize();
	}

}
