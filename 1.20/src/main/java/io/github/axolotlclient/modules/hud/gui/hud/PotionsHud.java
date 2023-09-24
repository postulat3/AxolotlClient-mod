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

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.gui.layout.CardinalOrder;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.Rectangle;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.texture.Sprite;
import net.minecraft.entity.effect.StatusEffect;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffectUtil;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class PotionsHud extends TextHudEntry implements DynamicallyPositionable {

	public static final Identifier ID = new Identifier("kronhud", "potionshud");

	private final EnumOption anchor = DefaultOptions.getAnchorPoint();

	private final EnumOption order = DefaultOptions.getCardinalOrder(CardinalOrder.TOP_DOWN);

	private final BooleanOption iconsOnly = new BooleanOption("iconsonly", ID.getPath(), false);

	public PotionsHud() {
		super(50, 200, false);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		List<StatusEffectInstance> effects = new ArrayList<>(client.player.getStatusEffects());
		if (effects.isEmpty()) {
			return;
		}
		renderEffects(graphics, effects);
	}

	private void renderEffects(GuiGraphics graphics, List<StatusEffectInstance> effects) {
		int calcWidth = calculateWidth(effects);
		int calcHeight = calculateHeight(effects);
		boolean changed = false;
		if (calcWidth != width) {
			setWidth(calcWidth);
			changed = true;
		}
		if (calcHeight != height) {
			setHeight(calcHeight);
			changed = true;
		}
		if (changed) {
			onBoundsUpdate();
		}
		int lastPos = 0;
		CardinalOrder direction = CardinalOrder.valueOf(order.get());

		Rectangle bounds = getBounds();
		int x = bounds.x();
		int y = bounds.y();
		for (int i = 0; i < effects.size(); i++) {
			StatusEffectInstance effect = effects.get(direction.getDirection() == -1 ? i : effects.size() - i - 1);
			if (direction.isXAxis()) {
				renderPotion(graphics, effect, x + lastPos + 1, y + 1);
				lastPos += (iconsOnly.get() ? 20 : 50);
			} else {
				renderPotion(graphics, effect, x + 1, y + 1 + lastPos);
				lastPos += 20;
			}
		}
	}

	private int calculateWidth(List<StatusEffectInstance> effects) {
		if (CardinalOrder.valueOf(order.get()).isXAxis()) {
			if (iconsOnly.get()) {
				return 20 * effects.size() + 2;
			}
			return 50 * effects.size() + 2;
		} else {
			if (iconsOnly.get()) {
				return 20;
			}
			return 50;
		}
	}

	private int calculateHeight(List<StatusEffectInstance> effects) {
		if (CardinalOrder.valueOf(order.get()).isXAxis()) {
			return 22;
		} else {
			return 20 * effects.size() + 2;
		}
	}

	private void renderPotion(GuiGraphics graphics, StatusEffectInstance effect, int x, int y) {
		StatusEffect type = effect.getEffectType();
		Sprite sprite = client.getStatusEffectSpriteManager().getSprite(type);

		RenderSystem.setShaderTexture(0, sprite.getId());
		RenderSystem.setShaderColor(1, 1, 1, 1);
		graphics.drawSprite(x, y, 0, 18, 18, sprite);
		if (!iconsOnly.get()) {
			drawString(graphics, StatusEffectUtil.durationToString(effect, 1).getString(), x + 19, y + 5,
				textColor.get().getAsInt(), shadow.get());
		}
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		StatusEffectInstance effect = new StatusEffectInstance(StatusEffects.SPEED);
		StatusEffectInstance jump = new StatusEffectInstance(StatusEffects.JUMP_BOOST);
		StatusEffectInstance haste = new StatusEffectInstance(StatusEffects.HASTE);
		renderEffects(graphics, List.of(effect, jump, haste));
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(anchor);
		options.add(order);
		options.add(iconsOnly);
		return options;
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public AnchorPoint getAnchor() {
		return AnchorPoint.valueOf(anchor.get());
	}
}
