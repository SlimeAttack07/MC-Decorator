package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeattack07.decorator.block.ModBlocks;

public class DecorationCrafterMenu extends AbstractMenu{

    public DecorationCrafterMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(5));
    }

    public DecorationCrafterMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DECORATION_CRAFTER_MENU.get(), pContainerId, inv, entity, data);
    }

    @Override
    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getStage(){
        return data.get(4);
    }

    @Override
    public int getInputSlotCount() {
        return 7;
    }

    @Override
    protected int getOutputSlotCount() {
        return 1;
    }

    @Override
    protected SlotLocation getLocationOfSlot(int slot) {
        return switch(slot){
            case 0 -> new SlotLocation(8, 54); // Slot for Decoration Instructions.
            case 1 -> new SlotLocation(62, 18); // 6 input slots for crafting materials.
            case 2 -> new SlotLocation(80, 18);
            case 3 -> new SlotLocation(98, 18);
            case 4 -> new SlotLocation(62, 36);
            case 5 -> new SlotLocation(80, 36);
            case 6 -> new SlotLocation(98, 36);
            default -> new SlotLocation(138, 54); // Slot 7, which is output slot 1.
        };
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.DECORATION_CRAFTER.get();
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        int progressIconPixelSize = 14;

        return maxProgress != 0 && progress != 0 ? progress * progressIconPixelSize / maxProgress : 0;
    }
}
