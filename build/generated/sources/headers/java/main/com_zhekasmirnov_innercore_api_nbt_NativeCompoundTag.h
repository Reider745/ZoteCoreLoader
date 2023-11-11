/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag */

#ifndef _Included_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
#define _Included_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    nativeConstruct
 * Signature: ()J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_nativeConstruct
  (JNIEnv *, jclass);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    nativeClone
 * Signature: (J)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_nativeClone
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    nativeFinalize
 * Signature: (J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_nativeFinalize
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getAllKeys
 * Signature: ()[Ljava/lang/String;
 */
JNIEXPORT jobjectArray JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getAllKeys
  (JNIEnv *, jobject);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    contains
 * Signature: (Ljava/lang/String;)Z
 */
JNIEXPORT jboolean JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_contains
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    containsValueOfType
 * Signature: (Ljava/lang/String;I)Z
 */
JNIEXPORT jboolean JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_containsValueOfType
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getValueType
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getValueType
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getByte
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getByte
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getShort
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getShort
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getInt
 * Signature: (Ljava/lang/String;)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getInt
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getInt64
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getInt64
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getFloat
 * Signature: (Ljava/lang/String;)F
 */
JNIEXPORT jfloat JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getFloat
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getDouble
 * Signature: (Ljava/lang/String;)D
 */
JNIEXPORT jdouble JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getDouble
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    getString
 * Signature: (Ljava/lang/String;)Ljava/lang/String;
 */
JNIEXPORT jstring JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_getString
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putByte
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putByte
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putShort
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putShort
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putInt
 * Signature: (Ljava/lang/String;I)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putInt
  (JNIEnv *, jobject, jstring, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putInt64
 * Signature: (Ljava/lang/String;J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putInt64
  (JNIEnv *, jobject, jstring, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putFloat
 * Signature: (Ljava/lang/String;F)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putFloat
  (JNIEnv *, jobject, jstring, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putDouble
 * Signature: (Ljava/lang/String;D)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putDouble
  (JNIEnv *, jobject, jstring, jdouble);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    putString
 * Signature: (Ljava/lang/String;Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_putString
  (JNIEnv *, jobject, jstring, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    remove
 * Signature: (Ljava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_remove
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    clear
 * Signature: ()V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_clear
  (JNIEnv *, jobject);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    nativeGetCompoundTag
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_nativeGetCompoundTag
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    nativeGetListTag
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_nativeGetListTag
  (JNIEnv *, jobject, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag
 * Method:    nativePutTag
 * Signature: (Ljava/lang/String;J)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_nbt_NativeCompoundTag_nativePutTag
  (JNIEnv *, jobject, jstring, jlong);

#ifdef __cplusplus
}
#endif
#endif