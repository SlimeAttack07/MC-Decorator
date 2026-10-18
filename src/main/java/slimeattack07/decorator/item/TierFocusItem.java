package slimeattack07.decorator.item;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import slimeattack07.decorator.util.DecorationTier;

public class TierFocusItem extends TooltippedItem {
    public TierFocusItem(Properties properties, String registryId, DecorationTier tier) {
        super(properties.component(DataComponents.CUSTOM_NAME, Component.translatable("item.decorator." + registryId)
            .withStyle(Style.EMPTY.withColor(tier.getColor()).withItalic(false))), registryId);
    }
}
