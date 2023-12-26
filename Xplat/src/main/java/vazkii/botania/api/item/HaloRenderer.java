package vazkii.botania.api.item;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface HaloRenderer {
	void renderCentralSegment(GuiGraphics gui, Player player, ItemStack stack);
	void renderSavedSlotSegment(GuiGraphics gui, Player player, ItemStack stack, int slot);
}
