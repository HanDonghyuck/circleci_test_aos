
#include <opencv2/core/mat.hpp>
#include <opencv2/imgproc.hpp>
#include "detect_image_quality.hpp"

int MCT_count_binary(cv::Mat src, float& left, float& right, float& up, float& down) {

	std::cout << "MCT_count_binary";

	left = 0;
	right = 0;
	up = 0;
	down = 0;

	return 0;
}


float image_entropy_logarithm_ratio(cv::Mat src) {
    std::cout << "image_entropy_logarithm_ratio";
	return 0;
}
