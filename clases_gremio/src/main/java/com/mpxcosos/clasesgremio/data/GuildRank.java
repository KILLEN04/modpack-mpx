package com.mpxcosos.clasesgremio.data;

import org.jetbrains.annotations.Nullable;

/** Rangos del gremio, de F (inicial) a S (máximo). */
public enum GuildRank {
    F("F", 0x9E9E9E),
    E("E", 0x8BC34A),
    D("D", 0x4FC3F7),
    C("C", 0x3F51B5),
    B("B", 0xAB47BC),
    A("A", 0xFF7043),
    S("S", 0xFFD54F);

    private final String letter;
    private final int color;

    GuildRank(String letter, int color) {
        this.letter = letter;
        this.color = color;
    }

    public String letter() {
        return letter;
    }

    public int color() {
        return color;
    }

    /** Etapa de armadura (0-3): F-E, D-C, B-A, S. */
    public int armorStage() {
        return switch (this) {
            case F, E -> 0;
            case D, C -> 1;
            case B, A -> 2;
            case S -> 3;
        };
    }

    public static GuildRank byIndex(int index) {
        GuildRank[] values = values();
        return values[Math.max(0, Math.min(values.length - 1, index))];
    }

    @Nullable
    public static GuildRank byLetter(String letter) {
        for (GuildRank r : values()) {
            if (r.letter.equalsIgnoreCase(letter)) return r;
        }
        return null;
    }
}
