package dev.technici4n.immersivewiring.modules.ae2;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

import blusunrize.immersiveengineering.api.IEProperties;
import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.WireType;
import blusunrize.immersiveengineering.common.blocks.IEBlockInterfaces;
import blusunrize.immersiveengineering.common.blocks.PlacementLimitation;
import blusunrize.immersiveengineering.common.blocks.generic.ImmersiveConnectableBlockEntity;
import blusunrize.immersiveengineering.common.blocks.metal.EnergyConnectorBlockEntity;

import appeng.api.networking.GridFlags;
import appeng.api.networking.GridHelper;
import appeng.api.networking.IGridNode;
import appeng.api.networking.IInWorldGridNodeHost;
import appeng.api.networking.IManagedGridNode;
import appeng.api.util.AECableType;

public class MEConnectorBlockEntity extends ImmersiveConnectableBlockEntity
        implements IEBlockInterfaces.IStateBasedDirectional, IEBlockInterfaces.IBlockBounds, IInWorldGridNodeHost {
    private final boolean relay;
    /**
     * The grid node of the connector itself.
     */
    final IManagedGridNode gridNode = GridHelper.createManagedNode(this, (nodeOwner, node) -> {
        this.setChanged();
    })
            .setFlags(GridFlags.DENSE_CAPACITY)
            .setIdlePowerUsage(0);

    /**
     * Grid nodes that serve as intermediate wire connections. These are always added to "end A" of the
     * {@link Connection}. The map is from the connection point of "end B" to the managed grid node.
     */
    final Map<ConnectionPoint, IManagedGridNode> wireGridNodes = new LinkedHashMap<>();

    IManagedGridNode createWireNode() {
        return GridHelper.createManagedNode(this, (nodeOwner, node) -> this.setChanged())
                .setIdlePowerUsage(0);
    }

    public MEConnectorBlockEntity(BlockPos pos, BlockState state) {
        super(state.is(AEModule.ME_RELAY) ? AEModule.ME_RELAY_BE.get() : AEModule.ME_CONNECTOR_BE.get(), pos, state);
        this.relay = state.is(AEModule.ME_RELAY);

        if (this.relay) {
            gridNode.setVisualRepresentation(AEModule.ME_RELAY_ITEM);
        } else {
            gridNode.setVisualRepresentation(AEModule.ME_CONNECTOR_ITEM);
            gridNode.setInWorldNode(true);
        }

        setBlockState(state); // update exposed sides
    }

    @Override
    public boolean canConnectCable(WireType wireType, ConnectionPoint connectionPoint, Vec3i vec3i) {
        if (this.relay) {
            return true;
        } else {
            LocalWireNetwork local = this.globalNet.getNullableLocalNet(new ConnectionPoint(this.worldPosition, 0));
            return local == null || local.getConnections(this.worldPosition).isEmpty();
        }
    }

    @Override
    public Vec3 getConnectionOffset(ConnectionPoint connectionPoint, ConnectionPoint connectionPoint1,
            WireType wireType) {
        Direction side = getFacing().getOpposite();
        double conRadius = wireType.getRenderDiameter() / 2;
        if (!relay) {
            conRadius += 0.25;
        }
        return new Vec3(.5 - conRadius * side.getStepX(), .5 - conRadius * side.getStepY(),
                .5 - conRadius * side.getStepZ());
    }

    @NotNull
    @Override
    public VoxelShape getBlockBounds(@Nullable CollisionContext collisionContext) {
        if (relay) {
            return EnergyConnectorBlockEntity.getConnectorBounds(getFacing(), 0.5f);
        } else {
            return EnergyConnectorBlockEntity.getConnectorBounds(getFacing(), 0.25f);
        }
    }

    @Override
    public Property<Direction> getFacingProperty() {
        return IEProperties.FACING_ALL;
    }

    @Override
    public PlacementLimitation getFacingLimitation() {
        return PlacementLimitation.SIDE_CLICKED;
    }

    @Override
    public boolean mirrorFacingOnPlacement(LivingEntity placer) {
        return true;
    }

    @Override
    public void load(CompoundTag nbtIn) {
        super.load(nbtIn);

        this.gridNode.loadFromNBT(nbtIn);

        ListTag wireNodes = nbtIn.getList("wirenodes", Tag.TAG_COMPOUND);
        for (int i = 0; i < wireNodes.size(); ++i) {
            var entry = wireNodes.getCompound(i);
            ConnectionPoint cp = new ConnectionPoint(entry);
            var node = createWireNode();
            node.loadFromNBT(entry);
            this.wireGridNodes.put(cp, node);
        }
    }

    @Override
    protected void saveAdditional(CompoundTag nbt) {
        super.saveAdditional(nbt);

        this.gridNode.saveToNBT(nbt);

        ListTag wireNodes = new ListTag();
        for (var entry : wireGridNodes.entrySet()) {
            var tag = entry.getKey().createTag();
            entry.getValue().saveToNBT(tag);
            wireNodes.add(tag);
        }
        nbt.put("wirenodes", wireNodes);
    }

    private ConnectionPoint getConnectionPoint() {
        return new ConnectionPoint(worldPosition, 0);
    }

    void loadConnectionOnA(Connection connection, LocalWireNetwork localNet) {
        if (!Objects.equals(connection.getEndA(), getConnectionPoint())) {
            throw new IllegalArgumentException("Cannot call loadConnection on B end.");
        }

        var point = connection.getEndB();

        var wireNode = wireGridNodes.get(point);
        if (wireNode == null) {
            // Add node if it doesn't exist
            wireGridNodes.put(point, wireNode = createWireNode());
        } else if (wireNode.isReady()) {
            // Node already exists, do nothing.
            // Probably means that the network is getting split, and the connection is being re-added.
            return;
        }
        // Update flags and active item based on wire
        if (connection.type == MEWireType.DENSE) {
            wireNode.setFlags(GridFlags.DENSE_CAPACITY);
            wireNode.setVisualRepresentation(AEModule.ME_WIRE_DENSE_COIL);
        } else {
            wireNode.setFlags(GridFlags.PREFERRED);
            wireNode.setVisualRepresentation(AEModule.ME_WIRE_COIL);
        }
        wireNode.create(getLevelNonnull(), null);

        // Connect between A and wire
        GridHelper.createConnection(gridNode.getNode(), wireNode.getNode());

        // If B end is loaded and initialized, connect between wire and B
        if (localNet.getConnector(connection.getEndB()) instanceof MEConnectorBlockEntity connectorB) {
            var otherNode = connectorB.gridNode.getNode();

            if (otherNode != null) {
                GridHelper.createConnection(wireNode.getNode(), otherNode);
            }
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();

        if (level.isClientSide()) {
            return;
        }

        this.gridNode.create(level, worldPosition);

        var connectionPoint = getConnectionPoint();
        var localNet = GlobalWireNetwork.getNetwork(level).getLocalNet(worldPosition);

        // PROCESS CONNECTIONS ON END A
        var connections = localNet.getConnections(connectionPoint);
        var connectedPoints = new HashSet<>();

        // Add or refresh all connections
        for (var connection : connections) {
            if (!connection.getEndA().equals(connectionPoint)) {
                continue;
            }

            loadConnectionOnA(connection, localNet);
            var point = connection.getEndB();
            connectedPoints.add(point);
        }

        // Remove connections that don't exist anymore
        wireGridNodes.keySet().removeIf(key -> !connectedPoints.contains(key));

        // IF ON END B, TRY TO CONNECT WITH WIRE NODE STORED IN END A
        for (var connection : connections) {
            if (!connection.getEndB().equals(connectionPoint)) {
                continue;
            }

            if (localNet.getConnector(connection.getEndA()) instanceof MEConnectorBlockEntity connectorA) {
                var wireNode = connectorA.wireGridNodes.get(connection.getEndB());

                if (wireNode != null && wireNode.isReady()) {
                    GridHelper.createConnection(wireNode.getNode(), gridNode.getNode());
                }
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        this.gridNode.destroy();
        this.wireGridNodes.values().forEach(IManagedGridNode::destroy);
    }

    @Override
    public void setRemovedIE() {
        super.setRemovedIE();
        this.gridNode.destroy();
        this.wireGridNodes.values().forEach(IManagedGridNode::destroy);
    }

    @Override
    @Nullable
    public IGridNode getGridNode(Direction dir) {
        return !relay && dir == getFacing() ? gridNode.getNode() : null;
    }

    @Override
    public AECableType getCableConnectionType(Direction dir) {
        return AECableType.DENSE_SMART;
    }

    @Override
    public void setBlockState(BlockState newState) {
        super.setBlockState(newState);

        if (level == null || !level.isClientSide()) {
            if (!this.relay) {
                this.gridNode.setExposedOnSides(Set.of(getFacing()));
            }
        }
    }

    @Override
    public Collection<ResourceLocation> getRequestedHandlers() {
        return List.of(MELocalHandler.ID);
    }
}
