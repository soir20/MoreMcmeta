package io.github.soir20.moremcmeta.client.resource;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonParseException;
import io.github.soir20.moremcmeta.client.texture.MockManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.IOException;
import java.util.Collection;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;

import static org.junit.Assert.*;

/**
 * Tests the {@link TextureReloadListener}. We assume that the retrieval of files with the correct extension
 * works because that is part of Minecraft's code.
 *
 * Note about low branch coverage for this class: The branches are almost all generated by the compiler
 * in the try-with-resources statement in the listener. Some of these branches might be unreachable.
 * Thus, it makes more sense to test representative cases here than to try to maximize branch coverage.
 * See https://stackoverflow.com/a/17356707 (StackOverflow explanation) and
 * https://docs.oracle.com/javase/specs/jls/se7/html/jls-14.html#jls-14.20.3.1 (standards definition
 * of try-with-resources).
 * @author soir20
 */
public class TextureReloadListenerTest {
    private final Logger LOGGER = LogManager.getLogger();

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @Test
    public void construct_TextureFactoryNull_NullPointerException() {
        MockManager<Integer> mockManager = new MockManager<>();

        expectedException.expect(NullPointerException.class);
        new TextureReloadListener<>(null, mockManager, LOGGER);
    }

    @Test
    public void construct_TextureManagerNull_NullPointerException() {
        expectedException.expect(NullPointerException.class);
        new TextureReloadListener<>((texStream, metadataStream) -> 1, null, LOGGER);
    }

    @Test
    public void construct_LoggerNull_NullPointerException() {
        MockManager<Integer> mockManager = new MockManager<>();

        expectedException.expect(NullPointerException.class);
        new TextureReloadListener<>((texStream, metadataStream) -> 1, mockManager, null);
    }

    @Test
    public void onResourceManagerReload_ResourceManagerNull_NullPointerException() {
        MockManager<Integer> mockManager = new MockManager<>();

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        expectedException.expect(NullPointerException.class);
        listener.onResourceManagerReload(null);
    }

    @Test
    public void onResourceManagerReload_ValidLocations_ManagerHasAllTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(6, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_ValidLocationsDuplicates_ManagerHasNoDuplicate() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "bat.png.moremcmeta",
                        "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(6, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/zombie.png")));
    }

    @Test
    public void onResourceManagerReload_FilteredLocations_ManagerHasFilteredTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper", "zombie.jpg", "ocelot.png"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(2, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
    }

    @Test
    public void onResourceManagerReload_MissingTextureLocations_ManagerHasNoMissingTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of("creeper.png", "zombie.png"), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(2, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
    }

    @Test
    public void onResourceManagerReload_MissingMetadataLocations_ManagerHasNoMissingTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of("creeper.png.moremcmeta", "zombie.png.moremcmeta"), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(2, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
    }

    @Test
    public void onResourceManagerReload_InvalidJson_ManagerOnlyValidTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        AtomicInteger texturesLoaded = new AtomicInteger();
        TextureReloadListener<Integer> listener = new TextureReloadListener<>(
                (texStream, metadataStream) -> {
                    texturesLoaded.getAndIncrement();
                    if (texturesLoaded.get() < 3) {
                        throw new JsonParseException("Dummy exception");
                    }
                    return 1;
                },
                mockManager, LOGGER
        );

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(4, locations.size());
    }

    @Test
    public void onResourceManagerReload_InvalidMetadata_ManagerOnlyValidTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        AtomicInteger texturesLoaded = new AtomicInteger();
        TextureReloadListener<Integer> listener = new TextureReloadListener<>(
                (texStream, metadataStream) -> {
                    texturesLoaded.getAndIncrement();
                    if (texturesLoaded.get() < 3) {
                        throw new IllegalArgumentException("Dummy exception");
                    }
                    return 1;
                },
                mockManager, LOGGER
        );

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(4, locations.size());
    }

    @Test
    public void onResourceManagerReload_UnknownException_ExceptionNotCaught() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );


        AtomicInteger texturesLoaded = new AtomicInteger();
        TextureReloadListener<Integer> listener = new TextureReloadListener<>(
                (texStream, metadataStream) -> {
                    texturesLoaded.getAndIncrement();
                    if (texturesLoaded.get() < 3) {
                        throw new RuntimeException("Dummy exception");
                    }
                    return 1;
                },
                mockManager, LOGGER
        );

        expectedException.expect(RuntimeException.class);
        listener.onResourceManagerReload(mockResourceManager);
    }

    @Test
    public void onResourceManagerReload_ResourceManagerThrowsUnknownException_ExceptionNotCaught() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        ) {
            @Override
            public Collection<ResourceLocation> listResources(String pathIn, Predicate<String> filter) {
                throw new RuntimeException();
            }
        };

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);


        expectedException.expect(RuntimeException.class);
        listener.onResourceManagerReload(mockResourceManager);
    }

    @Test
    public void onResourceManagerReload_ResourceManagerReturnsNullTexture_NullPointerException() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        ) {
            @Override
            public Resource getResource(ResourceLocation resourceLocationIn) throws IOException {
                if (resourceLocationIn.getPath().endsWith(".moremcmeta")) {
                    return super.getResource(resourceLocationIn);
                }

                return null;
            }
        };

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        expectedException.expect(NullPointerException.class);
        listener.onResourceManagerReload(mockResourceManager);
    }

    @Test
    public void onResourceManagerReload_ResourceManagerReturnsNullMetadata_NullPointerException() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        ) {
            @Override
            public Resource getResource(ResourceLocation resourceLocationIn) throws IOException {
                if (resourceLocationIn.getPath().endsWith(".moremcmeta")) {
                    return null;
                }

                return super.getResource(resourceLocationIn);
            }
        };

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        expectedException.expect(NullPointerException.class);
        listener.onResourceManagerReload(mockResourceManager);
    }

    @Test
    public void onResourceManagerReload_ClosureIOException_ManagerHasValidTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        ) {
            @Override
            public Resource getResource(ResourceLocation resourceLocationIn) throws IOException {
                if (resourceLocationIn.getPath().contains("bat")) {
                    return super.getResource(resourceLocationIn);
                }

                return new MockSimpleResource(resourceLocationIn, MockResourceManager.DEFAULT_PACK_NAME) {
                    @Override
                    public void close() throws IOException {
                        throw new IOException("Dummy exception");
                    }
                };
            }
        };

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);
        Set<ResourceLocation> locations = mockManager.getLocations();

        assertEquals(2, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
    }

    @Test
    public void onResourceManagerReload_ClosureUnknownException_ExceptionNotCaught() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        ) {
            @Override
            public Resource getResource(ResourceLocation resourceLocationIn) throws IOException {
                if (resourceLocationIn.getPath().contains("bat")) {
                    return super.getResource(resourceLocationIn);
                }

                return new MockSimpleResource(resourceLocationIn, MockResourceManager.DEFAULT_PACK_NAME) {
                    @Override
                    public void close() {
                        throw new RuntimeException("Dummy exception");
                    }
                };
            }
        };

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        expectedException.expect(RuntimeException.class);
        listener.onResourceManagerReload(mockResourceManager);
    }

    @Test
    public void onResourceManagerReload_TextureAndMetadataInDifferentPacks_ManagerSkipsSeparatedTextures() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), true
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(0, locations.size());
    }

    @Test
    public void onResourceManagerReload_ResourceLocationException_ManagerCleared() {
        MockManager<Integer> mockManager = new MockManager<>();

        ResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "bad location.png.moremcmeta", "fol der/ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload);
        listener.onResourceManagerReload(mockResourceManagerSecondReload);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(0, locations.size());
    }

    @Test
    public void onResourceManagerReload_PreviouslyLoadedTextures_OldDeletedNewLoaded() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload);
        listener.onResourceManagerReload(mockResourceManagerSecondReload);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(6, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/dolphin.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/ocelot.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/dolphin.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/ocelot.png")));
    }

    @Test
    public void onResourceManagerReload_ThirdReloadNoLongerAnimatedTextures_NonAnimatedNotDeleted() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManagerFirstReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "creeper.png.moremcmeta", "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerSecondReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "dolphin.png.moremcmeta", "ocelot.png.moremcmeta"),
                ImmutableList.of(), false
        );
        ResourceManager mockResourceManagerThirdReload = new MockResourceManager(
                ImmutableList.of("bat.png.moremcmeta", "cat.png.moremcmeta", "bear.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManagerFirstReload);
        listener.onResourceManagerReload(mockResourceManagerSecondReload);

        mockManager.register(new ResourceLocation("textures/creeper.png"),
                1);
        mockManager.register(new ResourceLocation("textures/zombie.png"),
                1);

        listener.onResourceManagerReload(mockResourceManagerThirdReload);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(8, locations.size());
        assertTrue(locations.contains(new ResourceLocation("textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/cat.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/bear.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/cat.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/bear.png")));
    }

    @Test
    public void onResourceManagerReload_DiffNamespaces_AllLoaded() {
        MockManager<Integer> mockManager = new MockManager<>();
        ResourceManager mockResourceManager = new MockResourceManager(
                ImmutableList.of("test:bat.png.moremcmeta", "moremcmeta:creeper.png.moremcmeta",
                        "zombie.png.moremcmeta"),
                ImmutableList.of(), false
        );

        TextureReloadListener<Integer> listener = new TextureReloadListener<>((texStream, metadataStream) -> 1,
                mockManager, LOGGER);

        listener.onResourceManagerReload(mockResourceManager);

        Set<ResourceLocation> locations = mockManager.getLocations();
        assertEquals(6, locations.size());
        assertTrue(locations.contains(new ResourceLocation("test", "textures/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("moremcmeta", "textures/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("textures/zombie.png")));
        assertTrue(locations.contains(new ResourceLocation("test", "optifine/bat.png")));
        assertTrue(locations.contains(new ResourceLocation("moremcmeta", "optifine/creeper.png")));
        assertTrue(locations.contains(new ResourceLocation("optifine/zombie.png")));
    }

}