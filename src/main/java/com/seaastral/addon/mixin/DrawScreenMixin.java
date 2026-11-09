package com.seaastral.addon.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(TitleScreen.class)
public class DrawScreenMixin {
   private static final Identifier CUSTOM_BG = Identifier.fromNamespaceAndPath("seaastral", "lty.png");

   @Inject(method = "extractBackground(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("TAIL"))
   private void onExtractBackground(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      int width = context.guiWidth();
      int height = context.guiHeight();
      context.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_BG, 0, 0, 0.0F, 0.0F, width, height, width, height);
   }
}
