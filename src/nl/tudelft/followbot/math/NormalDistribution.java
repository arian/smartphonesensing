package nl.tudelft.followbot.math;

import static nl.tudelft.followbot.math.ErrorFunction.erf;
import static nl.tudelft.followbot.math.ErrorFunction.inverseErf;

public class NormalDistribution implements INormalDistribution {

	/**
	 * Probability density function
	 * 
	 * @param mean
	 * @param sigma
	 * @param x
	 * @return
	 */
	public double getDensity(double mean, double sigma, double x) {
		double exp = (x - mean) / sigma;
		exp = -0.5 * exp * exp;
		return 1 / (sigma * Math.sqrt(2)) * Math.pow(Math.E, exp);
	}

	/**
	 * Cumulative density function
	 * 
	 * @param mean
	 * @param sigma
	 * @param x
	 * @return
	 */
	public double getProbability(double mean, double sigma, double x) {
		return 0.5 * (1 + erf((x - mean) / (sigma * Math.sqrt(2))));
	}

	/**
	 * Inverse cumulative density function (quantile function)
	 * 
	 * @param mean
	 * @param sigma
	 * @param p
	 * @return
	 */
	public double getQuantile(double mean, double sigma, double p) {
		return mean + sigma * Math.sqrt(2) * inverseErf(2 * p - 1);
	}

}
