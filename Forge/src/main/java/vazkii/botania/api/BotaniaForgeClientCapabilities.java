package vazkii.botania.api;

import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;

import vazkii.botania.api.block.WandHUD;
import vazkii.botania.api.item.HaloRenderer;

public final class BotaniaForgeClientCapabilities {
	public static final Capability<WandHUD> WAND_HUD = CapabilityManager.get(new CapabilityToken<>() {});
	public static final Capability<HaloRenderer> HALO_RENDERER = CapabilityManager.get(new CapabilityToken<>() {});

	private BotaniaForgeClientCapabilities() {}
}
