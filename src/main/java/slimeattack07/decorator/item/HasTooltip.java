package slimeattack07.decorator.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public interface HasTooltip {
    public void addToTooltip(List<Component> tooltip, ItemStack stack);
}
