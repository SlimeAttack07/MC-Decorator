package slimeattack07.decorator.recipe;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.Level;

public record DisassemblyTableRecipe(Ingredient inputItem, ItemStack output) implements Recipe<DisassemblyTableRecipeInput> {
    // draftInput & output ==> Read From JSON File!
    // DisassemblyTableRecipeInput --> INVENTORY of the Block Entity

    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create();
        list.add(inputItem);

        return list;
    }

    @Override
    public boolean matches(DisassemblyTableRecipeInput recipeInput, Level level) {
        return !level.isClientSide() && inputItem.test(recipeInput.getItem(0));
    }

    @Override
    public ItemStack assemble(DisassemblyTableRecipeInput recipeInput, HolderLookup.Provider provider) {
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
    public RecipeSerializer<? extends Recipe<DisassemblyTableRecipeInput>> getSerializer() {
        return ModRecipes.DISASSEMBLY_TABLE_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DisassemblyTableRecipeInput>> getType() {
        return ModRecipes.DISASSEMBLY_TABLE_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<DisassemblyTableRecipe> {
        public static final MapCodec<DisassemblyTableRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("ingredient").forGetter(DisassemblyTableRecipe::inputItem),
            ItemStack.CODEC.fieldOf("result").forGetter(DisassemblyTableRecipe::output)
        ).apply(inst, DisassemblyTableRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DisassemblyTableRecipe> STREAM_CODEC =
            StreamCodec.composite(
                Ingredient.CONTENTS_STREAM_CODEC, DisassemblyTableRecipe::inputItem,
                ItemStack.STREAM_CODEC, DisassemblyTableRecipe::output,
                DisassemblyTableRecipe::new);

        @Override
        public MapCodec<DisassemblyTableRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DisassemblyTableRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}