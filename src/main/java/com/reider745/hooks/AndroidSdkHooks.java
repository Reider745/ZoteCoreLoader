package com.reider745.hooks;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.annotation.FieldPatched;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Locale;

@Hooks
public class AndroidSdkHooks implements HookClass {
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String INCREMENTAL = "1661782216865";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String RELEASE = "13";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String RELEASE_OR_CODENAME = "13";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String RELEASE_OR_PREVIEW_DISPLAY = "13";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String BASE_OS = "";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String SECURITY_PATCH = "2022-09-05";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String SDK = "33";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final int SDK_INT = 33;
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String CODENAME = "REL";
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final int RESOURCES_SDK_INT = 33;
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final int MIN_SUPPORTED_TARGET_SDK_INT = 23;
	@FieldPatched(class_name = "android.os.Build.VERSION")
	public static final String[] ACTIVE_CODENAMES = ("Base,Base11,Cupcake,Donut,Eclair,Eclair01,EclairMr1,"
			+ "Froyo,Gingerbread,GingerbreadMr1,Honeycomb,HoneycombMr1,HoneycombMr2,IceCreamSandwich,"
			+ "IceCreamSandwichMr1,JellyBean,JellyBeanMr1,JellyBeanMr2,Kitkat,KitkatWatch,Lollipop,"
			+ "LollipopMr1,M,N,NMr1,O,OMr1,P,Q,R,S,Sv2,Tiramisu").split(",");

	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public float density = 3f;
	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public int densityDpi = 480;
	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public float scaledDensity = 3f;
	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public int widthPixels = 2028;
	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public int heightPixels = 1080;
	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public float xdpi = 480f;
	@FieldPatched(class_name = "android.util.DisplayMetrics")
	public float ydpi = 480f;

	@Inject(class_name = "android.os.Environment")
	public static File getStorageDirectory() {
        return new File("Android");
    }

	@Inject(class_name = "android.os.Environment")
	public static File getDataDirectory() {
        return new File(getStorageDirectory(), "data");
    }

	@Inject(class_name = "android.os.Environment")
	public static File getExternalStorageDirectory() {
        return new File(".");
    }

	@Inject(class_name = "android.util.Base64", method = "decode", signature = "(Ljava/lang/String;I)[B")
	public static byte[] decodeBase64(String str, int flags) {
		return decodeBase64(str.getBytes(), flags);
	}

	@Inject(class_name = "android.util.Base64", method = "decode", signature = "([BI)[B")
	public static byte[] decodeBase64(byte[] input, int flags) {
		return decodeBase64(input, 0, input.length, flags);
	}

	@Inject(class_name = "android.util.Base64", method = "decode", signature = "([BIII)[B")
	public static byte[] decodeBase64(byte[] input, int offset, int len, int flags) {
		return Base64.getDecoder().decode(input);
	}

	@Inject(class_name = "android.util.Base64", method = "encodeToString", signature = "([BI)Ljava/lang/String;")
	public static String encodeToStringBase64(byte[] input, int flags) {
		try {
			return new String(encodeBase64(input, flags), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// US-ASCII is guaranteed to be available.
			throw new AssertionError(e);
		}
	}

	@Inject(class_name = "android.util.Base64", method = "encodeToString", signature = "([BIII)Ljava/lang/String;")
	public static String encodeToStringBase64(byte[] input, int offset, int len, int flags) {
		try {
			return new String(encodeBase64(input, offset, len, flags), "US-ASCII");
		} catch (UnsupportedEncodingException e) {
			// US-ASCII is guaranteed to be available.
			throw new AssertionError(e);
		}
	}

	@Inject(class_name = "android.util.Base64", method = "encode", signature = "([BI)[B")
	public static byte[] encodeBase64(byte[] input, int flags) {
		return encodeBase64(input, 0, input.length, flags);
	}

	@Inject(class_name = "android.util.Base64", method = "encode", signature = "([BIII)[B")
	public static byte[] encodeBase64(byte[] input, int offset, int len, int flags) {
		return Base64.getEncoder().encode(input);
	}

	@Inject(class_name = "android.util.Half")
	public static float toFloat(short h) {
		int bits = h & 0xffff;
		int s = bits & 0x8000;
		int e = (bits >>> 10) & 0x1f;
		int m = bits & 0x3ff;
		int outE = 0;
		int outM = 0;
		if (e == 0) { // Denormal or 0
			if (m != 0) {
				// Convert denorm fp16 into normalized fp32
				float o = Float.intBitsToFloat(126 << 23 + m);
				o -= Float.intBitsToFloat(126 << 23);
				return s == 0 ? o : -o;
			}
		} else {
			outM = m << 13;
			if (e == 0x1f) { // Infinite or NaN
				outE = 0xff;
				if (outM != 0) { // SNaNs are quieted
					outM |= 0x400000;
				}
			} else {
				outE = e + 112;
			}
		}
		int out = (s << 16) | (outE << 23) | outM;
		return Float.intBitsToFloat(out);
	}

	@Inject(class_name = "android.graphics.Color")
	public static int parseColor(String colorString) {
		if (colorString.charAt(0) == '#') {
			// Use a long to avoid rollovers on #ffXXXXXX
			long color = Long.parseLong(colorString.substring(1), 16);
			if (colorString.length() == 7) {
				// Set the alpha value
				color |= 0x00000000ff000000;
			} else if (colorString.length() != 9) {
				throw new IllegalArgumentException("Unknown color");
			}
			return (int) color;
		} else {
			Integer color = sColorNameMap.get(colorString.toLowerCase(Locale.ROOT));
			if (color != null) {
				return color;
			}
		}
		throw new IllegalArgumentException("Unknown color");
	}

	@Inject(class_name = "android.graphics.Color", signature = "(J)F")
	public static float red(long color) {
		if ((color & 0x3fL) == 0L)
			return ((color >> 48) & 0xff) / 255.0f;
		return toFloat((short) ((color >> 48) & 0xffff));
	}

	@Inject(class_name = "android.graphics.Color", signature = "(J)F")
	public static float green(long color) {
		if ((color & 0x3fL) == 0L)
			return ((color >> 40) & 0xff) / 255.0f;
		return toFloat((short) ((color >> 32) & 0xffff));
	}

	@Inject(class_name = "android.graphics.Color", signature = "(J)F")
	public static float blue(long color) {
		if ((color & 0x3fL) == 0L)
			return ((color >> 32) & 0xff) / 255.0f;
		return toFloat((short) ((color >> 16) & 0xffff));
	}

	@Inject(class_name = "android.graphics.Color", signature = "(J)F")
	public static float alpha(long color) {
		if ((color & 0x3fL) == 0L)
			return ((color >> 56) & 0xff) / 255.0f;
		return ((color >> 6) & 0x3ff) / 1023.0f;
	}

	@Inject(class_name = "android.graphics.Color", signature = "(I)I")
	public static int alpha(int color) {
		return color >>> 24;
	}

	@Inject(class_name = "android.graphics.Color", signature = "(I)I")
	public static int red(int color) {
		return (color >> 16) & 0xFF;
	}

	@Inject(class_name = "android.graphics.Color", signature = "(I)I")
	public static int green(int color) {
		return (color >> 8) & 0xFF;
	}

	@Inject(class_name = "android.graphics.Color", signature = "(I)I")
	public static int blue(int color) {
		return color & 0xFF;
	}

	@Inject(class_name = "android.graphics.Color")
	public static int rgb(
			int red,
			int green,
			int blue) {
		return 0xff000000 | (red << 16) | (green << 8) | blue;
	}

	@Inject(class_name = "android.graphics.Color")
	public static int rgb(float red, float green, float blue) {
		return 0xff000000 |
				((int) (red * 255.0f + 0.5f) << 16) |
				((int) (green * 255.0f + 0.5f) << 8) |
				(int) (blue * 255.0f + 0.5f);
	}

	@Inject(class_name = "android.graphics.Color")
	public static int argb(
			int alpha,
			int red,
			int green,
			int blue) {
		return (alpha << 24) | (red << 16) | (green << 8) | blue;
	}

	@Inject(class_name = "android.graphics.Color")
	public static int argb(float alpha, float red, float green, float blue) {
		return ((int) (alpha * 255.0f + 0.5f) << 24) |
				((int) (red * 255.0f + 0.5f) << 16) |
				((int) (green * 255.0f + 0.5f) << 8) |
				(int) (blue * 255.0f + 0.5f);
	}

	private static final HashMap<String, Integer> sColorNameMap;
	static {
		sColorNameMap = new HashMap<>();
		sColorNameMap.put("black", (Integer) 0xFF000000);
		sColorNameMap.put("darkgray", 0xFF444444);
		sColorNameMap.put("gray", 0xFF888888);
		sColorNameMap.put("lightgray", 0xFFCCCCCC);
		sColorNameMap.put("white", 0xFFFFFFFF);
		sColorNameMap.put("red", 0xFFFF0000);
		sColorNameMap.put("green", 0xFF00FF00);
		sColorNameMap.put("blue", 0xFF0000FF);
		sColorNameMap.put("yellow", 0xFFFFFF00);
		sColorNameMap.put("cyan", 0xFF00FFFF);
		sColorNameMap.put("magenta", 0xFFFF00FF);
		sColorNameMap.put("aqua", 0xFF00FFFF);
		sColorNameMap.put("fuchsia", 0xFFFF00FF);
		sColorNameMap.put("darkgrey", 0xFF444444);
		sColorNameMap.put("grey", 0xFF888888);
		sColorNameMap.put("lightgrey", 0xFFCCCCCC);
		sColorNameMap.put("lime", 0xFF00FF00);
		sColorNameMap.put("maroon", 0xFF800000);
		sColorNameMap.put("navy", 0xFF000080);
		sColorNameMap.put("olive", 0xFF808000);
		sColorNameMap.put("purple", 0xFF800080);
		sColorNameMap.put("silver", 0xFFC0C0C0);
		sColorNameMap.put("teal", 0xFF008080);
	}

	private AndroidSdkHooks() {
	}
}
