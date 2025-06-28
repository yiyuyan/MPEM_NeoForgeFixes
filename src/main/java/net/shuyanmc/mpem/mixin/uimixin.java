package net.shuyanmc.mpem.mixin;

import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.shuyanmc.mpem.FastLanguage;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LanguageSelectScreen.class)
public abstract class uimixin {
    @Inject(method = "onDone", at = @At(value = "HEAD"))
    public void notReloadResourcePacks(CallbackInfo ci) {
        FastLanguage.langReload = true;
    }
}
