package slimeattack07.decorator.util;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import slimeattack07.decorator.Decorator;

public class ItemUtils {
    public static ItemStack getStackInSlot(ItemStacksResourceHandler handler, int slot){
        try{
            return handler.getResource(slot).toStack(handler.getAmountAsInt(slot));
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to get contents of slot " + slot + " for given handler.");

            return ItemStack.EMPTY;
        }
    }

    public static int getAmountInSlot(ItemStacksResourceHandler handler, int slot){
        try{
            return handler.getAmountAsInt(slot);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to get amount in slot " + slot + " for given handler.");

            return 0;
        }
    }

    public static boolean isSlotEmpty(ItemStacksResourceHandler handler, int slot){
        try{
            return handler.getResource(slot).isEmpty();
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to determine if slot " + slot + " is empty for given handler.");

            return true;
        }
    }

    public static boolean isSameItem(ItemStacksResourceHandler handler, int slot, ItemLike item){
        try{
            return handler.getResource(slot).is(item);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to determine if slot " + slot + " has the same item for given handler.");

            return true;
        }
    }

    public static int getMaxStackSize(ItemStacksResourceHandler handler, int slot, int defaultSize){
        try{
            ItemResource resource = handler.getResource(slot);

            return resource.isEmpty() ? defaultSize : resource.getMaxStackSize();
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to get max stack size for slot " + slot + " for given handler.");

            return defaultSize;
        }
    }

    public static ItemStack extractFromSlot(ItemStacksResourceHandler handler, int slot, int amount){
        try{
            ItemResource resource = handler.getResource(slot);
            ItemStack extracted = ItemStack.EMPTY;

            if(!resource.isEmpty()){
                try(Transaction tx = Transaction.open(null)){
                    int extractedAmount = handler.extract(slot, resource, amount, tx);
                    extracted = resource.toStack(extractedAmount);
                    tx.commit();
                }
            }

            return extracted;
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to extract from slot " + slot + " for given handler.");

            return ItemStack.EMPTY;
        }
    }

    public static void setStackInSlot(ItemStacksResourceHandler handler, int slot, ItemStack stack){
        try{
            handler.set(slot, ItemResource.of(stack), stack.getCount());
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to set item stack for slot " + slot + " for given handler.");
        }
    }

    public static void setStackInSlot(ItemStacksResourceHandler handler, int slot, ItemLike item, int count){
        try{
            handler.set(slot, ItemResource.of(item), count);
        } catch(Exception e){
            Decorator.LOGGER.warn("Failed to set item stack for slot " + slot + " for given handler.");
        }
    }
}
