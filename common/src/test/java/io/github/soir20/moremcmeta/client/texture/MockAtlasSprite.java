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

import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

/**
 * A fake {@link TextureAtlasSprite}.
 * @author soir20
 */
public class MockAtlasSprite implements ISprite {
    private final ResourceLocation NAME;

    public MockAtlasSprite(ResourceLocation name) {
        NAME = name;
    }

    @Override
    public void bind() {}

    @Override
    public ResourceLocation getName() {
        return NAME;
    }

    @Override
    public Point getUploadPoint() {
        return new Point(1, 2);
    }

}
