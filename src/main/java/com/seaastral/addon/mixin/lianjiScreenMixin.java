package com.seaastral.addon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public class lianjiScreenMixin {
   private static final Identifier CUSTOM_BG = Identifier.fromNamespaceAndPath("seaastral", "lty.png");

   @Inject(method = "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("HEAD"), cancellable = true)
   private void onExtractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      Minecraft client = Minecraft.getInstance();
      if (client.level == null) {
         ci.cancel();
         int width = context.guiWidth();
         int height = context.guiHeight();
         context.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_BG, 0, 0, 0.0F, 0.0F, width, height, width, height);
         context.fill(0, 0, width, height, Integer.MIN_VALUE);
      }
   }
}
