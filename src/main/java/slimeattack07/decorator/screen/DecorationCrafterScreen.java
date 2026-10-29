package slimeattack07.decorator.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import slimeattack07.decorator.Decorator;

public class DecorationCrafterScreen extends AbstractScreen<DecorationCrafterMenu>{
    private static final Identifier stageOneTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/decoration_crafter_1.png");
    private static final Identifier stageTwoTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/decoration_crafter_2.png");
    private static final Identifier stageThreeTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/decoration_crafter_3.png");
    private static final Identifier stageFourTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/decoration_crafter_4.png");

    public DecorationCrafterScreen(DecorationCrafterMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected Identifier getGuiTexture() {
        return Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/decoration_crafter.png");
    }

    @Override
    protected Identifier getProgressIconTexture() {
        return null; // We won't use this one for this particular screen because it uses 4 different progress textures.
    }

    @Override
    protected SlotLocation getProgressIconTopLeft() {
        return null; // Same as getProgressIconTexture.
    }

    @Override
    protected SlotLocation getWarningIconTopLeft() {
        return new SlotLocation(121, 54);
    }

    protected void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        int stage = menu.getStage();

        if(menu.isCrafting() || stage > 1) {
            // We intentionally leave out breaks so that the correct amount of textures automatically draw based on stage by putting the cases in reversed order.
            switch (stage) {
                case 4: partialOrFull(guiGraphics, x, y, stageFourTexture, 130, 37, 4, stage);
                case 3: partialOrFull(guiGraphics, x, y, stageThreeTexture, 130, 19, 3, stage);
                case 2: partialOrFull(guiGraphics, x, y, stageTwoTexture, 21, 37, 2, stage);
                default:  partialOrFull(guiGraphics, x, y, stageOneTexture, 21, 19, 1, stage); // 1.
            }
        }

        if(stage > 1) // If stage > 1, first stage has been completed and instructions slot is locked until last stage completes.
            render(guiGraphics, lockTexture,x + 27, y + 55, 0, 0, 10, 14, 10, 14);
    }

    private void partialOrFull(GuiGraphics guiGraphics, int x, int y, Identifier texture, int textureX, int textureY, int textureStage, int machineStage){
        boolean mustScale = textureStage == machineStage;
        int textureHeight = mustScale ? menu.getScaledProgress() : 14;
        int difference = 14 - textureHeight;
        render(guiGraphics, texture,x + textureX, y + textureY + difference, 0, difference, 25, textureHeight, 25, 14);
    }
}
