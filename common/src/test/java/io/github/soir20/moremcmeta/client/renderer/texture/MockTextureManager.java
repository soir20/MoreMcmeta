package io.github.soir20.moremcmeta.client.renderer.texture;

import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.Tickable;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @author soir20
 */
public class MockTextureManager implements ITextureManager {
    private final Map<ResourceLocation, AbstractTexture> TEXTURES;
    private final Map<ResourceLocation, Tickable> ANIMATED_TEXTURES;

    public MockTextureManager() {
        TEXTURES = new HashMap<>();
        ANIMATED_TEXTURES = new HashMap<>();
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, AbstractTexture textureObj) {
        TEXTURES.put(textureLocation, textureObj);
        ANIMATED_TEXTURES.remove(textureLocation);
        if (textureObj instanceof Tickable) {
            ANIMATED_TEXTURES.put(textureLocation, (Tickable) textureObj);
        }
    }

    @Override
    public void queueTexture(ResourceLocation textureLocation, Supplier<AbstractTexture> textureGetter) {
        loadTexture(textureLocation, textureGetter.get());
    }

    @Override
    public AbstractTexture getTexture(ResourceLocation textureLocation) {
        return TEXTURES.get(textureLocation);
    }

    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        TEXTURES.remove(textureLocation);
    }

    public Set<ResourceLocation> getLocations() {
        return TEXTURES.keySet();
    }

    public void tick() {
        ANIMATED_TEXTURES.values().forEach(Tickable::tick);
    }
}
