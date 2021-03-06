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

import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @param <R> resource type
 * @author soir20
 */
public class MockManager<R> implements IManager<R> {
    private final Map<ResourceLocation, R> TEXTURES;
    private final Map<ResourceLocation, Tickable> ANIMATED_TEXTURES;

    public MockManager() {
        TEXTURES = new HashMap<>();
        ANIMATED_TEXTURES = new HashMap<>();
    }

    @Override
    public void register(ResourceLocation textureLocation, R textureObj) {
        TEXTURES.put(textureLocation, textureObj);
        ANIMATED_TEXTURES.remove(textureLocation);
        if (textureObj instanceof Tickable) {
            ANIMATED_TEXTURES.put(textureLocation, (Tickable) textureObj);
        }
    }

    @Override
    public void unregister(ResourceLocation textureLocation) {
        TEXTURES.remove(textureLocation);
    }

    public Set<ResourceLocation> getLocations() {
        return TEXTURES.keySet();
    }

    public void tick() {
        ANIMATED_TEXTURES.values().forEach(Tickable::tick);
    }

    public R getTexture(ResourceLocation location) {
        return TEXTURES.get(location);
    }
}
