package io.github.axolotlclient.util.events.impl;

import lombok.Data;
import net.minecraft.client.option.KeyBinding;
@Data
public class KeyPressEvent {

	private final KeyBinding key;
}
