package slimeattack07.decorator.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Decorator.MOD_ID);

    public static final Supplier<SoundEvent> DRAFTING_TABLE_CRAFTING = registerSoundEvent("drafting_table_crafting");
    public static final Supplier<SoundEvent> DRAFTING_TABLE_FINISHED = registerSoundEvent("drafting_table_finished");
    public static final Supplier<SoundEvent> DECORATION_CRAFTER_CRAFTING1 = registerSoundEvent("decoration_crafter_crafting1");
    public static final Supplier<SoundEvent> DECORATION_CRAFTER_CRAFTING2 = registerSoundEvent("decoration_crafter_crafting2");
    public static final Supplier<SoundEvent> DECORATION_CRAFTER_CRAFTING3 = registerSoundEvent("decoration_crafter_crafting3");
    public static final Supplier<SoundEvent> DECORATION_CRAFTER_CRAFTING4 = registerSoundEvent("decoration_crafter_crafting4");
    public static final Supplier<SoundEvent> DECORATION_CRAFTER_FINISHED = registerSoundEvent("decoration_crafter_finished");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        Identifier id = Identifier.fromNamespaceAndPath(Decorator.MOD_ID, name);

        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
