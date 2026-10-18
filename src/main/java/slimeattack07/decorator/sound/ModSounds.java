package slimeattack07.decorator.sound;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredRegister;
import slimeattack07.decorator.Decorator;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(BuiltInRegistries.SOUND_EVENT, Decorator.MOD_ID);

    public static final Supplier<SoundEvent> DRAFTING_TABLE_CRAFTING = registerSoundEvent("drafting_table_crafting");
    public static final Supplier<SoundEvent> DRAFTING_TABLE_FINISHED = registerSoundEvent("drafting_table_finished");

    private static Supplier<SoundEvent> registerSoundEvent(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID, name);

        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    public static void register(IEventBus eventBus){
        SOUND_EVENTS.register(eventBus);
    }
}
