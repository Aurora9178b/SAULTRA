package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.phys.Vec3;

public class CircleMove extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> Speed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Speed")).description("Speed.")).defaultValue(0.5).range(0.1, 10.0).build());
   private final Setting<Double> Radius = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Radius")).description("Radius.")).defaultValue(10.0).range(1.0, 100.0).build());
   private double angle = 0.0;
   private Vec3 centerPos = null;

   public void onActivate() {
      if (this.mc.player != null) {
         this.centerPos = this.mc.player.position();
      }

      this.angle = 0.0;
   }

   public CircleMove() {
      super(SeaAstral.CATEGORY, "circle-move", "CircleNow!");
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.centerPos != null) {
         this.angle = this.angle + (Double)this.Speed.get();
         double r = (Double)this.Radius.get();
         double x = this.centerPos.x + r * Math.cos(this.angle);
         double z = this.centerPos.z + r * Math.sin(this.angle);
         Vec3 currentPos = this.mc.player.position();
         this.mc.player.setDeltaMovement(x - currentPos.x, 0.0, z - currentPos.z);
         this.mc.player.setYRot((float)Math.toDegrees(this.angle) - 90.0F);
      }
   }
}
