package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.recipe.DecorationCrafterRecipe;
import slimeattack07.decorator.recipe.DecorationCrafterRecipeInput;
import slimeattack07.decorator.recipe.ModRecipes;
import slimeattack07.decorator.screen.DecorationCrafterMenu;
import slimeattack07.decorator.sound.ModSounds;
import slimeattack07.decorator.util.ModTags;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class DecorationCrafterBE extends AbstractMachineBE{
    private static final int INSTRUCTIONS_INPUT_SLOT = 0;
    private static final int OUTPUT_SLOT = 7; // Everything between these two slots is material input.

    private int stage = 1; // Between 1 and 4 (both inclusive).

    // Caching for performance
    private DecorationCrafterRecipe lastRecipe;

    public DecorationCrafterBE(BlockPos pos, BlockState blockState) {
        super(pos, blockState, ModBlockEntities.DECORATION_CRAFTER_BE.get());
    }

    @Override
    protected int getTotalSlots() {
        return 8;
    }

    @Override
    protected int getFirstOutputSlot() {
        return OUTPUT_SLOT;
    }

    @Override
    protected int getMaxProgress() {
        return lastRecipe == null ? 20 : lastRecipe.stepDuration();
    }

    @Override
    protected String getRegistryName() {
        return "decoration_crafter";
    }

    @Override
    protected boolean craftItem() {
        DecorationCrafterRecipe recipe = getCurrentRecipe();

        if(recipe == null)
            return false; // Shouldn't happen here, but just in case.

        ItemStack output = recipe.output();
        ItemStack stack = itemHandler.getStackInSlot(OUTPUT_SLOT);

        // Only stage 4 actually produces output, so before that we don't have to do any checking for the output slot.
        if(stage >= 4){
            if(!isBasicCompatible(stack, output))// Something else is obstructing the output.
                return false;

            if(stack.isEmpty())
                stack = new ItemStack(output.getItem());
            else
                stack.setCount(stack.getCount() + output.getCount());
        }

        Map<ItemLike, Integer> countsMap = recipe.getStageInputsAsMap(stage);
        // Consume the inputs by going from last to first material input slot. This leaves behind a neater inventory than doing it in normal order.
        for(int i = OUTPUT_SLOT - 1; i > INSTRUCTIONS_INPUT_SLOT; i--){
            ItemStack materialStack = itemHandler.getStackInSlot(i);
            Integer requiredAmount = countsMap.get(materialStack.getItem());

            if(requiredAmount != null){ // If null, then this item is not required for the recipe we've just crafted.
                int usedAmount = Math.min(materialStack.getCount(), requiredAmount);
                itemHandler.extractItem(i, usedAmount, false);
                countsMap.computeIfPresent(materialStack.getItem(), (key, oldRequired) -> {
                    int stillRequired = oldRequired - usedAmount;

                    return stillRequired > 0 ? stillRequired : null; // Returning null removes the mapping, indicating we've consumed everything we need to for this item.
                });
            }
        }

        if(countsMap.isEmpty()){
            if(stage >= 4){ // Final stage succeeded, create output.
                itemHandler.setStackInSlot(OUTPUT_SLOT, stack);
                stage = 1;
            } else // This stage succeeded, but it's not the final stage. Don't create output, just increase stage.
                stage++;
        }

        else{
            Decorator.LOGGER.warn("Failed to extract all required items. Remaining items that still needed extraction: " + countsMap);
            stage = 1;
            // TODO: Generate a 'Botched Creation' when I've implemented that.
            return true;
        }

        return true;
    }

    @Override
    protected SoundEvent getCraftingInProgressSound() {
        return switch(stage){
            case 2 -> ModSounds.DECORATION_CRAFTER_CRAFTING2.get();
            case 3 -> ModSounds.DECORATION_CRAFTER_CRAFTING3.get();
            case 4 -> ModSounds.DECORATION_CRAFTER_CRAFTING4.get();
            default-> ModSounds.DECORATION_CRAFTER_CRAFTING1.get();
        };
    }

    @Override
    protected SoundEvent getCraftingFinishedSound() {
        return stage == 1 ? ModSounds.DECORATION_CRAFTER_FINISHED.get() : null; // See experiencedGained for explanation why I'm using stage 1 instead of 4 here.
    }

    @Override
    public boolean mayInsert(ItemStack itemStack, int slot) {
        // Only allow instructions in slot 0 (provided machine isn't crafting (so stage <= 1)), allow anything but instructions in the other input slots.
        return slot == INSTRUCTIONS_INPUT_SLOT ? stage <= 1 && itemStack.getTags().anyMatch(this::isInstructions) : itemStack.getTags().noneMatch(this::isInstructions);
    }

    @Override
    public boolean mayExtract(int slot) {
        return slot != INSTRUCTIONS_INPUT_SLOT ||  stage <= 1;
    }

    @Override
    protected int experiencedGained() {
        // This looks wrong, but since we modify the stage in craftItem and that method comes before this one, the stage has already changed.
        // This means that stage 4 succeeding & crafting something set the stage to 1. Other stages will have increased the stage, so it would not be 1 at this point.
        // We only grant exp for final crafts, so stage 4 crafts or in this case stage 1 since it's already been reset!
        return stage == 1 ? 1 : 0;
    }

    private boolean isInstructions(TagKey<Item> tagKey){
        return tagKey.equals(ModTags.Items.DECORATION_INSTRUCTIONS);
    }

    @Override
    public @Nullable AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DecorationCrafterMenu(i, inventory, this, data);
    }

    @Override
    protected boolean hasRecipe() {
        return getCurrentRecipe() != null;
    }

    private DecorationCrafterRecipe getCurrentRecipe() {
        if(level == null)
            return null;

        List<ItemStack> materialInputs = new ArrayList<>();

        for(int i = INSTRUCTIONS_INPUT_SLOT + 1; i < OUTPUT_SLOT; i++) {
            ItemStack stack = itemHandler.getStackInSlot(i);

            if(!stack.isEmpty())
                materialInputs.add(stack);
        }

        DecorationCrafterRecipeInput recipeInput = new DecorationCrafterRecipeInput(itemHandler.getStackInSlot(INSTRUCTIONS_INPUT_SLOT), materialInputs, stage);

        if(lastRecipe != null && lastRecipe.matches(recipeInput, level)) // This already takes care of checking the correct stage because we pass it above  ^
            return lastRecipe;

        level.getRecipeManager().getRecipeFor(ModRecipes.DECORATION_CRAFTER_TYPE.get(), recipeInput, level)
            .ifPresentOrElse(holder -> lastRecipe = holder.value(), () -> lastRecipe = null);

        // Make sure to update the maxProgress when a new recipe is selected to prevent the first craft's stage 1 from using the wrong maxProgress value.
        if(lastRecipe != null)
            maxProgress = lastRecipe.stepDuration();

        return lastRecipe;
    }

    @Override
    protected int getContainerDataCount() {
        return 5;
    }

    @Override
    protected int getUnknownFromContainerData(int i) {
        return i == 4 ? stage : 0;
    }

    @Override
    protected void setUnknownForContainerData(int i, int value) {
        if(i == 4)
            DecorationCrafterBE.this.stage = value;
    }
}
