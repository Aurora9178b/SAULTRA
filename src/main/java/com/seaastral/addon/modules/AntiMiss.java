package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class AntiMiss extends Module {
   public AntiMiss() {
      super(SeaAstral.CATEGORY, "anti-miss", "AntiSBMissMe");
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.mc.level != null) {
         this.fillHotbarWithTotems();
         this.fillOffhandWithTotem();
         this.switchToTotem();
      }
   }

   private void fillHotbarWithTotems() {
      for (int i = 0; i < 9; i++) {
         ItemStack stack = this.mc.player.getInventory().getItem(i);
         if (stack.isEmpty()) {
            int totemSlot = this.findTotemInInventory();
            if (totemSlot != -1) {
               InvUtils.move().from(totemSlot).to(i);
            }
         }
      }
   }

   private void fillOffhandWithTotem() {
      if (this.mc.player.getOffhandItem().getItem() != Items.TOTEM_OF_UNDYING) {
         int totemSlot = this.findTotemInInventory();
         if (totemSlot != -1) {
            InvUtils.move().from(totemSlot).toOffhand();
         }
      }
   }

   private void switchToTotem() {
      if (this.mc.player.getMainHandItem().getItem() != Items.TOTEM_OF_UNDYING) {
         for (int i = 0; i < 9; i++) {
            if (this.mc.player.getInventory().getItem(i).getItem() == Items.TOTEM_OF_UNDYING) {
               InvUtils.swap(i, false);
               return;
            }
         }
      }
   }

   private int findTotemInInventory() {
      for (int i = 9; i < 36; i++) {
         ItemStack stack = this.mc.player.getInventory().getItem(i);
         if (stack.getItem() == Items.TOTEM_OF_UNDYING) {
            return i;
         }
      }

      return -1;
   }
}
