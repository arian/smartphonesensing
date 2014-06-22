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

void SetDoubleField(JNIEnv* env, jobject thiz, jclass thisClass, char const * field, double value) {
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

		// calculate distance and orientation

		char text[255];

		// Compute slope angle of the line between the two blue markers (robot orientation)
		float alpha = atan2((c2[1] - c3[1]), (c2[0] - c3[0]));

		// Compute triangle centroid position
		int centroid_X = c1[0] + ((c2[0] + c3[0]) / 2 - c1[0]) * (2/3);
		int centroid_Y = c1[1] + ((c2[1] + c3[1]) / 2 - c1[1]) * (2/3);

		// Compute translation values
		int translation_X = centroid_X - width / 2;
		int translation_Y = height / 2 - centroid_Y;

		// Phone-robot distance
		float distance_phone = c1[2];

		// Find the largest radius
		if (c2[2] > distance_phone) {
			distance_phone = c2[2];
		}
		if (c3[2] > distance_phone) {
			distance_phone = c3[2];
		}

		distance_phone = -0.0024 * pow(distance_phone, 3.0)
						+ 0.36 * pow(distance_phone, 2.0)
						- 18 * distance_phone
						+ 340;

		sprintf(text, "%f   %f", c1[2], distance_phone);
		putText(mRgba, text, Point(20, 40), FONT_HERSHEY_SIMPLEX, 0.5, Scalar(255, 0, 0, 255));

		// Compute lengths of triangle sides
		float a = sqrt((c2[0]-c3[0])*(c2[0]-c3[0]) + (c2[1]-c3[1])*(c2[1]-c3[1]));
		float b = sqrt((c1[0]-c3[0])*(c1[0]-c3[0]) + (c1[1]-c3[1])*(c1[1]-c3[1]));
		float c = sqrt((c1[0]-c2[0])*(c1[0]-c2[0]) + (c1[1]-c2[1])*(c1[1]-c2[1]));

		// Compute triangle angles
		float au = acosf(((b*b + c*c - a*a) / (2*b*c))) * (180 / CV_PI);
		float bu = acosf(((a*a + c*c - b*b) / (2*a*c))) * (180 / CV_PI);
		float cu = acosf(((b*b + a*a - c*c) / (2*b*a))) * (180 / CV_PI);

		float beta;
		// Find the largest angle and use it in the linear formula for computing the skew:
		// beta = -0.75*max_angle + 0.75*180
		// This gives a decent estimate without the need to calibrate the camera or take pictures of the robot
		if ((au >= bu) && (au >= cu)) {
			beta = (-au + 180.0) * 0.75;
		}
		else if ((bu >= au) && (bu >= cu)) {
			beta = (-bu + 180.0) * 0.75;
		}
		else if ((cu >= au) && (cu >= bu)) {
			beta = (-cu + 180.0) * 0.75;
		}

		// Compute user-robot distance
		float distance_user = distance_phone * cos(beta * CV_PI / 180);

		// setting class values

		// Set robot detected flag
		robot_detected = 1;

		// circle positions and radii
		SetDoubleField(env, thiz, thisClass, "distance", (double) distance_user);
		
		if (abs(alpha) <= CV_PI / 2) {
			SetDoubleField(env, thiz, thisClass, "orientation", (double) alpha);
		}

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
