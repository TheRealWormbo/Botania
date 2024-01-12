/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */
package vazkii.botania.network.serverbound;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import vazkii.botania.api.corporea.CorporeaHelper;
import vazkii.botania.common.block.block_entity.corporea.CorporeaIndexBlockEntity;
import vazkii.botania.common.item.BotaniaItems;
import vazkii.botania.common.item.ReificationHaloItem;
import vazkii.botania.network.BotaniaPacket;

import static vazkii.botania.common.lib.ResourceLocationHelper.prefix;

public record IndexKeybindRequestPacket(ItemStack stack) implements BotaniaPacket {
	public static final ResourceLocation ID = prefix("idx");

	@Override
	public void encode(FriendlyByteBuf buf) {
		buf.writeItem(stack());
	}

	@Override
	public ResourceLocation getFabricId() {
		return ID;
	}

	public static IndexKeybindRequestPacket decode(FriendlyByteBuf buf) {
		return new IndexKeybindRequestPacket(buf.readItem());
	}

	public void handle(MinecraftServer server, ServerPlayer player) {
		var stack = this.stack();
		server.execute(() -> {
			if (player.isSpectator()) {
				return;
			}

			var nearbyValidIndexes = CorporeaIndexBlockEntity.getNearbyValidIndexes(player);
			boolean alreadyRequested = checkRequestWithHaloItem(player, player.getMainHandItem(), stack, nearbyValidIndexes.isEmpty());
			checkRequestWithHaloItem(player, player.getOffhandItem(), stack, !alreadyRequested && nearbyValidIndexes.isEmpty());
			for (CorporeaIndexBlockEntity index : nearbyValidIndexes) {
				index.performPlayerRequest(player, CorporeaHelper.instance().createMatcher(stack, true), stack.getCount());
			}
		});
	}

	private static boolean checkRequestWithHaloItem(ServerPlayer player, ItemStack haloStack, ItemStack requestStack, boolean performRequest) {
		if (!haloStack.is(BotaniaItems.corporeaHalo)) {
			return false;
		}
		ReificationHaloItem.saveLastRequested(haloStack, requestStack);
		if (performRequest) {
			var matcher = CorporeaHelper.instance().createMatcher(requestStack, true, false);
			return ReificationHaloItem.doRequest(player.level(), player, haloStack, matcher, requestStack.getCount());
		}
		return false;
	}
}
