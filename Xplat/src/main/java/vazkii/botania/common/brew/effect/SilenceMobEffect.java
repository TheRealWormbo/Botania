package vazkii.botania.common.brew.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TraceableEntity;
import net.minecraft.world.level.gameevent.GameEvent;

import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.common.brew.BotaniaMobEffects;

public class SilenceMobEffect extends MobEffect {

	public SilenceMobEffect() {
		super(MobEffectCategory.BENEFICIAL, 0x4c7f99);
	}

	public static boolean shouldSuppressVibration(Vec3 source, @NotNull GameEvent.Context context) {
		if (hasSilenceEffect(context.sourceEntity())) {
			return true;
		}

		// can the owner of the entity be determined? (e.g. projectiles, dropped items, or primed TNT)
		if (context.sourceEntity() instanceof TraceableEntity traceableEntity && hasSilenceEffect(traceableEntity.getOwner())) {
			return true;
		}

		// TODO: maybe cancel all vibrations in range of active incense plate with this effect?

		return false;
	}

	private static boolean hasSilenceEffect(@Nullable Entity entity) {
		return entity instanceof LivingEntity living && living.hasEffect(BotaniaMobEffects.silence);
	}
}
