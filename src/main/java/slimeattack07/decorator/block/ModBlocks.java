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
import slimeattack07.decorator.util.ResourceUtils;

import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Decorator.MOD_ID);

    public static final DeferredBlock<DisassemblyTable> DISASSEMBLY_TABLE = registerBlock("disassembly_table",
        () -> new DisassemblyTable(props("disassembly_table").strength(4f).requiresCorrectToolForDrops().sound(SoundType.IRON)));

    public static final DeferredBlock<DraftingTable> DRAFTING_TABLE = registerMachineBlock("drafting_table",
        () -> new DraftingTable(props("drafting_table").strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static final DeferredBlock<DecorationCrafter> DECORATION_CRAFTER = registerMachineBlock("decoration_crafter",
        () -> new DecorationCrafter(props("decoration_crafter").strength(4f).requiresCorrectToolForDrops().sound(SoundType.METAL)));

    public static void register(IEventBus eventBus){
        BLOCKS.register(eventBus);
    }

    // HELPER METHODS
    private static <T extends Block> void registerBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().setId(ResourceUtils.genId(block))));
    }

    private static <T extends Block> DeferredBlock<T> registerBlock(String name, Supplier<T> block){
        DeferredBlock<T> dBlock = BLOCKS.register(name, block);
        registerBlockItem(name, dBlock);

        return dBlock;
    }

    private static BlockBehaviour.Properties props(String name){
        return BlockBehaviour.Properties.of().setId(ResourceUtils.genBlockId(name));
    }

    private static <T extends Block> void registerMachineBlockItem(String name, DeferredBlock<T> block){
        ModItems.ITEMS.register(name, () -> new MachineBlockItem(block.get(), new Item.Properties().setId(ResourceUtils.genId(block))));
    }

    private static <T extends Block> DeferredBlock<T> registerMachineBlock(String name, Supplier<T> block){
        DeferredBlock<T> dBlock = BLOCKS.register(name, block);
        registerMachineBlockItem(name, dBlock);

        return dBlock;
    }
}
