package com.bewitchment.machine;

import net.minecraft.nbt.*;
import net.minecraft.util.datafix.*;
import com.mojang.serialization.Dynamic;
import java.io.*;
import java.nio.file.*;
import java.util.*;

/** Explicit reproducible developer import; never run as part of build/check. */
public final class LegacyStructurePort {
    public static void main(String[] args)throws Exception {
        Path source=Paths.get("../Bewitchment-1.12.2/src/main/resources/assets/bewitchment/structures");
        Path target=Paths.get("src/main/resources/data/bewitchment/structures");Files.createDirectories(target);
        try(java.util.stream.Stream<Path> files=Files.list(source)) {
            for(Path file:(Iterable<Path>)files.sorted()::iterator) {
                if(!file.toString().endsWith(".nbt"))continue;
                CompoundNBT original;
                try(InputStream in=Files.newInputStream(file)){original=CompressedStreamTools.readCompressed(in);}
                CompoundNBT tag=convert(original);
                try(OutputStream out=Files.newOutputStream(target.resolve(file.getFileName()))){CompressedStreamTools.writeCompressed(tag,out);}
                System.out.println("Ported "+file.getFileName()+": "+tag.getList("blocks",10).size()+" blocks, "+tag.getList("entities",10).size()+" entities");
            }
        }
    }
    public static CompoundNBT convert(CompoundNBT original) {
        CompoundNBT tag=NBTUtil.update(DataFixesManager.getDataFixer(),DefaultTypeReferences.STRUCTURE,original.copy(),1343,2586);
        ListNBT palette=tag.getList("palette",10),blocks=tag.getList("blocks",10);
        Map<String,CompoundNBT> positions=new HashMap<>();
        for(int i=0;i<blocks.size();i++)positions.put(blocks.getCompound(i).getList("pos",3).toString(),blocks.getCompound(i).copy());
        ListNBT rebuilt=new ListNBT();
        for(int i=0;i<blocks.size();i++) {
            CompoundNBT block=blocks.getCompound(i),state=palette.getCompound(block.getInt("state")).copy();
            CompoundNBT props=state.getCompound("Properties"),tile=block.getCompound("nbt");String name=state.getString("Name");
            if(name.equals("bewitchment:cypress_wood_slab")){props.putString("type",props.getString("half"));props.remove("half");props.remove("variant");}
            if(name.equals("bewitchment:cypress_sapling"))props.remove("type");
            if(name.equals("minecraft:peony")&&props.getString("half").equals("upper")) {
                ListNBT below=block.getList("pos",3).copy();below.set(1,IntNBT.valueOf(below.getInt(1)-1));
                CompoundNBT lower=positions.get(below.toString());
                if(lower==null)throw new IllegalStateException("Orphan double plant");
                String lowerName=palette.getCompound(lower.getInt("state")).getString("Name");
                if(!lowerName.equals("minecraft:tall_grass")&&!lowerName.equals("minecraft:large_fern"))throw new IllegalStateException("Unknown double plant "+lowerName);
                state.putString("Name",lowerName);
            }
            if(tile.getString("id").equals("minecraft:bed")){if(tile.getInt("color")!=15)throw new IllegalStateException("Unexpected bed color");state.putString("Name","minecraft:black_bed");tile.remove("color");}
            if(tile.getString("id").equals("minecraft:banner")){if(tile.getInt("Base")!=15)throw new IllegalStateException("Unexpected banner color");state.putString("Name","minecraft:black_wall_banner");tile.remove("Base");}
            if(tile.getString("id").equals("minecraft:skull")) {
                if(tile.getInt("SkullType")!=1)throw new IllegalStateException("Unexpected skull");
                state.putString("Name","minecraft:wither_skeleton_skull");props=new CompoundNBT();props.putString("rotation",Integer.toString(tile.getInt("Rot")&15));state.put("Properties",props);
                tile.remove("SkullType");tile.remove("Rot");
            }
            if(tile.getString("id").equals("bewitchment:witches_cauldron")) {
                if(!tile.getCompound("inventory_0").getList("Items",10).isEmpty()||!tile.getCompound("tank").contains("Empty"))throw new IllegalStateException("Nonempty legacy cauldron requires explicit migration");
                CompoundNBT empty=new CompoundNBT();empty.putString("id","bewitchment:witches_cauldron");block.put("nbt",empty);
            }
            int index=rebuilt.indexOf(state);if(index<0){index=rebuilt.size();rebuilt.add(state);}block.putInt("state",index);
        }
        tag.put("palette",rebuilt);tag.putInt("DataVersion",2586);
        ListNBT entities=tag.getList("entities",10);
        for(int i=0;i<entities.size();i++) {
            CompoundNBT entity=entities.getCompound(i).getCompound("nbt");
            entity.remove("UUID");entity.remove("UUIDMost");entity.remove("UUIDLeast");
            if(entity.getString("id").equals("bewitchment:cambion")) {
                // Invalid old random skin IDs used the legacy male-0 texture fallback.
                entity.putInt("CambionType",com.bewitchment.entity.CreatureRules.skin(entity.getInt("CambionType")));
                entity.remove("Attributes"); // Use registered attributes instead of obsolete 1.12 names/modifiers.
                for(String key:new String[]{"HandItems","ArmorItems"}) {
                    ListNBT items=entity.getList(key,10);
                    for(int j=0;j<items.size();j++)if(items.getCompound(j).contains("id"))
                        items.set(j,DataFixesManager.getDataFixer().update(TypeReferences.ITEM_STACK,new Dynamic<>(NBTDynamicOps.INSTANCE,items.getCompound(j)),1343,2586).getValue());
                }
            }
        }
        return tag;
    }
}
