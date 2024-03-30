package vazkii.botania.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.piston.PistonStructureResolver;

import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Mutable;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;

import vazkii.botania.common.helper.ForcePushHelper;

@Mixin(value = PistonStructureResolver.class)
public class PistonStructureResolverMixin {
	/**
	 * The piston position. Needs to be defined as mutable final shadow field, otherwise access transformer/widener
	 * entry is required for this mixin to work, even though its code doesn't appear to write to it.
	 */
	@Final
	@Mutable
	@Shadow
	@SuppressWarnings("unused")
	private BlockPos pistonPos;

	/**
	 * Since the pushing piston block is handled separately via its position,
	 * replace it with the force relay's position when pushing blocks that way.
	 */
	@WrapOperation(
		method = "<init>", at = @At(
			value = "FIELD", opcode = Opcodes.PUTFIELD,
			target = "Lnet/minecraft/world/level/block/piston/PistonStructureResolver;pistonPos:Lnet/minecraft/core/BlockPos;"
		)
	)
	private void modifyForcePushOrigin(PistonStructureResolver instance, BlockPos value, Operation<Void> original) {
		original.call(instance, ForcePushHelper.isForcePush() ? ForcePushHelper.getForcePushOrigin() : value);
	}
}
