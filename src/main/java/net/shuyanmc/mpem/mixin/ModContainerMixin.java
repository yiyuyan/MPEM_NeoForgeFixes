package net.shuyanmc.mpem.mixin;

import net.neoforged.fml.ModContainer;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.Marker;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ModContainer.class)
public class ModContainerMixin {
    @Redirect(method = "acceptEvent(Lnet/neoforged/bus/api/Event;)V",at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Lorg/apache/logging/log4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"))
    public void event(Logger instance, Marker marker, String s, Object o, Object o2, Object o3){
        if(!(o3 instanceof NoClassDefFoundError) &&!(o3 instanceof ClassNotFoundException)){
            instance.error(marker,s,o,o2,o3);
        }
    }

    @Redirect(method = "acceptEvent(Lnet/neoforged/bus/api/EventPriority;Lnet/neoforged/bus/api/Event;)V",at = @At(value = "INVOKE", target = "Lorg/apache/logging/log4j/Logger;error(Lorg/apache/logging/log4j/Marker;Ljava/lang/String;Ljava/lang/Object;Ljava/lang/Object;Ljava/lang/Object;)V"))
    public void event2(Logger instance, Marker marker, String s, Object o, Object o2, Object o3){
        if(!(o3 instanceof NoClassDefFoundError) &&!(o3 instanceof ClassNotFoundException)){
            instance.error(marker,s,o,o2,o3);
        }
    }
}
