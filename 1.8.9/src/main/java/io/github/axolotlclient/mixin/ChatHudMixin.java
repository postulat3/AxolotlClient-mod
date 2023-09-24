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

package io.github.axolotlclient.mixin;

import java.util.List;

import io.github.axolotlclient.modules.hud.HudManager;
import io.github.axolotlclient.modules.hypixel.nickhider.NickHider;
import io.github.axolotlclient.util.Util;
import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.ReceiveChatMessageEvent;
import net.minecraft.client.gui.DrawableHelper;
import net.minecraft.client.gui.hud.ChatHud;
import net.minecraft.client.gui.hud.ChatHudLine;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ChatHud.class)
public abstract class ChatHudMixin {

	@Shadow
	@Final
	private List<ChatHudLine> visibleMessages;

	@Inject(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;IIZ)V"), cancellable = true)
	public void axolotlclient$autoGG(Text message, int messageId, CallbackInfo ci) {
		if (message == null) {
			ci.cancel();
		}
	}

	@ModifyVariable(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At("HEAD"), argsOnly = true)
	private Text axolotlclient$onChatMessage(Text message) {
		ReceiveChatMessageEvent event = new ReceiveChatMessageEvent(false, message.asUnformattedString(), message);
		Events.RECEIVE_CHAT_MESSAGE_EVENT.invoker().invoke(event);
		if (event.isCancelled()) {
			return null;
		} else if (event.getNewMessage() != null) {
			return event.getNewMessage();
		}
		return message;
	}

	@ModifyArg(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;info(Ljava/lang/String;)V"), remap = false)
	public String axolotlclient$noNamesInLogIfHidden(String message) {
		return axolotlclient$editChat(new LiteralText(message)).asUnformattedString();
	}

	@ModifyArg(method = "addMessage(Lnet/minecraft/text/Text;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;addMessage(Lnet/minecraft/text/Text;IIZ)V"))
	public Text axolotlclient$editChat(Text message) {
		return NickHider.getInstance().editMessage(message);
	}

	@Redirect(method = "render", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/hud/ChatHud;fill(IIIII)V", ordinal = 0))
	public void axolotlclient$noBg(int x, int y, int x2, int y2, int color) {
		io.github.axolotlclient.modules.hud.gui.hud.ChatHud hud = (io.github.axolotlclient.modules.hud.gui.hud.ChatHud) HudManager
			.getInstance().get(io.github.axolotlclient.modules.hud.gui.hud.ChatHud.ID);
		if (hud.background.get()) {
			DrawableHelper.fill(x, y, x2, y2, color);
		}
	}

	@Inject(method = "render", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$render(int ticks, CallbackInfo ci) {
		io.github.axolotlclient.modules.hud.gui.hud.ChatHud hud = (io.github.axolotlclient.modules.hud.gui.hud.ChatHud) HudManager
			.getInstance().get(io.github.axolotlclient.modules.hud.gui.hud.ChatHud.ID);
		if (hud.isEnabled()) {
			hud.ticks = ticks;
			ci.cancel();
		}
	}

	@Inject(method = "getTextAt", at = @At("HEAD"), cancellable = true)
	public void axolotlclient$getTextAt(int x, int y, CallbackInfoReturnable<Text> cir) {
		io.github.axolotlclient.modules.hud.gui.hud.ChatHud hud = (io.github.axolotlclient.modules.hud.gui.hud.ChatHud) HudManager
			.getInstance().get(io.github.axolotlclient.modules.hud.gui.hud.ChatHud.ID);
		if (hud != null && hud.isEnabled()) {
			cir.setReturnValue(hud.getTextAt(Util.toMCCoordsX(x), Util.toMCCoordsY(y)));
		}
	}

	@ModifyConstant(method = "addMessage(Lnet/minecraft/text/Text;IIZ)V", constant = @Constant(intValue = 100), expect = 2)
	public int axolotlclient$moreChatHistory(int constant) {
		io.github.axolotlclient.modules.hud.gui.hud.ChatHud hud = (io.github.axolotlclient.modules.hud.gui.hud.ChatHud) HudManager
			.getInstance().get(io.github.axolotlclient.modules.hud.gui.hud.ChatHud.ID);
		int length = hud.chatHistory.get();

		if (length == hud.chatHistory.getMax()) {
			return visibleMessages.size() + 1;
		}
		return length;
	}
}
