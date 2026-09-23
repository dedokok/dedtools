package com.dedokok.systems.modules.Feature;

import com.dedokok.settings.BoolSetting;
import com.dedokok.settings.ColorSetting;
import com.dedokok.settings.Setting;
import com.dedokok.settings.SettingGroup;
import com.dedokok.systems.modules.Categories;
import com.dedokok.systems.modules.Module;
import com.dedokok.utils.render.color.SettingColor;
import net.minecraft.network.chat.*;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AuxProtectClickTeleport extends Module {
    private static boolean isActive = false;
    public AuxProtectClickTeleport() {
        super(Categories.Feature, "AuxProtect teleport", "Click to AuxProtect message to teleport to it",null);
        runInMainMenu=true;
    }




    public void parseSiblings(Component sibling, String level){
        List<Component>siblings = sibling.getSiblings();
        int count = 1;
        for(Component component : siblings){
            //System.out.println(level+"."+count+" "+component.getString());
            if(!component.getSiblings().isEmpty()){
                parseSiblings(component,level+"."+count);
            }
            count++;
        }
    }

    public Component onGameMessage(Component message, boolean overlay) {
        if(!isActive){return message;}
        String regex = checkAuxCoordsMessage(message.getString());
        if (regex!=null) {
            Coords coords = getAuxCoords(message.getSiblings().getLast().getString(), regex);
            String command = "/co teleport " + coords.world + " " + coords.x + " " + coords.y + " " + coords.z;

            List<Component> siblings = message.getSiblings();
            int lastIndex = siblings.size() - 1;
            Component wrapper = siblings.get(lastIndex); // "\n                 "

            List<Component> wrapperSiblings = wrapper.getSiblings();
            int coordsIndex = wrapperSiblings.size() - 1;
            Component coordsComponent = wrapperSiblings.get(coordsIndex);

            Component updatedCoords = coordsComponent.copy().withStyle(style -> style
                    .withClickEvent(new ClickEvent.RunCommand(command))
                    .withHoverEvent(new HoverEvent.ShowText(Component.literal(command)))
            );

            // Пересобираем wrapper: пустой текст + все старые siblings, но с заменённым последним
            MutableComponent newWrapper = wrapper.copy().withStyle(wrapper.getStyle());
            // copy() уже клонирует siblings, но они всё ещё те же ссылки —
            // поэтому строим wrapper с нуля вместо копирования:
            newWrapper = Component.literal(((MutableComponent) wrapper).getString().isEmpty() ? "" : "")
                    .withStyle(wrapper.getStyle());
            // Проще — берём текст напрямую через компонентную структуру:
            MutableComponent rebuiltWrapper = MutableComponent.create(wrapper.getContents()).withStyle(wrapper.getStyle());
            for (int i = 0; i < wrapperSiblings.size(); i++) {
                rebuiltWrapper.append(i == coordsIndex ? updatedCoords : wrapperSiblings.get(i));
            }

            // Пересобираем message аналогично
            MutableComponent rebuiltMessage = MutableComponent.create(message.getContents()).withStyle(message.getStyle());
            for (int i = 0; i < siblings.size(); i++) {
                rebuiltMessage.append(i == lastIndex ? rebuiltWrapper : siblings.get(i));
            }

            return rebuiltMessage;
        }
        return message;
    }


    public Component makeChat(Component message, TextColor color){
        MutableComponent result = Component.empty().withStyle(message.getStyle());
        for(int i = 0; i<message.getSiblings().size(); i++){
            if(i!=message.getSiblings().size()-1){
                result.append(message.getSiblings().get(i));
            }
            else{
                MutableComponent modified = Component.literal(message.getSiblings().get(i).getString()).withColor(color);
                result.append(modified);
            }
        }

        return result;
    }

    public String checkAuxCoordsMessage(String message){
        boolean isFind = false;
        String regex = "\\\\n                 \\(x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\) ";
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);
        if(matcher.find()){
           return regex;
        }

        regex = "\\\\n                 \\(x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\)";
        pattern = Pattern.compile(regex);
        matcher = pattern.matcher(message);
        if(matcher.find()){
            return regex;
        }
        return null;
    }

    public Coords getAuxCoords(String message, String regex){
        //String regex = "\\n*\\(x(.+)\\/y(.+)\\/z(.+)\\/(.+)\\) ";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(message);

        matcher.find();
        int x = Integer.parseInt(matcher.group(1));
        int y = Integer.parseInt(matcher.group(2));
        int z = Integer.parseInt(matcher.group(3));
        String world = matcher.group(4);
        Coords coords = new Coords(x,y,z,world);

        return coords;
    }
    @Override
    public void onActivate() {
        isActive = true;
    }

    @Override
    public void onDeactivate(){
        isActive = false;
    }

    public class Coords{
        int x;
        int y;
        int z;
        String world;
        public Coords(int x, int y, int z, String world){
            this.x = x;
            this.y = y;
            this.z = z;
            this.world = world;
        }
        public Coords(){}
    }

}
