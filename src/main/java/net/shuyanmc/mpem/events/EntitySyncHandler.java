package net.shuyanmc.mpem.events;

import net.minecraft.server.level.ServerPlayer;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.config.CoolConfig;

@AsyncHandler
@EventBusSubscriber
public class EntitySyncHandler {

    @SubscribeEvent
    public static void onEntityJoinLevel(EntityJoinLevelEvent event) {
        if (!CoolConfig.reduceEntityUpdates.get()) return;

        if (event.getEntity() instanceof ServerPlayer player) {
            if (player.distanceToSqr(event.getEntity()) > 4096.0) {
                event.setCanceled(true);
            }
        }
    }
}