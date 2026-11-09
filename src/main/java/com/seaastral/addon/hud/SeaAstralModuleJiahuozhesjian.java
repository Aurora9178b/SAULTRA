package com.seaastral.addon.hud;

import com.seaastral.addon.SeaAstral;
import java.util.ArrayList;
import java.util.List;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.events.render.Render2DEvent;
import meteordevelopment.meteorclient.renderer.text.TextRenderer;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.render.color.Color;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.Minecraft;

public class SeaAstralModuleJiahuozhesjian {
   private final Minecraft mc = Minecraft.getInstance();
   private final List<SeaAstralModuleJiahuozhesjian.HudEntry> entries = new ArrayList<>();
   private final Color white = new Color(255, 255, 255);
   private final Color cyan = new Color(0, 255, 255);
   private final Color gray = new Color(170, 170, 170);
   private static final double SLIDE_IN_START = -100.0;
   private static final double ANIMATION_SPEED = 0.15;
   private static final double FONT_SCALE = 1.25;

   public SeaAstralModuleJiahuozhesjian() {
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   @EventHandler
   private void onRender2D(Render2DEvent event) {
      List<Module> activeModules = new ArrayList<>();

      for (Module m : Modules.get().getAll()) {
         if (m.isActive() && m.category == SeaAstral.CATEGORY) {
            activeModules.add(m);
         }
      }

      for (Module mx : activeModules) {
         boolean exists = false;

         for (SeaAstralModuleJiahuozhesjian.HudEntry entry : this.entries) {
            if (entry.module == mx) {
               exists = true;
               if (!entry.isActive) {
                  entry.isActive = true;
                  entry.targetAlpha = 1.0;
                  entry.xOffset = -100.0;
               }
               break;
            }
         }

         if (!exists) {
            SeaAstralModuleJiahuozhesjian.HudEntry newEntry = new SeaAstralModuleJiahuozhesjian.HudEntry(mx);
            newEntry.xOffset = -100.0;
            this.entries.add(newEntry);
         }
      }

      for (SeaAstralModuleJiahuozhesjian.HudEntry entryx : this.entries) {
         if (entryx.isActive && !entryx.module.isActive()) {
            entryx.isActive = false;
            entryx.targetAlpha = 0.0;
         }
      }

      TextRenderer renderer = TextRenderer.get();
      int screenHeight = this.mc.getWindow().getGuiScaledHeight();
      double startX = 10.0;
      double startY = screenHeight * 1.25;
      renderer.begin(1.25);
      renderer.render("SA ULTRA Client 0.1.2", startX, startY, this.cyan, true);
      renderer.end();
      double currentY = startY + 25.0;
      double lineSpacing = 18.75;

      for (int i = 0; i < this.entries.size(); i++) {
         SeaAstralModuleJiahuozhesjian.HudEntry entryxx = this.entries.get(i);
         if (entryxx.isActive) {
            double diffX = -entryxx.xOffset;
            entryxx.xOffset += diffX * 0.2;
            if (Math.abs(entryxx.xOffset) < 1.0) {
               entryxx.xOffset = 0.0;
            }
         } else {
            entryxx.xOffset = entryxx.xOffset + (100.0 - entryxx.xOffset) * 0.3;
         }

         entryxx.alpha = entryxx.alpha + (entryxx.targetAlpha - entryxx.alpha) * 0.2;
         double diffY = currentY - entryxx.yOffset;
         entryxx.yOffset += diffY * 0.15;
         if (!(entryxx.alpha <= 0.05) || entryxx.isActive) {
            double drawX = startX + entryxx.xOffset;
            double drawY = entryxx.yOffset;
            Color drawColor = new Color(this.white.r, this.white.g, this.white.b, (int)(entryxx.alpha * 255.0));
            Color prefixColor = new Color(this.cyan.r, this.cyan.g, this.cyan.b, (int)(entryxx.alpha * 255.0));
            Color nameColor = entryxx.isActive ? drawColor : new Color(this.gray.r, this.gray.g, this.gray.b, (int)(entryxx.alpha * 255.0));
            String prefix = entryxx.isActive ? "[+] " : "[-] ";
            String name = entryxx.module.name;
            renderer.begin(1.25);
            renderer.render(prefix, drawX, drawY, prefixColor, true);
            renderer.render(name, drawX + renderer.getWidth(prefix), drawY, nameColor, true);
            renderer.end();
            if (!entryxx.isActive && entryxx.xOffset > 50.0) {
               this.entries.remove(i);
               i--;
            }

            currentY += lineSpacing;
         }
      }
   }

   private static class HudEntry {
      Module module;
      double yOffset;
      double targetYOffset;
      double xOffset;
      boolean isActive;
      double alpha;
      double targetAlpha;

      HudEntry(Module module) {
         this.module = module;
         this.yOffset = 0.0;
         this.targetYOffset = 0.0;
         this.xOffset = 0.0;
         this.isActive = true;
         this.alpha = 0.0;
         this.targetAlpha = 1.0;
      }
   }
}
