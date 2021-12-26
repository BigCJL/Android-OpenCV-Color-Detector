//
// Created by DELL on 2021-12-20.
//

#ifndef TESTCV1_COLORDETECTOR_H
#define TESTCV1_COLORDETECTOR_H
#include <jni.h>
#include "opencv2/opencv.hpp"

class ColorDetector {
public:
    static void Detect_color(long Mat_ptr, int color_flag, bool isGray);
};


#endif //TESTCV1_COLORDETECTOR_H

