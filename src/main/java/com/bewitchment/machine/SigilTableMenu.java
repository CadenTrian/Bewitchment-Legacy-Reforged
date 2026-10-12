package com.bewitchment.machine;

import net.minecraft.entity.player.*;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.container.*;
import net.minecraft.item.ItemStack;

public final class SigilTableMenu extends Container {
    private final SigilTableTile tile;
    private final Inventory preview=new Inventory(1);
    public SigilTableMenu(int id,PlayerInventory player){this(id,player,null);}
    public SigilTableMenu(int id,PlayerInventory player,SigilTableTile tile){
        super(ModMachines.SIGIL_TABLE_MENU.get(),id);this.tile=tile;Inventory matrix=tile==null?new Inventory(25):tile.matrix;
        for(int y=0;y<5;y++)for(int x=0;x<5;x++)addSlot(new Slot(matrix,x+y*5,25+x*18,9+y*18));
        addSlot(new Slot(preview,0,134,45){@Override public boolean mayPlace(ItemStack stack){return false;}@Override public boolean mayPickup(PlayerEntity player){return false;}});
        for(int row=0;row<3;row++)for(int col=0;col<9;col++)addSlot(new Slot(player,col+row*9+9,8+col*18,110+row*18));
        for(int col=0;col<9;col++)addSlot(new Slot(player,col,8+col*18,168));
    }
    @Override public boolean stillValid(PlayerEntity player){return tile==null || (!tile.isRemoved() && player.level.getBlockEntity(tile.getBlockPos())==tile && player.distanceToSqr(tile.getBlockPos().getX()+.5,tile.getBlockPos().getY()+.5,tile.getBlockPos().getZ()+.5)<=64);}
    @Override public void broadcastChanges(){if(tile!=null){SigilRecipe recipe=tile.recipe();preview.setItem(0,recipe==null?ItemStack.EMPTY:recipe.assemble(tile.matrix));}super.broadcastChanges();}
    @Override public ItemStack clicked(int index,int button,ClickType type,PlayerEntity player){
        if(index!=25)return super.clicked(index,button,type,player);
        if(tile==null || player.level.isClientSide || !stillValid(player))return ItemStack.EMPTY;
        if(type==ClickType.PICKUP && (button==0 || button==1)){
            Inventory cursor=new Inventory(player.inventory.getCarried().copy());
            if(SigilCrafting.craft(tile.recipe(),tile.matrix,cursor,1))player.inventory.setCarried(cursor.getItem(0));
        }else if(type==ClickType.QUICK_MOVE){
            // Bound a packet to one stack of crafts; stop before consuming if inventory fills.
            for(int i=0;i<64;i++)if(!SigilCrafting.craft(tile.recipe(),tile.matrix,player.inventory,36))break;
        }
        // Output clicks are server-authoritative, so vanilla's prediction-suppressed
        // acknowledgement path would otherwise leave the client's cursor stale.
        broadcastChanges();((ServerPlayerEntity)player).broadcastCarriedItem();return ItemStack.EMPTY;
    }
    @Override public ItemStack quickMoveStack(PlayerEntity player,int index){
        if(index==25 || index<0 || index>=slots.size())return ItemStack.EMPTY;
        Slot slot=slots.get(index);if(!slot.hasItem())return ItemStack.EMPTY;ItemStack stack=slot.getItem(),original=stack.copy();
        if(index<25){if(!moveItemStackTo(stack,26,62,true))return ItemStack.EMPTY;}else if(!moveItemStackTo(stack,0,25,false))return ItemStack.EMPTY;
        if(stack.isEmpty())slot.set(ItemStack.EMPTY);else slot.setChanged();slot.onTake(player,stack);return original;
    }
}
