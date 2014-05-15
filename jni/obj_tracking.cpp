#include <jni.h>
#include <opencv2/core/core.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/imgproc/imgproc_c.h>
#include <opencv2/features2d/features2d.hpp>
#include <vector>

using namespace std;
using namespace cv;

extern "C" {
JNIEXPORT void JNICALL Java_nl_tudelft_followbot_MainActivity_CircleObjectTrack(JNIEnv* env, jobject thiz,
    jint greenHmin, jint greenSmin, jint greenVmin, jint greenHmax, jint greenSmax, jint greenVmax,
	jint blueHmin, jint blueSmin, jint blueVmin, jint blueHmax, jint blueSmax, jint blueVmax,
	jint width, jint height, jlong addrGray, jlong addrRgba, jboolean debug);
	
JNIEXPORT void JNICALL Java_nl_tudelft_followbot_MainActivity_CircleObjectTrack(JNIEnv* env, jobject thiz,
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
    if (debug)
		cvCvtColor(thresh, &img_color, CV_GRAY2BGR);

    // Find green circle pattterns
    CvSeq* green_circles = cvHoughCircles(green_thresh, green_storage, CV_HOUGH_GRADIENT, 2,
											green_thresh->height/4, 100, 40, 10, 80);
											
	// Find blue circle pattterns
	CvSeq* blue_circles = cvHoughCircles(blue_thresh, blue_storage, CV_HOUGH_GRADIENT, 2,
											blue_thresh->height/4, 100, 40, 10, 80);
										
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
