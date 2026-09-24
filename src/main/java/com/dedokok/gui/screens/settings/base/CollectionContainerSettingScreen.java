package com.dedokok.gui.screens.settings.base;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WPressable;
import com.dedokok.settings.Setting;
import com.dedokok.systems.config.Config;

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;

import com.dedokok.gui.GuiTheme;
import com.dedokok.gui.WindowScreen;
import com.dedokok.gui.utils.Cell;
import com.dedokok.gui.widgets.WWidget;
import com.dedokok.gui.widgets.containers.WTable;
import com.dedokok.gui.widgets.input.WTextBox;
import com.dedokok.gui.widgets.pressable.WPressable;
import com.dedokok.settings.Setting;
import com.dedokok.systems.config.Config;
import com.dedokok.systems.modules.Feature.BlockBreakFinder;
import com.dedokok.utils.classes.Vein;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Predicate;

public abstract class CollectionContainerSettingScreen<T> extends WindowScreen {
    protected final Setting<?> setting;
    protected ArrayList<T> collection;
    protected Iterable<T> registry;

    protected WTable table;
    protected String filterText = "";

    public CollectionContainerSettingScreen(GuiTheme theme, String title, Setting<?> setting, ArrayList<T> collection, Iterable<T> registry) {
        super(theme, title);

        this.registry = registry;
        this.setting = setting;
        this.collection = collection;
    }

    //protected abstract String[] getValueNames(Block value);

    protected abstract boolean includeValue(Vein value);

    //protected abstract Row getAdditionalValue(Row value);

    public boolean isUpdatedScreen = false;

    @Override
    public void initWidgets() {
        // Filter
        WTextBox filter = add(theme.textBox("")).minWidth(400).expandX().widget();
        filter.setFocused(true);
        filter.action = () -> {

            filterText = filter.get().trim();

            table.clear();
            initTable();
            isUpdatedScreen = true;
        };

        table = add(theme.table()).expandX().widget();

        initTable();
    }

    protected void initTable() {
        WTable left = abc(registry, true, t -> {
            addValue(t);
        });
    }

    private WTable abc(Iterable<T> iterable, boolean isLeft, Consumer<T> buttonAction) {
        // Create
        Cell<WTable> cell = this.table.add(theme.table()).top();
        if (Config.get().syncListSettingWidths.get()) cell.group("sync-width");
        WTable table = cell.widget();

        // Sort
        Predicate<T> predicate = isLeft
                ? value -> this.includeValue(value)
                : this::includeValue;

        Iterable<T> sorted = SortingHelper.sort(iterable, predicate, this::getValueNames, filterText);
//        List<T> sortedList = new ArrayList<>();
//        sorted.forEach(sortedList::add);
//
//        sortedList.sort(Comparator.comparing(
//                value -> getTimestamp(value)
//        ));

        final int[] rowIdx = {0};
        sorted.forEach(t -> {
            WWidget widget = getValueWidget(t);
            if(widget!=null) {
                table.add(widget);

                rowIdx[0]++;
                if (rowIdx[0] == 11) {
                    table.row();
                    rowIdx[0] = 0;
                }
            }
        });

        if (!table.cells.isEmpty()) cell.expandX();

        return table;
    }




    protected void invalidateTable() {
        table.clear();
        initTable();
    }

    protected void addValue(T value) {
        //if (!collection.containsValue(value)) {
            collection.add(value);
            setting.onChanged();
            invalidateTable();
        //}
    }

    protected void removeValue(T value) {
        if (collection.remove(value)) {
            setting.onChanged();
            invalidateTable();
        }
    }

    protected void postWidgets(WTable left, WTable right) {}

    protected boolean includeValue(T value) {
        return true;
    }

    protected abstract WWidget getValueWidget(T value);

    protected abstract String[] getValueNames(T value);

    protected abstract Long getTimestamp(T value);

/*    protected T getAdditionalValue(T value) {
        return null;
    }*/
}
