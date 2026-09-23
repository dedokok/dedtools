package com.dedokok.mixin;

import com.dedokok.systems.modules.Feature.ShowRepairCost;
import com.dedokok.systems.modules.Modules;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class RepairCostMixin {

    @Inject(
            method = "getTooltipLines",
            at = @At(value = "RETURN", ordinal = 1)
    )
    private void tooltipMixin(Item.TooltipContext context, @Nullable Player player,
                          TooltipFlag type, CallbackInfoReturnable<List<Component>> cir) {
        if (type.isAdvanced() && Modules.get().isActive(ShowRepairCost.class)) {
            ItemStack self = (ItemStack) (Object) this;
            List<Component> tooltip = cir.getReturnValue();

            Integer repairCost = self.get(DataComponents.REPAIR_COST);

            if (repairCost != null && repairCost > 0) {
                tooltip.add(
                        Component.literal("Repair Cost: " + repairCost)
                                .withStyle(Style.EMPTY.withColor(TextColor.fromRgb(0xFF5555)))
                );
            }
        }

    }



//    @Inject(
//            method = "getItemName",
//            at = @At(value = "RETURN"),
//            cancellable = true
//    )
//    private void itemNameMixin(CallbackInfoReturnable<Component> cir) {
//        Component original = cir.getReturnValue();
//        if (original == null) return;
//
//        String oldText = original.getString();
//        String newText = convertTimeInText(oldText);
//
//        if (newText.equals(oldText)) return;
//
//        cir.setReturnValue(Component.literal(newText).setStyle(original.getStyle()));
//    }



    private static List<Component> lastItemName = null;
    private static List<Component> lastResult = null;

    @Inject(
            method = "getTooltipLines",
            at = @At(value = "RETURN"),
            cancellable = true
    )
    private void onGetTooltipLines(
            Item.TooltipContext context,
            @Nullable Player player,
            TooltipFlag flag,
            CallbackInfoReturnable<List<Component>> cir
    ) {
        List<Component> lines = cir.getReturnValue();
        if (lines == null || lines.isEmpty()) return;
        if(lastItemName==null)lastItemName=lines;
        if(lines.equals(lastItemName)) {
            cir.setReturnValue(lastItemName.equals(lastResult) ? lastItemName : lastResult);
            return;
        }
        lastItemName=lines;
        lastResult=lines;
        // Первая строка — это имя предмета
        Component nameLine = lines.get(0);

        // Применяем вашу замену времени
        Component modified = modifyItemName(nameLine);

        if (modified != null && !modified.equals(nameLine)) {
            // Заменяем первую строку на модифицированную
            List<Component> newLines = new ArrayList<>(lines);
            newLines.set(0, modified);
            lastResult=newLines;
            cir.setReturnValue(newLines);
        }
    }

    // Ваш метод замены времени (адаптированный для Component)
    private Component modifyItemName(Component original) {
        if (original == null) return null;

        String oldText = original.getString();
        String newText = convertTimeInText(oldText);

        if (newText.equals(oldText)) return original;

        return Component.literal(newText).setStyle(original.getStyle().withItalic(false));
    }

    private String convertTimeInText(String text) {
        Pattern pattern = Pattern.compile(
                "\\d{2}/\\d{2}/\\d{4} \\d{2}:\\d{2}:\\d{2} [AP]M \\w+");

        DateTimeFormatter inputFmt = DateTimeFormatter.ofPattern(
                "dd/MM/yyyy hh:mm:ss a z", Locale.ENGLISH);
        DateTimeFormatter outputFmt = DateTimeFormatter.ofPattern(
                "dd.MM.yyyy HH:mm:ss", Locale.ENGLISH);

        Matcher m = pattern.matcher(text);
        if (!m.find()) {
            return text; // ничего не нашли — вернуть как есть
        }

        String matched = m.group();
        ZonedDateTime zdt;
        try {
            zdt = ZonedDateTime.parse(matched, inputFmt)
                    .withZoneSameInstant(ZoneId.of("Europe/Moscow"));
        } catch (DateTimeParseException e) {
            return text; // не смогли распарсить — вернуть как есть
        }

        String converted = zdt.format(outputFmt) + " MSK";

        // Заменяем найденный фрагмент в исходной строке, сохраняя остальное
        return text.substring(0, m.start()) + converted + text.substring(m.end());
    }
}