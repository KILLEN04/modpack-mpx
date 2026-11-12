package com.mpxcosos.clasesgremio.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.data.GuildAbility;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildGate;
import com.mpxcosos.clasesgremio.data.GuildPassive;
import com.mpxcosos.clasesgremio.data.GuildRank;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import com.mpxcosos.clasesgremio.registry.ModArmor;
import com.mpxcosos.clasesgremio.registry.ModWeapons;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

/** Libro de clase: habilidades, pasivas y equipo por etapa. Se abre con la tecla K. */
public class LibroClaseScreen extends Screen {
    private static final ResourceLocation LIBRO = ClasesGremio.id("textures/gui/libro.png");
    private static final ResourceLocation FANTASIA = ClasesGremio.id("fantasia");
    private static final int W = 320, H = 200;
    private static final int INK = 0xFF3B2412, INK_SOFT = 0xFF7A5634, LOCKED = 0xFF9A8A70;

    public LibroClaseScreen() {
        super(Component.translatable("gui.clases_gremio.libro"));
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private Component fantasy(Component c) {
        return c.copy().withStyle(s -> s.withFont(FANTASIA));
    }

    private void text(GuiGraphics g, Component c, int x, int y, float scale, int color, boolean center) {
        Component t = fantasy(c);
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1);
        int dx = center ? -font.width(t) / 2 : 0;
        g.drawString(font, t, dx, 0, color, false);
        g.pose().popPose();
    }

    @Override
    public void render(GuiGraphics g, int mouseX, int mouseY, float partial) {
        super.render(g, mouseX, mouseY, partial);
        int x = (width - W) / 2, y = (height - H) / 2;
        g.blit(LIBRO, x, y, 0, 0, W, H, 512, 256);

        PlayerGuildData data = ClientGuildData.get();
        GuildClass c = GuildClass.byId(data.classId());
        if (c == null) {
            text(g, Component.translatable("gui.clases_gremio.libro.sin_clase"), x + W / 4, y + 90, 1f, INK, true);
            return;
        }
        GuildRank rank = GuildRank.byIndex(data.rank());
        boolean creative = minecraft != null && minecraft.player != null && minecraft.player.isCreative();
        int stage = creative ? 3 : rank.armorStage();
        List<Component> tooltip = null;

        // ---------------- página izquierda: clase y habilidades
        int lx = x + 22, ly = y + 14;
        text(g, c.displayName(), lx, ly, 1.5f, c.color() | 0xFF000000, false);
        text(g, Component.translatable("gui.clases_gremio.libro.rango", rank.letter(),
                Component.translatable("rango.clases_gremio." + rank.letter().toLowerCase())), lx + 2, ly + 21, 0.9f, INK_SOFT, false);
        text(g, Component.translatable("gui.clases_gremio.libro.habilidades"), lx, ly + 34, 1.0f, INK, false);
        int ry = ly + 47;
        for (GuildAbility a : GuildAbility.forClass(c)) {
            boolean locked = a.slot() > stage;
            if (locked) RenderSystem.setShaderColor(0.45f, 0.42f, 0.38f, 1f);
            g.blit(a.icon(), lx, ry, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            String key = ClientAbilities.HABILIDAD[a.slot()].getTranslatedKeyMessage().getString();
            text(g, a.displayName(), lx + 20, ry, 1.0f, locked ? LOCKED : INK, false);
            Component sub = locked
                    ? Component.translatable("gui.clases_gremio.libro.bloqueada", GuildGate.RANGO_MINIMO[a.slot()].letter())
                    : Component.translatable("gui.clases_gremio.libro.tecla_recarga", key, a.cooldown() / 20);
            text(g, sub, lx + 20, ry + 9, 0.8f, locked ? LOCKED : INK_SOFT, false);
            if (mouseX >= lx && mouseX < lx + 130 && mouseY >= ry && mouseY < ry + 18) {
                tooltip = new ArrayList<>();
                tooltip.add(a.displayName().copy().withColor(0xFFD27A));
                for (FormattedCharSequence line : font.split(a.description(), 180)) tooltip.add(toComponent(line));
            }
            ry += 30;
        }

        // ---------------- página derecha: pasivas y equipo
        int px = x + W / 2 + 14, py = y + 14;
        text(g, Component.translatable("gui.clases_gremio.libro.pasivas"), px, py + 2, 1.2f, INK, false);
        int qy = py + 22;
        for (GuildPassive p : GuildPassive.forClass(c)) {
            boolean locked = p.stage() > stage;
            if (locked) RenderSystem.setShaderColor(0.45f, 0.42f, 0.38f, 1f);
            g.blit(p.icon(), px, qy, 0, 0, 16, 16, 16, 16);
            RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            text(g, p.displayName(), px + 20, qy + 1, 0.95f, locked ? LOCKED : INK, false);
            List<FormattedCharSequence> lines = font.split(p.description(), 150);
            if (!lines.isEmpty()) {
                g.pose().pushPose();
                g.pose().translate(px + 20, qy + 10, 0);
                g.pose().scale(0.7f, 0.7f, 1);
                g.drawString(font, lines.get(0), 0, 0, locked ? LOCKED : INK_SOFT, false);
                g.pose().popPose();
            }
            if (mouseX >= px && mouseX < px + 130 && mouseY >= qy && mouseY < qy + 18) {
                tooltip = new ArrayList<>();
                tooltip.add(p.displayName().copy().withColor(0xFFD27A));
                for (FormattedCharSequence line : font.split(p.description(), 180)) tooltip.add(toComponent(line));
                if (locked) tooltip.add(Component.translatable("gui.clases_gremio.libro.bloqueada",
                        GuildGate.RANGO_MINIMO[p.stage()].letter()).withColor(0xFF8A70));
            }
            qy += 24;
        }

        text(g, Component.translatable("gui.clases_gremio.libro.equipo"), px, y + 128, 1.0f, INK, false);
        for (int s = 0; s < 4; s++) {
            int ex = px + s * 32, ey = y + 142;
            GuildRank r = GuildGate.RANGO_MINIMO[s];
            text(g, Component.literal(r.letter()), ex + 12, ey, 1.0f, (s <= stage ? r.color() : 0x9A8A70) | 0xFF000000, true);
            ModArmor.ArmorSet set = ModArmor.get(c, s);
            Item weapon = ModWeapons.get(c, s);
            if (set != null) item(g, new ItemStack(set.chestplate().get()), ex + 4, ey + 10, s <= stage);
            if (weapon != null) item(g, new ItemStack(weapon), ex + 4, ey + 28, s <= stage);
            if (mouseX >= ex && mouseX < ex + 26 && mouseY >= ey + 10 && mouseY < ey + 44) {
                tooltip = new ArrayList<>();
                tooltip.add(Component.translatable("gui.clases_gremio.libro.etapa", r.letter()).withColor(0xFFD27A));
                if (set != null) tooltip.add(new ItemStack(set.chestplate().get()).getHoverName());
                if (weapon != null) tooltip.add(new ItemStack(weapon).getHoverName());
            }
        }

        if (tooltip != null) g.renderComponentTooltip(font, tooltip, mouseX, mouseY);
    }

    private void item(GuiGraphics g, ItemStack stack, int x, int y, boolean unlocked) {
        g.renderItem(stack, x, y);
        if (!unlocked) g.fill(x, y, x + 16, y + 16, 0x88E6D2A8);
    }

    private static Component toComponent(FormattedCharSequence seq) {
        StringBuilder sb = new StringBuilder();
        seq.accept((index, style, codePoint) -> {
            sb.appendCodePoint(codePoint);
            return true;
        });
        return Component.literal(sb.toString());
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (ClientAbilities.LIBRO.matches(keyCode, scanCode)) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }
}
