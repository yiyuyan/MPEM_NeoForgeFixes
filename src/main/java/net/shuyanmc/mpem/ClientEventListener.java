package net.shuyanmc.mpem;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

@OnlyIn(Dist.CLIENT)
public class ClientEventListener {

    // 可以添加客户端特定的事件监听
    @SubscribeEvent
    public void onRenderLevelStage(RenderLevelStageEvent event) {
        // 可在此处添加调试渲染
    }
}
