package nl.tudelft.followbot.camera;

import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

import android.util.Log;

public class CameraEstimator implements CvCameraViewListener2 {

	public static final int VIEW_MODE_RGBA = 0;
	public static final int VIEW_MODE_THRESH = 2;
	public static final int VIEW_MODE_OD_RGBA = 5;

	private static final int THRESH_GREEN_HMIN = 30;
	private static final int THRESH_GREEN_SMIN = 50;
	private static final int THRESH_GREEN_VMIN = 50;

	private static final int THRESH_GREEN_HMAX = 60;
	private static final int THRESH_GREEN_SMAX = 255;
	private static final int THRESH_GREEN_VMAX = 255;

	private static final int THRESH_BLUE_HMIN = 0;
	private static final int THRESH_BLUE_SMIN = 50;
	private static final int THRESH_BLUE_VMIN = 50;

	private static final int THRESH_BLUE_HMAX = 25;
	private static final int THRESH_BLUE_SMAX = 255;
	private static final int THRESH_BLUE_VMAX = 255;

	private int mViewMode;
	private Mat mRgba;
	private Mat mGray;

	private CameraBridgeViewBase mOpenCvCameraView;

	private final float distancePhoneRobot = 0;
	private final float distanceUserRobot = 0;
	private final int angleOrientation = 0;
	private final int angleSkew = 0;
	private final int translationHorizontal = 0;
	private final int translationVertical = 0;

	public void enableCamera() {
		mOpenCvCameraView.enableView();
	}

	public void openCameraView(CameraBridgeViewBase viewBase, int frameWidth,
			int frameHeight) {
		// open new camera view
		mOpenCvCameraView = viewBase;
		mOpenCvCameraView.setMaxFrameSize(frameWidth, frameHeight);
		mOpenCvCameraView.setCvCameraViewListener(this);
	}

	public void disableCamera() {
		if (mOpenCvCameraView != null)
			mOpenCvCameraView.disableView();
	}

	@Override
	public void onCameraViewStarted(int width, int height) {
		mRgba = new Mat(height, width, CvType.CV_8UC4);
		mGray = new Mat(height, width, CvType.CV_8UC1);
	}

	@Override
	public void onCameraViewStopped() {
		mRgba.release();
		mGray.release();
	}

	@Override
	public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
		final int viewMode = this.mViewMode;

		switch (viewMode) {
		case VIEW_MODE_RGBA:
			// input frame has RBGA format
			mRgba = inputFrame.rgba();
			break;
		case VIEW_MODE_THRESH:
			// input frame has RGBA format
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();

			this.CircleObjectTrack(THRESH_GREEN_HMIN, THRESH_GREEN_SMIN,
					THRESH_GREEN_VMIN, THRESH_GREEN_HMAX, THRESH_GREEN_SMAX,
					THRESH_GREEN_VMAX, THRESH_BLUE_HMIN, THRESH_BLUE_SMIN,
					THRESH_BLUE_VMIN, THRESH_BLUE_HMAX, THRESH_BLUE_SMAX,
					THRESH_BLUE_VMAX, mRgba.width(), mRgba.height(),
					mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), true);

			Log.d("Position",
					this.getAngleSkew() + " " + this.getAngleOrientation()
							+ " " + this.getTranslationHorizontal() + " "
							+ this.getTranslationVertical() + " "
							+ this.getDistancePhoneRobot() + " "
							+ this.getDistanceUserRobot() + "");

			break;
		case VIEW_MODE_OD_RGBA:
			// input frame has RGBA format
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();

			this.CircleObjectTrack(THRESH_GREEN_HMIN, THRESH_GREEN_SMIN,
					THRESH_GREEN_VMIN, THRESH_GREEN_HMAX, THRESH_GREEN_SMAX,
					THRESH_GREEN_VMAX, THRESH_BLUE_HMIN, THRESH_BLUE_SMIN,
					THRESH_BLUE_VMIN, THRESH_BLUE_HMAX, THRESH_BLUE_SMAX,
					THRESH_BLUE_VMAX, mRgba.width(), mRgba.height(),
					mGray.getNativeObjAddr(), mRgba.getNativeObjAddr(), false);

			Log.d("Position",
					this.getAngleSkew() + " " + this.getAngleOrientation()
							+ " " + this.getTranslationHorizontal() + " "
							+ this.getTranslationVertical() + " "
							+ this.getDistancePhoneRobot() + " "
							+ this.getDistanceUserRobot() + "");

			break;
		}

		return mRgba;
	}

	public int getViewMode() {
		return mViewMode;
	}

	public void setViewMode(int mode) {
		mViewMode = mode;
	}

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
