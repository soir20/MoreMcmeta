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
import net.minecraft.resources.ResourceLocation;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureFinisher}.
 * @author soir20
 */
public class TextureFinisherTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_NullSpriteFinder_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureFinisher(null);
    }

    @Test
    public void queue_NullLocation_NullPointerException() {
        TextureFinisher finisher = new TextureFinisher(
                new SpriteFinder((atlasLocation) -> (spriteLocation) -> Optional.empty())
        );
        expectedException.expect(NullPointerException.class);
        finisher.queue(null, new EventDrivenTexture.Builder());
    }

    @Test
    public void queue_NullBuilder_NullPointerException() {
        TextureFinisher finisher = new TextureFinisher(
                new SpriteFinder((atlasLocation) -> (spriteLocation) -> Optional.empty())
        );
        expectedException.expect(NullPointerException.class);
        finisher.queue(new ResourceLocation("dummy"), null);
    }

    @Test
    public void queueAndFinish_AllSprites_SpriteUploadComponentAdded() {
        TextureFinisher finisher = new TextureFinisher(
                new SpriteFinder((atlasLocation) -> (spriteLocation) ->
                        Optional.of(new MockSprite(spriteLocation, new Point(2, 3)))
                )
        );

        Set<ResourceLocation> locations = new HashSet<>();
        locations.add(new ResourceLocation("textures/cat.png"));
        locations.add(new ResourceLocation("textures/bat.png"));
        locations.add(new ResourceLocation("textures/mat.png"));

        // Queue textures
        Set<MockRGBAImageFrame> frames = new HashSet<>();
        for (ResourceLocation location : locations) {
            EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
            MockRGBAImageFrame mockImage = new MockRGBAImageFrame();
            frames.add(mockImage);
            builder.setImage(mockImage);
            finisher.queue(location, builder);
        }

        // Finish and test textures
        Map<ResourceLocation, EventDrivenTexture> textures = finisher.finish();
        textures.values().forEach(EventDrivenTexture::upload);

        for (MockRGBAImageFrame frame : frames) {
            MockRGBAImage mockImage = (MockRGBAImage) frame.getImage(0);
            assertEquals(new Point(2, 3), mockImage.getLastUploadPoint());
            assertEquals(1, frame.getUploadCount());
        }

    }

    @Test
    public void queueAndFinish_AllSingles_SingleUploadComponentAdded() {
        TextureFinisher finisher = new TextureFinisher(
                new SpriteFinder((atlasLocation) -> (spriteLocation) -> Optional.empty())
        );

        Set<ResourceLocation> locations = new HashSet<>();
        locations.add(new ResourceLocation("textures/cat.png"));
        locations.add(new ResourceLocation("textures/bat.png"));
        locations.add(new ResourceLocation("textures/mat.png"));

        // Queue textures
        Set<MockRGBAImageFrame> frames = new HashSet<>();
        for (ResourceLocation location : locations) {
            EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
            MockRGBAImageFrame mockImage = new MockRGBAImageFrame();
            frames.add(mockImage);
            builder.setImage(mockImage);
            finisher.queue(location, builder);
        }

        // Finish and test textures
        Map<ResourceLocation, EventDrivenTexture> textures = finisher.finish();
        textures.values().forEach(EventDrivenTexture::upload);

        for (MockRGBAImageFrame frame : frames) {
            MockRGBAImage mockImage = (MockRGBAImage) frame.getImage(0);
            assertEquals(new Point(0, 0), mockImage.getLastUploadPoint());
            assertEquals(1, frame.getUploadCount());
        }

    }

    @Test
    public void finish_NoneQueued_NoneFinished() {
        TextureFinisher finisher = new TextureFinisher(
                new SpriteFinder((atlasLocation) -> (spriteLocation) -> Optional.empty())
        );

        assertTrue(finisher.finish().isEmpty());
    }

}