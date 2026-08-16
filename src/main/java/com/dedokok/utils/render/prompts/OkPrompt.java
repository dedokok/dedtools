/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package com.dedokok.utils.render.prompts;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.GuiThemes;
import com.dedokok.gui.widgets.pressable.WButton;
import net.minecraft.client.gui.screens.Screen;

import static com.dedokok.DedTools.mc;

public class OkPrompt extends Prompt<OkPrompt> {
    private Runnable onOk = () -> {
    };

    private OkPrompt(GuiTheme theme, Screen parent) {
        super(theme, parent);
    }

    public static OkPrompt create() {
        return new OkPrompt(GuiThemes.get(), mc.gui.screen());
    }

    public static OkPrompt create(GuiTheme theme, Screen parent) {
        return new OkPrompt(theme, parent);
    }

    public OkPrompt onOk(Runnable action) {
        this.onOk = action;
        return this;
    }

    @Override
    protected void initialiseWidgets(PromptScreen screen) {
        WButton okButton = screen.list.add(theme.button("Ok")).expandX().widget();
        okButton.action = () -> {
            dontShowAgain(screen);
            onOk.run();
            screen.onClose();
        };
    }
}
