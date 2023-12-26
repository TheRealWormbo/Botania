/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.common.impl.corporea;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.corporea.CorporeaRequestMatcher;
import vazkii.botania.common.helper.ItemNBTHelper;

public class CorporeaItemStackMatcher implements CorporeaRequestMatcher {
	private static final String TAG_REQUEST_STACK = "requestStack";
	private static final String TAG_REQUEST_CHECK_NBT = "requestCheckNBT";
	private static final String TAG_REQUEST_ALLOW_REPLAY = "requestAllowReplay";

	private final ItemStack match;
	private final boolean checkNBT;
	private final boolean allowReplay;

	public CorporeaItemStackMatcher(ItemStack match, boolean checkNBT, boolean allowReplay) {
		this.match = match;
		this.checkNBT = checkNBT;
		this.allowReplay = allowReplay;
	}

	@Override
	public boolean test(ItemStack stack) {
		return !stack.isEmpty() && !match.isEmpty() && ItemStack.isSameItem(stack, match) && (!checkNBT || ItemNBTHelper.matchTagAndManaFullness(stack, match));
	}

	public static CorporeaItemStackMatcher createFromNBT(CompoundTag tag) {
		return new CorporeaItemStackMatcher(ItemStack.of(tag.getCompound(TAG_REQUEST_STACK)), tag.getBoolean(TAG_REQUEST_CHECK_NBT),
				!tag.contains(TAG_REQUEST_ALLOW_REPLAY) || tag.getBoolean(TAG_REQUEST_ALLOW_REPLAY));
	}

	@Override
	public void writeToNBT(CompoundTag tag) {
		CompoundTag cmp = match.save(new CompoundTag());
		tag.put(TAG_REQUEST_STACK, cmp);
		tag.putBoolean(TAG_REQUEST_CHECK_NBT, checkNBT);
		tag.putBoolean(TAG_REQUEST_ALLOW_REPLAY, allowReplay);
	}

	@Override
	public Component getRequestName() {
		return match.getDisplayName();
	}

	@Override
	public boolean canBeReplayed() {
		return allowReplay;
	}
}
