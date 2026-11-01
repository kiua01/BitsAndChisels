package io.github.coolmineman.bitsandchisels.client;

import io.github.coolmineman.bitsandchisels.BitsAndChisels;
import io.github.coolmineman.bitsandchisels.critter.CritterMob;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.state.LivingEntityRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;

/** Cube-blob body + head + legs with simple walk bob (placeholder until Blockbench models). */
public final class CritterRenderer extends MobRenderer<CritterMob, LivingEntityRenderState, CritterRenderer.Model> {
    private final Identifier texture;

    public CritterRenderer(EntityRendererProvider.Context ctx, String textureId) {
        super(ctx, new Model(ctx.bakeLayer(Model.LAYER)), 0.4f);
        this.texture = BitsAndChisels.id("textures/entity/" + textureId + ".png");
    }

    @Override
    public LivingEntityRenderState createRenderState() {
        return new LivingEntityRenderState();
    }

    @Override
    public Identifier getTextureLocation(LivingEntityRenderState state) {
        return texture;
    }

    public static final class Model extends EntityModel<LivingEntityRenderState> {
        public static final net.minecraft.client.model.geom.ModelLayerLocation LAYER =
            new net.minecraft.client.model.geom.ModelLayerLocation(BitsAndChisels.id("critter"), "main");

        private final ModelPart body;
        private final ModelPart head;
        private final ModelPart leg0;
        private final ModelPart leg1;

        public Model(ModelPart root) {
            super(root);
            this.body = root.getChild("body");
            this.head = root.getChild("head");
            this.leg0 = root.getChild("leg0");
            this.leg1 = root.getChild("leg1");
        }

        public static LayerDefinition createBodyLayer() {
            MeshDefinition mesh = new MeshDefinition();
            PartDefinition root = mesh.getRoot();
            root.addOrReplaceChild("body",
                CubeListBuilder.create().texOffs(0, 0).addBox(-4, 4, -4, 8, 8, 8),
                PartPose.offset(0, 0, 0));
            root.addOrReplaceChild("head",
                CubeListBuilder.create().texOffs(32, 0).addBox(-3, 0, -3, 6, 6, 6),
                PartPose.offset(0, 12, -2));
            root.addOrReplaceChild("leg0",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0, -1.5f, 3, 4, 3),
                PartPose.offset(-2, 4, 2));
            root.addOrReplaceChild("leg1",
                CubeListBuilder.create().texOffs(0, 16).addBox(-1.5f, 0, -1.5f, 3, 4, 3),
                PartPose.offset(2, 4, 2));
            return LayerDefinition.create(mesh, 64, 32);
        }

        @Override
        public void setupAnim(LivingEntityRenderState state) {
            float w = state.walkAnimationPos;
            float a = state.walkAnimationSpeed;
            leg0.xRot = Mth.cos(w * 0.6662f) * 1.4f * a;
            leg1.xRot = Mth.cos(w * 0.6662f + (float) Math.PI) * 1.4f * a;
            head.yRot = state.yRot * ((float) Math.PI / 180f) * 0.25f;
            head.xRot = state.xRot * ((float) Math.PI / 180f) * 0.25f;
        }
    }
}
