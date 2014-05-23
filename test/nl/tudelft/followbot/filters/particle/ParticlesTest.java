package nl.tudelft.followbot.filters.particle;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class ParticlesTest {

	@Test
	public void testGetParticleAt() {
		Particles ps = new Particles();
		Particle p2 = new Particle(2, 0);
		ps.add(new Particle(0, 0));
		ps.add(new Particle(1, 0));
		ps.add(p2);
		ps.add(new Particle(3, 0));
		ps.add(new Particle(4, 0));
		Particle p = ps.getParticleAt(2, 1);
		assertSame(p2, p);
	}

	@Test
	public void testGetParticleAtNull() {
		Particles ps = new Particles();
		assertNull(ps.getParticleAt(0, 0));
	}

	@Test
	public void testGetParticlesAt() {
		Particles ps = new Particles();
		Particle p1 = new Particle(1, 0);
		Particle p2 = new Particle(2, 0);
		Particle p3 = new Particle(3, 0);
		ps.add(new Particle(-1, 0));
		ps.add(new Particle(0, 0));
		ps.add(p1);
		ps.add(p2);
		ps.add(p3);
		ps.add(new Particle(4, 0));
		ps.add(new Particle(5, 0));
		Particles psAt21 = ps.getParticlesAt(2, 1, 3);
		assertEquals(3, psAt21.size());
		assertTrue(psAt21.contains(p1));
		assertTrue(psAt21.contains(p2));
		assertTrue(psAt21.contains(p3));
	}

	@Test
	public void testNormalizeWeights() {
		Particles ps = new Particles();
		Particle p1 = new Particle(1, 0);
		Particle p2 = new Particle(2, 0);
		Particle p3 = new Particle(3, 0);
		Particle p4 = new Particle(3, 0);
		ps.add(p1);
		ps.add(p2);
		ps.add(p3);
		ps.add(p4);
		ps.normalizeWeights();
		assertEquals(0.25, p1.getWeight(), 1e-6);
	}

	@Test
	public void testIncreaseAndNormalizeWeights() {
		Particles ps = new Particles();
		Particle p1 = new Particle(1, 0);
		Particle p2 = new Particle(2, 0);
		ps.add(p1);
		ps.add(p2);
		ps.add(new Particle(3, 0));
		ps.add(new Particle(4, 0));
		ps.normalizeWeights();
		assertEquals(0.25, p1.getWeight(), 1e-6);
		assertEquals(0.25, p2.getWeight(), 1e-6);
		p1.setWeight(1.25);
		ps.normalizeWeights();
		assertEquals(0.625, p1.getWeight(), 1e-6);
		assertEquals(0.125, p2.getWeight(), 1e-6);
	}

}
