package com.hyperlynx.reactive.be;

import com.hyperlynx.reactive.ReactiveMod;
import com.hyperlynx.reactive.Registration;
import com.hyperlynx.reactive.alchemy.IPowerBearer;
import com.hyperlynx.reactive.alchemy.Power;
import com.hyperlynx.reactive.alchemy.rxn.Reaction;
import com.hyperlynx.reactive.alchemy.rxn.SpecialCaseMan;
import com.hyperlynx.reactive.blocks.CrucibleBlock;
import com.hyperlynx.reactive.recipes.TransmuteRecipe;
import com.hyperlynx.reactive.util.Color;
import com.hyperlynx.reactive.util.ConfigMan;
import com.hyperlynx.reactive.util.FakeContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.*;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;

/*
    The heart of the whole mod, the Crucible's Block Entity.
    This is a complicated class, but each method should be pretty self-explanatory, or documented if not.
    Overall, the crucible does these things every (configurable, default 30) ticks:
        - Check the blockstate to see if it should empty itself.
        - Check to see if there are new item entities.
            - If there are, check if they need to have any recipes applied, and do that if there are.
            - Otherwise, just dissolve them and add their Power to the pool.
        - Use ReactionMan to run reactions.
 */
public class CrucibleBlockEntity extends BlockEntity implements IPowerBearer {
    public static final int CRUCIBLE_MAX_POWER = 1600; // The maximum power the Crucible can hold.

    HashMap<Power, Integer> powers = new HashMap<>(); // A map of Powers to their amounts.

    private int tick_counter = 0; // Used for counting active ticks. See tick().
    private final Color mix_color = new Color(); // Used to cache mixture color between updates;
    public boolean color_changed = true; // This is set to true when the color needs to be updated next rendering tick.

    public CrucibleBlockEntity(BlockPos pos, BlockState state) {
        super(Registration.CRUCIBLE_BE_TYPE.get(), pos, state);
    }

    // ----- Tick and related worker methods -----
    public static void tick(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible) {
        crucible.tick_counter++;
        if(crucible.tick_counter >= ConfigMan.COMMON.crucibleTickDelay.get()) {
            crucible.tick_counter = 1;
            if (!level.isClientSide()){
                // Become empty when there's no water.
                if (!state.getValue(CrucibleBlock.FULL) && crucible.getTotalPowerLevel() > 0) {
                    SpecialCaseMan.checkEmptySpecialCases(crucible);
                    crucible.expendPower();
                    crucible.setDirty(level, pos, state);
                    level.playSound(null, pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.BLOCKS, 0.6F, 1F);
                }

                // Check for new items to dissolve into Power or transmute.
                if (processItemsInside(level, pos, state, crucible)) {
                    crucible.setDirty(level, pos, state);
                    level.playSound(null, pos, SoundEvents.BREWING_STAND_BREW, SoundSource.BLOCKS, 1F, 0.65F+(level.getRandom().nextFloat()/5));
                }

            }
            else {
                if (!state.getValue(CrucibleBlock.FULL)  && crucible.getTotalPowerLevel() > 0) {
                    // Repeated to make sure that the client side updates quickly.
                    crucible.expendPower();
                    crucible.setDirty(level, pos, state);
                }
            }

            // Do reactions, if you can. This intentionally happens on both logical sides.
            if(state.getValue(CrucibleBlock.FULL)) react(level, crucible);

        }
    }

    // The method that actually performs reactions.
    private static void react(Level level, CrucibleBlockEntity crucible){
        for(Reaction r : ReactiveMod.REACTION_MAN.getReactions(level)){
            if(r.conditionsMet(crucible)){
                r.run(crucible);
                crucible.setDirty();
            }
        }
    }

    private static boolean processItemsInside(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible){
        if(!state.getValue(CrucibleBlock.FULL)){
            return false;
        }
        boolean changed = false;
        for(Entity e : CrucibleBlock.getEntitesInside(pos, level)){
            if(e instanceof ItemEntity){
                SpecialCaseMan.checkDissolveSpecialCases(crucible, (ItemEntity) e);
                changed = changed || tryTransmute(level, pos, state, crucible, ((ItemEntity) e));
                changed = changed || tryReduceToPower(((ItemEntity) e).getItem(), crucible);

                // Remove entities that were completely transmuted or dissolved.
                System.out.println("Item " + ((ItemEntity) e).getItem().getDescriptionId() + " had count " + ((ItemEntity) e).getItem().getCount());
                if(((ItemEntity) e).getItem().getCount() == 0){
                    e.remove(Entity.RemovalReason.KILLED);
                }
            }
        }
        return changed;
    }

    public static boolean tryReduceToPower(ItemStack i, CrucibleBlockEntity crucible){
        List<Power> stack_power_list = Power.getSourcePower(i);
        boolean changed = false;
        for (Power p : stack_power_list) {
            int dissolve_capacity = (CrucibleBlockEntity.CRUCIBLE_MAX_POWER - crucible.getPowerLevel(p)) / Power.getSourceLevel(i, crucible.getLevel());
            if(dissolve_capacity <= 0){
                continue;
            }
            changed = changed || crucible.addPower(p, i.getCount() * Power.getSourceLevel(i, crucible.getLevel()) / stack_power_list.size());
            i.setCount(Math.max(i.getCount()-dissolve_capacity, 0));
        }
        return changed;
    }

    // Attempts to find a transmutation recipe that matches the item. Returns whether it did.
    private static boolean tryTransmute(Level level, BlockPos pos, BlockState state, CrucibleBlockEntity crucible, ItemEntity itemEntity) {
        List<TransmuteRecipe> purify_recipes = level.getRecipeManager().getAllRecipesFor(Registration.TRANS_RECIPE_TYPE.get());
        for (TransmuteRecipe r : purify_recipes) {
            if (r.matches(new FakeContainer(itemEntity.getItem()), level)) {
                System.err.println("Checking power levels for " + r);
                if (r.powerMet(crucible, level)) {
                    ItemStack result = r.apply(itemEntity.getItem(), crucible, level);
                    level.addFreshEntity(new ItemEntity(level, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, result));
                    crucible.setDirty(level, pos, state);
                    return true;
                }
            }
        }
        return false;
    }

    // ----- Helper and power management methods -----

    public void setDirty(){
        setDirty(this.getLevel(), this.getBlockPos(), this.getBlockState());
    }
    public void setDirty(Level level, BlockPos pos, BlockState state){
        this.setChanged();
        level.sendBlockUpdated(pos, state, state, Block.UPDATE_CLIENTS);
    }

    public float getOpacity() {
        return 0.7F + (.3F * getTotalPowerLevel()/CRUCIBLE_MAX_POWER);
    }

    @Override
    public boolean addPower(Power p, int amount) {
        if(p == null){
            return false;
        }
        if(getTotalPowerLevel() + amount > CRUCIBLE_MAX_POWER) {
            int excess = getTotalPowerLevel() + amount - CRUCIBLE_MAX_POWER;
            expendAnyPowerExcept(p, excess); // Replace other powers if needed.
            excess = getTotalPowerLevel() + amount - CRUCIBLE_MAX_POWER;
            if(excess > 0) {
                System.out.println("Excess " + excess + ", amount " + amount);
                amount -= excess;
            }
        }

        int prev = powers.getOrDefault(p, 0);
        if(prev > 0)
            powers.replace(p, amount + prev);
        else
            powers.put(p, amount);

        if(this.getLevel() != null && !this.getLevel().isClientSide)
            System.out.println("Tried to add " + amount + " " + p.getName() + ".");

        return true;
    }

    @Override
    public int getPowerLevel(Power t) {
        if(powers.isEmpty() || powers.get(t) == null){
            return 0;
        }
        return powers.get(t);
    }

    @Override
    public boolean expendPower(Power t, int amount) {
        if(powers.isEmpty()){
            return false;
        }
        int level = powers.get(t);
        if(level > amount){
            powers.put(t, level-amount);
            return true;
        }
        if (level == amount) {
            powers.put(t, 0);
            return true;
        }

        // This implies that all power t wasn't enough to meet amount.
        powers.put(t, 0);
        return false;
    }


    public void expendAnyPowerExcept(Power immune_power, int amount) {
        boolean expended = false;
        for(Power p : powers.keySet()){
            if(p != immune_power){
                expended = expendPower(p, amount);
            }
            if(expended) return;
        }
    }

    public void expendPower() {
        powers.clear();
        color_changed = true;
    }

    public int getTotalPowerLevel(){
        int totalpp = 0;
        if(powers == null) return 0;
        for (Power p : powers.keySet()) {
            totalpp += powers.get(p);
        }
        return totalpp;
    }

    // These methods calculate and return the combined color of the cauldron's mixture
    @Override
    public Color getCombinedColor(int water_color_number) {
        Color water_color = new Color(water_color_number);
        if(powers == null || powers.isEmpty() || getTotalPowerLevel() == 0){
            return water_color;
        }
        if(color_changed){
            updateColor(water_color);
        }
        return mix_color;
    }

    private void updateColor(Color water_color){
        if(powers == null){
            return;
        }
        // Iterate through each power and add its tint to the total, adjusted for its actual prevalence.
        mix_color.reset();
        for (Power p : powers.keySet()) {
            if(p == null){
                continue; // Skip any invalid values if they exist.
            }
            Color pow_color = p.getColor();
            float pow_weight = getPowerLevel(p) / (float) getTotalPowerLevel();
            mix_color.red += pow_color.red * pow_weight;
            mix_color.green += pow_color.green * pow_weight;
            mix_color.blue += pow_color.blue * pow_weight;
        }

        // Adjust the tint to be proportional to the amount of the crucible's maximum currently in use.
        float tint_alpha = (float) getTotalPowerLevel()/ (float) CRUCIBLE_MAX_POWER;
        mix_color.red = (int) (water_color.red * (1 - tint_alpha) + mix_color.red * (tint_alpha));
        mix_color.green = (int) (water_color.green * (1 - tint_alpha) + mix_color.green * (tint_alpha));
        mix_color.blue = (int) (water_color.blue * (1 - tint_alpha) + mix_color.blue * (tint_alpha));
        color_changed = false;
    }

    // ----- Data management methods -----

    @Override
    public @NotNull CompoundTag getUpdateTag() {
        CompoundTag tag = new CompoundTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    @Override
    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        super.onDataPacket(net, pkt);
        color_changed = true;
    }

    @Override
    protected void saveAdditional(@NotNull CompoundTag main_tag) {
        super.saveAdditional(main_tag);
        if(powers == null || powers.isEmpty()){
            return;
        }
        ListTag power_list_tag = new ListTag();
        for (Power p : powers.keySet()) {
            if(p == null) {
                System.err.println("Skipping null power in save process.");
                continue; // Purge bad nulls.
            }
            CompoundTag tag = new CompoundTag();
            tag.put("name", StringTag.valueOf(p.getName()));
            tag.put("level", IntTag.valueOf(getPowerLevel(p)));
            power_list_tag.add(tag);
        }
        main_tag.put("powers", power_list_tag);
    }

    @Override
    public void load(@NotNull CompoundTag main_tag) {
        super.load(main_tag);
        // Powers tag is guaranteed to be a list.
        ListTag power_list_tag = (ListTag) main_tag.get("powers");
        powers.clear();
        if(power_list_tag != null && !power_list_tag.isEmpty()) {
            for (Tag power_tag : power_list_tag) {
                Power p = Power.readPower((CompoundTag) power_tag);
                addPower(p, ((CompoundTag) power_tag).getInt("level"));
            }
        }
    }

}
