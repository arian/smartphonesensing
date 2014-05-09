package nl.tudelft.followbot.data;

import java.util.List;

public class Unbox {

	public static double[] fromDouble(List<Double> list) {
		int n = list.size();
		double[] res = new double[n];
		for (int i = 0; i < n; i++) {
			res[i] = list.get(i);
		}
		return res;
	}

	public static long[] fromLong(List<Long> list) {
		int n = list.size();
		long[] res = new long[n];
		for (int i = 0; i < n; i++) {
			res[i] = list.get(i);
		}
		return res;
	}

	public static int[] fromInteger(List<Integer> list) {
		int n = list.size();
		int[] res = new int[n];
		for (int i = 0; i < n; i++) {
			res[i] = list.get(i);
		}
		return res;
	}

	public static double[] intsToDoubles(int[] ints) {
		int n = ints.length;
		double[] doubles = new double[n];
		for (int i = 0; i < n; i++) {
			doubles[i] = ints[i];
		}
		return doubles;
	}

}
