package nl.tudelft.followbot.filters.particle;


public class Filter {

	private final Particles particles = new Particles();

	public Filter(int N, double radius) {
		fillInitialParticles(N, radius);
	}

	private void fillInitialParticles(int N, double radius) {
		for (int i = 0; i < N; i++) {
			double r = radius * Math.random();
			double a = 2 * Math.PI * Math.random();

			Particle p = new Particle(r * Math.cos(a), r * Math.sin(a));

			particles.add(p);
		}

		double[][] x = getPositions();

	}

	public double[][] getPositions() {
		double[][] x = new double[2][particles.size()];
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			x[0][i] = p.getX();
			x[1][i] = p.getY();
		}
		return x;
	}

	static public void main(String[] argv) {
		new Filter(5000, 10);
	}

}
