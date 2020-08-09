/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.tile;

import net.minecraft.util.Tickable;
import vazkii.botania.client.fx.WispParticleData;

public class TileSpiritShrine extends TileMod implements Tickable {

	private int ticks;

	public TileSpiritShrine() {
		super(null);
	}

	@Override
	public void tick() {
		if (world.isClient) {
			if (ticks >= 40) {
				float[][] colors = new float[][] {
						{ 0F, 0.25F, 1F },
						{ 1F, 0F, 0.2F },
						{ 0F, 1F, 0.25F },
						{ 1F, 1F, 0.25F },
						{ 1F, 0.25F, 1F },
						{ 0.25F, 1F, 1F }
				};

				int totalSpiritCount = 6;
				double tickIncrement = 360D / totalSpiritCount;

				int liftTicks = 40 * (totalSpiritCount + 1);
				int existTicks = liftTicks * 2;
				int lowerTicks = existTicks + liftTicks;

				if (ticks < lowerTicks) {
					int speed = 5;
					double wticks = ticks * speed - tickIncrement;
					double r = Math.sin((ticks >= liftTicks ? (ticks - liftTicks) * speed - tickIncrement : -tickIncrement) * Math.PI / 180 * 0.75) + 1 * 1.25 + 0.5;
					double g = Math.sin(wticks * Math.PI / 180 * 0.55);

					for (int i = 0; i < totalSpiritCount; i++) {
						double x = pos.getX() + Math.sin(wticks * Math.PI / 180) * r + 0.5;
						double y = pos.getY() + (ticks > existTicks ? 40 - (double) (ticks - existTicks) : Math.min(80 + 40 * i, ticks) - 40 * (i + 1)) * 0.1;
						double z = pos.getZ() + Math.cos(wticks * Math.PI / 180) * r + 0.5;

						wticks += tickIncrement;
						float[] colorsfx = colors[i >= colors.length ? 0 : i];
						WispParticleData data = WispParticleData.wisp(0.85F, colorsfx[0], colorsfx[1], colorsfx[2], 0.25F);
						world.addParticle(data, x, y, z, 0, (float) (-g * 0.05), 0);

						data = WispParticleData.wisp((float) Math.random() * 0.1F + 0.1F, colorsfx[0], colorsfx[1], colorsfx[2], 0.9F);
						world.addParticle(data, x, y, z, (float) (Math.random() - 0.5) * 0.05F, (float) (Math.random() - 0.5) * 0.05F, (float) (Math.random() - 0.5) * 0.05F);
					}
				}
			}

			++ticks;
		}
	}

}