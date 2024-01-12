/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.block.block_entity.corporea;

import it.unimi.dsi.fastutil.doubles.DoubleObjectPair;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.commons.lang3.mutable.MutableObject;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import vazkii.botania.api.corporea.*;
import vazkii.botania.common.BotaniaStats;
import vazkii.botania.common.advancements.CorporeaRequestTrigger;
import vazkii.botania.common.block.block_entity.BotaniaBlockEntities;
import vazkii.botania.common.helper.MathHelper;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ReificationHaloItem;
import vazkii.botania.network.serverbound.IndexStringRequestPacket;
import vazkii.botania.xplat.ClientXplatAbstractions;
import vazkii.botania.xplat.XplatAbstractions;

import java.util.*;
import java.util.function.Supplier;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CorporeaIndexBlockEntity extends BaseCorporeaBlockEntity implements CorporeaRequestor {
	public static final double RADIUS = 2.5;
	public static final int MAX_REQUEST = 1 << 16;

	private static final Set<CorporeaIndexBlockEntity> serverIndexes = Collections.newSetFromMap(new WeakHashMap<>());
	private static final Set<CorporeaIndexBlockEntity> clientIndexes = Collections.newSetFromMap(new WeakHashMap<>());

	private static final Map<Pattern, IRegexStacker> patterns = new LinkedHashMap<>();

	/**
	 * (name) = Item name, or "this" for the name of the item in your hand
	 * (n), (n1), (n2), etc = Numbers
	 * [text] = Optional
	 * <a/b> = Either a or b
	 */
	static {
		// (name) = 1
		addPattern("(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 1;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a][n] (name) = 1
		addPattern("a??n?? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 1;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		//(n)[x][ of] (name) = n
		addPattern("(\\d+)x?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// [a ]stack[ of] (name) = 64
		addPattern("(?:a )?stack(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// (n)[x] stack[s][ of] (name) = n * 64
		addPattern("(\\d+)x?? stacks?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64 * i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// [a ]stack <and/+> (n)[x][ of] (name) = 64 + n
		addPattern("(?:a )?stack (?:(?:and)|(?:\\+)) (\\d+)(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64 + i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// (n1)[x] stack[s] <and/+> (n2)[x][ of] (name) = n1 * 64 + n2
		addPattern("(\\d+)x?? stacks? (?:(?:and)|(?:\\+)) (\\d+)x?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 64 * i(m, 1) + i(m, 2);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(3);
			}
		});

		// [a ]half [of ][a ]stack[ of] (name) = 32
		addPattern("(?:a )?half (?:of )?(?:a )?stack(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 32;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a ]quarter [of ][a ]stack[ of] (name) = 16
		addPattern("(?:a )?quarter (?:of )?(?:a )?stack(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 16;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a ]dozen[ of] (name) = 12
		addPattern("(?:a )?dozen(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 12;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// (n)[x] dozen[s][ of] (name) = n * 12
		addPattern("(\\d+)x?? dozens?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 12 * i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// <all/every> [<of/the> ](name) = 2147483647
		addPattern("(?:all|every) (?:(?:of|the) )?(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return MAX_REQUEST;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [the ]answer to life[,] the universe and everything [of ](name) = 42
		addPattern("(?:the )?answer to life,? the universe and everything (?:of )?(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 42;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// [a ]nice [of ](name) = 69 
		addPattern("(?:a )?nice (?:of )?(.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 69;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});

		// (n)[x] nice[s][ of] (name) = n * 69
		addPattern("(\\d+)x?? nices?(?: of)? (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 69 * i(m, 1);
			}

			@Override
			public String getName(Matcher m) {
				return m.group(2);
			}
		});

		// <count/show/display/tell> (name) = 0 (display only)
		addPattern("(?:count|show|display|tell) (.+)", new IRegexStacker() {
			@Override
			public int getCount(Matcher m) {
				return 0;
			}

			@Override
			public String getName(Matcher m) {
				return m.group(1);
			}
		});
	}

	public int ticksWithCloseby = 0;
	public float closeby = 0F;
	public boolean hasCloseby;

	public CorporeaIndexBlockEntity(BlockPos pos, BlockState state) {
		super(BotaniaBlockEntities.CORPOREA_INDEX, pos, state);
	}

	public static void commonTick(Level level, BlockPos worldPosition, BlockState state, CorporeaIndexBlockEntity self) {
		double x = worldPosition.getX() + 0.5;
		double y = worldPosition.getY() + 0.5;
		double z = worldPosition.getZ() + 0.5;

		List<Player> players = level.getEntitiesOfClass(Player.class, new AABB(x - RADIUS, y - RADIUS, z - RADIUS, x + RADIUS, y + RADIUS, z + RADIUS));
		self.hasCloseby = false;
		if (self.getSpark() != null) {
			for (Player player : players) {
				if (self.isInRange(player)) {
					self.hasCloseby = true;
					break;
				}
			}
		}

		float step = 0.2F;
		if (self.hasCloseby) {
			self.ticksWithCloseby++;
			if (self.closeby < 1F) {
				self.closeby += step;
			}
		} else if (self.closeby > 0F) {
			self.closeby -= step;
		}

		if (!self.isRemoved()) {
			addIndex(self);
		}
	}

	public static List<CorporeaIndexBlockEntity> getNearbyValidIndexes(Player player) {
		List<CorporeaIndexBlockEntity> result = new ArrayList<>();
		for (var index : (player.level().isClientSide ? clientIndexes : serverIndexes)) {
			if (index.getSpark() != null && index.isInRange(player)) {
				result.add(index);
			}
		}
		return result;
	}

	@Override
	public void setRemoved() {
		super.setRemoved();
		removeIndex(this);
	}

	@Override
	public void doCorporeaRequest(CorporeaRequestMatcher request, int count, CorporeaSpark spark, @Nullable LivingEntity entity) {
		doRequest(request, count, spark, entity);
	}

	private CorporeaResult doRequest(CorporeaRequestMatcher matcher, int count, CorporeaSpark spark, @Nullable LivingEntity entity) {
		CorporeaResult result = CorporeaHelper.instance().requestItem(matcher, count, spark, entity, true);
		List<ItemStack> stacks = result.stacks();
		spark.onItemsRequested(stacks);
		for (ItemStack stack : stacks) {
			if (!stack.isEmpty()) {
				ItemEntity item = new ItemEntity(level, worldPosition.getX() + 0.5, worldPosition.getY() + 1.5, worldPosition.getZ() + 0.5, stack);
				level.addFreshEntity(item);
			}
		}
		notifyHeldCorporeaHaloItem(entity, result);
		return result;
	}

	private void notifyHeldCorporeaHaloItem(@Nullable LivingEntity living, CorporeaResult result) {
		if (living == null || result.stacks().isEmpty()) {
			return;
		}
		ItemStack requestedStack = result.stacks().get(0);
		notifyHeldCorporeaItem(living.getMainHandItem(), requestedStack);
		notifyHeldCorporeaItem(living.getOffhandItem(), requestedStack);
	}

	private static void notifyHeldCorporeaItem(ItemStack haloStack, ItemStack requestedStack) {
		if (haloStack.is(BotaniaItems.corporeaHalo)) {
			ReificationHaloItem.saveLastRequested(haloStack, requestedStack);
		}
	}

	private boolean isInRange(Player player) {
		return player.level().dimension() == level.dimension()
				&& MathHelper.pointDistancePlane(getBlockPos().getX() + 0.5, getBlockPos().getZ() + 0.5, player.getX(), player.getZ()) < RADIUS
				&& Math.abs(getBlockPos().getY() + 0.5 - player.getY()) < 5;
	}

	public static void addPattern(String pattern, IRegexStacker stacker) {
		patterns.put(Pattern.compile(pattern), stacker);
	}

	public static int i(Matcher m, int g) {
		try {
			int i = Math.abs(Integer.parseInt(m.group(g)));
			return i;
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private static void addIndex(CorporeaIndexBlockEntity index) {
		Set<CorporeaIndexBlockEntity> set = index.level.isClientSide ? clientIndexes : serverIndexes;
		set.add(index);
	}

	private static void removeIndex(CorporeaIndexBlockEntity index) {
		Set<CorporeaIndexBlockEntity> set = index.level.isClientSide ? clientIndexes : serverIndexes;
		set.remove(index);
	}

	public static void clearIndexCache() {
		clientIndexes.clear();
		serverIndexes.clear();
	}

	@NotNull
	public static List<CorporeaIndexBlockEntity> getIndexesByDistance(@NotNull Level level, @NotNull BlockPos referencePos, double maximumDistance) {
		final double maxDistSq = maximumDistance * maximumDistance;
		final List<DoubleObjectPair<CorporeaIndexBlockEntity>> distancesList = new ArrayList<>();
		for (var serverIndex : serverIndexes) {
			if (serverIndex.level != level) {
				continue;
			}
			var distanceSq = serverIndex.worldPosition.distSqr(referencePos);
			if (distanceSq <= maxDistSq) {
				distancesList.add(DoubleObjectPair.of(distanceSq, serverIndex));
			}
		}

		distancesList.sort(Comparator.comparingDouble(DoubleObjectPair::firstDouble));

		final List<CorporeaIndexBlockEntity> resultList = new ArrayList<>(distancesList.size());
		for (final var distanceEntry : distancesList) {
			resultList.add(distanceEntry.value());
		}
		return resultList;
	}

	public void performPlayerRequest(ServerPlayer player, CorporeaRequestMatcher request, int count) {
		if (!XplatAbstractions.INSTANCE.fireCorporeaIndexRequestEvent(player, request, count, this.getSpark())) {
			CorporeaResult res = this.doRequest(request, count, this.getSpark(), player);
			sendRequestResult(player, request, count, this.getBlockPos(), res);
		}
	}

	public static void sendRequestResult(ServerPlayer player, CorporeaRequestMatcher request, int count, BlockPos requestPos, CorporeaResult result) {
		player.sendSystemMessage(Component.translatable("botaniamisc.requestMsg", count, request.getRequestName(), result.matchedCount(), result.extractedCount()).withStyle(ChatFormatting.LIGHT_PURPLE));
		player.awardStat(BotaniaStats.CORPOREA_ITEMS_REQUESTED, result.extractedCount());
		CorporeaRequestTrigger.INSTANCE.trigger(player, player.serverLevel(), requestPos, result.extractedCount());
	}

	public static class ClientHandler {
		public static boolean onChat(Player player, String message) {
			if (ReificationHaloItem.isAccessingCorporeaIndex(player) || !getNearbyValidIndexes(player).isEmpty()) {
				ClientXplatAbstractions.INSTANCE.sendToServer(new IndexStringRequestPacket(message));
				return true;
			}
			return false;
		}
	}

	public static void onChatMessage(ServerPlayer player, String message) {
		if (player.isSpectator()) {
			return;
		}

		List<CorporeaIndexBlockEntity> nearbyIndexes = getNearbyValidIndexes(player);
		ItemStack mainHandStack = player.getMainHandItem();
		ItemStack offHandStack = player.getOffhandItem();
		if (!nearbyIndexes.isEmpty()) {
			MutableObject<String> name = new MutableObject<>("");
			MutableInt count = new MutableInt();
			parseMessage(player::getMainHandItem, message, count, name);

			for (CorporeaIndexBlockEntity index : nearbyIndexes) {
				index.performPlayerRequest(player, CorporeaHelper.instance().createMatcher(name.getValue()), count.intValue());
			}
		} else {
			boolean alreadyRequested = checkRequestWithHaloItem(player, message, mainHandStack, player::getOffhandItem,
					true);
			checkRequestWithHaloItem(player, message, offHandStack, player::getMainHandItem, !alreadyRequested);
		}
	}

	private static boolean checkRequestWithHaloItem(ServerPlayer player, String message, ItemStack haloStack,
			Supplier<ItemStack> heldItemSupplier, boolean performRequest) {
		if (haloStack.is(BotaniaItems.corporeaHalo)) {
			MutableObject<String> name = new MutableObject<>("");
			MutableInt count = new MutableInt();
			parseMessage(heldItemSupplier, message, count, name);
			var matcher = CorporeaHelper.instance().createMatcher(name.getValue(), false);
			if (performRequest) {
				return ReificationHaloItem.doRequest(player.level(), player, haloStack, matcher, count.intValue());
			}
		}
		return false;
	}

	private static void parseMessage(Supplier<ItemStack> heldItemSupplier, String message, MutableInt count, MutableObject<String> name) {
		String msg = message.toLowerCase(Locale.ROOT).trim();
		for (Pattern pattern : patterns.keySet()) {
			Matcher matcher = pattern.matcher(msg);
			if (matcher.matches()) {
				IRegexStacker stacker = patterns.get(pattern);
				count.setValue(Math.min(MAX_REQUEST, stacker.getCount(matcher)));
				name.setValue(stacker.getName(matcher).toLowerCase(Locale.ROOT).trim());
			}
		}

		if (name.getValue().equals("this")) {
			ItemStack stack = heldItemSupplier.get();
			if (!stack.isEmpty()) {
				name.setValue(stack.getHoverName().getString().toLowerCase(Locale.ROOT).trim());
			}
		}
	}

	public interface IRegexStacker {
		int getCount(Matcher m);

		String getName(Matcher m);

	}

}
