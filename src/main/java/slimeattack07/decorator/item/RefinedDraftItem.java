package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import slimeattack07.decorator.Decorator;

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

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
