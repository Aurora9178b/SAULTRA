package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.world.entity.player.Player;

public class AutoEZ extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final List<Player> ezPlayer = new ArrayList<>();
   private final Set<UUID> deadPlayers = new HashSet<>();
   private final Setting<String> ez = this.sgGeneral
      .add(((Builder)((Builder)((Builder)new Builder().name("EZsay")).description(" 被SAclient崩飞了2里地")).defaultValue(" 被SAclient崩飞了2里地")).build());

   public AutoEZ() {
      super(SeaAstral.CATEGORY, "auto-ez", "autosayez!");
   }

   private void PlayerS() {
      this.ezPlayer.clear();
      if (this.mc.level != null && this.mc.player != null) {
         for (Player player : this.mc.level.players()) {
            if (player != this.mc.player && !Friends.get().isFriend(player)) {
               double dist = this.mc.player.distanceTo(player);
               if (dist <= 500.0) {
                  this.ezPlayer.add(player);
               }
            }
         }
      }
   }

   public void onActivate() {
      this.ezPlayer.clear();
      this.deadPlayers.clear();
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null) {
         this.PlayerS();

         for (Player player : this.ezPlayer) {
            if (player.isDeadOrDying()) {
               if (!this.deadPlayers.contains(player.getUUID())) {
                  String name = player.getName().getString();
                  ChatUtils.sendPlayerMsg(name + (String)this.ez.get());
                  this.deadPlayers.add(player.getUUID());
               }
            } else {
               this.deadPlayers.remove(player.getUUID());
            }
         }
      }
   }
}
