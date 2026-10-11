package slimeattack07.salvager.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.LootTableSubProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;

import slimeattack07.salvager.block.ModBlocks;
import slimeattack07.salvager.block.staged.StagedBlock;
import slimeattack07.salvager.util.ResourceUtils;

import java.util.List;
import java.util.function.BiConsumer;

public class ModLootTableSubProvider implements LootTableSubProvider {
    public ModLootTableSubProvider(HolderLookup.Provider provider) {
    }

    @Override
    public void generate(BiConsumer<ResourceKey<LootTable>, LootTable.Builder> output) {
        ModBlocks.BLOCKS.getEntries().stream().map(deferredBlock -> deferredBlock.get())
            .filter(block -> block instanceof StagedBlock).forEach(block -> {
                StagedBlock stagedBlock = (StagedBlock) block; // No check needed, filter ensures all remaining blocks are of type StagedBlock.

                for(int i = 1; i <= stagedBlock.getStages(); i++){
                    List<ItemStack> droppedStacks = stagedBlock.getDroppedItemsForStage(i);
                    LootTable.Builder lootTablebuilder = LootTable.lootTable();

                    for(ItemStack droppedStack : droppedStacks) {
                        lootTablebuilder = lootTablebuilder.withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1)) // Pool with exactly 1 roll.
                            .add(LootItem.lootTableItem(droppedStack.getItem()) // Drop this item.
                                .apply(SetItemCountFunction.setCount(ConstantValue.exactly(droppedStack.getCount()))))); // Drop this many.
                    }

                    output.accept(ResourceUtils.genLootTableId(stagedBlock,"_stagedrops_" + i), lootTablebuilder);
                }
            });
    }



//    @Override
//    public LootTableSubProvider withConditions(List<ICondition> conditions) {
//        return LootTableSubProvider.super.withConditions(conditions);
//    }
}