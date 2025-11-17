package events;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import com.zebuin.gregsortire.GregSortire;
import com.zebuin.gregsortire.SortRequest;

@Mod.EventBusSubscriber(modid = GregSortire.MOD_ID, value = Dist.CLIENT)
public class ClientEvents {

    @SubscribeEvent
    public static void onMousePress(ScreenEvent.MouseButtonPressed.Pre preEvent) {
        if (preEvent.getButton() != 2) return;
        if (!(preEvent.getScreen() instanceof AbstractContainerScreen<?>)) return;

        GregSortire.CHANNEL.sendToServer(new SortRequest());
    }
}
