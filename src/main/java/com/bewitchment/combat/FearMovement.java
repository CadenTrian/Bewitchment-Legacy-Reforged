package com.bewitchment.combat;

import com.bewitchment.Bewitchment;
import com.bewitchment.registry.ModEffects;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.network.play.server.SEntityVelocityPacket;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import java.util.Random;

/** Legacy random horizontal movement, applied once on the authoritative server. */
@Mod.EventBusSubscriber(modid = Bewitchment.MOD_ID)
public final class FearMovement {
    public static Vector3d jitter(Vector3d motion, Random random) {
        return motion.add(random.nextGaussian() / 4.0, 0, random.nextGaussian() / 4.0);
    }
    @SubscribeEvent public static void tick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayerEntity)
                || !event.player.isAlive() || !event.player.hasEffect(ModEffects.FEAR.get())) return;
        ServerPlayerEntity player = (ServerPlayerEntity)event.player;
        player.setDeltaMovement(jitter(player.getDeltaMovement(), player.getRandom()));
        player.connection.send(new SEntityVelocityPacket(player));
    }
    private FearMovement() {}
}
