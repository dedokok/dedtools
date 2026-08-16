/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.llamalad7.mixinextras.injector.ModifyExpressionValue;
import com.llamalad7.mixinextras.injector.ModifyReturnValue;
import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.sugar.Local;
import com.dedokok.DedTools;
import com.dedokok.events.entity.player.DoAttackEvent;
import com.dedokok.events.entity.player.DoItemUseEvent;
import com.dedokok.events.entity.player.ItemUseCrosshairTargetEvent;
import com.dedokok.events.game.GameLeftEvent;
import com.dedokok.events.game.ResolutionChangedEvent;
import com.dedokok.events.game.ResourcePacksReloadedEvent;
import com.dedokok.events.world.TickEvent;
import com.dedokok.mixininterface.IMinecraft;
import com.dedokok.mixininterface.IVec3;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.CPSUtils;
import com.dedokok.utils.misc.MeteorStarscript;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.util.profiling.Profiler;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.HitResult;
import org.jspecify.annotations.Nullable;
import org.meteordev.starscript.Script;
import org.objectweb.asm.Opcodes;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.concurrent.CompletableFuture;

import static com.dedokok.DedTools.mc;

@Mixin(value = Minecraft.class, priority = 1001)
public abstract class MinecraftMixin implements IMinecraft {
    @Unique
    private boolean startUseItemCalled;
    @Unique
    private boolean rightClick;
    @Unique
    private long lastTime;
    @Unique
    private boolean firstFrame;

    @Shadow
    public ClientLevel level;
    @Shadow
    @Final
    public Options options;

    @Shadow
    protected abstract void startUseItem();

    @Shadow
    @Nullable
    public MultiPlayerGameMode gameMode;

    @Shadow
    private int rightClickDelay;

    @Shadow
    @Nullable
    public LocalPlayer player;

    @Shadow
    protected abstract void continueAttack(boolean down);

    @Unique
    private boolean freecamSet = false;

    @Final
    @Shadow
    public GameRenderer gameRenderer;

    @Shadow
    protected abstract void pick(float partialTicks);

    @Shadow
    public abstract Entity getCameraEntity();

    @Inject(method = "<init>", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        DedTools.INSTANCE.onInitializeClient();
        firstFrame = true;
    }

    @Inject(at = @At("HEAD"), method = "tick")
    private void onPreTick(CallbackInfo ci) {
        //OnlinePlayers.update();
        if (mc.options.keyUse.isDown()) {
            System.out.println(
                    "[TICK] keyUse=true, screen=" + mc.gui.screen()
            );
        }
        startUseItemCalled = false;

        Profiler.get().push(DedTools.MOD_ID + "_pre_update");
        DedTools.EVENT_BUS.post(TickEvent.Pre.get());
        Profiler.get().pop();

        if (rightClick && !startUseItemCalled && gameMode != null) startUseItem();
        rightClick = false;
    }

    @Inject(at = @At("TAIL"), method = "tick")
    private void onTick(CallbackInfo ci) {
        Profiler.get().push(DedTools.MOD_ID + "_post_update");
        DedTools.EVENT_BUS.post(TickEvent.Post.get());
        Profiler.get().pop();
    }

    @Inject(method = "startAttack", at = @At("HEAD"), cancellable = true)
    private void onStartAttack(CallbackInfoReturnable<Boolean> cir) {
        CPSUtils.onAttack();
        if (DedTools.EVENT_BUS.post(DoAttackEvent.get()).isCancelled()) cir.cancel();
    }

    @Inject(method = "startUseItem", at = @At("HEAD"))
    private void onStartUseItem(CallbackInfo ci) {
        startUseItemCalled = true;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;ZZ)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, boolean keepResourcePacks, boolean stopSound, CallbackInfo ci) {
        if (level != null) {
            DedTools.EVENT_BUS.post(GameLeftEvent.get());
        }
    }



    @Inject(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/InteractionHand;values()[Lnet/minecraft/world/InteractionHand;"), cancellable = true)
    private void onStartUseItemBeforeHands(CallbackInfo ci) {
        if (DedTools.EVENT_BUS.post(DoItemUseEvent.get()).isCancelled()) ci.cancel();
    }

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "FIELD", target = "Lnet/minecraft/client/Minecraft;hitResult:Lnet/minecraft/world/phys/HitResult;", ordinal = 1, opcode = Opcodes.GETFIELD))
    private HitResult startUseItemMinecraftClientCrosshairTargetProxy(HitResult original) {
        return DedTools.EVENT_BUS.post(ItemUseCrosshairTargetEvent.get(original)).target;
    }

    @ModifyReturnValue(method = "reloadResourcePacks(ZLnet/minecraft/client/GameLoadCookie;)Ljava/util/concurrent/CompletableFuture;", at = @At("RETURN"))
    private CompletableFuture<Void> onReloadResourcePacksNewCompletableFuture(CompletableFuture<Void> original) {
        return original.thenRun(() -> DedTools.EVENT_BUS.post(ResourcePacksReloadedEvent.get()));
    }

    @ModifyArg(method = "updateTitle", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/platform/Window;setTitle(Ljava/lang/String;)V"))
    private String setTitle(String original) {
        if (Config.get() == null || !Config.get().customWindowTitle.get()) return original;

        String customTitle = Config.get().customWindowTitleText.get();
        Script script = MeteorStarscript.compile(customTitle);

        if (script != null) {
            String title = MeteorStarscript.run(script);
            if (title != null) customTitle = title;
        }

        return customTitle;
    }



    @Inject(method = "resizeGui", at = @At("TAIL"))
    private void onResizeGui(CallbackInfo ci) {
        DedTools.EVENT_BUS.post(ResolutionChangedEvent.get());
    }

    // Time delta

    @Inject(method = "runTick", at = @At("HEAD"))
    private void onRunTick(CallbackInfo ci) {
        long time = System.currentTimeMillis();

        if (firstFrame) {
            lastTime = time;
            firstFrame = false;
        }

        Utils.frameTime = (time - lastTime) / 1000.0;
        lastTime = time;
    }

    // Multitask

    @ModifyExpressionValue(method = "startUseItem", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/MultiPlayerGameMode;isDestroying()Z"))
    private boolean startUseItemModifyIsBreakingBlock(boolean original) {
        return original;
    }

    @ModifyExpressionValue(method = "continueAttack", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z"))
    private boolean continueAttackModifyIsUsingItem(boolean original) {
        return original;
    }

    @ModifyExpressionValue(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;isUsingItem()Z", ordinal = 0))
    private boolean handleKeybindsModifyIsUsingItem(boolean original) {
        return original;
    }





    // faster inputs

    @Unique
    private boolean isBreaking = false;





    // Interface

    @Override
    public void meteor$rightClick() {
        rightClick = true;
    }


}
