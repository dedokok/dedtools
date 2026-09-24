package com.dedokok.gui.screens;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.tabs.TabScreen;
import com.dedokok.gui.tabs.Tabs;
import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WItemWithLabel;
import com.dedokok.gui.widgets.containers.*;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WButton;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Category;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.systems.modules.Module;
import com.dedokok.systems.modules.Modules;
import com.dedokok.utils.classes.Row;
import com.dedokok.utils.misc.NbtUtils;
import com.dedokok.utils.render.DisplayItemUtils;
import com.mojang.blaze3d.platform.MacosUtil;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import static com.dedokok.systems.modules.Feature.BlockBreakFinder.unpack;
import static com.dedokok.utils.Utils.getWindowHeight;
import static com.dedokok.utils.Utils.getWindowWidth;
import static org.lwjgl.glfw.GLFW.*;

public class OreVeinsScreenTab extends TabScreen {

    private WCategoryController controller;
    private WWindow searchWindow;
    private WTextBox searchTextBox;
    private static HashMap<Long, Row> rows = new HashMap<>();

    public OreVeinsScreenTab(GuiTheme theme) {
        super(theme, Tabs.get().getFirst());
    }

    @Override
    public void initWidgets() {
        //controller = add(new WCategoryController()).widget();

        // Help
        WVerticalList help = add(theme.verticalList()).pad(4).bottom().widget();
        help.add(theme.label("Left click - Toggle module"));
        help.add(theme.label("Right click - Open module settings"));

        WTable table = add(theme.table()).pad(3).bottom().widget();

    }
    @Override
    protected void init() {
        super.init();
        //controller.refresh();
    }

    public void setRows(HashMap<Long, Row> rows) {
        OreVeinsScreenTab.rows = rows;
    }


    private WTable table;
    public void setVeins(HashMap<Long, Row> rows){
        for (long coords : rows.keySet()) {
            //if (setting.filter != null && !setting.filter.test(block)) continue;
            //if (skipValue(block)) continue;
            Row row =  rows.get(coords);

            int[] coords_mass = unpack(coords);
            int x = coords_mass[0], y = coords_mass[1], z = coords_mass[2];

            Identifier itemId = Identifier.parse(row.getBlock());


            Item row_item = BuiltInRegistries.ITEM.get(itemId)
                    .map(Holder.Reference::value)
                    .orElseThrow(() -> new IllegalArgumentException("Unknown item: " + itemId));

            ItemStack stack = new ItemStack(row_item);


            WItemWithLabel item = theme.itemWithLabel(stack, row.getBlock());
            //if (!filterText.isEmpty() && !Strings.CI.contains(item.getLabelText(), filterText)) continue;
            table.add(item);

            WButton select = table.add(theme.button("Select")).expandCellX().right().widget();
            select.action = () -> {
                //setting.set(block);
                onClose();
            };

            table.row();
        }
    }


    protected WWindow createCategory(WContainer c, Category category, List<com.dedokok.systems.modules.Module> moduleList) {
        WWindow w = theme.window(category.name);
        w.id = category.name;
        w.padding = 0;
        w.spacing = 0;

        if (theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> wContainer.add(theme.item(category.icon.get())).pad(2);
        }

        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0;

        for (com.dedokok.systems.modules.Module module : moduleList) {
            w.add(theme.module(module)).expandX();
        }

        return w;
    }

    // Search

    protected void createSearchW(WContainer w, String text) {
        if (!text.isEmpty()) {
            // Titles
            List<Pair<com.dedokok.systems.modules.Module, String>> modules = Modules.get().searchTitles(text);

            if (!modules.isEmpty()) {
                WSection section = w.add(theme.section("Modules")).expandX().widget();
                section.spacing = 0;

                int count = 0;
                for (Pair<com.dedokok.systems.modules.Module, String> p : modules) {
                    if (count >= Config.get().moduleSearchCount.get() || count >= modules.size()) break;
                    section.add(theme.module(p.getFirst(), p.getSecond())).expandX();
                    count++;
                }
            }

            // Settings
            Set<com.dedokok.systems.modules.Module> settings = Modules.get().searchSettingTitles(text);

            if (!settings.isEmpty()) {
                WSection section = w.add(theme.section("Settings")).expandX().widget();
                section.spacing = 0;

                int count = 0;
                for (com.dedokok.systems.modules.Module module : settings) {
                    if (count >= Config.get().moduleSearchCount.get() || count >= settings.size()) break;
                    section.add(theme.module(module)).expandX();
                    count++;
                }
            }
        }
    }

    protected WWindow createSearch(WContainer c) {
        WWindow w = theme.window("Search");
        w.id = "search";
        searchWindow = w;

        if (theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> wContainer.add(theme.item(DisplayItemUtils.toStack(Items.COMPASS))).pad(2);
        }

        c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.maxHeight -= 20;

        WVerticalList l = theme.verticalList();

        WTextBox text = w.add(theme.textBox("")).minWidth(140).expandX().widget();
        text.setFocused(true);
        searchTextBox = text;
        text.action = () -> {
            l.clear();
            createSearchW(l, text.get());
        };

        w.add(l).expandX();
        createSearchW(l, text.get());

        return w;
    }

    @Override
    public boolean keyPressed(@NonNull KeyEvent value) {
        if (locked) return false;

        boolean cntrl = MacosUtil.IS_MACOS ? value.modifiers() == GLFW_MOD_SUPER : value.modifiers() == GLFW_MOD_CONTROL;

        if (cntrl && value.key() == GLFW_KEY_F) {
            if (searchWindow != null) searchWindow.setExpanded(true);
            if (searchTextBox != null) {
                searchTextBox.setFocused(true);
                searchTextBox.setCursorMax();
            }

            return true;
        }

        return super.keyPressed(value);
    }

    // Favorites

    protected Cell<WWindow> createFavorites(WContainer c) {
        boolean hasFavorites = Modules.get().getAll().stream().anyMatch(module -> module.favorite);
        if (!hasFavorites) return null;

        WWindow w = theme.window("Favorites");
        w.id = "favorites";
        w.padding = 0;
        w.spacing = 0;

        if (theme.categoryIcons()) {
            w.beforeHeaderInit = wContainer -> wContainer.add(theme.item(DisplayItemUtils.toStack(Items.NETHER_STAR))).pad(2);
        }

        Cell<WWindow> cell = c.add(w);
        w.view.scrollOnlyWhenMouseOver = true;
        w.view.hasScrollBar = false;
        w.view.spacing = 0;

        createFavoritesW(w);
        return cell;
    }

    protected boolean createFavoritesW(WWindow w) {
        List<com.dedokok.systems.modules.Module> modules = new ArrayList<>();

        for (com.dedokok.systems.modules.Module module : Modules.get().getAll()) {
            if (module.favorite) {
                modules.add(module);
            }
        }

        modules.sort((o1, o2) -> String.CASE_INSENSITIVE_ORDER.compare(o1.name, o2.name));

        for (com.dedokok.systems.modules.Module module : modules) {
            w.add(theme.module(module)).expandX();
        }

        return !modules.isEmpty();
    }

    @Override
    public boolean toClipboard() {
        return NbtUtils.toClipboard(Modules.get());
    }

    @Override
    public boolean fromClipboard() {
        return NbtUtils.fromClipboard(Modules.get());
    }

    @Override
    public void reload() {
    }

    // Stuff

    protected class WCategoryController extends WContainer {
        public final List<WWindow> windows = new ArrayList<>();
        private Cell<WWindow> favorites;

        @Override
        public void init() {
            List<com.dedokok.systems.modules.Module> moduleList = new ArrayList<>();
            for (Category category : Modules.loopCategories()) {
                for (Module module : Modules.get().getGroup(category)) {
                    if (!Config.get().hiddenModules.get().contains(module)) {
                        moduleList.add(module);
                    }
                }

                // Ensure empty categories are not shown
                if (!moduleList.isEmpty()) {
                    windows.add(createCategory(this, category, moduleList));
                    moduleList.clear();
                }
            }

            windows.add(createSearch(this));

            //refresh();
        }

//        protected void refresh() {
//            if (favorites == null) {
//                favorites = createFavorites(this);
//                if (favorites != null) windows.add(favorites.widget());
//            } else {
//                favorites.widget().clear();
//
//                if (!createFavoritesW(favorites.widget())) {
//                    remove(favorites);
//                    windows.remove(favorites.widget());
//                    favorites = null;
//                }
//            }
//        }

        @Override
        protected void onCalculateWidgetPositions() {
            double pad = theme.scale(4);
            double h = theme.scale(40);

            double x = this.x + pad;
            double y = this.y;

            for (Cell<?> cell : cells) {
                double windowWidth = getWindowWidth();
                double windowHeight = getWindowHeight();

                if (x + cell.width > windowWidth) {
                    x = x + pad;
                    y += h;
                }

                if (x > windowWidth) {
                    x = windowWidth / 2.0 - cell.width / 2.0;
                    if (x < 0) x = 0;
                }
                if (y > windowHeight) {
                    y = windowHeight / 2.0 - cell.height / 2.0;
                    if (y < 0) y = 0;
                }

                cell.x = x;
                cell.y = y;

                cell.width = cell.widget().width;
                cell.height = cell.widget().height;

                cell.alignWidget();

                x += cell.width + pad;
            }
        }
    }
}
