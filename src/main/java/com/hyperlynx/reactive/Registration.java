package com.hyperlynx.reactive;

import com.hyperlynx.reactive.advancements.CriteriaTriggers;
import com.hyperlynx.reactive.alchemy.Powers;
import com.hyperlynx.reactive.be.*;
import com.hyperlynx.reactive.blocks.*;
import com.hyperlynx.reactive.integration.create.ReactiveCreatePlugin;
import com.hyperlynx.reactive.items.*;
import com.hyperlynx.reactive.recipes.*;
import com.hyperlynx.reactive.util.HyperMobEffect;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

@SuppressWarnings("unused")
@Mod.EventBusSubscriber(modid=ReactiveMod.MODID, bus=Mod.EventBusSubscriber.Bus.MOD)
public class Registration {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, ReactiveMod.MODID);
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, ReactiveMod.MODID);
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, ReactiveMod.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, ReactiveMod.MODID);
    public static final DeferredRegister<ParticleType<?>> PARTICLES = DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, ReactiveMod.MODID);
    public static final DeferredRegister<BlockEntityType<?>> TILES = DeferredRegister.create(ForgeRegistries.BLOCK_ENTITY_TYPES, ReactiveMod.MODID);
    public static final DeferredRegister<RecipeType<?>> RECIPE_TYPES = DeferredRegister.create(ForgeRegistries.RECIPE_TYPES, ReactiveMod.MODID);
    public static final DeferredRegister<RecipeSerializer<?>> RECIPE_SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, ReactiveMod.MODID);
    public static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, ReactiveMod.MODID);

    public static void init() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        BLOCKS.register(bus);
        ITEMS.register(bus);
        MOB_EFFECTS.register(bus);
        POTIONS.register(bus);
        PARTICLES.register(bus);
        TILES.register(bus);
        Powers.POWERS.register(bus);
        RECIPE_TYPES.register(bus);
        RECIPE_SERIALIZERS.register(bus);
        SOUND_EVENTS.register(bus);
        bus.register(Registration.class);
    }

    // ----------------------- REGISTRATION ------------------------
    // Register the all-important Crucible.
    public static final RegistryObject<Block> CRUCIBLE = BLOCKS.register("crucible",
            () -> new CrucibleBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON)));
    public static final RegistryObject<Item> CRUCIBLE_ITEM = fromBlock(CRUCIBLE, ReactiveMod.CREATIVE_TAB);

    // Register the Shulker Crucible
    public static final RegistryObject<Block> SHULKER_CRUCIBLE = BLOCKS.register("shulker_crucible",
            () -> new ShulkerCrucibleBlock(BlockBehaviour.Properties.of(Material.SHULKER_SHELL)));
    public static final RegistryObject<Item> SHULKER_CRUCIBLE_ITEM = fromBlock(SHULKER_CRUCIBLE, ReactiveMod.CREATIVE_TAB);

    // Register the Crucible BE.
    public static final RegistryObject<BlockEntityType<CrucibleBlockEntity>> CRUCIBLE_BE_TYPE = TILES.register("crucible_be",
            () -> BlockEntityType.Builder.of(CrucibleBlockEntity::new, CRUCIBLE.get(), SHULKER_CRUCIBLE.get()).build(null));

    // Register the Symbol blocks, items, and the BE.
    public static final RegistryObject<Block> COPPER_SYMBOL = BLOCKS.register("copper_symbol",
            () -> new SymbolBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK)));
    public static final RegistryObject<Item> COPPER_SYMBOL_ITEM = SymbolItem.fromBlock(COPPER_SYMBOL, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> IRON_SYMBOL = BLOCKS.register("iron_symbol",
            () -> new SymbolBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK)));
    public static final RegistryObject<Item> IRON_SYMBOL_ITEM = SymbolItem.fromBlock(IRON_SYMBOL, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> GOLD_SYMBOL = BLOCKS.register("gold_symbol",
            () -> new SymbolBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK)));
    public static final RegistryObject<Item> GOLD_SYMBOL_ITEM = SymbolItem.fromBlock(GOLD_SYMBOL, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> OCCULT_SYMBOL = BLOCKS.register("occult_symbol",
            () -> new OccultSymbolBlock(BlockBehaviour.Properties.copy(Blocks.TRIPWIRE_HOOK)));
    public static final RegistryObject<Item> OCCULT_SYMBOL_ITEM = SymbolItem.fromBlock(OCCULT_SYMBOL, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<BlockEntityType<SymbolBlockEntity>> SYMBOL_BE_TYPE = TILES.register("symbol_be",
            () -> BlockEntityType.Builder.of(SymbolBlockEntity::new, COPPER_SYMBOL.get(), IRON_SYMBOL.get(), GOLD_SYMBOL.get(), OCCULT_SYMBOL.get()).build(null));

    // Additional blocks
    public static final RegistryObject<Block> BLAZE_ROD = BLOCKS.register("blaze_rod",
            () -> new BlazeRodBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD)));
    public static final RegistryObject<Item> BLAZE_ROD_ITEM = fromBlock(BLAZE_ROD, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> STARDUST = BLOCKS.register("stardust",
            () -> new StardustBlock(BlockBehaviour.Properties.copy(Blocks.TORCH).lightLevel((BlockState s) -> 15).sound(SoundType.WOOL)));

    public static final RegistryObject<Block> PURE_QUARTZ_BLOCK = BLOCKS.register("pure_quartz_block",
            () -> new PureQuartzBlock(BlockBehaviour.Properties.copy(Blocks.QUARTZ_BLOCK)));
    public static final RegistryObject<Item> PURE_QUARTZ_BLOCK_ITEM = fromBlock(PURE_QUARTZ_BLOCK, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> VOLT_CELL = BLOCKS.register("volt_cell",
            () -> new VoltCellBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)));
    public static final RegistryObject<Item> VOLT_CELL_ITEM = fromBlock(VOLT_CELL, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> WARP_SPONGE = BLOCKS.register("warp_sponge",
            () -> new WarpSpongeBlock(BlockBehaviour.Properties.copy(Blocks.WET_SPONGE)));
    public static final RegistryObject<Item> WARP_SPONGE_ITEM = fromBlock(WARP_SPONGE, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> GOLD_FOAM = BLOCKS.register("gold_foam",
            () -> new GoldFoamBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK)
                    .jumpFactor(0.7F).sound(SoundType.WOOL).speedFactor(1.15F)));
    public static final RegistryObject<Item> GOLD_FOAM_ITEM = fromBlock(GOLD_FOAM, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> SOLID_PORTAL = BLOCKS.register("solid_portal",
            () -> new SolidPortalBlock(BlockBehaviour.Properties.copy(Blocks.GLOWSTONE)));
    public static final RegistryObject<Item> SOLID_PORTAL_ITEM = fromBlock(SOLID_PORTAL, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> RUNESTONE = BLOCKS.register("runestone",
            () -> new RunestoneBlock(BlockBehaviour.Properties.copy(Blocks.SMOOTH_STONE)));
    public static final RegistryObject<Item> RUNESTONE_ITEM = fromBlock(RUNESTONE, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> SALTY_CRUCIBLE = BLOCKS.register("salty_crucible",
            () -> new SaltFilledCrucibleBlock(BlockBehaviour.Properties.copy(Blocks.CAULDRON).sound(SoundType.BASALT)));
    public static final RegistryObject<Item> SALTY_CRUCIBLE_ITEM = fromBlock(SALTY_CRUCIBLE);

    public static final RegistryObject<Block> SALT_BLOCK = BLOCKS.register("salt_block",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.SAND)));
    public static final RegistryObject<Item> SALT_BLOCK_ITEM = fromBlock(SALT_BLOCK, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> MOTION_SALT_BLOCK = BLOCKS.register("motion_salt_block",
            () -> new MotionSaltBlock(BlockBehaviour.Properties.copy(Blocks.TUFF).sound(SoundType.CALCITE)));
    public static final RegistryObject<Item> MOTION_SALT_BLOCK_ITEM = fromBlock(MOTION_SALT_BLOCK, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> FRAMED_MOTION_SALT_BLOCK = BLOCKS.register("framed_motion_salt_block",
            () -> new FramedMotionSaltBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)));
    public static final RegistryObject<Item> FRAMED_MOTION_SALT_BLOCK_ITEM = fromBlock(FRAMED_MOTION_SALT_BLOCK, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> FLOWER_VINES = BLOCKS.register("flower_vine",
            () -> new FlowerVineBlock(BlockBehaviour.Properties.copy(Blocks.VINE)));

    public static final RegistryObject<Block> FLOWER_VINES_BODY = BLOCKS.register("flower_vine_plant",
            () -> new FlowerVinePlantBlock(BlockBehaviour.Properties.copy(Blocks.VINE)));

    public static final RegistryObject<Item> FLOWER_VINES_ITEM = fromBlock(FLOWER_VINES, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> MIND_LICHEN = BLOCKS.register("mind_lichen",
            () -> new MindLichenBlock(BlockBehaviour.Properties.copy(Blocks.GLOW_LICHEN).lightLevel((state) -> 7)));

    public static final RegistryObject<Item> MIND_LICHEN_ITEM = fromBlock(MIND_LICHEN, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<Block> ACID_BLOCK = BLOCKS.register("acid_block",
            () -> new AcidBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).speedFactor(0.65F).strength(1.4F)));

    public static final RegistryObject<Item> ACID_BUCKET = ITEMS.register("acid_bucket",
            () -> new AcidBucketItem(ACID_BLOCK.get(), SoundEvents.BUCKET_FILL, new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));

    public static final RegistryObject<Block> GRAVITY_CHANDELIER = BLOCKS.register("gravity_chandelier",
            () -> new GravityChandelierBlock(BlockBehaviour.Properties.copy(Blocks.TORCH)));

    public static final RegistryObject<Item> GRAVITY_CHANDELIER_ITEM = fromBlock(GRAVITY_CHANDELIER, ReactiveMod.CREATIVE_TAB);

    public static final RegistryObject<BlockEntityType<GravityChandelierBlockEntity>> GRAVITY_CHANDELIER_BE_TYPE =
            TILES.register("gravity_chandelier_be",
            () -> BlockEntityType.Builder.of(GravityChandelierBlockEntity::new, GRAVITY_CHANDELIER.get()).build(null));

    public static final RegistryObject<Block> CURSE_CELL = BLOCKS.register("curse_cell",
            () -> new CellBlock(BlockBehaviour.Properties.copy(Blocks.COPPER_BLOCK)));
    public static final RegistryObject<Item> CURSE_CELL_ITEM = fromBlock(CURSE_CELL, ReactiveMod.CREATIVE_TAB);

    // Register Power bottles
    public static final RegistryObject<Item> ACID_BOTTLE = ITEMS.register("acid_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> BLAZE_BOTTLE = ITEMS.register("blaze_bottle",
            () -> new BlazeBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> MIND_BOTTLE = ITEMS.register("mind_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> SOUL_BOTTLE = ITEMS.register("soul_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> WARP_BOTTLE = ITEMS.register("warp_bottle",
            () -> new WarpBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> VERDANT_BOTTLE = ITEMS.register("verdant_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> BODY_BOTTLE = ITEMS.register("body_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> LIGHT_BOTTLE = ITEMS.register("light_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> VITAL_BOTTLE = ITEMS.register("vital_bottle",
            () -> new PowerBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));

    // Register staves
    public static final RegistryObject<Block> INCOMPLETE_STAFF = BLOCKS.register("incomplete_staff",
            () -> new IncompleteStaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD)));
    public static final RegistryObject<Item> INCOMPLETE_STAFF_ITEM = ITEMS.register(INCOMPLETE_STAFF.getId().getPath(),
            () -> new BlockItem(INCOMPLETE_STAFF.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).stacksTo(1)));

    public static final RegistryObject<Block> STAFF_OF_LIGHT = BLOCKS.register("light_staff",
            () -> new StaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD).lightLevel((BlockState) -> 15)));
    public static final RegistryObject<Item> STAFF_OF_LIGHT_ITEM = ITEMS.register(STAFF_OF_LIGHT.getId().getPath(),
            () -> new StaffItem(STAFF_OF_LIGHT.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(1000), StaffEffects::radiance, true, LIGHT_BOTTLE.get()));

    public static final RegistryObject<Block> STAFF_OF_WARP = BLOCKS.register("warp_staff",
            () -> new StaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD).lightLevel((BlockState) -> 7)));
    public static final RegistryObject<Item> STAFF_OF_WARP_ITEM = ITEMS.register(STAFF_OF_WARP.getId().getPath(),
            () -> new WarpStaffItem(STAFF_OF_WARP.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(500), WARP_BOTTLE.get()));

    public static final RegistryObject<Block> STAFF_OF_BLAZE = BLOCKS.register("blaze_staff",
            () -> new StaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD).lightLevel((BlockState) -> 13)));
    public static final RegistryObject<Item> STAFF_OF_BLAZE_ITEM = ITEMS.register(STAFF_OF_BLAZE.getId().getPath(),
            () -> new StaffItem(STAFF_OF_BLAZE.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(1200).fireResistant(), StaffEffects::blazing, false, BLAZE_BOTTLE.get()));

    public static final RegistryObject<Block> STAFF_OF_SOUL = BLOCKS.register("soul_staff",
            () -> new StaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD).lightLevel((BlockState) -> 7)));
    public static final RegistryObject<Item> STAFF_OF_SOUL_ITEM = ITEMS.register(STAFF_OF_SOUL.getId().getPath(),
            () -> new StaffItem(STAFF_OF_SOUL.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(1200), StaffEffects::spectral, false, SOUL_BOTTLE.get()));

    public static final RegistryObject<Block> STAFF_OF_MIND = BLOCKS.register("mind_staff",
            () -> new StaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD).lightLevel((BlockState) -> 7)));
    public static final RegistryObject<Item> STAFF_OF_MIND_ITEM = ITEMS.register(STAFF_OF_MIND.getId().getPath(),
            () -> new StaffItem(STAFF_OF_MIND.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(1200), StaffEffects::missile, false, MIND_BOTTLE.get()));

    public static final RegistryObject<Block> STAFF_OF_LIFE = BLOCKS.register("vital_staff",
            () -> new StaffBlock(BlockBehaviour.Properties.copy(Blocks.END_ROD).lightLevel((BlockState) -> 7)));
    public static final RegistryObject<Item> STAFF_OF_LIFE_ITEM = ITEMS.register(STAFF_OF_LIFE.getId().getPath(),
            () -> new StaffItem(STAFF_OF_LIFE.get(), new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(600), StaffEffects::living, true, VITAL_BOTTLE.get()));

    public static final RegistryObject<BlockEntityType<StaffBlockEntity>> STAFF_BE = TILES.register("staff_be",
            () -> BlockEntityType.Builder.of(StaffBlockEntity::new, STAFF_OF_LIGHT.get(), STAFF_OF_SOUL.get(), STAFF_OF_LIFE.get(), STAFF_OF_MIND.get(), STAFF_OF_BLAZE.get(), STAFF_OF_WARP.get()).build(null));

    // Register technical blocks.
    public static final RegistryObject<Block> ACTIVE_GOLD_FOAM = BLOCKS.register("active_gold_foam",
            () -> new ActiveGoldFoamBlock(BlockBehaviour.Properties.copy(Blocks.SLIME_BLOCK).jumpFactor(0.9F).sound(SoundType.WOOL)));

    public static final RegistryObject<BlockEntityType<ActiveFoamBlockEntity>> ACTIVE_GOLD_FOAM_BE = TILES.register("active_gold_foam_be",
            () -> BlockEntityType.Builder.of(ActiveFoamBlockEntity::new, ACTIVE_GOLD_FOAM.get()).build(null));

    public static final RegistryObject<Block> DISPLACED_BLOCK = BLOCKS.register("displaced_block",
            DisplacedBlock::new);

    public static final RegistryObject<BlockEntityType<DisplacedBlockEntity>> DISPLACED_BLOCK_BE = TILES.register("displaced_block_be",
            () -> BlockEntityType.Builder.of(DisplacedBlockEntity::new, DISPLACED_BLOCK.get()).build(null));

    public static final RegistryObject<Block> GLOWING_AIR = BLOCKS.register("glowing_air",
            () -> new AirLightBlock(BlockBehaviour.Properties.copy(Blocks.AIR).lightLevel((state) -> 15)));

    // Register items.
    public static final RegistryObject<Item> PURE_QUARTZ = ITEMS.register("quartz",
            () -> new Item(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> STARDUST_ITEM = ITEMS.register("stardust",
            () -> new StardustItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> SCROLL = ITEMS.register("scroll",
            () -> new AlchemyScrollItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)
                    .stacksTo(1).rarity(Rarity.RARE)));
    public static final RegistryObject<Item> LITMUS_PAPER = ITEMS.register("litmus_paper",
            () -> new LitmusPaperItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> QUARTZ_BOTTLE = ITEMS.register("quartz_bottle",
            () -> new QuartzBottleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> CRYSTAL_IRON = ITEMS.register("crystal_iron",
            () -> new CrystalIronItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).defaultDurability(64)));
    public static final RegistryObject<Item> PHANTOM_RESIDUE = ITEMS.register("phantom_residue",
            () -> new Item(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> SOUP = ITEMS.register("soup",
            () -> new SoupItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB).stacksTo(64).food((new FoodProperties.Builder().nutrition(7).saturationMod(0.5F)).build())));
    public static final RegistryObject<Item> SALT = ITEMS.register("salt",
            () -> new Item(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> MOTION_SALT = ITEMS.register("motion_salt",
            () -> new Item(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> FORCE_ROCK = ITEMS.register("force_rock",
            () -> new ForceRockItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> SECRET_SCALE = ITEMS.register("secret_scale",
            () -> new SecretScaleItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)));
    public static final RegistryObject<Item> DISPLACER = ITEMS.register("displacer",
            () -> new DisplacerItem(new Item.Properties().tab(ReactiveMod.CREATIVE_TAB)
                    .defaultDurability(350)));

    // Register mob effects
    public static final RegistryObject<MobEffect> NULL_GRAVITY = MOB_EFFECTS.register("no_gravity",
            () -> new HyperMobEffect(MobEffectCategory.NEUTRAL, 0xC0BF77)
                    .addAttributeModifier(ForgeMod.ENTITY_GRAVITY.get(), "fa350eb8-d5d3-4240-8342-dcc89c1693b9",
                            -1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    public static final RegistryObject<MobEffect> IMMOBILE = MOB_EFFECTS.register("immobility",
            () -> new HyperMobEffect(MobEffectCategory.NEUTRAL, 0x118066)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, "31861490-4050-11ee-be56-0242ac120002",
                            -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.FLYING_SPEED, "8712e51e-4050-11ee-be56-0242ac120002",
                            -1, AttributeModifier.Operation.MULTIPLY_TOTAL));

    // Register potions
    public static final RegistryObject<Potion> NULL_GRAVITY_POTION = POTIONS.register("no_gravity",
            () -> new Potion("no_gravity", new MobEffectInstance(NULL_GRAVITY.get(), 3000)));
    public static final RegistryObject<Potion> LONG_NULL_GRAVITY_POTION = POTIONS.register("no_gravity_long",
            () -> new Potion("no_gravity", new MobEffectInstance(NULL_GRAVITY.get(), 8000)));

    // Register particles
    public static final SimpleParticleType STARDUST_PARTICLE = new SimpleParticleType(false);
    public static final RegistryObject<ParticleType<SimpleParticleType>> STARDUST_PARTICLE_TYPE = PARTICLES.register("stardust",
            () -> STARDUST_PARTICLE);

    public static final SimpleParticleType RUNE_PARTICLE = new SimpleParticleType(false);
    public static final RegistryObject<ParticleType<SimpleParticleType>> RUNE_PARTICLE_TYPE = PARTICLES.register("runes",
            () -> RUNE_PARTICLE);

    public static final SimpleParticleType SMALL_RUNE_PARTICLE = new SimpleParticleType(false);
    public static final RegistryObject<ParticleType<SimpleParticleType>> SMALL_RUNE_PARTICLE_TYPE = PARTICLES.register("small_runes",
            () -> SMALL_RUNE_PARTICLE);

    public static final SimpleParticleType SMALL_BLACK_RUNE_PARTICLE = new SimpleParticleType(false);
    public static final RegistryObject<ParticleType<SimpleParticleType>> SMALL_BLACK_RUNE_PARTICLE_TYPE = PARTICLES.register("small_black_runes",
            () -> SMALL_BLACK_RUNE_PARTICLE);

    public static final SimpleParticleType ACID_BUBBLE_PARTICLE = new SimpleParticleType(false);
    public static final RegistryObject<ParticleType<SimpleParticleType>> ACID_BUBBLE_PARTICLE_TYPE = PARTICLES.register("acid_bubble",
            () -> ACID_BUBBLE_PARTICLE);

    // Register sound events.
    public static final RegistryObject<SoundEvent> ZAP_SOUND = SOUND_EVENTS.register("zap",
            () -> new SoundEvent(new ResourceLocation("reactive:zap")));

    // Register dummy blocks for the weird water types and the symbol eye render.
    public static final RegistryObject<Block> DUMMY_MAGIC_WATER = BLOCKS.register("magic_water",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WATER)));
    public static final RegistryObject<Block> DUMMY_NOISE_WATER = BLOCKS.register("noisy_water",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WATER)));
    public static final RegistryObject<Block> DUMMY_FAST_WATER = BLOCKS.register("fast_water",
            () -> new Block(BlockBehaviour.Properties.copy(Blocks.WATER)));

    //Register the recipe types and serializers.
    public static final RegistryObject<RecipeType<TransmuteRecipe>> TRANS_RECIPE_TYPE = RECIPE_TYPES.register("transmutation", () -> getRecipeType("transmutation"));
    public static final RegistryObject<RecipeSerializer<TransmuteRecipe>> TRANS_SERIALIZER = RECIPE_SERIALIZERS.register("transmutation", TransmuteRecipeSerializer::new);

    public static final RegistryObject<RecipeType<DissolveRecipe>> DISSOLVE_RECIPE_TYPE = RECIPE_TYPES.register("dissolve", () -> getRecipeType("dissolve"));
    public static final RegistryObject<RecipeSerializer<DissolveRecipe>> DISSOLVE_SERIALIZER = RECIPE_SERIALIZERS.register("dissolve", DissolveRecipeSerializer::new);

    public static final RegistryObject<RecipeType<PrecipitateRecipe>> PRECIPITATE_RECIPE_TYPE = RECIPE_TYPES.register("precipitation", () -> getRecipeType("precipitation"));
    public static final RegistryObject<RecipeSerializer<PrecipitateRecipe>> PRECIPITATE_SERIALIZER = RECIPE_SERIALIZERS.register("precipitation", PrecipitateRecipeSerializer::new);

    // ----------------------- METHODS ------------------------

    @SubscribeEvent
    public static void commonSetupHandler(FMLCommonSetupEvent evt){
        ((SymbolBlock) COPPER_SYMBOL.get()).setSymbolItem(COPPER_SYMBOL_ITEM.get());
        ((SymbolBlock) IRON_SYMBOL.get()).setSymbolItem(IRON_SYMBOL_ITEM.get());
        ((SymbolBlock) GOLD_SYMBOL.get()).setSymbolItem(GOLD_SYMBOL_ITEM.get());
        ((SymbolBlock) OCCULT_SYMBOL.get()).setSymbolItem(OCCULT_SYMBOL_ITEM.get());
        CriteriaTriggers.enqueue(evt);
        ComposterBlock.COMPOSTABLES.put(Registration.VERDANT_BOTTLE.get(), 1.0F);
        ComposterBlock.COMPOSTABLES.put(Registration.FLOWER_VINES_ITEM.get(), 0.4F);
        SecretScaleItem.registerGravPotions(evt);
        if(ModList.get().isLoaded("create")){
            ReactiveCreatePlugin.init();
        }
    }

    // Helper method for BlockItem registration
    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block, CreativeModeTab tab) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties().tab(tab)));
    }

    public static <B extends Block> RegistryObject<Item> fromBlock(RegistryObject<B> block) {
        return ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    // Helper method for Recipe Types.
    public static <T extends Recipe<?>> RecipeType<T> getRecipeType(final String id) {
        return new RecipeType<>()
        {
            public String toString() {
                return ReactiveMod.MODID + ":" + id;
            }
        };
    }
}
