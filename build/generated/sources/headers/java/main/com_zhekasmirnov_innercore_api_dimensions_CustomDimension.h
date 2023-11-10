/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zhekasmirnov_innercore_api_dimensions_CustomDimension */

#ifndef _Included_com_zhekasmirnov_innercore_api_dimensions_CustomDimension
#define _Included_com_zhekasmirnov_innercore_api_dimensions_CustomDimension
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeConstruct
 * Signature: (ILjava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeConstruct
  (JNIEnv *, jclass, jint, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeGetCustomDimensionById
 * Signature: (I)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeGetCustomDimensionById
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeIsLimboDimensionId
 * Signature: (I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeIsLimboDimensionId
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeOverrideVanillaGenerator
 * Signature: (IJ)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeOverrideVanillaGenerator
  (JNIEnv *, jclass, jint, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetGenerator
 * Signature: (JJ)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetGenerator
  (JNIEnv *, jclass, jlong, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetHasSkyLight
 * Signature: (JZ)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetHasSkyLight
  (JNIEnv *, jclass, jlong, jboolean);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetSkyColor
 * Signature: (JFFF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetSkyColor
  (JNIEnv *, jclass, jlong, jfloat, jfloat, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeResetSkyColor
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeResetSkyColor
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetFogColor
 * Signature: (JFFF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetFogColor
  (JNIEnv *, jclass, jlong, jfloat, jfloat, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeResetFogColor
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeResetFogColor
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetCloudColor
 * Signature: (JFFF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetCloudColor
  (JNIEnv *, jclass, jlong, jfloat, jfloat, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeResetCloudColor
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeResetCloudColor
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetSunsetColor
 * Signature: (JFFF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetSunsetColor
  (JNIEnv *, jclass, jlong, jfloat, jfloat, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeResetSunsetColor
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeResetSunsetColor
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeSetFogDistance
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeSetFogDistance
  (JNIEnv *, jclass, jlong, jfloat, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_dimensions_CustomDimension
 * Method:    nativeResetFogDistance
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_dimensions_CustomDimension_nativeResetFogDistance
  (JNIEnv *, jclass, jlong);

#ifdef __cplusplus
}
#endif
#endif
