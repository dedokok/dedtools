/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.dedokok.events.game.OpenScreenEvent;
import com.dedokok.gui.WidgetScreen;
import com.dedokok.utils.misc.input.Input;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.dedokok.DedTools;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Modules;

import com.dedokok.utils.Utils;
import com.dedokok.utils.misc.text.MeteorClickEvent;
import com.dedokok.utils.misc.text.RunnableClickEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.network.chat.ClickEvent;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import com.dedokok.DedTools;

import static org.lwjgl.glfw.GLFW.*;

@Mixin(value = Screen.class, priority = 500) // needs to be before baritone
public abstract class ScreenMixin {
    Minecraft mc = DedTools.mc;
    @Unique
    private static boolean meteor$isArray(int key) {
        return key == GLFW_KEY_RIGHT || key == GLFW_KEY_LEFT || key == GLFW_KEY_DOWN || key == GLFW_KEY_UP;
    }





}
