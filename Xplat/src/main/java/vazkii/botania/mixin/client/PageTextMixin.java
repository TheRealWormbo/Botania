/*
 * This class is distributed as part of the Botania Mod.
 * Get the Source Code in github:
 * https://github.com/Vazkii/Botania
 *
 * Botania is Open Source and distributed under the
 * Botania License: http://botaniamod.net/license.php
 */

package vazkii.botania.mixin.client;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;

import net.minecraft.client.resources.language.I18n;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import vazkii.patchouli.client.book.page.PageText;
import vazkii.patchouli.client.book.page.abstr.PageWithText;

@Mixin(PageText.class)
public abstract class PageTextMixin extends PageWithText {

	@ModifyExpressionValue(method = "render", at = @At(value = "CONSTANT", args = "stringValue="))
	private String addTagline(String original) {
		String entryName = ((BookEntryAccessor) (Object) (parent.getEntry())).botania_getName();
		if (entryName != null && entryName.startsWith("botania.entry.")) {
			String taglineKey = "botania.tagline." + entryName.substring(14);
			if (I18n.exists(taglineKey)) {
				return I18n.get(taglineKey);
			}
		}

		return original;
	}
}
