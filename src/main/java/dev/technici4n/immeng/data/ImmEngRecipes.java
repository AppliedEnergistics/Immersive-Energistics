package dev.technici4n.immeng.data;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.conditions.IConditionBuilder;

import dev.technici4n.immeng.ImmEng;

import appeng.api.ids.AEBlockIds;
import appeng.api.ids.AEItemIds;
import appeng.api.ids.AEPartIds;

public class ImmEngRecipes extends RecipeProvider implements IConditionBuilder {
    public ImmEngRecipes(PackOutput packOutput) {
        super(packOutput);
    }

    @Override
    protected void buildRecipes(RecipeOutput output) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ImmEng.ME_CONNECTOR, 4)
                .pattern(" f ")
                .pattern("FfF")
                .pattern("FfF")
                .define('f', BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL))
                .define('F', BuiltInRegistries.ITEM.get(AEBlockIds.FLUIX_BLOCK))
                .unlockedBy("has_fluix", has(BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL)))
                .save(output, ImmEng.id("connector_me"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ImmEng.ME_RELAY, 8)
                .pattern(" f ")
                .pattern("FfF")
                .define('f', BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL))
                .define('F', BuiltInRegistries.ITEM.get(AEBlockIds.FLUIX_BLOCK))
                .unlockedBy("has_fluix", has(BuiltInRegistries.ITEM.get(AEItemIds.FLUIX_CRYSTAL)))
                .save(output, ImmEng.id("connector_me_relay"));

        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ImmEng.ME_WIRE_COIL, 4)
                .pattern(" c ")
                .pattern("csc")
                .pattern(" c ")
                .define('c', BuiltInRegistries.ITEM.get(AEPartIds.CABLE_GLASS_TRANSPARENT))
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_cable", has(BuiltInRegistries.ITEM.get(AEPartIds.CABLE_GLASS_TRANSPARENT)))
                .save(output, ImmEng.id("wirecoil_me"));
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ImmEng.ME_WIRE_DENSE_COIL, 4)
                .pattern(" c ")
                .pattern("csc")
                .pattern(" c ")
                .define('c', BuiltInRegistries.ITEM.get(AEPartIds.CABLE_DENSE_COVERED_TRANSPARENT))
                .define('s', Tags.Items.RODS_WOODEN)
                .unlockedBy("has_cable", has(BuiltInRegistries.ITEM.get(AEPartIds.CABLE_DENSE_COVERED_TRANSPARENT)))
                .save(output, ImmEng.id("wirecoil_me_dense"));
    }
}
