package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.recipe.DisassemblyTableRecipe;
import slimeattack07.decorator.recipe.DisassemblyTableRecipeInput;
import slimeattack07.decorator.recipe.ModRecipes;
import slimeattack07.decorator.screen.DisassemblyTableMenu;
import slimeattack07.decorator.util.ItemUtils;

import java.util.Optional;

public class DisassemblyTableBE extends BlockEntity implements MenuProvider {
    public final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(2) {
        @Override
        protected void onContentsChanged(int index, ItemStack previousContents) {
            setChanged();

            if(!level.isClientSide())
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    };

    private static final int INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 1;

    protected final ContainerData data;
    private int progress = 0;
    private int maxProgress = 72;

    public DisassemblyTableBE(BlockPos pos, BlockState blockState) {
        super(ModBlockEntities.DISASSEMBLY_TABLE_BE.get(), pos, blockState);

        data = new ContainerData() {
            @Override
            public int get(int i) {
                return switch (i) {
                    case 0 -> DisassemblyTableBE.this.progress;
                    case 1 -> DisassemblyTableBE.this.maxProgress;
                    default -> 0;
                };
            }

            @Override
            public void set(int i, int value) {
                switch (i) {
                    case 0: DisassemblyTableBE.this.progress = value;
                    case 1: DisassemblyTableBE.this.maxProgress = value;
                }
            }

            @Override
            public int getCount() {
                return 2;
            }
        };
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable("block.decorator.disassembly_table");
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DisassemblyTableMenu(i, inventory, this, data);
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
        output.putInt("growth_chamber.progress", progress); // TODO change
        output.putInt("growth_chamber.max_progress", maxProgress);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        itemHandler.deserialize(input);
        progress = input.getIntOr("growth_chamber.progress", 0); // TODO change
        maxProgress = input.getIntOr("growth_chamber.max_progress", 0);
    }

    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        if(hasRecipe()) {
            increaseCraftingProgress();
            setChanged(level, blockPos, blockState);

            if(hasCraftingFinished()) {
                craftItem();
                resetProgress();
                level.playSound(null, blockPos, SoundEvents.ZOMBIE_BREAK_WOODEN_DOOR, SoundSource.BLOCKS, .25F, 1);
            }
        } else
            resetProgress();
    }

    private void craftItem() {
        Optional<RecipeHolder<DisassemblyTableRecipe>> recipe = getCurrentRecipe();
        ItemStack output = recipe.get().value().output();

        ItemUtils.extractFromSlot(itemHandler, INPUT_SLOT, 1);
        ItemUtils.setStackInSlot(itemHandler, OUTPUT_SLOT, output.getItem(), ItemUtils.getAmountInSlot(itemHandler, OUTPUT_SLOT) + output.getCount());
    }

    private void resetProgress() {
        progress = 0;
        maxProgress = 72;
    }

    private boolean hasCraftingFinished() {
        return this.progress >= this.maxProgress;
    }

    private void increaseCraftingProgress() {
        progress++;
    }

    private boolean hasRecipe() {
        Optional<RecipeHolder<DisassemblyTableRecipe>> recipe = getCurrentRecipe();

        if(recipe.isEmpty())
            return false;

        ItemStack output = recipe.get().value().output();

        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
    }

    private Optional<RecipeHolder<DisassemblyTableRecipe>> getCurrentRecipe() {
        return ((ServerLevel) this.level).recipeAccess().getRecipeFor(ModRecipes.DISASSEMBLY_TABLE_TYPE.get(),
            new DisassemblyTableRecipeInput(ItemUtils.getStackInSlot(itemHandler, INPUT_SLOT)), level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) {
        return ItemUtils.isSlotEmpty(itemHandler, OUTPUT_SLOT) || ItemUtils.isSameItem(itemHandler, OUTPUT_SLOT, output.getItem());
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = ItemUtils.getMaxStackSize(itemHandler, OUTPUT_SLOT, 64);
        int currentCount = ItemUtils.getAmountInSlot(itemHandler, OUTPUT_SLOT);

        return maxCount >= currentCount + count;
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
}
