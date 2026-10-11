package slimeattack07.decorator.block;

import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.phys.BlockHitResult;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.salvager.item.ModItems;
import slimeattack07.salvager.structures.GenerationContext;
import slimeattack07.salvager.structures.GenerationContextHolder;
import slimeattack07.salvager.structures.ThemeContext;
import slimeattack07.salvager.util.ResourceUtils;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

public class ModBlocks {

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(Decorator.MOD_ID);

    public static final DeferredBlock<DisassemblyTable> DISASSEMBLY_TABLE = registerBlock("disassembly_table",
        () -> new DisassemblyTable(BlockBehaviour.Properties.of().setId(ResourceUtils.genBlockId("disassembly_table"))
        .strength(4f).requiresCorrectToolForDrops().sound(SoundType.IRON)));

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
}
