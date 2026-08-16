/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.dedokok.DedTools;
import com.dedokok.events.meteor.CharTypedEvent;
import com.dedokok.events.meteor.KeyInputEvent;
import com.dedokok.gui.GuiKeyEvents;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.input.Input;
import com.dedokok.utils.misc.input.KeyAction;
import net.minecraft.client.KeyboardHandler;
import net.minecraft.client.Minecraft;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(KeyboardHandler.class)
public abstract class KeyboardHandlerMixin {
    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "keyPress", at = @At("HEAD"), cancellable = true)
    public void onKey(long handle, int action, KeyEvent event, CallbackInfo ci) {
        int modifiers = event.modifiers();
        if (event.key() != GLFW.GLFW_KEY_UNKNOWN) {
            // on Linux/X11 the modifier is not active when the key is pressed and still active when the key is released
            // https://github.com/glfw/glfw/issues/1630
            if (action == GLFW.GLFW_PRESS) {
                modifiers |= Input.getModifier(event.key());
            } else if (action == GLFW.GLFW_RELEASE) {
                modifiers &= ~Input.getModifier(event.key());
            }

            if (minecraft.gui.screen() instanceof WidgetScreen widgetScreen && action == GLFW.GLFW_REPEAT) {
                widgetScreen.keyRepeated(new KeyEvent(event.key(), event.scancode(), modifiers));
            }

            if (GuiKeyEvents.canUseKeys) {
                Input.setKeyState(event.key(), action != GLFW.GLFW_RELEASE);
                if (DedTools.EVENT_BUS.post(KeyInputEvent.get(new KeyEvent(event.key(), event.scancode(), modifiers), KeyAction.get(action))).isCancelled())
                    ci.cancel();
            }
        }
    }

    @Inject(method = "charTyped", at = @At("HEAD"), cancellable = true)
    private void onChar(long handle, CharacterEvent event, CallbackInfo ci) {
        if (Utils.canUpdate() && !minecraft.isPaused() && (minecraft.gui.screen() == null || minecraft.gui.screen() instanceof WidgetScreen)) {
            if (DedTools.EVENT_BUS.post(CharTypedEvent.get((char) event.codepoint())).isCancelled()) ci.cancel();
        }
    }
}
