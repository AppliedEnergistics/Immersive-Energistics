package dev.technici4n.immeng;

import java.util.List;
import java.util.Set;

import net.minecraft.core.registries.Registries;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import blusunrize.immersiveengineering.api.wires.WireApi;
import blusunrize.immersiveengineering.common.blocks.BlockItemIE;
import blusunrize.immersiveengineering.common.items.WireCoilItem;
import dev.technici4n.immeng.data.ImmEngBlockLoot;
import dev.technici4n.immeng.data.ImmEngBlockTags;
import dev.technici4n.immeng.data.ImmEngRecipes;
import dev.technici4n.immeng.data.ImmEngStatesProvider;

import appeng.api.AECapabilities;
import appeng.api.ids.AECreativeTabIds;

@Mod(ImmEng.ID)
public class ImmEng {
    public static final String ID = "immeng";

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(ID, path);
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister
            .create(Registries.BLOCK_ENTITY_TYPE, ID);

    @SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
    public static final DeferredBlock<MEConnectorBlock<MEConnectorBlockEntity>> ME_CONNECTOR = BLOCKS
            .register("connector_me", () -> new MEConnectorBlock<>(() -> ImmEng.ME_CONNECTOR_BE.get()));
    @SuppressWarnings({ "Convert2MethodRef", "FunctionalExpressionCanBeFolded" })
    public static final DeferredBlock<MEConnectorBlock<MEConnectorBlockEntity>> ME_RELAY = BLOCKS
            .register("connector_me_relay", () -> new MEConnectorBlock<>(() -> ImmEng.ME_RELAY_BE.get()));

    public static final DeferredItem<BlockItem> ME_CONNECTOR_ITEM = ITEMS.register("connector_me",
            () -> new BlockItemIE(ME_CONNECTOR.get()));
    public static final DeferredItem<BlockItem> ME_RELAY_ITEM = ITEMS.register("connector_me_relay",
            () -> new BlockItemIE(ME_RELAY.get()));

    public static final DeferredItem<WireCoilItem> ME_WIRE_COIL = ITEMS.register("wirecoil_me",
            () -> new WireCoilItem(MEWireType.NORMAL));
    public static final DeferredItem<WireCoilItem> ME_WIRE_DENSE_COIL = ITEMS.register("wirecoil_me_dense",
            () -> new WireCoilItem(MEWireType.DENSE));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEConnectorBlockEntity>> ME_CONNECTOR_BE = BLOCK_ENTITY_TYPES
            .register("connector_me",
                    () -> BlockEntityType.Builder.of(MEConnectorBlockEntity::new, ME_CONNECTOR.get()).build(null));
    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<MEConnectorBlockEntity>> ME_RELAY_BE = BLOCK_ENTITY_TYPES
            .register("connector_me_relay",
                    () -> BlockEntityType.Builder.of(MEConnectorBlockEntity::new, ME_RELAY.get()).build(null));

    public ImmEng(IEventBus modEventBus) {
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);

        WireApi.registerWireType(MEWireType.NORMAL);
        WireApi.registerWireType(MEWireType.DENSE);
        MELocalHandler.register(MELocalHandler.ID, MELocalHandler::new);

        modEventBus.addListener(ImmEng::initDatagen);

        modEventBus.addListener((RegisterCapabilitiesEvent event) -> {
            event.registerBlockEntity(AECapabilities.IN_WORLD_GRID_NODE_HOST, ME_CONNECTOR_BE.get(), (be, side) -> be);
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

    private static void initDatagen(GatherDataEvent event) {
        var gen = event.getGenerator();

        gen.addProvider(event.includeClient(),
                new ImmEngStatesProvider(gen.getPackOutput(), event.getExistingFileHelper()));

        gen.addProvider(event.includeServer(), new LootTableProvider(
                gen.getPackOutput(),
                Set.of(),
                List.of(
                        new LootTableProvider.SubProviderEntry(ImmEngBlockLoot::new, LootContextParamSets.BLOCK)),
                event.getLookupProvider()));
        gen.addProvider(event.includeServer(), new ImmEngRecipes(gen.getPackOutput(), event.getLookupProvider()));
        gen.addProvider(event.includeServer(),
                new ImmEngBlockTags(gen.getPackOutput(), event.getLookupProvider(), event.getExistingFileHelper()));
    }
}
