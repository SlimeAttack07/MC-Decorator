package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeattack07.decorator.block.ModBlocks;

public class DecorationScannerMenu extends AbstractMenu{

    public DecorationScannerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData){
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(4));
    }

    public DecorationScannerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DECORATION_SCANNER_MENU.get(), pContainerId, inv, entity, data);
    }

    @Override
    public boolean isCrafting() {
        return data.get(1) > 0;
    } // Since players will probably leave some Scan Templates in the input, it's better to check the decoration input slot instead.

    @Override
    public int getInputSlotCount() {
        return 2;
    }

    @Override
    protected int getOutputSlotCount() {
        return 2;
    }

    @Override
    protected SlotLocation getLocationOfSlot(int slot) {
        return switch(slot){
            case 0 -> new SlotLocation(35, 34);
            case 1 -> new SlotLocation(35, 52);
            case 2 -> new SlotLocation(116, 34);
            default -> new SlotLocation(116, 52); // Slot 3, which is output slot 2.
        };
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.DECORATION_SCANNER.get();
    }
}
