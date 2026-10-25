package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.Tuple;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.blockentities.AbstractMachineBE;
import slimeattack07.decorator.util.MachineTier;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractMenu extends AbstractContainerMenu {
    // CREDIT GOES TO: diesieben07 | https://github.com/diesieben07/SevenCommons
    // Must assign a slot number to each of the slots used by the GUI.
    // For this container, we can see both the tile inventory's slots as well as the player inventory slots and the hotbar.
    // Each time we add a Slot to the container, it automatically increases the slotIndex, which means
    //  0 - 8 = hotbar slots (which will map to the InventoryPlayer slot numbers 0 - 8)
    //  9 - 35 = player inventory slots (which map to the InventoryPlayer slot numbers 9 - 35)
    //  36+ = TileInventory slots (which map to our TileEntity slot numbers 0 - N)
    private static final int HOTBAR_SLOT_COUNT = 9;
    private static final int PLAYER_INVENTORY_ROW_COUNT = 3;
    private static final int PLAYER_INVENTORY_COLUMN_COUNT = 9;
    private static final int PLAYER_INVENTORY_SLOT_COUNT = PLAYER_INVENTORY_COLUMN_COUNT * PLAYER_INVENTORY_ROW_COUNT;
    private static final int VANILLA_SLOT_COUNT = HOTBAR_SLOT_COUNT + PLAYER_INVENTORY_SLOT_COUNT;
    private static final int VANILLA_FIRST_SLOT_INDEX = 0;
    private static final int TE_INVENTORY_FIRST_SLOT_INDEX = VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT;

    public final AbstractMachineBE blockEntity;
    private final Level level;
    protected final ContainerData data;

    // Caching for optimization.
    private int lastExperience = 0;
    private MachineTier lastTier = MachineTier.BASIC;

    public AbstractMenu(MenuType<?> menuType, int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(menuType, pContainerId);
        this.blockEntity = ((AbstractMachineBE) entity);
        this.level = inv.player.level();
        this.data = data;

        addPlayerInventory(inv);
        addPlayerHotbar(inv);

        for(int i = 0; i < getInputSlotCount(); i++){
            final int index = i; // Needed because the anonymous class requires it to be final.
            SlotLocation location = getLocationOfSlot(index);
            addSlot(new ResourceHandlerSlot(blockEntity.getItemHandler(), blockEntity.getItemHandler()::set, index, location.x(), location.y()){
                @Override
                public boolean mayPlace(ItemStack stack) {
                    return blockEntity.mayInsert(stack, index);
                }

                @Override
                public boolean mayPickup(Player playerIn) {
                    return blockEntity.mayExtract(index);
                }
            });
        }

        for(int i = getInputSlotCount(); i < getInputSlotCount() + getOutputSlotCount(); i++){
            SlotLocation location = getLocationOfSlot(i);
            addSlot(new OutputSlotHandler(blockEntity.getItemHandler(), i, location.x(), location.y()));
        }

        addDataSlots(data);
    }

    public int getScaledProgress() {
        int progress = data.get(0);
        int maxProgress = data.get(1);
        int progressIconPixelSize = 32;

        return maxProgress != 0 && progress != 0 ? progress * progressIconPixelSize / maxProgress : 0;
    }

    public MachineTier getCurrentMachineTier(){
        int experience = data.get(2);

        if(experience != lastExperience)
            lastTier = MachineTier.toTier(experience);

        return lastTier;
    }

    public boolean hasExperience(){
        return data.get(2) > 0;
    }

    public boolean showWarningIcon(){
        return data.get(3) > 0;
    }

    public int getHeightOfTierTexture(){
        int experience = data.get(2);

        // 60+ experience is max level, so just clamp there because the texture can't get any larger.
        return Math.max(0, Math.min(experience, 60));
    }

    @Override
    public ItemStack quickMoveStack(Player playerIn, int pIndex) {
        try {
            Slot sourceSlot = slots.get(pIndex);

            if (!sourceSlot.hasItem())
                return ItemStack.EMPTY;

            ItemStack sourceStack = sourceSlot.getItem();
            ItemStack copyOfSourceStack = sourceStack.copy();

            // Check if the slot clicked is one of the vanilla container slots.
            if (pIndex < VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT) {
                // This is a vanilla container slot so merge the stack into the tile inventory. We block inputting into output slots.
                List<Tuple<Integer, Integer>> ranges = computeRanges(sourceStack);
                boolean moved = false;

                for(Tuple<Integer, Integer> range : ranges)
                    moved |= moveItemStackTo(sourceStack, range.getA(), range.getB(), insertsReversed()); // Bitwise OR to ensure all calls run.

                if (!moved)
                    return ItemStack.EMPTY;
            } else if (pIndex < TE_INVENTORY_FIRST_SLOT_INDEX + getInputSlotCount() + getOutputSlotCount()) {
                // This is a TE slot so merge the stack into the players inventory.
                if (!blockEntity.mayExtract(pIndex) || !moveItemStackTo(sourceStack, VANILLA_FIRST_SLOT_INDEX, VANILLA_FIRST_SLOT_INDEX + VANILLA_SLOT_COUNT, false))
                    return ItemStack.EMPTY;
            } else {
                System.out.println("Invalid slotIndex:" + pIndex);

                return ItemStack.EMPTY;
            }
            // If stack size <= 0 (the entire stack was moved) set slot contents to null.
            if (sourceStack.getCount() <= 0)
                sourceSlot.set(ItemStack.EMPTY);
            else
                sourceSlot.setChanged();

            sourceSlot.onTake(playerIn, sourceStack);

            return copyOfSourceStack;
        } catch(Exception e){
            Decorator.LOGGER.error("Failed to quick move stack: " + e.getLocalizedMessage());

            return ItemStack.EMPTY;
        }
    }

    /** since moveItemStackTo doesn't grant access to slots and expects ranges, this function can determine the lists of ranges that it's allowed to use.
     * By overriding mayInsert, you can determine what kinds of items may go in which slots.
     *
     * @param stack
     * @return
     */
    private List<Tuple<Integer, Integer>> computeRanges(ItemStack stack){
        List<Tuple<Integer, Integer>> tuples = new ArrayList<>();
        Tuple<Integer, Integer> currentTuple = null;
        int slotIndex = TE_INVENTORY_FIRST_SLOT_INDEX;

        for(int i = 0; i < getInputSlotCount(); i++){
            if(blockEntity.mayInsert(stack, i)){
                if(currentTuple == null) // If not keeping track of a tuple yet, make a new tuples.
                    currentTuple = new Tuple<>(slotIndex, slotIndex + 1); // Tuples for ranges are (N, N - 1), meaning the last index is excluded.
                else
                    currentTuple.setB(currentTuple.getB() + 1); // Still in current range, so extend by one.
            } else{ // May not insert, so add the current tuple to the list if we're currently tracking one.
                if(currentTuple != null){
                    tuples.add(currentTuple);
                    currentTuple = null; // Reset for next tuple.
                }
            }

            slotIndex++; // Always increase slot index.
        }

        if(currentTuple != null) // If we're still tracking a tuple but ran out of slots, add the tuple.
            tuples.add(currentTuple);

        return tuples;
    }

    @Override
    public boolean stillValid(Player pPlayer) {
        return stillValid(ContainerLevelAccess.create(level, blockEntity.getBlockPos()), pPlayer, getBlock());
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

    protected boolean insertsReversed(){
        return false;
    }

    // ABSTRACT METHODS ========================
    /** Is the menu currently crafting?
     *
     * @return
     */
    public abstract boolean isCrafting();

    /** How many inputs slots should the menu?
     *
     * @return
     */
    public abstract int getInputSlotCount();

    /** How many ouptut slots should the menu have?
     *
     * @return
     */
    protected abstract int getOutputSlotCount();

    /** Return the coordinates of where to start drawing the given slot number.
     * Slots are 18x18 (16x16 for item with some spacing around it).
     * For placement, take coordinates of which pixel (starting top left) of your GUI it should start on.
     * Don't include screen size, that's done automatically. You use the inner 16x16's top left as the coordinate!
     *
     * @param slot
     * @return
     */
    protected abstract SlotLocation getLocationOfSlot(int slot);

    /** Return the block registration this menu belongs to.
     *
     * @return
     */
    protected abstract Block getBlock();
}
