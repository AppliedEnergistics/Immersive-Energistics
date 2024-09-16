package dev.technici4n.immeng.data;

import java.util.Set;

import com.google.common.collect.Iterables;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.loot.BlockLootSubProvider;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredHolder;

import dev.technici4n.immeng.ImmEng;

public class ImmEngBlockLoot extends BlockLootSubProvider {
    public ImmEngBlockLoot(HolderLookup.Provider registries) {
        super(Set.of(), FeatureFlags.VANILLA_SET, registries);
    }

    @Override
    protected void generate() {
        for (var block : getKnownBlocks()) {
            dropSelf(block);
        }
    }

    @Override
    protected Iterable<Block> getKnownBlocks() {
        return Iterables.transform(ImmEng.BLOCKS.getEntries(), DeferredHolder::get);
    }
}
