#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/features2d/features2d.hpp>

#include <opencv2/calib3d/calib3d.hpp>

#include <vector>

using namespace std;
using namespace cv;

extern "C" {

JNIEXPORT void JNICALL Java_nl_tudelft_followbot_camera_CameraEstimator_CircleObjectTrack(JNIEnv* env, jobject thiz,
	jint greenHmin, jint greenSmin, jint greenVmin, jint greenHmax, jint greenSmax, jint greenVmax,
	jint blueHmin, jint blueSmin, jint blueVmin, jint blueHmax, jint blueSmax, jint blueVmax,
	jint width, jint height, jlong addrGray, jlong addrRgba, jboolean debug);


vector<Point3f> Generate3DPoints()
{
	vector<Point3f> points;

	points.push_back(Point3f( 0,  0, 0));
	points.push_back(Point3f(10, -5, 0));
	points.push_back(Point3f(10,  5, 0));
	points.push_back(Point3f( 1,  0, 0));

	return points;
}

void SetDoubleField(JNIEnv* env, jobject thiz, jclass thisClass, char const * field, float value) {
	jfieldID fid = env->GetFieldID(thisClass, field, "D");
	if (fid != NULL) {
		env->SetDoubleField(thiz, fid, value);
	}
}

void SetFloatField(JNIEnv* env, jobject thiz, jclass thisClass, char const * field, float value) {
	jfieldID fid = env->GetFieldID(thisClass, field, "F");
	if (fid != NULL) {
		env->SetFloatField(thiz, fid, value);
	}
}

JNIEXPORT void JNICALL Java_nl_tudelft_followbot_camera_CameraEstimator_CircleObjectTrack(JNIEnv* env, jobject thiz,
	jint greenHmin, jint greenSmin, jint greenVmin, jint greenHmax, jint greenSmax, jint greenVmax,
	jint blueHmin, jint blueSmin, jint blueVmin, jint blueHmax, jint blueSmax, jint blueVmax,
	jint width, jint height, jlong addrGray, jlong addrRgba, jboolean debug)
{
	Mat& mGray = *(Mat*)addrGray;
	Mat& mRgba = *(Mat*)addrRgba;

	CvSize size = cvSize(width, height);
	IplImage *hsv_frame = cvCreateImage(size, IPL_DEPTH_8U, 3);
	IplImage *green_thresh = cvCreateImage(size, IPL_DEPTH_8U, 1);
	IplImage *blue_thresh = cvCreateImage(size, IPL_DEPTH_8U, 1);
	IplImage *thresh = cvCreateImage(size, IPL_DEPTH_8U, 1);

	IplImage img_color = mRgba;
	IplImage img_gray = mGray;

	// Build scalars for minimum and maximum HSV values
	CvScalar green_min = cvScalar(greenHmin, greenSmin, greenVmin);
	CvScalar green_max = cvScalar(greenHmax, greenSmax, greenVmax);

	CvScalar blue_min = cvScalar(blueHmin, blueSmin, blueVmin);
	CvScalar blue_max = cvScalar(blueHmax, blueSmax, blueVmax);

	// Convert color space to HSV as it is much easier to filter colors in the HSV color-space
	cvCvtColor(&img_color, hsv_frame, CV_BGR2HSV);

	// Filter out colors which are not green
	cvInRangeS(hsv_frame, green_min, green_max, green_thresh);

	// Filter out colors which are not blue
	cvInRangeS(hsv_frame, blue_min, blue_max, blue_thresh);

	// Memory for hough circles
	CvMemStorage* green_storage = cvCreateMemStorage(0);
	CvMemStorage* blue_storage = cvCreateMemStorage(0);

	// Image smoothing
	cvSmooth(green_thresh, green_thresh, CV_GAUSSIAN, 9, 9);
	cvSmooth(blue_thresh, blue_thresh, CV_GAUSSIAN, 9, 9);

	// Perform OR operation on images: green_thresh + blue_thresh = thresh
	cvOr(green_thresh, blue_thresh, thresh);

	// Show thresholded image
	if (debug) {
		cvCvtColor(thresh, &img_color, CV_GRAY2BGR);
	}

	// Find green circle pattterns
	CvSeq* green_circles = cvHoughCircles(green_thresh, green_storage, CV_HOUGH_GRADIENT, 2,
											green_thresh->height/4, 100, 40, 10, 80);

	// Find blue circle pattterns
	CvSeq* blue_circles = cvHoughCircles(blue_thresh, blue_storage, CV_HOUGH_GRADIENT, 2,
											blue_thresh->height/4, 100, 40, 10, 80);

	// Flag for robot detection
	int robot_detected = 0;

	// Get a reference to this object's class
	jclass thisClass = env->GetObjectClass(thiz);

	// Draw and connect the three circles
	if ((green_circles->total >= 1) && (blue_circles->total >= 2)) {

		float* c1 = (float*)cvGetSeqElem(green_circles, 0);
		float* c2 = (float*)cvGetSeqElem(blue_circles, 0);
		float* c3 = (float*)cvGetSeqElem(blue_circles, 1);

		circle(mRgba, Point(c1[0], c1[1]), c1[2], Scalar(0, 255, 0, 255), 4);
		circle(mRgba, Point(c2[0], c2[1]), c2[2], Scalar(0, 0, 255, 255), 4);
		circle(mRgba, Point(c3[0], c3[1]), c3[2], Scalar(0, 0, 255, 255), 4);

		line(mRgba, Point(c1[0], c1[1]), Point(c2[0], c2[1]), Scalar(0, 0, 255, 255), 2);
		line(mRgba, Point(c1[0], c1[1]), Point(c3[0], c3[1]), Scalar(0, 0, 255, 255), 2);
		line(mRgba, Point(c3[0], c3[1]), Point(c2[0], c2[1]), Scalar(0, 255, 0, 255), 2);

		line(mRgba, Point((c2[0] + c3[0])/2, (c2[1] + c3[1])/2), Point(c1[0], c1[1]), Scalar(255, 0, 0, 255), 2);

		// Read points
		vector<Point2f> imagePoints;//
		imagePoints.push_back(Point2f(c1[0], c1[1]));
		imagePoints.push_back(Point2f(c2[0], c2[1]));
		imagePoints.push_back(Point2f(c3[0], c3[1]));
		imagePoints.push_back(Point2f((c2[0] + c3[0]) / 2, (c2[1] + c3[1]) / 2));

		vector<Point3f> objectPoints = Generate3DPoints();

		Mat cameraMatrix(3, 3, DataType<double>::type);
		setIdentity(cameraMatrix);

		Mat distCoeffs(4, 1, DataType<double>::type);
		distCoeffs.at<double>(0) = 0;
		distCoeffs.at<double>(1) = 0;
		distCoeffs.at<double>(2) = 0;
		distCoeffs.at<double>(3) = 0;

		Mat rvec(3, 1, DataType<double>::type);
		Mat tvec(3, 1, DataType<double>::type);

		solvePnP(objectPoints, imagePoints, cameraMatrix, distCoeffs, rvec, tvec);

		vector<Point2f> projectedPoints;
		projectPoints(objectPoints, rvec, tvec, cameraMatrix, distCoeffs, projectedPoints);

		for (int i = 0; i < projectedPoints.size(); i++) {
			circle(mRgba, projectedPoints[i], 20, Scalar(255, 0, 255, 255), 10);
		}

		// setting class values

		// Set robot detected flag
		robot_detected = 1;

		// rotation vector
		SetDoubleField(env, thiz, thisClass, "rvec0", rvec.at<double>(0));
		SetDoubleField(env, thiz, thisClass, "rvec1", rvec.at<double>(1));
		SetDoubleField(env, thiz, thisClass, "rvec2", rvec.at<double>(2));

		// translation vector
		SetDoubleField(env, thiz, thisClass, "tvec0", tvec.at<double>(0));
		SetDoubleField(env, thiz, thisClass, "tvec1", tvec.at<double>(1));
		SetDoubleField(env, thiz, thisClass, "tvec2", tvec.at<double>(2));

		// circle positions and radii
		SetFloatField(env, thiz, thisClass, "x1", c1[0]);
		SetFloatField(env, thiz, thisClass, "y1", c1[1]);
		SetFloatField(env, thiz, thisClass, "r1", c1[2]);

		SetFloatField(env, thiz, thisClass, "x2", c2[0]);
		SetFloatField(env, thiz, thisClass, "y2", c2[1]);
		SetFloatField(env, thiz, thisClass, "r2", c2[2]);

		SetFloatField(env, thiz, thisClass, "x3", c3[0]);
		SetFloatField(env, thiz, thisClass, "y3", c3[1]);
		SetFloatField(env, thiz, thisClass, "r3", c3[2]);

	}
	else {
		// Set robot detected flag
		robot_detected = 0;
	}

	jfieldID fid = env->GetFieldID(thisClass, "robotDetected", "I");
	if (fid != NULL) {
		// Change the variable's value
		env->SetIntField(thiz, fid, robot_detected);
	}


	// Cleanup resources
	cvReleaseMemStorage(&green_storage);
	cvReleaseMemStorage(&blue_storage);
	cvReleaseImage(&hsv_frame);
	cvReleaseImage(&green_thresh);
	cvReleaseImage(&blue_thresh);
	cvReleaseImage(&thresh);
}

}
