package com.bewitchment.machine;
import com.bewitchment.registry.ModEffects;
import net.minecraft.item.*;
import net.minecraft.potion.*;
import net.minecraft.tags.*;
import net.minecraft.util.ResourceLocation;
import java.util.*;
import java.util.function.Supplier;
/** Implemented original brew ingredients; matching can contribute multiple effects. */
public final class BrewIngredients {
    private static final Map<String,List<Supplier<EffectInstance>>> ITEMS=new LinkedHashMap<>();
    private static final Map<ResourceLocation,Supplier<EffectInstance>> TAGS=new LinkedHashMap<>();
    private static final ITag.INamedTag<Item> SAPLINGS=ItemTags.createOptional(new ResourceLocation("minecraft:saplings"));
    private static final ITag.INamedTag<Item> SLIME=ItemTags.createOptional(new ResourceLocation("forge:slimeballs"));
    private static final ITag.INamedTag<Item> CANE=ItemTags.createOptional(new ResourceLocation("forge:sugarcane"));
    static {
        add(Effects.HEAL,1,"minecraft:apple");add(Effects.HARM,1,"minecraft:spider_eye");add(Effects.REGENERATION,400,"minecraft:glistering_melon_slice");
        add(Effects.POISON,400,"bewitchment:snake_venom","bewitchment:belladonna");add(Effects.WITHER,300,"bewitchment:bottle_of_blood");
        add(Effects.NIGHT_VISION,2400,"minecraft:carrot","bewitchment:eye_of_old");add(Effects.BLINDNESS,200,"bewitchment:iron_gall_ink","minecraft:ink_sac");
        add(Effects.DAMAGE_BOOST,1300,"minecraft:porkchop","minecraft:beef","minecraft:mutton","minecraft:chicken","minecraft:rabbit");
        add(Effects.WEAKNESS,900,"bewitchment:hellebore");add(Effects.DAMAGE_RESISTANCE,600,"minecraft:cooked_porkchop","minecraft:cooked_beef","minecraft:cooked_mutton","minecraft:cooked_chicken","minecraft:cooked_rabbit");
        add(Effects.FIRE_RESISTANCE,3000,"bewitchment:embergrass");add(Effects.JUMP,900,"minecraft:rabbit_hide","bewitchment:hoof");
        add(Effects.MOVEMENT_SPEED,900,"minecraft:sugar");add(Effects.MOVEMENT_SLOWDOWN,800,"minecraft:brown_mushroom");
        add(Effects.DIG_SLOWDOWN,600,"bewitchment:lizard_leg");add(Effects.LUCK,2400,"minecraft:rabbit_foot");add(Effects.UNLUCK,2400,"bewitchment:adders_fork");
        add(Effects.WATER_BREATHING,3000,"minecraft:cod","bewitchment:eye_of_old");add(Effects.INVISIBILITY,500,"bewitchment:ectoplasm");
        add(Effects.HUNGER,600,"bewitchment:tongue_of_dog");add(Effects.CONFUSION,200,"minecraft:red_mushroom");add(Effects.GLOWING,800,"bewitchment:spectral_dust");add(Effects.LEVITATION,80,"minecraft:chorus_fruit");
        addCustom(ModEffects.HELLWORLD,1,"bewitchment:hellhound_horn");
        addCustom(ModEffects.ICEWORLD,1,"minecraft:ice","minecraft:packed_ice","minecraft:snow_block");
        addCustom(ModEffects.CITRINITAS,1,"bewitchment:birch_soul");
        addCustom(ModEffects.RUBEDO,1,"bewitchment:acacia_resin");
        addCustom(ModEffects.ARACHNOPHOBIA,1,"minecraft:cobweb");
        addCustom(ModEffects.DESERTIFICATION,1,"minecraft:coal","minecraft:charcoal");
        addCustom(ModEffects.SPORE_CLOUD,1,"minecraft:mushroom_stew");
        addCustom(ModEffects.BLIGHT,1,"minecraft:rotten_flesh");
        addCustom(ModEffects.MENDING,1,"minecraft:golden_apple","minecraft:enchanted_golden_apple");
        addCustom(ModEffects.FERTILITY,1,"minecraft:blue_orchid");
        addCustom(ModEffects.HARVEST,1,"minecraft:pumpkin","minecraft:carved_pumpkin");
        addCustom(ModEffects.RUIN,1,"minecraft:cactus","minecraft:dead_bush");
        addCustom(ModEffects.PESTICIDE,1,"bewitchment:wormwood");
        tag("forge:crops/wormwood",ModEffects.PESTICIDE,1);
        addCustom(ModEffects.WOLFSBANE,1,"bewitchment:aconitum");
        tag("forge:crops/aconitum",ModEffects.WOLFSBANE,1);
        addCustom(ModEffects.DEVIANTS_DECOMPOSURE,1,"bewitchment:elderberries");
        addCustom(ModEffects.DEFLECTION,600,"minecraft:popped_chorus_fruit");
        addCustom(ModEffects.VOLATILITY,900,"minecraft:fire_charge");
        addCustom(ModEffects.RAGE,1,"minecraft:bone");
        addCustom(ModEffects.FRIENDSHIP,1,"minecraft:cookie");
        addCustom(ModEffects.FEAR,600,"bewitchment:demon_heart");
        addCustom(ModEffects.ABSENCE,1,"minecraft:milk_bucket");
        addCustom(ModEffects.PURIFICATION,1,"minecraft:melon_slice");
        addCustom(ModEffects.CORRUPTION,1,"minecraft:fermented_spider_eye");
        addCustom(ModEffects.SLEEPING,1,"minecraft:poppy");
        addCustom(ModEffects.CURSED_LEAPS,1,"bewitchment:toe_of_frog");
        addCustom(ModEffects.BUTTERFINGERS,1,"bewitchment:cloudy_oil");
        addCustom(ModEffects.DISROBING,1,"bewitchment:tallow");
        tag("forge:tallow",ModEffects.DISROBING,1);
        addCustom(ModEffects.MAGIC_RESISTANCE,1200,"bewitchment:dragons_blood_resin");
        addCustom(ModEffects.GRACE,600,"minecraft:feather");
        addCustom(ModEffects.SHELL_ARMOR,600,"bewitchment:coquina","bewitchment:coquina_smooth","bewitchment:coquina_shell");
        addCustom(ModEffects.BULK,600,"bewitchment:stone_ichor");
        addCustom(ModEffects.PAPER_SKIN,600,"minecraft:paper");
        addCustom(ModEffects.RHINO_HIDE,600,"minecraft:diamond");
        addCustom(ModEffects.CORROSION,600,"bewitchment:oil_of_vitriol");
        addCustom(ModEffects.HOLY_WATER,1,"bewitchment:white_sage","bewitchment:garlic","minecraft:allium");
        addCustom(ModEffects.REVEALING,1,"minecraft:ender_pearl","bewitchment:eye_of_old");
        addCustom(ModEffects.SINKING,400,"minecraft:gold_nugget","minecraft:iron_nugget","bewitchment:cold_iron_nugget");
        tag("forge:coquina",ModEffects.SHELL_ARMOR,600);
        tag("forge:crops/white_sage",ModEffects.HOLY_WATER,1);
        tag("forge:crops/garlic",ModEffects.HOLY_WATER,1);
        for(String metal:new String[]{"gold","iron","cold_iron","copper","tin","bronze","lead"})tag("forge:nuggets/"+metal,ModEffects.SINKING,400);
        ITEMS.computeIfAbsent("bewitchment:juniper_berries",k->new ArrayList<>()).add(()->new EffectInstance(ModEffects.MAGIC_WEAKNESS.get(),1200));
    }
    private static void addCustom(Supplier<Effect> effect,int duration,String... ids){
        for(String id:ids)ITEMS.computeIfAbsent(id,k->new ArrayList<>()).add(()->new EffectInstance(effect.get(),duration));
    }
    private static void tag(String id,Supplier<Effect> effect,int duration){
        ResourceLocation name=new ResourceLocation(id);ItemTags.createOptional(name);
        TAGS.put(name,()->new EffectInstance(effect.get(),duration));
    }
    private static boolean tagged(ItemStack stack,ResourceLocation name){return ItemTags.getAllTags().getTagOrEmpty(name).contains(stack.getItem());}
    private static void add(Effect effect,int duration,String... ids){for(String id:ids)ITEMS.computeIfAbsent(id,k->new ArrayList<>()).add(()->new EffectInstance(effect,duration));}
    public static String id(ItemStack stack){return stack.isEmpty()?"":String.valueOf(stack.getItem().getRegistryName());}
    public static List<EffectInstance> effects(ItemStack stack){
        List<EffectInstance> result=new ArrayList<>();List<Supplier<EffectInstance>> found=ITEMS.get(id(stack));if(found!=null)for(Supplier<EffectInstance> factory:found)result.add(factory.get());
        if(ItemTags.getAllTags().getTagOrEmpty(SAPLINGS.getName()).contains(stack.getItem()))result.add(new EffectInstance(Effects.ABSORPTION,700));
        if(ItemTags.getAllTags().getTagOrEmpty(SLIME.getName()).contains(stack.getItem()))result.add(new EffectInstance(Effects.MOVEMENT_SLOWDOWN,800));
        if(stack.getItem()==Items.SUGAR_CANE || ItemTags.getAllTags().getTagOrEmpty(CANE.getName()).contains(stack.getItem()))result.add(new EffectInstance(Effects.DIG_SPEED,600));
        TAGS.forEach((tag,factory)->{if(tagged(stack,tag))result.add(factory.get());});
        return new ArrayList<>(new LinkedHashSet<>(result));
    }
    public static boolean supported(ItemStack stack){String id=id(stack);return ITEMS.containsKey(id) || TAGS.keySet().stream().anyMatch(tag->tagged(stack,tag)) || ItemTags.getAllTags().getTagOrEmpty(SAPLINGS.getName()).contains(stack.getItem()) || ItemTags.getAllTags().getTagOrEmpty(SLIME.getName()).contains(stack.getItem()) || stack.getItem()==Items.SUGAR_CANE || ItemTags.getAllTags().getTagOrEmpty(CANE.getName()).contains(stack.getItem()) || modifier(id);}
    private static boolean modifier(String id){return id.equals("bewitchment:mandrake_root") || id.equals("bewitchment:ravens_feather") || id.equals("bewitchment:owlets_wing") || id.equals("minecraft:gunpowder") || id.equals("minecraft:redstone") || id.equals("minecraft:glowstone_dust");}
    public static ItemStack returned(ItemStack input){return returned(input,()->com.bewitchment.registry.ModItems.EMPTY_JAR.get());}
    public static ItemStack returned(ItemStack input,Supplier<Item> emptyJar){String id=id(input);if(id.equals("bewitchment:acacia_resin")||id.equals("bewitchment:birch_soul"))return new ItemStack(emptyJar.get());if(id.equals("minecraft:mushroom_stew"))return new ItemStack(Items.BOWL);return id.equals("bewitchment:snake_venom") || id.equals("bewitchment:bottle_of_blood")?new ItemStack(Items.GLASS_BOTTLE):ItemStack.EMPTY;}
    public static BrewPlan plan(List<ItemStack> ingredients,boolean alchemist,int leonards){
        List<EffectInstance> effects=new ArrayList<>();int red=0,glow=0;boolean gun=false,owl=false,hide=false;
        for(ItemStack stack:ingredients){if(!supported(stack))throw new IllegalArgumentException("Unsupported brew ingredient");effects.addAll(effects(stack));switch(id(stack)){case "minecraft:redstone":red++;break;case "minecraft:glowstone_dust":glow++;break;case "minecraft:gunpowder":gun=true;break;case "bewitchment:owlets_wing":owl=true;break;case "bewitchment:ravens_feather":hide=true;break;default:break;}}
        return new BrewPlan(effects,red,glow,alchemist,leonards,gun,owl,hide);
    }
    public static Set<String> ids(){return Collections.unmodifiableSet(ITEMS.keySet());}
    public static void init(){com.bewitchment.potion.TargetedBrewEffect.init();}
    private BrewIngredients(){}
}