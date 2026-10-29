package slimeattack07.decorator.item;

import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.block.AbstractMachineBlock;
import slimeattack07.decorator.block.ModBlocks;
import slimeattack07.decorator.component.ModDataComponents;

import java.util.function.Supplier;

public class ModCreativeTabs {

    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, Decorator.MOD_ID);

    public static final Supplier<CreativeModeTab> DECORATOR_ITEMS = TABS.register("decorator_items_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DRAFTING_PAPER.get()))
            .title(Component.translatable("creativetab.decorator.items"))
            .displayItems((parameters, output) -> {
                ModItems.ITEMS.getEntries().forEach(entry -> {
                    if(!(entry.get() instanceof BlockItem) && !hasOwnTab(entry.get()))
                        output.accept(entry.get());
                });
            }).build());

    public static final Supplier<CreativeModeTab> DECORATOR_INSTRUCTIONS = TABS.register("decorator_instructions_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DECORATION_INSTRUCTIONS.get()))
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID, "decorator_items_tab"))
            .title(Component.translatable("creativetab.decorator.instructions"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.DECORATION_INSTRUCTIONS.get());
            }).build());

    public static final Supplier<CreativeModeTab> DECORATOR_SCANS = TABS.register("decorator_scans_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.DECORATION_SCAN.get()))
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID, "decorator_instructions_tab"))
            .title(Component.translatable("creativetab.decorator.scans"))
            .displayItems((parameters, output) -> registerScans(output)).build());

    public static final Supplier<CreativeModeTab> DECORATOR_MACHINES = TABS.register("decorator_machines_tab",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModBlocks.DRAFTING_TABLE))
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID, "decorator_scans_tab"))
            .title(Component.translatable("creativetab.decorator.machines"))
            .displayItems((parameters, output) -> {
                registerMachine(output, ModBlocks.DRAFTING_TABLE.get());
                registerMachine(output, ModBlocks.DECORATION_CRAFTER.get());
                registerMachine(output, ModBlocks.DECORATION_SCANNER.get());
            }).build());

    public static final Supplier<CreativeModeTab> DECORATOR_PRINTED_DECORATIONS = TABS.register("decorator_printed_decorations",
        () -> CreativeModeTab.builder().icon(() -> new ItemStack(ModItems.TESTDEC_ONE.get()))
            .withTabsBefore(ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID, "decorator_machines_tab"))
            .title(Component.translatable("creativetab.decorator.printed_decorations"))
            .displayItems((parameters, output) -> {
                output.accept(ModItems.TESTDEC_ONE);
                output.accept(ModItems.TESTDEC_TWO);
            }).build());

    public static void register(IEventBus eventBus){
        TABS.register(eventBus);
    }

    private static void registerScans(CreativeModeTab.Output output){
        registerScan(output, ModItems.TESTDEC_ONE.get());
        registerScan(output, ModItems.TESTDEC_TWO.get());
    }

    private static void registerScan(CreativeModeTab.Output output, PrintedDecorationItem item){
        ItemStack stack = new ItemStack(ModItems.DECORATION_SCAN.get());
        stack.set(ModDataComponents.DECORATION, item.getScanId());
        output.accept(stack);
    }

    private static void registerMachine(CreativeModeTab.Output output, AbstractMachineBlock block){
        for(int i = 0; i <= 60; i = i + 15){
            ItemStack stack = new ItemStack(block);
            stack.set(ModDataComponents.EXPERIENCE, i);
            output.accept(stack);
        }
    }

    private static boolean hasOwnTab(Item item){
        return item.equals(ModItems.DECORATION_INSTRUCTIONS.get()) || item.equals(ModItems.DECORATION_SCAN.get()) || item instanceof PrintedDecorationItem;
    }
}
