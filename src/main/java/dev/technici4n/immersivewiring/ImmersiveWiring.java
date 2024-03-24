package dev.technici4n.immersivewiring;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.registries.DeferredRegister;

import dev.technici4n.immersivewiring.modules.ae2.AEModule;

@Mod(ImmersiveWiring.ID)
public class ImmersiveWiring {
    public static final String ID = "immersivewiring";

    public static ResourceLocation id(String path) {
        return new ResourceLocation(ID, path);
    }

    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(ID);
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(ID);
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITY_TYPES = DeferredRegister
            .create(Registries.BLOCK_ENTITY_TYPE, ID);

    public ImmersiveWiring(IEventBus modEventBus) {
        if (ModList.get().isLoaded("ae2")) {
            AEModule.init(modEventBus);
        }

        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        BLOCK_ENTITY_TYPES.register(modEventBus);
    }
}
