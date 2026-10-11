package slimeattack07.decorator.datagen;

import net.minecraft.client.data.models.BlockModelGenerators;
import net.minecraft.client.data.models.ItemModelGenerators;
import net.minecraft.client.data.models.ModelProvider;
import net.minecraft.client.data.models.MultiVariant;
import net.minecraft.client.data.models.blockstates.MultiVariantGenerator;
import net.minecraft.client.data.models.blockstates.PropertyDispatch;
import net.minecraft.client.data.models.model.*;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.data.PackOutput;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import org.jspecify.annotations.NonNull;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlocks;
import slimeattack07.decorator.item.ModItems;
import slimeattack07.decorator.util.ResourceUtils;

import java.util.stream.Stream;

public class ModModelsProvider extends ModelProvider {

    public ModModelsProvider(PackOutput output) {
        super(output, Decorator.MOD_ID);
    }

    @Override
    protected void registerModels(@NonNull BlockModelGenerators blockModels, ItemModelGenerators itemModels) {
        // Items.
        itemModels.generateFlatItem(ModItems.BROKEN_STICK.get(), ModelTemplates.FLAT_ITEM);

        // Blocks.
        generateSidesSameTopDifferent(ModBlocks.DISASSEMBLY_TABLE.get(), blockModels);
//        blockModels.createTrivialCube(ModBlocks.PLACE_STRUCTURE.get());

        // Block states.
//        generateStagedModels(ModBlocks.STAGED3.get(), "staged3", blockModels);
//        generateStagedModels(ModBlocks.STAGED5.get(), "staged5", blockModels);
    }

    @Override
    protected @NonNull Stream<? extends Holder<Block>> getKnownBlocks() {
        return ModBlocks.BLOCKS.getEntries().stream();
    }

    @Override
    protected @NonNull Stream<? extends Holder<Item>> getKnownItems() {
        return ModItems.ITEMS.getEntries().stream();
    }

    private void generateSidesSameTopDifferent(Block block, BlockModelGenerators blockModels) {
        TextureMapping texturemapping = new TextureMapping()
            .put(TextureSlot.PARTICLE, TextureMapping.getBlockTexture(block, "_side"))
            .put(TextureSlot.DOWN, TextureMapping.getBlockTexture(block, "_side"))
            .put(TextureSlot.UP, TextureMapping.getBlockTexture(block, "_up"))
            .put(TextureSlot.NORTH, TextureMapping.getBlockTexture(block, "_side"))
            .put(TextureSlot.SOUTH, TextureMapping.getBlockTexture(block, "_side"))
            .put(TextureSlot.EAST, TextureMapping.getBlockTexture(block, "_side"))
            .put(TextureSlot.WEST, TextureMapping.getBlockTexture(block, "_side"));
        blockModels.blockStateOutput.accept(BlockModelGenerators.createSimpleBlock(block, BlockModelGenerators.plainVariant(ModelTemplates.CUBE.create(block, texturemapping, blockModels.modelOutput))));
    }

//    private void generateStagedModels(StagedBlock stagedBlock, String id, BlockModelGenerators blockModels){
//        generateStagedModels(stagedBlock, id, stagedBlock.getStageProperty(), 1, stagedBlock.getStages(), blockModels);
//        blockModels.registerSimpleItemModel(stagedBlock, ModelLocationUtils.getModelLocation(stagedBlock, "_stage_1"));
//    }
//
//    private void generateStagedModels(Block block, String id, IntegerProperty property, int minValue, int maxValue, BlockModelGenerators blockModels){
//        var modelDispatch = PropertyDispatch.initial(property);
//
//        for(int i = minValue; i <= maxValue; i++)
//            modelDispatch = modelDispatch.select(i, variantOfExistingModel("block/" + id + "_" + property.getName() + "_" + i));
//
//        blockModels.blockStateOutput.accept(MultiVariantGenerator.dispatch(block).with(modelDispatch).with(BlockModelGenerators.ROTATION_HORIZONTAL_FACING));
//    }

//    private MultiVariant variantOfExistingModel(String id){
//        return BlockModelGenerators.plainVariant(ResourceUtils.identifierOf(id));
//    }
}
