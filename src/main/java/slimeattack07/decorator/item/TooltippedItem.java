package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import slimeattack07.decorator.Decorator;

import java.util.List;

public class TooltippedItem extends Item implements HasTooltip{
    private final String registryId;

    public TooltippedItem(Properties properties, String registryId) {
        super(properties);

        this.registryId = "tooltip." + Decorator.MOD_ID + "." + registryId;
    }

    @Override
    public void addToTooltip(List<Component> tooltip) {
        tooltip.add(Component.translatable(registryId).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
