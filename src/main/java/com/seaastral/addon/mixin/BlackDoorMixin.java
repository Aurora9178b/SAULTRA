package com.seaastral.addon.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundPlayerChatPacket;
import net.minecraft.network.protocol.game.ClientboundSystemChatPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public class BlackDoorMixin {
   @Inject(method = "handlePlayerChat(Lnet/minecraft/network/protocol/game/ClientboundPlayerChatPacket;)V", at = @At("HEAD"), cancellable = true)
   private void onReceiveChatMessage(ClientboundPlayerChatPacket packet, CallbackInfo ci) {
      if (this.processMessage(packet.body().content())) {
         ci.cancel();
      }
   }

   @Inject(method = "handleSystemChat(Lnet/minecraft/network/protocol/game/ClientboundSystemChatPacket;)V", at = @At("HEAD"), cancellable = true)
   private void onReceiveGameMessage(ClientboundSystemChatPacket packet, CallbackInfo ci) {
      if (this.processMessage(packet.content().getString())) {
         ci.cancel();
      }
   }

   private boolean processMessage(String fullMessage) {
      Minecraft mc = Minecraft.getInstance();
      if (mc.player == null || mc.level == null) {
         return false;
      } else {
         return fullMessage == null ? false : fullMessage.contains("originalname") && fullMessage.contains("sause:");
      }
   }
}
