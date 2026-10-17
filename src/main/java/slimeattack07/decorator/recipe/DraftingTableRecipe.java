package slimeattack07.decorator.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

import java.util.Optional;

public record DraftingTableRecipe(Ingredient draftInput, ItemStack output) implements Recipe<DraftingTableRecipeInput> {
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(draftInput);

        return list;
    }

    @Override
    public boolean matches(DraftingTableRecipeInput recipeInput, Level level) {
        return !level.isClientSide() && draftInput.test(recipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(DraftingTableRecipeInput recipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public boolean canCraftInDimensions(int i, int i1) {
        return true;
    }

    @Override
    public ItemStack getResultItem(HolderLookup.Provider provider) {
        return output;
    }

    @Override
    public RecipeSerializer<? extends Recipe<DraftingTableRecipeInput>> getSerializer() {
        return ModRecipes.DRAFTING_TABLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DraftingTableRecipeInput>> getType() {
        return ModRecipes.DRAFTING_TABLE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DraftingTableRecipe> {
        public static final MapCodec<DraftingTableRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("draftInput").forGetter(DraftingTableRecipe::draftInput),
            ItemStack.CODEC.fieldOf("result").forGetter(DraftingTableRecipe::output)
        ).apply(inst, DraftingTableRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DraftingTableRecipe> STREAM_CODEC =
            StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, DraftingTableRecipe::draftInput,
                ItemStack.STREAM_CODEC, DraftingTableRecipe::output,
                DraftingTableRecipe::new);

        @Override
        public MapCodec<DraftingTableRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DraftingTableRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}