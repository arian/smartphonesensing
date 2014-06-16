package nl.tudelft.followbot.knn;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class FeatureVectorTest {

	@Test
	public void testDistance() {
		FeatureVector a = new FeatureVector(null, new float[] { 0, 0, 0 });
		FeatureVector b = new FeatureVector(null, new float[] { 4, 0, 0 });
		assertEquals(
				0.25f, // square of 0.5
				a.distance(b, new float[] { 0, 0, 0 }, new float[] { 8, 0, 0 }),
				1e-6);
	}

	@Test
	public void testMax() {
		FeatureVector a = new FeatureVector(null, new float[] { 1, 4, 9, -5 });
		float[] x = a.max(new float[] { 5, 3 });
		assertEquals(4, x.length);
		assertArrayEquals(new float[] { 5, 4, 9, -5 }, x, 1e-6f);
	}

	@Test
	public void testMin() {
		FeatureVector a = new FeatureVector(null, new float[] { 1, 4, 9, -4 });
		float[] x = a.min(new float[] { 5, 3 });
		assertEquals(4, x.length);
		assertArrayEquals(new float[] { 1, 3, 9, -4 }, x, 1e-6f);
	}
}
