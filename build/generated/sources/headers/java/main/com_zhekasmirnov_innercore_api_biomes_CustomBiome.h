/* DO NOT EDIT THIS FILE - it is machine generated */
#include <jni.h>
/* Header for class com_zhekasmirnov_innercore_api_biomes_CustomBiome */

#ifndef _Included_com_zhekasmirnov_innercore_api_biomes_CustomBiome
#define _Included_com_zhekasmirnov_innercore_api_biomes_CustomBiome
#ifdef __cplusplus
extern "C" {
#endif
/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeRegister
 * Signature: (Ljava/lang/String;)J
 */
JNIEXPORT jlong JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeRegister
  (JNIEnv *, jclass, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeGetId
 * Signature: (J)I
 */
JNIEXPORT jint JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeGetId
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeIsInvalid
 * Signature: (J)Z
 */
JNIEXPORT jboolean JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeIsInvalid
  (JNIEnv *, jclass, jlong);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetGrassColor
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetGrassColor
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetSkyColor
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetSkyColor
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetFoliageColor
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetFoliageColor
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetWaterColor
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetWaterColor
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetTemperatureAndDownfall
 * Signature: (JFF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetTemperatureAndDownfall
  (JNIEnv *, jclass, jlong, jfloat, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetCoverBlock
 * Signature: (JIF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetCoverBlock
  (JNIEnv *, jclass, jlong, jint, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetSurfaceBlock
 * Signature: (JIF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetSurfaceBlock
  (JNIEnv *, jclass, jlong, jint, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetFillingBlock
 * Signature: (JIF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetFillingBlock
  (JNIEnv *, jclass, jlong, jint, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetSeaFloorBlock
 * Signature: (JIF)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetSeaFloorBlock
  (JNIEnv *, jclass, jlong, jint, jfloat);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetSeaFloorDepth
 * Signature: (JI)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetSeaFloorDepth
  (JNIEnv *, jclass, jlong, jint);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetClientJson
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetClientJson
  (JNIEnv *, jclass, jlong, jstring);

/*
 * Class:     com_zhekasmirnov_innercore_api_biomes_CustomBiome
 * Method:    nativeSetServerJson
 * Signature: (JLjava/lang/String;)V
 */
JNIEXPORT void JNICALL Java_com_zhekasmirnov_innercore_api_biomes_CustomBiome_nativeSetServerJson
  (JNIEnv *, jclass, jlong, jstring);

#ifdef __cplusplus
}
#endif
#endif
