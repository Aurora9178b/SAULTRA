package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.phys.Vec3;

public class Circle extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> radius = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Radius")).description("Orbit radius in blocks.")).defaultValue(10.0).range(3.0, 50.0).build());
   private final Setting<Double> speed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Speed")).description("Orbit speed in blocks per second.")).defaultValue(10.0).range(1.0, 100.0).build());
   private Vec3 center;
   private double angle;

   public Circle() {
      super(SeaAstral.CATEGORY, "circle", "Orbit around the fixed activation point.");
   }

   public void onActivate() {
      this.center = this.mc.player != null ? this.mc.player.position() : null;
      this.angle = 0.0;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.center != null) {
         double r = (Double)this.radius.get();
         this.angle = this.angle + (Double)this.speed.get() / 20.0 / r;
         if (this.angle > Math.PI * 2) {
            this.angle -= Math.PI * 2;
         }

         double x = this.center.x + Math.cos(this.angle) * r;
         double z = this.center.z + Math.sin(this.angle) * r;
         double y = this.center.y;
         float yaw = (float)Math.toDegrees(this.angle);
         this.mc.player.setPos(x, y, z);
         this.mc.player.setYRot(yaw);
         this.mc.player.setDeltaMovement(Vec3.ZERO);
         this.mc.player.fallDistance = 0.0;
         if (this.mc.getConnection() != null) {
            this.mc.getConnection().send(new PosRot(x, y, z, yaw, this.mc.player.getXRot(), this.mc.player.onGround(), this.mc.player.horizontalCollision));
         }
      }
   }
}
