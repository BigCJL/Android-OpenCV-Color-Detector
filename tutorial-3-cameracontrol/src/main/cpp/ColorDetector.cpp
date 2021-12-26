//
// Created by DELL on 2021-12-20.
//

#include "ColorDetector.h"

using namespace cv;
using namespace std;


static Mat dst, HSV_Img;
static Mat kernel = getStructuringElement(MORPH_RECT, Size(5, 5));
//开操作处理
std::vector<vector<Point>> contours;
std::vector<Vec4i> hireachy;
Rect rect;


extern "C"
JNIEXPORT void JNICALL
Java_org_opencv_samples_tutorial3_Tutorial3Activity_Color_1Detector_1FromJNI(JNIEnv *env,
                                                                             jobject thiz,
                                                                             jlong mat_ptr, jint color_flag,
                                                                             jboolean isGray) {
    // TODO: implement Color_Detector_FromJNI()
    ColorDetector::Detect_color(mat_ptr, color_flag, isGray);
}


void ColorDetector::Detect_color(long mat_ptr, int color_flag, bool isGray) {
    //从Java获取Mat
    Scalar lowerB(100, 43, 46);
    Scalar upperB(124, 255, 255);
    if(color_flag == 0){       // 0代表蓝色
        lowerB = Scalar (100, 43, 46);
        upperB = Scalar (124, 255, 255);
    }
    else if(color_flag == 1){  // 1代表黄色
        lowerB = Scalar (0, 80, 80);
        upperB = Scalar (50, 255, 255);
    }
    else if(color_flag == 2){  // 2代表红色   ?? 范围不完整 待优化
        lowerB = Scalar (156, 43, 46);
        upperB = Scalar (180, 255, 255);
    }
    else if(color_flag == 3){   // 3代表绿色
        lowerB = Scalar (35, 43, 46);
        upperB = Scalar (77, 255, 255);
    }
    contours.clear();
    hireachy.clear();
    Mat& frame = *(Mat*)mat_ptr;
    Mat BGR_tmp;
    cvtColor(frame, BGR_tmp, COLOR_RGBA2BGR);
    cvtColor(BGR_tmp, HSV_Img, COLOR_BGR2HSV);   //OpenCV 中图像是以 BGR存储的
    //inRange(frame, Scalar(0, 80, 80), Scalar(50, 255, 255), dst); // 黄色

    inRange(HSV_Img, lowerB, upperB, dst);
    //开操作
    //inRange(HSV_Img, Scalar(100, 43, 46), Scalar(124, 255, 255), dst);  //蓝色

    morphologyEx(dst, dst, MORPH_OPEN, kernel);
    //获取边界
    findContours(dst, contours, hireachy, RETR_EXTERNAL, CHAIN_APPROX_SIMPLE, Point(0, 0));
    //框选面积最大的边界
    if (!contours.empty())
    {
        double maxArea = 0;
        for (int i = 0; i < contours.size(); i++)
        {
            double area = contourArea(contours[static_cast<int>(i)]);
            if (area > maxArea)
            {
                maxArea = area;
                rect = boundingRect(contours[static_cast<int>(i)]);
            }
        }
    }
    rectangle(frame,rect, Scalar(0,255,0),2);
    transpose(frame, frame);
    //0: 沿X轴翻转； >0: 沿Y轴翻转； <0: 沿X轴和Y轴翻转
    flip(frame, frame, 1);// 翻转模式，flipCode == 0垂直翻转（沿X轴翻转），flipCode>0水平翻转（沿Y轴翻转），flipCode<0水平垂直翻转（先沿X轴翻转，再沿Y轴翻转，等价于旋转180°）

    transpose(dst, dst);
    flip(dst, dst, 1);

    if(isGray)
        frame = dst;
    int f = 0;

}

extern "C"
JNIEXPORT void JNICALL
Java_org_opencv_samples_tutorial3_Tutorial3Activity_Save_1IMG(JNIEnv *env, jobject thiz,
                                                              jlong mat_ptr, jstring filename) {
    // TODO: implement Save_IMG()
    Mat& frame = *(Mat*)mat_ptr;
    Mat BGR = Mat();
    cvtColor(frame, BGR, COLOR_RGBA2BGR);
    const char *cstr = env->GetStringUTFChars(filename, nullptr);
    std::string file_dir = std::string(cstr);
    string file_name = "/sdcard/IMG_ColorDetector/" + file_dir;
    imwrite(file_name, BGR);
}