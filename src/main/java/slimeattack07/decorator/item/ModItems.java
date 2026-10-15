package slimeattack07.decorator.item;

import net.minecraft.world.item.Item;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.ResourceUtils;

import javax.tools.Tool;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(Decorator.MOD_ID);

    public static final DeferredItem<Item> DRAFTING_PAPER = registerItem("drafting_paper");
    public static final DeferredItem<Item> RAW_DRAFT = registerTooltippedItem("raw_draft");
    public static final DeferredItem<Item> REFINED_DRAFT = ITEMS.register("refined_draft", () -> new RefinedDraftItem(new Item.Properties().setId(ResourceUtils.genId("refined_draft")), "refined_draft"));
    public static final DeferredItem<Item> SCAN_TEMPLATE = registerTooltippedItem("scan_template");
    public static final DeferredItem<Item> DECORATION_INSTRUCTIONS = registerTooltippedItem("decoration_instructions");
    public static final DeferredItem<Item> DECORATION_SCAN = registerTooltippedItem("decoration_scan");
    public static final DeferredItem<Item> GENERIC_PRINTING_PASTE = registerTooltippedItem("generic_printing_paste");
    public static final DeferredItem<Item> WHITE_PRINTING_PASTE = registerTooltippedItem("white_printing_paste");
    public static final DeferredItem<Item> LIGHT_GRAY_PRINTING_PASTE = registerTooltippedItem("light_gray_printing_paste");
    public static final DeferredItem<Item> GRAY_PRINTING_PASTE = registerTooltippedItem("gray_printing_paste");
    public static final DeferredItem<Item> BLACK_PRINTING_PASTE = registerTooltippedItem("black_printing_paste");
    public static final DeferredItem<Item> BROWN_PRINTING_PASTE = registerTooltippedItem("brown_printing_paste");
    public static final DeferredItem<Item> RED_PRINTING_PASTE = registerTooltippedItem("red_printing_paste");
    public static final DeferredItem<Item> ORANGE_PRINTING_PASTE = registerTooltippedItem("orange_printing_paste");
    public static final DeferredItem<Item> YELLOW_PRINTING_PASTE = registerTooltippedItem("yellow_printing_paste");
    public static final DeferredItem<Item> LIME_PRINTING_PASTE = registerTooltippedItem("lime_printing_paste");
    public static final DeferredItem<Item> GREEN_PRINTING_PASTE = registerTooltippedItem("green_printing_paste");
    public static final DeferredItem<Item> CYAN_PRINTING_PASTE = registerTooltippedItem("cyan_printing_paste");
    public static final DeferredItem<Item> LIGHT_BLUE_PRINTING_PASTE = registerTooltippedItem("light_blue_printing_paste");
    public static final DeferredItem<Item> BLUE_PRINTING_PASTE = registerTooltippedItem("blue_printing_paste");
    public static final DeferredItem<Item> PURPLE_PRINTING_PASTE = registerTooltippedItem("purple_printing_paste");
    public static final DeferredItem<Item> MAGENTA_PRINTING_PASTE = registerTooltippedItem("magenta_printing_paste");
    public static final DeferredItem<Item> PINK_PRINTING_PASTE = registerTooltippedItem("pink_printing_paste");

    public static void register(IEventBus eventBus){
        ITEMS.register(eventBus);
    }

    private static DeferredItem<Item> registerItem(String registryId){
        return ITEMS.register(registryId, () -> new Item(new Item.Properties().setId(ResourceUtils.genId(registryId))));
    }

    private static DeferredItem<Item> registerTooltippedItem(String registryId){
        return ITEMS.register(registryId, () -> new TooltippedItem(new Item.Properties().setId(ResourceUtils.genId(registryId)), registryId));
    }
}
