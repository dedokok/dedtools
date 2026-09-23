package com.dedokok.gui;

import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WContainer;
import com.dedokok.gui.widgets.containers.WView;

import java.util.ArrayList;
import java.util.List;

import static com.dedokok.DedTools.mc;

public final class GuiHitTest {
    /** Допуск при сравнении координат: события и рендер могут отдавать мышь с разным округлением. */

    private static WWidget topmost;
    private static double mouseX = Double.NaN, mouseY = Double.NaN;

    private static WWidget best;
    private static int bestLayer;

    private GuiHitTest() {}


    private static final class Floating {
        double scale = 1.0;
        long seen;
    }

    private static final java.util.Map<WWidget, Floating> floating = new java.util.LinkedHashMap<>();
    private static long frame;

    /** Вызывать каждый кадр, пока виджет рисуется. Перестали вызывать - через пару кадров он пропадёт из реестра. */
    public static void register(WWidget w) {
        register(w, 1.0);
    }

    public static void register(WWidget w, double coordScale) {
        Floating f = floating.computeIfAbsent(w, k -> new Floating());
        f.scale = coordScale;
        f.seen = frame;
    }

    public static void unregister(WWidget w) {
        floating.remove(w);   // необязательно: для мгновенного снятия
    }
    /** Вызывать раз в кадр до отрисовки дерева, с теми же координатами, что уйдут в root.render. */
    public static void update(WWidget root, double x, double y) {
        mouseX = x;
        mouseY = y;
        best = null;
        bestLayer = -1;

        frame++;
        floating.values().removeIf(f -> f.seen < frame - 2);

        final double INF = Double.POSITIVE_INFINITY;

        if (root != null) walk(root, x, y, 0, -INF, -INF, INF, INF);

        for (var e : floating.entrySet()) {
            double s = e.getValue().scale;
            walk(e.getKey(), x / s, y / s, 0, -INF, -INF, INF, INF);
        }

        topmost = bestLayer > 0 ? best : null;
        best = null;
    }

    public static void clear() {
        topmost = null;
        best = null;
        floating.clear();
        mouseX = mouseY = Double.NaN;
    }

    public static WWidget getTopmost() {
        return topmost;
    }

    private static double eps() {
        return mc.getWindow().getGuiScale() + 1;
    }
    /** true, если виджет находится под курсором, но его перекрывает другой виджет более высокого приоритета. */
    public static boolean isBlocked(WWidget w, double x, double y) {
        WWidget top = topmost;
        if (top == null || w == top) return false;

        double eps = eps();
        if (!(Math.abs(x - mouseX) <= eps && Math.abs(y - mouseY) <= eps)) return false;

        if (w.isAncestorOrSelfOf(top) || top.isAncestorOrSelfOf(w)) return false;
        return true;
    }

    private static void walk(WWidget w, double x, double y, int parentLayer,
                             double cx1, double cy1, double cx2, double cy2) {
        if (!w.visible) return;

        int layer = Math.max(parentLayer, w.layer);

        if(layer>0){
            int a;
            a = 2;
            a++;
        }

        // сам виджет: его границы + отсечение предков (для скролла)
        if (w.isOverRaw(x, y)) {
            if (x >= cx1) {
                if (x <= cx2)
                    if (y >= cy1) {
                        if (y <= cy2) {
                            if (layer >= bestLayer) {
                                bestLayer = layer;
                                best = w;
                            }
                        }
                    }
            }
        }

        if (!(w instanceof WContainer container)) return;

        // дети видят отсечение родителя, суженное самим родителем, если он режет содержимое
        if (clips(w)) {
            cx1 = Math.max(cx1, w.x);
            cy1 = Math.max(cy1, w.y);
            cx2 = Math.min(cx2, w.x + w.width);
            cy2 = Math.min(cy2, w.y + w.height);
            if (cx1 > cx2 || cy1 > cy2) return;
        }

        for (Cell<?> cell : container.cells) {
            walk(cell.widget(), x, y, layer, cx1, cy1, cx2, cy2);
        }
    }

    /** Контейнеры, которые обрезают содержимое по своим границам. Добавьте сюда свои, если есть. */
    private static boolean clips(WWidget w) {
        return w instanceof WView;
    }
}