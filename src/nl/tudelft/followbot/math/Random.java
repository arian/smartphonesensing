package nl.tudelft.followbot.math;

public class Random implements IRandom {

	@Override
	public double get(int x, int X) {
		return Math.random();
	}

}
