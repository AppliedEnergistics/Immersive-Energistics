package dev.technici4n.immeng;

import org.jetbrains.annotations.NotNull;

import net.minecraft.world.item.ItemStack;

import blusunrize.immersiveengineering.api.wires.Connection;
import blusunrize.immersiveengineering.api.wires.WireType;

public class MEWireType extends WireType {
    public static final MEWireType NORMAL = new MEWireType(false);
    public static final MEWireType DENSE = new MEWireType(true);

    private final boolean dense;

    public MEWireType(boolean dense) {
        this.dense = dense;
    }

    @Override
    public String getUniqueName() {
        return dense ? "me_dense" : "me";
    }

    @Override
    public int getColour(Connection connection) {
        return dense ? 0x4E3C95 : 0x915DCD;
    }

    @Override
    public double getSlack() {
        return dense ? 1.003 : 1.005;
    }

    @Override
    public int getMaxLength() {
        return dense ? 32 : 16;
    }

    @Override
    public ItemStack getWireCoil(Connection connection) {
        return dense ? ImmEng.ME_WIRE_DENSE_COIL.toStack() : ImmEng.ME_WIRE_COIL.toStack();
    }

    @Override
    public double getRenderDiameter() {
        return dense ? 0.0625 : 0.03125;
    }

    @NotNull
    @Override
    public String getCategory() {
        return "me";
    }
}
