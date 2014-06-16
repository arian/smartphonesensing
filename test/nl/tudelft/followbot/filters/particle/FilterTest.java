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

	@Test
	public void testOrientationMeasurement() {
		Filter filter = new Filter().withRandom(new RandomMock())
				.withDistribution(new NormalDistributionMock()).fill(4, 5);

		filter.orientationMeasurement(Math.PI * 0.5, 0.5);

		Particles ps = filter.getParticles();
		assertEquals(0, ps.get(0).getWeight(), 1e-6);
		assertEquals(0, ps.get(1).getWeight(), 1e-6);
		assertEquals(0, ps.get(2).getWeight(), 1e-6);
		assertEquals(1, ps.get(3).getWeight(), 1e-6);
	}

	@Test
	public void testUserMove() {
		Filter filter = new Filter().withRandom(new RandomMock())
				.withDistribution(new NormalDistributionMock());

		Particles ps = filter.getParticles();
		Particle p = new Particle(0, 0, 0);
		ps.add(p);

		// at all four [1,1] points around the origin
		p.setXYO(-1, 1, 0);
		filter.userMove(2, 0);
		assertEquals("(-1,-1,-90)", p.toRoundString());

		p.setXYO(1, 1, 0);
		filter.userMove(2, 0);
		assertEquals("(1,-1,90)", p.toRoundString());

		p.setXYO(1, -1, 0);
		filter.userMove(2, 0);
		assertEquals("(1,-3,27)", p.toRoundString());

		p.setXYO(-1, -1, 0);
		filter.userMove(2, 0);
		assertEquals("(-1,-3,-27)", p.toRoundString());

		// with initial orientation
		p.setXYO(1, 2, Math.PI / 2);
		filter.userMove(1, 0);
		assertEquals("(1,1,108)", p.toRoundString());

		p.setXYO(-1, 2, Math.PI / 2);
		filter.userMove(1, 0);
		assertEquals("(-1,1,72)", p.toRoundString());
	}

	@Test
	public void testUserRotate() {
		Filter filter = new Filter().withRandom(new RandomMock())
				.withDistribution(new NormalDistributionMock());

		Particles ps = filter.getParticles();
		Particle p = new Particle(0, 0, 0);
		ps.add(p);

		p.setXYO(1, 1, 0);
		filter.userRotate(Math.PI / 2, 0);
		assertEquals("(-1,1,0)", p.toRoundString());

		p.setXYO(1, 1, 0);
		filter.userRotate(Math.PI, 0);
		assertEquals("(-1,-1,0)", p.toRoundString());

		p.setXYO(-1, 1, 0);
		filter.userRotate(Math.PI, 0);
		assertEquals("(1,-1,0)", p.toRoundString());
	}

	@Test
	public void testRobotMove() {
		Filter filter = new Filter().withRandom(new RandomMock())
				.withDistribution(new NormalDistributionMock());

		Particles ps = filter.getParticles();
		Particle p = new Particle(0, 0, 0);
		ps.add(p);

		p.setXYO(0, 2, 0);
		filter.robotMove(1, 0);
		assertEquals("(0,1,0)", p.toRoundString());

		p.setXYO(1, 0, Math.PI / 2);
		filter.robotMove(1, 0);
		assertEquals("(1,-1,135)", p.toRoundString());

		p.setXYO(2, 2, -Math.PI / 4);
		filter.robotMove(2, 0);
		assertEquals("(0,2,-90)", p.toRoundString());

		p.setXYO(-2, -2, Math.PI / 4);
		filter.robotMove(2, 0);
		assertEquals("(-2,0,90)", p.toRoundString());
	}

}
