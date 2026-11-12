package com.mpxcosos.clasesgremio.data;

import com.mpxcosos.clasesgremio.ClasesGremio;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Habilidades activas de clase. slot = etapa en la que se desbloquea (0 F-E, 1 D-C, 2 B-A, 3 S). */
public enum GuildAbility {
    CARGA(GuildClass.GUERRERO, 0, 160, "carga"),
    GRITO(GuildClass.GUERRERO, 1, 500, "grito"),
    SISMICO(GuildClass.GUERRERO, 2, 300, "sismico"),
    FURIA(GuildClass.GUERRERO, 3, 1200, "furia");

    private final GuildClass guildClass;
    private final int slot;
    private final int cooldown;
    private final String id;

    GuildAbility(GuildClass guildClass, int slot, int cooldown, String id) {
        this.guildClass = guildClass;
        this.slot = slot;
        this.cooldown = cooldown;
        this.id = id;
    }

    public GuildClass guildClass() { return guildClass; }
    public int slot() { return slot; }
    /** Recarga en ticks. */
    public int cooldown() { return cooldown; }
    public String id() { return id; }
    public ResourceLocation icon() { return ClasesGremio.id("textures/gui/habilidad/" + id + ".png"); }
    public Component displayName() { return Component.translatable("habilidad.clases_gremio." + id); }
    public Component description() { return Component.translatable("habilidad.clases_gremio." + id + ".desc"); }

    public static List<GuildAbility> forClass(GuildClass c) {
        List<GuildAbility> out = new ArrayList<>();
        for (GuildAbility a : values()) if (a.guildClass == c) out.add(a);
        return out;
    }

    public static GuildAbility get(GuildClass c, int slot) {
        for (GuildAbility a : values()) if (a.guildClass == c && a.slot == slot) return a;
        return null;
    }
}
