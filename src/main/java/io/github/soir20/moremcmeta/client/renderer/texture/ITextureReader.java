package io.github.soir20.moremcmeta.client.renderer.texture;

import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.ITickable;
import net.minecraft.client.renderer.texture.Texture;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;

/**
 * Reads an animated texture.
 * @param <T>   the type of animated texture to create
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public interface ITextureReader<T extends Texture & ITickable> {

    /**
     * Reads an animated texture from file data.
     * @param textureStream     input stream of image data
     * @param metadataStream    input stream of texture metadata (JSON)
     * @return  an animated texture
     * @throws IOException  failure reading from either input stream
     */
    T read(InputStream textureStream, InputStream metadataStream) throws IOException;

}
