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

import io.github.axolotlclient.util.events.Events;
import io.github.axolotlclient.util.events.impl.KeyBindChangeEvent;
import io.github.axolotlclient.util.events.impl.KeyPressEvent;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.util.collection.IntObjectStorage;
import org.lwjgl.input.Keyboard;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(KeyBinding.class)
public abstract class KeyBindingMixin {

	@Shadow
	@Final
	private static IntObjectStorage<KeyBinding> KEY_MAP;
	@Shadow
	private boolean pressed;
	@Shadow
	private int code;

	@Inject(method = "setKeyPressed", at = @At(value = "FIELD", target = "Lnet/minecraft/client/option/KeyBinding;pressed:Z"))
	private static void axolotlclient$onPress(int keyCode, boolean pressed, CallbackInfo ci) {
		if (pressed) {
			Events.KEY_PRESS.invoker().invoke(new KeyPressEvent(KEY_MAP.get(keyCode)));
		}
	}

	@Inject(method = "isPressed", at = @At("HEAD"))
	public void axolotlclient$noMovementFixAfterInventory(CallbackInfoReturnable<Boolean> cir) {
		if (this.code == MinecraftClient.getInstance().options.sneakKey.getCode()
			|| code == MinecraftClient.getInstance().options.forwardKey.getCode()
			|| code == MinecraftClient.getInstance().options.backKey.getCode()
			|| code == MinecraftClient.getInstance().options.rightKey.getCode()
			|| code == MinecraftClient.getInstance().options.leftKey.getCode()
			|| code == MinecraftClient.getInstance().options.jumpKey.getCode()
			|| code == MinecraftClient.getInstance().options.sprintKey.getCode()) {
			this.pressed = Keyboard.isKeyDown(code) && (MinecraftClient.getInstance().currentScreen == null);
		}
	}

	@Inject(method = "setCode", at = @At("RETURN"))
	public void axolotlclient$boundKeySet(int code, CallbackInfo ci) {
		Events.KEYBIND_CHANGE.invoker().invoke(new KeyBindChangeEvent(code));
	}
}
