package com.seaastral.addon.commands;

import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import meteordevelopment.meteorclient.commands.Command;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.world.phys.Vec3;

public class TpCommand extends Command {
   public TpCommand() {
      super("tp", "tpxyz", new String[0]);
   }

   public void build(LiteralArgumentBuilder<ClientSuggestionProvider> builder) {
      builder.then(
         argument("x", DoubleArgumentType.doubleArg())
            .then(argument("y", DoubleArgumentType.doubleArg()).then(argument("z", DoubleArgumentType.doubleArg()).executes(context -> {
               double x = DoubleArgumentType.getDouble(context, "x");
               double y = DoubleArgumentType.getDouble(context, "y");
               double z = DoubleArgumentType.getDouble(context, "z");
               Vec3 pos = new Vec3(x, y, z);
               if (mc.player != null) {
                  mc.player.setPos(pos.x, pos.y, pos.z);
               }

               return 1;
            })))
      );
   }
}
