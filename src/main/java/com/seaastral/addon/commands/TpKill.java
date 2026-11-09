package com.seaastral.addon.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

public class TpKill extends Command {
   private Player zhongzhuan;
   private String fkplayer = "";

   public TpKill() {
      super("tpkill", "TpKillSB.", new String[0]);
   }

   private void PlayerS() {
      this.zhongzhuan = null;
      if (mc.level != null && mc.player != null && this.fkplayer != null && !this.fkplayer.isEmpty()) {
         for (Player player : mc.level.players()) {
            if (player != mc.player && !player.isDeadOrDying() && player.getName().getString().equalsIgnoreCase(this.fkplayer)) {
               this.zhongzhuan = player;
               return;
            }
         }
      }
   }

   private void sendMove(Vec3 pos) {
      if (mc.getConnection() != null) {
         ServerboundMovePlayerPacket movePacket = new PosRot(
            pos.x, pos.y, pos.z, mc.player.getYRot(), mc.player.getXRot(), false, mc.player.horizontalCollision
         );
         mc.player.connection.send(movePacket);
      }
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(argument("player", StringArgumentType.word()).executes(context -> {
         this.fkplayer = StringArgumentType.getString(context, "player");
         this.PlayerS();
         if (this.zhongzhuan == null) {
            this.error("NoPlayer" + this.fkplayer, new Object[0]);
         } else {
            FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
            if (!mace.found()) {
               this.error("No mace in inventory.", new Object[0]);
               return 1;
            }

            InvUtils.swap(mace.slot(), true);
            Vec3 xyznow = mc.player.position();
            Vec3 enemyVelocity = this.zhongzhuan.getDeltaMovement();
            Vec3 targetPos = this.zhongzhuan.position();
            Vec3 originalPos = mc.player.position();
            mc.player.setPos(targetPos.x, targetPos.y + 1.2, targetPos.z);

            for (int i = 0; i < 3; i++) {
               double currentFall = 20 + i * 10;
               this.sendMove(new Vec3(mc.player.getX(), mc.player.getY() + currentFall, mc.player.getZ()));
               this.sendMove(targetPos);
               mc.player.setPos(targetPos);
               mc.gameMode.attack(mc.player, this.zhongzhuan);
               mc.player.swing(InteractionHand.MAIN_HAND);
            }

            mc.player.setPos(xyznow.x, xyznow.y, xyznow.z);
            this.zhongzhuan = null;
         }

         return 1;
      }));
   }
}
