package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.component.DataComponentGetter;
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
            return slot >= getFirstOutputSlot() || !mayInsert(itemResource, amount, slot) ? 0 : itemHandler.insert(slot, itemResource, amount, transactionContext);
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

    private boolean mayInsert(ItemResource itemResource, int amount, int slot){
        return mayInsert(itemResource.toStack(amount), slot);
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
