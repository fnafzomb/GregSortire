package com.zebuin.gregsortire;

import com.gregtechceu.gtceu.api.registry.registrate.GTRegistrate;

import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(GregSortire.MOD_ID)
@SuppressWarnings("removal")
public class GregSortire {

    public static final String MOD_ID = "gregsortire";
    public static final Logger LOGGER = LogManager.getLogger();
    public static GTRegistrate REGISTRATE = GTRegistrate.create(GregSortire.MOD_ID);

    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(id("main"))
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .networkProtocolVersion(() -> PROTOCOL_VERSION)
            .simpleChannel();

    private static int packetId = 0;

    public GregSortire() {
        IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();

        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::clientSetup);

        // Most other events are fired on Forge's bus.
        // If we want to use annotations to register event listeners,
        // we need to register our object like this!
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        CHANNEL.registerMessage(packetId++, SortRequest.class, SortRequest::encode, SortRequest::decode,
                SortRequest::handle);
    }

    private void clientSetup(final FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.addListener((ScreenEvent.MouseButtonPressed.Pre preEvent) -> {
            if (preEvent.getButton() != 2) return;
            if (!(preEvent.getScreen() instanceof AbstractContainerScreen<?> containerScreen)) return;

            CHANNEL.sendToServer(new SortRequest());
        });
    }

    /**
     * Create a ResourceLocation in the format "modid:path"
     *
     * @param path
     * @return ResourceLocation with the namespace of your mod
     */
    public static ResourceLocation id(String path) {
        return new ResourceLocation(MOD_ID, path);
    }
}
