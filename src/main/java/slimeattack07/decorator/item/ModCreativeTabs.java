package slimeattack07.decorator.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.ModBlocks;

import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Decorator.MOD_ID);

    public static final Supplier<CreativeModeTab> DECORATOR_ITEMS = TABS.register("decorator_items_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DRAFTING_PAPER.get()))
        .title(Component.translatable("creativetab.decorator.decorator_items"))
        .displayItems((parameters, output) -> {
            ModItems.ITEMS.getEntries().forEach(entry -> {
                if(!(entry.get() instanceof BlockItem))
                    output.accept(entry.get());
            });
        }).build());

    public static final Supplier<CreativeModeTab> DECORATOR_MACHINES = TABS.register("decorator_machines_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DISASSEMBLY_TABLE))
            .withTabsBefore(Identifier.fromNamespaceAndPath(Decorator.MOD_ID, "decorator_items_tab"))
            .title(Component.translatable("creativetab.decorator.decorator_machines"))
            .displayItems((parameters, output) -> {
                output.accept(ModBlocks.DISASSEMBLY_TABLE);
//                output.accept(ModBlocks.STAGED3);
//                output.accept(ModBlocks.STAGED5);
//                output.accept(ModBlocks.PLACE_STRUCTURE);
            }).build());

    public static void register(IEventBus eventBus){
        TABS.register(eventBus);
    }
}
