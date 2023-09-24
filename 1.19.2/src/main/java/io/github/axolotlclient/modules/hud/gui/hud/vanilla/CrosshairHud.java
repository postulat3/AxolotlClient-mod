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

package io.github.axolotlclient.modules.hud.gui.hud.vanilla;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.*;
import io.github.axolotlclient.modules.hud.gui.AbstractHudEntry;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import lombok.AllArgsConstructor;
import net.minecraft.block.AbstractChestBlock;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.option.AttackIndicator;
import net.minecraft.client.render.Camera;
import net.minecraft.client.render.GameRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3f;
import net.minecraft.world.World;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CrosshairHud extends AbstractHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "crosshairhud");

	private final EnumOption type = new EnumOption("crosshair_type", Crosshair.values(), Crosshair.CROSS.toString());
	private final BooleanOption showInF5 = new BooleanOption("showInF5", false);
	private final ColorOption defaultColor = new ColorOption("defaultcolor", Color.WHITE);
	private final ColorOption entityColor = new ColorOption("entitycolor", Color.SELECTOR_RED);
	private final ColorOption containerColor = new ColorOption("blockcolor", Color.SELECTOR_BLUE);
	private final ColorOption attackIndicatorBackgroundColor = new ColorOption("attackindicatorbg",
		new Color(0xFF141414));
	private final ColorOption attackIndicatorForegroundColor = new ColorOption("attackindicatorfg", Color.WHITE);
	private final BooleanOption applyBlend = new BooleanOption("applyBlend", true);
	private final BooleanOption overrideF3 = new BooleanOption("overrideF3", false);

	private final GraphicsOption customTextureGraphics = new GraphicsOption("customTextureGraphics",
		new int[][]{
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, -1, -1, -1, -1, -1, -1, -1, -1, -1, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
			new int[]{0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0},
		}, true);

	public CrosshairHud() {
		super(15, 15);
	}

	@Override
	public boolean movable() {
		return false;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(type);
		options.add(customTextureGraphics);
		options.add(showInF5);
		options.add(overrideF3);
		options.add(applyBlend);
		options.add(defaultColor);
		options.add(entityColor);
		options.add(containerColor);
		options.add(attackIndicatorBackgroundColor);
		options.add(attackIndicatorForegroundColor);
		return options;
	}

	@Override
	public boolean overridesF3() {
		return overrideF3.get();
	}

	@Override
	public double getDefaultX() {
		return 0.5;
	}

	@Override
	public double getDefaultY() {
		return 0.5;
	}

	@Override
	public void render(MatrixStack matrices, float delta) {
		if (!client.options.getPerspective().isFirstPerson() && !showInF5.get()) {
			return;
		}

		RenderSystem.setShaderColor(1, 1, 1, 1);

		matrices.push();
		scale(matrices);

		int x = getPos().x;
		int y = getPos().y + 1;
		Color color = getColor();
		AttackIndicator indicator = this.client.options.getAttackIndicator().get();

		RenderSystem.enableBlend();

		// Need to not enable blend while the debug HUD is open because it does weird stuff. Why? no idea.
		if (color == defaultColor.get() && !type.get().equals(Crosshair.DIRECTION.toString()) && applyBlend.get()
			&& !client.options.debugEnabled) {
			RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.ONE_MINUS_DST_COLOR,
				GlStateManager.DestFactor.ONE_MINUS_SRC_COLOR, GlStateManager.SourceFactor.ONE,
				GlStateManager.DestFactor.ZERO);
		} else {
			RenderSystem.disableBlend();
		}

		if (type.get().equals(Crosshair.DOT.toString())) {
			fillRect(matrices, x + (getWidth() / 2) - 2, y + (getHeight() / 2) - 2, 3, 3, color.getAsInt());
		} else if (type.get().equals(Crosshair.CROSS.toString())) {
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 6, y + (getHeight() / 2) - 1, 6, 1, color);
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2), y + (getHeight() / 2) - 1, 5, 1, color);
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 1, y + (getHeight() / 2) - 6, 1, 5, color);
			RenderUtil.fillBlend(matrices, x + (getWidth() / 2) - 1, y + (getHeight() / 2), 1, 5, color);
		} else if (type.get().equals(Crosshair.DIRECTION.toString())) {
			Camera camera = this.client.gameRenderer.getCamera();
			MatrixStack matrixStack = RenderSystem.getModelViewStack();
			matrixStack.push();
			matrixStack.translate(client.getWindow().getScaledWidth() / 2F, client.getWindow().getScaledHeight() / 2F,
				0);
			matrixStack.multiply(Vec3f.NEGATIVE_X.getDegreesQuaternion(camera.getPitch()));
			matrixStack.multiply(Vec3f.POSITIVE_Y.getDegreesQuaternion(camera.getYaw()));
			matrixStack.scale(-getScale(), -getScale(), -getScale());
			RenderSystem.applyModelViewMatrix();
			RenderSystem.renderCrosshair(10);
			matrixStack.pop();
			RenderSystem.applyModelViewMatrix();
		} else if (type.get().equals(Crosshair.TEXTURE.toString()) || type.get().equals(Crosshair.CUSTOM.toString())) {
			RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
			if (type.get().equals(Crosshair.TEXTURE.toString())) {
				RenderSystem.setShader(GameRenderer::getPositionTexShader);
				RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
				// Draw crosshair
				RenderSystem.setShaderColor((float) color.getRed() / 255, (float) color.getGreen() / 255,
					(float) color.getBlue() / 255, (float) color.getAlpha() / 255);
				client.inGameHud.drawTexture(matrices,
					(int) (((client.getWindow().getScaledWidth() / getScale()) - 15) / 2),
					(int) (((client.getWindow().getScaledHeight() / getScale()) - 15) / 2), 0, 0, 15, 15);
			} else {
				customTextureGraphics.bindTexture();
				// Draw crosshair
				RenderSystem.setShaderColor((float) color.getRed() / 255, (float) color.getGreen() / 255,
					(float) color.getBlue() / 255, (float) color.getAlpha() / 255);

				drawTexture(matrices,
					(int) (((client.getWindow().getScaledWidth() / getScale()) - 15) / 2),
					(int) (((client.getWindow().getScaledHeight() / getScale()) - 15) / 2), 0, 0, 15, 15, 15, 15);

				RenderSystem.setShaderTexture(0, DrawableHelper.GUI_ICONS_TEXTURE);
			}

			RenderSystem.setShaderColor(1, 1, 1, 1);

			// Draw attack indicator
			if (indicator == AttackIndicator.CROSSHAIR) {
				float progress = this.client.player.getAttackCooldownProgress(0.0F);

				// Whether a cross should be displayed under the indicator
				boolean targetingEntity = false;
				if (this.client.targetedEntity != null && this.client.targetedEntity instanceof LivingEntity
					&& progress >= 1.0F) {
					targetingEntity = this.client.player.getAttackCooldownProgressPerTick() > 5.0F;
					targetingEntity &= this.client.targetedEntity.isAlive();
				}

				x = (int) ((client.getWindow().getScaledWidth() / getScale()) / 2 - 8);
				y = (int) ((client.getWindow().getScaledHeight() / getScale()) / 2 - 7 + 16);

				if (targetingEntity) {
					client.inGameHud.drawTexture(matrices, x, y, 68, 94, 16, 16);
				} else if (progress < 1.0F) {
					int k = (int) (progress * 17.0F);
					client.inGameHud.drawTexture(matrices, x, y, 36, 94, 16, 4);
					client.inGameHud.drawTexture(matrices, x, y, 52, 94, k, 4);
				}
			}
		}
		if (indicator == AttackIndicator.CROSSHAIR && !type.get().equals(Crosshair.TEXTURE.toString())) {
			float progress = this.client.player.getAttackCooldownProgress(0.0F);
			if (progress != 1.0F) {
				RenderUtil.drawRectangle(matrices, getRawX() + (getWidth() / 2) - 6, getRawY() + (getHeight() / 2) + 9,
					11, 1, attackIndicatorBackgroundColor.get());
				RenderUtil.drawRectangle(matrices, getRawX() + (getWidth() / 2) - 6, getRawY() + (getHeight() / 2) + 9,
					(int) (progress * 11), 1, attackIndicatorForegroundColor.get());
			}
		}
		RenderSystem.disableBlend();
		RenderSystem.defaultBlendFunc();
		matrices.pop();
	}

	public Color getColor() {
		HitResult hit = client.crosshairTarget;
		if (hit == null || hit.getType() == null) {
			return defaultColor.get();
		} else if (hit.getType() == HitResult.Type.ENTITY) {
			return entityColor.get();
		} else if (hit.getType() == HitResult.Type.BLOCK) {
			BlockPos blockPos = ((BlockHitResult) hit).getBlockPos();
			World world = this.client.world;
			if (world.getBlockState(blockPos).createScreenHandlerFactory(world, blockPos) != null
				|| world.getBlockState(blockPos).getBlock() instanceof AbstractChestBlock<?>) {
				return containerColor.get();
			}
		}
		return defaultColor.get();
	}

	@Override
	public void renderPlaceholder(MatrixStack matrices, float delta) {
		// Shouldn't need this...
	}

	@Override
	public AnchorPoint getAnchor() {
		return AnchorPoint.MIDDLE_MIDDLE;
	}

	@AllArgsConstructor
	public enum Crosshair {
		CROSS, DOT, DIRECTION, TEXTURE, CUSTOM
	}
}
