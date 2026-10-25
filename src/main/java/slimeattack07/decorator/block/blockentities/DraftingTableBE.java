package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.recipe.DraftingTableRecipe;
import slimeattack07.decorator.recipe.DraftingTableRecipeInput;
import slimeattack07.decorator.recipe.ModRecipes;
import slimeattack07.decorator.screen.DraftingTableMenu;
import slimeattack07.decorator.sound.ModSounds;
import slimeattack07.decorator.util.ItemUtils;
import slimeattack07.decorator.util.ModTags;

public class DraftingTableBE extends AbstractMachineBE {
    private static final int DRAFT_INPUT_SLOT = 0;
    private static final int GROUP_FOCUS_INPUT_SLOT = 1;
    private static final int TIER_FOCUS_INPUT_SLOT = 2;
    private static final int OUTPUT_SLOT = 3;

    // Caching for performance.
    private DraftingTableRecipe lastRecipe;

    public DraftingTableBE(BlockPos pos, BlockState blockState) {
        super(pos, blockState, ModBlockEntities.DRAFTING_TABLE_BE.get());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DraftingTableMenu(i, inventory, this, data);
    }

    @Override
    protected boolean craftItem() {
        DraftingTableRecipe recipe = getCurrentRecipe();

        if(recipe == null)
            return false; // Shouldn't happen here, but just in case.

        ItemStack output = recipe.output();
        ItemStack stack = ItemUtils.getStackInSlot(itemHandler, OUTPUT_SLOT);

        if(!isBasicCompatible(stack, output))
            return false; // Something else is obstructing the output.

        int chance = getCurrentMachineTier().getFocusChance();
        CompoundTag expectedTag = new CompoundTag();
        boolean newStack = false;

        if(stack.isEmpty()){
            stack = new ItemStack(output.getItem());
            newStack = true;
        }

        ItemStack groupFocus = ItemUtils.getStackInSlot(itemHandler, GROUP_FOCUS_INPUT_SLOT);

        if(!groupFocus.isEmpty()){
            groupFocus.getTags().filter(entry -> !entry.equals(ModTags.Items.GROUP_FOCUSES) &&
                entry.location().getNamespace().equals(Decorator.MOD_ID) && entry.location().getPath().contains("group_focus")).findFirst().ifPresent(groupTag -> {
                expectedTag.putString("group_focus", groupTag.location().toString());
                expectedTag.putDouble("group_chance", chance);
            });
        }

        ItemStack tierFocus = ItemUtils.getStackInSlot(itemHandler, TIER_FOCUS_INPUT_SLOT);
        if(!tierFocus.isEmpty()){
            tierFocus.getTags().filter(entry -> !entry.equals(ModTags.Items.TIER_FOCUSES) &&
                entry.location().getNamespace().equals(Decorator.MOD_ID) && entry.location().getPath().contains("tier_focus")).findFirst().ifPresent(groupTag -> {
                expectedTag.putString("tier_focus", groupTag.location().toString());
                expectedTag.putDouble("tier_chance", chance);
            });
        }

        if(tierFocus.isEmpty() || chance < 100){
            CompoundTag distributionTag = new CompoundTag();
            getCurrentMachineTier().getDistribution().forEach((key, value) -> distributionTag.putDouble(key.name(), value));
            expectedTag.put("distribution", distributionTag);
        }

        // Check if we can actually craft by checking if the applied group & tier focus are compatible with whatever is already in the output.
        // This prevents e.g. using a legend tier focus to upgrade an entire stack of items created without focus, which would be cheating.
        if(newStack) {
            if(!expectedTag.isEmpty())
                stack.set(DataComponents.CUSTOM_DATA, CustomData.of(expectedTag));
        } else{
            if(isAdvancedCompatible(stack, output, expectedTag))
                stack.setCount(stack.getCount() + output.getCount());
            else
                return false; // The current output is the same item but has different NBT, so we can't create the new output.
        }

        if(!expectedTag.isEmpty())
            stack.set(DataComponents.CUSTOM_DATA, CustomData.of(expectedTag));

        ItemUtils.extractFromSlot(itemHandler, DRAFT_INPUT_SLOT, 1);
        ItemUtils.extractFromSlot(itemHandler, GROUP_FOCUS_INPUT_SLOT, 1);
        ItemUtils.extractFromSlot(itemHandler, TIER_FOCUS_INPUT_SLOT, 1);
        ItemUtils.setStackInSlot(itemHandler, OUTPUT_SLOT, stack);

        return true;
    }

    @Override
    protected boolean hasRecipe() {
        return getCurrentRecipe() != null;
    }

    private DraftingTableRecipe getCurrentRecipe() {
        if(level == null)
            return null;

        DraftingTableRecipeInput recipeInput = new DraftingTableRecipeInput(ItemUtils.getStackInSlot(itemHandler, DRAFT_INPUT_SLOT));

        if(lastRecipe != null && lastRecipe.matches(recipeInput, level))
            return lastRecipe;

        ((ServerLevel) this.level).recipeAccess().getRecipeFor(ModRecipes.DRAFTING_TABLE_TYPE.get(), recipeInput, level)
            .ifPresentOrElse(holder -> lastRecipe = holder.value(), () -> lastRecipe = null);

        return lastRecipe;
    }

    @Override
    protected int getTotalSlots() {
        return 4;
    }

    @Override
    protected int getFirstOutputSlot() {
        return OUTPUT_SLOT;
    }

    @Override
    protected int getMaxProgress() {
        return 320; // 16 seconds.
    }

    @Override
    protected String getRegistryName() {
        return "drafting_table";
    }

    @Override
    protected SoundEvent getCraftingInProgressSound() {
        return ModSounds.DRAFTING_TABLE_CRAFTING.get();
    }

    @Override
    protected SoundEvent getCraftingFinishedSound() {
        return ModSounds.DRAFTING_TABLE_FINISHED.get();
    }

    @Override
    public boolean mayInsert(ItemStack itemStack, int slot) {
        return switch(slot){
            case 1 -> itemStack.getTags().anyMatch(entry -> entry.equals(ModTags.Items.GROUP_FOCUSES));
            case 2 -> itemStack.getTags().anyMatch(entry -> entry.equals(ModTags.Items.TIER_FOCUSES));
            default -> itemStack.getTags().noneMatch(entry -> entry.equals(ModTags.Items.GROUP_FOCUSES) || entry.equals(ModTags.Items.TIER_FOCUSES)); // Slot 0.
        };
    }
}
