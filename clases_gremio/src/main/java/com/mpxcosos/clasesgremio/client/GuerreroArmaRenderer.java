package com.mpxcosos.clasesgremio.client;

import com.mpxcosos.clasesgremio.item.GuerreroArmaItem;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

/** Arma del Guerrero: geo/item/<id>.geo.json; si tiene <id>_glowmask.png esa parte brilla. */
public class GuerreroArmaRenderer extends GeoItemRenderer<GuerreroArmaItem> {
    public GuerreroArmaRenderer(GuerreroArmaItem item) {
        super(item);
        if (item.hasGlow()) addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }
}
