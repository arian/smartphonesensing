package nl.tudelft.followbot.data;

public class FeatureExtractor {

	private final DataStack<Float> data;

	public static FeatureExtractor fromFloat4(DataStack<float[]> data,
			final long since) {
		DataStack<Float> d = data.filter(new DataStack.Filter<float[]>() {
			// Use all values since a certain time. For example we only have
			// data points of the last 10 seconds. That's important when
			// counting zero crossings
			@Override
			public boolean filter(float[] x) {
				return x.length >= 4 && x[0] >= since;
			}
		}).map(new DataStack.Map<float[], Float>() {
			@Override
			public Float map(float[] x) {
				// Multiply with the sign of the vertical axis, so it
				// can count the zero crossings
				return (x[1] * x[1] + x[2] * x[2] + x[3] * x[3])
						* Math.signum(x[2]);
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
