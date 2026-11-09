package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.StringSetting.Builder;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.ChatUtils;
import meteordevelopment.orbit.EventHandler;

public class AutoMama extends Module {
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<String> mama = this.sgGeneral
      .add(((Builder)((Builder)((Builder)((Builder)new Builder().name("mama")).description("iaa")).defaultValue("iaa")).onChanged(v -> {
         if (this.isActive()) {
            this.rebuildList();
         }
      })).build());
   private List<String> malist;
   private int cooldown = 0;

   public AutoMama() {
      super(SeaAstral.CATEGORY, "auto-mama", "AutoSayxxxmama");
   }

   public void onActivate() {
      this.cooldown = 0;
      this.rebuildList();
   }

   private void rebuildList() {
      String m = (String)this.mama.get();
      if (m == null || m.isEmpty()) {
         m = "iaa";
      }

      this.malist = new ArrayList<>(
         Arrays.asList(
            "妈妈(" + m + ")啊妈妈啊我想你，想在黑夜中哭泣!",
            "妈妈(" + m + ")我错了，不要再榨金了啊！",
            "妈妈(" + m + ")我错了，饶了我吧！",
            "呜呜呜，妈妈(" + m + ")不要打我了。。我知道错了",
            "妈妈妈妈(" + m + ")来闻闻我的大臭脚",
            "妈妈妈妈(" + m + ")好香啊~哇哦，！？❤❤❤",
            "妈妈(" + m + ")我给你下跪，不要曹我啊○|￣|_○|￣|_○|￣|_"
         )
      );
   }

   private String mamaSay() {
      if (this.malist != null && !this.malist.isEmpty()) {
         int start = 0;
         int end = Math.min(7, this.malist.size());
         List<String> sub = this.malist.subList(start, end);
         return sub.isEmpty() ? "" : sub.get(ThreadLocalRandom.current().nextInt(sub.size()));
      } else {
         return "";
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.mc.getConnection() != null) {
         if (this.cooldown > 0) {
            this.cooldown--;
         } else {
            String msg = this.mamaSay();
            if (msg != null && !msg.isEmpty()) {
               ChatUtils.sendPlayerMsg(msg);
            }

            this.cooldown = 20;
         }
      }
   }
}
