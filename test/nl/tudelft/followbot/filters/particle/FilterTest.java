package nl.tudelft.followbot.filters.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import nl.tudelft.followbot.math.NormalDistributionMock;
import nl.tudelft.followbot.math.RandomMock;

import org.junit.Test;

public class FilterTest {

	@Test
	public void testNewFilter() {
		Filter filter = new Filter().fill(2, 10);
		Particle p = filter.getParticles().getParticleAt(5, 5);
		assertNotNull(p);
	}

	@Test
	public void testInitialWeight() {
		Filter filter = new Filter().fill(4, 10);
		Particle p = filter.getParticles().getParticleAt(5, 5);
		assertEquals(0.25, p.getWeight(), 1e-6);
	}

	@Test
	public void testDistanceMeasurement() {
		Filter filter = new Filter().withRandom(new RandomMock())
				.withDistribution(new NormalDistributionMock()).fill(5, 5);

		filter.distanceMeasurement(3, 0.5);

		Particles ps = filter.getParticles();
		assertEquals(0, ps.get(0).getWeight(), 1e-6);
		assertEquals(0, ps.get(1).getWeight(), 1e-6);
		assertEquals(0, ps.get(2).getWeight(), 1e-6);
		assertEquals(1, ps.get(3).getWeight(), 1e-6);
		assertEquals(0, ps.get(4).getWeight(), 1e-6);
	}

}
