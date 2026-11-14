package slimeattack07.decorator.util;

import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.ItemLike;
import net.neoforged.neoforge.common.crafting.SizedIngredient;
import net.neoforged.neoforge.items.ItemStackHandler;
import slimeattack07.decorator.Decorator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    public static Map<ItemLike, Integer> getInputsAsMap(List<SizedIngredient> inputs){
        Map<ItemLike, Integer> countsMap = new HashMap<>();
        // We make an assumption that each 'ingredient' has exactly one stack, and we don't support tags in this function.
        inputs.forEach(input -> countsMap.merge(input.getItems()[0].getItem(), input.getItems()[0].getCount(), Integer::sum));

        return countsMap;
    }

    /** Gets a list of copied itemstacks between the given bounds (both inclusive)
     *
     * @param itemHandler
     * @param lowerBound
     * @param upperBound
     * @return
     */
    public static List<ItemStack> getStacksInSlotsCopied(ItemStackHandler itemHandler, int lowerBound, int upperBound){
        List<ItemStack> stacks = new ArrayList<>();

        for(int i = lowerBound; i <= upperBound; i++) {
            if(!isSlotEmpty(itemHandler, i))
                stacks.add(getStackInSlot(itemHandler, i).copy()); // Copy so we don't accidentally modify the handler.
        }

        return stacks;
    }

    public static Map<ItemLike, Integer> getItemsAsMap(ItemStackHandler itemHandler, int lowerBound, int upperBound){
        List<ItemStack> stacks = getStacksInSlotsCopied(itemHandler, lowerBound, upperBound);

        return getItemsAsMap(stacks);
    }

    public static Map<ItemLike, Integer> getItemsAsMap(List<ItemStack> stacks){
        Map<ItemLike, Integer> countsMap = new HashMap<>();
        // We make an assumption that each 'ingredient' has exactly one stack, and we don't support tags in this function.
        stacks.forEach(input -> {
            if(!input.isEmpty())
                countsMap.merge(input.getItem(), input.getCount(), Integer::sum);
        });

        return countsMap;
    }

    /* Simple logic:
     * 1. Build a map of how many of each item is required. The merge function will merge duplicate item stacks, so e.g. 2x '4 iron ingot' becomes 1x '8 iron ingot'.
     * 2. For each provided input, remove the amount of that item from the map (computeIfPresent only runs for items already present in the map, so inputs that don't
     * match the recipe (excess) are ignored).
     * 3. If the removed input leads to a 0 or negative count of the item, then we know enough of that material is present. By returning null, the key gets removed from the map.
     * 4. Check if the map is empty at the end. Because null values remove the key, we know all materials are present if all keys have been removed (empty map remains).
     */
    /** Checks if the given countsMap contains all inputs.
     *
     * @param inputs
     * @param countsMap
     * @return True if the inputs contains everything indicated by countsMap, false otherwise.
     */
    public static boolean matchesInputs(List<ItemStack> inputs, Map<ItemLike, Integer> countsMap){
        inputs.forEach(input -> countsMap.computeIfPresent(input.getItem(), (key, required) -> {
            int remaining = required - input.getCount();

            return remaining <= 0 ? null : remaining;
        }));

        return countsMap.isEmpty();
    }

    /** Consumes items in countsMap from the given itemHandler in reverse order to produce a neater end inventory.
     * Both bound are inclusive.
     *
     * @param itemHandler
     * @param countsMap
     * @param lowerBound The lowest index of the itemHandler to consume from.
     * @param upperBound The highest index of the itemHandler to consume from.
     * @return True if all items were consumed successfully, false otherwise.
     */
    public static boolean consumeItems(ItemStackHandler itemHandler, Map<ItemLike, Integer> countsMap, int lowerBound, int upperBound){
        // Consume the inputs by going from last to first material input slot. This leaves behind a neater inventory than doing it in normal order.
        for(int i = upperBound; i >= lowerBound; i--){
            ItemStack materialStack = itemHandler.getStackInSlot(i);
            Integer requiredAmount = countsMap.get(materialStack.getItem());

            if(requiredAmount != null){ // If null, then this item is not required for the recipe we've just crafted.
                int usedAmount = Math.min(materialStack.getCount(), requiredAmount);
                itemHandler.extractItem(i, usedAmount, false);
                countsMap.computeIfPresent(materialStack.getItem(), (key, oldRequired) -> {
                    int stillRequired = oldRequired - usedAmount;

                    return stillRequired > 0 ? stillRequired : null; // Returning null removes the mapping, indicating we've consumed everything we need to for this item.
                });
            }
        }

        return countsMap.isEmpty();
    }

    /** Checks if two item stacks are compatible excluding NBT.
     * It's compatible if they both have the same item and merging mergingIntoSource into source them would not exceed the max stack size.
     *
     * @param source
     * @param mergingIntoSource
     * @return
     */
    public static boolean isBasicCompatible(ItemStack source, ItemStack mergingIntoSource){
        if(source.isEmpty())
            return true;

        return source.getItem().equals(mergingIntoSource.getItem()) && source.getCount() + mergingIntoSource.getCount() <= source.getMaxStackSize();
    }

    /** Checks if two item stacks are compatible and share the same given tag.
     *
     * @param source
     * @param mergingInto
     * @param expectedTag
     * @return
     */
    public static boolean isAdvancedCompatible(ItemStack source, ItemStack mergingInto, CompoundTag expectedTag){
        if(!isBasicCompatible(source, mergingInto))
            return false;

        Object stackCustomDataObject = source.getComponents().getOrDefault(DataComponents.CUSTOM_DATA, new CompoundTag());

        if(stackCustomDataObject instanceof CustomData customData)
            stackCustomDataObject = customData.copyTag();

        if(stackCustomDataObject instanceof CompoundTag stackTag)
            return (stackTag.isEmpty() && expectedTag.isEmpty()) || stackTag.equals(expectedTag);
        else
            return false; // It's not a CompoundTag, meaning it's present as something else since we didn't get an empty tag and therefore we probably can't merge the item.
    }

    /** Checks if the given countsMap is compatible with the items between the bounds (both inclusive) in the item handler.
     *
     * @param itemHandler
     * @param countsMap
     * @param lowerBound The lowest index to check.
     * @param upperBound The highest index to check.
     * @return
     */
    public static boolean isBasicCompatible(ItemStackHandler itemHandler, Map<ItemLike, Integer> countsMap, int lowerBound, int upperBound){
        countsMap = new HashMap<>(countsMap); // Copy map so we don't modify the caller's map.
        List<ItemStack> contentStacks = getStacksInSlotsCopied(itemHandler, lowerBound, upperBound); // This returns a copy, so it's safe to modify.
        int emptySlots = 0;

        for(int i = lowerBound; i <= upperBound; i++){
            if(isSlotEmpty(itemHandler, i))
                emptySlots++;
        }

        // First check what already fits into occupied slots.
        for(ItemStack contentStack : contentStacks){
            ItemLike candidateItem = contentStack.getItem();
            Integer candidateCount = countsMap.get(candidateItem);

            if(candidateCount != null){
                int capacity = Math.min(contentStack.getMaxStackSize() - contentStack.getCount(), candidateCount);
                contentStack.grow(capacity);
                countsMap.computeIfPresent(candidateItem, (key, oldRequired) -> {
                    int stillRequired = oldRequired - capacity;

                    return stillRequired > 0 ? stillRequired : null; // Returning null removes the mapping, indicating we've consumed everything we need to for this item.
                });
            }
        }

        // Then fill any empty slots if required.
        while(emptySlots > 0 && !countsMap.isEmpty()){
            ItemLike firstItem = countsMap.keySet().iterator().next();
            countsMap.computeIfPresent(firstItem, (key, oldRequired) -> {
                int stillRequired = oldRequired - firstItem.asItem().getDefaultMaxStackSize();

                return stillRequired > 0 ? stillRequired : null; // Returning null removes the mapping, indicating we've consumed everything we need to for this item.
            });

            emptySlots--; // Since we're only checking if everything fits, we don't care where the empty slots are, just how many there are.
        }

        return countsMap.isEmpty();
    }

    /** Merges the given countsMap into the itemHandler between the bounds (both inclusive).
     *
     * @param itemHandler
     * @param countsMap
     * @param lowerBound The lowest index to check.
     * @param upperBound The highest index to check.
     * @return True if all items were merged, false otherwise.
     */
    public static boolean mergeItemStacksIntoHandler(ItemStackHandler itemHandler, Map<ItemLike, Integer> countsMap, int lowerBound, int upperBound){
        // First merge what already fits into occupied slots.
        for(int i = lowerBound; i <= upperBound; i++){
            ItemStack contentStack = getStackInSlot(itemHandler, i);
            ItemLike candidateItem = contentStack.getItem();
            Integer candidateCount = countsMap.get(candidateItem);

            if(candidateCount != null){
                int capacity = Math.min(contentStack.getMaxStackSize() - contentStack.getCount(), candidateCount);
                contentStack.grow(capacity);
                countsMap.computeIfPresent(candidateItem, (key, oldRequired) -> {
                    int stillRequired = oldRequired - capacity;

                    return stillRequired > 0 ? stillRequired : null; // Returning null removes the mapping, indicating we've consumed everything we need to for this item.
                });

                setStackInSlot(itemHandler, i, contentStack);
            }
        }

        // Then fill any empty slots if required.
        if(!countsMap.isEmpty()){
            for(int i = lowerBound; i <= upperBound; i++){
                if(countsMap.isEmpty())
                    break; // Early return.

                if(isSlotEmpty(itemHandler, i)){
                    ItemLike firstItem = countsMap.keySet().iterator().next();
                    int amount = Math.min(firstItem.asItem().getDefaultMaxStackSize(), countsMap.get(firstItem));
                    countsMap.computeIfPresent(firstItem, (key, oldRequired) -> {
                        int stillRequired = oldRequired - amount;

                        return stillRequired > 0 ? stillRequired : null; // Returning null removes the mapping, indicating we've consumed everything we need to for this item.
                    });

                    setStackInSlot(itemHandler, i, new ItemStack(firstItem, amount));
                }
            }
        }

        if(!countsMap.isEmpty()){
            Decorator.LOGGER.warn("Failed to merge all items into item stack handler. Remaining items: " + countsMap);

            return false;
        }

        return true;
    }

    public static void emptySlots(ItemStackHandler itemHandler, int lowerBound, int upperBound){
        for(int i = lowerBound; i <= upperBound; i++)
            setStackInSlot(itemHandler, i, ItemStack.EMPTY);
    }
}
