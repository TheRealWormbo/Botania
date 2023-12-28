package vazkii.botania.common.item;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.jetbrains.annotations.NotNull;

import vazkii.botania.api.item.HaloRenderer;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.PlayerHelper;

import java.util.function.Supplier;

public class ReificationHaloItem extends AbstractHaloItem {

	private static final ResourceLocation glowTexture = new ResourceLocation(ResourcesLib.MISC_GLOW_CORPOREA);
	// can't create a stack of BotaniaBlocks.corporeaIndex here directly because things would explode
	private static final Supplier<ItemStack> corporeaIndexReference = Suppliers.memoize(() -> new ItemStack(BotaniaBlocks.corporeaIndex));

	private static final String TAG_LAST_REQUESTED = "lastRequested";
	private static final String TAG_STORED_REQUEST_PREFIX = "storedRequest";

	public ReificationHaloItem(Properties props) {
		super(props);
	}

	@NotNull
	private static ItemStack getLastRequested(@NotNull ItemStack halo) {
		return getSavedItemStack(halo, TAG_LAST_REQUESTED);
	}

	private static void rememberLastRequested(@NotNull ItemStack halo, ItemStack requestedStack) {
		saveItemStack(halo, TAG_LAST_REQUESTED, requestedStack);
	}

	@NotNull
	private static ItemStack getSavedRequest(@NotNull ItemStack halo, int pos) {
		return getSavedItemStack(halo, TAG_STORED_REQUEST_PREFIX + pos);
	}

	private static void saveRequest(@NotNull ItemStack halo, int pos, ItemStack requestedStack) {
		saveItemStack(halo, TAG_STORED_REQUEST_PREFIX + pos, requestedStack);
	}

	@NotNull
	private static ItemStack getSavedItemStack(@NotNull ItemStack halo, String tagName) {
		CompoundTag tag = ItemNBTHelper.getCompound(halo, tagName, true);
		return tag != null ? ItemStack.of(tag) : ItemStack.EMPTY;
	}

	private static void saveItemStack(ItemStack halo, String tagName, ItemStack requestedStack) {
		if (requestedStack == null || requestedStack.isEmpty()) {
			ItemNBTHelper.removeEntry(halo, tagName);
		} else {
			ItemNBTHelper.setCompound(halo, tagName, requestedStack.copyWithCount(1).save(new CompoundTag()));
		}
	}

	@Override
	public ResourceLocation getGlowResource(ItemStack stack) {
		return glowTexture;
	}

	@Override
	public ItemStack getDisplayItem(Player player, ItemStack stack, int seg) {
		if (seg == 0) {
			ItemStack otherHeldStack = PlayerHelper.getFirstHeldItem(player,
					itemStack -> !ReificationHaloItem.class.isAssignableFrom(itemStack.getItem().getClass()));
			return !otherHeldStack.isEmpty() ? otherHeldStack : corporeaIndexReference.get();
		}
		// TODO: handle stored items
		return ItemStack.EMPTY;
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int pos, boolean equipped) {
		super.inventoryTick(stack, world, entity, pos, equipped);

	}

	public static class ReificationHaloRenderer implements HaloRenderer {
		private ItemStack stack;

		public ReificationHaloRenderer(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public void renderCentralSegment(GuiGraphics gui, Player player, ItemStack stack) {

		}

		@Override
		public void renderSavedSlotSegment(GuiGraphics gui, Player player, ItemStack stack, int slot) {
			ItemStack savedRequest = getSavedRequest(stack, slot);
			Component label;
			boolean setRequest = false;
			if (savedRequest.isEmpty()) {
				savedRequest = getLastRequested(stack);
				label = Component.translatable("");
			} else {
				label = savedRequest.getHoverName();
				setRequest = true;
			}

			renderRequestSlot(gui, label, savedRequest, player, setRequest);
		}

		private void renderRequestSlot(GuiGraphics gui, Component label, ItemStack savedRequest, Player player, boolean setRequest) {
			// TODO
		}
	}
}
