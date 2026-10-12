package com.bewitchment.machine;

import com.bewitchment.item.BolineItem;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.play.server.SChunkDataPacket;
import net.minecraft.util.*;
import net.minecraft.util.math.*;
import net.minecraft.util.registry.Registry;
import net.minecraft.world.biome.*;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.fml.common.ObfuscationReflectionHelper;
import java.lang.reflect.Field;
import java.util.UUID;

/** One loaded chunk per tick, followed by bounded soil work for Sowing Salt. */
public final class LandscapeRitual {
    private final BlockPos center;
    private final UUID owner;
    private final ResourceLocation biome;
    private final boolean salt;
    private int chunkCursor;
    private NatureBlessing soil;

    LandscapeRitual(BlockPos center, UUID owner, ResourceLocation biome, boolean salt) {
        this.center=center.immutable(); this.owner=owner; this.biome=biome; this.salt=salt;
    }
    static ResourceLocation target(Inventory inputs) {
        ResourceLocation found=null;
        for(int i=0;i<inputs.getContainerSize();i++) if(inputs.getItem(i).getItem() instanceof BolineItem) {
            if(found!=null) return null;
            found=BolineItem.recordedBiome(inputs.getItem(i));
            if(found==null) return null;
        }
        return found;
    }
    static Biome resolve(ServerWorld world, ResourceLocation id) {
        Registry<Biome> registry=world.registryAccess().registryOrThrow(Registry.BIOME_REGISTRY);
        return id!=null && registry.containsKey(id) ? registry.get(id) : null;
    }
    static ResourceLocation target(RitualEffect effect, Inventory inputs) {
        return effect==RitualEffect.SOWING_SALT ? new ResourceLocation("minecraft:desert") : target(inputs);
    }
    UUID owner() {return owner;}
    boolean finished() {return chunkCursor>=25 && (!salt || soil!=null && soil.finished());}
    void stop() {chunkCursor=25; if(salt && soil==null) soil=new NatureBlessing(center,owner,0,true); if(soil!=null) soil.stop();}
    static int biomeIndex(int x, int y, int z) {return (y<<4)|(z<<2)|x;}
    static boolean columnInside(BlockPos center, int x, int z) {
        for(int dx=0;dx<4;dx++) for(int dz=0;dz<4;dz++)
            if(!NatureBlessing.inCircle(x+dx-center.getX(),z+dz-center.getZ())) return false;
        return true;
    }
    ChunkPos chunk(int index) {return new ChunkPos(((center.getX()-32)>>4)+index%5,((center.getZ()-32)>>4)+index/5);}
    interface Cells {
        boolean permitted(int x, int y, int z);
        Biome get(int x, int y, int z);
        void set(int x, int y, int z, Biome biome);
    }
    /** Permissions cover every block represented by the cell, including vertical claims. */
    static int transform(BlockPos center, ChunkPos chunk, Biome target, Cells cells) {
        int changed=0;
        for(int x=0;x<4;x++) for(int z=0;z<4;z++) {
            int wx=chunk.getMinBlockX()+x*4, wz=chunk.getMinBlockZ()+z*4;
            if(!columnInside(center,wx,wz)) continue;
            for(int y=0;y<64;y++) {
                if(cells.get(x,y,z)==target) continue;
                boolean permitted=true;
                for(int dx=0;dx<4 && permitted;dx++) for(int dz=0;dz<4 && permitted;dz++) for(int dy=0;dy<4;dy++)
                    if(!cells.permitted(wx+dx,y*4+dy,wz+dz)) {permitted=false; break;}
                if(permitted) {cells.set(x,y,z,target); changed++;}
            }
        }
        return changed;
    }
    private static final class Access {
        // SRG name verified against the 1.16.5 mapped source; Forge remaps it in development.
        static final Field BIOMES=ObfuscationReflectionHelper.findField(BiomeContainer.class,"field_227054_f_");
        static Biome[] array(BiomeContainer container) {
            try {return (Biome[])BIOMES.get(container);} catch(IllegalAccessException e) {throw new IllegalStateException("Cannot access biome cells",e);}
        }
    }
    public static void verifyAccess() {
        // Fail during common setup rather than midway through a paid ritual.
        if(Access.BIOMES.getType()!=Biome[].class) throw new IllegalStateException("Unexpected biome storage");
        Registry<Biome> registry=net.minecraft.util.registry.WorldGenRegistries.BIOME;
        Biome plains=registry.get(Biomes.PLAINS), desert=registry.get(Biomes.DESERT);
        Biome[] original=new Biome[1024]; java.util.Arrays.fill(original,plains);
        BiomeContainer sample=new BiomeContainer(registry,original);
        Access.array(sample)[biomeIndex(3,63,3)]=desert;
        if(sample.getNoiseBiome(3,63,3)!=desert || sample.writeBiomes()[1023]!=registry.getId(desert)
                || sample.getNoiseBiome(0,0,0)!=plains) throw new IllegalStateException("Biome mutation did not reach serialized storage");
        org.apache.logging.log4j.LogManager.getLogger().info("Bewitchment biome storage access verified");
    }
    void tick(ServerWorld world, PlayerEntity player) {
        if(!player.isAlive() || !player.abilities.mayBuild) return;
        Biome target=resolve(world,biome);
        if(target==null) return; // Missing datapack target pauses the saved job.
        if(chunkCursor<25) {
            ChunkPos pos=chunk(chunkCursor++);
            if(!world.hasChunk(pos.x,pos.z)) return;
            Chunk chunk=world.getChunkSource().getChunkNow(pos.x,pos.z);
            if(chunk==null || chunk.getBiomes()==null) return;
            Biome[] cells=Access.array(chunk.getBiomes());
            int changed=transform(center,pos,target,new Cells() {
                public boolean permitted(int x,int y,int z) {
                    BlockPos block=new BlockPos(x,y,z);
                    return world.getWorldBorder().isWithinBounds(block) && world.mayInteract(player,block)
                            && player.mayUseItemAt(block,Direction.UP,ItemStack.EMPTY);
                }
                public Biome get(int x,int y,int z) {return cells[biomeIndex(x,y,z)];}
                public void set(int x,int y,int z,Biome biome) {cells[biomeIndex(x,y,z)]=biome;}
            });
            if(changed>0) {
                chunk.setUnsaved(true);
                SChunkDataPacket packet=new SChunkDataPacket(chunk,65535);
                world.getChunkSource().chunkMap.getPlayers(pos,false).forEach(watcher->watcher.connection.send(packet));
            }
        } else if(salt) {
            if(soil==null) soil=new NatureBlessing(center,owner,0,true);
            soil.tick(world,player);
        }
    }
    CompoundNBT save() {
        CompoundNBT tag=new CompoundNBT();
        tag.putInt("Version",1); tag.putUUID("Owner",owner); tag.putString("Biome",biome.toString());
        tag.putBoolean("Salt",salt); tag.putInt("ChunkCursor",chunkCursor);
        if(soil!=null) tag.put("Soil",soil.save());
        return tag;
    }
    static LandscapeRitual read(BlockPos center, CompoundNBT tag) {
        if(center.getY()<0 || center.getY()>255 || tag.getInt("Version")!=1 || !tag.hasUUID("Owner")
                || !tag.contains("Biome",8) || !tag.contains("Salt",1) || !tag.contains("ChunkCursor",3)) return null;
        String name=tag.getString("Biome"); ResourceLocation id=name.isEmpty()?null:ResourceLocation.tryParse(name);
        int cursor=tag.getInt("ChunkCursor");
        if(id==null || cursor<0 || cursor>25 || tag.getBoolean("Salt") && !id.equals(new ResourceLocation("minecraft:desert"))) return null;
        LandscapeRitual job=new LandscapeRitual(center,tag.getUUID("Owner"),id,tag.getBoolean("Salt"));
        job.chunkCursor=cursor;
        if(tag.contains("Soil")) {
            if(!job.salt || cursor!=25 || !tag.contains("Soil",10)) return null;
            job.soil=NatureBlessing.read(center,tag.getCompound("Soil"));
            if(job.soil==null || !job.soil.salted() || !job.owner.equals(job.soil.owner())) return null;
        }
        return job.finished()?null:job;
    }
}
