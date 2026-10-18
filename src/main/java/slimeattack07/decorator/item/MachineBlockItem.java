package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;
import slimeattack07.decorator.block.blockentities.AbstractMachineBE;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.util.MachineTier;

import java.util.List;

public class MachineBlockItem extends BlockItem {
    public MachineBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        if (stack.has(ModDataComponents.EXPERIENCE.get())) {
            Integer experience = stack.get(ModDataComponents.EXPERIENCE.get());

            if(experience != null){
                MachineTier tier = MachineTier.toTier(experience);
                ChatFormatting color = tier.getColor();
                tooltipComponents.add(Component.translatable("experience.decorator.machine." + tier.name().toLowerCase(), experience).withStyle(color));
            }
        }

        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
    }
}
