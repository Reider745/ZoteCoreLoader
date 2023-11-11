/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap */

#ifndef _Included_com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap
#define _Included_com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap
 * Method:    clearAll
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap_clearAll
  (JNIEnv *, jclass);

/*
 * Class:     com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap
 * Method:    mapConversion
 * Signature: (II)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap_mapConversion
  (JNIEnv *, jclass, jint, jint);

/*
 * Class:     com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap
 * Method:    dynamicToStatic
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap_dynamicToStatic
  (JNIEnv *, jclass, jint);

/*
 * Class:     com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap
 * Method:    staticToDynamic
 * Signature: (I)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_apparatus_cpp_NativeIdConversionMap_staticToDynamic
  (JNIEnv *, jclass, jint);

#ifdef __cplusplus
}
#endif
#endif