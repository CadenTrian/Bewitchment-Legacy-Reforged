package com.bewitchment.client;

import com.bewitchment.machine.SigilTableMenu;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.gui.screen.inventory.ContainerScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.ITextComponent;

public final class SigilTableScreen extends ContainerScreen<SigilTableMenu> {
    private static final ResourceLocation TEXTURE=new ResourceLocation("bewitchment","textures/gui/sigil_table.png");
    public SigilTableScreen(SigilTableMenu menu,PlayerInventory inventory,ITextComponent title){super(menu,inventory,title);imageHeight=192;titleLabelY=-10;inventoryLabelY=100;}
    @Override public void render(MatrixStack pose,int x,int y,float partial){renderBackground(pose);super.render(pose,x,y,partial);renderTooltip(pose,x,y);}
    @Override protected void renderBg(MatrixStack pose,float partial,int x,int y){RenderSystem.color4f(1,1,1,1);minecraft.getTextureManager().bind(TEXTURE);blit(pose,leftPos,topPos,0,0,imageWidth,imageHeight);}
    @Override protected void renderLabels(MatrixStack pose,int x,int y){font.draw(pose,title,8,-10,0xffffff);font.draw(pose,inventory.getDisplayName(),8,100,0x404040);}
}
