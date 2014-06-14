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

	public Particle(double x, double y, double orientation) {
		setX(x);
		setY(y);
		setOrientation(orientation);
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

	public void setOrientation(double orientation) {
		a = orientation;
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

	public double angleToOrigin() {
		return (Math.atan2(this.y, this.x) - Math.atan2(0, 1));
	}

	@Override
	public Particle clone() {
		Particle clone = new Particle(x, y, a);
		return clone;
	}

	@Override
	public String toString() {
		return "(" + x + "," + y + "," + a + ")";
	}

}
