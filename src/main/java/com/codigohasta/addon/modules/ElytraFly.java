package com.codigohasta.addon.modules;

import com.codigohasta.addon.AddonTemplate;
import meteordevelopment.meteorclient.events.entity.player.PlayerMoveEvent;
import meteordevelopment.meteorclient.events.packets.PacketEvent;
import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.*;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.meteorclient.utils.misc.Keybind;
import meteordevelopment.meteorclient.utils.player.FindItemResult;
import meteordevelopment.meteorclient.utils.player.InvUtils;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.projectile.FireworkRocketEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.s2c.play.PlayerPositionLookS2CPacket;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import com.codigohasta.addon.mixin.InventoryAccessor;

public class ElytraFly extends Module {

    public enum Mode {
        Control, Boost, Bounce, Freeze, None, Rotation, Pitch
    }

    private final SettingGroup sgGeneral = settings.getDefaultGroup();
    private final SettingGroup sgControl = settings.createGroup("Control");
    private final SettingGroup sgPitch = settings.createGroup("Pitch");
    private final SettingGroup sgBounce = settings.createGroup("Bounce");
    private final SettingGroup sgFirework = settings.createGroup("Firework");

    // General Settings
    public final Setting<Mode> mode = sgGeneral.add(new EnumSetting.Builder<Mode>().name("mode").description("Flight mode.").defaultValue(Mode.Control).build());
    public final Setting<Boolean> infiniteDura = sgGeneral.add(new BoolSetting.Builder().name("infinite-dura").description("Prevents elytra from taking damage.").defaultValue(false).build());
    public final Setting<Boolean> packet = sgGeneral.add(new BoolSetting.Builder().name("packet-mode").description("Uses packets to fly without equipping elytra (Chestplate fly).").defaultValue(true).build());
    private final Setting<Integer> packetDelay = sgGeneral.add(new IntSetting.Builder().name("packet-delay").description("Delay for packet mode.").defaultValue(0).min(0).max(20).visible(packet::get).build());
    private final Setting<Boolean> setFlag = sgGeneral.add(new BoolSetting.Builder().name("set-flag").description("Forces client-side flight flag.").defaultValue(false).visible(() -> mode.get() != Mode.Bounce).build());
    private final Setting<Boolean> autoStop = sgGeneral.add(new BoolSetting.Builder().name("auto-stop").description("Stops flying on unloaded chunks.").defaultValue(true).build());
    private final Setting<Boolean> instantFly = sgGeneral.add(new BoolSetting.Builder().name("instant-fly").description("Starts flying automatically when falling.").defaultValue(true).visible(() -> mode.get() != Mode.Bounce).build());
    private final Setting<Double> timeout = sgGeneral.add(new DoubleSetting.Builder().name("timeout").description("Instant fly timeout.").defaultValue(0.0).min(0.1).max(1.0).visible(() -> mode.get() != Mode.Bounce).build());
    public final Setting<Boolean> releaseSneak = sgGeneral.add(new BoolSetting.Builder().name("release-sneak").description("Releases shift when module disables.").defaultValue(false).build());

    // Control Settings
    public final Setting<Double> upPitch = sgControl.add(new DoubleSetting.Builder().name("up-pitch").defaultValue(0.0).min(0.0).max(90.0).visible(() -> mode.get() == Mode.Control).build());
    public final Setting<Double> upFactor = sgControl.add(new DoubleSetting.Builder().name("up-factor").defaultValue(1.0).min(0.0).max(10.0).visible(() -> mode.get() == Mode.Control).build());
    public final Setting<Double> downFactor = sgControl.add(new DoubleSetting.Builder().name("fall-speed").defaultValue(1.0).min(0.0).max(10.0).visible(() -> mode.get() == Mode.Control).build());
    public final Setting<Double> speed = sgControl.add(new DoubleSetting.Builder().name("speed").defaultValue(1.0).min(0.1).max(10.0).visible(() -> mode.get() == Mode.Control).build());
    public final Setting<Boolean> speedLimit = sgControl.add(new BoolSetting.Builder().name("speed-limit").defaultValue(true).visible(() -> mode.get() == Mode.Control).build());
    public final Setting<Double> maxSpeed = sgControl.add(new DoubleSetting.Builder().name("max-speed").defaultValue(2.5).min(0.1).max(10.0).visible(() -> mode.get() == Mode.Control && speedLimit.get()).build());
    public final Setting<Boolean> noDrag = sgControl.add(new BoolSetting.Builder().name("no-drag").defaultValue(false).visible(() -> mode.get() == Mode.Control).build());
    private final Setting<Double> sneakDownSpeed = sgControl.add(new DoubleSetting.Builder().name("down-speed").defaultValue(1.0).min(0.1).max(10.0).visible(() -> mode.get() == Mode.Control).build());
    private final Setting<Double> boost = sgControl.add(new DoubleSetting.Builder().name("boost").defaultValue(1.0).min(0.1).max(4.0).visible(() -> mode.get() == Mode.Boost).build());
    private final Setting<Boolean> freeze = sgControl.add(new BoolSetting.Builder().name("freeze").defaultValue(false).visible(() -> mode.get() == Mode.Rotation).build());
    private final Setting<Boolean> motionStop = sgControl.add(new BoolSetting.Builder().name("motion-stop").defaultValue(false).visible(() -> mode.get() == Mode.Rotation).build());

    // Pitch Settings (Grim Bypass)
    private final Setting<Double> infiniteMaxSpeed = sgPitch.add(new DoubleSetting.Builder().name("infinite-max-speed").defaultValue(150.0).min(50.0).max(170.0).visible(() -> mode.get() == Mode.Pitch).build());
    private final Setting<Double> infiniteMinSpeed = sgPitch.add(new DoubleSetting.Builder().name("infinite-min-speed").defaultValue(25.0).min(10.0).max(70.0).visible(() -> mode.get() == Mode.Pitch).build());
    private final Setting<Double> infiniteMaxHeight = sgPitch.add(new DoubleSetting.Builder().name("infinite-max-height").defaultValue(200.0).min(-50.0).max(360.0).visible(() -> mode.get() == Mode.Pitch).build());

    // Bounce Settings
    public final Setting<Boolean> autoJump = sgBounce.add(new BoolSetting.Builder().name("auto-jump").defaultValue(true).visible(() -> mode.get() == Mode.Bounce).build());
    private final Setting<Boolean> sprint = sgBounce.add(new BoolSetting.Builder().name("sprint").defaultValue(true).visible(() -> mode.get() == Mode.Bounce).build());
    private final Setting<Double> pitch = sgBounce.add(new DoubleSetting.Builder().name("pitch").defaultValue(88.0).min(-90.0).max(90.0).visible(() -> mode.get() == Mode.Bounce).build());

    // Firework Settings
    public final Setting<Boolean> firework = sgFirework.add(new BoolSetting.Builder().name("firework").description("Auto uses fireworks.").defaultValue(false).build());
    public final Setting<Keybind> fireWorkBind = sgFirework.add(new KeybindSetting.Builder().name("firework-bind").defaultValue(Keybind.none()).action(this::manualFirework).build());
    public final Setting<Boolean> inventory = sgFirework.add(new BoolSetting.Builder().name("inventory-swap").description("Pulls firework from inventory silently.").defaultValue(true).visible(firework::get).build());
    public final Setting<Boolean> onlyOne = sgFirework.add(new BoolSetting.Builder().name("only-one").description("Limits to one rocket entity.").defaultValue(true).visible(firework::get).build());
    private final Setting<Boolean> usingPause = sgFirework.add(new BoolSetting.Builder().name("using-pause").description("Pauses while using items.").defaultValue(true).visible(firework::get).build());
    private final Setting<Boolean> checkSpeed = sgFirework.add(new BoolSetting.Builder().name("check-speed").defaultValue(false).visible(() -> mode.get() != Mode.Bounce).build());
    public final Setting<Double> minSpeed = sgFirework.add(new DoubleSetting.Builder().name("min-speed").defaultValue(70.0).min(0.1).max(200.0).visible(() -> mode.get() != Mode.Bounce).build());
    private final Setting<Integer> delay = sgFirework.add(new IntSetting.Builder().name("delay").defaultValue(1000).min(0).max(20000).visible(() -> mode.get() != Mode.Bounce).build());

    // State Variables
    private long fireworkTimer = 0;
    private long instantFlyTimer = 0;
    private boolean hasElytra = false;
    private boolean flying = false;
    private int packetDelayInt = 0;
    private boolean down;
    private float infinitePitch;
    private float lastInfinitePitch;

    public ElytraFly() {
        super(AddonTemplate.CATEGORY, "甲飞鞘翅", "甲飞，没有绕过反作弊，娱乐功能");
    }

    @Override
    public void onActivate() {
        if (mc.player == null) return;
        hasElytra = false;
    }

    @Override
    public void onDeactivate() {
        if (mc.player != null && releaseSneak.get()) {
            mc.options.sneakKey.setPressed(false);
        }
    }

    private void manualFirework() {
        if (mc.player != null && (!mc.player.isUsingItem() || !usingPause.get()) && isFallFlying()) {
            if (System.currentTimeMillis() - fireworkTimer > delay.get()) {
                useFirework();
            }
        }
    }

    private void useFirework() {
        if (!inventory.get() || mc.currentScreen != null) return;

        if (onlyOne.get()) {
            for (net.minecraft.entity.Entity entity : mc.world.getEntities()) {
                if (entity instanceof FireworkRocketEntity fireworkRocket && fireworkRocket.distanceTo(mc.player) < 10) {
                    return;
                }
            }
        }

        fireworkTimer = System.currentTimeMillis();

        if (mc.player.getMainHandStack().isOf(Items.FIREWORK_ROCKET)) {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        } else {
            FindItemResult fireworkResult = InvUtils.find(Items.FIREWORK_ROCKET);
            if (fireworkResult.found()) {
                int oldSlot = ((InventoryAccessor) mc.player.getInventory()).getSelectedSlot();
                if (fireworkResult.isHotbar()) {
                    InvUtils.swap(fireworkResult.slot(), true);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    InvUtils.swapBack();
                } else {
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fireworkResult.slot(), oldSlot, SlotActionType.SWAP, mc.player);
                    mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
                    mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, fireworkResult.slot(), oldSlot, SlotActionType.SWAP, mc.player);
                }
            }
        }
    }

   
    public static boolean checkConditions(ClientPlayerEntity player) {
        ItemStack itemStack = player.getEquippedStack(EquipmentSlot.CHEST);
        return !player.getAbilities().flying && !player.hasVehicle() && !player.isClimbing()
            && itemStack.isOf(Items.ELYTRA) && (itemStack.getMaxDamage() - itemStack.getDamage() > 1);
    }

    private boolean ignoreGround(ClientPlayerEntity player) {
        if (!player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION)) {
            if (checkConditions(player)) {
                player.startGliding();
                return true;
            }
        }
        return false;
    }

    private void boostFunc() {
        if (hasElytra && isFallFlying()) {
            float yaw = (float) Math.toRadians(mc.player.getYaw());
            
            if (mc.player.input.playerInput.forward()) {
                mc.player.addVelocity(-MathHelper.sin(yaw) * boost.get() / 10.0, 0.0, MathHelper.cos(yaw) * boost.get() / 10.0);
            }
        }
    }

    @EventHandler
    private void onTick(TickEvent.Pre event) {
        if (mc.player == null || mc.world == null) return;

        getInfinitePitch();
        flying = false;

   
        if (packet.get()) {
            hasElytra = InvUtils.find(Items.ELYTRA).found();
        } else {
            hasElytra = mc.player.getEquippedStack(EquipmentSlot.CHEST).isOf(Items.ELYTRA);

          
            if (infiniteDura.get() && !mc.player.isOnGround() && hasElytra) {
                flying = true;
                
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
                mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, 0, SlotActionType.PICKUP, mc.player);
                mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                mc.player.startGliding();
            }

            if (mode.get() == Mode.Bounce) return;
        }

        double x = mc.player.getX() - mc.player.lastRenderX;
        double y = mc.player.getY() -mc.player.lastRenderY;
        double z = mc.player.getZ() - mc.player.lastRenderZ;
        double dist = Math.sqrt(x * x + z * z + y * y) / 1000.0;
        double speedVal = dist / 1.388888888888889E-5;

        if (mode.get() == Mode.Boost) boostFunc();

       
        boolean isMoving = mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward() ||
                           mc.player.input.playerInput.left() || mc.player.input.playerInput.right();

       
        if (packet.get()) {
            if (!mc.player.isOnGround()) {
                packetDelayInt++;
                if (packetDelayInt > packetDelay.get()) {
                    FindItemResult elytra = InvUtils.find(Items.ELYTRA);
                    if (elytra.found()) {
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, elytra.slot(), SlotActionType.SWAP, mc.player);
                        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                        mc.player.startGliding();

                        if ((!checkSpeed.get() || speedVal <= minSpeed.get()) && firework.get()
                            && (System.currentTimeMillis() - fireworkTimer > delay.get())
                            && (isMoving || (mode.get() == Mode.Rotation && mc.options.jumpKey.isPressed()))
                            && (!mc.player.isUsingItem() || !usingPause.get()) && isFallFlying()) {
                            useFirework();
                        }
                        
                        mc.interactionManager.clickSlot(mc.player.currentScreenHandler.syncId, 6, elytra.slot(), SlotActionType.SWAP, mc.player);
                        packetDelayInt = 0;
                    }
                }
            }
        } else {
            
            if ((!checkSpeed.get() || speedVal <= minSpeed.get()) && firework.get()
                && (System.currentTimeMillis() - fireworkTimer > delay.get())
                && (isMoving || (mode.get() == Mode.Rotation && mc.options.jumpKey.isPressed()))
                && (!mc.player.isUsingItem() || !usingPause.get()) && isFallFlying()) {
                useFirework();
            }

           
            if (!isFallFlying() && hasElytra) {
                if (!mc.player.isOnGround() && instantFly.get() && mc.player.getVelocity().y < 0.0 && !infiniteDura.get()) {
                    if (System.currentTimeMillis() - instantFlyTimer < (long) (1000.0 * timeout.get())) return;
                    instantFlyTimer = System.currentTimeMillis();
                    mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));
                    mc.player.startGliding();
                }
            }
        }

        
        if (isFallFlying()) {
            if (mode.get() == Mode.Rotation) {
                float rotPitch = -1.9f;
                if (mc.options.jumpKey.isPressed()) rotPitch = -45f;
                else if (mc.options.sneakKey.isPressed()) rotPitch = 45f;
                mc.player.setPitch(rotPitch);
            } else if (mode.get() == Mode.Pitch) {
                mc.player.setPitch(infinitePitch);
            } else if (mode.get() == Mode.Bounce) {
                mc.player.setPitch(pitch.get().floatValue());
                if (autoJump.get()) mc.options.jumpKey.setPressed(true);
            }
        }
    }

    @EventHandler
    private void onPlayerMove(PlayerMoveEvent event) {
        if (mc.player == null) return;

        if (autoStop.get() && isFallFlying()) {
            int chunkX = (int) (mc.player.getX() / 16.0);
            int chunkZ = (int) (mc.player.getZ() / 16.0);
            if (!mc.world.getChunkManager().isChunkLoaded(chunkX, chunkZ)) {
                ((meteordevelopment.meteorclient.mixininterface.IVec3d) event.movement).meteor$set(0.0, 0.0, 0.0);
                return;
            }
        }

        if (hasElytra && isFallFlying()) {
            boolean isMoving = mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward() ||
                               mc.player.input.playerInput.left() || mc.player.input.playerInput.right();

            if ((mode.get() == Mode.Freeze || (mode.get() == Mode.Rotation && freeze.get()))
                && !isMoving && !mc.options.jumpKey.isPressed() && !mc.options.sneakKey.isPressed()) {
                ((meteordevelopment.meteorclient.mixininterface.IVec3d) event.movement).meteor$set(0.0, 0.0, 0.0);
                return;
            }

           
            if (mode.get() == Mode.Control) {
                double velX = mc.player.getVelocity().x;
                double velY = mc.player.getVelocity().y;
                double velZ = mc.player.getVelocity().z;

                if (firework.get()) {
                    if (mc.options.sneakKey.isPressed() && mc.options.jumpKey.isPressed()) velY = 0.0;
                    else if (mc.options.sneakKey.isPressed()) velY = -sneakDownSpeed.get();
                    else if (mc.options.jumpKey.isPressed()) velY = upFactor.get();
                    else velY = -3.0E-11 * downFactor.get();

                    double[] dir = directionSpeed(speed.get());
                    velX = dir[0]; velZ = dir[1];
                } else {
                    Vec3d lookVec = getRotationVector(-upPitch.get().floatValue(), mc.player.getYaw());
                    double lookDist = Math.sqrt(lookVec.x * lookVec.x + lookVec.z * lookVec.z);
                    double motionDist = Math.sqrt(velX * velX + velZ * velZ);

                    if (mc.options.sneakKey.isPressed()) velY = -sneakDownSpeed.get();
                    else if (!mc.options.jumpKey.isPressed()) velY = -3.0E-11 * downFactor.get();

                    if (mc.options.jumpKey.isPressed()) {
                        if (motionDist > upFactor.get() / 10.0) {
                            double rawUpSpeed = motionDist * 0.01325;
                            velY += rawUpSpeed * 3.2;
                            velX -= lookVec.x * rawUpSpeed / lookDist;
                            velZ -= lookVec.z * rawUpSpeed / lookDist;
                        } else {
                            double[] dir = directionSpeed(speed.get());
                            velX = dir[0]; velZ = dir[1];
                        }
                    }

                    if (lookDist > 0.0) {
                        velX += (lookVec.x / lookDist * motionDist - velX) * 0.1;
                        velZ += (lookVec.z / lookDist * motionDist - velZ) * 0.1;
                    }

                    if (!mc.options.jumpKey.isPressed()) {
                        double[] dir = directionSpeed(speed.get());
                        velX = dir[0]; velZ = dir[1];
                    }

                    if (!noDrag.get()) {
                        velY *= 0.99;
                        velX *= 0.98;
                        velZ *= 0.99;
                    }

                    double finalDist = Math.sqrt(velX * velX + velZ * velZ);
                    if (speedLimit.get() && finalDist > maxSpeed.get()) {
                        velX = velX * maxSpeed.get() / finalDist;
                        velZ = velZ * maxSpeed.get() / finalDist;
                    }
                }

                ((meteordevelopment.meteorclient.mixininterface.IVec3d) event.movement).meteor$set(velX, velY, velZ);
                mc.player.setVelocity(velX, velY, velZ); 
            }
        }
    }

    @EventHandler
    private void onPacketSend(PacketEvent.Send event) {
        if (mc.player == null) return;
        if (mode.get() == Mode.Bounce && hasElytra && event.packet instanceof ClientCommandC2SPacket packet) {
           
            if (packet.getMode() == ClientCommandC2SPacket.Mode.START_FALL_FLYING && !sprint.get()) {
                mc.player.setSprinting(true);
            }
        }
    }

    @EventHandler
    private void onPacketReceive(PacketEvent.Receive event) {
        if (mc.player == null) return;
        if (mode.get() == Mode.Bounce && hasElytra && event.packet instanceof PlayerPositionLookS2CPacket) {
            mc.player.stopGliding(); 
        }
    }

    private void getInfinitePitch() {
        lastInfinitePitch = infinitePitch;
        double speedVal = Math.hypot(mc.player.getX() - mc.player.lastRenderX, mc.player.getZ() - mc.player.lastRenderZ);
        if (mc.player.getY() < infiniteMaxHeight.get()) {
            if (speedVal * 72.0 < infiniteMinSpeed.get() && !down) down = true;
            if (speedVal * 72.0 > infiniteMaxSpeed.get() && down) down = false;
        } else {
            down = true;
        }
        infinitePitch += down ? 3.0f : -3.0f;
        infinitePitch = MathHelper.clamp(infinitePitch, -40.0f, 40.0f);
    }

    public boolean isFallFlying() {
        return mc.player.isGliding() || (packet.get() && hasElytra && !mc.player.isOnGround()) || flying;

    }

    private Vec3d getRotationVector(float pitch, float yaw) {
        float f = pitch * (float) (Math.PI / 180.0);
        float g = -yaw * (float) (Math.PI / 180.0);
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d(i * j, -k, h * j);
    }

    private double[] directionSpeed(double speedVal) {
        
        float forward = mc.player.input.playerInput.forward() ? 1.0f : (mc.player.input.playerInput.backward() ? -1.0f : 0.0f);
        float side = mc.player.input.playerInput.left() ? 1.0f : (mc.player.input.playerInput.right() ? -1.0f : 0.0f);
        float yaw = mc.player.getYaw();

        if (forward == 0.0f && side == 0.0f) return new double[]{0.0, 0.0};

        if (forward != 0.0f) {
            if (side > 0.0f) yaw += (forward > 0.0f ? -45 : 45);
            else if (side < 0.0f) yaw += (forward > 0.0f ? 45 : -45);
            side = 0.0f;
            if (forward > 0.0f) forward = 1.0f;
            else if (forward < 0.0f) forward = -1.0f;
        }

        double sin = Math.sin(Math.toRadians(yaw + 90.0f));
        double cos = Math.cos(Math.toRadians(yaw + 90.0f));
        double posX = forward * speedVal * cos + side * speedVal * sin;
        double posZ = forward * speedVal * sin - side * speedVal * cos;
        return new double
        []{posX, posZ};
    }
@EventHandler(priority = -9999) // 确保在最后执行，覆盖所有其他模块的修改
    private void onUpdateRotation(meteordevelopment.meteorclient.events.entity.player.SendMovementPacketsEvent.Pre event) {
        if (!isFallFlying()) return;

        float targetYaw = mc.player.getYaw();
        float targetPitch = mc.player.getPitch();

        if (mode.get() == Mode.Rotation) {
            
            boolean isMoving = mc.player.input.playerInput.forward() || mc.player.input.playerInput.backward() ||
                               mc.player.input.playerInput.left() || mc.player.input.playerInput.right();
            
            if (isMoving) {
                if (mc.options.jumpKey.isPressed()) targetPitch = -45.0f;
                else if (mc.options.sneakKey.isPressed()) targetPitch = 45.0f;
                else targetPitch = -1.9f;
            } else {
                if (mc.options.jumpKey.isPressed()) targetPitch = -89.0f;
                else if (mc.options.sneakKey.isPressed()) targetPitch = 89.0f;
            }
            
            
            targetYaw = getSprintYaw(mc.player.getYaw());
            
        } else if (mode.get() == Mode.Pitch) {
         
            targetPitch = infinitePitch;
        } else if (mode.get() == Mode.Bounce) {
        
            targetPitch = pitch.get().floatValue();
        }

       
        mc.player.setYaw(targetYaw);
        mc.player.setPitch(targetPitch);
    }

    
    private float getSprintYaw(float yaw) {
        float moveForward = mc.player.input.playerInput.forward() ? 1 : (mc.player.input.playerInput.backward() ? -1 : 0);
        float moveStrafe = mc.player.input.playerInput.left() ? 1 : (mc.player.input.playerInput.right() ? -1 : 0);

        if (moveForward != 0) {
            if (moveStrafe > 0) yaw += (moveForward > 0 ? -45 : 45);
            else if (moveStrafe < 0) yaw += (moveForward > 0 ? 45 : -45);
            if (moveForward < 0) yaw += 180;
        } else if (moveStrafe != 0) {
            yaw += (moveStrafe > 0 ? -90 : 90);
        }
        return yaw;
    }

    
    @EventHandler
    private void onTickPost(TickEvent.Post event) {
        if (mc.player == null || mode.get() != Mode.Bounce) return;

        
        try {
            
            if (autoJump.get() && hasElytra) {
                mc.options.jumpKey.setPressed(true);
            }
        } catch (Exception ignored) {}

        if (checkConditions(mc.player)) {
            if (sprint.get()) {
                mc.player.setSprinting(true);
            } else {
                mc.player.setSprinting(mc.player.isOnGround());
            }
        }
    }

    
    private void processAutoFirework() {
        if (mc.world == null || mc.player == null) return;

       
        double distX = mc.player.getX() - mc.player.lastRenderX;
        double distZ = mc.player.getZ() - mc.player.lastRenderZ;
        double speedMetric = Math.sqrt(distX * distX + distZ * distZ) * 72.0; 

        if (checkSpeed.get() && speedMetric > minSpeed.get()) return;

        if (System.currentTimeMillis() - fireworkTimer < delay.get()) return;

      
        if (usingPause.get() && mc.player.isUsingItem()) return;

       
        useFirework();
    }

    
    public Vec3d getRotationVec(float tickDelta) {
        float p = -upPitch.get().floatValue();
        float y = mc.player.getYaw(tickDelta);
        float f = p * 0.017453292F;
        float g = -y * 0.017453292F;
        float h = MathHelper.cos(g);
        float i = MathHelper.sin(g);
        float j = MathHelper.cos(f);
        float k = MathHelper.sin(f);
        return new Vec3d((i * j), (-k), (h * j));
    }

    
    private void doPacketFlyInteractions() {
        FindItemResult elytra = InvUtils.find(Items.ELYTRA);
        if (!elytra.found()) return;

        int syncId = mc.player.currentScreenHandler.syncId; 

   
        mc.interactionManager.clickSlot(syncId, 6, elytra.slot(), SlotActionType.SWAP, mc.player);
     
        mc.getNetworkHandler().sendPacket(new ClientCommandC2SPacket(mc.player, ClientCommandC2SPacket.Mode.START_FALL_FLYING));

        if (setFlag.get()) {
            mc.player.startGliding();
        }

        
        mc.interactionManager.clickSlot(syncId, 6, elytra.slot(), SlotActionType.SWAP, mc.player);
    }

   
    @Override
    public String getInfoString() {
        return mode.get().name();
    }
}