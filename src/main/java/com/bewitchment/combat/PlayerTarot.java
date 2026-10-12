package com.bewitchment.combat;

import com.bewitchment.machine.TarotReading;
import com.bewitchment.machine.TarotHistory;
import com.bewitchment.machine.TarotCompanions;
import com.bewitchment.machine.TarotProtection;
import com.bewitchment.registry.ModCreatures;
import com.bewitchment.world.DemonPledges;
import net.minecraft.entity.EntityType;
import net.minecraft.stats.Stats;
import net.minecraftforge.registries.ForgeRegistries;
import com.bewitchment.registry.ModClothing;
import net.minecraft.entity.player.*;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.text.*;
import java.util.UUID;
import java.util.function.IntPredicate;

public final class PlayerTarot {
    public static void read(PlayerEntity reader,boolean allowed,IntPredicate pay) {
        if(reader.level.isClientSide)return;
        UUID id=TarotReading.boundId(reader.getMainHandItem().getTag());
        ServerPlayerEntity target=id==null?null:reader.getServer().getPlayerList().getPlayer(id);
        int[] count={0};
        String outcome=TarotReading.read(reader.getUUID(),new TarotReading.Access(){
            public boolean eligible(){return allowed&&PlayerFortunes.eligible(reader);}
            public UUID target(){return id;}
            public boolean online(){return target!=null;}
            public boolean pay(int amount){return pay.test(amount);}
            public boolean shrouded(){
                EquipmentSlotType[] slots={EquipmentSlotType.HEAD,EquipmentSlotType.CHEST,EquipmentSlotType.LEGS,EquipmentSlotType.FEET};
                String[] armor=new String[4];
                for(int i=0;i<slots.length;i++){ItemStack stack=target.getItemBySlot(slots[i]);ResourceLocation key=stack.getItem().getRegistryName();armor[i]=stack.isEmpty()||key==null?null:key.toString();}
                return TarotReading.shrouded(armor);
            }
            public String reveal(){
                TarotHistory.Category category=TarotHistory.choose(target.getRandom().nextInt(TarotHistory.Category.values().length));
                switch(category){
                    case COMPANIONS:return companions(target);
                    case PROTECTION:
                        if(TarotProtection.heldTotem(target.getMainHandItem(),target.getOffhandItem()))return "carried_protection";
                        for(int slot=0;slot<target.inventory.getContainerSize();slot++)if(TarotProtection.poppet(target.inventory.getItem(slot),target.getUUID()))return "carried_protection";
                        return "no_carried_protection";
                    case MONSTER_KILLS:
                        count[0]=TarotHistory.monsterKills(ForgeRegistries.ENTITIES.getValues(),type->target.getStats().getValue(Stats.ENTITY_KILLED.get(type)));
                        return "monster_kills";
                    case PEOPLE_KILLS:
                        count[0]=TarotHistory.peopleKills(target.getStats().getValue(Stats.CUSTOM.get(Stats.PLAYER_KILLS)),target.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.VILLAGER)));
                        return "people_kills";
                    case BOSSES:
                        return TarotHistory.defeatedBoss(target.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.WITHER)),
                                target.getStats().getValue(Stats.ENTITY_KILLED.get(EntityType.ENDER_DRAGON)),
                                target.getStats().getValue(Stats.ENTITY_KILLED.get(ModCreatures.LEONARD.get())),
                                target.getStats().getValue(Stats.ENTITY_KILLED.get(ModCreatures.BAPHOMET.get())))?"has_boss":"no_boss";
                    case PLEDGES:return TarotHistory.pledged(DemonPledges.get(target.getLevel()),target.getUUID())?"pledged":"not_pledged";
                    case NOTHING:return "nothing";
                    case FORTUNE:return TarotReading.reveal(1,PlayerFortunes.read(target).kind(),false,false,"");
                    case EFFECTS:return TarotReading.reveal(2,null,target.getActiveEffects().stream().anyMatch(effect->!effect.isAmbient()),false,"");
                    case CURSES:return TarotReading.reveal(3,null,false,!PlayerCurses.read(target).active().isEmpty(),"");
                    case ATTIRE:
                        String attire="";for(String family:new String[]{"green_witch","besmirched","alchemist","witches"})if(ModClothing.hasSet(target,family)){attire=family;break;}
                        return TarotReading.reveal(4,null,false,false,attire);
                }
                throw new IllegalStateException("Unsupported tarot category: "+category);
            }
        });
        // Resolve display names from the online player, never from untrusted boundName NBT.
        reader.displayClientMessage(new TranslationTextComponent("tarot.bewitchment."+outcome,target==null?new StringTextComponent(""):target.getDisplayName(),count[0]),true);
    }
    private static String companions(ServerPlayerEntity target){
        java.util.List<Iterable<net.minecraft.entity.Entity>> worlds=new java.util.ArrayList<>();
        for(net.minecraft.world.server.ServerWorld world:target.getServer().getAllLevels())worlds.add(world.getAllEntities());
        TarotCompanions.Result result=TarotCompanions.scan(worlds,entity->{
            if(entity instanceof net.minecraft.entity.passive.TameableEntity){
                net.minecraft.entity.passive.TameableEntity pet=(net.minecraft.entity.passive.TameableEntity)entity;
                return TarotCompanions.owned(pet.isAlive(),pet.isTame(),pet.getOwnerUUID(),target.getUUID());
            }
            if(entity instanceof net.minecraft.entity.passive.horse.AbstractHorseEntity){
                net.minecraft.entity.passive.horse.AbstractHorseEntity horse=(net.minecraft.entity.passive.horse.AbstractHorseEntity)entity;
                return TarotCompanions.owned(horse.isAlive(),horse.isTamed(),horse.getOwnerUUID(),target.getUUID());
            }
            return false;
        },TarotCompanions.LIMIT);
        return result==TarotCompanions.Result.FOUND?"companion_found":result==TarotCompanions.Result.NONE?"no_loaded_companion":"companion_incomplete";
    }
    private PlayerTarot(){}
}
