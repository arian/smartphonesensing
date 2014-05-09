package nl.tudelft.followbot.knn;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class FeatureVectorTest {

	@Test
	public void testDistance() {
		FeatureVector a = new FeatureVector(null, new float[] { 1, 0, 0 });
		FeatureVector b = new FeatureVector(null, new float[] { 4, 0, 0 });
		assertTrue(a.distance(b) == 9);
	}
}
