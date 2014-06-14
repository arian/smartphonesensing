package nl.tudelft.followbot.math;

public interface INormalDistribution {

	/**
	 * Probability density function
	 * 
	 * @param mean
	 * @param sigma
	 * @param x
	 * @return
	 */
	public double getDensity(double mean, double sigma, double x);

	/**
	 * Inverse cumulative density function (quantile function)
	 * 
	 * @param mean
	 * @param sigma
	 * @param p
	 * @return
	 */
	public double getQuantile(double mean, double sigma, double p);

}
