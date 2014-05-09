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
    jint hmin, jint smin, jint vmin, jint hmax, jint smax, jint vmax, jint width, jint height, jlong addrGray, jlong addrRgba, jboolean debug);
	
JNIEXPORT void JNICALL Java_nl_tudelft_followbot_MainActivity_CircleObjectTrack(JNIEnv* env, jobject thiz,
    jint hmin, jint smin, jint vmin, jint hmax, jint smax, jint vmax, jint width, jint height, jlong addrGray, jlong addrRgba, jboolean debug)
{
    Mat& mGray = *(Mat*)addrGray;
    Mat& mRgba = *(Mat*)addrRgba;

    CvSize size = cvSize(width, height);
    IplImage *hsv_frame = cvCreateImage(size, IPL_DEPTH_8U, 3);
    IplImage *thresholded = cvCreateImage(size, IPL_DEPTH_8U, 1);
    
    IplImage img_color = mRgba;
    IplImage img_gray = mGray;
	
	// Detect a red ball
	CvScalar hsv_min = cvScalar(hmin, smin, vmin);
    CvScalar hsv_max = cvScalar(hmax, smax, vmax);
	
	// Convert color space to HSV as it is much easier to filter colors in the HSV color-space
    cvCvtColor(&img_color, hsv_frame, CV_BGR2HSV);

    // Filter out colors which are out of range
    cvInRangeS(hsv_frame, hsv_min, hsv_max, thresholded);

    // Memory for hough circles
    CvMemStorage* storage = cvCreateMemStorage(0);
	
    // Image smoothing
    cvSmooth(thresholded, thresholded, CV_GAUSSIAN, 9, 9);

    // show thresholded
    if (debug)
		cvCvtColor(thresholded, &img_color, CV_GRAY2BGR);

    // find circle pattterns
    CvSeq* circles = cvHoughCircles(thresholded, storage, CV_HOUGH_GRADIENT, 2,
                                        thresholded->height/4, 100, 40, 10, 80);
										
    // draw found circles
    for (int i = 0; i < circles->total; i++) // max 3 circles
    {
        float* p = (float*)cvGetSeqElem(circles, i);
        circle(mRgba, Point(p[0], p[1]), 3, Scalar(0, 255, 0, 255), 2);
        circle(mRgba, Point(p[0], p[1]), p[2], Scalar(0, 0, 255, 255), 4);
    }

    // cleanup resources
    cvReleaseMemStorage(&storage);
    cvReleaseImage(&hsv_frame);
    cvReleaseImage(&thresholded);
}
}
