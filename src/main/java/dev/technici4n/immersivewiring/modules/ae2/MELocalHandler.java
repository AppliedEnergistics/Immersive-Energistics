package dev.technici4n.immersivewiring.modules.ae2;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.ConnectionPoint;
import blusunrize.immersiveengineering.api.wires.GlobalWireNetwork;
import blusunrize.immersiveengineering.api.wires.IImmersiveConnectable;
import blusunrize.immersiveengineering.api.wires.LocalWireNetwork;
import blusunrize.immersiveengineering.api.wires.localhandlers.LocalNetworkHandler;
import dev.technici4n.immersivewiring.ImmersiveWiring;

public class MELocalHandler extends LocalNetworkHandler {
    public static final ResourceLocation ID = ImmersiveWiring.id("me");

    protected MELocalHandler(LocalWireNetwork net, GlobalWireNetwork global) {
        super(net, global);
    }

    @Override
    public LocalNetworkHandler merge(LocalNetworkHandler localNetworkHandler) {
        return this;
    }

    @Override
    public void onConnectorLoaded(ConnectionPoint connectionPoint, IImmersiveConnectable iImmersiveConnectable) {
        // Handled by the connector
    }

    @Override
    public void onConnectorUnloaded(BlockPos blockPos, IImmersiveConnectable iImmersiveConnectable) {
        // Handled by the connector
    }

    @Override
    public void onConnectorRemoved(BlockPos blockPos, IImmersiveConnectable iImmersiveConnectable) {
        // Handled by the connector
    }

    @Override
    public void onConnectionAdded(Connection connection) {
        if (localNet.getConnector(connection.getEndA()) instanceof MEConnectorBlockEntity connectorA
                && !connectorA.getLevelNonnull().isClientSide()) {
            connectorA.loadConnectionOnA(connection, localNet); // Will also try to connect with B
        }

    }

    @Override
    public void onConnectionRemoved(Connection connection) {
        // Remove wire node (stored in end A) if loaded
        if (localNet.getConnector(connection.getEndA()) instanceof MEConnectorBlockEntity connectorA
                && !connectorA.getLevelNonnull().isClientSide()) {
            var wireNode = connectorA.wireGridNodes.remove(connection.getEndB());
            if (wireNode != null) {
                wireNode.destroy();
            }
            connectorA.setChanged();
        }
    }
}
