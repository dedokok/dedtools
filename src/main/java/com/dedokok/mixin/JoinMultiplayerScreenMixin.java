/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.mixin;

import com.dedokok.gui.GuiThemes;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Modules;

import com.dedokok.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.multiplayer.JoinMultiplayerScreen;
import net.minecraft.network.chat.Component;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.dedokok.DedTools.mc;

@Mixin(JoinMultiplayerScreen.class)
public abstract class JoinMultiplayerScreenMixin extends Screen {
    @Unique
    private int textColor1;
    @Unique
    private int textColor2;

    @Unique
    private String loggedInAs;
    @Unique
    private int loggedInAsLength;

    @Unique
    private Button accounts;

    @Unique
    private Button proxies;

    @Unique
    private static final int BUTTON_WIDTH = 75;
    @Unique
    private static final int BUTTON_HEIGHT = 20;
    @Unique
    private static final int MARGIN = 3;
    @Unique
    private static final int GAP = 2;

    public JoinMultiplayerScreenMixin(Component title) {
        super(title);
    }

    @Inject(method = "repositionElements", at = @At("TAIL"))
    private void onInit(CallbackInfo ci) {
        textColor1 = Color.fromRGBA(255, 255, 255, 255);
        textColor2 = Color.fromRGBA(175, 175, 175, 255);

        loggedInAs = "Logged in as ";
        loggedInAsLength = font.width(loggedInAs);


        Config config = Config.get();
        Config.ButtonPosition accountPos = config.accountButtonAnchor.get();
        Config.ButtonPosition proxiesPos = config.proxiesButtonAnchor.get();
        boolean accountsVisible = accountPos != Config.ButtonPosition.Hidden;
        boolean proxiesVisible = proxiesPos != Config.ButtonPosition.Hidden;

        accounts.visible = accountsVisible;
        proxies.visible = proxiesVisible;

        positionButton(accounts, accountPos, proxiesVisible && proxiesPos == accountPos, true);
        positionButton(proxies, proxiesPos, accountsVisible && accountPos == proxiesPos, false);
    }

    @Unique
    private void positionButton(Button button, Config.ButtonPosition anchor, boolean sharingCorner, boolean isAccounts) {
        int leftOffset  = sharingCorner && isAccounts  ? BUTTON_WIDTH + GAP : 0;
        int rightOffset = sharingCorner && !isAccounts ? BUTTON_WIDTH + GAP : 0;

        switch (anchor) {
            case TopRight    -> button.setPosition(this.width  - MARGIN - BUTTON_WIDTH - rightOffset, MARGIN);
            case TopLeft     -> button.setPosition(MARGIN + leftOffset, MARGIN);
            case BottomLeft  -> button.setPosition(MARGIN + leftOffset, this.height - MARGIN - BUTTON_HEIGHT);
            case BottomRight -> button.setPosition(this.width  - MARGIN - BUTTON_WIDTH - rightOffset, this.height - MARGIN - BUTTON_HEIGHT);
            default -> button.setPosition(this.width  - MARGIN - BUTTON_WIDTH - rightOffset, MARGIN); // Top right
        }
    }

    @Override
    public void extractRenderState(@NonNull GuiGraphicsExtractor graphics, int mouseX, int mouseY, float deltaTicks) {
        super.extractRenderState(graphics, mouseX, mouseY, deltaTicks);

        Config config = Config.get();

        if (!config.showAccountStatus.get() && !config.showProxiesStatus.get()) {
            return;
        }

        // Shifts the top left account and proxy text to right if buttons are also top left
        int x = MARGIN;
        if (config.proxiesButtonAnchor.get() != Config.ButtonPosition.Hidden && config.proxiesButtonAnchor.get() == Config.ButtonPosition.TopLeft) {
            x += BUTTON_WIDTH + GAP;
        }
        if (config.accountButtonAnchor.get() != Config.ButtonPosition.Hidden && config.accountButtonAnchor.get() == Config.ButtonPosition.TopLeft) {
            x += BUTTON_WIDTH + GAP;
        }

        int y = MARGIN;


        if (!config.showProxiesStatus.get()) {
            return;
        }
    }
}
