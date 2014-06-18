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

	public Particle setX(double xp) {
		x = xp;
		return this;
	}

	public double getX() {
		return x;
	}

	public Particle setY(double yp) {
		y = yp;
		return this;
	}

	public double getY() {
		return y;
	}

	public Particle setOrientation(double orientation) {
		a = orientation;
		// Make sure a is in (-pi, pi]
		if (a > Math.PI)
			a -= Math.PI * 2;
		else if (a <= -Math.PI)
			a += Math.PI * 2;
		return this;
	}

	public double getOrientation() {
		return a;
	}

	public Particle setXYO(double x, double y, double o) {
		return setX(x).setY(y).setOrientation(o);
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
		double a = ((Math.atan2(y, x) + Math.atan2(1, 0)));
		// Make sure a is in (-pi, pi]
		if (a > Math.PI)
			a -= Math.PI * 2;
		else if (a <= -Math.PI)
			a += Math.PI * 2;
		return a;
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

	public String toRoundString() {
		return "(" + Math.round(x) + "," + Math.round(y) + ","
				+ Math.round(a * 180 / Math.PI) + ")";
	}

}
