package com.bewitchment.combat;

import com.bewitchment.machine.*;
import net.minecraft.entity.item.ItemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.*;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.eventbus.api.*;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid="bewitchment")
public final class DiggingFortunes {
    @SubscribeEvent(priority=EventPriority.LOWEST)
    public static void breakBlock(BlockEvent.BreakEvent event) {
        PlayerEntity player = event.getPlayer();
        if (event.isCanceled() || !(event.getWorld() instanceof ServerWorld) || !PlayerFortunes.eligible(player)) return;
        ServerWorld world = (ServerWorld)event.getWorld();
        FortuneDispatch.executeIf(player.getUUID(), new FortuneDispatch.Store() {
            public FortuneData read() { return PlayerFortunes.read(player); }
            public void write(FortuneData data) { PlayerFortunes.write(player, data); }
        }, kind -> DiggingFortuneRules.eligible(kind, event.getState().getBlock(), world.dimension() == World.NETHER), kind -> {
            ResourceLocation table = new ResourceLocation("bewitchment", "chests/" + (kind == FortuneData.Kind.TREASURE ? "materials" : "nether_materials"));
            Vector3d origin = Vector3d.atCenterOf(event.getPos());
            LootContext context = new LootContext.Builder(world).withRandom(player.getRandom()).withLuck(player.getLuck())
                    .withParameter(LootParameters.ORIGIN, origin).withParameter(LootParameters.THIS_ENTITY, player).create(LootParameterSets.CHEST);
            for (ItemStack stack : world.getServer().getLootTables().get(table).getRandomItems(context)) {
                if (stack.isEmpty()) continue;
                ItemEntity item = new ItemEntity(world, origin.x, origin.y, origin.z, stack);
                item.setNoPickUpDelay();
                world.addFreshEntity(item);
            }
        });
    }
    private DiggingFortunes() {}
}
