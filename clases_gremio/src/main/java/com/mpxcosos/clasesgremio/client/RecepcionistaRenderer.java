package com.mpxcosos.clasesgremio.client;

import com.mpxcosos.clasesgremio.ClasesGremio;
import com.mpxcosos.clasesgremio.entity.RecepcionistaEntity;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

/**
 * Usa los archivos:
 * assets/clases_gremio/geo/entity/recepcionista.geo.json
 * assets/clases_gremio/animations/entity/recepcionista.animation.json
 * assets/clases_gremio/textures/entity/recepcionista.png
 * El hueso "head" gira para mirar al jugador.
 */
public class RecepcionistaRenderer extends GeoEntityRenderer<RecepcionistaEntity> {
    public RecepcionistaRenderer(EntityRendererProvider.Context context) {
        super(context, new DefaultedEntityGeoModel<>(ClasesGremio.id("recepcionista"), true));
        this.shadowRadius = 0.4f;
    }
}
