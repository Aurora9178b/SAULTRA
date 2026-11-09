package com.seaastral.addon;

import com.mojang.logging.LogUtils;
import com.seaastral.addon.commands.TpCommand;
import com.seaastral.addon.commands.TpKill;
import com.seaastral.addon.commands.TpPop;
import com.seaastral.addon.hud.SeaAstralModuleJiahuozhesjian;
import com.seaastral.addon.modules.AntiMiss;
import com.seaastral.addon.modules.AutoEZ;
import com.seaastral.addon.modules.AutoMama;
import com.seaastral.addon.modules.BizarreTp;
import com.seaastral.addon.modules.Circle;
import com.seaastral.addon.modules.CircleMove;
import com.seaastral.addon.modules.MonsterKill;
import com.seaastral.addon.modules.SpearAura;
import com.seaastral.addon.modules.TpGo;
import com.seaastral.addon.modules.TpMace;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import meteordevelopment.meteorclient.MeteorClient;
import meteordevelopment.meteorclient.addons.MeteorAddon;
import meteordevelopment.meteorclient.commands.Commands;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.systems.config.Config;
import meteordevelopment.meteorclient.systems.modules.Category;
import meteordevelopment.meteorclient.systems.modules.Modules;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWImage;
import org.lwjgl.glfw.GLFWImage.Buffer;
import org.lwjgl.stb.STBImage;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;
import org.slf4j.Logger;

public class SeaAstral extends MeteorAddon {
   public static final Logger LOG = LogUtils.getLogger();
   public static final Category CATEGORY = new Category("SA ULTRA");

   private void windowDraw() {
      if (MeteorClient.mc != null && MeteorClient.mc.getWindow() != null) {
         long windowHandle = MeteorClient.mc.getWindow().handle();

         try (InputStream stream = SeaAstral.class.getResourceAsStream("/assets/seaastral/ltytox.png")) {
            if (stream != null) {
               byte[] bytes = stream.readAllBytes();
               ByteBuffer fileBuffer = MemoryUtil.memAlloc(bytes.length);

               try {
                  fileBuffer.put(bytes).flip();
                  MemoryStack stack = MemoryStack.stackPush();

                  try {
                     IntBuffer w = stack.mallocInt(1);
                     IntBuffer h = stack.mallocInt(1);
                     IntBuffer channels = stack.mallocInt(1);
                     ByteBuffer image = STBImage.stbi_load_from_memory(fileBuffer, w, h, channels, 4);
                     if (image != null) {
                        try {
                           Buffer icons = GLFWImage.malloc(1);

                           try {
                              icons.position(0);
                              icons.width(w.get(0));
                              icons.height(h.get(0));
                              icons.pixels(image);
                              GLFW.glfwSetWindowIcon(windowHandle, icons);
                           } finally {
                              icons.free();
                           }
                        } finally {
                           STBImage.stbi_image_free(image);
                        }
                     }
                  } finally {
                     stack.close();
                  }
               } finally {
                  MemoryUtil.memFree(fileBuffer);
               }
            }
         } catch (Exception e) {
            LOG.error("有个错误", e);
         }
      }
   }

   public void onInitialize() {
      ChatUtils.registerCustomPrefix("com.seaastral.addon", () -> Component.literal("§f[§bSA ULTRA§f]"));
      LOG.info("Initializing SA ULTRA Client");
      Modules.get().add(new TpMace());
      Modules.get().add(new Circle());
      Modules.get().add(new TpGo());
      Modules.get().add(new CircleMove());
      Modules.get().add(new AntiMiss());
      Modules.get().add(new MonsterKill());
      Modules.get().add(new BizarreTp());
      Modules.get().add(new AutoEZ());
      Modules.get().add(new AutoMama());
      Modules.get().add(new SpearAura());
      Commands.add(new TpCommand());
      Commands.add(new TpKill());
      Commands.add(new TpPop());
      new SeaAstralModuleJiahuozhesjian();
      MeteorClient.EVENT_BUS.subscribe(this);
   }

   public void onRegisterCategories() {
      Modules.registerCategory(CATEGORY);
   }

   public String getPackage() {
      return "com.seaastral.addon";
   }

   @EventHandler
   private void onTick(Post event) {
      if (MeteorClient.mc != null) {
         Config.get().customWindowTitle.set(true);
         Config.get().customWindowTitleText.set("SA ULTRA Client");
         MeteorClient.mc.updateTitle();
         this.windowDraw();
      }
   }
}
