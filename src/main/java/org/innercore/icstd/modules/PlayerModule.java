package org.innercore.icstd.modules;

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

	public static native void getMoveInputVectorArray(float[] output);
}
