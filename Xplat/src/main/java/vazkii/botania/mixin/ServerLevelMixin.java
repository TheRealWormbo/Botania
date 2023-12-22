/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.mixin;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.Vec3;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import vazkii.botania.common.brew.effect.SilenceMobEffect;
import vazkii.botania.common.world.SkyblockWorldEvents;
import vazkii.botania.xplat.XplatAbstractions;

@Mixin(ServerLevel.class)
public class ServerLevelMixin {
	@Inject(at = @At("RETURN"), method = "addPlayer")
	private void onEntityAdd(ServerPlayer entity, CallbackInfo ci) {
		if (XplatAbstractions.INSTANCE.gogLoaded()) {
			SkyblockWorldEvents.syncGogStatus(entity);
		}
	}

	@Inject(at = @At("HEAD"), method = "gameEvent", cancellable = true)
	private void onGameEvent(GameEvent gameEvent, Vec3 source, GameEvent.Context context, CallbackInfo ci) {
		if (VibrationSystem.getGameEventFrequency(gameEvent) != 0 && SilenceMobEffect.shouldSuppressVibration(source, context)) {
			ci.cancel();
		}
	}
}
