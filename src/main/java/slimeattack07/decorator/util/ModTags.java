package slimeattack07.decorator.util;

import net.minecraft.resources.Identifier;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import slimeattack07.decorator.Decorator;

public class ModTags {
    public static class Items{
        public static final TagKey<Item> GROUP_FOCUSES = createTag("group_focus/focuses");
//        public static final TagKey<Item> SHIP_FOCUS = createTag("group_focus/ship_focus");
//        public static final TagKey<Item> STRUCTURE_FOCUS = createTag("group_focus/structure_focus");

        public static final TagKey<Item> TIER_FOCUSES = createTag("tier_focus/focuses");
//        public static final TagKey<Item> BASIC_FOCUS = createTag("tier_focus/basic_focus");
//        public static final TagKey<Item> EXPERIENCED_FOCUS = createTag("tier_focus/experienced_focus");
        public static final TagKey<Item> DECORATION_INSTRUCTIONS = createTag("decoration_instructions");

        private static TagKey<Item> createTag(String name){
            return ItemTags.create(Identifier.fromNamespaceAndPath(Decorator.MOD_ID, name));
        }
    }
}
