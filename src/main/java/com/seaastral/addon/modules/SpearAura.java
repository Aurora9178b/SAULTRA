package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import meteordevelopment.meteorclient.events.world.TickEvent.Pre;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

public class SpearAura extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> speed = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Speed")).description("SpearSpeed")).defaultValue(4.5).range(0.1, 100.0).build());
   private final Setting<Double> range = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Range")).description("Range")).defaultValue(500.0).range(10.0, 1000.0).build());
   private Vec3 originalpos;
   private Player target;
   private boolean charging;

   public SpearAura() {
      super(SeaAstral.CATEGORY, "spear-aura", "Fuck2kwSB");
   }

   public void onActivate() {
      this.originalpos = null;
      this.target = null;
      this.charging = false;
   }

   public void onDeactivate() {
      this.stopCharging();
      if (this.originalpos != null && this.mc.player != null) {
         this.mc.player.setPos(this.originalpos.x, this.originalpos.y, this.originalpos.z);
         this.originalpos = null;
      }

      this.target = null;
   }

   @EventHandler
   private void onTick(Pre event) {
      if (this.mc.player != null && this.mc.level != null && this.mc.gameMode != null) {
         if (this.target == null || !this.target.isDeadOrDying() && !(this.target.getHealth() <= 0.0F) && this.target.isAlive()) {
            if (this.target == null) {
               this.target = this.findNearestPlayer();
               if (this.target != null) {
                  this.originalpos = new Vec3(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
                  Vec3 zhongzhuan = new Vec3(this.target.getX(), this.target.getY(), this.target.getZ());
                  this.mc.player.setPos(zhongzhuan.x, zhongzhuan.y, zhongzhuan.z + 1.0);
                  this.charging = true;
               }
            } else {
               if (this.charging) {
                  this.mc.gameMode.useItem(this.mc.player, InteractionHand.MAIN_HAND);
               }

               Vec3 targetPos = new Vec3(this.target.getX(), this.target.getY(), this.target.getZ());
               Vec3 current = new Vec3(this.mc.player.getX(), this.mc.player.getY(), this.mc.player.getZ());
               Vec3 direction = targetPos.subtract(current).normalize();
               double speedVal = (Double)this.speed.get();
               Vec3 next = current.add(direction.scale(speedVal));
               this.mc.player.setPos(next.x, next.y, next.z);
               this.faceTarget(this.target);
               if (this.mc.player.distanceTo(this.target) < 2.0 && this.originalpos != null) {
                  this.mc.player.setPos(this.originalpos.x, this.originalpos.y, this.originalpos.z);
               }
            }
         } else {
            this.stopCharging();
            if (this.originalpos != null) {
               this.mc.player.setPos(this.originalpos.x, this.originalpos.y, this.originalpos.z);
               this.originalpos = null;
            }

            this.target = null;
         }
      }
   }

   private Player findNearestPlayer() {
      Player closest = null;
      double bestDist = (Double)this.range.get();

      for (Player player : this.mc.level.players()) {
         if (player != this.mc.player && !player.isDeadOrDying() && !(player.getHealth() <= 0.0F)) {
            double dist = this.mc.player.distanceTo(player);
            if (dist <= bestDist) {
               bestDist = dist;
               closest = player;
            }
         }
      }

      return closest;
   }

   private void faceTarget(Player target) {
      if (this.mc.player != null && target != null) {
         Vec3 diff = new Vec3(target.getX() - this.mc.player.getX(), target.getEyeY() - this.mc.player.getEyeY(), target.getZ() - this.mc.player.getZ());
         double horizDist = Math.sqrt(diff.x * diff.x + diff.z * diff.z);
         float yaw = (float)(Math.toDegrees(Math.atan2(diff.z, diff.x)) - 90.0);
         float pitch = (float)(-Math.toDegrees(Math.atan2(diff.y, horizDist)));
         this.mc.player.setYRot(yaw);
         this.mc.player.setXRot(pitch);
      }
   }

   private void stopCharging() {
      if (this.charging && this.mc.gameMode != null && this.mc.player != null) {
         this.mc.gameMode.releaseUsingItem(this.mc.player);
      }

      this.charging = false;
   }
}
