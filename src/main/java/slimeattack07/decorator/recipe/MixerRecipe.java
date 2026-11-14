package slimeattack07.decorator.recipe;

import com.mojang.serialization.Codec;
import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.ItemUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public record MixerRecipe(List<SizedIngredient> inputs, ItemStack output, Optional<ItemStack> byProduct, int stepDuration) implements Recipe<MixerRecipeInput> {
    @Override
    public NonNullList<Ingredient> getIngredients() {
        NonNullList<Ingredient> list = NonNullList.create(); // Don't use this method.

        return list;
    }

    @Override
    public boolean matches(MixerRecipeInput recipeInput, Level level) {
        if(level.isClientSide())
            return false;

        try {
            Map<ItemLike, Integer> countsMap = ItemUtils.getInputsAsMap(inputs);

            return ItemUtils.matchesInputs(recipeInput.inputs(), countsMap);
        } catch(Exception e){
            Decorator.LOGGER.warn("Recipe crashed: " + e.getLocalizedMessage());

            return false;
        }
    }

    @Override
    public ItemStack assemble(MixerRecipeInput recipeInput, HolderLookup.Provider provider) {
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
    public RecipeSerializer<? extends Recipe<MixerRecipeInput>> getSerializer() {
        return ModRecipes.MIXER_SERIALIZER.get();
    }

    @Override
    public RecipeType<? extends Recipe<MixerRecipeInput>> getType() {
        return ModRecipes.MIXER_TYPE.get();
    }

    public static class Serializer implements RecipeSerializer<MixerRecipe> {
        public static final MapCodec<MixerRecipe> CODEC = RecordCodecBuilder.mapCodec(inst -> inst.group(
            SizedIngredient.FLAT_CODEC.listOf().fieldOf("inputs").forGetter(MixerRecipe::inputs),
            ItemStack.CODEC.fieldOf("result").forGetter(MixerRecipe::output),
            ItemStack.CODEC.optionalFieldOf("byProduct").forGetter(MixerRecipe::byProduct),
            Codec.INT.fieldOf("stepDuration").forGetter(MixerRecipe::stepDuration)
        ).apply(inst, MixerRecipe::new));

        public static final StreamCodec<RegistryFriendlyByteBuf, MixerRecipe> STREAM_CODEC =
            StreamCodec.of(
                (buf, recipe) -> {
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).encode(buf, recipe.inputs());
                    ItemStack.STREAM_CODEC.encode(buf, recipe.output());
                    ByteBufCodecs.optional(ItemStack.STREAM_CODEC).encode(buf, recipe.byProduct());
                    ByteBufCodecs.VAR_INT.encode(buf, recipe.stepDuration());
                },buf -> new MixerRecipe(
                    SizedIngredient.STREAM_CODEC.apply(ByteBufCodecs.list()).decode(buf),
                    ItemStack.STREAM_CODEC.decode(buf),
                    ByteBufCodecs.optional(ItemStack.STREAM_CODEC).decode(buf),
                    ByteBufCodecs.VAR_INT.decode(buf)
                ));

        @Override
        public MapCodec<MixerRecipe> codec() {
            return CODEC;
        }

        @Override
        public StreamCodec<RegistryFriendlyByteBuf, MixerRecipe> streamCodec() {
            return STREAM_CODEC;
        }
    }
}