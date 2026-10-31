package com.example.spearmacro;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.Entity;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import org.lwjgl.glfw.GLFW;

public class SpearMacroMod implements ClientModInitializer {

    // Hotbar-Slots (0-basiert): Slot 2 = Index 1, Slot 3 = Index 2
    private static final int WIND_SLOT = 1;
    private static final int SPEAR_SLOT = 2;

    // Ticks Pause zwischen den Schritten (bei Lag auf 2 oder 3 erhöhen)
    private static final int STEP_DELAY = 1;

    private static KeyBinding macroKey;
    private static KeyBinding menuKey;

    private int phase = 0; // 0 = Windcharge, 1 = Speer
    private int delay = 0;
    private int totemCooldown = 0;

    @Override
    public void onInitializeClient() {
        SpearMacroConfig.load();

        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("spearmacro", "main"));

        // Vordere Maus-Seitentaste (Maustaste 5)
        macroKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.spearmacro.macro", InputUtil.Type.MOUSE, GLFW.GLFW_MOUSE_BUTTON_5, cat));
        // Right Shift öffnet das Menü
        menuKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.spearmacro.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));

        ClientTickEvents.END_CLIENT_TICK.register(this::tick);
    }

    private void tick(MinecraftClient mc) {
        ClientPlayerEntity player = mc.player;
        if (player == null || mc.interactionManager == null) return;

        if (mc.currentScreen == null) {
            while (menuKey.wasPressed()) {
                mc.setScreen(new SpearMacroScreen());
                return;
            }
        } else {
            return;
        }

        if (totemCooldown > 0) totemCooldown--;
        if (SpearMacroConfig.INSTANCE.totemEnabled) doAutoTotem(mc, player);

        if (SpearMacroConfig.INSTANCE.macroEnabled && macroKey.isPressed()) {
            runMacro(mc, player);
        } else {
            phase = 0;
            delay = 0;
        }
    }

    private void runMacro(MinecraftClient mc, ClientPlayerEntity player) {
        if (delay > 0) {
            delay--;
            return;
        }

        if (phase == 0) {
            player.getInventory().setSelectedSlot(WIND_SLOT);
            var windStack = player.getInventory().getStack(WIND_SLOT);
            if (windStack.isOf(Items.WIND_CHARGE) && !player.getItemCooldownManager().isCoolingDown(windStack)) {
                mc.interactionManager.interactItem(player, Hand.MAIN_HAND);
                player.swingHand(Hand.MAIN_HAND);
            }
            phase = 1;
        } else {
            player.getInventory().setSelectedSlot(SPEAR_SLOT);
            HitResult hit = mc.crosshairTarget;
            if (hit != null && hit.getType() == HitResult.Type.ENTITY) {
                Entity target = ((EntityHitResult) hit).getEntity();
                mc.interactionManager.attackEntity(player, target);
            }
            player.swingHand(Hand.MAIN_HAND);
            phase = 0;
        }
        delay = STEP_DELAY;
    }

    private void doAutoTotem(MinecraftClient mc, ClientPlayerEntity player) {
        if (totemCooldown > 0) return;
        if (player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        for (int i = 0; i < 36; i++) {
            if (player.getInventory().getStack(i).isOf(Items.TOTEM_OF_UNDYING)) {
                int screenSlot = i < 9 ? i + 36 : i;
                // SWAP mit Button 40 = Offhand
                mc.interactionManager.clickSlot(
                        player.playerScreenHandler.syncId, screenSlot, 40,
                        SlotActionType.SWAP, player);
                totemCooldown = 5;
                return;
            }
        }
    }
}
