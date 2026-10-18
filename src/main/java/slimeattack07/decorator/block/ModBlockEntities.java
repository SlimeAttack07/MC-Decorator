package slimeattack07.decorator.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.blockentities.DisassemblyTableBE;
import slimeattack07.decorator.block.blockentities.DraftingTableBE;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Decorator.MOD_ID);

    public static final Supplier<BlockEntityType<DisassemblyTableBE>> DISASSEMBLY_TABLE_BE = BLOCK_ENTITIES.register("disassembly_table_be",
        () -> BlockEntityType.Builder.of(DisassemblyTableBE::new, ModBlocks.DISASSEMBLY_TABLE.get()).build(null));

    public static final Supplier<BlockEntityType<DraftingTableBE>> DRAFTING_TABLE_BE = BLOCK_ENTITIES.register("drafting_table_be",
        () -> BlockEntityType.Builder.of(DraftingTableBE::new, ModBlocks.DRAFTING_TABLE.get()).build(null));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}