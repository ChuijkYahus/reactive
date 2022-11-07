package com.hyperlynx.reactive.alchemy;

import com.hyperlynx.reactive.Registration;
import com.hyperlynx.reactive.be.CrucibleBlockEntity;
import com.hyperlynx.reactive.items.CrystalIronItem;
import com.hyperlynx.reactive.items.WarpBottleItem;
import com.hyperlynx.reactive.util.ConfigMan;
import com.hyperlynx.reactive.util.Helper;
import com.hyperlynx.reactive.util.WorldSpecificValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.StructureTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.EyeOfEnder;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CandleBlock;
import net.minecraft.world.level.block.MossBlock;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

// It's SpecialCaseMan, to the rescue once again!
// Handles various circumstances that go beyond the normal logic of the mod.
public class SpecialCaseMan {

    public static void checkDissolveSpecialCases(CrucibleBlockEntity c, ItemEntity e){
        if(e.getItem().is(Tags.Items.ENDER_PEARLS))
            enderPearlDissolve(Objects.requireNonNull(c.getLevel()), c.getBlockPos(), e, c);
        else if(e.getItem().is(Tags.Items.GUNPOWDER) && c.getPowerLevel(Powers.BLAZE_POWER.get()) > 10)
            explodeGunpowderDueToBlaze(Objects.requireNonNull(c.getLevel()), c.getBlockPos(), e);
        else if(e.getItem().is(Items.CARVED_PUMPKIN))
            pumpkinMagic(Objects.requireNonNull(c.getLevel()), e, c);
        else if(e.getItem().is(Items.WRITABLE_BOOK))
            waterWriting(c, e);
        else if(e.getItem().is(Tags.Items.INGOTS_COPPER) && c.getPowerLevel(Powers.ACID_POWER.get()) > 10)
            copperCharging(c);
        else if(e.getItem().is(Items.ENDER_EYE) && c.getPowerLevel(Powers.CURSE_POWER.get()) < 10)
            enderEyeFlyAway(c, e);

        tryEmptyPowerBottle(e, c);
    }

    public static void checkEmptySpecialCases(CrucibleBlockEntity c){
        if(c.getLevel() == null) return;
        if(c.getPowerLevel(Powers.SOUL_POWER.get()) > WorldSpecificValue.get(c.getLevel(), "soul_escape_threshold", 300, 600))
            soulEscape(c);
        if(c.getPowerLevel(Powers.CURSE_POWER.get()) > 665)
            curseEscape(c);
        if(c.getPowerLevel(Powers.BLAZE_POWER.get()) > WorldSpecificValue.get(c.getLevel(), "blaze_escape_threshold", 20, 100))
            blazeEscape(c);
        if(c.getPowerLevel(Powers.VERDANT_POWER.get()) > WorldSpecificValue.get(c.getLevel(), "verdant_escape_threshold", 1300, 1500))
            verdantEscape(c);
    }

    public static ItemStack checkBottleSpecialCases(CrucibleBlockEntity c, ItemStack bottle){
        if(c.enderRiftStrength > 0 && bottle.is(Registration.WARP_BOTTLE.get()))
            return makeRiftBottle(c, bottle);
        return bottle;
    }

    private static void tryEmptyPowerBottle(ItemEntity e, CrucibleBlockEntity c){
        final int BOTTLE_RETURN = WorldSpecificValue.get(Objects.requireNonNull(c.getLevel()), "bottle_return", 750, 840);
        boolean changed = false;
        for(Power p : Powers.POWER_SUPPLIER.get()){
            if(p.matchesBottle(e.getItem())){
                if(c.addPower(p, BOTTLE_RETURN)) {
                    if(e.getItem().is(Registration.WARP_BOTTLE.get()) && WarpBottleItem.isRiftBottle(e.getItem())){
                        c.enderRiftStrength = 2000;
                    }
                    if (e.getItem().getCount() == 1) {
                        e.setItem(Registration.QUARTZ_BOTTLE.get().getDefaultInstance());
                    }
                    else {
                        e.getItem().shrink(1);
                        ItemEntity empty_bottle = new ItemEntity(c.getLevel(), e.getX(), e.getY(), e.getZ(), Registration.QUARTZ_BOTTLE.get().getDefaultInstance());
                        e.getLevel().addFreshEntity(empty_bottle);
                    }
                    changed = true;
                }
            }
        }

        if(changed){
            c.setDirty();
            c.getLevel().playSound(null, c.getBlockPos(), SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1F, 0.65F+(c.getLevel().getRandom().nextFloat()/5));
        }
    }

    // Copper ingots in acid charge the Crucible.
    private static void copperCharging(CrucibleBlockEntity c) {
        if(c.electricCharge < 20)
            c.electricCharge += 2;
    }

    // Ender eyes that are thrown into the Crucible without Curse are launched as if used.
    private static void enderEyeFlyAway(CrucibleBlockEntity c, ItemEntity e) {
        ServerLevel serverlevel = (ServerLevel) c.getLevel();
        BlockPos blockpos = serverlevel.findNearestMapStructure(StructureTags.EYE_OF_ENDER_LOCATED, c.getBlockPos(), 100, false);
        if (blockpos != null) {
            EyeOfEnder eyeofender = new EyeOfEnder(c.getLevel(), c.getBlockPos().getX(), c.getBlockPos().getY(), c.getBlockPos().getZ());
            eyeofender.setItem(e.getItem());
            eyeofender.signalTo(blockpos);
            c.getLevel().addFreshEntity(eyeofender);
            c.getLevel().playSound(null, c.getBlockPos().getX()+0.5, c.getBlockPos().getY()+0.5, c.getBlockPos().getZ()+0.5, SoundEvents.ENDER_EYE_LAUNCH, SoundSource.NEUTRAL, 0.5F, 0.4F / (c.getLevel().getRandom().nextFloat() * 0.4F + 0.8F));
            e.getItem().shrink(1);
            if(e.getItem().getCount() < 1)
                e.kill();
        }
    }

    // Dissolving a carved pumpkin might have many effects.
    private static void pumpkinMagic(Level level, ItemEntity e, CrucibleBlockEntity c) {
        if (level.isClientSide)
            return;

        int cause = WorldSpecificValues.GOLEM_CAUSE.get(level);
        BlockPos candlePos = c.areaMemory.fetch(level, ConfigMan.COMMON.crucibleRange.get(), Blocks.CANDLE);
        boolean ironSymbol = c.areaMemory.exists(level, ConfigMan.COMMON.crucibleRange.get(), Registration.IRON_SYMBOL.get());

        if (candlePos != null && !ironSymbol && level.getBlockState(candlePos).getValue(CandleBlock.LIT)) {
            if (cause == 1) { // It's most likely that an Allay will spawn.
                if (level.random.nextFloat() > 0.07 && !(c.getPowerLevel(Powers.CURSE_POWER.get()) > 20))
                    EntityType.ALLAY.spawn((ServerLevel) level, null, null, candlePos, MobSpawnType.MOB_SUMMONED, true, true);
                else
                    EntityType.VEX.spawn((ServerLevel) level, null, null, candlePos, MobSpawnType.MOB_SUMMONED, true, true);
            } else if (cause == 2) { // It's most likely that a Vex will spawn.
                if (level.random.nextFloat() > 0.07 && !(c.getPowerLevel(Powers.MIND_POWER.get()) > 20))
                    EntityType.VEX.spawn((ServerLevel) level, null, null, candlePos, MobSpawnType.MOB_SUMMONED, true, true);
                else
                    EntityType.ALLAY.spawn((ServerLevel) level, null, null, candlePos, MobSpawnType.MOB_SUMMONED, true, true);
            }
            e.kill();
            Helper.drawParticleLine(level, ParticleTypes.ENCHANTED_HIT,
                    c.getBlockPos().getX() + 0.5, c.getBlockPos().getY() + 0.5125, c.getBlockPos().getZ() + 0.5,
                    candlePos.getX() + 0.5, candlePos.getY() + 0.38, candlePos.getZ() + 0.5, 20, 0.01);

            for(int i = 0; i < 10; i++) {
                ((ServerLevel) level).sendParticles(ParticleTypes.POOF,
                        candlePos.getX() + 0.5, candlePos.getY() + 0.38, candlePos.getZ() + 0.5,
                        1, 0, 0, 0, 0.0);
            }

            level.playSound(null, candlePos, SoundEvents.SOUL_ESCAPE, SoundSource.BLOCKS, 1.0F, 1.0F);
        }
    }

    // Dissolving an Ender Pearl teleports you onto the crucible if there's not too much Warp.
    private static void enderPearlDissolve(Level l, BlockPos p, ItemEntity e, CrucibleBlockEntity c){
        float chance = (float) c.getPowerLevel(Powers.WARP_POWER.get()) / CrucibleBlockEntity.CRUCIBLE_MAX_POWER;
        if(l.random.nextFloat() > chance){
            return;
        }

        for(int i = 0; i < 32; ++i) {
            ((ServerLevel) l).sendParticles(ParticleTypes.PORTAL, e.getX(), e.getY() + l.random.nextDouble() * 2.0, e.getZ(), 1, l.random.nextGaussian(), 0.0, l.random.nextGaussian(), 0.0);
        }

        boolean foundTarget = false;

        UUID thrower = e.getThrower();
        if(thrower != null) {
            Player player = l.getPlayerByUUID(thrower);
            if(!l.isClientSide)
                Registration.ENDER_PEARL_DISSOLVE_TRIGGER.trigger((ServerPlayer) player);
            if(player != null && e.getLevel().dimension().equals(player.getLevel().dimension())){
                player.teleportTo(p.getX() + 0.5, p.getY() + 0.85, p.getZ() + 0.5);
                foundTarget = true;
            }
        }
        if(!foundTarget){
            Helper.triggerForNearbyPlayers((ServerLevel) l, Registration.MAKE_RIFT_TRIGGER, p, 20);
            c.enderRiftStrength = 2000;
        }
        e.kill();
    }

    // Attempts to teleport an entity with Crucible range of pos to the destination.
    public static boolean tryTeleportNearbyEntity(BlockPos pos, Level level, BlockPos destination, boolean can_teleport_players){
        AABB aoe = new AABB(pos);
        aoe = aoe.inflate(ConfigMan.COMMON.crucibleRange.get());
        List<LivingEntity> nearby_ents = Objects.requireNonNull(level).getEntitiesOfClass(LivingEntity.class, aoe);

        List<LivingEntity> to_be_excluded = new ArrayList<>();

        for(LivingEntity e : nearby_ents){
            if(ConfigMan.COMMON.doNotTeleport.get().contains(e.getEncodeId())){
                to_be_excluded.add(e);
            }
            if(e instanceof Player && !can_teleport_players){
                to_be_excluded.add(e);
            }
        }

        nearby_ents.removeAll(to_be_excluded);

        if(!nearby_ents.isEmpty() && CrystalIronItem.effectNotBlocked(level, nearby_ents.get(0), level.random.nextFloat() < 0.02 ? 1 : 0, true)){
            nearby_ents.get(0).teleportTo(destination.getX() + 0.5, destination.getY() + 0.85, destination.getZ() + 0.5);
            return true;
        }
        return false;
    }

    // Make a Warp Bottle into a Rift Bottle.
    public static ItemStack makeRiftBottle(CrucibleBlockEntity c, ItemStack bottle){
        if(bottle.getTag() == null){
            bottle.setTag(new CompoundTag());
        }
        WarpBottleItem.addTeleportTags(Objects.requireNonNull(c.getLevel()).dimension(), c.getBlockPos(), bottle.getTag());
        c.enderRiftStrength = 0;
        return bottle;
    }

    // Explode gunpowder due to blaze.
    private static void explodeGunpowderDueToBlaze(Level l, BlockPos p, ItemEntity e){
        l.explode(e, p.getX()+0.5, p.getY()+0.5, p.getZ()+0.5, 1.0F, Explosion.BlockInteraction.NONE);
        e.kill();
    }

    // Putting a writable book in a crucible with Mind will fill it with gibberish.
    private static void waterWriting(CrucibleBlockEntity c, ItemEntity e){
        if(c.getPowerLevel(Powers.MIND_POWER.get()) < WorldSpecificValue.get(Objects.requireNonNull(c.getLevel()), "water_write_threshold",
                WorldSpecificValue.get(e.level, "water_write_cost", 20, 50), 700)) {
            return;
        }

        String CHAR_LIST = "abcdefhijklmnopqstuvwxyz, -;6'";

        CompoundTag book_tag = e.getItem().getTag();
        if(book_tag == null) {
            e.getItem().addTagElement("pages", new ListTag());
            book_tag = e.getItem().getTag();
        }

        ListTag pages = book_tag.getList("pages", CompoundTag.TAG_STRING);
        int pagecount = Math.max(c.getPowerLevel(Powers.MIND_POWER.get()) / 220, pages.size());
        for(int i = 0; i < pagecount; i++){
            StringBuilder gibberish = new StringBuilder();
            for(int j = 0; j < 200; j++){
                gibberish.append(CHAR_LIST.charAt(e.level.random.nextInt(CHAR_LIST.length())));
            }
            if(i < pages.size())
                pages.set(i, StringTag.valueOf(gibberish.toString()));
            else
                pages.add(i, StringTag.valueOf(gibberish.toString()));
        }

        e.level.playSound(null, c.getBlockPos(), SoundEvents.BOOK_PAGE_TURN, SoundSource.BLOCKS, 1F, 1F);
        e.level.playSound(null, c.getBlockPos(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.BLOCKS, 0.7F, 0.7F);
        c.expendPower(Powers.MIND_POWER.get(), WorldSpecificValue.get(e.level, "water_write_cost", 20, 50));
        c.setDirty();
    }

    private static void soulEscape(CrucibleBlockEntity c){
        if(c.getLevel() == null) return;
        if(c.getLevel().isClientSide()){
            c.getLevel().addParticle(ParticleTypes.SOUL, c.getBlockPos().getX() + 0.5, c.getBlockPos().getY() + 0.65,
                    c.getBlockPos().getZ() + 0.5, 0, 0, 0);
        }else{
            ((ServerLevel) c.getLevel()).sendParticles(ParticleTypes.SOUL, c.getBlockPos().getX() + 0.5, c.getBlockPos().getY() + 0.65,
                    c.getBlockPos().getZ() + 0.5, 1,0, 0, 0, 0.0);
        }
    }

    private static void curseEscape(CrucibleBlockEntity c){
        if(c.getLevel() == null) return;
        AABB aoe = new AABB(c.getBlockPos());
        aoe = aoe.inflate(5); // Inflate the AOE to be 5x the size of the crucible.
        if(!c.getLevel().isClientSide()){
            if(c.getPowerLevel(Powers.CURSE_POWER.get()) > 1400){
                Monster m;
                if(c.getLevel().getRandom().nextFloat() < 0.35){
                    m = new Skeleton(EntityType.SKELETON, c.getLevel());
                }else{
                   m = new Zombie(EntityType.ZOMBIE, c.getLevel());
                }
                m.setSilent(true);
                m.setPos(aoe.getCenter().add(WorldSpecificValue.get(c.getLevel(), "monster_summon_x", -5, 5), 1, WorldSpecificValue.get(c.getLevel(), "monster_summon_z", -5, 5)));
                c.getLevel().addFreshEntity(m);
            }
            List<LivingEntity> nearby_ents = c.getLevel().getEntitiesOfClass(LivingEntity.class, aoe);
            for(LivingEntity e : nearby_ents){
                if(e.getMobType().equals(MobType.UNDEAD))
                    continue;
                e.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 300, 1));
                e.addEffect(new MobEffectInstance(MobEffects.WITHER, 200, 1));
                e.hurt(DamageSource.MAGIC, 10);
                if(e instanceof Player){
                    Registration.BE_CURSED_TRIGGER.trigger((ServerPlayer) e);
                }
            }
            c.getLevel().playSound(null, c.getBlockPos(), SoundEvents.AMBIENT_CAVE, SoundSource.BLOCKS, 1, 1);
        }
    }

    private static void blazeEscape(CrucibleBlockEntity c){
        if(c.getLevel() == null) return;
        AABB blast_zone = new AABB(c.getBlockPos());
        blast_zone.inflate(1, 3, 1); // Inflate the AOE to be 5x the size of the crucible.
        if(!c.getLevel().isClientSide()){
            List<LivingEntity> nearby_ents = c.getLevel().getEntitiesOfClass(LivingEntity.class, blast_zone);
            for(LivingEntity e : nearby_ents){
                e.hurt(DamageSource.IN_FIRE, 12);
                e.setSecondsOnFire(3);
            }
            c.getLevel().playSound(null, c.getBlockPos(), SoundEvents.BLAZE_SHOOT, SoundSource.BLOCKS, 1.0F, 1.0F);
            for(int i = 0; i < 10; i++) {
                if(c.getPowerLevel(Powers.SOUL_POWER.get()) > 20){
                    Helper.drawParticleCrucibleTop(c.getLevel(), ParticleTypes.SOUL_FIRE_FLAME, c.getBlockPos(), 1, 0, 1, 0);
                }else{
                    Helper.drawParticleCrucibleTop(c.getLevel(), ParticleTypes.FLAME, c.getBlockPos(), 1, 0, 1, 0);
                }
            }
        }
    }

    private static void verdantEscape(CrucibleBlockEntity c) {
        if(c.getLevel() == null || c.getLevel().isClientSide || WorldSpecificValue.getBool((ServerLevel) c.getLevel(), "no_moss", 0.5F)) return;
        ((MossBlock) Blocks.MOSS_BLOCK).performBonemeal((ServerLevel) c.getLevel(), c.getLevel().random, c.getBlockPos().below(), c.getBlockState());
    }
}
