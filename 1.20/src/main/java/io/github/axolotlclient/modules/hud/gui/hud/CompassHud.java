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
import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.options.IntegerOption;
import io.github.axolotlclient.AxolotlClientConfig.options.Option;
import io.github.axolotlclient.modules.hud.gui.component.DynamicallyPositionable;
import io.github.axolotlclient.modules.hud.gui.entry.TextHudEntry;
import io.github.axolotlclient.modules.hud.gui.layout.AnchorPoint;
import io.github.axolotlclient.modules.hud.util.DrawPosition;
import io.github.axolotlclient.modules.hud.util.DrawUtil;
import io.github.axolotlclient.modules.hud.util.RenderUtil;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Identifier;

/**
 * This implementation of Hud modules is based on KronHUD.
 * <a href="https://github.com/DarkKronicle/KronHUD">Github Link.</a>
 *
 * @license GPL-3.0
 */

public class CompassHud extends TextHudEntry implements DynamicallyPositionable {

	public final Identifier ID = new Identifier("kronhud", "compasshud");

	private final IntegerOption widthOption = new IntegerOption("width", this::updateWidth, width, 100, 800);

	private final ColorOption lookingBox = new ColorOption("lookingbox", new Color(0x80000000));
	private final ColorOption degreesColor = new ColorOption("degreescolor", new Color(-1));
	private final ColorOption majorIndicatorColor = new ColorOption("majorindicator", new Color(-1));
	private final ColorOption minorIndicatorColor = new ColorOption("minorindicator", new Color(0xCCFFFFFF));
	private final ColorOption cardinalColor = new ColorOption("cardinalcolor", Color.WHITE);
	private final ColorOption semiCardinalColor = new ColorOption("semicardinalcolor", new Color(0xFFAAAAAA));
	private final BooleanOption invert = new BooleanOption("invert_direction", false);
	private final BooleanOption showDegrees = new BooleanOption("showdegrees", true);

	public CompassHud() {
		super(240, 33, false);
	}

	private void updateWidth(int newWidth) {
		setWidth(newWidth);
		onBoundsUpdate();
	}

	@Override
	public Identifier getId() {
		return ID;
	}

	@Override
	public void renderComponent(GuiGraphics graphics, float delta) {
		renderCompass(graphics, delta);
	}

	@Override
	public void renderPlaceholderComponent(GuiGraphics graphics, float delta) {
		renderCompass(graphics, delta);
	}

	public void renderCompass(GuiGraphics graphics, float delta) {
		// N = 0
		// E = 90
		// S = 180
		// W = 270
		if (client.player == null) {
			return;
		}
		float halfWidth = width / 2f;
		float degrees = (client.player.getYaw(delta) + 180) % 360;
		if (degrees < -180) {
			degrees += 360;
		}
		float start = degrees - 150 + 360;
		//        float end = degrees + 150 + 360;
		int startIndicator = ((int) (start + 8) / 15) * 15;
		int amount = 21;
		//        int endIndicator = startIndicator + 15 * amount;
		int dist = width / (amount);
		DrawPosition pos = getPos();
		int x = pos.x();
		int y = pos.y() + 1;
		RenderSystem.setShaderColor(1, 1, 1, 1);
		graphics.fill(pos.x() + (int) halfWidth - 1, pos.y() + 1, pos.x() + (int) halfWidth - 1 + 3, pos.y() + 10, lookingBox.get().getAsInt());
		if (showDegrees.get()) {
			DrawUtil.drawCenteredString(graphics, client.textRenderer, String.valueOf((int) degrees),
				x + (int) halfWidth, y + 20, degreesColor.get(), shadow.get());
		}
		float shift = (startIndicator - start) / 15f * dist;
		if (invert.get()) {
			shift = dist - shift;
		}
		graphics.getMatrices().translate(shift, 0, 0);
		for (int i = 0; i < amount; i++) {
			int d;
			if (invert.get()) {
				d = (startIndicator + ((amount - i - 2) * 15)) % 360;
			} else {
				d = (startIndicator + i * 15) % 360;
			}
			int innerX = x + dist * (i + 1);
			Indicator indicator = getIndicator(d);

			float trueDist;
			if (invert.get()) {
				trueDist = ((amount - i) * dist) - shift;
			} else {
				trueDist = ((i + 1) * dist) - shift;
			}

			float targetOpacity = 1 - Math.abs((halfWidth - trueDist)) / halfWidth;
			RenderSystem.setShaderColor(1, 1, 1, targetOpacity);
			if (indicator == Indicator.CARDINAL) {
				// We have to call .color() here so that transparency stays
				RenderUtil.drawRectangle(graphics, innerX, y, 1, 9, majorIndicatorColor.get().getAsInt());
				Color color = cardinalColor.get();
				color = color.withAlpha((int) (color.getAlpha() * targetOpacity));
				if (color.getAlpha() > 0) {
					DrawUtil.drawCenteredString(graphics, client.textRenderer, getCardString(indicator, d), innerX + 1,
						y + 10, color, shadow.get());
				}
			} else if (indicator == Indicator.SEMI_CARDINAL) {
				Color color = semiCardinalColor.get();
				color = color.withAlpha((int) (color.getAlpha() * targetOpacity));
				if (color.getAlpha() > 0) {
					DrawUtil.drawCenteredString(graphics, client.textRenderer, getCardString(indicator, d), innerX + 1,
						y + 1, color, shadow.get());
				}
			} else {
				// We have to call .color() here so that transparency stays
				RenderUtil.drawRectangle(graphics, innerX, y, 1, 5, minorIndicatorColor.get().getAsInt());
			}
		}
		RenderSystem.setShaderColor(1, 1, 1, 1);
		graphics.getMatrices().translate(-shift, 0, 0);
	}

	private static Indicator getIndicator(int degrees) {
		if (degrees % 90 == 0) {
			return Indicator.CARDINAL;
		}
		if (degrees % 45 == 0) {
			return Indicator.SEMI_CARDINAL;
		}
		return Indicator.SMALL;
	}

	private static String getCardString(Indicator indicator, int degrees) {
		if (indicator == Indicator.CARDINAL) {
			return switch (degrees) {
				case 0 -> "N";
				case 90 -> "E";
				case 180 -> "S";
				case 270 -> "W";
				default -> "NaD";
			};
		}
		return switch (degrees) {
			case 45 -> "NE";
			case 135 -> "SE";
			case 225 -> "SW";
			case 315 -> "NW";
			default -> "NaD";
		};
	}

	@Override
	public List<Option<?>> getConfigurationOptions() {
		List<Option<?>> options = super.getConfigurationOptions();
		options.add(widthOption);
		options.add(showDegrees);
		options.add(invert);
		options.add(lookingBox);
		options.add(degreesColor);
		options.add(cardinalColor);
		options.add(semiCardinalColor);
		options.add(majorIndicatorColor);
		options.add(minorIndicatorColor);
		return options;
	}

	@Override
	public AnchorPoint getAnchor() {
		// Won't be dynamically set
		return AnchorPoint.TOP_MIDDLE;
	}

	private enum Indicator {
		CARDINAL, SEMI_CARDINAL, SMALL

	}
}
