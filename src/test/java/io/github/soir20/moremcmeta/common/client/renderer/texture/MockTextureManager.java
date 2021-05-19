package io.github.soir20.moremcmeta.common.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;
import net.minecraft.util.ResourceLocation;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Keeps track of the {@link ResourceLocation}s of textures that would have been added to a real texture manager.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class MockTextureManager implements ITextureManager {
    private final Map<ResourceLocation, Texture> TEXTURES;
    private final Map<ResourceLocation, ITickable> ANIMATED_TEXTURES;

    public MockTextureManager() {
        TEXTURES = new HashMap<>();
        ANIMATED_TEXTURES = new HashMap<>();
    }

    @Override
    public void loadTexture(ResourceLocation textureLocation, Texture textureObj) {
        TEXTURES.put(textureLocation, textureObj);
        ANIMATED_TEXTURES.remove(textureLocation);
        if (textureObj instanceof ITickable) {
            ANIMATED_TEXTURES.put(textureLocation, (ITickable) textureObj);
        }
    }

    @Override
    public void deleteTexture(ResourceLocation textureLocation) {
        TEXTURES.remove(textureLocation);
    }

    public Set<ResourceLocation> getLocations() {
        return TEXTURES.keySet();
    }

    public void tick() {
        ANIMATED_TEXTURES.values().forEach(ITickable::tick);
    }
}