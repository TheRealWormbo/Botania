package vazkii.botania.common.item;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.CorporeaSpark;
import vazkii.botania.api.item.HaloRenderer;
import vazkii.botania.api.mana.ManaItemHandler;
import vazkii.botania.client.lib.ResourcesLib;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.block.BotaniaBlocks;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.entity.CorporeaSparkEntity;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.PlayerHelper;

import java.util.Comparator;
import java.util.function.Supplier;

public class ReificationHaloItem extends AbstractHaloItem {
	private static final int EQUIPPED_CHECK_INTERVAL = 20;
	private static final int IDLE_CHECK_INTERVAL = 100;
	private static final double MAXIMUM_INDEX_DISTANCE = 100.0;
	private static final int MANA_COST_COUNT_BASE = 2;
	private static final int MANA_COST_EXTRACTION_BASE = 10;
	private static final double MANA_COST_DISTANCE_FACTOR = 2;
	private static final double MANA_COST_DISTANCE_EXPONENT = 0.25;

	private static final ResourceLocation glowTexture = new ResourceLocation(ResourcesLib.MISC_GLOW_CORPOREA);
	// can't create a stack of BotaniaBlocks.corporeaIndex here directly because things would explode
	private static final Supplier<ItemStack> corporeaIndexReference = Suppliers.memoize(() -> new ItemStack(BotaniaBlocks.corporeaIndex));

	private static final String TAG_LAST_REQUESTED = "lastRequested";
	private static final String TAG_STORED_REQUEST_PREFIX = "storedRequest";
	private static final String TAG_COUNT_UPDATE_TIME = "countUpdateTime";
	private static final String TAG_LAST_KNOWN_COUNTS = "lastKnownItemCounts";
	private static final String TAG_CONNECTED_INDEX_POS = "connectedIndexPos";
	private static final String TAG_CONNECTED_NETWORK_COLOR = "connectedNetworkColor";
	private static final String TAG_CONNECTING_SPARK_POS = "connectionSparkPos";

	public ReificationHaloItem(Properties props) {
		super(props);
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack haloStack = player.getItemInHand(hand);
		if (!world.isClientSide) {
			int segment = getSegmentLookedAt(haloStack, player);
			ItemStack requestStack = getRequestItemType(player, haloStack, segment);

			if (!requestStack.isEmpty()) {
				int count = player.isShiftKeyDown() ? requestStack.getMaxStackSize() : 1;
				// TODO: make Corporea request
			} else if (segment != 0) {
				ItemStack lastRequest = getLastRequested(haloStack);
				if (!lastRequest.isEmpty()) {
					saveSegmentItem(haloStack, segment, lastRequest);
				}
			}
		}

		return InteractionResultHolder.sidedSuccess(haloStack, world.isClientSide());
	}

	@NotNull
	private static ItemStack getLastRequested(@NotNull ItemStack haloStack) {
		return getSavedItemStack(haloStack, TAG_LAST_REQUESTED);
	}

	private static void saveLastRequested(@NotNull ItemStack haloStack, @Nullable ItemStack requestedStack) {
		saveItemStack(haloStack, TAG_LAST_REQUESTED, requestedStack);
	}

	@NotNull
	private static ItemStack getSegmentItem(@NotNull ItemStack haloStack, int pos) {
		return getSavedItemStack(haloStack, TAG_STORED_REQUEST_PREFIX + pos);
	}

	private static void saveSegmentItem(@NotNull ItemStack haloStack, int pos, @Nullable ItemStack requestedStack) {
		saveItemStack(haloStack, TAG_STORED_REQUEST_PREFIX + pos, requestedStack);
		clearCounterSlot(haloStack, pos);
	}

	@Nullable
	private static BlockPos getConnectedIndexPos(@NotNull ItemStack haloStack) {
		return getSavedPosition(haloStack, TAG_CONNECTED_INDEX_POS);
	}

	@Nullable
	private static DyeColor getConnectedNetworkColor(@NotNull ItemStack haloStack) {
		int colorId = ItemNBTHelper.getInt(haloStack, TAG_CONNECTED_NETWORK_COLOR, -1);
		return colorId != -1 ? DyeColor.byId(colorId) : null;
	}

	@Nullable
	private static BlockPos getConnectingSparkPos(@NotNull ItemStack haloStack) {
		return getSavedPosition(haloStack, TAG_CONNECTING_SPARK_POS);
	}

	private static void saveConnection(@NotNull ItemStack haloStack, @NotNull BlockPos connectedIndexPos,
			@NotNull DyeColor connectedNetworkColor, @NotNull BlockPos connectingSparkPos) {
		ItemNBTHelper.setLong(haloStack, TAG_CONNECTED_INDEX_POS, connectedIndexPos.asLong());
		ItemNBTHelper.setLong(haloStack, TAG_CONNECTING_SPARK_POS, connectingSparkPos.asLong());
		saveConnectedNetworkColor(haloStack, connectedNetworkColor);
	}

	private static void saveConnectedNetworkColor(@NotNull ItemStack haloStack, @NotNull DyeColor connectedNetworkColor) {
		ItemNBTHelper.setInt(haloStack, TAG_CONNECTED_NETWORK_COLOR, connectedNetworkColor.getId());
	}

	private void clearConnection(@NotNull ItemStack haloStack) {
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTED_INDEX_POS);
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTED_NETWORK_COLOR);
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTING_SPARK_POS);
	}

	private void saveItemCounts(@NotNull ItemStack haloStack, long gameTime, @NotNull ItemStack flexSlotStack, int[] counts) {
		ItemNBTHelper.setLong(haloStack, TAG_COUNT_UPDATE_TIME, gameTime);
		if (!flexSlotStack.isEmpty()) {
			saveSegmentItem(haloStack, 0, flexSlotStack);
		}
		saveLastKnownCounts(haloStack, counts, false);
	}

	private static void saveLastKnownCounts(@NotNull ItemStack haloStack, int[] counts, boolean mightBeEmpty) {
		if (mightBeEmpty) {
			int slot = counts.length - 1;
			while (slot >= 0 && counts[slot] != -1) {
				slot--;
			}
			if (slot == -1) {
				ItemNBTHelper.removeEntry(haloStack, TAG_LAST_KNOWN_COUNTS);
				return;
			}
		}
		ItemNBTHelper.setIntArray(haloStack, TAG_LAST_KNOWN_COUNTS, counts);
	}

	private static void clearCounterSlot(@NotNull ItemStack haloStack, int slot) {
		int[] counts = ItemNBTHelper.getIntArray(haloStack, TAG_LAST_KNOWN_COUNTS);
		if (counts.length == 0) {
			return;
		}
		counts[slot] = -1;
		saveLastKnownCounts(haloStack, counts, true);
	}

	@NotNull
	private static ItemStack getSavedItemStack(@NotNull ItemStack haloStack, @NotNull String tagName) {
		CompoundTag tag = ItemNBTHelper.getCompound(haloStack, tagName, true);
		return tag != null ? ItemStack.of(tag) : ItemStack.EMPTY;
	}

	private static void saveItemStack(@NotNull ItemStack haloStack, @NotNull String tagName, @Nullable ItemStack requestedStack) {
		if (requestedStack == null || requestedStack.isEmpty()) {
			ItemNBTHelper.removeEntry(haloStack, tagName);
		} else {
			ItemNBTHelper.setCompound(haloStack, tagName, requestedStack.copyWithCount(1).save(new CompoundTag()));
		}
	}

	@Nullable
	private static BlockPos getSavedPosition(@NotNull ItemStack haloStack, String tagName) {
		long posLong = ItemNBTHelper.getLong(haloStack, tagName, -1L);
		return posLong != -1L ? BlockPos.of(posLong) : null;
	}

	@Override
	public ResourceLocation getGlowResource(ItemStack haloStack) {
		return glowTexture;
	}

	@Override
	public ItemStack getDisplayItem(Player player, ItemStack haloStack, int seg) {
		if (seg == 0) {
			ItemStack otherHeldStack = getOtherHeldStack(player);
			return !otherHeldStack.isEmpty() ? otherHeldStack : corporeaIndexReference.get();
		}
		if (seg < 0 || seg >= SEGMENTS) {
			return ItemStack.EMPTY;
		}
		return getSegmentItem(haloStack, seg);
	}

	private static ItemStack getOtherHeldStack(LivingEntity living) {
		return PlayerHelper.getFirstHeldItem(living,
				itemStack -> !ReificationHaloItem.class.isAssignableFrom(itemStack.getItem().getClass()));
	}

	private static ItemStack getRequestItemType(LivingEntity living, ItemStack haloStack, int seg) {
		return seg == 0 ? getOtherHeldStack(living) : getSegmentItem(haloStack, seg);
	}

	@Override
	public void inventoryTick(ItemStack haloStack, Level world, Entity entity, int pos, boolean equipped) {
		super.inventoryTick(haloStack, world, entity, pos, equipped);

		if (!(entity instanceof Player player)) {
			return;
		}
		if (!wasEquipped(haloStack)) {
			if (world.getGameTime() % IDLE_CHECK_INTERVAL == 0) {
				updateConnection(world, player, haloStack);
			} else {
				validateKnownConnection(world, player, haloStack);
			}
			return;
		}

		ItemStack otherHeldItem = getOtherHeldStack(player);
		ItemStack lastHeldItem = getSegmentItem(haloStack, 0);
		if (!ItemNBTHelper.matchTagAndManaFullness(otherHeldItem, lastHeldItem)) {
			saveSegmentItem(haloStack, 0, ItemStack.EMPTY);
		}

		CorporeaSpark connectingSpark;
		if (world.getGameTime() % EQUIPPED_CHECK_INTERVAL == 0) {
			connectingSpark = updateConnection(world, player, haloStack);
			if (connectingSpark == null) {
				return;
			}
		} else {
			validateKnownConnection(world, player, haloStack);
			return;
		}

		ItemStack[] rememberedStacks = new ItemStack[SEGMENTS];
		int nonEmptySlots = 0;
		for (int seg = 0; seg < SEGMENTS; seg++) {
			ItemStack requestItemType = getRequestItemType(player, haloStack, seg);
			rememberedStacks[seg] = requestItemType;
			if (!requestItemType.isEmpty()) {
				nonEmptySlots++;
			}
		}
		if (nonEmptySlots == 0 || !ManaItemHandler.instance().requestManaExact(haloStack, player,
				(int) (MANA_COST_COUNT_BASE * EQUIPPED_CHECK_INTERVAL * Math.sqrt(nonEmptySlots)), true)) {
			return;
		}

		int[] counts = new int[SEGMENTS];
		for (int seg = 0; seg < SEGMENTS; seg++) {
			if (rememberedStacks[seg].isEmpty()) {
				counts[seg] = -1;
				continue;
			}
			var matcher = CorporeaHelper.instance().createMatcher(rememberedStacks[seg], true, false);
			var result = CorporeaHelper.instance().requestItem(matcher, -1, connectingSpark, player, true);
			counts[seg] = result.matchedCount();
		}

		saveItemCounts(haloStack, world.getGameTime(), rememberedStacks[0], counts);
	}

	private CorporeaSpark updateConnection(Level world, LivingEntity living, ItemStack haloStack) {
		final var indexes = CorporeaIndexBlockEntity.getIndexesByDistance(world, living.blockPosition(), MAXIMUM_INDEX_DISTANCE);
		if (indexes.isEmpty()) {
			clearConnection(haloStack);
			return null;
		}

		final BlockPos assumedHaloSparkPos = living.blockPosition().above();
		final var nearbySparks = world.getEntitiesOfClass(CorporeaSparkEntity.class,
				new AABB(assumedHaloSparkPos).inflate(CorporeaSparkEntity.SCAN_RANGE),
				sparkEntity -> sparkEntity.getMaster() != null);
		if (nearbySparks.isEmpty()) {
			clearConnection(haloStack);
			return null;
		}

		nearbySparks.sort(Comparator.comparingDouble(a -> a.blockPosition().distSqr(assumedHaloSparkPos)));

		for (var index : indexes) {
			var indexSpark = index.getSpark();
			if (indexSpark == null || indexSpark.getMaster() == null) {
				continue;
			}

			for (var connectionSpark : nearbySparks) {
				if (connectionSpark.getConnections().contains(indexSpark)) {
					saveConnection(haloStack, index.getBlockPos(), connectionSpark.getNetwork(), connectionSpark.blockPosition());
					return connectionSpark;
				}
			}
		}

		clearConnection(haloStack);
		return null;
	}

	private CorporeaSpark validateKnownConnection(Level world, LivingEntity living, ItemStack haloStack) {
		BlockPos indexPos = getConnectedIndexPos(haloStack);
		BlockPos sparkPos = getConnectingSparkPos(haloStack);
		if (indexPos == null || sparkPos == null) {
			return null;
		}

		if (indexPos.distSqr(living.blockPosition()) > MAXIMUM_INDEX_DISTANCE * MAXIMUM_INDEX_DISTANCE) {
			clearConnection(haloStack);
			return null;
		}

		var sparkOffset = living.blockPosition().above().offset(sparkPos);
		if (Math.abs(sparkOffset.getX()) > CorporeaSparkEntity.SCAN_RANGE
				|| Math.abs(sparkOffset.getY()) > CorporeaSparkEntity.SCAN_RANGE
				|| Math.abs(sparkOffset.getZ()) > CorporeaSparkEntity.SCAN_RANGE) {
			clearConnection(haloStack);
			return null;
		}

		CorporeaSpark connectingSpark = CorporeaHelper.INSTANCE.getSparkForBlock(world, sparkPos.below());
		if (connectingSpark == null || connectingSpark.getMaster() == null) {
			clearConnection(haloStack);
			return null;
		}

		var expectedCorporeaIndex = world.getBlockEntity(indexPos, BotaniaBlockEntities.CORPOREA_INDEX);
		var indexSpark = expectedCorporeaIndex.map(CorporeaIndexBlockEntity::getSpark);
		if (!indexSpark.isPresent() || connectingSpark.getConnections().contains(indexSpark.get())) {
			clearConnection(haloStack);
			return null;
		}

		if (getConnectedNetworkColor(haloStack) != connectingSpark.getNetwork()) {
			// in case the connected network somehow changed color (e.g. while this was not in the player's inventory)
			saveConnectedNetworkColor(haloStack, connectingSpark.getNetwork());
		}
		return connectingSpark;
	}

	@Override
	@SoftImplement("IForgeItem")
	public boolean onEntitySwing(ItemStack haloStack, LivingEntity living) {
		int segment = getSegmentLookedAt(haloStack, living);
		if (segment == 0) {
			return false;
		}

		ItemStack savedRequest = getSegmentItem(haloStack, segment);
		if (!savedRequest.isEmpty() && living.isShiftKeyDown()) {
			saveSegmentItem(haloStack, segment, null);
			return true;
		}

		return false;
	}

	@SoftImplement("IForgeItem")
	public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
		return slotChanged || reequipAnimation(oldStack, newStack);
	}

	@SoftImplement("FabricItem")
	public boolean allowNbtUpdateAnimation(Player player, InteractionHand hand, ItemStack before, ItemStack after) {
		return reequipAnimation(before, after);
	}

	private boolean reequipAnimation(ItemStack before, ItemStack after) {
		return !before.is(this) || !after.is(this) || !wasEquipped(after);
	}

	public static class ReificationHaloRenderer implements HaloRenderer {
		private final ItemStack stack;

		public ReificationHaloRenderer(ItemStack stack) {
			this.stack = stack;
		}

		@Override
		public void renderCentralSegment(GuiGraphics gui, Player player, ItemStack stack) {

		}

		@Override
		public void renderSavedSlotSegment(GuiGraphics gui, Player player, ItemStack stack, int slot) {
			ItemStack savedRequest = getSegmentItem(stack, slot);
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
