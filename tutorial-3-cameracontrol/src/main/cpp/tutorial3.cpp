//#include <jni.h>
//#include "opencv2/opencv.hpp"
//
//using namespace cv;
//using namespace std;
//
//
//static Mat dst, HSV_Img;
//static Mat kernel = getStructuringElement(MORPH_RECT, Size(5, 5));
////开操作处理
//std::vector<std::vector<Point>> contours;
//std::vector<Vec4i> hireachy;
//Rect rect;
//
//extern "C"
//JNIEXPORT void JNICALL
//Java_org_opencv_samples_tutorial3_Tutorial3Activity_Color_1Detector_1FromJNI(JNIEnv *env,
//                                                                             jobject thiz,
//                                                                             jlong mat_ptr) {
//    // TODO: implement Color_Detector_FromJNI()
//    //从Java获取Mat
//    Mat& frame = *(Mat*)mat_ptr;
//    cvtColor(frame, HSV_Img, COLOR_BGR2HSV);   //OpenCV 中图像是以 BGR存储的
//    //inRange(frame, Scalar(0, 80, 80), Scalar(50, 255, 255), dst); // 黄色
//    inRange(HSV_Img, Scalar(100, 43, 46), Scalar(124, 255, 255), dst);  //蓝色
//    //开操作
//    morphologyEx(dst, dst, MORPH_OPEN, kernel);
//    //获取边界
//    findContours(dst, contours, hireachy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE, Point(0, 0));
//    //框选面积最大的边界
//    if (!contours.empty())
//    {
//        double maxArea = 0;
//        for (int i = 0; i < contours.size(); i++)
//        {
//            double area = contourArea(contours[static_cast<int>(i)]);
//            if (area > maxArea)
//            {
//                maxArea = area;
//                rect = boundingRect(contours[static_cast<int>(i)]);
//            }
//        }
//    }
//    rectangle(frame,rect, Scalar(0,255,0),2);
//    int f = 1;
//}