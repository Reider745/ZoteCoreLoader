package org.innercore.icstd.modules;

import com.reider745.api.ClientOnly;
import org.innercore.icstd.common.Vec3f;
import org.innercore.icstd.common.script.GenericScriptPos3;

import org.innercore.icstd.annotations.ApiBinding;
import com.zhekasmirnov.innercore.api.gen.PlayerBindGenBase;

@ApiBinding("Player")
public class PlayerModule extends PlayerBindGenBase {
	public static Vec3f getMoveInputVector() {
		float[] vector = new float[3];
		getMoveInputVectorArray(vector);
		return new Vec3f(vector[0], vector[1], vector[2]);
	}

	@ApiBinding("getMoveInputVector")
	public static GenericScriptPos3 getMoveInputVectorForScript() {
		float[] vector = new float[3];
		getMoveInputVectorArray(vector);
		return new GenericScriptPos3(vector[0], vector[1], vector[2]);
	}

	@ClientOnly
	public static void getMoveInputVectorArray(float[] output) {}

	@ClientOnly
	public static boolean wantsMove() {
		return false;
	}

	@ClientOnly
	public static float getMoveInputVectorX() {
		return 0;
	}

	@ClientOnly
	public static float getMoveInputVectorY() {
		return 0;
	}

	@ClientOnly
	public static float getMoveInputVectorZ() {
		return 0;
	}

	@ClientOnly
	public static void setFov(float fov) {}

	@ClientOnly
	public static void resetFov() {}

	@ClientOnly
	public static void setViewPerspective(int perspective) {}

	@ClientOnly
	public static void resetViewPerspective() {}
}
