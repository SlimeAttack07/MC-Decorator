package slimeattack07.decorator;

import com.mojang.logging.LogUtils;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import slimeattack07.decorator.block.ModBlockEntities;
import slimeattack07.decorator.block.ModBlocks;
import slimeattack07.decorator.block.blockentities.DecorationCrafterBE;
import slimeattack07.decorator.block.blockentities.DecorationScannerBE;
import slimeattack07.decorator.block.blockentities.DraftingTableBE;
import slimeattack07.decorator.component.ModDataComponents;
import slimeattack07.decorator.item.ModCreativeTabs;
import slimeattack07.decorator.item.ModItems;
import slimeattack07.decorator.recipe.ModRecipes;
import slimeattack07.decorator.screen.*;
import slimeattack07.decorator.sound.ModSounds;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Decorator.MOD_ID)
public class Decorator {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "decorator";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Decorator(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading.
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerCapabilities);

        // Register ourselves for server and other game events we are interested in.
        // Note that this is necessary if and only if we want *this* class (ExampleMod) to respond directly to events.
        // Do not add this line if there are no @SubscribeEvent-annotated functions in this class, like onServerStarting() below.
        NeoForge.EVENT_BUS.register(this);
        ModCreativeTabs.register(modEventBus);
        ModBlocks.register(modEventBus);
        ModItems.register(modEventBus);
        ModBlockEntities.register(modEventBus);
        ModMenuTypes.register(modEventBus);
        ModRecipes.register(modEventBus);
        ModDataComponents.register(modEventBus);
        ModSounds.register(modEventBus);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    private void registerCapabilities(RegisterCapabilitiesEvent event) {
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DRAFTING_TABLE_BE.get(), DraftingTableBE::getAutomationHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DECORATION_CRAFTER_BE.get(), DecorationCrafterBE::getAutomationHandler);
        event.registerBlockEntity(Capabilities.ItemHandler.BLOCK, ModBlockEntities.DECORATION_SCANNER_BE.get(), DecorationScannerBE::getAutomationHandler);
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {

    }

    @EventBusSubscriber(modid = MOD_ID, value = Dist.CLIENT)
    public static class ClientModEvents {
        @SubscribeEvent
        public static void registerScreens(RegisterMenuScreensEvent event) {
            event.register(ModMenuTypes.DISASSEMBLY_TABLE_MENU.get(), DisassemblyTableScreen::new);
            event.register(ModMenuTypes.DRAFTING_TABLE_MENU.get(), DraftingTableScreen::new);
            event.register(ModMenuTypes.DECORATION_CRAFTER_MENU.get(), DecorationCrafterScreen::new);
            event.register(ModMenuTypes.DECORATION_SCANNER_MENU.get(), DecorationScannerScreen::new);
        }
    }
}
