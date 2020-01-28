//
// Created by fitpet on 2019-06-20.
//

#ifndef DETECT_AOS_CAMERA_DETECT_ISP_ERROR_H
#define DETECT_AOS_CAMERA_DETECT_ISP_ERROR_H

#endif //DETECT_AOS_CAMERA_DETECT_ISP_ERROR_H
// - 제거 -

#define ERROR_PRE_READ_IMAGE    0xFFFFFF0F   // - 제거 -
#define ERROR_PHIL_AREA			0xFFFFFF1F   // - 제거 -
// 추후 확장
#define ERROR_NOSEHOLE_AREA		0xFFFFFF2F   // - 제거 -
#define ERROR_NOSECURVE_AREA	0xFFFFFF3F   // - 제거 -
#define ERROR_YOLO				0xFFFFFF4F   // - 제거 -
#define ERROR_FEATURE		    0xFFFFFF5F   // - 제거 -
#define ERROR_MATCH				0xFFFFFF6F   // - 제거 -
#define ERROR_PRE_READ_AREA		0xFFFFFF9F   // - 제거 -


#define SUCCESS_ALL				0x00000000   // - 제거 -
#define SUCCESS_PREPROCESS		0x00000001   // - 제거 -
#define SUCCESS_YOLO			0x00000002   // - 제거 -
#define SUCCESS_DETECT		    0x00000003   // - 제거 -



// Definitions for Matching Errors
#define ERROR_MATCH_NOT_ENOUGH_KP                   0xFFFFF00F // - 제거 -
#define ERROR_MATCH_WRONG_POSITION                  0xFFFFF01F // - 제거 -
#define ERROR_MATCH_CROSS_MATCHED                   0xFFFFF02F // - 제거 -
#define ERROR_MATCH_CONVEX_RATIO_WRONG              0xFFFFF03F // - 제거 -
#define ERROR_MATCH_IRREGULAR_TANGENT               0xFFFFF04F // - 제거 -
#define ERROR_MATCH_IRREGULAR_TANGENT_RANGE         0xFFFFF040 // - 제거 -
#define ERROR_MATCH_IRREGULAR_TANGENT_COUNT         0xFFFFF041 // - 제거 -
#define ERROR_MATCH_IRREGULAR_DISTNACE              0xFFFFF05F // - 제거 -

// - 제거 -
#define SUCCESS_MATCH                       0x00000F00

// - 제거 -
#define SUCCESS_STEP2						0x00000F10
#define SUCCESS_STEP3						0x00000F20
#define SUCCESS_STEP4						0x00000F30








// VERIFICATION
#define STEP2_SCORE_WEIGHT 4
#define STEP2_SCORE_DIVIDER 1

#define STEP3_SCORE_WEIGHT 1
#define STEP3_SCORE_DIVIDER 2

#define STEP4_SCORE_WEIGHT 1
#define STEP4_SCORE_DIVIDER 2



#define DETECT_VERIFICATION_MATCH_OPTION_NORMAL 0
#define DETECT_VERIFICATION_MATCH_OPTION_DETAIL 1

#define ERROR_NO_IMAGE_INPUT 0x7ffff00