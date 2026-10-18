package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.DecorationTier;

import java.util.List;

public class RefinedDraftItem extends Item implements HasTooltip{
    private final String registryId;

    public RefinedDraftItem(Item.Properties properties, String registryId) {
        super(properties);

        this.registryId = "tooltip." + Decorator.MOD_ID + "." + registryId;
    }

    @Override
    public void addToTooltip(List<Component> tooltip, ItemStack stack) {
        tooltip.add(Component.translatable(registryId, Component.keybind("key.use")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));

        CustomData data = stack.getComponents().get(DataComponents.CUSTOM_DATA);

        if(data != null){
            CompoundTag tag = data.copyTag();
            String groupFocus = tag.getString("group_focus").orElse("");
            String tierFocus = tag.getString("tier_focus").orElse("");

            if(!groupFocus.isBlank()){
                String[] parts = groupFocus.split("/");
                String groupName = parts[parts.length - 1];
                double groupChance = tag.getDouble("group_chance").orElse(0d);
                ChatFormatting color = groupChance >= 50 ? ChatFormatting.GREEN : ChatFormatting.RED;
                tooltip.add(Component.translatable("focus.decorator.group_focus." + groupName, Component.literal(groupChance + "%").withStyle(color)));
            }

            if(!tierFocus.isBlank()){
                String[] parts = tierFocus.split("/");
                String tierName = parts[parts.length - 1];
                double tierChance = tag.getDouble("tier_chance").orElse(0d);
                ChatFormatting percentageColor = tierChance >= 50 ? ChatFormatting.GREEN : ChatFormatting.RED;
                DecorationTier tier = DecorationTier.parse(tierName);
                ChatFormatting color = tier == null ? ChatFormatting.RESET : tier.getColor();
                tooltip.add(Component.translatable("focus.decorator.tier_focus." + tierName, Component.literal(tierChance + "%").withStyle(percentageColor))
                    .withStyle(color));
            }
        }
    }
}
