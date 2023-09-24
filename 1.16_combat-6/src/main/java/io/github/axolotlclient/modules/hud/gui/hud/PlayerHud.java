/*
 * Copyright © 2021-2023 moehreag <moehreag@gmail.com> & Contributors
 *
 * This file is part of AxolotlClient.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 3 of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 *
 * For more information, see the LICENSE file.
 */

package io.github.axolotlclient.modules.hud.gui.hud;

import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.DoubleOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.entry.BoxHudEntry;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.PlayerDirectionChangeEvent;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.DiffuseLighting;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderDispatcher;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3f;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Quaternion;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PlayerHud extends BoxHudEntry {

	public static final Identifier ID = new Identifier("kronhud", "playerhud");
	@Getter
	private static boolean currentlyRendering;
	private final DoubleOption rotation = new DoubleOption("rotation", 0, 0, 360);
	private final BooleanOption dynamicRotation = new BooleanOption("dynamicrotation", true);
	private final BooleanOption autoHide = new BooleanOption("autoHide", false);
	private float lastYawOffset = 0;
	private float yawOffset = 0;
	private float lastYOffset = 0;
	private float yOffset = 0;
	private long hide;

	public PlayerHud() {
		super(62, 94, true);
		Events.PLAYER_DIRECTION_CHANGE.register(this::onPlayerDirectionChange);
	}

	public void onPlayerDirectionChange(PlayerDirectionChangeEvent event) {
		yawOffset += (event.getYaw() - event.getPrevYaw()) / 2;
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		lastYawOffset = yawOffset;
		yawOffset *= .93f;
		lastYOffset = yOffset;
		if (client.player != null && client.player.isInSwimmingPose()) {
			float rawPitch = client.player.isTouchingWater() ? -90.0F - client.player.getPitch(0) : -90.0F;
			float pitch = MathHelper.lerp(client.player.getLeaningPitch(1), 0.0F, rawPitch);
			float height = client.player.getHeight();
			// sin = opposite / hypotenuse
			float offset = (float) (Math.sin(Math.toRadians(pitch)) * height);
			yOffset = Math.abs(offset) + 35;
		} else if (client.player != null && client.player.isFallFlying()) {
			// Elytra!

			float j = (float) client.player.getRoll() + 1;
			float k = MathHelper.clamp(j * j / 100.0F, 0.0F, 1.0F);

			float pitch = k * (-90.0F - client.player.getPitch(0)) + 90;
			float height = client.player.getHeight();
			// sin = opposite / hypotenuse
			float offset = (float) (Math.sin(Math.toRadians(pitch)) * height) * 50;
			yOffset = 35 - offset;
			if (pitch < 0) {
				yOffset -= ((1 / (1 + Math.exp(-pitch / 4))) - .5) * 20;
			}
		} else {
			yOffset *= .8;
		}
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(dynamicRotation);
		options.add(rotation);
		options.add(autoHide);
		return options;
	}

	@Override
	public void renderComponent(MatrixStack matrices, float delta) {
		renderPlayer(false, getTruePos().x() + 31 * getScale(), getTruePos().y() + 86 * getScale(), delta);
	}

	@Override
	public void renderPlaceholderComponent(MatrixStack matrices, float delta) {
		renderPlayer(true, getTruePos().x() + 31 * getScale(), getTruePos().y() + 86 * getScale(), 0); // If delta was delta, it would start jittering
	}

	@Override
	public boolean movable() {
		return true;
	}

	public void renderPlayer(boolean placeholder, double x, double y, float delta) {
		if (client.player == null) {
			return;
		}

		if (!placeholder && autoHide.get()) {
			if (isPerformingAction()) {
				hide = -1;
			} else if (hide == -1) {
				hide = System.currentTimeMillis();
			}

			if (hide != -1 && System.currentTimeMillis() - hide > 500) {
				return;
			}
		}

		float lerpY = (lastYOffset + ((yOffset - lastYOffset) * delta));

		RenderSystem.pushMatrix();
		RenderSystem.translated(x, y - lerpY, 1050);
		RenderSystem.scalef(1, 1, -1);

		MatrixStack nextStack = new MatrixStack();
		nextStack.translate(0, 0, 1000);
		float scale = getScale() * 40;
		nextStack.scale(scale, scale, scale);

		Quaternion quaternion = Vector3f.POSITIVE_Z.getDegreesQuaternion(180.0F);

		nextStack.multiply(quaternion);
		// Rotate to whatever is wanted. Also make sure to offset the yaw
		float deltaYaw = client.player.getYaw(delta);
		if (dynamicRotation.get()) {
			deltaYaw -= (lastYawOffset + ((yawOffset - lastYawOffset) * delta));
		}
		nextStack.multiply(new Quaternion(new Vector3f(0, 1, 0), deltaYaw - 180 + rotation.get().floatValue(), true));

		// Save these to set them back later
		float pastYaw = client.player.getYaw(0);
		float pastPrevYaw = client.player.prevYaw;

		DiffuseLighting.disable();
		EntityRenderDispatcher renderer = client.getEntityRenderDispatcher();
		renderer.setRotation(quaternion);
		renderer.setRenderShadows(false);

		VertexConsumerProvider.Immediate immediate = MinecraftClient.getInstance().getBufferBuilders()
			.getEntityVertexConsumers();

		currentlyRendering = true;
		renderer.render(client.player, 0, 0, 0, 0, delta, nextStack, immediate, 15728880);
		immediate.draw();
		currentlyRendering = false;
		renderer.setRenderShadows(true);
		RenderSystem.popMatrix();

		client.player.setYaw(pastYaw);
		client.player.prevYaw = pastPrevYaw;

		DiffuseLighting.enableGuiDepthLighting();
	}

	private boolean isPerformingAction() {
		// inspired by tr7zw's mod
		ClientPlayerEntity player = client.player;
		return player.isSneaking() || player.isSprinting() || player.isFallFlying() || player.abilities.flying
			|| player.isSubmergedInWater() || player.isInSwimmingPose() || player.hasVehicle()
			|| player.isUsingItem() || player.handSwinging || player.hurtTime > 0 || player.isOnFire();
	}
}
