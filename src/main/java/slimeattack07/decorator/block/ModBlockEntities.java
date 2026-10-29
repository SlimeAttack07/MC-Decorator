package slimeattack07.decorator.block;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.blockentities.DecorationCrafterBE;
import slimeattack07.decorator.block.blockentities.DecorationScannerBE;
import slimeattack07.decorator.block.blockentities.DisassemblyTableBE;
import slimeattack07.decorator.block.blockentities.DraftingTableBE;

import java.util.function.Supplier;

public class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
        DeferredRegister.create(BuiltInRegistries.BLOCK_ENTITY_TYPE, Decorator.MOD_ID);

    public static final Supplier<BlockEntityType<DisassemblyTableBE>> DISASSEMBLY_TABLE_BE = BLOCK_ENTITIES.register("disassembly_table_be",
        () -> new BlockEntityType<>(DisassemblyTableBE::new, ModBlocks.DISASSEMBLY_TABLE.get()));

    public static final Supplier<BlockEntityType<DraftingTableBE>> DRAFTING_TABLE_BE = BLOCK_ENTITIES.register("drafting_table_be",
        () -> new BlockEntityType<>(DraftingTableBE::new, ModBlocks.DRAFTING_TABLE.get()));

    public static final Supplier<BlockEntityType<DecorationCrafterBE>> DECORATION_CRAFTER_BE = BLOCK_ENTITIES.register("decoration_crafter_be",
        () -> new BlockEntityType<>(DecorationCrafterBE::new, ModBlocks.DECORATION_CRAFTER.get()));

    public static final Supplier<BlockEntityType<DecorationScannerBE>> DECORATION_SCANNER_BE = BLOCK_ENTITIES.register("decoration_scanner_be",
        () -> new BlockEntityType<>(DecorationScannerBE::new, ModBlocks.DECORATION_SCANNER.get()));

    public static void register(IEventBus eventBus) {
        BLOCK_ENTITIES.register(eventBus);
    }
}