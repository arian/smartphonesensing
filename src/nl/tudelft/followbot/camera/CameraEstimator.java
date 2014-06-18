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

	private int robotDetected;

	private float distance;
	private float orientation;

	private float x1;
	private float y1;
	private float r1;

	private float x2;
	private float y2;
	private float r2;

	private float x3;
	private float y3;
	private float r3;

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

		switch (mViewMode) {
		case VIEW_MODE_RGBA:
			// input frame has RBGA format
			mRgba = inputFrame.rgba();
			break;
		case VIEW_MODE_THRESH:
		case VIEW_MODE_OD_RGBA:

			// input frame has RGBA format
			mRgba = inputFrame.rgba();
			mGray = inputFrame.gray();

			int width = mRgba.width();
			int height = mRgba.height();

			boolean debug = mViewMode == VIEW_MODE_THRESH;

			CircleObjectTrack(THRESH_GREEN_HMIN, THRESH_GREEN_SMIN,
					THRESH_GREEN_VMIN, THRESH_GREEN_HMAX, THRESH_GREEN_SMAX,
					THRESH_GREEN_VMAX, THRESH_BLUE_HMIN, THRESH_BLUE_SMIN,
					THRESH_BLUE_VMIN, THRESH_BLUE_HMAX, THRESH_BLUE_SMAX,
					THRESH_BLUE_VMAX, width, height, mGray.getNativeObjAddr(),
					mRgba.getNativeObjAddr(), debug);

			Log.d("FOO", "» " + distance + "      " + orientation);

			break;
		}

		return mRgba;
	}

	public native void CircleObjectTrack(int greenHmin, int greenSmin,
			int greenVmin, int greenHmax, int greenSmax, int greenVmax,
			int blueHmin, int blueSmin, int blueVmin, int blueHmax,
			int blueSmax, int blueVmax, int width, int height, long matAddrGr,
			long matAddrRgba, boolean debug);

	public int getViewMode() {
		return mViewMode;
	}

	public void setViewMode(int mode) {
		mViewMode = mode;
	}

	public boolean robotSeen() {
		return (robotDetected == 1);
	}

	public float getDistance() {
		return distance;
	}

	public float getOrientation() {
		return orientation;
	}

}
