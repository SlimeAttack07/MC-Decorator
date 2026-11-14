package slimeattack07.decorator.screen;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import slimeattack07.decorator.block.ModBlocks;

/* Container data:
 * 0 & 1 = stage 1 progress/max progress.
 * 2 & 3 = machine xp / failure cooldown (enforced by super).
 * 4 & 5 = stage 2 progress/max progress.
 * 6 & 7 = stage 3 progress/max progress.

 */
public class MixerMenu extends AbstractMenu{

    public MixerMenu(int pContainerId, Inventory inv, FriendlyByteBuf extraData) {
        this(pContainerId, inv, inv.player.level().getBlockEntity(extraData.readBlockPos()), new SimpleContainerData(8));
    }

    public MixerMenu(int pContainerId, Inventory inv, BlockEntity entity, ContainerData data) {
        super(ModMenuTypes.MIXER_MENU.get(), pContainerId, inv, entity, data);
    }

    @Override
    public boolean isCrafting() {
        return isFirstStageCrafting() || isSecondStageCrafting() || isThirdStageCrafting();
    }

    public boolean isFirstStageCrafting(){
        return data.get(0) > 0;
    }

    public boolean isSecondStageCrafting(){
        return data.get(4) > 0;
    }

    public boolean isThirdStageCrafting(){
        return data.get(6) > 0;
    }

    @Override
    public int getInputSlotCount() {
        return 8;
    }

    @Override
    protected int getOutputSlotCount() {
        return 2;
    }

    @Override
    protected SlotLocation getLocationOfSlot(int slot) {
        return switch(slot){
            case 0 -> new SlotLocation(8, 18); // 4 input slots for crafting materials.
            case 1 -> new SlotLocation(26, 18);
            case 2 -> new SlotLocation(8, 36);
            case 3 -> new SlotLocation(26, 36);
            case 8 -> new SlotLocation(116, 58); // 2 output slots.
            case 9 -> new SlotLocation(134, 58);
            default -> new SlotLocation(-10000, -10000); // Other slots (chambers two and three) cannot be interacted with.
        };
    }

    @Override
    protected Block getBlock() {
        return ModBlocks.MIXER.get();
    }

    public int getFirstStageScaledProgress(){
        return getScaledProgress(0);
    }

    public int getSecondStageScaledProgress(){
        return getScaledProgress(4);
    }

    public int getThirdStageScaledProgress(){
        return getScaledProgress(6);
    }

    private int getScaledProgress(int progressIndex) {
        int progress = data.get(progressIndex);
        int maxProgress = data.get(progressIndex + 1);
        int progressIconPixelSize = 32;

        return maxProgress != 0 && progress != 0 ? progress * progressIconPixelSize / maxProgress : 0;
    }
}
