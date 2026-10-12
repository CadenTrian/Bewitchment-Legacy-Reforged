package com.bewitchment.client;

import com.bewitchment.entity.*;
import com.mojang.blaze3d.matrix.MatrixStack;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.util.ResourceLocation;

public final class CreatureRenderers {
    public static final class Baphomet extends MobRenderer<BaphometEntity,LegacyBaphometModel> {
        public Baphomet(EntityRendererManager manager){super(manager,new LegacyBaphometModel(),.3F);addLayer(new net.minecraft.client.renderer.entity.layers.HeldItemLayer<>(this));}
        @Override public ResourceLocation getTextureLocation(BaphometEntity entity){return new ResourceLocation("bewitchment:textures/entity/baphomet.png");}
        @Override protected void scale(BaphometEntity entity,MatrixStack matrices,float partial){matrices.scale(1.25F,1.25F,1.25F);}
    }
    public static final class Leonard extends MobRenderer<LeonardEntity,LegacyLeonardModel> {
        public Leonard(EntityRendererManager manager){super(manager,new LegacyLeonardModel(),.3F);addLayer(new net.minecraft.client.renderer.entity.layers.HeldItemLayer<>(this));}
        @Override public ResourceLocation getTextureLocation(LeonardEntity entity){return new ResourceLocation("bewitchment:textures/entity/leonard.png");}
        @Override protected void scale(LeonardEntity entity,MatrixStack matrices,float partial){matrices.scale(1.5F,1.5F,1.5F);}
    }
    public static final class Demon extends MobRenderer<DemonEntity,LegacyDemonModel> {
        public Demon(EntityRendererManager manager){super(manager,new LegacyDemonModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(DemonEntity entity){return new ResourceLocation("bewitchment","textures/entity/demon_"+entity.skin()+".png");}
        @Override protected void scale(DemonEntity entity,MatrixStack matrices,float partial){matrices.scale(1.4F,1.4F,1.4F);}
    }
    public static final class Demoness extends MobRenderer<DemonEntity,LegacyDemonessModel> {
        public Demoness(EntityRendererManager manager){super(manager,new LegacyDemonessModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(DemonEntity entity){return new ResourceLocation("bewitchment","textures/entity/demoness_"+entity.skin()+".png");}
        @Override protected void scale(DemonEntity entity,MatrixStack matrices,float partial){matrices.scale(1.4F,1.4F,1.4F);}
    }
    public static final class Werewolf extends MobRenderer<WerewolfEntity,LegacyWerewolfModel> {
        public Werewolf(EntityRendererManager manager){super(manager,new LegacyWerewolfModel(),.3F);addLayer(new WerewolfEyes(this));}
        @Override public ResourceLocation getTextureLocation(WerewolfEntity entity){return new ResourceLocation("bewitchment","textures/entity/werewolf/werewolf_"+WerewolfRules.texture(entity.skin())+".png");}
    }
    private static final class WerewolfEyes extends net.minecraft.client.renderer.entity.layers.LayerRenderer<WerewolfEntity,LegacyWerewolfModel> {
        private static final ResourceLocation TEXTURE=new ResourceLocation("bewitchment","textures/entity/werewolf/werewolf_eyes.png");
        WerewolfEyes(Werewolf renderer){super(renderer);}
        @Override public void render(MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light,WerewolfEntity entity,float swing,float amount,float partial,float age,float yaw,float pitch){
            // Preserve the original transparent background; eyes remain visible during invisibility.
            getParentModel().renderToBuffer(matrices,buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(TEXTURE)),15728640,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
        }
    }
    public static final class Druden extends MobRenderer<DrudenEntity,LegacyDrudenModel> {
        public Druden(EntityRendererManager manager){super(manager,new LegacyDrudenModel(),.3F);addLayer(new net.minecraft.client.renderer.entity.layers.HeldItemLayer<>(this));addLayer(new DrudenEyes(this));}
        @Override public ResourceLocation getTextureLocation(DrudenEntity entity){return texture("drude",entity.skin());}
    }
    private static final class DrudenEyes extends net.minecraft.client.renderer.entity.layers.LayerRenderer<DrudenEntity,LegacyDrudenModel> {
        private static final ResourceLocation TEXTURE=new ResourceLocation("bewitchment","textures/entity/drude_eyes.png");
        DrudenEyes(Druden renderer){super(renderer);}
        @Override public void render(MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light,DrudenEntity entity,float swing,float amount,float partial,float age,float yaw,float pitch){
            // This original overlay has an opaque black background: additive blending
            // keeps black neutral. Black Dog overlays instead require source-alpha blending.
            if(!entity.isInvisible() && !entity.isBaby())getParentModel().renderToBuffer(matrices,buffers.getBuffer(net.minecraft.client.renderer.RenderType.eyes(TEXTURE)),15728640,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
        }
    }
    public static final class Cambion extends MobRenderer<CambionEntity,CambionModelBase> {
        private final CambionModelBase regular=new LegacyCambionModel(),slim=new LegacyCambionSlimModel();
        public Cambion(EntityRendererManager manager){
            super(manager,new LegacyCambionModel(),.3F);
            addLayer(new net.minecraft.client.renderer.entity.layers.HeldItemLayer<>(this));
            addLayer(new net.minecraft.client.renderer.entity.layers.BipedArmorLayer<CambionEntity,CambionModelBase,net.minecraft.client.renderer.entity.model.BipedModel<CambionEntity>>(
                this,new net.minecraft.client.renderer.entity.model.BipedModel<>(.5F),new net.minecraft.client.renderer.entity.model.BipedModel<>(1)){
                @Override protected void setPartVisibility(net.minecraft.client.renderer.entity.model.BipedModel<CambionEntity> armor,net.minecraft.inventory.EquipmentSlotType slot){
                    super.setPartVisibility(armor,slot);
                    // Legacy head/chest armor sits 0.1 block above the vanilla origin.
                    if(slot==net.minecraft.inventory.EquipmentSlotType.HEAD){armor.head.y-=1.6F;armor.hat.y-=1.6F;}
                    if(slot==net.minecraft.inventory.EquipmentSlotType.CHEST){armor.body.y-=1.6F;armor.leftArm.y-=1.6F;armor.rightArm.y-=1.6F;}
                }
            });
        }
        @Override public void render(CambionEntity entity,float yaw,float partial,MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light){
            model=entity.skin()>1?slim:regular;
            super.render(entity,yaw,partial,matrices,buffers,light);
        }
        @Override public ResourceLocation getTextureLocation(CambionEntity entity){return texture("cambion",entity.skin());}
    }
    private static ResourceLocation texture(String name,int skin){return new ResourceLocation("bewitchment","textures/entity/"+name+"_"+CreatureRules.skin(skin)+".png");}
    public static final class Lizard extends MobRenderer<LizardEntity,LegacyLizardModel> {
        public Lizard(EntityRendererManager manager){super(manager,new LegacyLizardModel(),.1F);}
        @Override public ResourceLocation getTextureLocation(LizardEntity entity){return texture("lizard",entity.skin());}
        @Override protected void scale(LizardEntity entity,MatrixStack matrices,float partial){float size=entity.isBaby()?.1F:.2F;matrices.scale(size,size,size);}
    }
    public static final class Toad extends MobRenderer<ToadEntity,LegacyToadModel> {
        public Toad(EntityRendererManager manager){super(manager,new LegacyToadModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(ToadEntity entity){return texture("toad",entity.skin());}
        @Override protected void scale(ToadEntity entity,MatrixStack matrices,float partial){float size=entity.isBaby()?.2F:.4F;matrices.scale(size,size,size);}
    }
    public static final class Snake extends MobRenderer<SnakeEntity,LegacySnakeModel> {
        public Snake(EntityRendererManager manager){super(manager,new LegacySnakeModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(SnakeEntity entity){return new ResourceLocation("bewitchment","textures/entity/snake_"+entity.skin()+".png");}
        @Override protected void scale(SnakeEntity entity,MatrixStack matrices,float partial){float size=entity.isBaby()?.3F:.5F;matrices.scale(size,size,size);}
    }
    public static final class Raven extends MobRenderer<RavenEntity,LegacyRavenModel> {
        public Raven(EntityRendererManager manager){super(manager,new LegacyRavenModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(RavenEntity entity){return new ResourceLocation("bewitchment","textures/entity/raven.png");}
        @Override protected void scale(RavenEntity entity,MatrixStack matrices,float partial){float size=entity.isBaby()?.4F:.6F;matrices.scale(size,size,size);}
    }
    public static final class Owl extends MobRenderer<OwlEntity,LegacyOwlModel> {
        public Owl(EntityRendererManager manager){super(manager,new LegacyOwlModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(OwlEntity entity){return texture("owl",entity.skin());}
        @Override protected void scale(OwlEntity entity,MatrixStack matrices,float partial){float size=entity.isBaby()?.4F:.6F;matrices.scale(size,size,size);}
    }
    public static final class ShadowPerson extends MobRenderer<ShadowPersonEntity,LegacyShadowPersonModel> {
        public ShadowPerson(EntityRendererManager manager){super(manager,new LegacyShadowPersonModel(),.3F);addLayer(new ShadowPersonEyes(this));}
        @Override public ResourceLocation getTextureLocation(ShadowPersonEntity entity){return shadowTexture(entity,false);}
        @Override protected void scale(ShadowPersonEntity entity,MatrixStack matrices,float partial){matrices.scale(1.05F,1.05F,1.05F);}
    }
    private static ResourceLocation shadowTexture(ShadowPersonEntity entity,boolean eyes){
        return new ResourceLocation("bewitchment","textures/entity/shadowperson"+(ShadowPersonRules.texture(entity.skin())==1?"_hatdemon":"")+(eyes?"_eyes":"")+".png");
    }
    private static final class ShadowPersonEyes extends net.minecraft.client.renderer.entity.layers.LayerRenderer<ShadowPersonEntity,LegacyShadowPersonModel> {
        ShadowPersonEyes(ShadowPerson renderer){super(renderer);}
        @Override public void render(MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light,ShadowPersonEntity entity,float swing,float amount,float partial,float age,float yaw,float pitch){
            if(ShadowPersonRules.eyes(entity.isInvisible(),entity.isBaby()))
                getParentModel().renderToBuffer(matrices,buffers.getBuffer(net.minecraft.client.renderer.RenderType.eyes(shadowTexture(entity,true))),15728640,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
        }
    }
    public static final class Bafometyr extends MobRenderer<BafometyrEntity,LegacyBafometyrModel> {
        public Bafometyr(EntityRendererManager manager){super(manager,new LegacyBafometyrModel(),.3F);addLayer(new BafometyrFlames(this));}
        @Override public ResourceLocation getTextureLocation(BafometyrEntity entity){return new ResourceLocation("bewitchment","textures/entity/bafometyr.png");}
        @Override public void render(BafometyrEntity entity,float yaw,float partial,MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light){
            super.render(entity,yaw,partial,matrices,buffers,15728880);
        }
    }
    private static final class BafometyrFlames extends net.minecraft.client.renderer.entity.layers.LayerRenderer<BafometyrEntity,LegacyBafometyrModel> {
        private static final ResourceLocation[] FRAMES=new ResourceLocation[8];
        static {for(int i=0;i<8;i++)FRAMES[i]=new ResourceLocation("bewitchment","textures/entity/layer/bafometyr_fire_"+i+".png");}
        BafometyrFlames(Bafometyr renderer){super(renderer);}
        @Override public void render(MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light,BafometyrEntity entity,float swing,float amount,float partial,float age,float yaw,float pitch){
            getParentModel().renderToBuffer(matrices,buffers.getBuffer(net.minecraft.client.renderer.RenderType.eyes(FRAMES[BafometyrRules.flame(entity.flameTimer())])),15728880,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
        }
    }
    public static final class Cleaver extends MobRenderer<CleaverEntity,LegacyCleaverModel> {
        public Cleaver(EntityRendererManager manager){
            super(manager,new LegacyCleaverModel(1),.3F);
            addLayer(new net.minecraft.client.renderer.entity.layers.HeldItemLayer<>(this));
            addLayer(new net.minecraft.client.renderer.entity.layers.BipedArmorLayer<CleaverEntity,LegacyCleaverModel,net.minecraft.client.renderer.entity.model.BipedModel<CleaverEntity>>(
                this,new net.minecraft.client.renderer.entity.model.BipedModel<>(.5F),new net.minecraft.client.renderer.entity.model.BipedModel<>(1)){
                @Override protected void setPartVisibility(net.minecraft.client.renderer.entity.model.BipedModel<CleaverEntity> armor,net.minecraft.inventory.EquipmentSlotType slot){
                    super.setPartVisibility(armor,slot);
                    if(slot==net.minecraft.inventory.EquipmentSlotType.HEAD){armor.head.y-=6.4F;armor.hat.y-=6.4F;}
                    if(slot==net.minecraft.inventory.EquipmentSlotType.CHEST){armor.body.y-=4.8F;armor.leftArm.y-=4.8F;armor.rightArm.y-=4.8F;}
                    if(slot==net.minecraft.inventory.EquipmentSlotType.LEGS || slot==net.minecraft.inventory.EquipmentSlotType.FEET)armor.setAllVisible(false);
                }
            });
        }
        @Override public ResourceLocation getTextureLocation(CleaverEntity entity){return new ResourceLocation("bewitchment","textures/entity/cleaver"+(entity.skin()==1?"_2":"")+".png");}
    }
    public static final class Ghost extends MobRenderer<GhostEntity,LegacyGhostModel> {
        public Ghost(EntityRendererManager manager){super(manager,new LegacyGhostModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(GhostEntity entity){return new ResourceLocation("bewitchment","textures/entity/ghost_"+(entity.skin()+1)+".png");}
        @Override protected net.minecraft.client.renderer.RenderType getRenderType(GhostEntity entity,boolean visible,boolean translucent,boolean outline){
            return visible?net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(entity)):super.getRenderType(entity,visible,translucent,outline);
        }
    }
    private CreatureRenderers(){}
    public static final class Imp extends MobRenderer<ImpEntity,LegacyImpModel> {
        public Imp(EntityRendererManager manager){super(manager,new LegacyImpModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(ImpEntity entity){return new ResourceLocation("bewitchment","textures/entity/imp_"+entity.skin()+".png");}
        @Override protected void scale(ImpEntity entity,MatrixStack matrices,float partial){matrices.scale(.9F,.9F,.9F);}
    }
    public static final class BlackDog extends MobRenderer<BlackDogEntity,LegacyBlackDogModel> {
        public BlackDog(EntityRendererManager manager){super(manager,new LegacyBlackDogModel(),.3F);addLayer(new BlackDogEyes(this));}
        @Override public ResourceLocation getTextureLocation(BlackDogEntity entity){return new ResourceLocation("bewitchment","textures/entity/black_dog_"+entity.skin()+".png");}
        @Override protected void scale(BlackDogEntity entity,MatrixStack matrices,float partial){matrices.scale(1.8F,1.8F,1.8F);}
    }
    private static final class BlackDogEyes extends net.minecraft.client.renderer.entity.layers.LayerRenderer<BlackDogEntity,LegacyBlackDogModel> {
        BlackDogEyes(BlackDog renderer){super(renderer);}
        @Override public void render(MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light,BlackDogEntity entity,float swing,float amount,float partial,float age,float yaw,float pitch){
            ResourceLocation texture=new ResourceLocation("bewitchment","textures/entity/black_dog_eyes_"+entity.skin()+".png");
            // Legacy overlays contain colored RGB even at alpha zero. Additive eyes()
            // ignores that alpha and floods the body with light; blend by source alpha.
            getParentModel().renderToBuffer(matrices,buffers.getBuffer(net.minecraft.client.renderer.RenderType.entityTranslucent(texture)),15728640,net.minecraft.client.renderer.texture.OverlayTexture.NO_OVERLAY,1,1,1,1);
        }
    }
    public static final class Hellhound extends MobRenderer<HellhoundEntity,LegacyHellhoundModel> {
        public Hellhound(EntityRendererManager manager){super(manager,new LegacyHellhoundModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(HellhoundEntity entity){return texture("hellhound",entity.skin());}
        @Override protected void scale(HellhoundEntity entity,MatrixStack matrices,float partial){matrices.scale(1.3F,1.3F,1.3F);}
    }
    public static final class Feuerwurm extends MobRenderer<FeuerwurmEntity,LegacyFeuerwurmModel> {
        public Feuerwurm(EntityRendererManager manager){super(manager,new LegacyFeuerwurmModel(),.3F);}
        @Override public ResourceLocation getTextureLocation(FeuerwurmEntity entity){return new ResourceLocation("bewitchment","textures/entity/feuerwurm_"+entity.skin()+".png");}
        @Override protected net.minecraft.client.renderer.RenderType getRenderType(FeuerwurmEntity entity,boolean visible,boolean translucent,boolean outline){
            return entity.spectral() && visible?net.minecraft.client.renderer.RenderType.entityTranslucent(getTextureLocation(entity)):super.getRenderType(entity,visible,translucent,outline);
        }
        @Override public void render(FeuerwurmEntity entity,float yaw,float partial,MatrixStack matrices,net.minecraft.client.renderer.IRenderTypeBuffer buffers,int light){
            shadowRadius=entity.spectral()?0:.3F;super.render(entity,yaw,partial,matrices,buffers,light);
        }
    }
}
