package slimeattack07.decorator.recipe;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

public record DecorationCrafterRecipeInput(ItemStack instructions, List<ItemStack> inputs, int stage) implements RecipeInput {
    @Override
    public ItemStack getItem(int i) {
        return i == 0 ? instructions : i > 0 ? (i - 1 <= inputs.size() ? inputs.get(i - 1) : inputs.getLast()) : inputs.getFirst();
    }

    @Override
    public int size() {
        return inputs.size() + 1;
    }
}