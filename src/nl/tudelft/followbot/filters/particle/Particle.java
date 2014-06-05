package nl.tudelft.followbot.filters.particle;

public class Particle {

	/**
	 * Particle Weight
	 */
	private double w = 0;

	/**
	 * X position relative to the user
	 */
	private double x = 0;

	/**
	 * Y position relative to user
	 */
	private double y = 0;

	/**
	 * Orientation relative to user
	 */
	private double a = 0;

	public Particle(double x, double y) {
		setX(x);
		setY(y);
	}

	public double getWeight() {
		return w;
	}

	public void setWeight(double weight) {
		assert w >= 0 : "Weight should be positive";
		w = weight;
	}

	public void setX(double xp) {
		x = xp;
	}

	public double getX() {
		return x;
	}

	public void setY(double yp) {
		y = yp;
	}

	public double getY() {
		return y;
	}

	public double getOrientation() {
		return a;
	}

	public double distanceToSquare(double x, double y) {
		double dx = this.x - x;
		double dy = this.y - y;
		return dx * dx + dy * dy;
	}

	public double distanceToOrigin() {
		return Math.sqrt(distanceToSquare(0, 0));
	}

	@Override
	public Particle clone() {
		Particle clone = new Particle(x, y);
		clone.a = a;
		return clone;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + a + ")";
	}

}
