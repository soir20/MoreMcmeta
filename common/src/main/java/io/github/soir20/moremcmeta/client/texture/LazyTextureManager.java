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

package io.github.soir20.moremcmeta.client.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;

import static java.util.Objects.requireNonNull;

/**
 * Finishes loaded textures lazily with upload components according to the provided {@link IFinisher}.
 * @param <I> type of texture builders (input)
 * @param <O> type of textures (output)
 * @author soir20
 */
public class LazyTextureManager<I, O extends AbstractTexture & CustomTickable> implements IManager<I> {
    private final IManager<AbstractTexture> DELEGATE;
    private final Map<ResourceLocation, CustomTickable> ANIMATED_TEXTURES;
    private final IFinisher<I, O> FINISHER;

    /**
     * Creates the TextureManagerWrapper.
     * @param delegate      Minecraft's the texture manager
     * @param finisher      lazily finishes textures once resource loading is complete
     */
    public LazyTextureManager(IManager<AbstractTexture> delegate, IFinisher<I, O> finisher) {
        DELEGATE = requireNonNull(delegate, "Delegate manager cannot be null");
        ANIMATED_TEXTURES = new HashMap<>();
        FINISHER = requireNonNull(finisher, "Finisher cannot be null");
    }

    /**
     * Registers a texture that needs to be finished. What happens when duplicate locations are added
     * depends on the provided {@link IFinisher}.
     * @param textureLocation   file location of texture identical to how it is used in a entity/gui/map
     * @param builder           unfinished texture
     */
    @Override
    public void register(ResourceLocation textureLocation, I builder) {
        requireNonNull(textureLocation, "Texture location cannot be null");
        requireNonNull(builder, "Texture builder cannot be null");

        /* Clear any existing texture immediately to prevent PreloadedTextures
           from re-adding themselves. The texture manager will reload before the
           EventDrivenTextures are added, causing a race condition with the
           registration CompletableFuture inside PreloadedTexture's reset method. */
        DELEGATE.unregister(textureLocation);

        FINISHER.queue(textureLocation, builder);
    }

    /**
     * Finishes all queued textures by adding them to Minecraft's texture manager
     * according to the provided {@link IFinisher}.
     */
    public void finishQueued() {
        Map<ResourceLocation, O> textures = FINISHER.finish();

        textures.forEach((location, texture) -> {
            DELEGATE.register(location, texture);
            ANIMATED_TEXTURES.put(location, texture);
        });
    }

    /**
     * Deletes a texture so Minecraft is no longer aware of it. This also allows the texture to be replaced.
     * @param textureLocation   file location of texture to delete
     */
    @Override
    public void unregister(ResourceLocation textureLocation) {
        requireNonNull(textureLocation, "Texture location cannot be null");

        DELEGATE.unregister(textureLocation);
        ANIMATED_TEXTURES.remove(textureLocation);
    }

    /**
     * Updates all animated textures that were loaded through this manager.
     */
    @Override
    public void tick() {
        ANIMATED_TEXTURES.values().forEach(CustomTickable::tick);
    }

}
