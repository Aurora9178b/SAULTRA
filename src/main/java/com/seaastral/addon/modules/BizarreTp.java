package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import java.util.Random;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

public class BizarreTp extends Module {
   private boolean isblock = false;
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> tprange = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("TpRange")).description("SBTpRange")).defaultValue(30.0).range(2.0, 400.0).build());

   public BizarreTp() {
      super(SeaAstral.CATEGORY, "bizarre-tp", "sbtp");
   }

   private void checkBlock(Vec3 randomPos) {
      BlockPos pos1 = BlockPos.containing(randomPos.x, randomPos.y, randomPos.z);
      BlockPos pos2 = BlockPos.containing(randomPos.x, randomPos.y + 1.2, randomPos.z);
      this.isblock = !this.mc.level.getBlockState(pos1).isAir() || !this.mc.level.getBlockState(pos2).isAir();
   }

   private void shitTp() {
      Random random = new Random();
      Vec3 randomPos = new Vec3(
         this.mc.player.getX() + (random.nextDouble() - 0.5) * (Double)this.tprange.get() * 2.0,
         this.mc.player.getY(),
         this.mc.player.getZ() + (random.nextDouble() - 0.5) * (Double)this.tprange.get() * 2.0
      );
      this.checkBlock(randomPos);
      if (!this.isblock) {
         this.mc.player.setPos(randomPos.x, randomPos.y, randomPos.z);
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.mc.getConnection() != null && this.mc.player != null && this.mc.getConnection() != null) {
         this.shitTp();
      }
   }
}
