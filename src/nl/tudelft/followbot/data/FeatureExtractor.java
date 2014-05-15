package nl.tudelft.followbot.data;

public class FeatureExtractor {

	private final DataStack<Float> data;

	public static FeatureExtractor fromFloat3(DataStack<float[]> data) {
		DataStack<Float> d = data.filter(new DataStack.Filter<float[]>() {
			@Override
			public boolean filter(float[] x) {
				return x.length >= 3;
			}
		}).map(new DataStack.Map<float[], Float>() {
			@Override
			public Float map(float[] x) {
				// Multiply with the sign of the vertical axis, so it
				// can count the zero crossings
				return (x[0] * x[0] + x[1] * x[1] + x[2] * x[2])
						* Math.signum(x[1]);
			}
		});

		return new FeatureExtractor(d);
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

	public DataStack<Float> getData() {
		return data;
	}

}
