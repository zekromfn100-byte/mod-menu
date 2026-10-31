package com.example.spearmacro;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.text.Text;

public class SpearMacroScreen extends Screen {

    private ButtonWidget macroButton;
    private ButtonWidget totemButton;

    public SpearMacroScreen() {
        super(Text.literal("Spear Macro"));
    }

    @Override
    protected void init() {
        int cx = this.width / 2;
        int cy = this.height / 2;

        macroButton = ButtonWidget.builder(macroText(), b -> {
            SpearMacroConfig.INSTANCE.macroEnabled = !SpearMacroConfig.INSTANCE.macroEnabled;
            SpearMacroConfig.save();
            b.setMessage(macroText());
        }).dimensions(cx - 100, cy - 30, 200, 20).build();

        totemButton = ButtonWidget.builder(totemText(), b -> {
            SpearMacroConfig.INSTANCE.totemEnabled = !SpearMacroConfig.INSTANCE.totemEnabled;
            SpearMacroConfig.save();
            b.setMessage(totemText());
        }).dimensions(cx - 100, cy - 4, 200, 20).build();

        ButtonWidget done = ButtonWidget.builder(Text.literal("Fertig"), b -> this.close())
                .dimensions(cx - 100, cy + 30, 200, 20).build();

        addDrawableChild(macroButton);
        addDrawableChild(totemButton);
        addDrawableChild(done);
    }

    private Text macroText() {
        return Text.literal("Speer-Macro: " + (SpearMacroConfig.INSTANCE.macroEnabled ? "AN" : "AUS"));
    }

    private Text totemText() {
        return Text.literal("Auto-Totem: " + (SpearMacroConfig.INSTANCE.totemEnabled ? "AN" : "AUS"));
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        context.drawCenteredTextWithShadow(this.textRenderer, this.title, this.width / 2, this.height / 2 - 55, 0xFFFFFFFF);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
