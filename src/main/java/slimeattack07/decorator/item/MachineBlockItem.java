package slimeattack07.decorator.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.util.MachineTier;

import java.util.List;

public class MachineBlockItem extends BlockItem implements HasTooltip{
    public MachineBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void addToTooltip(List<Component> tooltip, ItemStack stack) {
        if (stack.has(ModDataComponents.EXPERIENCE.get())) {
            Integer experience = stack.get(ModDataComponents.EXPERIENCE.get());

            if(experience != null){
                MachineTier tier = MachineTier.toTier(experience);
                ChatFormatting color = tier.getColor();
                tooltip.add(Component.translatable("experience.decorator.machine." + tier.name().toLowerCase(), experience).withStyle(color));
            }
        }
    }
}
