package slimeattack07.decorator.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.Slot;
import slimeattack07.decorator.Decorator;

import javax.sound.sampled.Mixer;

public class MixerScreen extends AbstractScreen<MixerMenu>{
    private static final ResourceLocation stageOneTexture = ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/fire.png");
    private static final ResourceLocation stageTwoTexture = ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/mixer_spill.png");
    private static final ResourceLocation stageThreeTexture = ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/cooling.png");

    public MixerScreen(MixerMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected ResourceLocation getGuiTexture() {
        return ResourceLocation.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/mixer.png");
    }

    @Override
    protected ResourceLocation getProgressIconTexture() {
        return null; // We won't use this one for this particular screen because it uses 3 different progress textures.
    }

    @Override
    protected SlotLocation getProgressIconTopLeft() {
        return null; // Same as getProgressIconTexture.
    }

    @Override
    protected SlotLocation getWarningIconTopLeft() {
        return new SlotLocation(99, 58);
    }

    @Override
    protected void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isFirstStageCrafting())
            draw(guiGraphics, x, y, stageOneTexture, 47, 19, menu.getFirstStageScaledProgress());

        if(menu.isSecondStageCrafting())
            draw(guiGraphics, x, y, stageTwoTexture, 82, 19, menu.getSecondStageScaledProgress());

        if(menu.isThirdStageCrafting())
            draw(guiGraphics, x, y, stageThreeTexture, 117, 19, menu.getThirdStageScaledProgress());
    }

    private void draw(GuiGraphics guiGraphics, int x, int y, ResourceLocation texture, int textureX, int textureY, int scale){
        int difference = 32 - scale;
        render(guiGraphics, texture,x + textureX, y + textureY + difference, 0, difference, 32, scale, 32, 32);
    }
}
