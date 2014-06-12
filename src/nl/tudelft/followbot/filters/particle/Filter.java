package nl.tudelft.followbot.filters.particle;

import static nl.tudelft.followbot.math.NormalDistribution.getDensity;
import static nl.tudelft.followbot.math.NormalDistribution.getQuantile;

import javax.swing.JFrame;

import org.math.plot.Plot3DPanel;

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

			// orientation is between [-90; 90] degrees
			double orientation = 90 * (2 * Math.random() - 1);

			Particle p = new Particle(r * Math.cos(a), r * Math.sin(a),
					orientation);

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
	 * Distance Measurement from the phone camera or estimated from the activity
	 * monitoring
	 *
	 * @param distance
	 * @param sigma
	 */
	public void distanceMeasurement(double distance, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			double x = p.distanceToOrigin();
			double w = getDensity(distance, sigma, x);
			p.setWeight(w);
		}
	}

	/**
	 * Orientation Measurement from the phone camera or from the the orientation
	 * sensor
	 *
	 * @param orientation
	 * @param sigma
	 */
	public void orientationMeasurement(double orientation, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			double a = p.getOrientation();
			double w = getDensity(orientation, sigma, a);
			p.setWeight(w);
		}
	}

	/**
	 * User moves d meters in direction alpha, with std deviation sigma
	 *
	 * @param d
	 * @param alpha
	 * @param sigma
	 */
	public void userMove(double d, double alpha, double sigma) {
		// alpha = 0 means moving forward -> increasing all y with d
		alpha += Math.PI / 2;

		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			double dist = getQuantile(d, sigma, Math.random());

			double oldX = p.getX();
			double oldY = p.getY();

			double newX = oldX + Math.cos(alpha) * dist;
			double newY = oldY + Math.sin(alpha) * dist;

			double newOrientation = p.getOrientation()
					+ Math.acos((oldX * newX + oldY * newY)
							/ (Math.sqrt(oldX * oldX + oldY * oldY) * Math
									.sqrt(newX * newX + newY * newY)));

			p.setX(newX);
			p.setY(newY);
			p.setOrientation(newOrientation);
		}
	}

	/**
	 * User rotates, so all particles rotate around the origin
	 *
	 * @param rot
	 *            difference in radians
	 * @link http://en.wikipedia.org/wiki/Rotation_(mathematics)#Two_dimensions
	 */
	public void userRotate(double rot) {
		for (Particle p : particles) {
			double x = p.getX();
			double y = p.getY();
			p.setX(x * Math.cos(rot) - y * Math.sin(rot));
			p.setY(x * Math.sin(rot) + y * Math.cos(rot));
		}
	}

	/**
	 * Robot moves d meters along its direction, with std deviation sigma
	 *
	 * @param d
	 * @param sigma
	 *
	 *            TODO: Add actual robot movement through IOIO
	 */
	public void robotMove(double d, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);

			double dist = getQuantile(d, sigma, Math.random());

			p.setX(p.getX() + Math.cos(p.getOrientation()) * dist);
			p.setY(p.getY() + Math.sin(p.getOrientation()) * dist);
		}

		// IOIO code
	}

	/**
	 * Robot rotates with angle alpha [rad], with std deviation sigma
	 *
	 * @param d
	 * @param sigma
	 *
	 *            TODO: Add actual robot movement through IOIO
	 */
	public void robotRotate(double alpha, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);

			double dalpha = getQuantile(alpha, sigma, Math.random());

			p.setOrientation(p.getOrientation() + dalpha);
		}

		// IOIO code
	}

	/**
	 * Robot moves d meters along its direction, with std deviation sigma
	 *
	 * @param d
	 * @param sigma
	 *
	 *            TODO: Add actual robot movement through IOIO
	 */
	public void robotMove(double d, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);

			double dist = getQuantile(d, sigma, Math.random());

			p.setX(p.getX() + Math.cos(p.getOrientation()) * dist);
			p.setY(p.getY() + Math.sin(p.getOrientation()) * dist);
		}

		// IOIO code
	}

	/**
	 * Robot rotates with angle alpha [rad], with std deviation sigma
	 *
	 * @param d
	 * @param sigma
	 *
	 *            TODO: Add actual robot movement through IOIO
	 */
	public void robotRotate(double alpha, double sigma) {
		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);

			double dalpha = getQuantile(alpha, sigma, Math.random());

			p.setOrientation(p.getOrientation() + dalpha);
		}

		// IOIO code
	}

	public double getDistanceEstimate() {
		double distance = 0;

		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			distance += p.distanceToOrigin();
		}

		return (distance / particles.size());
	}

	public double getOrientationEstimate() {
		double orientation = 0;

		for (int i = 0; i < particles.size(); i++) {
			Particle p = particles.get(i);
			orientation += p.getOrientation();
		}

		return (orientation / particles.size());
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
		double[][] x = getPositions();

		Plot3DPanel plot = new Plot3DPanel();
		plot.addScatterPlot("particles", x);

		JFrame frame = new JFrame();
		frame.setTitle(title);
		frame.setSize(800, 800);
		frame.setContentPane(plot);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	static public void main(String[] argv) {
		Filter filter = new Filter(50000, 1000);

		filter.distanceMeasurement(200.0, 30);
		filter.plot("Initial measurement");

		Particles prior1 = filter.getParticles();

		filter.resample();
		filter.plot("after resampling");

		filter.orientationMeasurement(0, 10);
		filter.plot("Orientation Measurement");

		Particles prior2 = filter.getParticles();

		filter.resample();
		filter.plot("after resampling 2");

		filter.robotMove(30, 5);

		filter.distanceMeasurement(175, 30);
		filter.plot("new distance measurement");

		filter.multiplyPrior(prior1);
		filter.plot("after multiplying with prior");

		prior1 = filter.getParticles();

		filter.resample();
		filter.plot("after resampling 3");

		filter.orientationMeasurement(0, 10);
		filter.plot("Orientation Measurement");

		filter.multiplyPrior(prior2);
		filter.plot("after multiplying with prior 2");

		prior2 = filter.getParticles();

		filter.resample();
		filter.plot("after resampling 4");

		// another test

		filter.robotRotate(10, 5);

		filter.distanceMeasurement(175, 30);
		filter.plot("new distance measurement");

		filter.multiplyPrior(prior1);
		filter.plot("after multiplying with prior");

		filter.resample();
		filter.plot("after resampling 3");

		filter.orientationMeasurement(8, 10);
		filter.plot("Orientation Measurement");

		filter.multiplyPrior(prior2);
		filter.plot("after multiplying with prior 2");

		filter.resample();
		filter.plot("after resampling 4");

		System.out.println("Distance: " + filter.getDistanceEstimate()
				+ " Orientation: " + filter.getOrientationEstimate());

		/*
		 * filter.orientationMeasurement(Math.PI / 3.0, 2.0);
		 * filter.plot("Orientation Measurement");
		 *
		 *
		 *
		 * filter.robotMove(0.5, 0.2); filter.distanceMeasurement(19.5, 4.0);
		 * filter.plot("new distance measurement");
		 *
		 * filter.multiplyPrior(prior);
		 * filter.plot("after multiplying with prior");
		 *
		 * filter.resample(); filter.plot("after resampling 3");
		 *
		 * filter.orientationMeasurement(Math.PI / 3.0, 2.0);
		 * filter.plot("new orientation measurement");
		 *
		 * filter.resample(); filter.plot("after resampling 4");
		 *
		 * /* bla
		 */
		/*
		 * filter.robotRotate(Math.PI / 3.0, 0.2);
		 * filter.distanceMeasurement(19.5, 4.0);
		 * filter.plot("new distance measurement 2");
		 *
		 * filter.multiplyPrior(prior);
		 * filter.plot("after multiplying with prior2");
		 *
		 * filter.resample(); filter.plot("after resampling 5");
		 *
		 * filter.orientationMeasurement(0, 0.2);
		 * filter.plot("new orientation measurement2");
		 *
		 * filter.resample(); filter.plot("after resampling 6");
		 *
		 * /* filter.move(-5, 0, 2); filter.plot("after moving");
		 *
		 * filter.distanceMeasurement(0.5, 1);
		 * filter.plot("new distance measurement");
		 *
		 * filter.multiplyPrior(prior);
		 * filter.plot("after multiplying with prior");
		 *
		 * Particles prior2 = filter.getParticles(); filter.resample();
		 * filter.plot("second resampling");
		 *
		 * filter.move(-1, 0, 2); filter.plot("after second moving");
		 *
		 * filter.distanceMeasurement(2, 1);
		 * filter.plot("new distance measurement");
		 */
	}
}
