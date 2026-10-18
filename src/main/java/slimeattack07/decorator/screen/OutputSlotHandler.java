package slimeattack07.decorator.screen;

import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.item.ResourceHandlerSlot;

/** Simple extension of SlotItemHandler that prevents users from placing items in it.
 * Code/machines can still place items in it, just the client GUI cannot.
 *
 */
public class OutputSlotHandler extends ResourceHandlerSlot {
    public OutputSlotHandler(ItemStacksResourceHandler itemHandler, int index, int xPosition, int yPosition) {
        super(itemHandler, itemHandler::set, index, xPosition, yPosition);
    }

    @Override
    public boolean mayPlace(ItemStack stack) {
        return false;
    }
}
