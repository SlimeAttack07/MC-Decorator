package slimeattack07.decorator.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import slimeattack07.decorator.block.blockentities.AbstractBE;

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

            if (blockEntity instanceof AbstractBE abstractTE)
                abstractTE.drops();
        }

        super.onRemove(pState, pLevel, pPos, pNewState, pIsMoving);
    }
}
