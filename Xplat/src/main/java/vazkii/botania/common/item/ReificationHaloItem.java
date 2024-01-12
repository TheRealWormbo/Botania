package vazkii.botania.common.item;

import com.google.common.base.Suppliers;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
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

import vazkii.botania.api.BotaniaAPI;
import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.api.corporea.CorporeaRequestMatcher;
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
import vazkii.botania.xplat.XplatAbstractions;

import java.util.Comparator;
import java.util.function.Supplier;

public class ReificationHaloItem extends AbstractHaloItem {
	/**
	 * How far away a Corporea Index block may (Euclidean distance) be to attempt a connection.
	 */
	private static final double MAXIMUM_INDEX_DISTANCE = 100.0;

	/**
	 * Scan range for a nearby Corporea spark to access a network containing a Corporea Index.
	 */
	private static final int SPARK_SCAN_RANGE = CorporeaSparkEntity.SCAN_RANGE;

	private static final int EQUIPPED_CHECK_INTERVAL = 2 * SEGMENTS;
	private static final int IDLE_CHECK_INTERVAL = 100;

	/**
	 * Base upkeep mana cost while holding the halo, multiplied by the number of segments with stored items.
	 */
	private static final int MANA_COST_COUNT_BASE = 1;

	/**
	 * Base mana cost per extracted item.
	 */
	private static final int MANA_COST_EXTRACTION_BASE = 2;

	/**
	 * Multiplier for distance component of extraction mana cost.
	 */
	private static final double MANA_COST_DISTANCE_FACTOR = 2.0;

	/**
	 * Exponent for (pre-squared) Euclidean distance value to determine distance component of
	 */
	private static final double MANA_COST_DISTANCE_EXPONENT = 0.25;

	private static final ResourceLocation glowTexture = new ResourceLocation(ResourcesLib.MISC_GLOW_CORPOREA);
	// can't create a stack of BotaniaBlocks.corporeaIndex here directly because things would explode
	private static final Supplier<ItemStack> corporeaIndexReference = Suppliers.memoize(() -> new ItemStack(BotaniaBlocks.corporeaIndex));

	private static final String TAG_LAST_REQUESTED = "lastRequested";
	private static final String TAG_STORED_REQUEST_PREFIX = "storedRequest";
	private static final String TAG_COUNT_UPDATE_TIME_PREFIX = "countUpdateTime";
	private static final String TAG_LAST_KNOWN_COUNT_PREFIX = "lastKnownItemCount";
	private static final String TAG_CONNECTED_INDEX_POS = "connectedIndexPos";
	private static final String TAG_CONNECTED_NETWORK_COLOR = "connectedNetworkColor";
	private static final String TAG_CONNECTING_SPARK_POS = "connectionSparkPos";
	private static final String TAG_CONNECTION_UPDATE_TIME = "connectionUpdateTime";

	public ReificationHaloItem(Properties props) {
		super(props);
	}

	public static boolean isHoldingHalo(Player player) {
		return player != null && (player.getMainHandItem().is(BotaniaItems.corporeaHalo) || player.getOffhandItem().is(BotaniaItems.corporeaHalo));
	}

	/**
	 * Checks whether the player is currently accessing a corporea index via the index segment of a Reification Halo.
	 * (meant to be used on the clientside)
	 * 
	 * @param player The player.
	 * @return {@code true} if holding the halo in main or off-hand, looking at segment 0, and the connected network
	 *         color is known, {@code false} otherwise.
	 */
	public static boolean isAccessingCorporeaIndex(Player player) {
		ItemStack mainHandStack = player.getMainHandItem();
		ItemStack offHandStack = player.getOffhandItem();
		return isAccessingCorporeaIndex(player, mainHandStack) || isAccessingCorporeaIndex(player, offHandStack);
	}

	private static boolean isAccessingCorporeaIndex(Player player, ItemStack haloStack) {
		return haloStack.getItem() instanceof ReificationHaloItem &&
				getSegmentLookedAt(haloStack, player) == 0 &&
				getConnectedNetworkColor(haloStack) != null;
	}

	@NotNull
	@Override
	public InteractionResultHolder<ItemStack> use(Level world, Player player, InteractionHand hand) {
		ItemStack haloStack = player.getItemInHand(hand);
		int segment = getSegmentLookedAt(haloStack, player);
		ItemStack requestStack = getRequestItemType(player, haloStack, segment);

		if (world.isClientSide()) {
			// TODO: maybe open chat if connected and looking at segment 0 to make text request?
			return InteractionResultHolder.sidedSuccess(haloStack, world.isClientSide());
		}

		if (requestStack.isEmpty()) {
			if (segment != 0) {
				ItemStack otherHeldStack = getOtherHeldStack(player);
				ItemStack requestStackToSave = otherHeldStack.isEmpty() ? getLastRequested(haloStack) : otherHeldStack;
				if (!requestStackToSave.isEmpty() && XplatAbstractions.INSTANCE.findManaItem(requestStack) == null) {
					saveSegmentItem(haloStack, segment, requestStackToSave);
					return InteractionResultHolder.sidedSuccess(haloStack, world.isClientSide());
				}
			}
			return InteractionResultHolder.fail(haloStack);
		}

		saveLastRequested(haloStack, requestStack);
		int count = player.isSecondaryUseActive() ? requestStack.getMaxStackSize() : 1;
		CorporeaRequestMatcher matcher = CorporeaHelper.instance().createMatcher(requestStack, true, false);
		return doRequest(world, player, haloStack, matcher, count)
				? InteractionResultHolder.sidedSuccess(haloStack, world.isClientSide())
				: InteractionResultHolder.fail(haloStack);
	}

	public static boolean doRequest(Level world, Player player, ItemStack haloStack, CorporeaRequestMatcher matcher, int count) {
		CorporeaSpark connectingSpark = validateKnownConnection(world, player, haloStack);
		BlockPos indexPos = getConnectedIndexPos(haloStack);
		if (connectingSpark == null || indexPos == null) {
			return false;
		}

		int manaCost = calcRequestCost(count, indexPos.distSqr(player.blockPosition()));
		if (!ManaItemHandler.instance().requestManaExact(haloStack, player, manaCost, true)) {
			return false;
		}
		var result = CorporeaHelper.instance().requestItem(matcher, count, connectingSpark, player, true);
		connectingSpark.onItemsRequested(result.stacks());
		for (ItemStack resultStack : result.stacks()) {
			if (!player.addItem(resultStack)) {
				player.drop(resultStack, true, true);
			}
		}
		CorporeaIndexBlockEntity.sendRequestResult((ServerPlayer) player, matcher, count, player.blockPosition(), result);

		return true;
	}

	private static int calcRequestCost(int count, double distSqr) {
		double distanceCostFactor = MANA_COST_DISTANCE_FACTOR * Math.pow(distSqr, MANA_COST_DISTANCE_EXPONENT);
		return (int) Math.ceil(MANA_COST_EXTRACTION_BASE * count * distanceCostFactor);
	}

	@NotNull
	private static ItemStack getLastRequested(@NotNull ItemStack haloStack) {
		return getSavedItemStack(haloStack, TAG_LAST_REQUESTED);
	}

	public static void saveLastRequested(@NotNull ItemStack haloStack, @Nullable ItemStack requestedStack) {
		saveItemStack(haloStack, TAG_LAST_REQUESTED, requestedStack);
	}

	@NotNull
	private static ItemStack getSegmentItem(@NotNull ItemStack haloStack, int pos) {
		return getSavedItemStack(haloStack, TAG_STORED_REQUEST_PREFIX + pos);
	}

	private static void saveSegmentItem(@NotNull ItemStack haloStack, int pos, @Nullable ItemStack requestedStack) {
		saveItemStack(haloStack, TAG_STORED_REQUEST_PREFIX + pos, requestedStack);
		clearItemCount(haloStack, pos);
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
			@NotNull DyeColor connectedNetworkColor, @NotNull BlockPos connectingSparkPos, long updateTime) {
		ItemNBTHelper.setLong(haloStack, TAG_CONNECTED_INDEX_POS, connectedIndexPos.asLong());
		ItemNBTHelper.setLong(haloStack, TAG_CONNECTING_SPARK_POS, connectingSparkPos.asLong());
		saveConnectedNetworkColor(haloStack, connectedNetworkColor);
		saveConnectionUpdateTime(haloStack, updateTime);
	}

	private static void saveConnectedNetworkColor(@NotNull ItemStack haloStack, @NotNull DyeColor connectedNetworkColor) {
		ItemNBTHelper.setInt(haloStack, TAG_CONNECTED_NETWORK_COLOR, connectedNetworkColor.getId());
	}

	private static void saveConnectionUpdateTime(@NotNull ItemStack haloStack, long updateTime) {
		ItemNBTHelper.setLong(haloStack, TAG_CONNECTION_UPDATE_TIME, updateTime);
	}

	private void clearConnection(@NotNull ItemStack haloStack) {
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTED_INDEX_POS);
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTED_NETWORK_COLOR);
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTING_SPARK_POS);
		ItemNBTHelper.removeEntry(haloStack, TAG_CONNECTION_UPDATE_TIME);
	}

	private void saveItemCount(@NotNull ItemStack haloStack, int segment, int count, long gameTime) {
		ItemNBTHelper.setInt(haloStack, TAG_LAST_KNOWN_COUNT_PREFIX + segment, count);
		ItemNBTHelper.setLong(haloStack, TAG_COUNT_UPDATE_TIME_PREFIX + segment, gameTime);
	}

	private static void clearItemCount(@NotNull ItemStack haloStack, int segment) {
		ItemNBTHelper.removeEntry(haloStack, TAG_LAST_KNOWN_COUNT_PREFIX + segment);
		ItemNBTHelper.removeEntry(haloStack, TAG_COUNT_UPDATE_TIME_PREFIX + segment);
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
			return corporeaIndexReference.get();
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
		return seg > 0 && seg < SEGMENTS ? getSegmentItem(haloStack, seg) : ItemStack.EMPTY;
	}

	@Override
	public void inventoryTick(ItemStack haloStack, Level world, Entity entity, int pos, boolean equipped) {

		super.inventoryTick(haloStack, world, entity, pos, equipped);

		if (!(entity instanceof Player player)) {
			return;
		}

		if (!wasEquipped(haloStack)) {
			// still occasionally refresh network connection to keep the icon up-to-date
			if (world.getGameTime() % IDLE_CHECK_INTERVAL == 0) {
				updateConnection(world, player, haloStack);
			} else {
				validateKnownConnection(world, player, haloStack);
			}
			return;
		}

		int checkTick = (int) (world.getGameTime() % EQUIPPED_CHECK_INTERVAL);
		CorporeaSpark connectingSpark = (checkTick == 0)
				? updateConnection(world, player, haloStack)
				: validateKnownConnection(world, player, haloStack);

		if (connectingSpark == null) {
			return;
		}

		int updateSegment = checkTick / 2;
		ItemStack itemTypeToUpdate = ItemStack.EMPTY;
		int nonEmptySlots = 0;
		for (int seg = 0; seg < SEGMENTS; seg++) {
			ItemStack requestItemType = getRequestItemType(player, haloStack, seg);
			if (!requestItemType.isEmpty()) {
				nonEmptySlots++;
				if (seg == updateSegment) {
					itemTypeToUpdate = requestItemType;
				}
			}
		}
		if (nonEmptySlots == 0 || !ManaItemHandler.instance().requestManaExact(haloStack, player,
				(int) (MANA_COST_COUNT_BASE * EQUIPPED_CHECK_INTERVAL * Math.sqrt(nonEmptySlots)), true)) {
			return;
		}

		if (!itemTypeToUpdate.isEmpty()) {
			var matcher = CorporeaHelper.instance().createMatcher(itemTypeToUpdate, true, false);
			var result = CorporeaHelper.instance().requestItem(matcher, -1, connectingSpark, player, false);
			saveItemCount(haloStack, updateSegment, result.matchedCount(), world.getGameTime());
		}
	}

	private CorporeaSpark updateConnection(Level world, LivingEntity living, ItemStack haloStack) {
		final var indexes = CorporeaIndexBlockEntity.getIndexesByDistance(world, living.blockPosition(), MAXIMUM_INDEX_DISTANCE);
		if (indexes.isEmpty()) {
			clearConnection(haloStack);
			return null;
		}

		final BlockPos assumedHaloSparkPos = living.blockPosition().above();
		final var nearbySparks = world.getEntitiesOfClass(CorporeaSparkEntity.class,
				new AABB(assumedHaloSparkPos).inflate(SPARK_SCAN_RANGE),
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
					saveConnection(haloStack, index.getBlockPos(), connectionSpark.getNetwork(), connectionSpark.blockPosition(), world.getGameTime());
					return connectionSpark;
				}
			}
		}

		clearConnection(haloStack);
		return null;
	}

	private static CorporeaSpark validateKnownConnection(Level world, LivingEntity living, ItemStack haloStack) {
		BlockPos indexPos = getConnectedIndexPos(haloStack);
		BlockPos sparkPos = getConnectingSparkPos(haloStack);
		if (indexPos == null || sparkPos == null) {
			return null;
		}

		if (indexPos.distSqr(living.blockPosition()) > MAXIMUM_INDEX_DISTANCE * MAXIMUM_INDEX_DISTANCE) {
			BotaniaAPI.LOGGER.info("Out of index range: " + Math.sqrt(indexPos.distSqr(living.blockPosition())));
			return null;
		}

		var sparkOffset = living.blockPosition().above().subtract(sparkPos);
		if (Math.abs(sparkOffset.getX()) > SPARK_SCAN_RANGE
				|| Math.abs(sparkOffset.getY()) > SPARK_SCAN_RANGE
				|| Math.abs(sparkOffset.getZ()) > SPARK_SCAN_RANGE) {
			BotaniaAPI.LOGGER.info("Out of connecting spark range: " + sparkOffset);
			return null;
		}

		CorporeaSpark connectingSpark = CorporeaHelper.INSTANCE.getSparkForBlock(world, sparkPos.below());
		if (connectingSpark == null || connectingSpark.getConnections() == null) {
			BotaniaAPI.LOGGER.info("No connecting spark: " + connectingSpark);
			return null;
		}

		var expectedCorporeaIndex = world.getBlockEntity(indexPos, BotaniaBlockEntities.CORPOREA_INDEX);
		var indexSpark = expectedCorporeaIndex.map(CorporeaIndexBlockEntity::getSpark);
		if (indexSpark.isEmpty() || !connectingSpark.getConnections().contains(indexSpark.get())) {
			BotaniaAPI.LOGGER.info("No index spark: " + indexSpark);
			return null;
		}

		if (!world.isClientSide()) {
			if (getConnectedNetworkColor(haloStack) != connectingSpark.getNetwork()) {
				// in case the connected network somehow changed color (e.g. while this was not in the player's inventory)
				saveConnectedNetworkColor(haloStack, connectingSpark.getNetwork());
			}
			saveConnectionUpdateTime(haloStack, world.getGameTime());
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
