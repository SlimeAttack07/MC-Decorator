package slimeattack07.decorator.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeattack07.decorator.block.blockentities.AbstractMachineBE;
import slimeattack07.decorator.component.ModDataComponents;

public abstract class AbstractMachineBlock extends BaseEntityBlock {
    public AbstractMachineBlock(Properties props) {
        super(props);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public void onRemove(BlockState pState, Level pLevel, BlockPos pPos, BlockState pNewState, boolean pIsMoving) {
        if (pState.getBlock() != pNewState.getBlock()) {
            BlockEntity blockEntity = pLevel.getBlockEntity(pPos);

            if (blockEntity instanceof AbstractMachineBE abstractTE)
                abstractTE.drops();
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);

        if (level.getBlockEntity(pos) instanceof AbstractMachineBE blockEntity && stack.has(ModDataComponents.EXPERIENCE.get())) {
            Integer experience = stack.get(ModDataComponents.EXPERIENCE.get());

            if(experience != null)
                blockEntity.setExperience(experience);
        }
    }
}
