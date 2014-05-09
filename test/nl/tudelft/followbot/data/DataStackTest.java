package nl.tudelft.followbot.data;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.Test;

public class DataStackTest {

	@Test
	public void testPush() {
		DataStack<Double> data = new DataStack<Double>(5);
		data.push(4.0);
		data.push(5.0);
		assertEquals(2, data.getSize());
	}

	@Test
	public void testPushMax() {
		DataStack<Integer> data = new DataStack<Integer>(3);
		data.push(4);
		data.push(5);
		data.push(4);
		data.push(5);
		assertEquals(3, data.getSize());
	}

	@Test
	public void testGet() {
		DataStack<Double> data = new DataStack<Double>(5);
		data.push(4.0);
		data.push(5.0);
		assertEquals(4.0, data.get(0), 1e-6);
		assertEquals(5.0, data.get(1), 1e-6);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetOutOfBounds() {
		DataStack<Double> data = new DataStack<Double>(5);
		data.push(4.0);
		data.get(5);
	}

	@Test
	public void testGetRinged() {
		DataStack<Integer> data = new DataStack<Integer>(5);
		for (int i = 0; i < 10; i++) {
			data.push(i);
		}
		assertEquals(5, data.get(0), 1e-6);
		assertEquals(9, data.get(4), 1e-6);
	}

	@Test
	public void testToArray() {
		DataStack<Integer> data = new DataStack<Integer>(5);
		data.push(4);
		data.push(5);
		data.push(4);
		data.push(5);
		ArrayList<Integer> array = data.toArray();
		assertEquals(4, array.size());
		assertEquals(4, array.get(0), 1e-6);
		assertEquals(5, array.get(3), 1e-6);
	}

}
