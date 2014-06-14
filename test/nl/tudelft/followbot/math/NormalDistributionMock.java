package nl.tudelft.followbot.math;

public class NormalDistributionMock implements IDistribution {

	@Override
	public double getDensity(double mean, double sigma, double x) {
		if (Math.abs(x - mean) < sigma) {
			// integral is still 1
			return 1 / (sigma * 2);
		}
		return 0;
	}

	@Override
	public double getQuantile(double mean, double sigma, double p) {
		return 0;
	}

}
