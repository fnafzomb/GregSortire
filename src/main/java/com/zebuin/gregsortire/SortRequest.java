package com.zebuin.gregsortire;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import com.zebuin.gregsortire.sorting.SortHandler;

import java.util.*;
import java.util.function.Supplier;

public class SortRequest {

    public SortRequest() {}

    public static void encode(SortRequest packet, FriendlyByteBuf buf) {}

    public static SortRequest decode(FriendlyByteBuf buf) {
        return new SortRequest();
    }

    public static void handle(SortRequest packet, Supplier<NetworkEvent.Context> supplier) {
        NetworkEvent.Context context = supplier.get();

        context.enqueueWork(() -> SortHandler.sortFromContext(context));
        context.setPacketHandled(true);
    }
}
