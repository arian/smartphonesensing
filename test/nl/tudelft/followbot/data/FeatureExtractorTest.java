package nl.tudelft.followbot.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeatureExtractorTest {

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
