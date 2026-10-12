package com.bewitchment.machine;

import com.bewitchment.block.GlyphBlock;
import com.bewitchment.item.GrimoireMagiaItem;
import com.bewitchment.registry.ModRituals;
import net.minecraft.block.*;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.crafting.IRecipe;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.tileentity.*;
import net.minecraft.util.*;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.server.ServerWorld;
import java.util.UUID;

public final class GlyphTile extends TileEntity implements ITickableTileEntity {
    public final RitualInventory inventory = new RitualInventory(this::changed);
    private ResourceLocation active;
    private UUID caster;
    private NatureBlessing blessing;
    private LandscapeRitual landscape;
    private final RitualConsumption consumption = new RitualConsumption();
    private final RitualClock clock = new RitualClock();
    public GlyphTile() { super(ModMachines.GLYPH.get()); }
    private Inventory inputs() {
        Inventory result = new Inventory(10);
        for (int i = 0; i < 10; i++) result.setItem(i, inventory.getStackInSlot(i).copy());
        return result;
    }
    private boolean circle(RitualRecipe recipe) {
        for (int ring = 0; ring < 3; ring++) {
            final int required = recipe.circleType(ring);
            if (required != -1 && !RitualCircle.matches(ring, worldPosition, p -> level.hasChunkAt(p)
                    && level.getBlockState(p).is(ModRituals.GLYPH.get())
                    && RitualCircle.accepts(required, level.getBlockState(p).getValue(GlyphBlock.TYPE)))) return false;
        }
        return true;
    }
    private boolean pay(PlayerEntity player, int amount) {
        if (amount == 0) return true;
        AltarTile altar = AltarTile.nearest((ServerWorld)level, worldPosition);
        return altar != null && altar.tryDrainMagic(amount) || GrimoireMagiaItem.drain(player, amount);
    }
    private void message(PlayerEntity player, String key) { player.displayClientMessage(new TranslationTextComponent("ritual.bewitchment." + key), true); }
    public void interact(PlayerEntity player) {
        if (level == null || level.isClientSide) return;
        if (landscape != null) {
            landscape.stop(); landscape = null; changed(); message(player, "landscape_stopped"); return;
        }
        if (blessing != null) {
            blessing.stop(); blessing = null; changed(); message(player, "blessing_stopped"); return;
        }
        if (active != null) {
            boolean consumed = consumption.consumed();
            cancel(); message(player, consumed ? "consumed_cancelled" : "cancelled"); return;
        }
        if (player.isShiftKeyDown()) {
            for (int i = 9; i >= 0; i--) if (!inventory.getStackInSlot(i).isEmpty()) {
                Block.popResource(level, worldPosition, inventory.extractItem(i, 1, false)); return;
            }
            return;
        }
        ItemStack held = player.getMainHandItem();
        if (!held.isEmpty()) {
            for (int i = 0; i < 10; i++) if (inventory.getStackInSlot(i).isEmpty()) {
                ItemStack offered = held.copy(); offered.setCount(1);
                if (inventory.insertItem(i, offered, false).isEmpty()) held.shrink(1);
                return;
            }
            message(player, "full"); return;
        }
        activate(player, null);
    }
    /** A death trigger must not insert held items, cancel active work or bypass permissions. */
    public void onChickenDeath(PlayerEntity player) { onSacrifice(player, RitualEffect.CONJURE_IMP); }
    public void onSacrifice(PlayerEntity player, RitualEffect effect) {
        if (effect != RitualEffect.CONJURE_IMP && effect != RitualEffect.CONJURE_DEMON && effect != RitualEffect.CONJURE_LEONARD && effect != RitualEffect.CONJURE_BAPHOMET) return;
        if (level == null || level.isClientSide || active != null || landscape != null || blessing != null
                || !player.isAlive() || player.isSpectator() || !player.abilities.mayBuild || !level.mayInteract(player, worldPosition)) return;
        activate(player, effect);
    }
    private void activate(PlayerEntity player, RitualEffect sacrifice) {
        if (RitualInhibition.blocked(player)) { message(player, "inhibited"); return; }
        java.util.List<RitualRecipe> candidates = new java.util.ArrayList<>(level.getRecipeManager().getRecipesFor(RitualRecipe.TYPE, inputs(), level));
        if (sacrifice != null) {
            candidates.removeIf(recipe -> recipe.effect != sacrifice);
            if (candidates.isEmpty()) return;
        }
        if (candidates.isEmpty()) { message(player, "ingredients"); return; }
        RitualRecipe recipe = candidates.stream().filter(this::circle).sorted(java.util.Comparator.comparing(r -> r.getId().toString())).findFirst().orElse(null);
        if (recipe == null) { message(player, "circle"); return; }
        String failure = recipe.effect.failure((ServerWorld)level);
        if (failure != null) { message(player, failure); return; }
        if (recipe.effect.landscape() && (!player.abilities.mayBuild || LandscapeRitual.resolve((ServerWorld)level, LandscapeRitual.target(recipe.effect, inputs())) == null)) {
            message(player, "landscape_target"); return;
        }
        if (recipe.effect == RitualEffect.NATURES_BLESSING && !player.abilities.mayBuild) {
            message(player, "blessing_permission"); return;
        }
        if (recipe.effect == RitualEffect.TELEPORT && RitualTeleport.prepare((ServerWorld)level, worldPosition, inputs()) == null) {
            message(player, "teleport_blocked"); return;
        }
        if (recipe.effect.conjuration() && RitualConjuration.prepare(recipe.effect, (ServerWorld)level, worldPosition) == null) {
            message(player, "conjuration_blocked"); return;
        }
        RitualDrawing.Prepared drawing = recipe.effect.instant()
                ? RitualDrawing.prepare((ServerWorld)level, worldPosition, player, recipe.effect.drawingRing()) : null;
        if (recipe.effect.instant() && drawing == null) { message(player, "drawing"); return; }
        int cost = RitualPower.startingCost(recipe.startingPower, com.bewitchment.combat.AccessoryEffects.wearing(player, com.bewitchment.item.LegacyAccessoryItem.Kind.HECATE));
        if (!pay(player, cost)) { message(player, "power"); return; }
        if (drawing != null) {
            if (!drawing.apply()) { message(player, "drawing_failed"); return; }
            for (int i = 0; i < 10; i++) inventory.extractItem(i, 1, false);
            level.playSound(null, worldPosition, ModRituals.CHALK_SOUND.get(), SoundCategory.BLOCKS, 1, 1);
            message(player, "finished"); changed(); return;
        }
        active = recipe.getId(); caster = player.getUUID(); clock.reset();
        consumption.start(recipe, inventory);
        changed();
        level.playSound(null, worldPosition, SoundEvents.FIRE_EXTINGUISH, SoundCategory.BLOCKS, 1, 1);
        message(player, recipe.effect.consumesOnStart() ? "consumed_started" : "started");
    }
    public void cancel() {
        if (level == null || level.isClientSide) return;
        if (landscape != null) landscape.stop();
        landscape = null;
        if (blessing != null) blessing.stop();
        blessing = null;
        active = null; caster = null; consumption.reset(); clock.reset();
        for (int i = 0; i < 10; i++) Block.popResource(level, worldPosition, inventory.extractItem(i, 1, false));
        changed();
    }
    @Override public void tick() {
        if (level == null || level.isClientSide) return;
        if (landscape != null) {
            PlayerEntity owner = level.getPlayerByUUID(landscape.owner());
            if (owner != null) {
                LandscapeRitual working = landscape;
                working.tick((ServerWorld)level, owner); setChanged();
                if (landscape == working && working.finished()) { landscape = null; message(owner, "finished"); changed(); }
            }
            return;
        }
        if (blessing != null) {
            PlayerEntity owner = level.getPlayerByUUID(blessing.owner());
            if (owner != null) {
                NatureBlessing working = blessing;
                working.tick((ServerWorld)level, owner);
                setChanged();
                if (blessing == working && working.finished()) { blessing = null; message(owner, "finished"); changed(); }
            }
            return;
        }
        if (active == null) return;
        IRecipe<?> raw = level.getRecipeManager().byKey(active).orElse(null);
        PlayerEntity player = caster == null ? null : level.getPlayerByUUID(caster);
        if (!(raw instanceof RitualRecipe) || player == null || !player.isAlive() || !circle((RitualRecipe)raw)) {
            cancel(); return;
        }
        RitualRecipe recipe = (RitualRecipe)raw;
        if (recipe.effect.instant() || !consumption.accepts(recipe) || (!recipe.effect.consumesOnStart() && !recipe.matches(inputs(), level))
                || (recipe.effect.consumesOnStart() && recipe.effect.failure((ServerWorld)level) != null)) {
            cancel(); return;
        }
        RitualClock.Step step = recipe.effect.continuous()
                ? clock.advanceContinuous(recipe.runningPower, amount -> pay(player, amount))
                : clock.advance(recipe.seconds, recipe.runningPower, amount -> pay(player, amount));
        setChanged();
        if (step == RitualClock.Step.NO_POWER) { message(player, recipe.effect.consumesOnStart() ? "consumed_power" : "power"); cancel(); return; }
        if (recipe.effect.continuous()) ContinuousRitualEffects.tick(recipe.effect, (ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.HUNGRY_FLAMES) HungryFlames.tick((ServerWorld)level, worldPosition);
        if (recipe.effect.conjuration()) RitualConjuration.particles(recipe.effect, (ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.LESSER_HELLMOUTH) LesserHellmouth.particles((ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.WEDNESDAY) WednesdayRitual.tick((ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.SPIRITUAL_RIFT) SpiritualRift.particles((ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.GREATER_HELLMOUTH) GreaterHellmouth.particles((ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.HELLMOUTH) Hellmouth.particles((ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.CALL_OF_THE_WILD) CallOfTheWild.particles((ServerWorld)level, worldPosition);
        if (recipe.effect == RitualEffect.BIOME_SHIFT)
            ((ServerWorld)level).sendParticles(ParticleTypes.ENCHANT, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, 15, .5, .5, .5, .1);
        if (recipe.effect == RitualEffect.NATURES_BLESSING)
            ((ServerWorld)level).sendParticles(ParticleTypes.BUBBLE, worldPosition.getX() + .5, worldPosition.getY() + .5, worldPosition.getZ() + .5, 15, .5, .5, .5, .1);
        if (step != RitualClock.Step.WAITING) {
            ((ServerWorld)level).sendParticles(ParticleTypes.END_ROD, worldPosition.getX() + .5, worldPosition.getY() + .2, worldPosition.getZ() + .5, 2, .2, .2, .2, .01);
            if (step == RitualClock.Step.FINISHED) {
                ResourceLocation targetBiome = recipe.effect.landscape() ? LandscapeRitual.target(recipe.effect, inputs()) : null;
                if (recipe.effect.landscape() && (!player.abilities.mayBuild || LandscapeRitual.resolve((ServerWorld)level, targetBiome) == null)) {
                    message(player, "landscape_target"); cancel(); return;
                }
                if (recipe.effect == RitualEffect.NATURES_BLESSING && !player.abilities.mayBuild) {
                    message(player, "blessing_permission"); cancel(); return;
                }
                java.util.List<net.minecraft.entity.MobEntity> hellmouth = recipe.effect == RitualEffect.LESSER_HELLMOUTH
                        ? LesserHellmouth.prepare((ServerWorld)level, worldPosition) : recipe.effect == RitualEffect.HELLMOUTH ? Hellmouth.prepare((ServerWorld)level, worldPosition)
                        : recipe.effect == RitualEffect.GREATER_HELLMOUTH ? GreaterHellmouth.prepare((ServerWorld)level, worldPosition) : null;
                if (hellmouth != null && hellmouth.isEmpty()) { message(player, "hellmouth_failed"); cancel(); return; }
                java.util.List<net.minecraft.entity.MobEntity> wild = recipe.effect == RitualEffect.CALL_OF_THE_WILD
                        ? CallOfTheWild.prepare((ServerWorld)level, worldPosition) : null;
                if (wild != null && wild.isEmpty()) { message(player, "wild_failed"); cancel(); return; }
                net.minecraft.entity.MobEntity summoned = recipe.effect.conjuration()
                        ? RitualConjuration.prepare(recipe.effect, (ServerWorld)level, worldPosition) : null;
                if (recipe.effect.conjuration() && summoned == null) {
                    message(player, "conjuration_failed"); cancel(); return;
                }
                java.util.List<net.minecraft.entity.MobEntity> rift = recipe.effect == RitualEffect.SPIRITUAL_RIFT
                        ? SpiritualRift.prepare((ServerWorld)level, worldPosition) : null;
                if (rift != null && rift.isEmpty()) { message(player, "rift_failed"); cancel(); return; }
                java.util.List<net.minecraft.entity.MobEntity> wednesday = recipe.effect == RitualEffect.WEDNESDAY
                        ? WednesdayRitual.prepare((ServerWorld)level, worldPosition) : null;
                if (wednesday != null && wednesday.isEmpty()) { message(player, "wednesday_failed"); cancel(); return; }
                Inventory offerings = inputs();
                RitualTeleport.Plan teleport = recipe.effect == RitualEffect.TELEPORT
                        ? RitualTeleport.prepare((ServerWorld)level, worldPosition, offerings) : null;
                if (recipe.effect == RitualEffect.TELEPORT && teleport == null) {
                    message(player, "teleport_failed"); cancel(); return;
                }
                ItemStack returned = teleport == null ? ItemStack.EMPTY : RitualTeleport.returnedOffering(offerings, player.isCreative());
                ItemStack output = recipe.assemble(offerings);
                // Clear storage before spawning output, so interruption cannot duplicate it.
                active = null; caster = null; consumption.reset(); clock.reset();
                NonNullList<ItemStack> consumed = inventory.takeAll();
                if (wednesday != null && WednesdayRitual.spawn((ServerWorld)level, wednesday) == 0) {
                    for (ItemStack stack : consumed) Block.popResource(level, worldPosition, stack);
                    message(player, "wednesday_failed"); changed(); return;
                }
                if (rift != null && SpiritualRift.spawn((ServerWorld)level, rift) == 0) {
                    for (ItemStack stack : consumed) Block.popResource(level, worldPosition, stack);
                    message(player, "rift_failed"); changed(); return;
                }
                if (wild != null && CallOfTheWild.spawn((ServerWorld)level, wild) == 0) {
                    for (ItemStack stack : consumed) Block.popResource(level, worldPosition, stack);
                    message(player, "wild_failed"); changed(); return;
                }
                if (hellmouth != null && (recipe.effect == RitualEffect.GREATER_HELLMOUTH ? GreaterHellmouth.spawn((ServerWorld)level, hellmouth) : recipe.effect == RitualEffect.HELLMOUTH ? Hellmouth.spawn((ServerWorld)level, hellmouth) : LesserHellmouth.spawn((ServerWorld)level, hellmouth)) == 0) {
                    for (ItemStack stack : consumed) Block.popResource(level, worldPosition, stack);
                    message(player, "hellmouth_failed"); changed(); return;
                }
                if (summoned != null && !RitualConjuration.spawn(recipe.effect, (ServerWorld)level, worldPosition, summoned)) {
                    for (ItemStack stack : consumed) Block.popResource(level, worldPosition, stack);
                    message(player, "conjuration_failed"); changed(); return;
                }
                if (recipe.effect.cleansing()) com.bewitchment.combat.PlayerCurses.cleanse(player, recipe.effect == RitualEffect.LESSER_CURSE_CLEANSING);
                recipe.effect.complete((ServerWorld)level);
                if (recipe.effect.landscape()) {
                    landscape = new LandscapeRitual(worldPosition, player.getUUID(), targetBiome, recipe.effect == RitualEffect.SOWING_SALT);
                    message(player, "landscape_started"); changed(); return;
                }
                if (recipe.effect == RitualEffect.NATURES_BLESSING) {
                    blessing = new NatureBlessing(worldPosition, player.getUUID(), 0);
                    message(player, "blessing_started"); changed(); return;
                }
                if (teleport != null) {
                    inventory.setStackInSlot(0, returned);
                    teleport.move();
                }
                if (recipe.effect == RitualEffect.FRENZIED_GROWTH) FrenziedGrowth.complete((ServerWorld)level, worldPosition, player);
                if (!output.isEmpty()) Block.popResource(level, worldPosition, output);
                level.playSound(null, worldPosition, SoundEvents.EXPERIENCE_ORB_PICKUP, SoundCategory.BLOCKS, .7F, .7F);
                message(player, "finished"); changed();
            }
        }
    }
    private void changed() {
        setChanged();
        if (level != null && !level.isClientSide) level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
    }
    @Override public CompoundNBT save(CompoundNBT tag) {
        super.save(tag); tag.put("Inventory", inventory.serializeNBT());
        if (active != null) tag.putString("Ritual", active.toString()); else tag.remove("Ritual");
        if (caster != null) tag.putUUID("Caster", caster); else tag.remove("Caster");
        consumption.save(tag, active != null);
        if (blessing != null) tag.put("NatureBlessing", blessing.save()); else tag.remove("NatureBlessing");
        if (landscape != null) tag.put("Landscape", landscape.save()); else tag.remove("Landscape");
        tag.putInt("Ticks", clock.ticks()); return tag;
    }
    @Override public void load(BlockState state, CompoundNBT tag) {
        super.load(state, tag); inventory.deserializeNBT(tag.getCompound("Inventory"));
        active = readRitualId(tag); caster = tag.hasUUID("Caster") ? tag.getUUID("Caster") : null;
        consumption.load(tag, active != null);
        clock.load(tag.getInt("Ticks"));
        blessing = tag.contains("NatureBlessing", 10) ? NatureBlessing.read(worldPosition, tag.getCompound("NatureBlessing")) : null;
        if (blessing != null) { active = null; caster = null; consumption.reset(); }
        landscape = tag.contains("Landscape", 10) ? LandscapeRitual.read(worldPosition, tag.getCompound("Landscape")) : null;
        if (landscape != null) { blessing = null; active = null; caster = null; consumption.reset(); }
        if (active == null) clock.reset();
    }
    static ResourceLocation readRitualId(CompoundNBT tag) {
        String id = tag.getString("Ritual");
        return id.isEmpty() ? null : ResourceLocation.tryParse(id);
    }
    @Override public CompoundNBT getUpdateTag() { return save(new CompoundNBT()); }
    @Override public SUpdateTileEntityPacket getUpdatePacket() { return new SUpdateTileEntityPacket(worldPosition, 0, getUpdateTag()); }
    @Override public void onDataPacket(NetworkManager network, SUpdateTileEntityPacket packet) { load(getBlockState(), packet.getTag()); }
}
