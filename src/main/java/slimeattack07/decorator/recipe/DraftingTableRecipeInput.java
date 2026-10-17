package slimeattack07.decorator.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

public record DraftingTableRecipeInput(ItemStack draftInput) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return draftInput;
    }

    @Override
    public int size() {
        return 1;
    }
}