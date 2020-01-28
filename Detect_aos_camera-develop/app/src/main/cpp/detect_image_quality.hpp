
// Created by fitpet_detect on 19.05.02.002.
//

#include <opencv2/opencv_modules.hpp>
#include <opencv2/core/mat.hpp>
#include <opencv2/core.hpp>
#include <opencv2/core/utility.hpp>
#include <opencv2/core/ocl.hpp>
#include <opencv2/features2d.hpp>
#include <opencv2/highgui.hpp>
#include <opencv2/calib3d.hpp>
#include <opencv2/xfeatures2d.hpp>
#include <opencv2/imgproc.hpp>

#define MAX_BIN 128
#define MIN_BIN 8


#define MCT_orSum_threshold 0.02f
#define MCT_orSumDown_threshold 0.03f



#ifndef DETECT_DATACOLLECTOR_DETECT_IMAGE_QUALITY_H
#define DETECT_DATACOLLECTOR_DETECT_IMAGE_QUALITY_H

#endif //DETECT_DATACOLLECTOR_DETECT_IMAGE_QUALITY_H



int MCT_count_binary(cv::Mat src, float& left, float& right, float& up, float& down);
float image_entropy_logarithm_ratio(cv::Mat src);