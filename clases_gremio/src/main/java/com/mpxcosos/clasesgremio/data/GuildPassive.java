package com.mpxcosos.clasesgremio.data;

import com.mpxcosos.clasesgremio.ClasesGremio;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.ArrayList;
import java.util.List;

/** Pasivas de clase, siempre activas desde su etapa. */
public enum GuildPassive {
    VIDA(GuildClass.GUERRERO, 0, "vida"),
    FIRMEZA(GuildClass.GUERRERO, 1, "firmeza"),
    ESCUDO(GuildClass.GUERRERO, 2, "escudo"),
    ALIENTO(GuildClass.GUERRERO, 3, "aliento");

    private final GuildClass guildClass;
    private final int stage;
    private final String id;

    GuildPassive(GuildClass guildClass, int stage, String id) {
        this.guildClass = guildClass;
        this.stage = stage;
        this.id = id;
    }

    public GuildClass guildClass() { return guildClass; }
    public int stage() { return stage; }
    public ResourceLocation icon() { return ClasesGremio.id("textures/gui/habilidad/" + id + ".png"); }
    public Component displayName() { return Component.translatable("pasiva.clases_gremio." + id); }
    public Component description() { return Component.translatable("pasiva.clases_gremio." + id + ".desc"); }

    public static List<GuildPassive> forClass(GuildClass c) {
        List<GuildPassive> out = new ArrayList<>();
        for (GuildPassive p : values()) if (p.guildClass == c) out.add(p);
        return out;
    }
}
