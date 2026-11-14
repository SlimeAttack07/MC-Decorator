package slimeattack07.decorator.block.blockentities;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.jetbrains.annotations.Nullable;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.recipe.MixerRecipe;
import slimeattack07.decorator.recipe.MixerRecipeInput;
import slimeattack07.decorator.recipe.ModRecipes;
import slimeattack07.decorator.screen.MixerMenu;
import slimeattack07.decorator.sound.ModSounds;
import slimeattack07.decorator.util.ItemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MixerBE extends AbstractMachineBE {
    private int stageTwoProgress = 0;
    private int stageTwoMaxProgress = 0;
    private int stageThreeProgress = 0;
    private int stageThreeMaxProgress = 0;

    private int nextChamberTwoSoundIn = 0;
    private int nextChamberThreeSoundIn = 0;
    private int failureCooldownChamberOne = 0;
    private int failureCooldownChamberTwo = 0;
    private int failureCooldownChamberThree = 0;

    // Caching for performance. Null if not (re)evaluated.
    private Boolean canCraft = null;
    private MixerRecipe lastRecipe;
    private int noChamberTwoInputCooldown = 0;
    private int noChamberThreeInputCooldown = 0;
    private Boolean hasChamberTwoInputs = null;
    private Boolean hasChamberThreeInputs = null;

    public MixerBE(BlockPos pos, BlockState blockState) {
        super(pos, blockState, ModBlockEntities.MIXER_BE.get());
    }

    @Nullable
    @Override
    public AbstractContainerMenu createMenu(int i, Inventory inventory, Player player) {
        return new MixerMenu(i, inventory, this, data);
    }

    @Override
    public void tick(Level level, BlockPos blockPos, BlockState blockState) {
        tickChamberOne(level, blockPos, blockState);
        tickChamberTwo(level, blockPos, blockState);
        tickChamberThree(level, blockPos, blockState);

        failureCooldown = Math.max(failureCooldownChamberOne, Math.max(failureCooldownChamberTwo, failureCooldownChamberThree));
    }

    private void tickChamberOne(Level level, BlockPos blockPos, BlockState blockState){
        if(nextSoundIn > 0)
            nextSoundIn--; // Modify sound timer here to ensure it doesn't linger between crafts.

        if(noRecipeCooldown > 0) { // Couldn't find a recipe last time and no slot has updated, so it'll still not find a recipe. Stop early.
            noRecipeCooldown--;

            return;
        }

        if(hasRecipe()) {
            if(failureCooldownChamberOne > 0)
                failureCooldownChamberOne--;
            else {
                increaseCraftingChamberOneProgress(blockPos);
                setChanged(level, blockPos, blockState);

                if (progress >= maxProgress) {
                    if(craftItem())
                        resetProgress(1);
                    else // We assume that if crafting fails, it's due to something in the output slot blocking creating a new item there.
                        failureCooldownChamberOne = 1200; // As long as the slots don't change, it'll always fail so we use a cooldown to lighten the load.
                }
            }
        } else {
            resetProgress(1);
            noRecipeCooldown = 1200;
        }
    }

    private void tickChamberTwo(Level level, BlockPos blockPos, BlockState blockState){
        if(nextChamberTwoSoundIn > 0)
            nextChamberTwoSoundIn--; // Modify sound timer here to ensure it doesn't linger between crafts.

        if(noChamberTwoInputCooldown > 0) { // The chamber was empty and no slot has updated, so it's still empty.
            noChamberTwoInputCooldown--;

            return;
        }

        if(hasChamberTwoInputs()) {
            if (failureCooldownChamberTwo > 0)
                failureCooldownChamberTwo--;
            else{
                increaseCraftingChamberTwoProgress(blockPos);
                setChanged(level, blockPos, blockState);

                if (stageTwoProgress >= stageTwoMaxProgress) {
                    if (moveToChamberThree())
                        resetProgress(2);
                    else // We assume that if crafting fails, it's due to something in the output slot blocking creating a new item there.
                        failureCooldownChamberTwo = 1200; // As long as the slots don't change, it'll always fail so we use a cooldown to lighten the load.
                }
            }
        }
    }

    private void tickChamberThree(Level level, BlockPos blockPos, BlockState blockState){
        if(nextChamberThreeSoundIn > 0)
            nextChamberThreeSoundIn--; // Modify sound timer here to ensure it doesn't linger between crafts.

        if(noChamberThreeInputCooldown > 0) { // The chamber was empty and no slot has updated, so it's still empty.
            noChamberThreeInputCooldown--;

            return;
        }

        if(hasChamberThreeInputs()) {
            if (failureCooldownChamberThree > 0)
                failureCooldownChamberThree--;
            else{
                increaseCraftingChamberThreeProgress(blockPos);
                setChanged(level, blockPos, blockState);

                if (stageThreeProgress >= stageThreeMaxProgress) {
                    if (moveToOutput()) {
                        resetProgress(3);
                        SoundEvent soundEvent = getCraftingFinishedSound();

                        if (soundEvent != null)
                            level.playSound(null, blockPos, soundEvent, SoundSource.BLOCKS, .25F, 1);

                        experience = Math.min(60, experience + experiencedGained());
                    } else // We assume that if crafting fails, it's due to something in the output slot blocking creating a new item there.
                        failureCooldownChamberThree = 1200; // As long as the slots don't change, it'll always fail so we use a cooldown to lighten the load.
                }
            }
        }
    }

    protected void increaseCraftingChamberOneProgress(BlockPos pos) {
        if(progress < maxProgress) {
            progress = Math.min(maxProgress, progress + getCurrentMachineTier().getMultiplier());

            if(nextSoundIn <= 0) { // Decreasing timer happens in tick().
                level.playSound(null, pos, ModSounds.MIXER_CRAFTING1.get(), SoundSource.BLOCKS, .25F, 1);
                nextSoundIn = 100;
            }
        }
    }

    protected void increaseCraftingChamberTwoProgress(BlockPos pos) {
        if(stageTwoProgress < stageTwoMaxProgress) {
            stageTwoProgress = Math.min(stageTwoMaxProgress, stageTwoProgress + getCurrentMachineTier().getMultiplier());

            if(nextChamberTwoSoundIn <= 0) { // Decreasing timer happens in tick().
                level.playSound(null, pos, ModSounds.MIXER_CRAFTING2.get(), SoundSource.BLOCKS, .25F, 1);
                nextChamberTwoSoundIn = 100;
            }
        }
    }

    protected void increaseCraftingChamberThreeProgress(BlockPos pos) {
        if(stageThreeProgress < stageThreeMaxProgress) {
            stageThreeProgress = Math.min(stageThreeMaxProgress, stageThreeProgress + getCurrentMachineTier().getMultiplier());

            if(nextChamberThreeSoundIn <= 0) { // Decreasing timer happens in tick().
                level.playSound(null, pos, ModSounds.MIXER_CRAFTING3.get(), SoundSource.BLOCKS, .25F, 1);
                nextChamberThreeSoundIn = 100;
            }
        }
    }

    @Override
    protected boolean craftItem() { // moveToChamberTwo
        if(!canCraft(true))
            return false;

        MixerRecipe currentRecipe = getCurrentRecipe();

        if(currentRecipe == null)
            return false; // Shouldn't happen.

        if(!ItemUtils.isSlotEmpty(itemHandler, 4) || !ItemUtils.isSlotEmpty(itemHandler, 5))
            return false; // Something else is blocking the move from chamber one to two.

        ItemStack newOutput = currentRecipe.output();
        ItemStack newByproduct = currentRecipe.byProduct().orElse(ItemStack.EMPTY);

        if(ItemUtils.mergeItemStacksIntoHandler(itemHandler, ItemUtils.getItemsAsMap(List.of(newOutput, newByproduct)), 4, 5)){
            ItemUtils.consumeItems(itemHandler, ItemUtils.getInputsAsMap(currentRecipe.inputs()), 0, 3);
            stageTwoMaxProgress = maxProgress;
            progress = 0;
        } else
            return false;

        return true;
    }

    private boolean moveToChamberThree(){
        if(!ItemUtils.isSlotEmpty(itemHandler, 6) || !ItemUtils.isSlotEmpty(itemHandler, 7))
            return false; // Something else is blocking the move from chamber two to three.

        ItemStack newOutput = ItemUtils.getStackInSlot(itemHandler, 4);
        ItemStack newByproduct = ItemUtils.getStackInSlot(itemHandler, 5);

        Map<ItemLike, Integer> countsMap = ItemUtils.getItemsAsMap(List.of(newOutput, newByproduct));

        if(ItemUtils.mergeItemStacksIntoHandler(itemHandler, countsMap, 6, 7)){
            ItemUtils.emptySlots(itemHandler, 4, 5);
            stageThreeMaxProgress = stageTwoProgress;
            stageTwoProgress = 0;
        } else
            return false;

        return true;
    }

    private boolean moveToOutput(){
        ItemStack newOutput = ItemUtils.getStackInSlot(itemHandler, 6);
        ItemStack newByproduct = ItemUtils.getStackInSlot(itemHandler, 7);
        Map<ItemLike, Integer> countsMap = ItemUtils.getItemsAsMap(List.of(newOutput, newByproduct));

        if(!ItemUtils.isBasicCompatible(itemHandler, countsMap, 8, 9))
            return false; // Something else is blocking the output slot.

        if(ItemUtils.mergeItemStacksIntoHandler(itemHandler, countsMap, 8, 9)){
            ItemUtils.emptySlots(itemHandler, 6, 7);
            stageThreeProgress = 0;
        } else
            return false;

        return true;
    }

    private void resetProgress(int chamber){
        switch(chamber){
            case 1 -> {
                noRecipeCooldown = 0;
                progress = 0;
                failureCooldownChamberOne = 0;
            }
            case 2 -> {
                stageTwoProgress = 0;
                failureCooldownChamberTwo = 0;
            }
            default -> {
                stageThreeProgress = 0;
                failureCooldownChamberThree = 0;
            }
        }
    }

    @Override
    protected boolean hasRecipe() {
        return canCraft(false);
    }

    private boolean hasChamberTwoInputs(){
        if(hasChamberTwoInputs == null)
            hasChamberTwoInputs = !ItemUtils.isSlotEmpty(itemHandler, 4) || !ItemUtils.isSlotEmpty(itemHandler, 5);

        return hasChamberTwoInputs;
    }

    private boolean hasChamberThreeInputs(){
        if(hasChamberThreeInputs == null)
            hasChamberThreeInputs = !ItemUtils.isSlotEmpty(itemHandler, 6) || !ItemUtils.isSlotEmpty(itemHandler, 7);

        return hasChamberThreeInputs;
    }

    private boolean canCraft(boolean forceEval) {
        if(level == null)
            canCraft = null;
        else if (canCraft == null || forceEval)
            canCraft = getCurrentRecipe() != null;

        return canCraft != null && canCraft;
    }

    private MixerRecipe getCurrentRecipe() {
        if(level == null)
            return null;

        List<ItemStack> materialInputs = new ArrayList<>();

        for(int i = 0; i < 4; i++) {
            ItemStack stack = ItemUtils.getStackInSlot(itemHandler, i);

            if(!stack.isEmpty())
                materialInputs.add(stack);
        }

        MixerRecipeInput recipeInput = new MixerRecipeInput(materialInputs);

        if(lastRecipe != null && lastRecipe.matches(recipeInput, level))
            return lastRecipe;

        ((ServerLevel) this.level).recipeAccess().getRecipeFor(ModRecipes.MIXER_TYPE.get(), recipeInput, level)
            .ifPresentOrElse(holder -> lastRecipe = holder.value(), () -> lastRecipe = null);

        // Make sure to update the maxProgress when a new recipe is selected to prevent the first craft's stage 1 from using the wrong maxProgress value.
        if(lastRecipe != null)
            maxProgress = lastRecipe.stepDuration();

        return lastRecipe;
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);

        stageTwoProgress = input.getIntOr("stageTwoProgress", 0);
        stageThreeProgress = input.getIntOr("stageThreeProgress", 0);
        stageTwoMaxProgress = input.getIntOr("stageTwoMaxProgress", 0);
        stageThreeMaxProgress = input.getIntOr("stageThreeMaxProgress", 0);
    }

    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);

        output.putInt("stageTwoProgress", stageTwoProgress);
        output.putInt("stageThreeProgress", stageThreeProgress);
        output.putInt("stageTwoMaxProgress", stageTwoMaxProgress);
        output.putInt("stageThreeMaxProgress", stageThreeMaxProgress);
    }

    @Override
    protected int getTotalSlots() {
        return 10;
    }

    @Override
    protected int getFirstOutputSlot() {
        return 8; // By setting this to 8, we already prevent extraction from chamber slots.
    }

    @Override
    protected String getRegistryName() {
        return "mixer";
    }

    @Override
    protected SoundEvent getCraftingFinishedSound() {
        return ModSounds.MIXER_FINISHED.get();
    }

    @Override
    public boolean mayInsert(ItemStack itemStack, int slot) {
        return slot < 4; // Slots 4 to 7 are treated as chamber slots that cannot be inserted into.
    }

    @Override public boolean mayExtract(int slot) {
        return slot < 4 || slot > 7;
    }

    @Override
    protected void onMachineContentsChanged() {
        canCraft = null;
        hasChamberTwoInputs = null;
        hasChamberThreeInputs = null;
        failureCooldownChamberOne = 0;
        failureCooldownChamberTwo = 0;
        failureCooldownChamberThree = 0;
    }

    @Override
    protected int getContainerDataCount() {
        return 8;
    }

    @Override protected int getUnknownFromContainerData(int i) {
        return switch(i){
            case 4 -> MixerBE.this.stageTwoProgress;
            case 5 -> MixerBE.this.stageTwoMaxProgress;
            case 6 -> MixerBE.this.stageThreeProgress;
            case 7 -> MixerBE.this.stageThreeMaxProgress;
            default -> 0;
        };
    }

    @Override protected void setUnknownForContainerData(int i, int value) {
        switch(i){
            case 4 -> MixerBE.this.stageTwoProgress = value;
            case 5 -> MixerBE.this.stageTwoMaxProgress = value;
            case 6 -> MixerBE.this.stageThreeProgress = value;
            case 7 -> MixerBE.this.stageThreeMaxProgress = value;
        }
    }

    @Override
    protected SoundEvent getCraftingInProgressSound() {
        return null; // We won't be using this method.
    }

    @Override
    protected int getMaxProgress() {
        return 20; // Unused.
    }
}
