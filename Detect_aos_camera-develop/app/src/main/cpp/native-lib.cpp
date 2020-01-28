#include <jni.h>
#include <string>
#include <iostream>

#include <android/log.h>
#include "opencv2/core/mat.hpp"

#include "detect_image_quality.hpp"
#include "detect_verification_mobile.h"


#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, "NATIVE_CODE", __VA_ARGS__))
using namespace cv;
using namespace std;

extern "C" {

JNIEXPORT jstring JNICALL Java_kr_co_camera_view_CameraActivity_stringFromJNI(JNIEnv *env, jobject /* this */) {
    string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}

JNIEXPORT jlong JNICALL Java_kr_co_camera_view_CameraActivity_test1(JNIEnv *env, jobject /* this */, jlong inputLong) {
    return inputLong;
}

JNIEXPORT jfloat JNICALL Java_kr_co_camera_view_CameraActivity_test2(JNIEnv *env, jobject /* this */, jfloat inputFloat) {
    return inputFloat;
}

JNIEXPORT jint JNICALL Java_kr_co_camera_view_CameraActivity_test3(JNIEnv *env, jobject /* this */, jint inputInt) {
    return inputInt;
}


// NEW JNI CONNECTION CODE

// NAME RULE
// JNIEXPORT "return type" JNICALL Java_"package name divide with '_' "_"ClassName"_"functionName"
JNIEXPORT jlong JNICALL Java_kr_co_camera_view_CameraActivity_example(JNIEnv *env,/*super class*/ jobject super,
                                                                 jlong inputMat, jlong retMat, jint stdWidth,
                                                                 jobject retEv1,
                                                                 jobject retEv2) {//,/*parameter 1*/ jobject _object,/*parameter 2*/ jobject _retObj){

return 0x7fffffff;
}

// region camera preview 에서 작업

JNIEXPORT jint JNICALL
Java_kr_co_camera_view_CameraActivity_detectStep2(JNIEnv *env,
        jobject super /* this */,
        jlong src,
        jlong src2,
        jint inputInt) {

    Mat &srcMat = *(Mat *) src;
    Mat &srcMat2 = *(Mat *) src2;

    int result = detect_verification_step2_fast(srcMat, srcMat2, inputInt);

    return result;
}

JNIEXPORT jfloat JNICALL
Java_kr_co_camera_view_CameraActivity_imageEntropy(JNIEnv *env,
        /*super class*/ jobject super,
        jlong src) {
    //LOGD("JNI start");

    Mat &srcMat = *(Mat *) src;

    float entropy = image_entropy_logarithm_ratio(srcMat);

    //LOGD("JNI end");

    return entropy;
}

JNIEXPORT jfloatArray JNICALL
Java_kr_co_camera_view_CameraActivity_mctCountBinary(JNIEnv *env,/*super class*/ jobject super, jlong src) {
    //LOGD("JNI start");

    Mat &srcMat = *(Mat *) src;
    float left;
    float right;
    float up;
    float down;
    // left, right, up, and down must be read kotlin code.

    MCT_count_binary(srcMat, left, right, up, down);

    jfloatArray result;
    result = env->NewFloatArray(4);

    jfloat buf[4];
    buf[0] = left;
    buf[1] = right;
    buf[2] = up;
    buf[3] = down;

    env->SetFloatArrayRegion(result, 0, 4, buf);

    //LOGD("JNI end");

    return result;
}
// endregion



JNIEXPORT jint JNICALL
Java_kr_co_camera_view_result_1image_ResultActivity_extractKpts(JNIEnv *env,/*super class*/ jobject super, jlong src) {
    LOGD("KPTS1 START");

    Mat &srcMat = *(Mat *) src;
    Mat srcMatArr[5];
    srcMatArr[0]=srcMat.clone();
    srcMatArr[1]=srcMat.clone();
    srcMatArr[2]=srcMat.clone();
    srcMatArr[3]=srcMat.clone();
    srcMatArr[4]=srcMat.clone();
    vector<KeyPoint> kpts[5];
    Mat desc[5];

    detect_feature_extract_sequences(srcMatArr, 5, kpts, desc);

    LOGD("KPTS1 END");

    return 0;
}


JNIEXPORT jint JNICALL
Java_kr_co_camera_view_result_1image_ResultActivity_Verification(JNIEnv *env,/*super class*/ jobject super, jlong srcRef, jlong srcTar) {

    Mat &refRaw = *(Mat *) srcRef;
    Mat &tarRaw = *(Mat *) srcTar;

    Mat ref;
    Mat tar;

    cv::cvtColor(refRaw,ref,cv::COLOR_RGB2GRAY);
    cv::cvtColor(tarRaw,tar,cv::COLOR_RGB2GRAY);

    int width = ref.cols;
    int height = ref.rows;
    Rect crop = Rect((int)((float)width * 0.3f),(int)( (float)height * 0.2),(int)((float)width*0.3f),(int)((float)height*0.5));

    Mat cropRef=ref(crop);
    Mat cropTar=tar(crop);

    int result;

    LOGD("VERIFICATION START");

    detect_verification_step2_fast(cropRef, cropTar, result);


    LOGD("VERIFICATION END");

    return result;
}


JNIEXPORT jint JNICALL
Java_kr_co_camera_view_result_1image_ResultActivity_Verification3(JNIEnv *env,/*super class*/ jobject super, jlong srcRef, jlong srcTar) {

    Mat &refRaw = *(Mat *) srcRef;
    Mat &tarRaw = *(Mat *) srcTar;

    Mat ref;
    Mat tar;

    cv::cvtColor(refRaw,ref,cv::COLOR_RGB2GRAY);
    cv::cvtColor(tarRaw,tar,cv::COLOR_RGB2GRAY);

    //Rect crop = Rect((int)((float)width * 0.3f),(int)( (float)height * 0.2),(int)((float)width*0.3f),(int)((float)height*0.5));
    int ref_x=247;
    int ref_y=181;
    int ref_w=117;
    int ref_h=185;
    Rect ref_crop = Rect(ref_x,ref_y,ref_w,ref_h);

    Mat cropRef=ref(ref_crop);

    int tar_x=234;
    int tar_y=184;
    int tar_w=123;
    int tar_h=182;
    //int tar_x=247;
    //int tar_y=181;
    //int tar_w=117;
    //int tar_h=185;
    Rect tar_crop = Rect(tar_x,tar_y,tar_w,tar_h);


    Mat cropTar=tar(tar_crop);

    int result;

    LOGD("VERIFICATION START");

    detect_verification_step2_fast(cropRef, cropTar, result);
    //detect_verification_step3_fast(cropRef, cropTar, result);
    //detect_verification_step3_layer_increase_ver(cropRef, cropTar, result);


    LOGD("VERIFICATION END");

    return result;
}


JNIEXPORT jint JNICALL
Java_kr_co_camera_view_SearchModeActivity_vervice(JNIEnv *env,/*super class*/ jobject super, jlong srcRef, jlong srcTar) {

    Mat &refRaw = *(Mat *) srcRef;
    Mat &tarRaw = *(Mat *) srcTar;

    Mat ref;
    Mat tar;

    cv::cvtColor(refRaw,ref,cv::COLOR_RGB2GRAY);
    cv::cvtColor(tarRaw,tar,cv::COLOR_RGB2GRAY);

    int result;

    LOGD("VERIFICATION START");

    detect_verification_step3_fast(refRaw, tarRaw, result);
    //detect_verification_step3_fast(cropRef, cropTar, result);
    //detect_verification_step3_layer_increase_ver(cropRef, cropTar, result);


    LOGD("VERIFICATION END");

    return result;
}











}