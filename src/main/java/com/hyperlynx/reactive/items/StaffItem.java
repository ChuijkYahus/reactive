package com.hyperlynx.reactive.items;

import com.hyperlynx.reactive.be.StaffBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.function.Function;

public class StaffItem extends BlockItem {
    Function<Player, Player> effectFunction;
    boolean beam; // Whether the effect should render as a beam (true) or zap (false).
    public Item repair_item;

    public StaffItem(Block block, Properties props, Function<Player, Player> effect, boolean beam, Item repair_item) {
        super(block, props);
        effectFunction = effect;
        this.beam = beam;
        this.repair_item = repair_item;
    }

    @Override
    public boolean shouldCauseReequipAnimation(ItemStack oldStack, ItemStack newStack, boolean slotChanged) {
        return slotChanged;
    }

    @Override
    public void onUseTick(Level level, LivingEntity player, ItemStack stack, int ticks) {
        if(ticks % 10 == 1) {
            if(level.isClientSide && !beam)
                effectFunction.apply((Player) player);

            if(!level.isClientSide) {
                effectFunction.apply((Player) player);
                if (player.getOffhandItem().is(stack.getItem())) {
                    player.getOffhandItem().hurtAndBreak(1, player, (LivingEntity l) -> {});
                } else {
                    player.getMainHandItem().hurtAndBreak(1, player, (LivingEntity l) -> {});
                }
            }
        }
        if (level.isClientSide && beam) effectFunction.apply((Player) player);
    }

    @Override
    public @NotNull UseAnim getUseAnimation(@NotNull ItemStack stack) {
        return UseAnim.BOW;
    }

    public int getUseDuration(ItemStack stack) {
        return 72000;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if(!player.isCrouching())
            player.startUsingItem(hand);
        return super.use(level, player, hand);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if(context.getPlayer() == null)
            return InteractionResult.SUCCESS;
        if(context.getPlayer().isCrouching())
            return super.useOn(context);
        return InteractionResult.PASS;
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return true;
    }

    // Check if the item being used to repair is the assigned repair bottle for this staff.
    @Override
    public boolean isValidRepairItem(ItemStack self, ItemStack repair_item_candidate) {
        return repair_item != null && repair_item_candidate.is(repair_item);
    }

    // Called when the item is placed to store item stack data into the block entity.
    @Override
    protected boolean updateCustomBlockEntityTag(BlockPos pos, Level level, @Nullable Player placer, ItemStack stack, BlockState state) {
        MinecraftServer server = level.getServer();
        if (server == null)
            return false;

        BlockEntity blockentity = level.getBlockEntity(pos);
        if (blockentity == null)
            return false;

        CompoundTag data_tag = blockentity.saveWithoutMetadata();
        CompoundTag prior_data_tag = data_tag.copy();
        data_tag.put(StaffBlockEntity.ITEM_STACK_TAG, stack.hasTag() ? Objects.requireNonNull(stack.getTag()) : new CompoundTag());

        if (!data_tag.equals(prior_data_tag)) {
            blockentity.load(data_tag);
            blockentity.setChanged();
            return true;
        }

        return false;
    }
}
