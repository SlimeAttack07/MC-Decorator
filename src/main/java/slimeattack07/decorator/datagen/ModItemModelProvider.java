package slimeattack07.decorator.datagen;

import net.minecraft.data.PackOutput;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.client.model.generators.ItemModelProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlocks;
import slimeattack07.decorator.item.ModItems;

public class ModItemModelProvider extends ItemModelProvider {

    public ModItemModelProvider(PackOutput output, ExistingFileHelper existingFileHelper) {
        super(output, Decorator.MOD_ID, existingFileHelper);
    }

    @Override
    protected void registerModels() {
        ModItems.ITEMS.getEntries().forEach(entry -> {
            if(!(entry.get() instanceof BlockItem))
                basicItem(entry.get());
        });
    }
}
