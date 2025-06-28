package net.shuyanmc.mpem.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.CraftingScreen;
import net.minecraft.client.gui.screens.options.LanguageSelectScreen;
import net.minecraft.client.resources.language.LanguageManager;
import net.minecraft.server.packs.resources.ResourceManager;
import net.shuyanmc.mpem.AsyncHandler;
import net.shuyanmc.mpem.FastLanguage;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.concurrent.CompletableFuture;

@AsyncHandler
@Mixin(Minecraft.class)
public abstract class mcmixinx {
    @Shadow
    @Nullable
    public Screen screen;
    @Shadow
    @Final
    private LanguageManager languageManager;

    @Shadow
    public abstract ResourceManager getResourceManager();

    @Inject(method = "reloadResourcePacks()Ljava/util/concurrent/CompletableFuture;", at = @At("HEAD"), cancellable = true)
    public void reloadRes(CallbackInfoReturnable<CompletableFuture<Void>> cir) {
        if (FastLanguage.langReload) {
            this.languageManager.onResourceManagerReload(this.getResourceManager());
            FastLanguage.langReload = false;
            cir.setReturnValue(null);
            cir.cancel();
        }
    }

    @Inject(method = "setOverlay", at = @At("HEAD"), cancellable = true)
    public void reloadRes(Overlay overlay, CallbackInfo ci) {
        if (this.screen instanceof LanguageSelectScreen || this.screen instanceof CraftingScreen) ci.cancel();
    }
}
