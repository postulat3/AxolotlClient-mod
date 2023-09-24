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

package io.github.axolotlclient.modules.hud.gui.entry;

import java.util.List;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.gui.layout.Justification;
import io.github.axolotlclient.modules.hud.util.DefaultOptions;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import net.minecraft.client.gui.GuiGraphics;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public abstract class SimpleTextHudEntry extends TextHudEntry implements DynamicallyPositionable {

	protected final EnumOption justification = new EnumOption("justification", Justification.values(),
		Justification.CENTER.toString());
	protected final EnumOption anchor = DefaultOptions.getAnchorPoint();

	private final IntegerOption minWidth;

	public SimpleTextHudEntry() {
		this(53, 13, true);
	}

	protected SimpleTextHudEntry(int width, int height, boolean backgroundAllowed) {
		super(width, height, backgroundAllowed);
		minWidth = new IntegerOption("minwidth", width, 1, 300);
	}

	protected SimpleTextHudEntry(int width, int height) {
		this(width, height, true);
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		RenderSystem.enableBlend();
		RenderSystem.blendFuncSeparate(GlStateManager.SourceFactor.SRC_ALPHA,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SourceFactor.ONE,
			GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
		DrawPosition pos = getPos();
		String value = getValue();

		int valueWidth = client.textRenderer.getWidth(value);
		int elementWidth = valueWidth + 4;

		int min = minWidth.get();
		if (elementWidth < min) {
			if (width != min) {
				setWidth(min);
				onBoundsUpdate();
			}
		} else if (elementWidth != width) {
			setWidth(elementWidth);
			onBoundsUpdate();
		}
		drawString(graphics, value,
			pos.x() + Justification.valueOf(justification.get()).getXOffset(valueWidth, getWidth() - 4) + 2,
			pos.y() + (Math.round((float) getHeight() / 2)) - 4, getTextColor().getAsInt(), shadow.get());
		RenderSystem.disableBlend();
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		DrawPosition pos = getPos();
		String value = getPlaceholder();
		drawString(graphics, value,
			pos.x() + Justification.valueOf(justification.get()).getXOffset(value, getWidth() - 4) + 2,
			pos.y() + (Math.round((float) getHeight() / 2)) - 4, textColor.get().getAsInt(), shadow.get());
	}

	public abstract String getPlaceholder();

	public abstract String getValue();

	public Color getTextColor() {
		return textColor.get();
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(justification);
		options.add(anchor);
		options.add(minWidth);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		return AnchorPoint.valueOf(anchor.get());
	}
}
