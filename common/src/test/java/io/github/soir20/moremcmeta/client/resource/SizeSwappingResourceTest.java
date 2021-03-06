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

package io.github.soir20.moremcmeta.client.resource;

import com.google.gson.JsonObject;
import net.minecraft.client.resources.metadata.animation.AnimationMetadataSection;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.metadata.MetadataSectionSerializer;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.SimpleResource;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.junit.Assert.*;

/**
 * Tests the {@link SizeSwappingResource}.
 *
 * Note about low branch coverage for this class: The branches are almost all generated by the compiler
 * in the try-with-resources statement in the listener. Some of these branches might be unreachable.
 * Thus, it makes more sense to test representative cases here than to try to maximize branch coverage.
 * See https://stackoverflow.com/a/17356707 (StackOverflow explanation) and
 * https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.20.3.1 (standards definition
 * of try-with-resources).
 */
public class SizeSwappingResourceTest {
    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    private final InputStream EMPTY_STREAM = new ByteArrayInputStream("".getBytes());

    @Test
    public void construct_NullResource_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new SizeSwappingResource(null, EMPTY_STREAM);
    }

    @Test
    public void getLocation_OriginalHasName_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getSourceName(), wrapper.getSourceName());
    }

    @Test
    public void getLocation_OriginalNullName_SameAsOriginal() {
        Resource original = new SimpleResource(null, new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getSourceName(), wrapper.getSourceName());
    }

    @Test
    public void getLocation_OriginalHasLocation_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getLocation(), wrapper.getLocation());
    }

    @Test
    public void getLocation_OriginalNullLocation_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", null,
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getLocation(), wrapper.getLocation());
    }

    @Test
    public void getLocation_OriginalHasStream_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                new ByteArrayInputStream("dummy stream".getBytes()), EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getInputStream(), wrapper.getInputStream());
    }

    @Test
    public void getLocation_OriginalNullStream_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                null, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getInputStream(), wrapper.getInputStream());
    }

    @Test
    public void equals_OriginalProvided_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertNotEquals(wrapper, original);
    }

    @Test
    public void equals_Reflexive_Equal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(wrapper, wrapper);
    }

    @Test
    public void equals_SameOriginalNullMetadata_Equal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original, null);
        assertEquals(wrapper, otherWrapper);
    }

    @Test
    public void equals_SameOriginalSameMetadata_Equal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        InputStream stream = new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
            @Override
            public boolean equals(Object other) {
                return other instanceof ByteArrayInputStream;
            }
        };
        InputStream otherStream = new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
            @Override
            public boolean equals(Object other) {
                return other instanceof ByteArrayInputStream;
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original, stream);
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original, otherStream);
        assertEquals(wrapper, otherWrapper);
    }

    @Test
    public void equals_OtherMetadataStreamNull_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()));
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original, null);
        assertNotEquals(wrapper, otherWrapper);
    }

    @Test
    public void equals_DifferentMetadataStreams_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()));
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 8 } }".getBytes()));
        assertNotEquals(wrapper, otherWrapper);
    }

    @Test
    public void equals_DifferentOriginals_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        Resource otherOriginal = new SimpleResource("dummy2", new ResourceLocation("dummy-location2"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        SizeSwappingResource otherWrapper = new SizeSwappingResource(otherOriginal, null);
        assertNotEquals(wrapper, otherWrapper);
    }

    @Test
    public void hashCode_OriginalProvided_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertNotEquals(wrapper.hashCode(), original.hashCode());
    }

    @Test
    public void hashCode_Reflexive_Equal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(wrapper.hashCode(), wrapper.hashCode());
    }

    @Test
    public void hashCode_SameOriginalNullMetadata_Equal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original, null);
        assertEquals(wrapper.hashCode(), otherWrapper.hashCode());
    }

    @Test
    public void hashCode_SameOriginalSameMetadata_Equal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        InputStream stream = new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
            @Override
            public int hashCode() {
                return Integer.MAX_VALUE;
            }
        };
        InputStream otherStream = new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
            @Override
            public int hashCode() {
                return Integer.MAX_VALUE;
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original, stream);
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original, otherStream);
        assertEquals(wrapper.hashCode(), otherWrapper.hashCode());
    }

    @Test
    public void hashCode_OtherMetadataStreamNull_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()));
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original, null);
        assertNotEquals(wrapper.hashCode(), otherWrapper.hashCode());
    }

    @Test
    public void hashCode_DifferentMetadataStreams_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()));
        SizeSwappingResource otherWrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 8 } }".getBytes()));
        assertNotEquals(wrapper.hashCode(), otherWrapper.hashCode());
    }

    @Test
    public void hashCode_DifferentOriginals_NotEqual() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM);
        Resource otherOriginal = new SimpleResource("dummy2", new ResourceLocation("dummy-location2"),
                EMPTY_STREAM, EMPTY_STREAM);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        SizeSwappingResource otherWrapper = new SizeSwappingResource(otherOriginal, null);
        assertNotEquals(wrapper.hashCode(), otherWrapper.hashCode());
    }

    @Test
    public void close_MetadataNotNull_BothClosed() throws IOException {
        final boolean[] closed = {false, false};
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM) {
            @Override
            public void close() throws IOException {
                super.close();
                closed[0] = true;
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
            @Override
            public void close() throws IOException {
                super.close();
                closed[1] = true;
            }
        });
        wrapper.close();

        assertTrue(closed[0]);
        assertTrue(closed[1]);
    }

    @Test
    public void close_MetadataNull_BothClosed() throws IOException {
        final boolean[] closed = {false};
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM) {
            @Override
            public void close() throws IOException {
                super.close();
                closed[0] = true;
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        wrapper.close();

        assertTrue(closed[0]);
    }

    @Test
    public void close_OriginalThrowsException_MetadataClosed() throws IOException {
        final boolean[] closed = {false, false};
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM) {
            @Override
            public void close() throws IOException {
                throw new IOException("dummy");
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
            @Override
            public void close() throws IOException {
                super.close();
                closed[1] = true;
            }
        });

        try {
            expectedException.expect(IOException.class);
            wrapper.close();
        } finally {
            assertTrue(closed[1]);
        }
    }

    @Test
    public void close_MetadataThrowsException_OriginalClosed() throws IOException {
        final boolean[] closed = {false, false};
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM) {
            @Override
            public void close() {
                closed[0] = true;
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
                    @Override
                    public void close() throws IOException {
                        throw new IOException("dummy");
                    }
                });

        try {
            expectedException.expect(IOException.class);
            wrapper.close();
        } finally {
            assertTrue(closed[0]);
        }
    }

    @Test
    public void close_BothThrowException_OriginalExceptionThrown() throws IOException {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM, EMPTY_STREAM) {
            @Override
            public void close() throws IOException {
                throw new IOException("original");
            }
        };

        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()) {
                    @Override
                    public void close() throws IOException {
                        throw new IOException("metadata");
                    }
                });

        expectedException.expect(IOException.class);
        expectedException.expectMessage("original");
        wrapper.close();
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getMetadata_OriginalHasMetadataStreamNoModMetadata_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
               EMPTY_STREAM,  new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getMetadata(AnimationMetadataSection.SERIALIZER).getDefaultFrameTime(),
                wrapper.getMetadata(AnimationMetadataSection.SERIALIZER).getDefaultFrameTime());
    }

    @Test
    public void getMetadata_OriginalNullMetadataStreamNoModMetadata_SameAsOriginal() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  null);
        SizeSwappingResource wrapper = new SizeSwappingResource(original, null);
        assertEquals(original.getMetadata(AnimationMetadataSection.SERIALIZER),
                wrapper.getMetadata(AnimationMetadataSection.SERIALIZER));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getMetadata_OriginalHasMetadataStreamHasModMetadata_OriginalMetadataPreferred() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"animation\": { \"frametime\": 4 } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 10 } }".getBytes()));
        assertEquals(4, wrapper.getMetadata(AnimationMetadataSection.SERIALIZER).getDefaultFrameTime());
    }

    @Test
    public void getMetadata_DifferentSerializer_ModMetadataSkipped() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": 10 } }".getBytes()));

        MetadataSectionSerializer<AnimationMetadataSection> mockSerializer =
                new MetadataSectionSerializer<AnimationMetadataSection>() {
            @Override
            public String getMetadataSectionName() {
                return "animation";
            }

            @Override
            public AnimationMetadataSection fromJson(JsonObject jsonObject) {
                return AnimationMetadataSection.SERIALIZER.fromJson(jsonObject);
            }
        };

        assertNull(wrapper.getMetadata(mockSerializer));
    }

    @Test
    public void getMetadata_NullSerializer_NullPointerException() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"texture\": { \"blur\": true } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"width\": 100, \"height\": 200 } }".getBytes()));

        expectedException.expect(NullPointerException.class);
        wrapper.getMetadata(null);
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getMetadata_OriginalHasNoAnimMetadata_SingleFrameAnimData() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"texture\": { \"blur\": true } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"width\": 100, \"height\": 200 } }".getBytes()));

        AnimationMetadataSection metadata = wrapper.getMetadata(AnimationMetadataSection.SERIALIZER);
        assertEquals(1, metadata.getUniqueFrameIndices().size());
        assertEquals(100, metadata.getFrameWidth(-1));
        assertEquals(200, metadata.getFrameHeight(-1));
    }

    @Test
    @SuppressWarnings("ConstantConditions")
    public void getMetadata_ReadTwice_SingleFrameAnimData() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"texture\": { \"blur\": true } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"width\": 100, \"height\": 200 } }".getBytes()));

        wrapper.getMetadata(AnimationMetadataSection.SERIALIZER);
        AnimationMetadataSection metadata = wrapper.getMetadata(AnimationMetadataSection.SERIALIZER);
        assertEquals(1, metadata.getUniqueFrameIndices().size());
        assertEquals(100, metadata.getFrameWidth(-1));
        assertEquals(200, metadata.getFrameHeight(-1));
    }

    @Test
    public void getMetadata_IOException_NullMetadata() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"texture\": { \"blur\": true } }".getBytes()));

        // Use a non-UTF8 character in "animation" to trigger an example read exception
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"ãnimation\": { \"frametime\": 10 } }".getBytes()));

        assertNull(wrapper.getMetadata(AnimationMetadataSection.SERIALIZER));
    }

    @Test
    public void getMetadata_JsonParseException_NullMetadata() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"texture\": { \"blur\": true } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream(" \"animation\": { \"frametime\": 10 } }".getBytes()));

        assertNull(wrapper.getMetadata(AnimationMetadataSection.SERIALIZER));
    }

    @Test
    public void getMetadata_IncorrectMetadataValues_NullMetadata() {
        Resource original = new SimpleResource("dummy", new ResourceLocation("dummy-location"),
                EMPTY_STREAM,  new ByteArrayInputStream("{ \"texture\": { \"blur\": true } }".getBytes()));
        SizeSwappingResource wrapper = new SizeSwappingResource(original,
                new ByteArrayInputStream("{ \"animation\": { \"frametime\": -1 } }".getBytes()));

        assertNull(wrapper.getMetadata(AnimationMetadataSection.SERIALIZER));
    }

}