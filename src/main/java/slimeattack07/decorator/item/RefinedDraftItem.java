package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.DecorationTier;

import java.util.List;

public class RefinedDraftItem extends Item{
    private final String registryId;

    public RefinedDraftItem(Item.Properties properties, String registryId) {
        super(properties);

        this.registryId = "tooltip." + Decorator.MOD_ID + "." + registryId;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        tooltipComponents.add(Component.translatable(registryId, Component.keybind("key.use")).withStyle(ChatFormatting.GRAY).withStyle(ChatFormatting.ITALIC));

        CustomData data = stack.getComponents().get(DataComponents.CUSTOM_DATA);

        if(data != null){
            CompoundTag tag = data.copyTag();
            String groupFocus = tag.getString("group_focus");
            String tierFocus = tag.getString("tier_focus");

            if(!groupFocus.isBlank()){
                String[] parts = groupFocus.split("/");
                String groupName = parts[parts.length - 1];
                double groupChance = tag.getDouble("group_chance");
                ChatFormatting color = groupChance >= 50 ? ChatFormatting.GREEN : ChatFormatting.RED;
                tooltipComponents.add(Component.translatable("focus.decorator.group_focus." + groupName, Component.literal(groupChance + "%").withStyle(color)));
            }

            if(!tierFocus.isBlank()){
                String[] parts = tierFocus.split("/");
                String tierName = parts[parts.length - 1];
                double tierChance = tag.getDouble("tier_chance");
                ChatFormatting percentageColor = tierChance >= 50 ? ChatFormatting.GREEN : ChatFormatting.RED;
                DecorationTier tier = DecorationTier.parse(tierName);
                ChatFormatting color = tier == null ? ChatFormatting.RESET : tier.getColor();
                tooltipComponents.add(Component.translatable("focus.decorator.tier_focus." + tierName, Component.literal(tierChance + "%").withStyle(percentageColor))
                    .withStyle(color));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
