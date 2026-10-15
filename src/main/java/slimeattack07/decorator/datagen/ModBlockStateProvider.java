package slimeattack07.decorator.datagen;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.registries.DeferredBlock;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlocks;

public class ModBlockStateProvider extends BlockStateProvider {
    public ModBlockStateProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, Decorator.MOD_ID, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        blockWithItem(ModBlocks.DISASSEMBLY_TABLE);
    }

    private void blockWithItem(DeferredBlock<?> deferredBlock) {
//        simpleBlockWithItem(deferredBlock.get(), cubeAll(deferredBlock.get()));
    }
}
