package com.hyperlynx.reactive.blocks;

import com.hyperlynx.reactive.items.CrystalIronItem;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ExperienceOrb;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BonemealableBlock;
import net.minecraft.world.level.block.MultifaceBlock;
import net.minecraft.world.level.block.MultifaceSpreader;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public class MindLichenBlock extends MultifaceBlock implements BonemealableBlock {
    private final MultifaceSpreader spreader = new MultifaceSpreader(this);

    public MindLichenBlock(Properties props) {
        super(props);
    }

    @Override
    public @NotNull MultifaceSpreader getSpreader() {
        return spreader;
    }

    public boolean isValidBonemealTarget(BlockGetter getter, BlockPos pos, BlockState state, boolean flag) {
        return Direction.stream().anyMatch((matching_direction) -> {
            return this.spreader.canSpreadInAnyDirection(state, getter, pos, matching_direction.getOpposite());
        });
    }

    public boolean isBonemealSuccess(Level level, RandomSource randomSource, BlockPos pos, BlockState state) {
        return true;
    }

    public void performBonemeal(ServerLevel level, RandomSource randomSource, BlockPos pos, BlockState state) {
        this.spreader.spreadFromRandomFaceTowardRandomDirection(state, level, pos, randomSource);
    }

    public void entityInside(BlockState state, Level level, BlockPos pos, Entity entity) {
        if (entity instanceof Player player) {
            if(CrystalIronItem.effectNotBlocked(player, 1) && player.totalExperience > 10){
                player.giveExperiencePoints(-10);
                ExperienceOrb xp = new ExperienceOrb(level, player.getX(), player.getY() + 0.8, player.getZ(), 10);
                double throw_power = 1.9;
                xp.setDeltaMovement((level.getRandom().nextDouble() - 0.5) * throw_power, (level.getRandom().nextDouble() - 0.5) * throw_power, (level.getRandom().nextDouble() - 0.5) * throw_power);
                level.addFreshEntity(xp);
                if(level instanceof ServerLevel slevel)
                    performBonemeal(slevel, level.random, pos, level.getBlockState(pos));
            }
        }
    }
}
