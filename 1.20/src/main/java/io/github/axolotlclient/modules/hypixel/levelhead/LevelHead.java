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

package io.github.axolotlclient.modules.hypixel.levelhead;

import io.github.axolotlclient.AxolotlClientConfig.Color;
import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.ColorOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;

public class LevelHead implements AbstractHypixelMod {

	private static final LevelHead Instance = new LevelHead();

	private final OptionCategory category = new OptionCategory("levelhead");
	public BooleanOption enabled = new BooleanOption("enabled", false);
	public BooleanOption background = new BooleanOption("background", false);
	public ColorOption textColor = new ColorOption("textColor", Color.GOLD);
	public EnumOption mode = new EnumOption("levelHeadMode", LevelHeadMode.values(), LevelHeadMode.NETWORK.toString());

	public static LevelHead getInstance() {
		return Instance;
	}

	@Override
	public void init() {
		category.add(enabled);
		category.add(textColor);
		category.add(background);
		category.add(mode);
	}

	@Override
	public OptionCategory getCategory() {
		return category;
	}
}
