package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeattack07.decorator.block.ModBlocks;

public class DraftingTableMenu extends AbstractMenu{
    public DraftingTableMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData){
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public DraftingTableMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DRAFTING_TABLE_MENU.get(), pContainerId, inv, entity, data);
    }

    @Override
    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    @Override
    public int getInputSlotCount() {
        return 3;
    }

    @Override
    protected int getOutputSlotCount() {
        return 1;
    }

    @Override
    protected boolean insertsReversed() {
        return true;
    }

    @Override
    protected SlotLocation getLocationOfSlot(int slot) {
        return switch(slot){
            case 0 -> new SlotLocation(35, 34);
            case 1 -> new SlotLocation(26, 52);
            case 2 -> new SlotLocation(44, 52);
            default -> new SlotLocation(116, 43); // Slot 3, which is output slot 1.
        };
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.DRAFTING_TABLE.get();
    }
}
