package com.zebuin.gregsortire;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

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

        context.enqueueWork(() -> {
            ServerPlayer player = context.getSender();
            if (player == null) return;
            if (!(player.containerMenu instanceof ModularUIContainer menu)) return;

            List<Slot> targetSlots = menu.slots.stream()
                    .filter(slot -> slot.container != player.getInventory())
                    .toList();
            if (targetSlots.isEmpty()) return;

            List<ItemStack> stacks = targetSlots.stream()
                    .map(Slot::getItem)
                    .filter(stack -> !stack.isEmpty())
                    .map(ItemStack::copy)
                    .toList();
            if (stacks.isEmpty()) return;

            Map<String, ItemStack> mergedStacks = new HashMap<>();
            for (ItemStack stack : stacks) {
                String key = stack.getDescriptionId();
                if (mergedStacks.containsKey(key)) {
                    ItemStack currentStack = mergedStacks.get(key);
                    int newCount = currentStack.getCount() + stack.getCount();

                    currentStack.setCount(newCount);
                } else {
                    mergedStacks.put(key, stack.copy());
                }
            }

            List<ItemStack> sortedStacks = mergedStacks.values().stream()
                    .sorted(Comparator
                            .comparingInt((ItemStack stack) -> -stack.getCount())
                            .thenComparing((ItemStack stack) -> stack.getDisplayName().getString()))
                    .toList();

            // распределить по стакам учитывая их размеры
            int slotIndex = 0;
            for (ItemStack combinedStack : sortedStacks) {
                int remainingCount = combinedStack.getCount();
                int max = combinedStack.getMaxStackSize();

                while (remainingCount > 0 && slotIndex < targetSlots.size()) {
                    int currentCount = Math.min(max, remainingCount);
                    ItemStack currentStack = combinedStack.copy();
                    currentStack.setCount(currentCount);

                    Slot slot = targetSlots.get(slotIndex);
                    slot.set(currentStack);
                    slot.container.setChanged();

                    remainingCount -= currentCount;
                    slotIndex++;
                }
            }

            // заполнить оставшиеся слоты воздухом
            while (slotIndex < targetSlots.size()) {
                Slot slot = targetSlots.get(slotIndex);
                slot.set(ItemStack.EMPTY);
                slot.container.setChanged();
                slotIndex++;
            }

            menu.broadcastChanges();
        });

        context.setPacketHandled(true);
    }
}
