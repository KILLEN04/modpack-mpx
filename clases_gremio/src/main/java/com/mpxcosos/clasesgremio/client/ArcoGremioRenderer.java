package com.mpxcosos.clasesgremio.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.mpxcosos.clasesgremio.item.ArcoGremioItem;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoItemRenderer;

/**
 * Usa assets/clases_gremio/geo/item/<id>.geo.json y textures/item/<id>.png.
 * El modelo está de perfil (plano YZ); aquí se gira para quedar en diagonal como el arco vanilla,
 * así sirven las mismas posiciones de mano que el arco normal.
 */
public class ArcoGremioRenderer extends GeoItemRenderer<ArcoGremioItem> {
    /** Escalas por vista: iguales para los 4 arcos, así se nota que crecen con la etapa. */
    private static final float FIRST_PERSON_SCALE = 0.4f;
    private static final float THIRD_PERSON_SCALE = 0.6f;
    private static final float OTHER_SCALE = 0.55f;
    private final float guiScale;

    public ArcoGremioRenderer(ArcoGremioItem item) {
        super(item);
        this.guiScale = 24f / item.lengthPx();
    }

    @Override
    public void preRender(PoseStack poseStack, ArcoGremioItem animatable, BakedGeoModel model,
                          @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer,
                          boolean isReRender, float partialTick, int packedLight, int packedOverlay, int colour) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, colour);
        if (!isReRender) {
            float s = switch (this.renderPerspective) {
                case GUI -> guiScale;
                case FIRST_PERSON_RIGHT_HAND, FIRST_PERSON_LEFT_HAND -> FIRST_PERSON_SCALE;
                case THIRD_PERSON_RIGHT_HAND, THIRD_PERSON_LEFT_HAND -> THIRD_PERSON_SCALE;
                default -> OTHER_SCALE;
            };
            poseStack.scale(s, s, s);
            // diagonal como el arco vanilla: pala de arriba hacia arriba-derecha, dorso hacia arriba-izquierda
            poseStack.mulPose(Axis.ZP.rotationDegrees(-45));
            poseStack.mulPose(Axis.YP.rotationDegrees(90));
        }
    }
}
