package eu.invouk.projectnuclear.gui.screen;

import eu.invouk.projectnuclear.Projectnuclear;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;

import java.util.ArrayList;
import java.util.List;

public abstract class GenericContainerScreen<T extends AbstractContainerMenu> extends AbstractContainerScreen<T> {

    public record ItemSlot(int x, int y){}

    private static final ResourceLocation COAL_GENERATOR_TEXTURE = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/generic_background.png");
    private static final ResourceLocation ITEM_SLOT_TEXTURE = ResourceLocation.fromNamespaceAndPath(Projectnuclear.MODID, "textures/gui/widgets/slot.png");

    private final List<AbstractWidget> widgets = new ArrayList<>();
    private final List<ItemSlot> itemSlots = new ArrayList<>();

    protected int centerX, centerY;

    public GenericContainerScreen(T menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);

        this.imageWidth = 176;
        this.imageHeight = 201;
    }

    @Override
    protected void init() {
        super.init();

        widgets.clear();
        itemSlots.clear();

        for (int i = 0; i < this.menu.slots.size() - 36; i++) {
            Slot slot = this.menu.slots.get(i);
            registerItemSlot(slot.x + this.leftPos-1, slot.y + this.topPos-1);
        }

        centerX = (this.width / 2);
        centerY = (this.height/2);
        this.titleLabelX = (this.imageWidth - this.font.width(this.title)) / 2;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);

        getWidgets().forEach((widget) -> widget.render(guiGraphics, mouseX, mouseY, partialTicks));
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float v, int i, int i1) {
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;

        guiGraphics.blit(RenderType::guiTextured, COAL_GENERATOR_TEXTURE, x, y, 0,0, this.imageWidth, this.imageHeight, 256, 256);
        itemSlots.forEach((itemSlot) -> guiGraphics.blit(RenderType::guiTexturedOverlay, ITEM_SLOT_TEXTURE, itemSlot.x(), itemSlot.y(), 0,0, 18,18,18,18));
    }

    public List<? extends AbstractWidget> getWidgets() {
        return widgets;
    }

    public void registerWidget(AbstractWidget abstractWidget) {
        widgets.add(abstractWidget);
    }

    public void registerItemSlot(ItemSlot itemSlot) {
        itemSlots.add(itemSlot);
    }

    public void registerItemSlot(int x, int y) {
        registerItemSlot(new ItemSlot(x, y));
    }


}
