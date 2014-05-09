package nl.tudelft.followbot.data;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.Arrays;

import org.junit.Test;

public class UnboxTest {

	@Test
	public void testUnboxDouble() {
		ArrayList<Double> list = new ArrayList<Double>();
		list.add(2.0);
		list.add(3.0);
		list.add(4.0);
		double[] array = Unbox.fromDouble(list);
		assertTrue(Arrays.equals(array, new double[] { 2.0, 3.0, 4.0 }));
	}

	@Test
	public void testUnboxLong() {
		ArrayList<Long> list = new ArrayList<Long>();
		list.add(2L);
		list.add(3L);
		list.add(4L);
		long[] array = Unbox.fromLong(list);
		assertTrue(Arrays.equals(array, new long[] { 2, 3, 4 }));
	}

	@Test
	public void testUnboxInteger() {
		ArrayList<Integer> list = new ArrayList<Integer>();
		list.add(2);
		list.add(3);
		list.add(4);
		int[] array = Unbox.fromInteger(list);
		assertTrue(Arrays.equals(array, new int[] { 2, 3, 4 }));
	}

	@Test
	public void testIntsToDoubles() {
		int[] ints = new int[] { 1, 2, 3 };
		double[] doubles = Unbox.intsToDoubles(ints);
		assertTrue(Arrays.equals(doubles, new double[] { 1, 2, 3 }));
	}

}
