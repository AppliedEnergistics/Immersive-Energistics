package dev.technici4n.immeng;

import java.util.function.Supplier;

import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.common.blocks.generic.ConnectorBlock;

public class MEConnectorBlock<T extends BlockEntity & IImmersiveConnectable> extends ConnectorBlock<T> {
    public MEConnectorBlock(Supplier<BlockEntityType<T>> type) {
        super(PROPERTIES.get(), type);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(IEProperties.FACING_ALL, BlockStateProperties.WATERLOGGED);
    }

    public void neighborChanged(BlockState state, Level world, BlockPos pos, Block block, BlockPos fromPos,
            boolean isMoving) {
        super.neighborChanged(state, world, pos, block, fromPos, isMoving);
        BlockEntity te = world.getBlockEntity(pos);
        if (te instanceof MEConnectorBlockEntity connector) {
            if (world.isEmptyBlock(pos.relative(connector.getFacing()))) {
                popResource(world, pos, new ItemStack(this));
                connector.getLevelNonnull().removeBlock(pos, false);
            }
        }
    }
}
