package vazkii.botania.common.item;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import vazkii.botania.client.lib.ResourcesLib;

public class ReificationHaloItem extends AbstractHaloItem {

	private static final ResourceLocation glowTexture = new ResourceLocation(ResourcesLib.MISC_GLOW_PURPLE);
	private static final ItemStack corporeaIndex = new ItemStack(Blocks.CRAFTING_TABLE);

	public ReificationHaloItem(Properties props) {
		super(props);
	}

	@Override
	public ResourceLocation getGlowResource(ItemStack stack) {
		return glowTexture;
	}

	@Override
	public ItemStack getDisplayItem(Level level, ItemStack stack, int seg) {
		if (seg == 0) {
			return corporeaIndex;
		}
		// TODO: handle stored items
		return ItemStack.EMPTY;
	}

	@Override
	public void renderCentralSegment(GuiGraphics gui, Player player, ItemStack stack) {

	}

	@Override
	public void renderSavedSlotSegment(GuiGraphics gui, Player player, ItemStack stack, int slot) {

	}
}
