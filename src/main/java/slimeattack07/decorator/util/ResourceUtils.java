package slimeattack07.decorator.util;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootTable;
import net.neoforged.neoforge.registries.DeferredBlock;
import slimeattack07.decorator.Decorator;

public class ResourceUtils {

    /** Gens a resource key that can be used for registering an item.
     *
     * @param id
     * @return
     */
    public static ResourceKey<Item> genId(String id){
        return ResourceKey.create(Registries.ITEM, identifierOf(id));
    }

    /** Gens a resource key that can be used for registering an item.
     *
     * @param idSource
     * @return
     */
    public static <T extends Block> ResourceKey<Item> genId(DeferredBlock<T> idSource){
        return ResourceKey.create(Registries.ITEM, idSource.getId());
    }

    /** Gens a resource key that can be used for registering an item.
     *
     * @param id
     * @return
     */
    public static ResourceKey<Block> genBlockId(String id){
        return ResourceKey.create(Registries.BLOCK, identifierOf(id));
    }

    /** Gens a resource key that can be used for registering or fetching a loot table.
     *
     * @param block
     * @param suffix
     * @return
     */
    public static ResourceKey<LootTable> genLootTableId(Block block, String suffix){
        return ResourceKey.create(Registries.LOOT_TABLE, appendTo(BuiltInRegistries.BLOCK.getKey(block), suffix));
    }

    public static Identifier identifierOf(String id){
        return Identifier.fromNamespaceAndPath(Decorator.MOD_ID, id);
    }

    public static Identifier appendTo(Identifier identifier, String suffix){
        return Identifier.fromNamespaceAndPath(identifier.getNamespace(), identifier.getPath() + suffix);
    }
}
