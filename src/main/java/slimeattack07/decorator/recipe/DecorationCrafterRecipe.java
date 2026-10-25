package slimeattack07.decorator.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.*;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeattack07.decorator.Decorator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public record DecorationCrafterRecipe(Ingredient instructions, List<SizedIngredient> inputsOne, List<SizedIngredient> inputsTwo, List<SizedIngredient> inputsThree,
        List<SizedIngredient> inputsFour, ItemStack output, int stepDuration) implements Recipe<DecorationCrafterRecipeInput> {

    @Override
    public boolean matches(DecorationCrafterRecipeInput recipeInput, Level level) {
        if(level.isClientSide())
            return false;

        // First check if the correct instructions are in the machine.
        if(!instructions.test(recipeInput.instructions()))
            return false;

        /* Simple logic:
         * 1. Build a map of how many of each item is required. The merge function will merge duplicate item stacks, so e.g. 2x '4 iron ingot' becomes 1x '8 iron ingot'.
         * 2. For each provided input, remove the amount of that item from the map (computeIfPresent only runs for items already present in the map, so inputs that don't
         * match the recipe (excess) are ignored).
         * 3. If the removed input leads to a 0 or negative count of the item, then we know enough of that material is present. By returning null, the key gets removed from the map.
         * 4. Check if the map is empty at the end. Because null values remove the key, we know all materials are present if all keys have been removed (empty map remains).
         */
        try {
            Map<ItemLike, Integer> countsMap = getStageInputsAsMap(recipeInput.stage());
            recipeInput.inputs().forEach(input -> countsMap.computeIfPresent(input.getItem(), (key, required) -> {
                int remaining = required - input.getCount();

                return remaining <= 0 ? null : remaining;
            }));

            return countsMap.isEmpty();
        } catch(Exception e){
            Decorator.LOGGER.warn("Recipe crashed: " + e.getLocalizedMessage());

            return false;
        }
    }

    public List<SizedIngredient> getStageInputs(int stage){
        return switch(stage){
            case 2 -> inputsTwo;
            case 3 -> inputsThree;
            case 4 -> inputsFour;
            default -> inputsOne;
        };
    }

    public Map<ItemLike, Integer> getStageInputsAsMap(int stage){
        List<SizedIngredient> inputs = getStageInputs(stage);
        Map<ItemLike, Integer> countsMap = new HashMap<>();
        // We make an assumption that each 'ingredient' has exactly one stack, and we don't support tags for this machine.
        inputs.forEach(input -> {
            Ingredient ingredient = input.ingredient();

            if(ingredient.isCustom())
                Decorator.LOGGER.warn("NeoForge doesn't support custom ingredients inside SizedIngredient, so this ingredient will never work correctly: " + ingredient);
            else
                countsMap.merge(ingredient.getValues().get(0).value(), input.count(), Integer::sum);
        });

        return countsMap;
    }

    @Override
    public ItemStack assemble(DecorationCrafterRecipeInput recipeInput, HolderLookup.Provider provider) {
        return output.copy();
    }

    @Override
    public RecipeSerializer<? extends Recipe<DecorationCrafterRecipeInput>> getSerializer() {
        return ModRecipes.DECORATION_CRAFTER_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<DecorationCrafterRecipeInput>> getType() {
        return ModRecipes.DECORATION_CRAFTER_TYPE.get();
    }

    @Override
    public RecipeBookCategory recipeBookCategory() {
        return RecipeBookCategories.CRAFTING_MISC;
    }

    @Override
    public PlacementInfo placementInfo() {
        return PlacementInfo.create(instructions);
    }

    public static class Serializer implements RecipeSerializer<DecorationCrafterRecipe> {
        public static final MapCodec<DecorationCrafterRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            Ingredient.CODEC.fieldOf("instructions").forGetter(DecorationCrafterRecipe::instructions),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("inputsOne").forGetter(DecorationCrafterRecipe::inputsOne),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("inputsTwo").forGetter(DecorationCrafterRecipe::inputsTwo),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("inputsThree").forGetter(DecorationCrafterRecipe::inputsThree),
            SizedIngredient.NESTED_CODEC.listOf().fieldOf("inputsFour").forGetter(DecorationCrafterRecipe::inputsFour),
            ItemStack.CODEC.fieldOf("result").forGetter(DecorationCrafterRecipe::output),
            Codec.INT.fieldOf("stepDuration").forGetter(DecorationCrafterRecipe::stepDuration)
        ).apply(inst, DecorationCrafterRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, DecorationCrafterRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    Ingredient.CONTENTS_STREAM_CODEC.encode(buf, recipe.instructions());
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputsOne());
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputsTwo());
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputsThree());
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputsFour());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.output());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.stepDuration());
                },buf -> new DecorationCrafterRecipe(
                    Ingredient.CONTENTS_STREAM_CODEC.decode(buf),
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    ItemStack.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
                ));

        @Override
        public MapCodec<DecorationCrafterRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, DecorationCrafterRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}