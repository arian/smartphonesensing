package nl.tudelft.followbot.filters.particle;

import static nl.tudelft.followbot.math.NormalDistribution.getDensity;
import static nl.tudelft.followbot.math.NormalDistribution.getQuantile;

public class Filter {

	private Particles particles = new Particles();

	public Filter(int N, double radius) {
		assert N > 0 : "Number of particles should be positive";
		fillInitialParticles(N, radius);
	}

	private void fillInitialParticles(int N, double radius) {
		for (int i = 0; i < N; i++) {
			double r = radius * Math.random();
			double a = 2 * Math.PI * Math.random();
			Particle p = new Particle(r * Math.cos(a), r * Math.sin(a));
			p.setWeight(1.0 / N);
			particles.add(p);
		}
	}

	public Particles getParticles() {
		return particles;
	}

	public void multiplyPrior(Particles prior) {
		if (prior.size() != particles.size()) {
			throw new IllegalArgumentException(
					"Prior size does not correspond with number of particles");
		}

		for (int i = 0; i < particles.size(); i++) {
			Particle ppost = particles.get(i);
			Particle pprior = prior.getParticleAt(ppost.getX(), ppost.getY());

			System.out.println(pprior.getWeight());
			ppost.setWeight(ppost.getWeight() * pprior.getWeight());
		}
	}

	public void resample() {
		Particles ps = new Particles();

		particles.normalizeWeights();

		int N = particles.size();
		double newWeight = 1 / N;

		for (int i = 0; i < N; i++) {

			double x = Math.random();
			double sum = 0;

			for (int j = 0; j < N; j++) {
				Particle p = particles.get(j);
				sum += p.getWeight();
				if (sum > x) {
					Particle newParticle = p.clone();
					newParticle.setWeight(newWeight);
					ps.add(newParticle);
					break;
				}
			}
		}

		particles = ps;
	}

	/**
	 * Distance Measurement, for example from the Bluetooth RSS
	 * 
	 * @param d
	 * @param sigma
	 */
	public void distanceMeasurement(double d, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			double x = p.distanceToOrigin();
			double w = getDensity(d, sigma, x);
			p.setWeight(w);
		}
	}

	/**
	 * Move d in direction alpha, with std deviation sigma
	 * 
	 * @param d
	 * @param alpha
	 * @param sigma
	 */
	public void move(double d, double alpha, double sigma) {
		// alpha = 0 means moving forward -> increasing all y with d
		alpha += Math.PI / 2;
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			double dist = getQuantile(d, sigma, Math.random());
			p.setX(p.getX() + Math.cos(alpha) * dist);
			p.setY(p.getY() + Math.sin(alpha) * dist);
		}
	}

	public double[][] getPositions() {
		double[][] x = new double[3][particles.size()];
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			x[0][i] = p.getX();
			x[1][i] = p.getY();
			x[2][i] = p.getWeight();
		}
		return x;
	}

	public void plot(String title) {
		// double[][] x = getPositions();

		// Plot3DPanel plot = new Plot3DPanel();
		// plot.addScatterPlot("particles", x);

		// JFrame frame = new JFrame();
		// frame.setTitle(title);
		// frame.setSize(800, 800);
		// frame.setContentPane(plot);
		// frame.setVisible(true);
		// frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	// static public void main(String[] argv) {
	// Filter filter = new Filter(2000, 25);
	//
	// filter.distanceMeasurement(5.0, 2.0);
	// filter.plot("Initial measurement");
	//
	// Particles prior = filter.getParticles();
	//
	// filter.resample();
	// filter.plot("after resampling");
	//
	// filter.move(-5, 0, 2);
	// filter.plot("after moving");
	//
	// filter.distanceMeasurement(0.5, 1);
	// filter.plot("new distance measurement");
	//
	// filter.multiplyPrior(prior);
	// filter.plot("after multiplying with prior");
	//
	// Particles prior2 = filter.getParticles();
	// filter.resample();
	// filter.plot("second resampling");
	//
	// filter.move(-1, 0, 2);
	// filter.plot("after second moving");
	//
	// filter.distanceMeasurement(2, 1);
	// filter.plot("new distance measurement");
	//
	// }

}
