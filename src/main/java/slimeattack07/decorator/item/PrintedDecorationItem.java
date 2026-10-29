package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.DecorationTier;

import java.util.List;

public class PrintedDecorationItem extends Item implements HasTooltip{
    private final String registryId;
    private final DecorationTier tier;

    public PrintedDecorationItem(Properties properties, String registryId, DecorationTier tier) {
        super(properties.component(DataComponents.CUSTOM_NAME, Component.translatable("item.decorator." + registryId)
            .withStyle(Style.EMPTY.withColor(tier.getColor()).withItalic(false))));

        this.registryId = "tooltip." + Decorator.MOD_ID + "." + registryId;
        this.tier = tier;
    }

    public String getTierString(){
        return tier.name();
    }

    public String getScanId(){
        return BuiltInRegistries.ITEM.getKey(this) + "," + getTierString();
    }

    @Override
    public void addToTooltip(List<Component> tooltip, ItemStack stack) {
        tooltip.add(Component.translatable(registryId).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
        tooltip.add(Component.translatable("rarity.decorator." + tier.name().toLowerCase()).withStyle(tier.getColor()).withStyle(ChatFormatting.BOLD));
    }
}
