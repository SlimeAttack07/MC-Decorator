package slimeattack07.decorator.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.item.salvageable.BrokenStick;
import slimeattack07.salvager.util.ResourceUtils;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Decorator.MOD_ID);

    public static final DeferredItem<Item> BROKEN_STICK = ITEMS.register("broken_stick", () -> new Item(new Item.Properties().setId(ResourceUtils.genId("broken_stick"))));

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }
}
