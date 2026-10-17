package slimeattack07.decorator.screen;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.SlotItemHandler;

/** Simple extension of SlotItemHandler that prevents users from placing items in it.
 * Code/machines can still place items in it, just the client GUI cannot.
 *
 */
public class OutputSlotHandler extends SlotItemHandler {
    public OutputSlotHandler(IItemHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
