package nl.tudelft.followbot.data;

import static org.junit.Assert.assertEquals;

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

}
