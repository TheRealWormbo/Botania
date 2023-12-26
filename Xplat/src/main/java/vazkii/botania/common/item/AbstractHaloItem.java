package vazkii.botania.common.item;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import org.joml.Matrix4f;

import vazkii.botania.api.item.HaloRenderer;
import vazkii.botania.client.core.handler.ClientTickHandler;
import vazkii.botania.client.core.helper.RenderHelper;
import vazkii.botania.common.annotations.SoftImplement;
import vazkii.botania.common.helper.ItemNBTHelper;
import vazkii.botania.common.helper.PlayerHelper;
import vazkii.botania.common.helper.VecHelper;
import vazkii.botania.xplat.ClientXplatAbstractions;

public abstract class AbstractHaloItem extends Item {
	public static final int SEGMENTS = 12;
	private static final String TAG_EQUIPPED = "equipped";
	private static final String TAG_ROTATION_BASE = "rotationBase";

	protected AbstractHaloItem(Properties $$0) {
		super($$0);
	}

	@Override
	public void inventoryTick(ItemStack stack, Level world, Entity entity, int pos, boolean equipped) {
		if (!(entity instanceof LivingEntity living)) {
			return;
		}

		boolean eqLastTick = wasEquipped(stack);

		if (!equipped && living.getOffhandItem() == stack) {
			equipped = true;
		}

		if (eqLastTick != equipped) {
			setEquipped(stack, equipped);
		}

		if (!equipped) {
			int angles = 360;
			int segAngles = angles / SEGMENTS;
			float shift = segAngles / 2.0F;
			setRotationBase(stack, getCheckingAngle((LivingEntity) entity) - shift);
		}
	}

	@SoftImplement("IForgeItem")
	public boolean onEntitySwing(ItemStack stack, LivingEntity living) {
		return false;
	}

	protected static int getSegmentLookedAt(ItemStack stack, LivingEntity living) {
		float yaw = AbstractHaloItem.getCheckingAngle(living, AbstractHaloItem.getRotationBase(stack));

		int angles = 360;
		int segAngles = angles / SEGMENTS;
		for (int seg = 0; seg < SEGMENTS; seg++) {
			float calcAngle = (float) seg * segAngles;
			if (yaw >= calcAngle && yaw < calcAngle + segAngles) {
				return seg;
			}
		}
		return -1;
	}

	protected static float getCheckingAngle(LivingEntity living) {
		return AbstractHaloItem.getCheckingAngle(living, 0F);
	}

	// Screw the way minecraft handles rotation
	// Really...
	private static float getCheckingAngle(LivingEntity living, float base) {
		float yaw = Mth.wrapDegrees(living.getYRot()) + 90F;
		int angles = 360;
		int segAngles = angles / SEGMENTS;
		float shift = segAngles / 2;

		if (yaw < 0) {
			yaw = 180F + (180F + yaw);
		}
		yaw -= 360F - base;
		float angle = 360F - yaw + shift;

		if (angle < 0) {
			angle = 360F + angle;
		}

		return angle;
	}

	protected static boolean wasEquipped(ItemStack stack) {
		return ItemNBTHelper.getBoolean(stack, TAG_EQUIPPED, false);
	}

	protected static void setEquipped(ItemStack stack, boolean equipped) {
		ItemNBTHelper.setBoolean(stack, TAG_EQUIPPED, equipped);
	}

	protected static float getRotationBase(ItemStack stack) {
		return ItemNBTHelper.getFloat(stack, TAG_ROTATION_BASE, 0F);
	}

	protected static void setRotationBase(ItemStack stack, float rotation) {
		ItemNBTHelper.setFloat(stack, TAG_ROTATION_BASE, rotation);
	}

	public abstract ResourceLocation getGlowResource(ItemStack stack);

	public abstract ItemStack getDisplayItem(Player player, ItemStack stack, int seg);

	public static class Rendering {
		public static void onRenderWorldLast(Camera camera, float partialTicks, PoseStack ms, RenderBuffers buffers) {
			Player player = Minecraft.getInstance().player;
			ItemStack stack = PlayerHelper.getFirstHeldItemClass(player, AbstractHaloItem.class);
			if (stack.isEmpty()) {
				return;
			}

			MultiBufferSource.BufferSource bufferSource = buffers.bufferSource();

			double renderPosX = camera.getPosition().x();
			double renderPosY = camera.getPosition().y();
			double renderPosZ = camera.getPosition().z();

			ms.pushPose();
			float alpha = ((float) Math.sin((ClientTickHandler.ticksInGame + partialTicks) * 0.2F) * 0.5F + 0.5F) * 0.4F + 0.3F;

			double posX = player.xo + (player.getX() - player.xo) * partialTicks;
			double posY = player.yo + (player.getY() - player.yo) * partialTicks + player.getEyeHeight();
			double posZ = player.zo + (player.getZ() - player.zo) * partialTicks;

			ms.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);

			float base = getRotationBase(stack);
			int angles = 360;
			int segAngles = angles / SEGMENTS;
			float shift = base - segAngles / 2.0F;

			float u = 1F;
			float v = 0.25F;

			float s = 3F;
			float m = 0.8F;
			float y = v * s * 2;
			float y0 = 0;

			int segmentLookedAt = getSegmentLookedAt(stack, player);
			AbstractHaloItem item = (AbstractHaloItem) stack.getItem();
			RenderType layer = RenderHelper.getHaloLayer(item.getGlowResource(stack));

			for (int seg = 0; seg < SEGMENTS; seg++) {
				boolean inside = false;
				float rotationAngle = (seg + 0.5F) * segAngles + shift;
				ms.pushPose();
				ms.mulPose(VecHelper.rotateY(rotationAngle));
				ms.translate(s * m, -0.75F, 0F);

				if (segmentLookedAt == seg) {
					inside = true;
				}

				ItemStack slotStack = item.getDisplayItem(player, stack, seg);
				if (!slotStack.isEmpty()) {
					float scale = seg == 0 ? 0.9F : 0.8F;
					ms.scale(scale, scale, scale);
					ms.mulPose(VecHelper.rotateY(180F));
					ms.translate(seg == 0 ? 0.5F : 0F, seg == 0 ? -0.1F : 0.6F, 0F);

					ms.mulPose(VecHelper.rotateY(90.0F));
					Minecraft.getInstance().getItemRenderer().renderStatic(slotStack, ItemDisplayContext.GUI,
							0xF000F0, OverlayTexture.NO_OVERLAY, ms, bufferSource, player.level(), player.getId());
				}
				ms.popPose();

				ms.pushPose();
				ms.mulPose(VecHelper.rotateX(180));
				float r = 1, g = 1, b = 1, a = alpha;
				if (inside) {
					a += 0.3F;
					y0 = -y;
				}

				if (seg % 2 == 0) {
					r = g = b = 0.6F;
				}

				VertexConsumer buffer = bufferSource.getBuffer(layer);
				for (int i = 0; i < segAngles; i++) {
					Matrix4f mat = ms.last().pose();
					float ang = i + seg * segAngles + shift;
					float xp = (float) Math.cos(ang * Math.PI / 180F) * s;
					float zp = (float) Math.sin(ang * Math.PI / 180F) * s;

					buffer.vertex(mat, xp * m, y, zp * m).color(r, g, b, a).uv(u, v).endVertex();
					buffer.vertex(mat, xp, y0, zp).color(r, g, b, a).uv(u, 0).endVertex();

					xp = (float) Math.cos((ang + 1) * Math.PI / 180F) * s;
					zp = (float) Math.sin((ang + 1) * Math.PI / 180F) * s;

					buffer.vertex(mat, xp, y0, zp).color(r, g, b, a).uv(0, 0).endVertex();
					buffer.vertex(mat, xp * m, y, zp * m).color(r, g, b, a).uv(0, v).endVertex();
				}
				y0 = 0;
				ms.popPose();
			}
			ms.popPose();
			bufferSource.endBatch();
		}

		public static void renderHUD(GuiGraphics gui, Player player, ItemStack stack) {
			HaloRenderer renderer = ClientXplatAbstractions.INSTANCE.findHaloRenderer(stack);
			if (renderer == null) {
				return;
			}

			int slot = getSegmentLookedAt(stack, player);
			if (slot == 0) {
				renderer.renderCentralSegment(gui, player, stack);
			} else {
				renderer.renderSavedSlotSegment(gui, player, stack, slot);
			}
		}
	}
}
