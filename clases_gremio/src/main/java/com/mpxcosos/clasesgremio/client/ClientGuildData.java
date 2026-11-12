package com.mpxcosos.clasesgremio.client;

import com.mpxcosos.clasesgremio.data.PlayerGuildData;

/** Copia local (cliente) de los datos del gremio del jugador. */
public final class ClientGuildData {
    private static PlayerGuildData data = PlayerGuildData.EMPTY;

    public static PlayerGuildData get() {
        return data;
    }

    static void set(PlayerGuildData newData) {
        data = newData;
    }

    private ClientGuildData() {
    }
}
