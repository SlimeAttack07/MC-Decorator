package slimeattack07.decorator.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Decorator.MOD_ID);

    public static final DeferredItem<Item> BROKEN_STICK = ITEMS.register("broken_stick", () -> new Item(new Item.Properties()));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
