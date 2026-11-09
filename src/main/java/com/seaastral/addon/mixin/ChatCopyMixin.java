package com.seaastral.addon.mixin;

import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.components.ChatComponent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.ClickEvent.CopyToClipboard;
import net.minecraft.network.chat.HoverEvent.ShowText;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

@Mixin(ChatComponent.class)
public class ChatCopyMixin {
   @ModifyVariable(
      method = "addMessage(Lnet/minecraft/network/chat/Component;Lnet/minecraft/network/chat/MessageSignature;Lnet/minecraft/client/multiplayer/chat/GuiMessageSource;Lnet/minecraft/client/multiplayer/chat/GuiMessageTag;)V",
      at = @At("HEAD"),
      argsOnly = true
   )
   private Component addCopyButton(Component originalMessage) {
      if (originalMessage == null) {
         return null;
      }

      String plainText = originalMessage.getString();
      MutableComponent copyPrefix = Component.literal("§f[§cC§f] ")
         .setStyle(
            Style.EMPTY
               .withColor(ChatFormatting.WHITE)
               .withClickEvent(new CopyToClipboard(plainText))
               .withHoverEvent(new ShowText(Component.literal("Copy is")))
         );
      return copyPrefix.append(originalMessage);
   }
}
