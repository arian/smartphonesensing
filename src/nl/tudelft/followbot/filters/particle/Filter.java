package nl.tudelft.followbot.filters.particle;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;

import javax.swing.JFrame;

import nl.tudelft.followbot.math.IDistribution;
import nl.tudelft.followbot.math.IRandom;
import nl.tudelft.followbot.math.NormalDistribution;
import nl.tudelft.followbot.math.Random;

import org.math.plot.Plot3DPanel;

public class Filter {

	private Particles particles = new Particles();
	private IDistribution distribution;
	private IRandom random;

	public Filter() {
		distribution = new NormalDistribution();
		random = new Random();
	}

	public Filter withDistribution(IDistribution d) {
		distribution = d;
		return this;
	}

	public Filter withRandom(IRandom r) {
		random = r;
		return this;
	}

	public Filter fill(int N, double radius) {
		assert N > 0 : "Number of particles should be positive";
		assert radius > 0 : "Radius should be greater than 0";

		for (int i = 0; i < N; i++) {
			double r = radius * random.get(i, N);
			double a = 2 * Math.PI * random.get(i, N);

			// orientation is between [-pi; pi] degrees
			// double orientation = Math.PI * (2 * random.get(i, N) - 1);
			double orientation = 0;

			double x = 0;
			double y = -1.0;

			// Particle p = new Particle(r * Math.cos(a), r * Math.sin(a),
			// orientation);
			Particle p = new Particle(x, y, orientation);

			p.setWeight(1.0 / N);
			particles.add(p);
		}
		return this;
	}

	public Particles getParticles() {
		return particles;
	}

	public void multiplyPrior(Particles prior) {
		if (prior.size() != particles.size()) {
			throw new IllegalArgumentException(
					"Prior size does not correspond with number of particles");
		}

		for (Particle ppost : particles) {
			Particle pprior = prior.getParticleAt(ppost.getX(), ppost.getY());
			ppost.setWeight(ppost.getWeight() * pprior.getWeight());
		}
	}

	/**
	 * Multinomial resampling
	 * 
	 * @link http://robotics.stackexchange.com/a/484
	 * @link http://users.isy.liu.se/rt/schon/Publications/HolSG2006.pdf
	 */
	public void resample() {
		Particles ps = new Particles();

		particles.normalizeWeights();

		int N = particles.size();
		double newWeight = 1 / N;

		for (int i = 0; i < N; i++) {
			double x = random.get(i, N);
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
	 * New fancy resampling
	 * 
	 * @TODO find out which method this is.
	 */
	public void newResample() {
		Particles ps = new Particles();

		particles.normalizeWeights();

		int index = 0;
		int N = particles.size();

		double newWeight = 1 / N;
		double mw = particles.maxWeight();
		double beta = 0;

		for (int i = 0; i < N; i++) {
			beta += random.get() * 2.0 * mw;
			while (beta > particles.get(index).getWeight()) {
				beta -= particles.get(index).getWeight();
				index = (index + 1) % N;
			}
			Particle newParticle = particles.get(index).clone();
			newParticle.setWeight(newWeight);
			ps.add(newParticle);
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
		for (Particle p : particles) {
			double x = p.distanceToOrigin();
			double w = distribution.getDensity(distance, sigma, x);
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
		for (Particle p : particles) {
			double a = p.getOrientation();
			double w = distribution.getDensity(orientation, sigma, a);
			p.setWeight(w);
		}
	}

	public void headingMeasurement(double heading, double sigma) {
		for (Particle p : particles) {
			double a = p.angleToOrigin();
			double w = distribution.getDensity(heading, sigma, a);
			p.setWeight(w);
		}
	}

	/**
	 * User moves d meters in direction alpha, with std deviation sigma
	 * 
	 * @param d
	 * @param sigma
	 */
	public void userMove(double d, double sigma) {
		for (Particle p : particles) {
			double dy = distribution.getQuantile(d, sigma, random.get());

			// current point
			double x = p.getX();
			double y = p.getY();

			// new y position
			double ny = y - dy;

			// angles of the points
			double angle1 = Math.atan2(y, x);
			double angle2 = Math.atan2(ny, x);

			// angle between points from the origin
			double angle = angle1 - angle2;

			// set new values
			p.setY(ny);
			p.setOrientation(p.getOrientation() + angle);
		}
	}

	/**
	 * User rotates, so all particles rotate around the origin
	 * 
	 * @param rot
	 *            difference in radians
	 * @param sigma
	 *            standard deviation of the rotation
	 * @link http://en.wikipedia.org/wiki/Rotation_(mathematics)#Two_dimensions
	 */
	public void userRotate(double rot, double sigma) {
		for (Particle p : particles) {
			double x = p.getX();
			double y = p.getY();

			double r = distribution.getQuantile(rot, sigma, random.get());

			p.setX(x * Math.cos(r) - y * Math.sin(r));
			p.setY(x * Math.sin(r) + y * Math.cos(r));
		}
	}

	/**
	 * Robot moves d meters along its orientation, with std deviation sigma
	 * 
	 * @param d
	 * @param sigma
	 */
	public void robotMove(double d, double sigma) {
		for (Particle p : particles) {
			double dist = distribution.getQuantile(d, sigma, random.get());

			double x = p.getX();
			double y = p.getY();

			// Angle pointing to the origin
			double a = Math.atan2(y, x) + Math.PI;
			double o = p.getOrientation();

			// angle to move to relative in the coordinate system
			double b = a + o;

			// new position
			double nx = x + Math.cos(b) * dist;
			double ny = y + Math.sin(b) * dist;

			// new angle
			double na = Math.atan2(ny, nx) - Math.PI;

			p.setX(nx);
			p.setY(ny);
			p.setOrientation(b - na);
		}
	}

	/**
	 * Robot rotates with angle alpha [rad], with std deviation sigma
	 * 
	 * @param d
	 * @param sigma
	 */
	public void robotRotate(double alpha, double sigma) {
		for (Particle p : particles) {

			double dalpha = distribution
					.getQuantile(alpha, sigma, random.get());

			p.setOrientation(p.getOrientation() + dalpha);
		}
	}

	/**
	 * @return the most likely particle that should be the robot according to
	 *         the filter
	 */
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

	/**
	 * Convert all particle positions / weight to an array
	 * 
	 * @return
	 */
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

	/**
	 * Create a 3D plot
	 * 
	 * @param title
	 */
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

	public void saveToFile(File file) {
		PrintWriter writer;
		try {
			writer = new PrintWriter(file, "UTF-8");
			for (Particle p : particles) {
				writer.println(String.format("%f,%f,%f,%f", p.getX(), p.getY(),
						p.getOrientation(), p.getWeight()));
			}
			writer.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	static public void main(String[] argv) {
		Filter filter = new Filter().fill(100, 10);

		// filter.plot("Initial");

		filter.userMove(0.0, 0.05);
		filter.robotMove(0.5, 0.05);
		System.out.println(filter.getDistanceEstimate());

		filter.userMove(0.0, 0.05);
		filter.robotMove(0.1, 0.05);
		System.out.println(filter.getDistanceEstimate());

		filter.userMove(0.0, 0.05);
		filter.robotMove(0.5, 0.05);
		System.out.println(filter.getDistanceEstimate());

		filter.userMove(0.0, 0.05);
		filter.robotMove(0.1, 0.05);
		System.out.println(filter.getDistanceEstimate());

		filter.userMove(0.10, 0.05);
		System.out.println(filter.getDistanceEstimate());

		filter.plot("move");

	}
}
