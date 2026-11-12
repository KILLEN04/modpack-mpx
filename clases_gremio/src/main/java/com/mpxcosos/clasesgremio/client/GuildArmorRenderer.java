package com.mpxcosos.clasesgremio.client;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.item.GuildArmorItem;
import software.bernie.geckolib.model.DefaultedItemGeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

/**
 * Renderizador de armaduras del gremio.
 * Usa geo/item/armor/<set>.geo.json y textures/item/armor/<set>.png.
 * Los huesos extra (capa, faldones, hombreras, cuernos) cuelgan de los huesos armorX,
 * así que aparecen solo con la pieza correspondiente puesta.
 */
public class GuildArmorRenderer extends GeoArmorRenderer<GuildArmorItem> {
    public GuildArmorRenderer(String setId) {
        super(new DefaultedItemGeoModel<>(ClasesGremio.id("armor/" + setId)));
    }
}
