package dev.technici4n.immersivewiring.modules.ae2;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import dev.technici4n.immersivewiring.ImmersiveWiring;
import dev.technici4n.immersivewiring.data.IWBlockStatesProvider;

import appeng.api.ids.AEBlockIds;
import appeng.api.ids.AEItemIds;
import appeng.api.ids.AEPartIds;

public class AEModuleDatagen {
    public static void init(GatherDataEvent event) {
        var gen = event.getGenerator();

        gen.addProvider(event.includeClient(), new StatesProvider(gen.getPackOutput(), event.getExistingFileHelper()));

        gen.addProvider(event.includeServer(), new LootTableProvider(gen.getPackOutput(), Set.of(), List.of(
                new LootTableProvider.SubProviderEntry(BlockLoot::new, LootContextParamSets.BLOCK))));
        gen.addProvider(event.includeServer(), new Recipes(gen.getPackOutput()));
        gen.addProvider(event.includeServer(),
                new TagBlocks(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
    }

    private static class StatesProvider extends IWBlockStatesProvider {
        public StatesProvider(PackOutput output, ExistingFileHelper exFileHelper) {
            super(output, exFileHelper, "AE2");
        }

        @Override
        protected void registerStatesAndModels() {
            createAllRotatedBlock(AEModule.ME_CONNECTOR,
                    models().getExistingFile(ImmersiveWiring.id("block/connector_me")));
            createAllRotatedBlock(AEModule.ME_RELAY,
                    models().getExistingFile(ImmersiveWiring.id("block/connector_me_relay")));

            itemModels().basicItem(AEModule.ME_WIRE_COIL.asItem());
            itemModels().basicItem(AEModule.ME_WIRE_DENSE_COIL.asItem());
        }
    }

    private static class Recipes extends RecipeProvider implements IConditionBuilder {
        public Recipes(PackOutput packOutput) {
            super(packOutput);
        }

        @Override
        protected void buildRecipes(RecipeOutput baseOutput) {
            var output = baseOutput.withConditions(modLoaded("ae2"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEModule.ME_CONNECTOR, 4)
                    .pattern(" f ")
                    .pattern("FfF")
                    .pattern("FfF")
                    .define('f', BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL))
                    .define('F', BuiltInRegistries.ITEM.get(AEBlockIds.FLUIX_BLOCK))
                    .unlockedBy("has_fluix", has(BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL)))
                    .save(output, ImmersiveWiring.id("ae2/connector_me"));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEModule.ME_RELAY, 8)
                    .pattern(" f ")
                    .pattern("FfF")
                    .define('f', BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL))
                    .define('F', BuiltInRegistries.ITEM.get(AEBlockIds.FLUIX_BLOCK))
                    .unlockedBy("has_fluix", has(BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL)))
                    .save(output, ImmersiveWiring.id("ae2/connector_me_relay"));

            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEModule.ME_WIRE_COIL, 4)
                    .pattern(" c ")
                    .pattern("csc")
                    .pattern(" c ")
                    .define('c', BuiltInRegistries.ITEM.get(AEPartIds.CABLE_GLASS_TRANSPARENT))
                    .define('s', Tags.Items.RODS_WOODEN)
                    .unlockedBy("has_cable", has(BuiltInRegistries.ITEM.get(AEPartIds.CABLE_GLASS_TRANSPARENT)))
                    .save(output, ImmersiveWiring.id("ae2/wirecoil_me"));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, AEModule.ME_WIRE_DENSE_COIL, 4)
                    .pattern(" c ")
                    .pattern("csc")
                    .pattern(" c ")
                    .define('c', BuiltInRegistries.ITEM.get(AEPartIds.CABLE_DENSE_COVERED_TRANSPARENT))
                    .define('s', Tags.Items.RODS_WOODEN)
                    .unlockedBy("has_cable", has(BuiltInRegistries.ITEM.get(AEPartIds.CABLE_DENSE_COVERED_TRANSPARENT)))
                    .save(output, ImmersiveWiring.id("ae2/wirecoil_me_dense"));
        }
    }

    private static class BlockLoot extends BlockLootSubProvider {
        protected BlockLoot() {
            super(Set.of(), FeatureFlags.VANILLA_SET);
        }

        @Override
        protected void generate() {
            dropSelf(AEModule.ME_CONNECTOR.get());
            dropSelf(AEModule.ME_RELAY.get());
        }

        @Override
        protected Iterable<Block> getKnownBlocks() {
            return List.of(AEModule.ME_CONNECTOR.get(), AEModule.ME_RELAY.get());
        }
    }

    private static class TagBlocks extends BlockTagsProvider {
        public TagBlocks(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries,
                ExistingFileHelper existingFileHelper) {
            super(packOutput, registries, ImmersiveWiring.ID, existingFileHelper);
        }

        @Override
        protected void addTags(HolderLookup.Provider registries) {
            tag(BlockTags.MINEABLE_WITH_PICKAXE).add(AEModule.ME_CONNECTOR.get(), AEModule.ME_RELAY.get());
        }
    }
}
