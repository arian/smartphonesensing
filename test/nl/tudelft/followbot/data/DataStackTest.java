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
	public void testPeek() {
		DataStack<Integer> data = new DataStack<Integer>(5);
		for (int i = 0; i < 10; i++) {
			data.push(i);
			int x = data.peek();
			assertEquals(i, x, 1e-6);
		}
	}

	@Test
	public void testFilter() {
		DataStack<Integer> data = new DataStack<Integer>(15);
		for (int i = 0; i < 10; i++) {
			data.push(i);
		}
		DataStack<Integer> filtered = data
				.filter(new DataStack.Filter<Integer>() {
					@Override
					public boolean filter(Integer x) {
						return x > 5;
					}
				});
		assertEquals(6, filtered.get(0), 1e-6);
		assertEquals(7, filtered.get(1), 1e-6);
		assertEquals(8, filtered.get(2), 1e-6);
		assertEquals(9, filtered.get(3), 1e-6);
	}

	@Test
	public void testMap() {
		DataStack<Integer> data = new DataStack<Integer>(4);
		for (int i = 0; i < 4; i++) {
			data.push(i);
		}
		DataStack<Float> mapped = data.map(new DataStack.Map<Integer, Float>() {
			@Override
			public Float map(Integer x) {
				return ((float) x) / 2;
			}
		});
		assertEquals(0, mapped.get(0), 1e-6);
		assertEquals(0.5, mapped.get(1), 1e-6);
		assertEquals(1, mapped.get(2), 1e-6);
		assertEquals(1.5, mapped.get(3), 1e-6);
	}

	@Test
	public void testEach() {
		DataStack<Integer> data = new DataStack<Integer>(4);
		final int sum[] = { 0 };
		for (int i = 0; i < 3; i++) {
			data.push(i);
		}
		data.each(new DataStack.Each<Integer>() {
			@Override
			public void each(Integer t) {
				sum[0] += t;
			}
		});
		assertEquals(3, sum[0], 1e-6);
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
