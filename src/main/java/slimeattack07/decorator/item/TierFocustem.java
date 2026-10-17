package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.DecorationTier;

import java.util.List;

public class TierFocustem extends TooltippedItem {
    public TierFocustem(Properties properties, String registryId, DecorationTier tier) {
        super(properties.component(DataComponents.CUSTOM_NAME, Component.translatable("item.decorator." + registryId)
            .withStyle(Style.EMPTY.withColor(tier.getColor()).withItalic(false))), registryId);
    }
}
