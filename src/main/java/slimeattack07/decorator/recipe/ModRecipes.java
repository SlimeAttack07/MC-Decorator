package slimeattack07.decorator.recipe;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;

public class ModRecipes {
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
        DeferredRegister.create(Registries.RECIPE_SERIALIZER, Decorator.MOD_ID);
    public static final DeferredRegister<RecipeType<?>> TYPES =
        DeferredRegister.create(Registries.RECIPE_TYPE, Decorator.MOD_ID);

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DisassemblyTableRecipe>> DISASSEMBLY_TABLE_SERIALIZER =
        SERIALIZERS.register("disassembly_table", DisassemblyTableRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<DisassemblyTableRecipe>> DISASSEMBLY_TABLE_TYPE =
        TYPES.register("disassembly_table", () -> new RecipeType<DisassemblyTableRecipe>() {
            @Override
            public String toString() {
                return "disassembly_table";
            }
        });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DraftingTableRecipe>> DRAFTING_TABLE_SERIALIZER =
        SERIALIZERS.register("drafting_table", DraftingTableRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<DraftingTableRecipe>> DRAFTING_TABLE_TYPE =
        TYPES.register("drafting_table", () -> new RecipeType<DraftingTableRecipe>() {
            @Override
            public String toString() {
                return "drafting_table";
            }
        });

    public static final DeferredHolder<RecipeSerializer<?>, RecipeSerializer<DecorationCrafterRecipe>> DECORATION_CRAFTER_SERIALIZER =
        SERIALIZERS.register("decoration_crafter", DecorationCrafterRecipe.Serializer::new);
    public static final DeferredHolder<RecipeType<?>, RecipeType<DecorationCrafterRecipe>> DECORATION_CRAFTER_TYPE =
        TYPES.register("decoration_crafter", () -> new RecipeType<>() {
            @Override
            public String toString() {
                return "decoration_crafter";
            }
        });

    public static void register(IEventBus eventBus) {
        SERIALIZERS.register(eventBus);
        TYPES.register(eventBus);
    }
}