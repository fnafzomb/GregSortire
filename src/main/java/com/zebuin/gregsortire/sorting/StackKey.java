package com.zebuin.gregsortire.sorting;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.Objects;

public record StackKey(Item item, CompoundTag tag) {

    public static StackKey of(ItemStack itemStack) {
        return new StackKey(itemStack.getItem(), itemStack.getTag());
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) return true;
        if (!(object instanceof StackKey otherKey)) return false;

        return item == otherKey.item && Objects.equals(tag, otherKey.tag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(item, tag);
    }
}
