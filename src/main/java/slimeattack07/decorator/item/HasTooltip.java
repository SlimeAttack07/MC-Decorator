package slimeattack07.decorator.item;

import net.minecraft.network.chat.Component;

import java.util.List;

public interface HasTooltip {
    public void addToTooltip(List<Component> tooltip);
}
