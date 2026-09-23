package com.dedokok.gui.renderer;/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */


import com.dedokok.events.render.GUIRenderEvent;
import com.dedokok.events.render.Render2DEvent;
import com.dedokok.gui.GuiHitTest;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.screens.settings.VeinsListSettingScreen;
import com.dedokok.gui.themes.meteor.widgets.WMeteorVeinChoose;
import com.dedokok.gui.widgets.WVeinChoose;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.utils.Utils;
import com.mojang.blaze3d.systems.RenderSystem;
import it.unimi.dsi.fastutil.Stack;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import com.dedokok.DedTools;
import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.renderer.operations.TextOperation;
import com.dedokok.gui.renderer.packer.GuiTexture;
import com.dedokok.gui.renderer.packer.TexturePacker;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.renderer.Renderer2D;
import com.dedokok.renderer.Texture;
import com.dedokok.utils.PostInit;
import com.dedokok.utils.misc.Pool;
import com.dedokok.utils.render.RenderUtils;
import com.dedokok.utils.render.color.Color;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemStack;

import java.util.List;

import static com.dedokok.DedTools.mc;
import static com.dedokok.utils.Utils.getWindowHeight;
import static com.dedokok.utils.Utils.getWindowWidth;

public class GuiRenderer {
    private static final Color WHITE = new Color(255, 255, 255);

    private static final TexturePacker TEXTURE_PACKER = new TexturePacker();
    private static Texture TEXTURE;

    public static GuiTexture CIRCLE;
    public static GuiTexture TRIANGLE;
    public static GuiTexture EDIT;
    public static GuiTexture RESET;
    public static GuiTexture FAVORITE_NO, FAVORITE_YES;
    public static GuiTexture COPY, PASTE;

    public GuiTheme theme;

    private final Renderer2D r = new Renderer2D(false);
    private final Renderer2D rTex = new Renderer2D(true);

    private final Pool<Scissor> scissorPool = new Pool<>(Scissor::new);
    private final Stack<Scissor> scissorStack = new ObjectArrayList<>();

    private final Pool<TextOperation> textPool = new Pool<>(TextOperation::new);
    private final List<TextOperation> texts = new ObjectArrayList<>();

    private final List<Runnable> postTasks = new ObjectArrayList<>();

    public String tooltip, lastTooltip;
    public WWidget tooltipWidget;
    private double tooltipAnimProgress;

    public GuiGraphicsExtractor graphics;

    public static GuiTexture addTexture(Identifier id) {
        return TEXTURE_PACKER.add(id);
    }

    @PostInit
    public static void init() {
        CIRCLE = addTexture(DedTools.identifier("textures/icons/gui/circle.png"));
        TRIANGLE = addTexture(DedTools.identifier("textures/icons/gui/triangle.png"));
        EDIT = addTexture(DedTools.identifier("textures/icons/gui/edit.png"));
        RESET = addTexture(DedTools.identifier("textures/icons/gui/reset.png"));
        FAVORITE_NO = addTexture(DedTools.identifier("textures/icons/gui/favorite_no.png"));
        FAVORITE_YES = addTexture(DedTools.identifier("textures/icons/gui/favorite_yes.png"));

        COPY = addTexture(DedTools.identifier("textures/icons/gui/copy.png"));
        PASTE = addTexture(DedTools.identifier("textures/icons/gui/paste.png"));

        TEXTURE = TEXTURE_PACKER.pack();
    }

    public void begin(GuiGraphicsExtractor graphics) {
        this.graphics = graphics;
        this.graphics.nextStratum();

        var matrices = graphics.pose();
        matrices.pushMatrix();
        matrices.scale(1.0f / mc.getWindow().getGuiScale());

        scissorStart(0, 0, getWindowWidth(), getWindowHeight());


    }

    public void end() {
        scissorEnd();

        for (Runnable task : postTasks) task.run();
        postTasks.clear();

        graphics.pose().popMatrix();
        graphics.nextStratum();
    }

    public void beginRender() {
        r.begin();
        rTex.begin();
    }

    public void endRender() {
        endRender(null);
    }

    public void endRender(Scissor scissor) {
        if (scissor != null) scissor.push();

        r.end();
        rTex.end();

        r.render();
        rTex.render("u_Texture", TEXTURE.getTextureView(), TEXTURE.getSampler());

        // Normal text
        theme.textRenderer().begin(graphics, theme.scale(1));
        for (TextOperation text : texts) {
            if (!text.title) text.run(textPool);
        }
        theme.textRenderer().end();

        // Title text
        theme.textRenderer().begin(graphics, theme.scale(1.25));
        for (TextOperation text : texts) {
            if (text.title) text.run(textPool);
        }
        theme.textRenderer().end();

        texts.clear();

        if (scissor != null) scissor.pop();
    }

    public void scissorStart(double x, double y, double width, double height) {
        if (!scissorStack.isEmpty()) {
            Scissor parent = scissorStack.top();

            if (x < parent.x) x = parent.x;
            else if (x + width > parent.x + parent.width) width -= (x + width) - (parent.x + parent.width);

            if (y < parent.y) y = parent.y;
            else if (y + height > parent.y + parent.height) height -= (y + height) - (parent.y + parent.height);

            endRender(parent);
        }

        scissorStack.push(scissorPool.get().set(x, y, width, height));
        graphics.enableScissor((int) x, (int) y, (int) (x + width), (int) (y + height));

        beginRender();
    }

    public void scissorEnd() {
        Scissor scissor = scissorStack.pop();

        endRender(scissor);

        scissor.push();
        for (Runnable task : scissor.postTasks) task.run();
        scissor.pop();

        graphics.disableScissor();
        if (!scissorStack.isEmpty()) beginRender();

        scissorPool.free(scissor);
    }

    public boolean renderTooltip(GuiGraphicsExtractor graphics, double mouseX, double mouseY, double delta) {
        tooltipAnimProgress += (tooltip != null ? 1 : -1) * delta * 14;
        tooltipAnimProgress = Mth.clamp(tooltipAnimProgress, 0, 1);

        boolean toReturn = false;

        if (tooltipAnimProgress > 0) {
            if (tooltip != null && !tooltip.equals(lastTooltip)) {
                tooltipWidget = theme.tooltip(tooltip);
                tooltipWidget.init();
            }

            double deltaX = -tooltipWidget.x + mouseX + 12;
            double deltaY = -tooltipWidget.y + mouseY + 12;

            if (mouseX + 12 + tooltipWidget.width > getWindowWidth())
                deltaX = -tooltipWidget.x + getWindowWidth() - tooltipWidget.width;
            if (mouseY + 12 + tooltipWidget.height > getWindowHeight())
                deltaY = -tooltipWidget.y + getWindowHeight() - tooltipWidget.height;

            tooltipWidget.move(deltaX, deltaY);

            setAlpha(tooltipAnimProgress);

            begin(graphics);
            graphics.nextStratum();
            //RenderSystem.disableDepthTest();
            tooltipWidget.render(this, mouseX, mouseY, delta);
           // RenderSystem.enableDepthTest();
            end();

            setAlpha(1);

            lastTooltip = tooltip;
            toReturn = true;
        }

        tooltip = null;
        return toReturn;
    }


    public WVeinChoose createVeinMenu(VeinsListSettingScreen screen, BlockBreakFinder.Vein vein){
        WVeinChoose veinWidget = new WMeteorVeinChoose(screen, vein);
        veinWidget.theme = theme;
        veinWidget.init();
        return veinWidget;
    }

    private double lastMouseX=-1;
    private double lastMouseY=-1;

    public WVeinChoose renderVeinMenu(WVeinChoose veinWidget, GuiGraphicsExtractor graphics,
                                      double menuX, double menuY,
                                      double mouseX, double mouseY,
                                      double delta) {
        Utils.unscaledProjection();
        boolean toReturn = false;
        //if (tooltip != null && !tooltip.equals(lastTooltip)) {


        int s = mc.getWindow().getGuiScale();
        menuX *= s;
        menuY *= s;



        double deltaX = -veinWidget.x + menuX + 12;
        double deltaY = -veinWidget.y + menuY + 12;

        if (menuX + 12 + veinWidget.width > getWindowWidth())
            deltaX = -veinWidget.x + getWindowWidth() - veinWidget.width;
        if (menuY + 12 + veinWidget.height > getWindowHeight())
            deltaY = -veinWidget.y + getWindowHeight() - veinWidget.height;

        veinWidget.move(deltaX, deltaY);


        veinWidget.x = menuX;
        veinWidget.y = menuY;
        veinWidget.calculateSize();
        veinWidget.calculateWidgetPositions();

        veinWidget.mouseMoved(mouseX, mouseY, lastMouseX, lastMouseY);   // <-- эта строка
        lastMouseX = mouseX;
        lastMouseY = mouseY;


        begin(graphics);
        graphics.nextStratum();
        veinWidget.render(this, mouseX, mouseY, delta);
        GuiHitTest.register(veinWidget);


        end();

        setAlpha(1);

        toReturn = true;


        veinWidget.layer=1;

        Utils.scaledProjection();

        return veinWidget;
    }

    public void renderWidget(WWidget widget, double mouseX, double mouseY, double delta) {
        widget.render(this, mouseX, mouseY, delta);
    }




    public void setAlpha(double a) {
        r.setAlpha(a);
        rTex.setAlpha(a);

        theme.textRenderer().setAlpha(a);
    }

    public void tooltip(String text) {
        tooltip = text;
    }

    public void quad(double x, double y, double width, double height, Color cTopLeft, Color cTopRight, Color cBottomRight, Color cBottomLeft) {
        r.quad(x, y, width, height, cTopLeft, cTopRight, cBottomRight, cBottomLeft);
    }

    public void quad(double x, double y, double width, double height, Color colorLeft, Color colorRight) {
        quad(x, y, width, height, colorLeft, colorRight, colorRight, colorLeft);
    }

    public void quad(double x, double y, double width, double height, Color color) {
        quad(x, y, width, height, color, color);
    }

    public void quad(WWidget widget, Color color) {
        quad(widget.x, widget.y, widget.width, widget.height, color);
    }

    public void quad(double x, double y, double width, double height, GuiTexture texture, Color color) {
        rTex.texQuad(x, y, width, height, texture.get(width, height), color);
    }

    public void rotatedQuad(double x, double y, double width, double height, double rotation, GuiTexture texture, Color color) {
        rTex.texQuad(x, y, width, height, rotation, texture.get(width, height), color);
    }

    public void triangle(double x1, double y1, double x2, double y2, double x3, double y3, Color color) {
        r.triangle(x1, y1, x2, y2, x3, y3, color);
    }

    public void text(String text, double x, double y, Color color, boolean title) {
        texts.add(getOp(textPool, x, y, color).set(text, theme.textRenderer(), title));
    }

    public void texture(double x, double y, double width, double height, double rotation, Texture texture) {
        post(() -> {
            rTex.begin();
            rTex.texQuad(x, y, width, height, rotation, 0, 0, 1, 1, WHITE);
            rTex.end();

            rTex.render(texture.getTextureView(), texture.getSampler());
        });
    }

    public void post(Runnable task) {
        scissorStack.top().postTasks.add(task);
    }

    public void item(ItemStack itemStack, int x, int y, float scale, boolean overlay) {
        RenderUtils.drawItem(graphics, itemStack, x, y, scale, overlay, null, false);
    }

    public void absolutePost(Runnable task) {
        postTasks.add(task);
    }

    private <T extends GuiRenderOperation<T>> T getOp(Pool<T> pool, double x, double y, Color color) {
        T op = pool.get();
        op.set(x, y, color);
        return op;
    }
}
