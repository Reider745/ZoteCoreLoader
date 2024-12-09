package com.reider745.event;

import com.reider745.api.hooks.HookClass;
import com.reider745.api.hooks.HookController;
import com.reider745.api.hooks.TypeHook;
import com.reider745.api.hooks.annotation.Hooks;
import com.reider745.api.hooks.annotation.Inject;
import com.reider745.hooks.item.ItemUtils;
import com.zhekasmirnov.innercore.api.NativeCallback;

import cn.nukkit.block.BlockDispenser;
import cn.nukkit.dispenser.DefaultDispenseBehavior;
import cn.nukkit.event.Cancellable;
import cn.nukkit.event.block.BlockEvent;
import cn.nukkit.item.Item;
import cn.nukkit.math.BlockFace;
import cn.nukkit.math.BlockFace.Axis;
import cn.nukkit.math.Vector3;

@Hooks(className = "cn.nukkit.block.BlockDispenser")
public class DispenseBehaviorOverrides implements HookClass {
	public static int dispenseSlot = -1;

	public static class BlockDispenseEvent extends BlockEvent implements Cancellable {
		public BlockDispenseEvent(BlockDispenser block) {
			super(block);
		}
	}

	@Inject(className = "cn.nukkit.dispenser.DefaultDispenseBehavior", type = TypeHook.BEFORE, signature = "(Lcn/nukkit/block/BlockDispenser;Lcn/nukkit/math/BlockFace;Lcn/nukkit/item/Item;)Lcn/nukkit/item/Item;")
	public static void dispense(HookController<DefaultDispenseBehavior> controller, DefaultDispenseBehavior behavior, BlockDispenser block,
			BlockFace face, Item item) {
		Vector3 dispensePos = block.getDispensePosition();

		if (face.getAxis() == Axis.Y) {
			dispensePos.y -= 0.125;
		} else {
			dispensePos.y -= 0.15625;
		}

		BlockDispenseEvent event = new BlockDispenseEvent(block);
		EventListener.consumeEvent(event,
				() -> NativeCallback.onItemDispensed((float) dispensePos.x, (float) dispensePos.y,
						(float) dispensePos.z, face.getIndex(), item.getId(), item.getCount(), item.getDamage(),
						ItemUtils.getItemInstanceExtra(item), block.getLevel(), dispenseSlot));
		dispenseSlot = -1;

		if (event.isCancelled()) {
			controller.setReplace(true);
			controller.setResult(null);
		}
	}
}
