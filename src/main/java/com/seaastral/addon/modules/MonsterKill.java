package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.skeleton.Skeleton;
import net.minecraft.world.entity.monster.spider.Spider;
import net.minecraft.world.entity.monster.zombie.Zombie;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class MonsterKill extends Module {
   private Entity target = null;
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> range = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Killrange")).description("KillMonsterRange")).defaultValue(30.0).range(1.0, 2.0E7).build());

   public MonsterKill() {
      super(SeaAstral.CATEGORY, "monster-kill", "tpmonster+kill");
   }

   private void sendMove(Vec3 pos) {
      if (this.mc.getConnection() != null) {
         ServerboundMovePlayerPacket movePacket = new PosRot(
            pos.x, pos.y, pos.z, this.mc.player.getYRot(), this.mc.player.getXRot(), false, this.mc.player.horizontalCollision
         );
         this.mc.player.connection.send(movePacket);
      }
   }

   private void MonsterS() {
      Entity atkmon = null;
      double bestDist = Double.MAX_VALUE;

      for (Entity entity : this.mc.level.entitiesForRendering()) {
         if (entity != this.mc.player
            && entity.isAlive()
            && (entity instanceof Spider || entity instanceof Zombie || entity instanceof Skeleton || entity instanceof Creeper)) {
            double dist = this.mc.player.distanceTo(entity);
            if (dist <= (Double)this.range.get() && dist < bestDist) {
               bestDist = dist;
               atkmon = entity;
            }
         }
      }

      this.target = atkmon;
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.mc.getConnection() != null) {
         this.MonsterS();
         Vec3 originalPos = this.mc.player.position();
         if (this.target != null) {
            double x = this.target.getX();
            double y = this.target.getY();
            double z = this.target.getZ();
            FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
            if (mace.found()) {
               InvUtils.swap(mace.slot(), true);
               this.mc.player.setPos(x, y + 1.2, z);

               for (int i = 0; i < 3; i++) {
                  double currentFall = 20 + i * 10;
                  this.sendMove(new Vec3(this.mc.player.getX(), this.mc.player.getY() + currentFall, this.mc.player.getZ()));
                  this.sendMove(this.target.position());
                  Vec3 pos = this.target.position();
                  this.mc.player.setPos(pos.x, pos.y, pos.z);
                  this.mc.gameMode.attack(this.mc.player, this.target);
                  this.mc.player.swing(InteractionHand.MAIN_HAND);
               }

               this.mc.player.setPos(originalPos.x, originalPos.y, originalPos.z);
               this.target = null;
            }
         }
      }
   }
}
