package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.recipe.*;
import slimeattack07.decorator.screen.DraftingTableMenu;
import slimeattack07.decorator.util.ModTags;

import java.util.Optional;

public class DraftingTableBE extends AbstractBE {
    private static final int DRAFT_INPUT_SLOT = 0;
    private static final int GROUP_FOCUS_INPUT_SLOT = 1;
    private static final int TIER_FOCUS_INPUT_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;

    public DraftingTableBE(BlockPos pos, BlockState blockState) {
        super(pos, blockState, ModBlockEntities.DRAFTING_TABLE_BE.get());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DraftingTableMenu(i, inventory, this, data);
    }

    protected boolean craftItem() {
        Optional<RecipeHolder<DraftingTableRecipe>> recipe = getCurrentRecipe();
        ItemStack output = recipe.get().value().output();
        int chance = getCurrentMachineTier().getFocusChance();

        ItemStack stack = itemHandler.getStackInSlot(OUTPUT_SLOT);
        CompoundTag expectedTag = new CompoundTag();
        boolean newStack = false;

        if(stack.equals(ItemStack.EMPTY)){
            stack = new ItemStack(output.getItem());
            newStack = true;
        }

        ItemStack groupFocus = itemHandler.getStackInSlot(GROUP_FOCUS_INPUT_SLOT);

        if(!groupFocus.equals(ItemStack.EMPTY)){
            groupFocus.getTags().filter(entry -> !entry.equals(ModTags.Items.GROUP_FOCUSES) &&
                entry.location().getNamespace().equals(Decorator.MOD_ID) && entry.location().getPath().contains("group_focus")).findFirst().ifPresent(groupTag -> {
                expectedTag.putString("group_focus", groupTag.location().toString());
                expectedTag.putDouble("group_chance", chance);
            });
        }

        ItemStack tierFocus = itemHandler.getStackInSlot(TIER_FOCUS_INPUT_SLOT);
        if(!tierFocus.equals(ItemStack.EMPTY)){
            tierFocus.getTags().filter(entry -> !entry.equals(ModTags.Items.TIER_FOCUSES) &&
                entry.location().getNamespace().equals(Decorator.MOD_ID) && entry.location().getPath().contains("tier_focus")).findFirst().ifPresent(groupTag -> {
                expectedTag.putString("tier_focus", groupTag.location().toString());
                expectedTag.putDouble("tier_chance", chance);
            });
        }

        // Check if we can actually craft by checking if the applied group & tier focus are compatible with whatever is already in the output.
        // This prevents e.g. using a legend tier focus to upgrade an entire stack of items created without focus, which would be cheating.
        if(newStack) {
            if(!expectedTag.isEmpty())
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(expectedTag));
        } else if(!stack.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, expectedTag).equals(CustomData.of(expectedTag)))
            return false; // This will trigger a cooldown for performance.
        else
            stack.setCount(stack.getCount() + output.getCount());

        if(!expectedTag.isEmpty())
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(expectedTag));

        itemHandler.extractItem(DRAFT_INPUT_SLOT, 1, false);
        itemHandler.extractItem(GROUP_FOCUS_INPUT_SLOT, 1, false);
        itemHandler.extractItem(TIER_FOCUS_INPUT_SLOT, 1, false);
        itemHandler.setStackInSlot(OUTPUT_SLOT, stack);

        return true;
    }

    @Override
    protected void onCraftingFinished(Level level, BlockPos pos) {
        level.playSound(null, pos, SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, .25F, 1);
    }

    @Override
    protected boolean hasRecipe() {
        Optional<RecipeHolder<DraftingTableRecipe>> recipe = getCurrentRecipe();

        if(recipe.isEmpty())
            return false;

        ItemStack output = recipe.get().value().output();

        return canInsertAmountIntoOutputSlot(output.getCount()) && canInsertItemIntoOutputSlot(output);
    }

    private Optional<RecipeHolder<DraftingTableRecipe>> getCurrentRecipe() {
        return this.level.getRecipeManager()
            .getRecipeFor(ModRecipes.DRAFTING_TABLE_TYPE.get(), new DraftingTableRecipeInput(itemHandler.getStackInSlot(DRAFT_INPUT_SLOT)), level);
    }

    private boolean canInsertItemIntoOutputSlot(ItemStack output) { // TODO: Check if this handles NBT correctly.
        return itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() || itemHandler.getStackInSlot(OUTPUT_SLOT).getItem().equals(output.getItem());
    }

    private boolean canInsertAmountIntoOutputSlot(int count) {
        int maxCount = itemHandler.getStackInSlot(OUTPUT_SLOT).isEmpty() ? 64 : itemHandler.getStackInSlot(OUTPUT_SLOT).getMaxStackSize();
        int currentCount = itemHandler.getStackInSlot(OUTPUT_SLOT).getCount();

        return maxCount >= currentCount + count;
    }

    @Override
    protected int getTotalSlots() {
        return 4;
    }

    @Override
    protected int getMaxProgress() {
        return 320; // 16 seconds.
    }

    @Override
    protected String getRegistryName() {
        return "drafting_table";
    }
}
