package slimeattack07.decorator.util;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.items.ItemStackHandler;
import slimeattack07.decorator.Decorator;

/* Note: A lot of these things are probably easier to just do directly. However, since I'm maintaining both 1.21.1 and 1.21.11 versions of this mod, it's better to use this
 * util class instead since item manipulation requires more lines of code in 1.21.11 than it does here in 1.21.1.
 * By using this util class, I don't need to keep replacing the 1.21.1 specific calls and only need to provide a correct version of these util methods instead.
 */
public class ItemUtils {
    public static ItemStack getStackInSlot(ItemStackHandler handler, int slot){
        try{
            return handler.getStackInSlot(slot);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to get contents of slot " + slot + " for given handler.");

            return ItemStack.EMPTY;
        }
    }

    public static int getAmountInSlot(ItemStackHandler handler, int slot){
        try{
            return handler.getStackInSlot(slot).getCount();
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to get amount in slot " + slot + " for given handler.");

            return 0;
        }
    }

    public static boolean isSlotEmpty(ItemStackHandler handler, int slot){
        try{
            return handler.getStackInSlot(slot).isEmpty();
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to determine if slot " + slot + " is empty for given handler.");

            return true;
        }
    }

    public static boolean isSameItem(ItemStackHandler handler, int slot, Item item){
        try{
            return handler.getStackInSlot(slot).is(item);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to determine if slot " + slot + " has the same item for given handler.");

            return true;
        }
    }

    public static int getMaxStackSize(ItemStackHandler handler, int slot, int defaultSize){
        try{
            ItemStack stack = handler.getStackInSlot(slot);

            return stack.isEmpty() ? defaultSize : stack.getMaxStackSize();
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to get max stack size for slot " + slot + " for given handler.");

            return defaultSize;
        }
    }

    public static ItemStack extractFromSlot(ItemStackHandler handler, int slot, int amount){
        try{
            return handler.extractItem(slot, amount, false);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to extract from slot " + slot + " for given handler.");

            return ItemStack.EMPTY;
        }
    }

    public static void setStackInSlot(ItemStackHandler handler, int slot, ItemStack stack){
        try{
            handler.setStackInSlot(slot, stack);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to set item stack for slot " + slot + " for given handler.");
        }
    }

    public static void setStackInSlot(ItemStackHandler handler, int slot, ItemLike item, int count){
        try{
            handler.setStackInSlot(slot, new ItemStack(item, count));
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to set item stack for slot " + slot + " for given handler.");
        }
    }
}
