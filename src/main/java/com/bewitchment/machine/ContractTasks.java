package com.bewitchment.machine;

import com.bewitchment.item.KillContractItem;
import com.bewitchment.machine.CurseData.Kind;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import java.util.*;

/** Shared legacy task generation for commands and future acquisition sources. */
public final class ContractTasks {
    private static final List<Kind> POOL=Collections.unmodifiableList(Arrays.asList(
        Kind.BERSERKER,Kind.DESICCATION,Kind.DEVOURING_RAGE,Kind.IGNITION,
        Kind.SALAMANDRINE_TOLERANCE,Kind.MAHAPADMA));

    public static List<Kind> kinds(){return POOL;}

    public static ItemStack random(Item item,Random random){
        validateItem(item);Objects.requireNonNull(random,"random");
        return generate(item,POOL.get(random.nextInt(POOL.size())),random);
    }

    public static ItemStack generate(Item item,Kind kind,Random random){
        validateItem(item);Objects.requireNonNull(random,"random");
        if(!POOL.contains(kind))throw new IllegalArgumentException("Unsupported contract kind");
        // Legacy rolls a kill count even for donation tasks.
        int kills=6+random.nextInt(5);
        if(KillContractData.supported(kind))return KillContractItem.task(item,kind,kills);
        int[] amounts=new int[DonationContractData.size(kind)];
        for(int i=0;i<amounts.length;i++){
            int rolled=6+random.nextInt(6);
            amounts[i]=DonationContractData.ingredient(kind,i)==Items.WATER_BUCKET?1:rolled;
        }
        return KillContractItem.donationTask(item,kind,amounts);
    }

    public static ItemStack creative(Item item,Kind kind){
        validateItem(item);
        if(!POOL.contains(kind))throw new IllegalArgumentException("Unsupported contract kind");
        if(KillContractData.supported(kind))return KillContractItem.task(item,kind,6);
        int[] amounts=new int[DonationContractData.size(kind)];
        for(int i=0;i<amounts.length;i++)amounts[i]=DonationContractData.ingredient(kind,i)==Items.WATER_BUCKET?1:6;
        return KillContractItem.donationTask(item,kind,amounts);
    }

    private static void validateItem(Item item){if(!(item instanceof KillContractItem))throw new IllegalArgumentException("Contract item required");}
    private ContractTasks(){}
}