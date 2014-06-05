package nl.tudelft.followbot.filters.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;

public class FilterTest {

	@Test
	public void testNewFilter() {
		Filter filter = new Filter(2, 10);
		Particle p = filter.getParticles().getParticleAt(5, 5);
		assertNotNull(p);
	}

	@Test
	public void testInitialWeight() {
		Filter filter = new Filter(4, 10);
		Particle p = filter.getParticles().getParticleAt(5, 5);
		assertEquals(0.25, p.getWeight(), 1e-6);
	}

}
