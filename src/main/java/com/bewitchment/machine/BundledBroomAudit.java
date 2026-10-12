package com.bewitchment.machine;

import com.bewitchment.entity.BroomRules;
import com.bewitchment.item.BroomItem;
import com.bewitchment.registry.*;
import net.minecraft.nbt.*;
import net.minecraft.block.Block;
import net.minecraft.state.Property;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.gen.feature.template.Template;
import net.minecraftforge.registries.ForgeRegistries;
import java.io.*;

/** Validate shipped structures against actual mod registries, without generating a world. */
public final class BundledBroomAudit {
    public static final String[] STRUCTURES={"burnedwickerman","cambion1","cambionmedium1","menhir1","menhir2","menhir3","menhir4","menhir5","stonecircle1","stonecircle2","wickerman"};
    public static void verify() {
        for(BroomRules.Kind kind:BroomRules.Kind.values()) {
            if(!(ModBrooms.ITEMS.get(kind).get() instanceof BroomItem)||ModBrooms.ENTITIES.get(kind).get()==null)
                throw new IllegalStateException("Missing broom: "+kind);
        }
        if(ModEnchantments.MAGIC_PROTECTION.get().getMaxLevel()!=4)throw new IllegalStateException("Missing magic protection");
        int blocks=0,entities=0;
        for(String name:STRUCTURES) {
            String path="data/bewitchment/structures/"+name+".nbt";
            try(InputStream stream=BundledBroomAudit.class.getClassLoader().getResourceAsStream(path)) {
                if(stream==null)throw new FileNotFoundException(path);
                CompoundNBT tag=CompressedStreamTools.readCompressed(stream);
                if(tag.getInt("DataVersion")!=2586)throw new IllegalStateException("Wrong structure version: "+name);
                ListNBT palette=tag.getList("palette",10),entries=tag.getList("blocks",10),size=tag.getList("size",3);
                if(size.size()!=3||entries.isEmpty()||palette.isEmpty())throw new IllegalStateException("Empty structure: "+name);
                for(int i=0;i<palette.size();i++) {
                    CompoundNBT state=palette.getCompound(i);ResourceLocation id=new ResourceLocation(state.getString("Name"));
                    if(!ForgeRegistries.BLOCKS.containsKey(id))throw new IllegalStateException("Missing structure block: "+id);
                    Block block=ForgeRegistries.BLOCKS.getValue(id);CompoundNBT props=state.getCompound("Properties");
                    for(String key:props.getAllKeys()) {
                        Property<?> property=block.getStateDefinition().getProperty(key);
                        if(property==null||!property.getValue(props.getString(key)).isPresent())throw new IllegalStateException("Invalid structure state: "+state);
                    }
                }
                java.util.Set<String> occupied=new java.util.HashSet<>();
                for(int i=0;i<entries.size();i++) {
                    CompoundNBT entry=entries.getCompound(i);ListNBT pos=entry.getList("pos",3);
                    if(entry.getInt("state")<0||entry.getInt("state")>=palette.size()||pos.size()!=3||!occupied.add(pos.toString()))throw new IllegalStateException("Invalid/duplicate structure block: "+name);
                    for(int axis=0;axis<3;axis++)if(pos.getInt(axis)<0||pos.getInt(axis)>=size.getInt(axis))throw new IllegalStateException("Structure out of bounds: "+name);
                    if(entry.contains("nbt")) {
                        ResourceLocation id=new ResourceLocation(entry.getCompound("nbt").getString("id"));
                        if(!ForgeRegistries.TILE_ENTITIES.containsKey(id))throw new IllegalStateException("Missing structure tile: "+id);
                    }
                }
                ListNBT inhabitants=tag.getList("entities",10);
                for(int i=0;i<inhabitants.size();i++) {
                    CompoundNBT entity=inhabitants.getCompound(i).getCompound("nbt");
                    if(!ForgeRegistries.ENTITIES.containsKey(new ResourceLocation(entity.getString("id"))))throw new IllegalStateException("Missing structure entity: "+entity);
                    if(entity.contains("UUID")||entity.contains("UUIDMost")||entity.contains("UUIDLeast"))throw new IllegalStateException("Template must generate fresh entity UUIDs");
                }
                new Template().load(tag);blocks+=entries.size();entities+=inhabitants.size();
            }catch(IOException error){throw new IllegalStateException("Cannot read "+path,error);}
        }
        org.apache.logging.log4j.LogManager.getLogger().info("Verified four broom registrations, Magic Protection and 11 native structure templates ({} blocks, {} entities)",blocks,entities);
    }
    private BundledBroomAudit(){}
}
