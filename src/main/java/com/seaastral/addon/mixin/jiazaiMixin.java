package com.seaastral.addon.mixin;

import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LoadingOverlay.class)
public class jiazaiMixin {
   @Shadow
   private float currentProgress;
   private static final Identifier CUSTOM_LOGO = Identifier.fromNamespaceAndPath("seaastral", "seaastralyishuzi.png");
   private static final Identifier CUSTOM_BG = Identifier.fromNamespaceAndPath("seaastral", "ltyjiazai.png");

   @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V", at = @At("TAIL"))
   private void onExtractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, CallbackInfo ci) {
      int screenWidth = context.guiWidth();
      int screenHeight = context.guiHeight();
      context.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_BG, 0, 0, 0.0F, 0.0F, screenWidth, screenHeight, screenWidth, screenHeight);
      int logoWidth = 480;
      int logoHeight = 60;
      int logoX = (screenWidth - logoWidth) / 2;
      int logoY = (screenHeight - logoHeight) / 2 - 20;
      context.blit(RenderPipelines.GUI_TEXTURED, CUSTOM_LOGO, logoX, logoY, 0.0F, 0.0F, logoWidth, logoHeight, logoWidth, logoHeight);
      int barWidth = 200;
      int barHeight = 4;
      int barX = (screenWidth - barWidth) / 2;
      int barY = (int)(screenHeight * 0.8);
      context.fill(barX - 1, barY - 1, barX + barWidth + 1, barY + barHeight + 1, -11184811);
      context.fill(barX, barY, barX + barWidth, barY + barHeight, -16777216);
      float actualProgress = Mth.clamp(this.currentProgress, 0.0F, 1.0F);
      int fillWidth = (int)(barWidth * actualProgress);
      context.fill(barX, barY, barX + fillWidth, barY + barHeight, -1);
   }
}
