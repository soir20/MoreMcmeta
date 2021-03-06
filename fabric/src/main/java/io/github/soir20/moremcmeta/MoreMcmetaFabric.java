/*
 * MoreMcmeta is a Minecraft mod expanding texture animation capabilities.
 * Copyright (C) 2021 soir20
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation version 3 of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package io.github.soir20.moremcmeta;

import io.github.soir20.moremcmeta.client.event.ResourceManagerInitializedCallback;
import io.github.soir20.moremcmeta.client.mixin.MinecraftAccessor;
import io.github.soir20.moremcmeta.client.mixin.TextureManagerAccessor;
import io.github.soir20.moremcmeta.client.resource.SizeSwappingResourceManager;
import io.github.soir20.moremcmeta.client.resource.TextureLoader;
import io.github.soir20.moremcmeta.client.texture.EventDrivenTexture;
import io.github.soir20.moremcmeta.client.texture.LazyTextureManager;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.resource.SimpleResourceReloadListener;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.Logger;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * The main mod class and entrypoint for Fabric.
 * @author soir20
 */
@SuppressWarnings("unused")
public class MoreMcmetaFabric extends MoreMcmeta implements ClientModInitializer {

    /**
     * Begins the startup process on the client.
     */
    @Override
    public void onInitializeClient() {
        start();
    }

    /**
     * Gets the action that should be executed to unregister a texture on Fabric.
     * @return the action that will unregister textures
     */
    @Override
    public BiConsumer<TextureManager, ResourceLocation> getUnregisterAction() {
        return (manager, location) -> {
            TextureManagerAccessor accessor = (TextureManagerAccessor) manager;
            accessor.getByPath().remove(location);
            manager.release(location);
        };
    }

    /**
     * Executes a callback when the vanilla resource manager is initialized in Fabric.
     * @param callback      the callback to execute
     */
    @Override
    public void onResourceManagerInitialized(Consumer<Minecraft> callback) {
        ResourceManagerInitializedCallback.EVENT.register(callback::accept);
    }

    /**
     * Creates a new reload listener that loads and queues animated textures for Fabric.
     * @param texManager    manages prebuilt textures
     * @param loader        loads textures from resource packs
     * @param logger        a logger to write output
     * @return a reload listener that loads and queues animated textures
     */
    @Override
    public PreparableReloadListener makeListener(
            LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager,
            TextureLoader<EventDrivenTexture.Builder> loader, Logger logger) {

        return new SimpleResourceReloadListener<Map<ResourceLocation, EventDrivenTexture.Builder>>() {
            private final Map<ResourceLocation, EventDrivenTexture.Builder> LAST_TEXTURES_ADDED = new HashMap<>();

            @Override
            public ResourceLocation getFabricId() {
                return new ResourceLocation("moremcmeta", "texture_reload_listener");
            }

            @Override
            public CompletableFuture<Map<ResourceLocation, EventDrivenTexture.Builder>> load(ResourceManager manager,
                                                                                             ProfilerFiller profiler,
                                                                                             Executor executor) {
                return CompletableFuture.supplyAsync(() -> {
                    Map<ResourceLocation, EventDrivenTexture.Builder> textures = new HashMap<>();
                    textures.putAll(loader.load(manager, "textures"));
                    textures.putAll(loader.load(manager, "optifine"));
                    return textures;
                }, executor);
            }

            @Override
            public CompletableFuture<Void> apply(Map<ResourceLocation, EventDrivenTexture.Builder> data,
                                                 ResourceManager manager, ProfilerFiller profiler, Executor executor) {
                return CompletableFuture.runAsync(() -> {
                    LAST_TEXTURES_ADDED.keySet().forEach(texManager::unregister);
                    LAST_TEXTURES_ADDED.clear();
                    LAST_TEXTURES_ADDED.putAll(data);

                    data.forEach(texManager::register);
                }, executor);
            }
        };
    }

    /**
     * Replaces the {@link net.minecraft.server.packs.resources.SimpleReloadableResourceManager}
     * with the mod's custom one in Fabric.
     * @param client        the Minecraft client
     * @param manager       the manager that should be made Minecraft's resource manager
     * @param logger        a logger to write output
     */
    @Override
    public void replaceResourceManager(Minecraft client, SizeSwappingResourceManager manager, Logger logger) {
        MinecraftAccessor accessor = (MinecraftAccessor) client;
        accessor.setResourceManager(manager);
    }

    /**
     * Begins ticking the {@link LazyTextureManager} on Fabric.
     * @param texManager        the manager to begin ticking
     */
    @Override
    public void startTicking(LazyTextureManager<EventDrivenTexture.Builder, EventDrivenTexture> texManager) {
        ClientTickEvents.START_CLIENT_TICK.register((client) -> texManager.tick());
    }

}
