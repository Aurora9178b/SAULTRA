package com.seaastral.addon.modules;

import com.seaastral.addon.SeaAstral;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import meteordevelopment.meteorclient.events.world.TickEvent.Post;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.settings.DoubleSetting.Builder;
import meteordevelopment.meteorclient.systems.friends.Friends;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.Pos;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.PosRot;
import net.minecraft.network.protocol.game.ServerboundMovePlayerPacket.StatusOnly;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class TpMace extends Module {
   private static final int MIN_PREDICT = 3;
   private static final int MAX_PREDICT = 6;
   private static final double MAX_PREDICT_SPEED = 64.0;
   private static final double PREDICT_MERGE_DISTANCE_SQR = 0.0625;
   private static final double MIN_MOVE_EPS = 1.0E-7;
   private Player zhongzhuan;
   private boolean isArmor = false;
   private boolean hasAngle = false;
   private double lastAngle = 0.0;
   private double angularSpeed = 0.0;
   private final Map<Entity, Vec3> lastVelocity = new WeakHashMap<>(16);
   private final SettingGroup sgGeneral = this.settings.getDefaultGroup();
   private final Setting<Double> range = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Range")).description("Range")).defaultValue(500.0).range(20.0, 4.0E7).build());
   private final Setting<Boolean> LBLTMode = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("LBLTMode"))
                  .description("LBLTMode"))
               .defaultValue(false))
            .build()
      );
   private final Setting<Integer> fallfor = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("fallfor"))
                  .description("fallllllllllll"))
               .defaultValue(3))
            .range(1, 10)
            .build()
      );
   private final Setting<Double> onefall = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Fall")).description("SBFall")).defaultValue(20.0).range(1.0, 320.0).build());
   private final Setting<Double> fallpp = this.sgGeneral
      .add(((Builder)((Builder)new Builder().name("Fall++")).description("Fall++")).defaultValue(10.0).range(1.0, 320.0).build());
   private final Setting<Boolean> predict = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("predict1"))
                  .description("predictON/OFF"))
               .defaultValue(true))
            .build()
      );
   private final Setting<Boolean> predictcir = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)((meteordevelopment.meteorclient.settings.BoolSetting.Builder)new meteordevelopment.meteorclient.settings.BoolSetting.Builder()
                     .name("predictcir"))
                  .description("predictcirON/OFF"))
               .defaultValue(false))
            .build()
      );
   private final Setting<Integer> pt = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("P1Tick"))
                  .description("Tickpredict"))
               .defaultValue(4))
            .range(1, 800)
            .build()
      );
   private final Setting<Integer> circleTicks = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("CircleTick"))
                  .description("CircleTick"))
               .defaultValue(2))
            .range(1, 10)
            .build()
      );
   private final Setting<Integer> predictCount = this.sgGeneral
      .add(
         ((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)((meteordevelopment.meteorclient.settings.IntSetting.Builder)new meteordevelopment.meteorclient.settings.IntSetting.Builder()
                     .name("PredictPositions"))
                  .description("How many legal next-tick positions to predict and attack (3-6)"))
               .defaultValue(5))
            .range(3, 6)
            .build()
      );

   public void onActivate() {
      this.lastAngle = 0.0;
      this.angularSpeed = 0.0;
      this.hasAngle = false;
      this.zhongzhuan = null;
      this.lastVelocity.clear();
   }

   private void sendMove(Vec3 pos) {
      if (this.mc.getConnection() != null) {
         ServerboundMovePlayerPacket movePacket = new PosRot(
            pos.x, pos.y, pos.z, this.mc.player.getYRot(), this.mc.player.getXRot(), false, this.mc.player.horizontalCollision
         );
         this.mc.player.connection.send(movePacket);
      }
   }

   public TpMace() {
      super(SeaAstral.CATEGORY, "tp-mace", "TpKill");
   }

   private void PlayerS() {
      Player ATKplayer = null;
      double mD = Double.MAX_VALUE;

      for (Player player : this.mc.level.players()) {
         if (player != this.mc.player && !player.isDeadOrDying() && !Friends.get().isFriend(player) && !player.getName().getString().equals("originalname")) {
            double dist = this.mc.player.distanceTo(player);
            if (dist <= (Double)this.range.get() && dist < mD) {
               mD = dist;
               if (player != this.zhongzhuan) {
                  this.lastAngle = 0.0;
                  this.angularSpeed = 0.0;
               }

               this.zhongzhuan = player;
            }
         }
      }
   }

   private void dataPacket() {
      if (this.mc.player != null && this.mc.getConnection() != null && this.zhongzhuan != null) {
         Vec3 currentPos = this.mc.player.position();
         Vec3 targetPos = new Vec3(this.zhongzhuan.getX(), this.zhongzhuan.getY() + 1.5, this.zhongzhuan.getZ());
         double distance = currentPos.distanceTo(targetPos);
         double stepSize = 5.0;
         int steps = (int)Math.ceil(distance / stepSize);
         if (steps <= 1) {
            this.mc.player.setPos(targetPos.x, targetPos.y, targetPos.z);
            this.mc.getConnection().send(new Pos(targetPos.x, targetPos.y, targetPos.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
         } else {
            double dx = (targetPos.x - currentPos.x) / steps;
            double dy = (targetPos.y - currentPos.y) / steps;
            double dz = (targetPos.z - currentPos.z) / steps;

            for (int i = 1; i <= steps; i++) {
               double x = currentPos.x + dx * i;
               double y = currentPos.y + dy * i;
               double z = currentPos.z + dz * i;
               this.mc.player.setPos(x, y, z);
               this.mc.getConnection().send(new Pos(x, y, z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
            }
         }
      }
   }

   private void hasArmor(Player target) {
      if (target != null) {
         this.isArmor = false;

         for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            ItemStack stack = target.getItemBySlot(slot);
            if (!stack.isEmpty()) {
               this.isArmor = true;
            }
         }
      }
   }

   private void DataPacketBA(Vec3 targetPos) {
      if (this.mc.player != null && this.mc.getConnection() != null && targetPos != null) {
         Vec3 currentPos = this.mc.player.position();
         double distance = currentPos.distanceTo(targetPos);
         double stepSize = 5.0;
         int steps = (int)Math.ceil(distance / stepSize);
         if (steps <= 1) {
            this.mc.player.setPos(targetPos.x, targetPos.y, targetPos.z);
            this.mc.getConnection().send(new Pos(targetPos.x, targetPos.y, targetPos.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
         } else {
            double dx = (targetPos.x - currentPos.x) / steps;
            double dy = (targetPos.y - currentPos.y) / steps;
            double dz = (targetPos.z - currentPos.z) / steps;

            for (int i = 1; i <= steps; i++) {
               double x = currentPos.x + dx * i;
               double y = currentPos.y + dy * i;
               double z = currentPos.z + dz * i;
               this.mc.player.setPos(x, y, z);
               this.mc.getConnection().send(new Pos(x, y, z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
            }
         }
      }
   }

   private void breachAttack(Player target) {
      if (target != null) {
         Vec3 playerPos = this.mc.player.position();

         for (int i = 0; i < 4; i++) {
            this.mc.getConnection().send(new StatusOnly(false, this.mc.player.horizontalCollision));
         }

         double[] heights = new double[]{20.0, 60.0, 120.0};

         for (double h : heights) {
            Vec3 upPos = new Vec3(target.getX(), target.getY() + h, target.getZ());
            this.DataPacketBA(upPos);
            this.DataPacketBA(new Vec3(target.getX(), target.getY(), target.getZ()));
            this.mc.gameMode.attack(this.mc.player, target);
            this.mc.player.swing(InteractionHand.MAIN_HAND);
            this.DataPacketBA(playerPos);
         }

         this.mc.player.setDeltaMovement(this.mc.player.getDeltaMovement().x, 0.1, this.mc.player.getDeltaMovement().z);
         this.mc.player.fallDistance = 0.0;
      }
   }

   private List<Vec3> predictCandidates(Player target, boolean circleFirst) {
      List<Vec3> out = new ArrayList<>(6);
      if (target != null && this.mc.level != null) {
         int wanted = Math.max(3, Math.min(6, (Integer)this.predictCount.get()));
         Vec3 cur = target.position();
         Vec3 xoVel = cur.subtract(new Vec3(target.xo, target.yo, target.zo));
         Vec3 deltaVel = target.getDeltaMovement();
         Vec3 velocity = xoVel.lengthSqr() >= deltaVel.lengthSqr() ? xoVel : deltaVel;
         if (velocity.lengthSqr() < 1.0E-7) {
            velocity = xoVel.lengthSqr() >= deltaVel.lengthSqr() ? deltaVel : xoVel;
         }

         double speed = velocity.length();
         if (speed > 64.0) {
            velocity = velocity.scale(64.0 / speed);
            speed = 64.0;
         }

         Vec3 previousVelocity = this.lastVelocity.put(target, velocity);
         Vec3 acceleration = previousVelocity == null ? Vec3.ZERO : velocity.subtract(previousVelocity);
         Vec3 forward = horizontal(target.getLookAngle());
         if (forward.lengthSqr() < 1.0E-7) {
            forward = horizontal(velocity);
         }

         if (forward.lengthSqr() < 1.0E-7) {
            forward = new Vec3(0.0, 0.0, 1.0);
         } else {
            forward = forward.normalize();
         }

         Vec3 right = new Vec3(-forward.z, 0.0, forward.x);
         double ptTicks = Math.max(1.0, Math.min(((Integer)this.pt.get()).intValue(), 10.0));
         int horizon = (int)Math.round(ptTicks);
         Vec3 core = this.linearPoint(target, cur, velocity, horizon);
         Vec3 legacy = this.predictScaled(target, cur, velocity);
         Vec3 accelPoint = this.resolve(target, cur, cur.add(velocity.scale(ptTicks)).add(acceleration.scale(0.5 * ptTicks * ptTicks)));
         double nearRadius = Math.max(0.8, Math.min(3.0, 0.9 + speed * 0.4));
         double farRadius = Math.max(nearRadius + 0.8, Math.min(6.0, nearRadius * 2.0));
         List<TpMace.ScoredPos> pool = new ArrayList<>(256);
         pool.add(new TpMace.ScoredPos(cur, 82.0));
         this.addTimedSeries(pool, this.linearSeries(target, cur, xoVel, 10), 112.0, 7.0, ptTicks);
         this.addTimedSeries(pool, this.linearSeries(target, cur, deltaVel, 10), 107.0, 7.0, ptTicks);
         this.addTimedSeries(pool, this.accelSeries(target, cur, velocity, acceleration, 10), 101.0, 7.5, ptTicks);
         this.addTimedSeries(pool, this.lookSeries(target, cur, target.getLookAngle(), speed, 10), 90.0, 7.0, ptTicks);
         this.addArcCandidates(pool, target, cur, velocity, speed, ptTicks, circleFirst ? 14.0 : 0.0);
         pool.add(new TpMace.ScoredPos(core, 96.0));
         pool.add(new TpMace.ScoredPos(accelPoint, 92.0));
         pool.add(new TpMace.ScoredPos(legacy, 62.0));
         this.addFlatSeries(pool, this.directionSeries(target, core, forward, nearRadius, farRadius), 46.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, forward.scale(-1.0), nearRadius, farRadius), 46.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, right, nearRadius, farRadius), 46.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, right.scale(-1.0), nearRadius, farRadius), 46.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, forward.add(right).normalize(), nearRadius, farRadius), 43.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, forward.add(right.scale(-1.0)).normalize(), nearRadius, farRadius), 43.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, forward.scale(-1.0).add(right).normalize(), nearRadius, farRadius), 43.0, 6.0);
         this.addFlatSeries(pool, this.directionSeries(target, core, forward.scale(-1.0).add(right.scale(-1.0)).normalize(), nearRadius, farRadius), 43.0, 6.0);
         this.addFlatSeries(pool, this.verticalDirectionSeries(target, core, 1.0), 41.0, 5.0);
         this.addFlatSeries(pool, this.verticalDirectionSeries(target, core, -1.0), 41.0, 5.0);
         this.addFlatSeries(pool, this.verticalSeries(target, cur, velocity, false), 32.0, 3.0);
         this.addFlatSeries(pool, this.directionSeries(target, legacy, forward, farRadius, farRadius * 1.5), 24.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, legacy, right, farRadius, farRadius * 1.5), 24.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, legacy, forward.scale(-1.0), farRadius, farRadius * 1.5), 22.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, legacy, right.scale(-1.0), farRadius, farRadius * 1.5), 22.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, cur, new Vec3(1.0, 0.0, 0.0), nearRadius, farRadius), 26.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, cur, new Vec3(-1.0, 0.0, 0.0), nearRadius, farRadius), 26.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, cur, new Vec3(0.0, 0.0, 1.0), nearRadius, farRadius), 26.0, 4.0);
         this.addFlatSeries(pool, this.directionSeries(target, cur, new Vec3(0.0, 0.0, -1.0), nearRadius, farRadius), 26.0, 4.0);
         this.addFlatSeries(pool, this.hitboxSeries(target, cur), 14.0, 2.0);
         pool.sort(Comparator.comparingDouble(TpMace.ScoredPos::score).reversed());

         for (TpMace.ScoredPos candidate : pool) {
            if (this.addCandidate(out, candidate.pos(), wanted) && out.size() >= wanted) {
               break;
            }
         }

         if (out.isEmpty()) {
            out.add(legacy);
         }

         return out;
      } else {
         return out;
      }
   }

   private void addTimedSeries(List<TpMace.ScoredPos> pool, List<Vec3> points, double base, double penalty, double centerTick) {
      for (int i = 0; i < points.size(); i++) {
         pool.add(new TpMace.ScoredPos(points.get(i), base - penalty * Math.abs(i + 1 - centerTick)));
      }
   }

   private void addFlatSeries(List<TpMace.ScoredPos> pool, List<Vec3> points, double base, double decay) {
      for (int i = 0; i < points.size(); i++) {
         pool.add(new TpMace.ScoredPos(points.get(i), base - decay * i));
      }
   }

   private Vec3 linearPoint(Player target, Vec3 cur, Vec3 velocity, int ticks) {
      Vec3 pos = cur;

      for (int t = 0; t < ticks; t++) {
         pos = pos.add(this.step(target, cur, pos, velocity));
      }

      return pos;
   }

   private List<Vec3> linearSeries(Player target, Vec3 cur, Vec3 velocity, int ticks) {
      List<Vec3> list = new ArrayList<>(ticks);
      if (velocity.lengthSqr() < 1.0E-7) {
         return list;
      }

      Vec3 pos = cur;

      for (int t = 0; t < ticks; t++) {
         Vec3 step = this.step(target, cur, pos, velocity);
         pos = pos.add(step);
         list.add(pos);
         if (step.lengthSqr() < 1.0E-7) {
            break;
         }
      }

      return list;
   }

   private Vec3 predictScaled(Player target, Vec3 cur, Vec3 velocity) {
      Vec3 pos = cur;
      Vec3 scaled = velocity.scale(3.0);

      for (int i = 0; i < this.pt.get(); i++) {
         Vec3 step = this.step(target, cur, pos, scaled);
         pos = pos.add(step);
         if (step.lengthSqr() < 1.0E-7) {
            break;
         }
      }

      return pos;
   }

   private List<Vec3> accelSeries(Player target, Vec3 cur, Vec3 velocity, Vec3 acceleration, int ticks) {
      List<Vec3> list = new ArrayList<>(ticks);
      if (velocity.lengthSqr() < 1.0E-7 && acceleration.lengthSqr() < 1.0E-9) {
         return list;
      }

      for (int t = 1; t <= ticks; t++) {
         Vec3 offset = velocity.scale(t).add(acceleration.scale(0.5 * t * t));
         if (offset.lengthSqr() > 4096.0) {
            break;
         }

         list.add(this.resolve(target, cur, cur.add(offset)));
      }

      return list;
   }

   private List<Vec3> lookSeries(Player target, Vec3 cur, Vec3 look, double speed, int ticks) {
      List<Vec3> list = new ArrayList<>(ticks);
      double step = Math.max(0.35, Math.min(speed, 10.0));

      for (int t = 1; t <= ticks; t++) {
         Vec3 raw = cur.add(look.x * step * t, look.y * step * t, look.z * step * t);
         list.add(this.resolve(target, cur, raw));
      }

      return list;
   }

   private void addArcCandidates(List<TpMace.ScoredPos> pool, Player target, Vec3 cur, Vec3 velocity, double speed, double centerTick, double bonus) {
      if (speed < 0.03) {
         this.hasAngle = false;
      } else {
         double angle = Math.atan2(velocity.z, velocity.x);
         if (!this.hasAngle) {
            this.lastAngle = angle;
            this.angularSpeed = 0.0;
            this.hasAngle = true;
         } else {
            double delta = angle - this.lastAngle;

            while (delta > Math.PI) {
               delta -= Math.PI * 2;
            }

            while (delta < -Math.PI) {
               delta += Math.PI * 2;
            }

            this.angularSpeed = Math.max(-1.0, Math.min(1.0, this.angularSpeed * 0.35 + delta * 0.65));
            this.lastAngle = angle;
            double omega = this.angularSpeed;
            double base = (Math.abs(omega) >= 0.08 ? 104.0 : 60.0) + bonus;
            int maxTicks = Math.max((Integer)this.circleTicks.get(), 10);

            for (int t = 1; t <= maxTicks; t++) {
               double theta = omega * t;
               double chord = Math.abs(omega) < 1.0E-4 ? speed * t : 2.0 * (speed / Math.abs(omega)) * Math.sin(Math.abs(theta) * 0.5);
               double chordAngle = angle + theta * 0.5;
               Vec3 raw = new Vec3(cur.x + Math.cos(chordAngle) * chord, cur.y, cur.z + Math.sin(chordAngle) * chord);
               double score = base - 6.5 * Math.abs(t - centerTick);
               pool.add(new TpMace.ScoredPos(this.resolve(target, cur, raw), score));
            }
         }
      }
   }

   private List<Vec3> directionSeries(Player target, Vec3 center, Vec3 dir, double near, double far) {
      List<Vec3> list = new ArrayList<>(2);
      Vec3 unit = dir.lengthSqr() < 1.0E-7 ? dir : dir.normalize();
      list.add(this.resolve(target, center, center.add(unit.scale(near))));
      list.add(this.resolve(target, center, center.add(unit.scale(far))));
      return list;
   }

   private List<Vec3> verticalDirectionSeries(Player target, Vec3 center, double sign) {
      List<Vec3> list = new ArrayList<>(6);
      double[] offsets = new double[]{1.0, 2.0, 3.0, 5.0, 8.0, 12.0};

      for (double offset : offsets) {
         list.add(this.resolve(target, center, center.add(0.0, sign * offset, 0.0)));
      }

      return list;
   }

   private List<Vec3> verticalSeries(Player target, Vec3 center, Vec3 velocity, boolean upFirst) {
      List<Vec3> list = new ArrayList<>(14);
      double[] offsets = upFirst
         ? new double[]{1.0, -1.0, 2.0, -2.0, 3.0, -3.0, 5.0, -5.0, 8.0, -8.0}
         : new double[]{-1.0, 1.0, -2.0, 2.0, -3.0, 3.0, -5.0, 5.0, -8.0, 8.0};

      for (double offset : offsets) {
         list.add(this.resolve(target, center, center.add(0.0, offset, 0.0)));
      }

      for (int t = 1; t <= 6; t++) {
         double dy = velocity.y * t - 0.08 * t * t;
         list.add(this.resolve(target, center, center.add(0.0, dy, 0.0)));
      }

      return list;
   }

   private List<Vec3> hitboxSeries(Player target, Vec3 cur) {
      List<Vec3> list = new ArrayList<>(12);
      double half = Math.max(0.2, target.getBbWidth() * 0.5);
      double[] heights = new double[]{0.0, 0.6, 1.2};

      for (double height : heights) {
         list.add(new Vec3(cur.x + half, cur.y + height, cur.z));
         list.add(new Vec3(cur.x - half, cur.y + height, cur.z));
         list.add(new Vec3(cur.x, cur.y + height, cur.z + half));
         list.add(new Vec3(cur.x, cur.y + height, cur.z - half));
      }

      return list;
   }

   private Vec3 step(Player target, Vec3 origin, Vec3 from, Vec3 movement) {
      return Entity.collideBoundingBox(target, movement, target.getBoundingBox().move(from.subtract(origin)), target.level(), List.of());
   }

   private Vec3 resolve(Player target, Vec3 from, Vec3 to) {
      return from.distanceToSqr(to) < 1.0E-7 ? to : from.add(this.step(target, target.position(), from, to.subtract(from)));
   }

   private static Vec3 horizontal(Vec3 vec) {
      return new Vec3(vec.x, 0.0, vec.z);
   }

   private boolean addCandidate(List<Vec3> positions, Vec3 candidate, int cap) {
      if (candidate != null && positions.size() < cap) {
         for (Vec3 existing : positions) {
            if (existing.distanceToSqr(candidate) < 0.0625) {
               return false;
            }
         }

         if (!this.isLegal(candidate)) {
            return false;
         }

         positions.add(candidate);
         return true;
      } else {
         return false;
      }
   }

   private boolean isLegal(Vec3 pos) {
      if (this.mc.player == null || this.mc.level == null || !this.mc.level.isLoaded(BlockPos.containing(pos))) {
         return false;
      }

      if (!this.isSafe(pos)) {
         return false;
      }

      Vec3 playerPos = this.mc.player.position();
      AABB box = this.mc.player.getBoundingBox().move(pos.x - playerPos.x, pos.y - playerPos.y, pos.z - playerPos.z);
      return this.mc.level.noCollision(this.mc.player, box);
   }

   private boolean isSafe(Vec3 pos) {
      if (this.mc.level == null) {
         return false;
      } else {
         BlockPos lower = BlockPos.containing(pos.x, pos.y, pos.z);
         BlockPos upper = BlockPos.containing(pos.x, pos.y + 1.2, pos.z);
         if (!this.mc.level.getFluidState(lower).isEmpty()) {
            return false;
         } else if (this.mc.level.getBlockState(lower).isSolidRender()) {
            return false;
         } else {
            return !this.mc.level.getFluidState(upper).isEmpty() ? false : !this.mc.level.getBlockState(upper).isSolidRender();
         }
      }
   }

   @EventHandler
   private void onTick(Post event) {
      if (this.isActive() && this.mc.player != null && this.mc.getConnection() != null) {
         Vec3 xyznow = this.mc.player.position();
         this.PlayerS();
         if (this.zhongzhuan != null) {
            this.hasArmor(this.zhongzhuan);
            FindItemResult mace = InvUtils.find(new Item[]{Items.MACE});
            if (mace.found()) {
               InvUtils.swap(mace.slot(), true);
               if ((Boolean)this.LBLTMode.get()) {
                  this.dataPacket();
                  Vec3 originalPosLB = this.mc.player.position();
                  if (!this.isArmor) {
                     for (int i = 0; i < 3; i++) {
                        double currentFall = 20 + i * 10;
                        this.mc.player.setPos(this.mc.player.getX(), this.mc.player.getY() + currentFall, this.mc.player.getZ());
                        this.mc
                           .getConnection()
                           .send(
                              new Pos(
                                 this.mc.player.getX(),
                                 this.mc.player.getY(),
                                 this.mc.player.getZ(),
                                 this.mc.player.onGround(),
                                 this.mc.player.horizontalCollision
                              )
                           );
                        this.mc.player.setPos(originalPosLB.x, originalPosLB.y, originalPosLB.z);
                        this.mc
                           .getConnection()
                           .send(new Pos(originalPosLB.x, originalPosLB.y, originalPosLB.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
                        this.mc.gameMode.attack(this.mc.player, this.zhongzhuan);
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                     }

                     this.mc.player.setPos(xyznow.x, xyznow.y, xyznow.z);
                     this.mc.getConnection().send(new Pos(xyznow.x, xyznow.y, xyznow.z, this.mc.player.onGround(), this.mc.player.horizontalCollision));
                     this.zhongzhuan = null;
                  } else {
                     this.breachAttack(this.zhongzhuan);
                     this.zhongzhuan = null;
                  }
               } else if ((Boolean)this.predictcir.get()) {
                  List<Vec3> targetPositions = this.predictCandidates(this.zhongzhuan, true);
                  if (targetPositions.isEmpty()) {
                     this.zhongzhuan = null;
                     return;
                  }

                  for (Vec3 targetPos : targetPositions) {
                     this.mc.player.setPos(targetPos.x, targetPos.y + 1.2, targetPos.z);

                     for (int i = 0; i < this.fallfor.get(); i++) {
                        double currentFall = (Double)this.onefall.get() + i * (Double)this.fallpp.get();
                        this.sendMove(new Vec3(this.mc.player.getX(), this.mc.player.getY() + currentFall, this.mc.player.getZ()));
                        this.sendMove(targetPos);
                        this.mc.player.setPos(targetPos);
                        this.mc.gameMode.attack(this.mc.player, this.zhongzhuan);
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                     }
                  }

                  this.mc.player.setPos(xyznow.x, xyznow.y, xyznow.z);
                  this.zhongzhuan = null;
               } else if ((Boolean)this.predict.get()) {
                  List<Vec3> targetPositions = this.predictCandidates(this.zhongzhuan, false);
                  if (targetPositions.isEmpty()) {
                     this.zhongzhuan = null;
                     return;
                  }

                  for (Vec3 targetPos : targetPositions) {
                     this.mc.player.setPos(targetPos.x, targetPos.y + 1.2, targetPos.z);

                     for (int i = 0; i < this.fallfor.get(); i++) {
                        double currentFall = (Double)this.onefall.get() + i * (Double)this.fallpp.get();
                        this.sendMove(new Vec3(this.mc.player.getX(), this.mc.player.getY() + currentFall, this.mc.player.getZ()));
                        this.sendMove(targetPos);
                        this.mc.player.setPos(targetPos);
                        this.mc.gameMode.attack(this.mc.player, this.zhongzhuan);
                        this.mc.player.swing(InteractionHand.MAIN_HAND);
                     }
                  }

                  this.mc.player.setPos(xyznow.x, xyznow.y, xyznow.z);
                  this.zhongzhuan = null;
               } else {
                  Vec3 enemyVelocity = this.zhongzhuan.getDeltaMovement();
                  Vec3 targetPos = this.zhongzhuan.position();
                  Vec3 originalPos = this.mc.player.position();
                  this.mc.player.setPos(targetPos.x, targetPos.y + 1.2, targetPos.z);

                  for (int i = 0; i < this.fallfor.get(); i++) {
                     double currentFall = (Double)this.onefall.get() + i * (Double)this.fallpp.get();
                     this.sendMove(new Vec3(this.mc.player.getX(), this.mc.player.getY() + currentFall, this.mc.player.getZ()));
                     this.sendMove(targetPos);
                     this.mc.player.setPos(targetPos);
                     this.mc.gameMode.attack(this.mc.player, this.zhongzhuan);
                     this.mc.player.swing(InteractionHand.MAIN_HAND);
                  }

                  this.mc.player.setPos(xyznow.x, xyznow.y, xyznow.z);
                  this.zhongzhuan = null;
               }
            }
         }
      }
   }

   private record ScoredPos(Vec3 pos, double score) {
   }
}
