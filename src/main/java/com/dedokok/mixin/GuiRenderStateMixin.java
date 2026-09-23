package com.dedokok.mixin;

import net.minecraft.client.renderer.state.gui.BlitRenderState;
import net.minecraft.client.renderer.state.gui.GuiElementRenderState;
import net.minecraft.client.renderer.state.gui.GuiItemRenderState;
import net.minecraft.client.renderer.state.gui.GuiRenderState;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.util.List;

@Mixin(GuiRenderState.class)
public abstract class GuiRenderStateMixin {

    private static Object getField(Object target, String name) {
        try {
            Field f = GuiRenderState.class.getDeclaredField(name);
            f.setAccessible(true);
            return f.get(target);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    @Inject(method = "addItem", at = @At("RETURN"))
    private void logItem(GuiItemRenderState itemState, CallbackInfo ci) {
        List<?> strata = (List<?>) getField(this, "strata");
        Object current = getField(this, "current");
//        System.out.println("[ITEM] stratumIdx=" + strata.indexOf(current)
//                + " node=" + System.identityHashCode(current)
//                + " bounds=" + itemState.bounds());
    }

    @Inject(method = "addGuiElement", at = @At("RETURN"))
    private void logElement(GuiElementRenderState elementState, CallbackInfo ci) {
        List<?> strata = (List<?>) getField(this, "strata");
        Object current = getField(this, "current");
//        System.out.println("[ELEMENT] stratumIdx=" + strata.indexOf(current)
//                + " node=" + System.identityHashCode(current)
//                + " bounds=" + elementState.bounds());
    }

    @Inject(method = "addBlitToCurrentLayer", at = @At("HEAD"))
    private void logItemBlit(BlitRenderState blitState, CallbackInfo ci) {
        List<?> strata = (List<?>) getField(this, "strata");
        Object current = getField(this, "current");
//        System.out.println("[ITEM-BLIT] stratumIdx=" + strata.indexOf(current)
//                + " node=" + System.identityHashCode(current));
    }
}