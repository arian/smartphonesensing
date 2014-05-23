package nl.tudelft.followbot.filters.particle;

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
			Particle p = new Particle(r * Math.cos(a), r * Math.sin(a));
			p.setWeight(1.0 / N);
			particles.add(p);
		}
	}

	public Particles getParticles() {
		return particles;
	}

	public void resample() {
		Particles ps = new Particles();

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

	public void plot() {
		double[][] x = getPositions();

		Plot3DPanel plot = new Plot3DPanel();
		plot.addScatterPlot("particles", x);

		JFrame frame = new JFrame();
		frame.setSize(800, 800);
		frame.setContentPane(plot);
		frame.setVisible(true);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	}

	static public void main(String[] argv) {
		Filter filter = new Filter(2000, 10);
		for (int j = 0; j < 10; j++) {
			double x = 4 + Math.random();
			double y = 4 + Math.random();

			Particles close = filter.getParticles().getParticlesAt(x, y, 100);
			for (int i = 0; i < close.size(); i++) {
				close.get(i).setWeight(1 / close.get(i).distanceToSquare(x, y));
			}
		}
		filter.getParticles().normalizeWeights();
		filter.plot();
		filter.resample();
		filter.plot();
	}

}
