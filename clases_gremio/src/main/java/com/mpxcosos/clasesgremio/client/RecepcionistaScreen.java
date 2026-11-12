package com.mpxcosos.clasesgremio.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.data.GuildClass;
import com.mpxcosos.clasesgremio.data.GuildRank;
import com.mpxcosos.clasesgremio.data.PlayerGuildData;
import com.mpxcosos.clasesgremio.network.ChooseClassPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

import java.util.List;

/**
 * Menú de la recepcionista:
 * DIALOG (texto que se escribe solo) -> CARDS (5 cartas de clase) -> CONFIRM -> WAITING -> DIALOG -> FICHA.
 * Si el jugador ya tiene clase: DIALOG (saludo) -> FICHA (clase y rango).
 */
public class RecepcionistaScreen extends Screen {
    private enum Phase { DIALOG, CARDS, CONFIRM, WAITING, FICHA }

    // Colores ARGB
    private static final int GOLD = 0xFFE2B548;
    private static final int GOLD_LIGHT = 0xFFFFE9A0;
    private static final int GOLD_DARK = 0xFF7A5A16;
    private static final int PANEL_TOP = 0xF21C2446;
    private static final int PANEL_BOTTOM = 0xF20D1124;
    private static final int TEXT = 0xFFF2EAD3;
    private static final int TEXT_DIM = 0xFFB8AF98;
    private static final int INK = 0xFF3B2412;
    private static final int INK_SOFT = 0xFF7A5634;

    private static final ResourceLocation FANTASIA = ClasesGremio.id("fantasia");
    private static final ResourceLocation PAPIRO = ClasesGremio.id("textures/gui/papiro.png");
    private static final int PAPIRO_W = 240, PAPIRO_H = 176;
    private static final ResourceLocation SELLO = ClasesGremio.id("textures/gui/sello.png");
    private static final int SELLO_W = 44, SELLO_H = 58;

    private static final float CHARS_PER_TICK = 1.6f;
    private static final GuildClass[] CLASSES = GuildClass.values();

    private final int entityId;

    private Phase phase = Phase.DIALOG;
    private Phase afterDialog = Phase.CARDS;
    private List<Component> lines = List.of();
    private int lineIndex;
    private float revealed;

    private GuildClass selected;
    private int hoveredCard = -1;
    private int ticks;
    private int waitTicks;

    public RecepcionistaScreen(int entityId) {
        super(Component.translatable("gui.clases_gremio.titulo"));
        this.entityId = entityId;

        PlayerGuildData data = ClientGuildData.get();
        GuildClass current = data.guildClass();
        if (current != null) {
            prepareDialog(List.of(Component.translatable("gui.clases_gremio.regreso", playerName(),
                    current.displayName())), Phase.FICHA);
        } else {
            prepareDialog(List.of(
                    Component.translatable("gui.clases_gremio.intro.1", playerName()),
                    Component.translatable("gui.clases_gremio.intro.2"),
                    Component.translatable("gui.clases_gremio.intro.3")), Phase.CARDS);
        }
    }

    // ------------------------------------------------------------------ estado

    private void prepareDialog(List<Component> newLines, Phase next) {
        this.phase = Phase.DIALOG;
        this.afterDialog = next;
        this.lines = newLines;
        this.lineIndex = 0;
        this.revealed = 0;
    }

    private void setPhase(Phase newPhase) {
        this.phase = newPhase;
        setupButtons();
    }

    private Component playerName() {
        return net.minecraft.client.Minecraft.getInstance().player != null
                ? net.minecraft.client.Minecraft.getInstance().player.getName()
                : Component.literal("aventurero");
    }

    private String currentLine() {
        return lines.isEmpty() ? "" : lines.get(lineIndex).getString();
    }

    private boolean lineFinished() {
        return revealed >= currentLine().length();
    }

    private void advanceDialog() {
        if (!lineFinished()) {
            revealed = currentLine().length();
        } else if (lineIndex < lines.size() - 1) {
            lineIndex++;
            revealed = 0;
        } else {
            setPhase(afterDialog);
        }
        playClick(1.6f);
    }

    private void confirmChoice() {
        if (selected == null) return;
        PacketDistributor.sendToServer(new ChooseClassPayload(entityId, selected.id()));
        waitTicks = 0;
        setPhase(Phase.WAITING);
    }

    private void playClick(float pitch) {
        if (minecraft != null) {
            minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK.value(), pitch, 0.4f));
        }
    }

    // ------------------------------------------------------------------ layout

    private int dialogX() { return 16; }
    private int dialogW() { return width - 32; }
    private int dialogH() { return 66; }
    private int dialogY() { return height - dialogH() - 10; }

    private int cardW() { return Math.min(96, (width - 40 - 4 * 8) / 5); }
    private int cardH() { return Math.min(150, height - 80); }
    private int cardGap() { return 8; }
    private int cardsX() { return (width - (5 * cardW() + 4 * cardGap())) / 2; }
    private int cardsY() { return (height - cardH()) / 2 + 10; }

    // ------------------------------------------------------------------ widgets

    @Override
    protected void init() {
        setupButtons();
    }

    private void setupButtons() {
        clearWidgets();
        switch (phase) {
            case DIALOG -> addRenderableWidget(Button.builder(
                            Component.translatable("gui.clases_gremio.siguiente"), b -> advanceDialog())
                    .bounds(dialogX() + dialogW() - 82, dialogY() + dialogH() - 24, 74, 18).build());
            case CONFIRM -> {
                int bx = width / 2 - 10;
                int by = height / 2 + 34;
                addRenderableWidget(Button.builder(Component.translatable("gui.clases_gremio.confirmar"),
                        b -> confirmChoice()).bounds(bx, by, 90, 20).build());
                addRenderableWidget(Button.builder(Component.translatable("gui.clases_gremio.volver"), b -> {
                    selected = null;
                    setPhase(Phase.CARDS);
                }).bounds(bx + 96, by, 70, 20).build());
            }
            case FICHA -> addRenderableWidget(Button.builder(Component.translatable("gui.clases_gremio.cerrar"),
                    b -> onClose()).bounds(width / 2 - 40, fichaY() + PAPIRO_H + 4, 80, 20).build());
            default -> {
            }
        }
    }

    // ------------------------------------------------------------------ input

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (super.mouseClicked(mouseX, mouseY, button)) return true;
        if (phase == Phase.DIALOG) {
            advanceDialog();
            return true;
        }
        if (phase == Phase.CARDS && hoveredCard >= 0) {
            selected = CLASSES[hoveredCard];
            playClick(1.0f);
            setPhase(Phase.CONFIRM);
            return true;
        }
        return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (phase == Phase.DIALOG && (keyCode == GLFW.GLFW_KEY_SPACE || keyCode == GLFW.GLFW_KEY_ENTER)) {
            advanceDialog();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void tick() {
        ticks++;
        if (phase == Phase.DIALOG && !lineFinished()) {
            revealed = Math.min(currentLine().length(), revealed + CHARS_PER_TICK);
        }
        if (phase == Phase.WAITING) {
            waitTicks++;
            GuildClass got = ClientGuildData.get().guildClass();
            if (got != null) {
                prepareDialog(List.of(Component.translatable("gui.clases_gremio.felicitacion", got.displayName())),
                        Phase.FICHA);
                setupButtons();
            } else if (waitTicks > 100) {
                onClose();
            }
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    // ------------------------------------------------------------------ dibujo

    @Override
    public void renderBackground(GuiGraphics g, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(g, mouseX, mouseY, partialTick);
        switch (phase) {
            case DIALOG -> renderDialog(g, mouseX, mouseY);
            case CARDS -> renderCards(g, mouseX, mouseY);
            case CONFIRM -> renderConfirm(g);
            case WAITING -> g.drawCenteredString(font, Component.translatable("gui.clases_gremio.esperando"),
                    width / 2, height / 2, TEXT);
            case FICHA -> renderFicha(g);
        }
    }

    private void renderDialog(GuiGraphics g, int mouseX, int mouseY) {
        int x = dialogX(), y = dialogY(), w = dialogW(), h = dialogH();

        // Retrato de la recepcionista (sigue al ratón)
        int px1 = x + 8, px2 = x + 98, py2 = y - 4, py1 = Math.max(4, py2 - 120);
        drawPanel(g, px1, py1, px2 - px1, py2 - py1);
        renderPortrait(g, px1 + 3, py1 + 3, px2 - 3, py2 - 3, mouseX, mouseY);

        drawPanel(g, x, y, w, h);

        // Placa con el nombre
        Component name = Component.translatable("gui.clases_gremio.nombre_npc");
        int nameW = font.width(name) + 16;
        drawPanel(g, px2 + 6, y - 16, nameW, 16);
        g.drawString(font, name, px2 + 14, y - 12, GOLD_LIGHT, true);

        String full = currentLine();
        String shown = full.substring(0, Math.min(full.length(), (int) revealed));
        List<FormattedCharSequence> wrapped = font.split(Component.literal(shown), w - 110);
        int ty = y + 10;
        for (int i = 0; i < wrapped.size() && i < 4; i++) {
            g.drawString(font, wrapped.get(i), x + 12, ty + i * 11, TEXT, false);
        }

        // Contador de líneas
        String counter = (lineIndex + 1) + "/" + lines.size();
        g.drawString(font, counter, x + w - 82 - font.width(counter) - 6, y + h - 19, TEXT_DIM, false);
    }

    private void renderPortrait(GuiGraphics g, int x1, int y1, int x2, int y2, int mouseX, int mouseY) {
        if (minecraft == null || minecraft.level == null) return;
        Entity entity = minecraft.level.getEntity(entityId);
        if (!(entity instanceof LivingEntity living)) return;
        boolean nameVisible = living.isCustomNameVisible();
        living.setCustomNameVisible(false);
        InventoryScreen.renderEntityInInventoryFollowsMouse(g, x1, y1, x2, y2, 52, 0.0625f,
                mouseX, mouseY, living);
        living.setCustomNameVisible(nameVisible);
    }

    private void renderCards(GuiGraphics g, int mouseX, int mouseY) {
        int cw = cardW(), ch = cardH(), gap = cardGap(), x0 = cardsX(), y0 = cardsY();

        drawTitle(g, Component.translatable("gui.clases_gremio.elige"), y0 - 30);
        g.drawCenteredString(font, Component.translatable("gui.clases_gremio.elige.sub"), width / 2, y0 - 14, TEXT_DIM);

        hoveredCard = -1;
        for (int i = 0; i < CLASSES.length; i++) {
            int cx = x0 + i * (cw + gap);
            if (mouseX >= cx && mouseX < cx + cw && mouseY >= y0 - 4 && mouseY < y0 + ch) {
                hoveredCard = i;
            }
        }
        for (int i = 0; i < CLASSES.length; i++) {
            int cx = x0 + i * (cw + gap);
            int lift = i == hoveredCard ? -4 : 0;
            drawClassCard(g, CLASSES[i], cx, y0 + lift, cw, ch, i == hoveredCard);
        }
    }

    private void drawClassCard(GuiGraphics g, GuildClass c, int x, int y, int w, int h, boolean highlight) {
        int col = c.color();
        g.fillGradient(x, y, x + w, y + h, argb(scale(col, 0.42f), 0xF4), argb(scale(col, 0.12f), 0xF4));
        g.fill(x, y, x + w, y + 3, argb(col, 0xFF));
        drawBorder(g, x, y, w, h, highlight ? GOLD_LIGHT : GOLD_DARK);
        drawBorder(g, x + 2, y + 2, w - 4, h - 4, argb(scale(col, 0.7f), 0xFF));
        drawRivets(g, x, y, w, h, highlight ? GOLD_LIGHT : GOLD);

        // Icono
        g.pose().pushPose();
        g.pose().translate(x + w / 2f - 16, y + 10, 0);
        g.pose().scale(2.0f, 2.0f, 1.0f);
        g.renderItem(iconFor(c), 0, 0);
        g.pose().popPose();

        // Nombre
        g.drawCenteredString(font, c.displayName(), x + w / 2, y + 48, argb(lighten(col, 0.35f), 0xFF));
        g.fill(x + 8, y + 60, x + w - 8, y + 61, highlight ? GOLD : GOLD_DARK);

        // Descripción
        List<FormattedCharSequence> desc = font.split(c.description(), w - 10);
        int maxLines = Math.max(0, (h - 70) / 10);
        for (int i = 0; i < desc.size() && i < maxLines; i++) {
            g.drawString(font, desc.get(i), x + 5, y + 66 + i * 10, TEXT, false);
        }
    }

    private void renderConfirm(GuiGraphics g) {
        if (selected == null) return;
        int cardX = width / 2 - 130;
        int cardY = height / 2 - 75;
        drawClassCard(g, selected, cardX, cardY, 100, 150, true);

        int px = width / 2 - 14, py = height / 2 - 70, pw = 182, ph = 98;
        drawPanel(g, px, py, pw, ph);
        Component question = Component.translatable("gui.clases_gremio.confirmar.pregunta", selected.displayName());
        int y = py + 10;
        for (FormattedCharSequence line : font.split(question, pw - 20)) {
            g.drawString(font, line, px + 10, y, GOLD_LIGHT, true);
            y += 11;
        }
        y += 4;
        for (FormattedCharSequence line : font.split(Component.translatable("gui.clases_gremio.confirmar.aviso"), pw - 20)) {
            g.drawString(font, line, px + 10, y, TEXT, false);
            y += 10;
        }
    }

    /** Ficha del gremio dibujada como un papiro con sello de cera del color del rango. */
    private void renderFicha(GuiGraphics g) {
        PlayerGuildData data = ClientGuildData.get();
        GuildClass c = data.guildClass();
        GuildRank r = data.guildRank();
        int x = fichaX(), y = fichaY();

        RenderSystem.enableBlend();
        g.blit(PAPIRO, x, y, 0, 0, PAPIRO_W, PAPIRO_H, PAPIRO_W, PAPIRO_H);

        // Título
        drawFantasy(g, Component.translatable("gui.clases_gremio.ficha"), x + PAPIRO_W / 2, y + 24, 1.6f, INK, true);

        // Datos
        int tx = x + 30;
        drawFantasy(g, Component.translatable("gui.clases_gremio.ficha.nombre"), tx, y + 56, 1.0f, INK_SOFT, false);
        drawFantasy(g, playerName(), tx + 4, y + 67, 1.15f, INK, false);

        drawFantasy(g, Component.translatable("gui.clases_gremio.ficha.clase"), tx, y + 86, 1.0f, INK_SOFT, false);
        Component claseLabel = c == null ? Component.translatable("cmd.clases_gremio.sin_clase") : c.displayName();
        int claseColor = c == null ? INK : argb(scale(c.color(), 0.62f), 0xFF);
        drawFantasy(g, claseLabel, tx + 4, y + 97, 1.3f, claseColor, false);

        drawFantasy(g, Component.translatable("gui.clases_gremio.ficha.rango"), tx, y + 117, 1.0f, INK_SOFT, false);
        Component rankLabel = Component.literal(r.letter() + " · ")
                .append(Component.translatable("rango.clases_gremio." + r.letter().toLowerCase()));
        drawFantasy(g, rankLabel, tx + 4, y + 128, 1.15f, INK, false);

        // Sello de cera teñido con el color del rango
        int sx = x + 168, sy = y + 60;
        float red = ((r.color() >> 16) & 0xFF) / 255f;
        float green = ((r.color() >> 8) & 0xFF) / 255f;
        float blue = (r.color() & 0xFF) / 255f;
        RenderSystem.setShaderColor(red, green, blue, 1.0f);
        g.blit(SELLO, sx, sy, 0, 0, SELLO_W, SELLO_H, SELLO_W, SELLO_H);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        drawFantasy(g, Component.literal(r.letter()), sx + SELLO_W / 2, sy + 12, 2.2f,
                argb(scale(r.color(), 0.35f), 0xFF), true);

        // Firma
        drawFantasy(g, Component.translatable("gui.clases_gremio.ficha.firma"), x + PAPIRO_W - 26
                - (int) (font.width(fantasy(Component.translatable("gui.clases_gremio.ficha.firma"))) * 0.85f),
                y + 145, 0.85f, INK_SOFT, false);
        RenderSystem.disableBlend();
    }

    private int fichaX() { return width / 2 - PAPIRO_W / 2; }
    private int fichaY() { return Math.max(4, height / 2 - PAPIRO_H / 2 - 12); }

    private static Component fantasy(Component text) {
        return text.copy().withStyle(s -> s.withFont(FANTASIA));
    }

    /** Texto con la fuente de fantasía, escalado; si centered, x es el centro. */
    private void drawFantasy(GuiGraphics g, Component text, int x, int y, float scale, int color, boolean centered) {
        Component styled = fantasy(text);
        g.pose().pushPose();
        g.pose().translate(x, y, 0);
        g.pose().scale(scale, scale, 1.0f);
        int dx = centered ? -font.width(styled) / 2 : 0;
        g.drawString(font, styled, dx, 0, color, false);
        g.pose().popPose();
    }

    // ------------------------------------------------------------------ utilidades de dibujo

    private void drawTitle(GuiGraphics g, Component text, int y) {
        g.pose().pushPose();
        g.pose().translate(width / 2f, y, 0);
        g.pose().scale(1.5f, 1.5f, 1.0f);
        g.drawCenteredString(font, text, 0, 0, GOLD_LIGHT);
        g.pose().popPose();
    }

    /** Panel oscuro con doble borde dorado y remaches en las esquinas. */
    private void drawPanel(GuiGraphics g, int x, int y, int w, int h) {
        g.fillGradient(x, y, x + w, y + h, PANEL_TOP, PANEL_BOTTOM);
        drawBorder(g, x, y, w, h, GOLD);
        drawBorder(g, x + 2, y + 2, w - 4, h - 4, GOLD_DARK);
        drawRivets(g, x, y, w, h, GOLD_LIGHT);
    }

    private static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private static void drawRivets(GuiGraphics g, int x, int y, int w, int h, int color) {
        if (w < 12 || h < 12) return;
        int s = 2;
        g.fill(x + 3, y + 3, x + 3 + s, y + 3 + s, color);
        g.fill(x + w - 3 - s, y + 3, x + w - 3, y + 3 + s, color);
        g.fill(x + 3, y + h - 3 - s, x + 3 + s, y + h - 3, color);
        g.fill(x + w - 3 - s, y + h - 3 - s, x + w - 3, y + h - 3, color);
    }

    private static ItemStack iconFor(GuildClass c) {
        return switch (c) {
            case GUERRERO -> new ItemStack(Items.IRON_SWORD);
            case MAGO -> new ItemStack(Items.BLAZE_ROD);
            case ARQUERO -> new ItemStack(Items.BOW);
            case PALADIN -> new ItemStack(Items.GOLDEN_SWORD);
            case NIGROMANTE -> new ItemStack(Items.WITHER_SKELETON_SKULL);
        };
    }

    private static int argb(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0xFFFFFF);
    }

    private static int scale(int rgb, float f) {
        int r = (int) (((rgb >> 16) & 0xFF) * f);
        int gr = (int) (((rgb >> 8) & 0xFF) * f);
        int b = (int) ((rgb & 0xFF) * f);
        return (r << 16) | (gr << 8) | b;
    }

    private static int lighten(int rgb, float f) {
        int r = (rgb >> 16) & 0xFF, gr = (rgb >> 8) & 0xFF, b = rgb & 0xFF;
        r += (int) ((255 - r) * f);
        gr += (int) ((255 - gr) * f);
        b += (int) ((255 - b) * f);
        return (r << 16) | (gr << 8) | b;
    }
}
