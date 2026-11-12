package com.mpxcosos.clasesgremio.data;

import net.minecraft.network.chat.Component;
import org.jetbrains.annotations.Nullable;

/** Las 5 clases del gremio. El id se guarda en los datos del jugador. */
public enum GuildClass {
    GUERRERO("guerrero", 0xC0392B),
    MAGO("mago", 0xE67E22),
    ARQUERO("arquero", 0x27AE60),
    PALADIN("paladin", 0xF1C40F),
    NIGROMANTE("nigromante", 0x8E44AD);

    private final String id;
    private final int color;

    GuildClass(String id, int color) {
        this.id = id;
        this.color = color;
    }

    public String id() {
        return id;
    }

    /** Color RGB sin alfa. */
    public int color() {
        return color;
    }

    public Component displayName() {
        return Component.translatable("clase.clases_gremio." + id);
    }

    public Component description() {
        return Component.translatable("clase.clases_gremio." + id + ".desc");
    }

    @Nullable
    public static GuildClass byId(String id) {
        for (GuildClass c : values()) {
            if (c.id.equals(id)) return c;
        }
        return null;
    }
}
