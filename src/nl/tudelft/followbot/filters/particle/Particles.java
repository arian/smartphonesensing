package nl.tudelft.followbot.filters.particle;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class Particles extends ArrayList<Particle> {

	private static final long serialVersionUID = -2674900584014828724L;

	public double sumWeights() {
		double sum = 0;
		for (int i = 0; i < size(); i++) {
			sum += get(i).getWeight();
		}
		return sum;
	}

	public void normalizeWeights() {
		double sum = sumWeights();
		if (sum == 0) {
			double w = 1.0 / size();
			for (int i = 0; i < size(); i++) {
				get(i).setWeight(w);
			}
		} else {
			for (int i = 0; i < size(); i++) {
				Particle p = get(i);
				p.setWeight(p.getWeight() / sum);
			}
		}
	}

	public void sortAround(final double x, final double y) {
		Collections.sort(this, new Comparator<Particle>() {

			@Override
			public int compare(Particle p1, Particle p2) {
				double dist1 = p1.distanceToSquare(x, y);
				double dist2 = p2.distanceToSquare(x, y);
				return dist1 < dist2 ? -1 : dist1 > dist2 ? 1 : 0;
			}

		});
	}

	public Particles getParticlesAt(double x, double y, int N) {

		sortAround(x, y);

		Particles ps = new Particles();

		for (int i = 0; i < Math.min(N, size()); i++) {
			ps.add(this.get(i));
		}

		return ps;
	}

	public Particle getParticleAt(double x, double y) {
		if (size() == 0) {
			return null;
		}
		Particle p = get(0);
		double dist = Double.MAX_VALUE;
		for (int i = 0; i < size(); i++) {
			Particle p1 = get(i);
			double dist1 = p1.distanceToSquare(x, y);
			if (dist1 < dist) {
				dist = dist1;
				p = p1;
			}
		}
		return p;
	}

}
