/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin.client;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import vazkii.patchouli.client.book.BookEntry;

@Mixin(BookEntry.class)
public interface BookEntryAccessor {
	@Accessor("name")
	String botania_getName();
}
