package com.bewitchment.registry;

import com.bewitchment.entity.*;
import com.bewitchment.item.BroomItem;
import net.minecraft.entity.*;
import net.minecraft.item.*;
import net.minecraft.util.*;
import net.minecraftforge.fml.RegistryObject;
import java.util.*;

public final class ModBrooms {
    public static final Map<BroomRules.Kind,RegistryObject<EntityType<BroomEntity>>> ENTITIES=new EnumMap<>(BroomRules.Kind.class);
    public static final Map<BroomRules.Kind,RegistryObject<Item>> ITEMS=new EnumMap<>(BroomRules.Kind.class);
    public static final RegistryObject<Item> BROOM=ModItems.ITEMS.register("broom",()->new BroomItem(null));
    public static final RegistryObject<Item> HIDDEN_BAT=ModItems.ITEMS.register("hidden_bat",()->new Item(new Item.Properties().durability(1)));
    public static final RegistryObject<SoundEvent> SWEEP=ModCreatures.SOUNDS.register("broom_sweep",()->new SoundEvent(new ResourceLocation("bewitchment:broom_sweep")));
    static {for(BroomRules.Kind kind:BroomRules.Kind.values()){
        ENTITIES.put(kind,ModCreatures.ENTITIES.register(kind.id(),()->EntityType.Builder.<BroomEntity>of((type,world)->new BroomEntity(type,world,kind),EntityClassification.MISC)
                .sized(.7F,.7F).clientTrackingRange(8).updateInterval(1).build("bewitchment:"+kind.id())));
        ITEMS.put(kind,ModItems.ITEMS.register(kind.id(),()->new BroomItem(kind)));
    }}
    public static void init(){}
    private ModBrooms(){}
}
