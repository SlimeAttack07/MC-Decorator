package slimeattack07.decorator.block;

import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.item.MachineBlockItem;
import slimeattack07.decorator.item.ModItems;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Decorator.MOD_ID);

    public static final DeferredBlock<DisassemblyTable> DISASSEMBLY_TABLE = registerBlock("disassembly_table",
        () -> new DisassemblyTable(props("disassembly_table").strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<DraftingTable> DRAFTING_TABLE = registerMachineBlock("drafting_table",
        () -> new DraftingTable(props("drafting_table").strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<DecorationCrafter> DECORATION_CRAFTER = registerMachineBlock("decoration_crafter",
        () -> new DecorationCrafter(props("decoration_crafter").strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<DecorationScanner> DECORATION_SCANNER = registerMachineBlock("decoration_scanner",
        () -> new DecorationScanner(props("decoration_scanner").strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }

    // HELPER METHODS
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> dBlock = BLOCKS.register(name, block);
        registerBlockItem(name, dBlock);

        return dBlock;
    }

    private static <T extends Block> void registerMachineBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new MachineBlockItem(block.get(), new Item.Properties()));
    }

    private static <T extends Block> DeferredBlock<T> registerMachineBlock(String name, Supplier<T> block){
        DeferredBlock<T> dBlock = BLOCKS.register(name, block);
        registerMachineBlockItem(name, dBlock);

        return dBlock;
    }

    // While this looks pointless here, the 1.21.11 version requires calling '.setId(registryId)' on the properties.
    // By having this method here, I don't need to make any changes to this file when adding new items here and merging to the 1.21.11 version.
    private static BlockBehaviour.Properties props(String name){
        return BlockBehaviour.Properties.of();
    }
}
