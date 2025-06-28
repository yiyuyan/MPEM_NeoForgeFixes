package net.shuyanmc.mpem.mixin;

import net.minecraft.world.entity.vehicle.Boat;
import net.shuyanmc.mpem.AsyncHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
@AsyncHandler
@Mixin(Boat.class)
public abstract class MixinBoat {

    /**
     * 注入到checkFallDamage方法头部，在计算摔落伤害前重置摔落距离
     * 
     * @param y 垂直移动距离
     * @param onGround 是否在地面
     * @param state 方块状态
     * @param pos 位置
     * @param ci 回调信息
     */
    @Inject(
        method = "checkFallDamage",
        at = @At("HEAD"),
        cancellable = true
    )
    private void onCheckFallDamage(double y, boolean onGround, net.minecraft.world.level.block.state.BlockState state, net.minecraft.core.BlockPos pos, CallbackInfo ci) {
        Boat boat = (Boat)(Object)this;
        // 重置船的摔落距离为0
        boat.fallDistance = 0.0F;
        
        // 可选：完全取消后续伤害计算（更彻底）
        ci.cancel();
    }
}