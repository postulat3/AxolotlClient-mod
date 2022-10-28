package io.github.axolotlclient.modules.hud.gui.hud.simple;

import io.github.axolotlclient.modules.hud.gui.entry.SimpleTextHudEntry;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.entity.Entity;
import net.minecraft.util.Identifier;
import net.minecraft.util.Util;

public class ComboHud extends SimpleTextHudEntry {

    public static final Identifier ID = new Identifier("kronhud", "combohud");

    private long lastTime = 0;
    private int target = -1;
    private int count = 0;

    @Override
    public Identifier getId() {
        return ID;
    }

    @Override
    public String getValue() {
        if (count == 0) {
            return I18n.translate("combocounter.no_hits");
        }
        if (lastTime + 2000 < MinecraftClient.getTime()) {
            count = 0;
            return "0 hits";
        }
        if (count == 1) {
            return I18n.translate("combocounter.one_hit");
        }
        return I18n.translate("combocounter.hits", count);
    }

    @Override
    public String getPlaceholder() {
        return I18n.translate("combocounter.hits", 3);
    }

    public void onEntityAttack(Entity attacked) {
        target = attacked.getEntityId();
    }

    public void onEntityDamage(Entity entity) {
        if (client.player == null) {
            return;
        }
        if (entity.getEntityId() == client.player.getEntityId()) {
            target = -1;
            count = 0;
            return;
        }
        if (entity.getEntityId() == target) {
            count++;
            lastTime = MinecraftClient.getTime();
        }
    }

}
