package com.mpxcosos.clasesgremio.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.data.GuildAbility;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildGate;
import com.mpxcosos.clasesgremio.data.GuildRank;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

/** Barra de habilidades a la derecha de la barra rápida, con recarga y candado por rango. */
public final class HabilidadesHud {
    public static final ResourceLocation ID = ClasesGremio.id("habilidades");
    private static final ResourceLocation MARCO = ClasesGremio.id("textures/gui/habilidad/marco.png");
    private static final ResourceLocation CANDADO = ClasesGremio.id("textures/gui/habilidad/candado.png");

    public static void render(GuiGraphics g, DeltaTracker delta) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.options.hideGui || mc.player == null || mc.player.isSpectator()) return;
        PlayerGuildData data = ClientGuildData.get();
        GuildClass c = GuildClass.byId(data.classId());
        if (c == null) return;
        List<GuildAbility> list = GuildAbility.forClass(c);
        if (list.isEmpty()) return;
        int stage = mc.player.isCreative() ? 3 : GuildRank.byIndex(data.rank()).armorStage();
        float partial = delta.getGameTimeDeltaPartialTick(false);

        int x0 = g.guiWidth() / 2 + 91 + 8;
        int y0 = g.guiHeight() - 23;
        for (GuildAbility a : list) {
            int x = x0 + a.slot() * 24;
            boolean locked = a.slot() > stage;
            g.blit(MARCO, x, y0, 0, 0, 22, 22, 22, 22);
            if (locked) RenderSystem.setShaderColor(0.3f, 0.3f, 0.3f, 1f);
            g.blit(a.icon(), x + 3, y0 + 3, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            if (locked) {
                g.blit(CANDADO, x + 7, y0 + 6, 0, 0, 9, 10, 9, 10);
                g.drawString(mc.font, GuildGate.RANGO_MINIMO[a.slot()].letter(), x + 15, y0 + 13, 0xFFFFD27A, true);
                continue;
            }
            float f = ClientAbilities.cooldownFraction(a.slot(), partial);
            if (f > 0) {
                int h = Math.round(16 * f);
                g.fill(x + 3, y0 + 3 + (16 - h), x + 19, y0 + 19, 0xB0000000);
                String s = String.valueOf(ClientAbilities.cooldownSeconds(a.slot()));
                g.drawString(mc.font, s, x + 11 - mc.font.width(s) / 2, y0 + 7, 0xFFFFFFFF, true);
            }
            String key = ClientAbilities.HABILIDAD[a.slot()].getTranslatedKeyMessage().getString();
            if (key.length() > 2) key = key.substring(0, 2);
            g.drawString(mc.font, key, x + 11 - mc.font.width(key) / 2, y0 - 9, 0xFFFFE6A8, true);
        }
    }

    private HabilidadesHud() {
    }
}
