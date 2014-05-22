package nl.tudelft.followbot.filters.particle;

import javax.swing.JFrame;

import org.math.plot.Plot3DPanel;

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
		new Filter(5000, 10).plot();
	}

}
