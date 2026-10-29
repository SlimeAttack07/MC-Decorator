package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.util.DecorationTier;

import java.util.List;

public class DecorationScanItem extends Item implements HasTooltip{
    private final String registryId;

    public DecorationScanItem(Properties properties, String registryId) {
        super(properties);

        this.registryId = "tooltip." + Decorator.MOD_ID + "." + registryId;
    }

    @Override
    public void addToTooltip(List<Component> tooltip, ItemStack stack) {
        MutableComponent replacement;
        String decoration = stack.get(ModDataComponents.DECORATION.get());

        if(decoration != null){
            String[] parts = decoration.split(",");

            if(parts.length > 1){
                DecorationTier tier = DecorationTier.parse(parts[1]);

                if(tier == null)
                    tier = DecorationTier.MODEST;

                replacement = Component.translatable("item." + parts[0].replaceAll(":", ".")).withStyle(tier.getColor());
            } else
                replacement = Component.literal("?");
        } else
            replacement = Component.literal("?");

        tooltip.add(Component.translatable(registryId, replacement).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));
    }
}
