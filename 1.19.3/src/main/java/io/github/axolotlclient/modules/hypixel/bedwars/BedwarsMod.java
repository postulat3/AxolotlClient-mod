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

package io.github.axolotlclient.modules.hypixel.bedwars;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.regex.Pattern;

import io.github.axolotlclient.AxolotlClientConfig.options.BooleanOption;
import io.github.axolotlclient.AxolotlClientConfig.options.EnumOption;
import io.github.axolotlclient.AxolotlClientConfig.options.OptionCategory;
import io.github.axolotlclient.modules.hypixel.AbstractHypixelMod;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import io.github.axolotlclient.util.events.impl.ScoreboardRenderEvent;
import io.github.axolotlclient.util.events.impl.WorldLoadEvent;
import lombok.Getter;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.scoreboard.ScoreboardPlayerScore;
import net.minecraft.scoreboard.Team;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

/**
 * @author DarkKronicle
 */

public class BedwarsMod implements AbstractHypixelMod {

	private final static Pattern[] GAME_START = {
		Pattern.compile("^\\s*?Protect your bed and destroy the enemy beds\\.\\s*?$")
	};

	@Getter
	private static BedwarsMod instance = new BedwarsMod();

	@Getter
	private final OptionCategory category = new OptionCategory("bedwars");

	private final BooleanOption enabled = new BooleanOption("enabled", false);

	public final BooleanOption hardcoreHearts = new BooleanOption(getTranslationKey("hardcoreHearts"), true);

	public final BooleanOption showHunger = new BooleanOption(getTranslationKey("showHunger"), false);

	public final BooleanOption displayArmor = new BooleanOption(getTranslationKey("displayArmor"), true);

	public final BooleanOption bedwarsLevelHead = new BooleanOption(getTranslationKey("bedwarsLevelHead"), true);
	public final EnumOption bedwarsLevelHeadMode = new EnumOption(getTranslationKey("bedwarsLevelHeadMode"),
		BedwarsLevelHeadMode.values(),
		BedwarsLevelHeadMode.GAME_KILLS_GAME_DEATHS.toString());


	protected BedwarsGame currentGame = null;

	@Getter
	protected final TeamUpgradesOverlay upgradesOverlay;


	protected final BooleanOption removeAnnoyingMessages = new BooleanOption(getTranslationKey("removeAnnoyingMessages"), true);


	private final BooleanOption tabRenderLatencyIcon = new BooleanOption(getTranslationKey("tabRenderLatencyIcon"), false);

	private final BooleanOption showChatTime = new BooleanOption(getTranslationKey("showChatTime"), true);

	protected final BooleanOption overrideMessages = new BooleanOption(getTranslationKey("overrideMessages"), true);
	private int targetTick = -1;
	private boolean waiting = false;

	public BedwarsMod() {
		upgradesOverlay = new TeamUpgradesOverlay(this);
	}

	@Override
	public void init() {
		category.add(enabled, hardcoreHearts, showHunger, displayArmor, bedwarsLevelHead, bedwarsLevelHeadMode,
			removeAnnoyingMessages, tabRenderLatencyIcon, showChatTime, overrideMessages);
		category.add(upgradesOverlay.getAllOptions());
		category.add(BedwarsDeathType.getOptions());

		instance = this;

		Events.RECEIVE_CHAT_MESSAGE_EVENT.register(this::onMessage);
		Events.SCOREBOARD_RENDER_EVENT.register(this::onScoreboardRender);
		Events.WORLD_LOAD_EVENT.register(this::onWorldLoad);
	}

	public boolean isEnabled() {
		return enabled.get();
	}

	public void onWorldLoad(WorldLoadEvent event) {
		if (currentGame != null) {
			gameEnd();
		}
	}

	public boolean isWaiting() {
		if (inGame()) {
			waiting = false;
		}
		return waiting;
	}

	public void onMessage(ReceiveChatMessageEvent event) {
		// Remove formatting
		String rawMessage = event.getFormattedMessage().getString();
		if (currentGame != null) {
			currentGame.onChatMessage(rawMessage, event);
			String time = "§7" + currentGame.getFormattedTime() + Formatting.RESET + " ";
			if (!event.isCancelled() && showChatTime.get()) {
				// Add time to every message received in game
				if (event.getNewMessage() != null) {
					event.setNewMessage(Text.literal(time).append(event.getNewMessage()));
				} else {
					event.setNewMessage(Text.literal(time).append(event.getFormattedMessage()));
				}
			}
		} else if (enabled.get() && targetTick < 0 && BedwarsMessages.matched(GAME_START, rawMessage).isPresent()) {
			// Give time for Hypixel to sync
			targetTick = MinecraftClient.getInstance().inGameHud.getTicks() + 10;
		}
	}

	public Optional<BedwarsGame> getGame() {
		return currentGame == null ? Optional.empty() : Optional.of(currentGame);
	}

	@Override
	public boolean tickable() {
		return true;
	}

	@Override
	public void tick() {
		if (currentGame != null) {
			waiting = false;
			if (currentGame.isStarted()) {
				// Trigger setting the header
				currentGame.tick();
				MinecraftClient.getInstance().inGameHud.getPlayerListHud().setHeader(null);
			} else {
				if (checkReady()) {
					currentGame.onStart();
				}
			}
		} else {
			if (targetTick > 0 && MinecraftClient.getInstance().inGameHud.getTicks() > targetTick) {
				currentGame = new BedwarsGame(this);
				targetTick = -1;
			}
		}
	}

	private boolean checkReady() {
		for (PlayerListEntry player : MinecraftClient.getInstance().player.networkHandler.getPlayerList()) {
			String name = MinecraftClient.getInstance().inGameHud.getPlayerListHud().getPlayerName(player).getString().replaceAll("§.", "");
			if (name.charAt(1) == ' ') {
				return true;
			}
		}
		return false;
	}

	public boolean inGame() {
		return currentGame != null && currentGame.isStarted();
	}

	public void onScoreboardRender(ScoreboardRenderEvent event) {
		if (inGame()) {
			waiting = false;
			currentGame.onScoreboardRender(event);
			return;
		}
		if (!Formatting.strip(event.getObjective().getDisplayName().getString()).contains("BED WARS")) {
			return;
		}
		Scoreboard scoreboard = event.getObjective().getScoreboard();
		Collection<ScoreboardPlayerScore> scores = scoreboard.getAllPlayerScores(event.getObjective());
		List<ScoreboardPlayerScore> filteredScores = scores.stream()
			.filter(p_apply_1_ -> p_apply_1_.getPlayerName() != null && !p_apply_1_.getPlayerName().startsWith("#"))
			.toList();
		waiting = filteredScores.stream().anyMatch(score -> {
			Team team = scoreboard.getPlayerTeam(score.getPlayerName());
			String format = Formatting.strip(Team.decorateName(team, Text.literal(score.getPlayerName())).getString()).replaceAll("[^A-z0-9 .:]", "");
			return format.contains("Waiting...") || format.contains("Starting in");
		});
	}

	public void gameEnd() {
		upgradesOverlay.onEnd();
		currentGame = null;
	}

	public boolean blockLatencyIcon() {
		return !tabRenderLatencyIcon.get();
	}

	private String getTranslationKey(String name) {
		return "bedwars." + name;
	}

}
