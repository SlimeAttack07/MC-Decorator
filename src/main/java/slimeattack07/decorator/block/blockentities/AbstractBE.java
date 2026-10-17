package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.util.MachineTier;

import java.util.stream.Stream;

public abstract class AbstractBE extends BlockEntity implements MenuProvider {
    public final ItemStackHandler itemHandler = new ItemStackHandler(getTotalSlots()) {
        @Override
        protected void onContentsChanged(int slot) {
            setChanged();

            if(!level.isClientSide()) {
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
                failureCooldown = 0;
            }
        }
    };

    protected final ContainerData data;

    private int maxProgress = getMaxProgress();
    private int progress = 0;
    private int experience = 0;

    private int failureCooldown = 0;

    // Caching for performance.
    private MachineTier lastTier = MachineTier.BASIC;
    private int lastExperience;

    public AbstractBE(BlockPos pos, BlockState blockState, BlockEntityType<?> type) {
        super(type, pos, blockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> AbstractBE.this.progress;
                    case 1 -> AbstractBE.this.maxProgress;
                    case 2 -> AbstractBE.this.experience;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: AbstractBE.this.progress = value;
                    case 1: AbstractBE.this.maxProgress = value;
                    case 2: AbstractBE.this.experience = value;
                }
            }

            @Override
            public int getCount() {
                return 3;
            }
        };
    }

    protected MachineTier getCurrentMachineTier(){
        if(experience != lastExperience)
            lastTier = Stream.of(MachineTier.reversed()).filter(entry -> entry.hasReachedTier(experience)).findFirst().orElse(MachineTier.BASIC);

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

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(hasRecipe()) {
            if(failureCooldown > 0)
                failureCooldown--;
            else {
                increaseCraftingProgress();
                setChanged(level, blockPos, blockState);

                if (hasCraftingFinished()) {
                    if(craftItem()) {
                        resetProgress();
                        onCraftingFinished(level, blockPos);
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
    }

    private boolean hasCraftingFinished() {
        return progress >= maxProgress;
    }

    protected void increaseCraftingProgress() {
        progress += getCurrentMachineTier().getMultiplier();
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

    protected abstract int getMaxProgress();

    protected abstract String getRegistryName();

    protected abstract boolean hasRecipe();

    protected abstract boolean craftItem();

    protected abstract void onCraftingFinished(Level level, BlockPos pos);
}
