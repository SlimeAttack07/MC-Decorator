package slimeattack07.decorator.screen;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import slimeattack07.decorator.Decorator;

public class DraftingTableScreen extends AbstractScreen<DraftingTableMenu> {
    public DraftingTableScreen(DraftingTableMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/drafting_table.png");
    }

    @Override
    protected ResourceLocation getProgressIconTexture() {
        return ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/pencil.png");
    }

    @Override
    protected SlotLocation getProgressIconTopLeft() {
        return new SlotLocation(72, 34);
    }

    @Override
    protected SlotLocation getWarningIconTopLeft() {
        return new SlotLocation(116, 26);
    }
}
