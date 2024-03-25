package dev.technici4n.immeng.data;

import net.minecraft.data.PackOutput;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import dev.technici4n.immeng.ImmEng;

public class ImmEngStatesProvider extends ExtendedBlockStatesProvider {
    public ImmEngStatesProvider(PackOutput output, ExistingFileHelper exFileHelper) {
        super(output, exFileHelper);
    }

    @Override
    protected void registerStatesAndModels() {
        createAllRotatedBlock(ImmEng.ME_CONNECTOR,
                models().getExistingFile(ImmEng.id("block/connector_me")));
        createAllRotatedBlock(ImmEng.ME_RELAY,
                models().getExistingFile(ImmEng.id("block/connector_me_relay")));

        itemModels().basicItem(ImmEng.ME_WIRE_COIL.asItem());
        itemModels().basicItem(ImmEng.ME_WIRE_DENSE_COIL.asItem());
    }
}
