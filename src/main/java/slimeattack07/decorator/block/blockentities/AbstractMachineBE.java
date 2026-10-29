package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.util.ItemUtils;
import slimeattack07.decorator.util.MachineTier;

public abstract class AbstractMachineBE extends BlockEntity implements MenuProvider {
    protected final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(getTotalSlots()) {
        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            setChanged();

            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                failureCooldown = 0;
                noRecipeCooldown = 0;
                onMachineContentsChanged();
            }
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return mayInsert(resource, 1, slot);
        }
    };

    public ResourceHandler<ItemResource> automationHandler = new ResourceHandler<>() {
        @Override
        public int size() {
            return itemHandler.size();
        }

        @Override
        public ItemResource getResource(int slot) {
            return itemHandler.getResource(slot);
        }

        @Override
        public long getAmountAsLong(int slot) {
            return itemHandler.getAmountAsLong(slot);
        }

        @Override
        public long getCapacityAsLong(int slot, ItemResource itemResource) {
            return itemHandler.getCapacityAsLong(slot, itemResource);
        }

        @Override
        public boolean isValid(int slot, ItemResource itemResource) {
            return slot < getFirstOutputSlot() && mayInsert(itemResource, 1, slot);
        }

        @Override
        public int insert(int slot, ItemResource itemResource, int amount, TransactionContext transactionContext) {
            return (slot >= getFirstOutputSlot() || !mayInsert(itemResource, amount, slot)) ? 0 : itemHandler.insert(slot, itemResource, amount, transactionContext);
        }

        @Override
        public int insert(ItemResource resource, int amount, TransactionContext transaction) {
            int remaining = amount;

            for (int slot = 0; slot < getFirstOutputSlot() && remaining > 0; slot++) {
                int inserted = insert(slot, resource, remaining, transaction);
                remaining -= inserted;
            }

            return amount - remaining;
        }

        @Override
        public int extract(int slot, ItemResource itemResource, int amount, TransactionContext transactionContext) {
            return slot < getFirstOutputSlot() ? 0 : itemHandler.extract(slot, itemResource, amount, transactionContext);
        }

        @Override
        public int extract(ItemResource resource, int amount, TransactionContext transaction) {
            int remaining = amount;
            int totalExtracted = 0;

            for (int slot = getFirstOutputSlot(); slot < getTotalSlots() && remaining > 0; slot++) {
                int extracted = extract(slot, resource, remaining, transaction);
                remaining -= extracted;
                totalExtracted += extracted;
            }

            return totalExtracted;
        }
    };

    protected final ContainerData data;

    protected int maxProgress = getMaxProgress();
    protected int progress = 0;
    private int experience = 0;
    private int nextSoundIn = 0; // Don't need to save this.

    // Caching for performance.
    private MachineTier lastTier = MachineTier.BASIC;
    private int lastExperience;
    private int failureCooldown = 0; // Doesn't need to be saved to block entity.
    private int noRecipeCooldown = 0;

    public AbstractMachineBE(BlockPos pos, BlockState blockState, BlockEntityType<?> type) {
        super(type, pos, blockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> AbstractMachineBE.this.progress;    // I don't remember why, but I know it's important to specify 'YourBEClass.this.yourProperty'.
                    case 1 -> AbstractMachineBE.this.maxProgress; // Otherwise, something breaks.
                    case 2 -> AbstractMachineBE.this.experience;
                    case 3 -> AbstractMachineBE.this.failureCooldown;
                    default -> getUnknownFromContainerData(i);
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0 -> AbstractMachineBE.this.progress = value;
                    case 1 -> AbstractMachineBE.this.maxProgress = value;
                    case 2 -> AbstractMachineBE.this.experience = value;
                    case 3 -> AbstractMachineBE.this.failureCooldown = value;
                    default -> setUnknownForContainerData(i, value);
                }
            }

            @Override
            public int getCount() {
                return getContainerDataCount();
            }
        };
    }

    /** How many indexes the container data understands.
     *
     * @return
     */
    protected int getContainerDataCount(){
        return 4;
    }

    /** Called for indexes outside AbstractBE's reserved 0-3 indexes.
     * Returns 0 by default;
     *
     * @param i
     * @return
     */
    protected int getUnknownFromContainerData(int i){
        return 0;
    }

    /** Called for indexes outside AbstractBE's reserved 0-3 indexes.
     * Does nothing by default.
     *
     * @param i
     * @param value
     */
    protected void setUnknownForContainerData(int i, int value){}

    public ResourceHandler<ItemResource> getAutomationHandler(Direction side){
        return automationHandler;
    }

    public ItemStacksResourceHandler getItemHandler(){
        return itemHandler;
    }

    protected MachineTier getCurrentMachineTier(){
        if(experience != lastExperience)
            lastTier = MachineTier.toTier(experience);

        return lastTier;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.decorator." + getRegistryName());
    }

    public void drops() {
        SimpleContainer inventory = new SimpleContainer(itemHandler.size());

        for (int i = 0; i < itemHandler.size(); i++)
            inventory.setItem(i, ItemUtils.getStackInSlot(itemHandler, i));

        Containers.dropContents(level, worldPosition, inventory);
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        drops();

        super.preRemoveSideEffects(pos, state);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        itemHandler.serialize(output);
        output.putInt("decorator.progress", progress);
        output.putInt("decorator.max_progress", maxProgress);
        output.putInt("decorator.experience", experience);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input);
        progress = input.getIntOr("decorator.progress", 0);
        maxProgress = input.getIntOr("decorator.max_progress", 0);
        experience = input.getIntOr("decorator.experience", 0);
    }

    public void setExperience(int experience) {
        this.experience = experience;
        setChanged();
    }

    @Override
    protected void collectImplicitComponents(DataComponentMap.Builder builder) { // This lets the dropped block keep it's experience.
        super.collectImplicitComponents(builder);

        builder.set(ModDataComponents.EXPERIENCE.get(), experience);
    }

    @Override
    protected void applyImplicitComponents(DataComponentGetter componentGetter) { // This lets the block restore its experience when placed if it had experience when broken.
        super.applyImplicitComponents(componentGetter);

        experience = componentGetter.getOrDefault(ModDataComponents.EXPERIENCE.get(), 0);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(nextSoundIn > 0)
            nextSoundIn--; // Modify sound timer here to ensure it doesn't linger between crafts.

        if(noRecipeCooldown > 0) { // Couldn't find a recipe last time and no slot has updated, so it'll still not find a recipe. Stop early.
            noRecipeCooldown--;

            return;
        }

        if(hasRecipe()) {
            if(failureCooldown > 0)
                failureCooldown--;
            else {
                increaseCraftingProgress(blockPos);
                setChanged(level, blockPos, blockState);

                if (hasCraftingFinished()) {
                    if(craftItem()) {
                        resetProgress();
                        SoundEvent soundEvent = getCraftingFinishedSound();

                        if(soundEvent != null)
                            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, .25F, 1);

                        experience = Math.min(60, experience + experiencedGained());
                    } else // We assume that if crafting fails, it's due to something in the output slot blocking creating a new item there.
                        failureCooldown = 1200; // As long as the slots don't change, it'll always fail so we use a cooldown to lighten the load.
                }
            }
        } else {
            resetProgress();
            noRecipeCooldown = 1200;
        }
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = getMaxProgress();
        noRecipeCooldown = 0;
    }

    private boolean hasCraftingFinished() {
        return progress >= maxProgress;
    }

    protected void increaseCraftingProgress(BlockPos pos) {
        if(progress < maxProgress) {
            progress = Math.min(maxProgress, progress + getCurrentMachineTier().getMultiplier());

            if(nextSoundIn <= 0) { // Decreasing timer happens in tick().
                level.playSound(null, pos, getCraftingInProgressSound(), SoundSource.BLOCKS, .25F, 1);
                nextSoundIn = 100;
            }
        }
    }

    /** Return how much experience a craft should grant.
     * Defaults to 1.
     *
     * @return
     */
    protected int experiencedGained(){
        return 1;
    }

    /** Checks if two item stacks are compatible excluding NBT.
     * It's compatible if they both have the same item and merging mergingIntoSource into source them would not exceed the max stack size.
     *
     * @param source
     * @param mergingIntoSource
     * @return
     */
    protected boolean isBasicCompatible(ItemStack source, ItemStack mergingIntoSource){
        if(source.isEmpty())
            return true;

        return source.getItem().equals(mergingIntoSource.getItem()) && source.getCount() + mergingIntoSource.getCount() <= source.getMaxStackSize();
    }

    /** Checks if two item stacks are compatible and share the same given tag.
     *
     * @param source
     * @param mergingInto
     * @param expectedTag
     * @return
     */
    protected boolean isAdvancedCompatible(ItemStack source, ItemStack mergingInto, CompoundTag expectedTag){
        if(!isBasicCompatible(source, mergingInto))
            return false;

        Object stackCustomDataObject = source.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, new CompoundTag());

        if(stackCustomDataObject instanceof CustomData customData)
            stackCustomDataObject = customData.copyTag();

        if(stackCustomDataObject instanceof CompoundTag stackTag)
            return (stackTag.isEmpty() && expectedTag.isEmpty()) || stackTag.equals(expectedTag);
        else
            return false; // It's not a CompoundTag, meaning it's present as something else since we didn't get an empty tag and therefore we probably can't merge the item.
    }

    /** Called when the contents of this machine change.
     * Does nothing by default.
     */
    protected void onMachineContentsChanged(){}

    /** Whether something may be extracted from this slot.
     * Returns true by default.
     *
     * @param slot
     * @return
     */
    public boolean mayExtract(int slot){
        return true;
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider pRegistries) {
        return saveWithoutMetadata(pRegistries);
    }

    @Nullable
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private boolean mayInsert(ItemResource itemResource, int amount, int slot){
        return mayInsert(itemResource.toStack(amount), slot);
    }

    // ABSTRACT METHODS ======================================
    /** Return the total amount of slots that the inventory has (input + output).
     *
     * @return
     */
    protected abstract int getTotalSlots();

    /** Where does the first output slot start? This block will assume that slots are ordered such that all slots after the first output slot are output slots and
     * all slots before are input slots.
     *
     * @return
     */
    protected abstract int getFirstOutputSlot();

    /** What is the initial max progress?
     * Recipe resets also reset the max progress to the initial value.
     *
     * @return
     */
    protected abstract int getMaxProgress();

    /** Return the registry name.
     *
     * @return
     */
    protected abstract String getRegistryName();

    /** Can this BE currently execute a valid recipe based on its current inputs?
     *
     * @return
     */
    protected abstract boolean hasRecipe();

    /** Called when the crafting process has finished and the output can be created.
     *
     * @return Whether the output has been successfully been created. Failure will temporarily delay the machine, but progress will not be reset.
     */
    protected abstract boolean craftItem();

    /** Return the sound to play while crafting is progress.
     * Ideally, this should be a sound clip of around 5 seconds.
     *
     * @return
     */
    protected abstract SoundEvent getCraftingInProgressSound();

    /** Return the sound to play when crafting finishes ({@link #craftItem()} returned true).
     *
     * @return
     */
    protected abstract SoundEvent getCraftingFinishedSound();

    /** Is it allowed to move the given ItemStack to this slot? You only need to specify whether it's allowed, checking for space is already done by this class.
     * You may assume this method is only called for input slots. Output slots are already excluded from insertion and do not call this method.
     *
     * @param itemStack
     * @param slot
     * @return
     */
    public abstract boolean mayInsert(ItemStack itemStack, int slot);
}
