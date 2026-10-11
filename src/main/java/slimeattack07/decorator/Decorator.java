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
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import org.slf4j.Logger;
import slimeattack07.salvager.block.ModBlockEntities;
import slimeattack07.salvager.block.ModBlocks;
import slimeattack07.salvager.item.ModCreativeTabs;
import slimeattack07.salvager.item.ModItems;
import slimeattack07.salvager.recipe.ModRecipes;
import slimeattack07.salvager.screen.DisassemblyTableScreen;
import slimeattack07.salvager.screen.ModMenuTypes;
import slimeattack07.salvager.structures.ModProcessors;
import slimeattack07.salvager.structures.ThemeLoader;
import slimeattack07.salvager.util.ResourceUtils;

// The value here should match an entry in the META-INF/neoforge.mods.toml file
@Mod(Decorator.MOD_ID)
public class Decorator {
    // Define mod id in a common place for everything to reference
    public static final String MOD_ID = "salvager";
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();

    // The constructor for the mod class is the first code that is run when your mod is loaded.
    // FML will recognize some parameter types like IEventBus or ModContainer and pass them in automatically.
    public Decorator(IEventBus modEventBus, ModContainer modContainer) {
        // Register the commonSetup method for modloading.
        modEventBus.addListener(this::commonSetup);

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
        ModProcessors.register(modEventBus);

        // Register the item to a creative tab
        modEventBus.addListener(this::addCreative);

        // Register our mod's ModConfigSpec so that FML can create and load the config file for us
        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
    }

    private void commonSetup(FMLCommonSetupEvent event) {

    }

    // Add the example block item to the building blocks tab
    private void addCreative(BuildCreativeModeTabContentsEvent event) {
//        if(event.getTabKey().equals(CreativeModeTabs.INGREDIENTS)) {
//            event.accept(ModItems.BROKEN_STICK);
//            event.accept(ModBlocks.DISASSEMBLY_TABLE);
//        }
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
        }
    }
}
