package org.innercore.icstd.modules;

import com.reider745.api.ClientOnly;
import org.innercore.icstd.annotations.ArgOverload;
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

	@ApiBinding
	@ClientOnly
	public static String getName() {
		return "";
	}

	@ClientOnly
	@ApiBinding
	public static void getMoveInputVectorArray(float[] output) {}

	@ClientOnly
	@ApiBinding
	public static boolean wantsMove() {
		return false;
	}

	@ClientOnly
	@ApiBinding
	public static float getMoveInputVectorX() {
		return 0;
	}

	@ClientOnly
	@ApiBinding
	public static float getMoveInputVectorY() {
		return 0;
	}

	@ClientOnly
	@ApiBinding
	public static float getMoveInputVectorZ() {
		return 0;
	}

	@ClientOnly
	@ApiBinding
	public static float getFov(@ArgOverload("false") boolean applyModifiers) {
		return 1;
	}

	@ClientOnly
	@ApiBinding
	public static void setFov(float fov, @ArgOverload("false") boolean applyModifiers) {}

	@ClientOnly
	@ApiBinding
	public static void resetFov() {}

	@ApiBinding
	@ClientOnly
	public static float getFovModifier() {
		return 1;
	}

	@ClientOnly
	public static int getViewPerspective() {
		return 0;
	}

	@ClientOnly
	@ApiBinding
	public static void setViewPerspective(int perspective) {}

	@ClientOnly
	@ApiBinding
	public static void resetViewPerspective() {}
}
