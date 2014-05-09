package nl.tudelft.followbot.knn;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class KNNTest {

	@Test
	public void testClassify() {
		KNN knn = new KNN();

		KNNClass x = new KNNClass("x");
		KNNClass y = new KNNClass("y");

		knn.add(new FeatureVector(x, new float[] { 1, 1, 1 }));
		knn.add(new FeatureVector(x, new float[] { 1, 2, 1 }));

		knn.add(new FeatureVector(y, new float[] { 6, 1, 1 }));
		knn.add(new FeatureVector(y, new float[] { 5, 2, 1 }));
		knn.add(new FeatureVector(y, new float[] { 6, 1, 1 }));

		float[] feat = new float[] { 5.2f, 1, 1 };
		KNNClass z = knn.classify(new FeatureVector(null, feat), 1);

		assertEquals(y, z);
	}

	@Test
	public void testClassifyNis3() {
		KNN knn = new KNN();

		KNNClass x = new KNNClass("x");
		KNNClass y = new KNNClass("y");

		knn.add(new FeatureVector(x, new float[] { 1, 1, 1 }));
		knn.add(new FeatureVector(x, new float[] { 5.5f, 1, 1 }));

		knn.add(new FeatureVector(y, new float[] { 6, 1, 1 }));
		knn.add(new FeatureVector(y, new float[] { 5, 1, 1 }));
		knn.add(new FeatureVector(y, new float[] { 6, 1, 1 }));

		float[] feat = new float[] { 5.5f, 1, 1 };
		KNNClass z = knn.classify(new FeatureVector(null, feat), 3);

		assertEquals(y, z);
	}

}
