package com.zebuin.gregsortire.sorting;

import com.lowdragmc.lowdraglib.gui.modular.ModularUIContainer;

import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.network.NetworkEvent;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SortHandler {

    public static void sortFromContext(NetworkEvent.Context context) {
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

        Map<StackKey, ItemStack> mergedStacks = new HashMap<>();
        for (ItemStack stack : stacks) {
            StackKey key = StackKey.of(stack);
            mergedStacks.merge(key, stack.copy(), (a, b) -> {
                a.setCount(a.getCount() + b.getCount());
                return a;
            });
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
    }
}
