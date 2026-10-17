package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeattack07.decorator.block.ModBlocks;
import slimeattack07.decorator.util.ModTags;

public class DraftingTableMenu extends AbstractMenu{
    public DraftingTableMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DRAFTING_TABLE_MENU.get(), pContainerId, inv, entity, data);
    }

    public DraftingTableMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData){
        super(ModMenuTypes.DRAFTING_TABLE_MENU.get(), pContainerId, inv, extraData);
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
    protected SlotLocation getLocationOfSlot(int slot) {
        return switch(slot){
            case 0 -> new SlotLocation(35, 34);
            case 1 -> new SlotLocation(26, 52);
            case 2 -> new SlotLocation(44, 52);
            default -> new SlotLocation(116, 43); // Slot 3, which is output slot 1.
        };
    }

    @Override
    protected boolean mayInsert(ItemStack itemStack, int slot) {
        return switch(slot){
            case 1 -> itemStack.getTags().anyMatch(entry -> entry.equals(ModTags.Items.GROUP_FOCUSES));
            case 2 -> itemStack.getTags().anyMatch(entry -> entry.equals(ModTags.Items.TIER_FOCUSES));
            default -> true; // Slot 0.
        };
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.DRAFTING_TABLE.get();
    }
}
