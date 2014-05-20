package nl.tudelft.followbot.camera;

public class CameraEstimator {
	private float distancePhoneRobot = 0;
	private float distanceUserRobot = 0;
	private int angleOrientation = 0;
	private int angleSkew = 0;
	private int translationHorizontal = 0;
	private int translationVertical = 0;

	public float getDistancePhoneRobot() {
		return distancePhoneRobot;
	}

	public float getDistanceUserRobot() {
		return distanceUserRobot;
	}

	public int getAngleOrientation() {
		return angleOrientation;
	}

	public int getAngleSkew() {
		return angleSkew;
	}

	public int getTranslationHorizontal() {
		return translationHorizontal;
	}

	public int getTranslationVertical() {
		return translationVertical;
	}

	public native void CircleObjectTrack(int greenHmin, int greenSmin,
			int greenVmin, int greenHmax, int greenSmax, int greenVmax,
			int blueHmin, int blueSmin, int blueVmin, int blueHmax,
			int blueSmax, int blueVmax, int width, int height, long matAddrGr,
			long matAddrRgba, boolean debug);

}
