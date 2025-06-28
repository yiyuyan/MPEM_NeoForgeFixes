package net.shuyanmc.mpem.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ClientboundBlockUpdatePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ClientboundBlockUpdatePacket.class)
public abstract class MixinBlockUpdatePacket {
    private static BlockPos lastSentPos;
    @Shadow
    private BlockPos pos;
/*
    @Inject(method = "write", at = @At("HEAD"), cancellable = true)
    private void onWrite(FriendlyByteBuf buf, CallbackInfo ci) {
        if (CoolConfig.filterRedundantBlockUpdates.get()) {
            // 过滤相同位置的重复方块更新
            if (pos.equals(lastSentPos)) {
                ci.cancel();
            }
            lastSentPos = pos;
        }
    }*/
}