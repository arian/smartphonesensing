package nl.tudelft.followbot.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeatureExtractorTest {

	@Test
	public void testFromFloat3() {
		DataStack<float[]> d = new DataStack<float[]>(10);
		d.push(new float[] { 0, 3, -2, 1 });
		d.push(new float[] { 1, 1, -2, 3 });
		d.push(new float[] { 2, 1, 2, 3 });
		d.push(new float[] { 3, 1, -2, 3 });
		d.push(new float[] { 4, 1, 2 });
		d.push(new float[] { 5, 1, 2 });
		FeatureExtractor f = FeatureExtractor.fromFloat4(d, 1);
		DataStack<Float> x = f.getData();
		assertEquals(-14, x.get(0), 1e-6);
		assertEquals(14, x.get(1), 1e-6);
		assertEquals(-14, x.get(2), 1e-6);

		boolean thrown = false;
		try {
			x.get(3);
		} catch (IndexOutOfBoundsException e) {
			thrown = true;
		}
		assertTrue(thrown);
	}

	@Test
	public void testZeroCrossings() {
		DataStack<Float> data = new DataStack<Float>(10);
		data.push(10f);
		data.push(5f);
		data.push(-5f);
		data.push(-3f);
		data.push(-2f);
		data.push(10f);
		data.push(5f);
		data.push(-5f);
		data.push(-3f);
		data.push(-2f);

		FeatureExtractor extractor = new FeatureExtractor(data);
		int crossings = extractor.zeroCrossings();
		assertEquals(3, crossings);
	}

	@Test
	public void testPower() {
		DataStack<Float> data = new DataStack<Float>(4);
		data.push(1f);
		data.push(2f);
		data.push(3f);
		data.push(4f);

		FeatureExtractor extractor = new FeatureExtractor(data);
		float pwr = extractor.avgPower();
		assertTrue(pwr == 7.5f);
	}

}
