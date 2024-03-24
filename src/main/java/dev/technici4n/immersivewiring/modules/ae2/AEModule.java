package dev.technici4n.immersivewiring.modules.ae2;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.items.WireCoilItem;
import dev.technici4n.immersivewiring.ImmersiveWiring;

import appeng.api.ids.AECreativeTabIds;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.core.AppEng;

public class AEModule {
    @SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
    static final DeferredBlock<MEConnectorBlock<MEConnectorBlockEntity>> ME_CONNECTOR = ImmersiveWiring.BLOCKS
            .register("connector_me", () -> new MEConnectorBlock<>(() -> AEModule.ME_CONNECTOR_BE.get()));
    @SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
    static final DeferredBlock<MEConnectorBlock<MEConnectorBlockEntity>> ME_RELAY = ImmersiveWiring.BLOCKS
            .register("connector_me_relay", () -> new MEConnectorBlock<>(() -> AEModule.ME_RELAY_BE.get()));

    static final DeferredItem<BlockItem> ME_CONNECTOR_ITEM = ImmersiveWiring.ITEMS.register("connector_me",
            () -> new BlockItemIE(ME_CONNECTOR.get()));
    static final DeferredItem<BlockItem> ME_RELAY_ITEM = ImmersiveWiring.ITEMS.register("connector_me_relay",
            () -> new BlockItemIE(ME_RELAY.get()));

    static final DeferredItem<WireCoilItem> ME_WIRE_COIL = ImmersiveWiring.ITEMS.register("wirecoil_me",
            () -> new WireCoilItem(MEWireType.NORMAL));
    static final DeferredItem<WireCoilItem> ME_WIRE_DENSE_COIL = ImmersiveWiring.ITEMS.register("wirecoil_me_dense",
            () -> new WireCoilItem(MEWireType.DENSE));

    static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEConnectorBlockEntity>> ME_CONNECTOR_BE = ImmersiveWiring.BLOCK_ENTITY_TYPES
            .register("connector_me",
                    () -> BlockEntityType.Builder.of(MEConnectorBlockEntity::new, ME_CONNECTOR.get()).build(null));
    static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEConnectorBlockEntity>> ME_RELAY_BE = ImmersiveWiring.BLOCK_ENTITY_TYPES
            .register("connector_me_relay",
                    () -> BlockEntityType.Builder.of(MEConnectorBlockEntity::new, ME_RELAY.get()).build(null));

    static final BlockCapability<IInWorldGridNodeHost, @Nullable Direction> IN_WORLD_GRID_NODE_HOST = BlockCapability
            .createSided(AppEng.makeId("inworld_gridnode_host"), IInWorldGridNodeHost.class);

    public static void init(IEventBus modEventBus) {
        WireApi.registerWireType(MEWireType.NORMAL);
        WireApi.registerWireType(MEWireType.DENSE);
        MELocalHandler.register(MELocalHandler.ID, MELocalHandler::new);

        modEventBus.addListener(AEModuleDatagen::init);

        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(IN_WORLD_GRID_NODE_HOST, ME_CONNECTOR_BE.get(), (be, side) -> be);
        });

        modEventBus.addListener((BuildCreativeModeTabContentsEvent event) -> {
            if (event.getTabKey() == AECreativeTabIds.MAIN) {
                event.accept(ME_WIRE_COIL);
                event.accept(ME_WIRE_DENSE_COIL);
                event.accept(ME_CONNECTOR_ITEM);
                event.accept(ME_RELAY_ITEM);
            }
        });
    }
}
