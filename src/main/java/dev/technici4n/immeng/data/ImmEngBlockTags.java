package dev.technici4n.immeng.data;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.BlockTags;
import net.neoforged.neoforge.common.data.BlockTagsProvider;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import dev.technici4n.immeng.ImmEng;

public class ImmEngBlockTags extends BlockTagsProvider {
    public ImmEngBlockTags(PackOutput packOutput, CompletableFuture<HolderLookup.Provider> registries,
            ExistingFileHelper existingFileHelper) {
        super(packOutput, registries, ImmEng.ID, existingFileHelper);
    }

    @Override
    protected void addTags(HolderLookup.Provider registries) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(ImmEng.ME_CONNECTOR.get(), ImmEng.ME_RELAY.get());
    }
}
