package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.phys.Vec3;

public class TpGo extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> y = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Y")).description("Y")).defaultValue(2.0E7).range(-100.0, 5.0E7).build());

   public TpGo() {
      super(SeaAstral.CATEGORY, "tpgo", "Tp");
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null) {
         Vec3 pos = this.mc.player.position();
         this.mc.player.setPos(pos.x, (Double)this.y.get(), pos.z);
         this.toggle();
      }
   }
}
