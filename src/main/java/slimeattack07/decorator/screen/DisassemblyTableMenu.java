package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import slimeattack07.decorator.block.ModBlocks;
import slimeattack07.decorator.block.blockentities.DisassemblyTableBE;

public class DisassemblyTableMenu extends AbstractContainerMenu {
    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36 - 44 = TileInventory slots, which map to our TileEntity slot numbers 0 - 8)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;
    // THIS YOU HAVE TO DEFINE!
    private static final int TE_INVENTORY_SLOT_COUNT = 2;  // must be the number of slots you have!

    public final DisassemblyTableBE blockEntity;
    private final Level level;
    private final ContainerData data;

    public DisassemblyTableMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(2));
    }

    public DisassemblyTableMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.DISASSEMBLY_TABLE_MENU.get(), pContainerId);
        this.blockEntity = ((DisassemblyTableBE) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        addSlot(new ResourceHandlerSlot(blockEntity.itemHandler, blockEntity.itemHandler::set, 0, 54, 34));
        addSlot(new ResourceHandlerSlot(blockEntity.itemHandler, blockEntity.itemHandler::set, 1, 104, 34){
            @Override
            public boolean mayPlace(ItemStack stack) {
                return false; // Extract only output slot.
            }
        });
        addDataSlots(data);
    }

    public boolean isCrafting() {
        return data.get(0) > 0;
    }

    public int getScaledArrowProgress() {
        int progress = this.data.get(0);
        int maxProgress = this.data.get(1);
        int arrowPixelSize = 24;

        return maxProgress != 0 && progress != 0 ? progress * arrowPixelSize / maxProgress : 0;
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        Slot sourceSlot = slots.get(pIndex);

        if (sourceSlot == null || !sourceSlot.hasItem())
            return ItemStack.EMPTY;

        ItemStack sourceStack = sourceSlot.getItem();
        ItemStack copyOfSourceStack = sourceStack.copy();

        // Check if the slot clicked is one of the vanilla container slots.
        if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
            // This is a vanilla container slot so merge the stack into the tile inventory
            if (!moveItemStackTo(sourceStack, TE_INVENTORY_FIRST_SLOT_INDEX, TE_INVENTORY_FIRST_SLOT_INDEX + 1, false))
                return ItemStack.EMPTY;  // EMPTY_ITEM
        } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + TE_INVENTORY_SLOT_COUNT) {
            // This is a TE slot so merge the stack into the players inventory.
            if (!moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false))
                return ItemStack.EMPTY;
        } else {
            System.out.println("Invalid slotIndex:" + pIndex);

            return ItemStack.EMPTY;
        }
        // If stack size == 0 (the entire stack was moved) set slot contents to null.
        if (sourceStack.getCount() == 0)
            sourceSlot.set(ItemStack.EMPTY);
        else
            sourceSlot.setChanged();

        sourceSlot.onTake(playerIn, sourceStack);

        return copyOfSourceStack;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, ModBlocks.DISASSEMBLY_TABLE.get());
    }

    private void addPlayerInventory(Inventory playerInventory) {
        for (int i = 0; i < 3; ++i) {
            for (int l = 0; l < 9; ++l)
                addSlot(new Slot(playerInventory, l + i * 9 + 9, 8 + l * 18, 84 + i * 18));
        }
    }

    private void addPlayerHotbar(Inventory playerInventory) {
        for (int i = 0; i < 9; ++i)
            addSlot(new Slot(playerInventory, i, 8 + i * 18, 142));
    }

    // Note to self: Slots are 18x18 (16x16 for item with some spacing around it).
    // For placement, take coordinates of which pixel (starting top left) of your GUI it should start on.
    // Don't include screen size, that's done automatically. You use the inner 16x16's top left as the coordinate!
}
