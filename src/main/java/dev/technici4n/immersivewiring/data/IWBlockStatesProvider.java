package dev.technici4n.immersivewiring.data;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import net.minecraft.core.Direction;
import net.minecraft.data.PackOutput;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.neoforge.client.model.generators.BlockStateProvider;
import net.neoforged.neoforge.client.model.generators.ConfiguredModel;
import net.neoforged.neoforge.client.model.generators.ModelFile;
import net.neoforged.neoforge.client.model.generators.VariantBlockStateBuilder;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import blusunrize.immersiveengineering.api.IEProperties;
import dev.technici4n.immersivewiring.ImmersiveWiring;

public abstract class IWBlockStatesProvider extends BlockStateProvider {
    private final String module;

    public IWBlockStatesProvider(PackOutput output, ExistingFileHelper exFileHelper, String module) {
        super(output, ImmersiveWiring.ID, exFileHelper);
        this.module = module;
    }

    @Override
    public final String getName() {
        return super.getName() + " (" + module + ")";
    }

    // Mostly copy-pasted from IE
    protected void createAllRotatedBlock(Supplier<? extends Block> block, ModelFile model) {
        createAllRotatedBlock(block, $ -> model, List.of());
    }

    protected void createAllRotatedBlock(Supplier<? extends Block> block,
            Function<VariantBlockStateBuilder.PartialBlockstate, ModelFile> model, List<Property<?>> additionalProps) {
        createRotatedBlock(block, model, IEProperties.FACING_ALL, additionalProps, 90, 0);
    }

    protected void createRotatedBlock(Supplier<? extends Block> block, ModelFile model, Property<Direction> facing,
            List<Property<?>> additionalProps, int offsetRotX, int offsetRotY) {
        createRotatedBlock(block, $ -> model, facing, additionalProps, offsetRotX, offsetRotY);
    }

    protected void createRotatedBlock(Supplier<? extends Block> block,
            Function<VariantBlockStateBuilder.PartialBlockstate, ModelFile> model, Property<Direction> facing,
            List<Property<?>> additionalProps, int offsetRotX, int offsetRotY) {
        VariantBlockStateBuilder stateBuilder = getVariantBuilder(block.get());
        forEachState(stateBuilder.partialState(), additionalProps, state -> {
            ModelFile modelLoc = model.apply(state);
            for (Direction d : facing.getPossibleValues()) {
                int x;
                int y;
                switch (d) {
                    case UP -> {
                        x = 90;
                        y = 0;
                    }
                    case DOWN -> {
                        x = -90;
                        y = 0;
                    }
                    default -> {
                        y = getAngle(d, offsetRotY);
                        x = 0;
                    }
                }
                state.with(facing, d).setModels(new ConfiguredModel(modelLoc, x + offsetRotX, y, false));
            }
        });
    }

    protected int getAngle(Direction dir, int offset) {
        return (int) ((dir.toYRot() + offset) % 360);
    }

    public static <T extends Comparable<T>> void forEach(VariantBlockStateBuilder.PartialBlockstate base,
            Property<T> prop,
            List<Property<?>> remaining, Consumer<VariantBlockStateBuilder.PartialBlockstate> out) {
        for (T value : prop.getPossibleValues())
            forEachState(base, remaining, map -> {
                map = map.with(prop, value);
                out.accept(map);
            });
    }

    public static void forEachState(VariantBlockStateBuilder.PartialBlockstate base, List<Property<?>> props,
            Consumer<VariantBlockStateBuilder.PartialBlockstate> out) {
        if (props.size() > 0) {
            List<Property<?>> remaining = props.subList(1, props.size());
            Property<?> main = props.get(0);
            forEach(base, main, remaining, out);
        } else
            out.accept(base);
    }
}
