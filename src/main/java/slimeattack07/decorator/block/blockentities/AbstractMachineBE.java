package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentMap;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.util.MachineTier;

public abstract class AbstractMachineBE extends BlockEntity implements MenuProvider {
    protected final ItemStackHandler itemHandler = new ItemStackHandler(getTotalSlots()) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                failureCooldown = 0;
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return mayInsert(stack, slot);
        }
    };

    public IItemHandler automationHandler = new IItemHandler() {
        @Override
        public int getSlots() {
            return itemHandler.getSlots();
        }

        @Override
        public ItemStack getStackInSlot(int slot) {
            return itemHandler.getStackInSlot(slot);
        }

        @Override
        public ItemStack insertItem(int slot, ItemStack itemStack, boolean simulate) {
            return slot >= getFirstOutputSlot() || !mayInsert(itemStack, slot) ? itemStack : itemHandler.insertItem(slot, itemStack, simulate);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot < getFirstOutputSlot() ? ItemStack.EMPTY : itemHandler.extractItem(slot, amount, simulate);
        }

        @Override
        public int getSlotLimit(int i) {
            return itemHandler.getSlotLimit(i);
        }

        @Override
        public boolean isItemValid(int slot, ItemStack itemStack) {
            return slot < getFirstOutputSlot() && mayInsert(itemStack, slot);
        }
    };

    protected final ContainerData data;

    private int maxProgress = getMaxProgress();
    private int progress = 0;
    private int experience = 0;

    // Caching for performance.
    private MachineTier lastTier = MachineTier.BASIC;
    private int lastExperience;
    private int failureCooldown = 0; // Doesn't need to be saved to block entity.
    private int nextSoundAt = 0;

    public AbstractMachineBE(BlockPos pos, BlockState blockState, BlockEntityType<?> type) {
        super(type, pos, blockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> AbstractMachineBE.this.progress;
                    case 1 -> AbstractMachineBE.this.maxProgress;
                    case 2 -> AbstractMachineBE.this.experience;
                    case 3 -> AbstractMachineBE.this.failureCooldown;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: AbstractMachineBE.this.progress = value;
                    case 1: AbstractMachineBE.this.maxProgress = value;
                    case 2: AbstractMachineBE.this.experience = value;
                    case 3: AbstractMachineBE.this.failureCooldown = value;
                }
            }

            @Override
            public int getCount() {
                return 4;
            }
        };
    }

    public IItemHandler getAutomationHandler(Direction side){
        return automationHandler;
    }

    public IItemHandler getItemHandler(){
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
        SimpleContainer inventory = new SimpleContainer(itemHandler.getSlots());

        for (int i = 0; i < itemHandler.getSlots(); i++)
            inventory.setItem(i, itemHandler.getStackInSlot(i));

        Containers.dropContents(level, worldPosition, inventory);
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);

        tag.put("inventory", itemHandler.serializeNBT(registries));
        tag.putInt("decorator.progress", progress);
        tag.putInt("decorator.max_progress", maxProgress);
        tag.putInt("decorator.experience", experience);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);

        itemHandler.deserializeNBT(registries, tag.getCompound("inventory"));
        progress = tag.getInt("decorator.progress");
        maxProgress = tag.getInt("decorator.max_progress");
        experience = tag.getInt("decorator.experience");
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
    protected void applyImplicitComponents(DataComponentInput input) { // This lets the block restore its experience when placed if it had experience when broken.
        super.applyImplicitComponents(input);

        experience = input.getOrDefault(ModDataComponents.EXPERIENCE.get(), 0);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(hasRecipe()) {
            if(failureCooldown > 0)
                failureCooldown--;
            else {
                increaseCraftingProgress(blockPos);
                setChanged(level, blockPos, blockState);

                if (hasCraftingFinished()) {
                    if(craftItem()) {
                        resetProgress();
                        level.playSound(null, blockPos, getCraftingFinishedSound(), SoundSource.BLOCKS, .25F, 1);
                        experience = Math.min(60, experience + 1);
                    } else // We assume that if crafting fails, it's due to something in the output slot blocking creating a new item there.
                        failureCooldown = 1200; // As long as the slots don't change, it'll always fail so we use a cooldown to lighten the load.
                }
            }
        } else
            resetProgress();
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = getMaxProgress();
        nextSoundAt = 0;
    }

    private boolean hasCraftingFinished() {
        return progress >= maxProgress;
    }

    protected void increaseCraftingProgress(BlockPos pos) {
        if(progress < maxProgress) {
            progress = Math.min(maxProgress, progress + getCurrentMachineTier().getMultiplier());

            if(progress > nextSoundAt) {
                level.playSound(null, pos, getCraftingInProgessSound(), SoundSource.BLOCKS, .25F, 1);
                nextSoundAt += 100;
            }
        }
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

    // ABSTRACT METHODS ======================================
    protected abstract int getTotalSlots();

    protected abstract int getFirstOutputSlot();

    protected abstract int getMaxProgress();

    protected abstract String getRegistryName();

    protected abstract boolean hasRecipe();

    protected abstract boolean craftItem();

    protected abstract SoundEvent getCraftingInProgessSound();

    protected abstract SoundEvent getCraftingFinishedSound();

    /** Is it allowed to move the given ItemStack to this slot? You only need to specify whether it's allowed, checking for space is already done by this class.
     *
     * @param itemStack
     * @param slot
     * @return
     */
    public abstract boolean mayInsert(ItemStack itemStack, int slot);
}
