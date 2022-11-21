package com.hyperlynx.reactive.blocks;

import com.hyperlynx.reactive.util.WorldSpecificValue;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import org.jetbrains.annotations.Nullable;

import java.util.Random;

public class RunestoneBlock extends HorizontalDirectionalBlock {

    public RunestoneBlock(BlockBehaviour.Properties props) {
        super(props);
        registerDefaultState(getStateDefinition().any().setValue(FACING, Direction.NORTH));
    }

    @Override
    public float getEnchantPowerBonus(BlockState state, LevelReader level, BlockPos pos) {
        return WorldSpecificValue.get((Level) level, "rune_power", 0.36F, 0.56F);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        Random random = new Random();
        int choice = random.nextInt(1, 4);
        if(choice == 1)
            return getStateDefinition().any().setValue(FACING, Direction.NORTH);
        else if(choice == 2)
            return getStateDefinition().any().setValue(FACING, Direction.WEST);
        if(choice == 3)
            return getStateDefinition().any().setValue(FACING, Direction.EAST);
        return getStateDefinition().any().setValue(FACING, Direction.SOUTH);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(FACING);
    }
}
