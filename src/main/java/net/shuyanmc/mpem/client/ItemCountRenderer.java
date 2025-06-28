package net.shuyanmc.mpem.client;

import net.minecraft.world.entity.item.ItemEntity;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderNameTagEvent;
import net.shuyanmc.mpem.AsyncHandler;

@AsyncHandler
public class ItemCountRenderer {
    @SubscribeEvent
    public static void onNameTagRender(RenderNameTagEvent event) {
        if (event.getEntity() instanceof ItemEntity itemEntity) {
            if (itemEntity.getItem().getCount() > 1 && itemEntity.hasCustomName()) {
                // 提高文本显示优先级
                event.setContent(itemEntity.getCustomName());
            }
        }
    }
}