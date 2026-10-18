package slimeattack07.decorator.screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.Inventory;
import slimeattack07.decorator.Decorator;
import slimeattack07.decorator.util.MachineTier;

/** A basic implementation of a screen that assumes the GUI has both a progress texture and a tier texture.
 *
 * @param <T>
 */
public abstract class AbstractScreen<T extends AbstractMenu> extends AbstractContainerScreen<T> {
    private final Identifier guiTexture = getGuiTexture();
    private final Identifier progressIconTexture = getProgressIconTexture();
    private final Identifier warningIconTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/warning.png");

    private static final Identifier basicMachineTierTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/basic.png");
    private static final Identifier experiencedMachineTierTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/experienced.png");
    private static final Identifier expertMachineTierTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/expert.png");
    private static final Identifier masterMachineTierTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/master.png");
    private static final Identifier legendMachineTierTexture = Identifier.fromNamespaceAndPath(Decorator.MOD_ID,"textures/gui/legend.png");

    public AbstractScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (width - imageWidth) / 2;   // imageWidth = 176 if not overridden here.
        int y = (height - imageHeight) / 2; // imageHeight = 166 if not overridden here.

        guiGraphics.blit(RenderPipelines.GUI_TEXTURED, guiTexture, x, y, 0, 0, imageWidth, imageHeight, 256, 256);
        renderProgressArrow(guiGraphics, x, y);
        renderTier(guiGraphics, x, y);
        renderWarningIcon(guiGraphics, x, y);
    }

    private void renderProgressArrow(GuiGraphics guiGraphics, int x, int y) {
        if(menu.isCrafting()){
            SlotLocation location = getProgressIconTopLeft();
            int textureHeight = menu.getScaledProgress();
            int difference = 32 - textureHeight;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, progressIconTexture,x + location.x(), y + location.y() + difference, 0, difference, 32, textureHeight, 32, 32);
        }
    }

    private void renderTier(GuiGraphics guiGraphics, int x, int y){
        if(menu.hasExperience()){
            MachineTier tier = menu.getCurrentMachineTier();
            Identifier texture = switch(tier){
                case EXPERIENCED -> experiencedMachineTierTexture;
                case EXPERT -> expertMachineTierTexture;
                case MASTER -> masterMachineTierTexture;
                case LEGEND -> legendMachineTierTexture;
                case null, default -> basicMachineTierTexture;
            };

            SlotLocation location = getExpBarLocation();
            int textureHeight = menu.getHeightOfTierTexture();
            int difference = 60 - textureHeight;
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, texture,x + location.x(), y + location.y() + difference, 0, difference, 10, textureHeight, 10, 60);
        }
    }

    private void renderWarningIcon(GuiGraphics guiGraphics, int x, int y){
        if(menu.showWarningIcon()){
            SlotLocation location = getWarningIconTopLeft();
            guiGraphics.blit(RenderPipelines.GUI_TEXTURED, warningIconTexture,x + location.x(), y + location.y(), 0, 0, 16, 16, 16, 16);
        }
    }

    private void renderTierBar(GuiGraphics guiGraphics, int x, int y){
        MachineTier tier = menu.getCurrentMachineTier();
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);

        renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }

    protected SlotLocation getExpBarLocation(){
        return new SlotLocation(159, 13);
    }

    // ABSTRACT METHODS ====================
    protected abstract Identifier getGuiTexture();

    protected abstract Identifier getProgressIconTexture();

    protected abstract SlotLocation getProgressIconTopLeft();

    protected abstract SlotLocation getWarningIconTopLeft();
}