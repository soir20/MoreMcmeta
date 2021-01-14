package io.github.soir20.moremcmeta.client;

import io.github.soir20.moremcmeta.MoreMcmeta;
import io.github.soir20.moremcmeta.client.renderer.texture.AnimatedTexture;
import io.github.soir20.moremcmeta.client.renderer.texture.TextureManagerWrapper;
import io.github.soir20.moremcmeta.resource.TextureReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.resources.IReloadableResourceManager;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLConstructModEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Handles client-relevant events.
 * @author soir20
 */
@Mod.EventBusSubscriber(modid = MoreMcmeta.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
@SuppressWarnings("unused")
public class ClientModEventSubscriber {

    /**
     * Adds the texture reload listener before resources are loaded for the first time.
     * @param event     the mod construction event
     */
    @SubscribeEvent
    public static void onPreInit(final FMLConstructModEvent event) {
        Minecraft minecraft = Minecraft.getInstance();
        IReloadableResourceManager manager =
                ((IReloadableResourceManager) minecraft.getResourceManager());
        TextureManagerWrapper texManager = new TextureManagerWrapper(minecraft::getTextureManager);
        Logger logger = LogManager.getLogger();

        manager.addReloadListener(new TextureReloadListener<>(MoreMcmeta.FOLDERS, texManager,
                AnimatedTexture::new, NativeImage::read, AnimationMetadataSection.SERIALIZER, logger));
        logger.debug("Added texture reload listener");
    }

}