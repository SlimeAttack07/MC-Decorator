package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.item.ModItems;
import slimeattack07.decorator.item.PrintedDecorationItem;
import slimeattack07.decorator.screen.DecorationScannerMenu;
import slimeattack07.decorator.sound.ModSounds;
import slimeattack07.decorator.util.ItemUtils;
import slimeattack07.decorator.util.ModTags;

public class DecorationScannerBE extends AbstractMachineBE {
    private static final int TEMPLATE_INPUT_SLOT = 0;
    private static final int DECORATION_INPUT_SLOT = 1;
    private static final int TEMPLATE_OUTPUT_SLOT = 2;
    private static final int DECORATION_OUTPUT_SLOT = 3;

    // Caching for performance. Null if not (re)evaluated.
    private Boolean canCraft = null;

    public DecorationScannerBE(BlockPos pos, BlockState blockState) {
        super(pos, blockState, ModBlockEntities.DECORATION_SCANNER_BE.get());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new DecorationScannerMenu(i, inventory, this, data);
    }

    @Override
    protected boolean craftItem() {
        if(!canCraft(true))
            return false;

        ItemStack template = ItemUtils.getStackInSlot(itemHandler, TEMPLATE_INPUT_SLOT);
        ItemStack decoration = ItemUtils.getStackInSlot(itemHandler, DECORATION_INPUT_SLOT);

        ItemStack newOutput = new ItemStack(ModItems.DECORATION_SCAN.get());
        ItemStack currentTemplateOutput = itemHandler.getStackInSlot(TEMPLATE_OUTPUT_SLOT);
        ItemStack currentDecorationOutput = itemHandler.getStackInSlot(DECORATION_OUTPUT_SLOT);

        if(!isBasicCompatible(currentDecorationOutput, decoration))
            return false; // Something else is obstructing the output for the decoration.

        if(!isBasicCompatible(currentTemplateOutput, newOutput))
            return false; // Something else is obstructing the output for the template (or it has reached max stack size).

        String expectedDecoration = BuiltInRegistries.ITEM.getKey(decoration.getItem()).toString();

        if(decoration.getItem() instanceof PrintedDecorationItem printedDecoration)
            expectedDecoration += "," + printedDecoration.getTierString();

        if(!currentTemplateOutput.isEmpty() && !expectedDecoration.equals(currentTemplateOutput.get(ModDataComponents.DECORATION.get())))
            return false; // A different template already occupies the template output slot.

        if(currentTemplateOutput.isEmpty()){
            currentTemplateOutput = newOutput;
            currentTemplateOutput.set(ModDataComponents.DECORATION.get(), expectedDecoration);
        } else
            currentTemplateOutput.setCount(currentTemplateOutput.getCount() + 1);

        if(currentDecorationOutput.isEmpty())
            currentDecorationOutput = new ItemStack(decoration.getItem());
        else
            currentDecorationOutput.setCount(currentDecorationOutput.getCount() + 1);

        ItemUtils.extractFromSlot(itemHandler, TEMPLATE_INPUT_SLOT, 1);
        ItemUtils.extractFromSlot(itemHandler, DECORATION_INPUT_SLOT, 1);
        ItemUtils.setStackInSlot(itemHandler, TEMPLATE_OUTPUT_SLOT, currentTemplateOutput);
        ItemUtils.setStackInSlot(itemHandler, DECORATION_OUTPUT_SLOT, currentDecorationOutput);

        return true;
    }

    @Override
    protected boolean hasRecipe() {
        return canCraft(false);
    }

    private boolean canCraft(boolean forceEval) {
        if(level == null)
            canCraft = null;
        else if (canCraft == null || forceEval) {
            ItemStack input = ItemUtils.getStackInSlot(itemHandler, TEMPLATE_INPUT_SLOT);

            if(input.getTags().anyMatch(tag -> tag.equals(ModTags.Items.SCAN_TEMPLATES))){
                input = ItemUtils.getStackInSlot(itemHandler, DECORATION_INPUT_SLOT);
                canCraft = input.getTags().anyMatch(tag -> tag.equals(ModTags.Items.PRINTED_DECORATIONS));
            } else
                canCraft = false;
        }

        return canCraft != null && canCraft;
    }

    @Override
    protected int getTotalSlots() {
        return 4;
    }

    @Override
    protected int getFirstOutputSlot() {
        return TEMPLATE_OUTPUT_SLOT;
    }

    @Override
    protected int getMaxProgress() {
        return 1200; // 60 seconds.
    }

    @Override
    protected String getRegistryName() {
        return "decoration_scanner";
    }

    @Override
    protected SoundEvent getCraftingInProgressSound() {
        return ModSounds.DECORATION_SCANNER_CRAFTING.get();
    }

    @Override
    protected SoundEvent getCraftingFinishedSound() {
        return ModSounds.DECORATION_SCANNER_FINISHED.get();
    }

    @Override
    public boolean mayInsert(ItemStack itemStack, int slot) {
        TagKey<Item> matchingTag = slot == 0? ModTags.Items.SCAN_TEMPLATES : ModTags.Items.PRINTED_DECORATIONS;

        return itemStack.getTags().anyMatch(tag -> tag.equals(matchingTag));
    }

    @Override
    protected void onMachineContentsChanged() {
        canCraft = null;
    }
}
