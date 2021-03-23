package io.github.soir20.moremcmeta.client.renderer.texture;

import com.google.gson.JsonParseException;
import com.mojang.datafixers.util.Pair;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import net.minecraft.client.resources.data.TextureMetadataSection;
import net.minecraft.resources.SimpleResource;
import net.minecraft.util.ResourceLocation;
import org.apache.logging.log4j.Logger;

import javax.annotation.ParametersAreNonnullByDefault;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import static java.util.Objects.requireNonNull;

/**
 * Reads an {@link AnimatedTexture} from file data. It is reusable for all animated textures with the same
 * mipmap level.
 * @author soir20
 */
@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class AnimatedTextureReader implements ITextureReader<AnimatedTexture<NativeImageFrame>> {
    private final int MIPMAP;
    private final Logger LOGGER;

    /**
     * Creates a new reader for animated textures.
     * @param mipmap    number of mipmap levels to use
     * @param logger    logger for reading-related messages
     */
    public AnimatedTextureReader(int mipmap, Logger logger) {
        MIPMAP = mipmap;
        LOGGER = requireNonNull(logger, "Logger cannot be null");
    }

    /**
     * Reads an {@link AnimatedTexture}.
     * @param textureStream           input stream with image data
     * @param metadataStream          input stream with texture and animation properties
     * @return  an animated texture based on the provided data
     * @throws IOException  failure reading from either input stream
     */
    public AnimatedTexture<NativeImageFrame> read(InputStream textureStream, InputStream metadataStream)
            throws IOException {
        requireNonNull(textureStream, "Texture input stream cannot be null");
        requireNonNull(metadataStream, "Metadata input stream cannot be null");

        NativeImage image = NativeImage.read(textureStream);
        LOGGER.debug("Successfully read image from input");

        NativeImage[] mipmaps = MipmapGenerator.generateMipmaps(image, MIPMAP);

        /* The SimpleResource class would normally handle metadata parsing when we originally
           got the resource. However, the ResourceManager only looks for .mcmeta metadata, and its
           nested structure and an unordered (stream) accessor for resource packs cannot be
           easily overridden. However, we can create a dummy resource to parse the metadata. */
        SimpleResource metadataParser = new SimpleResource("dummy", new ResourceLocation(""),
                textureStream, metadataStream);

        AnimationMetadataSection animationMetadata = null;
        TextureMetadataSection textureMetadata = null;
        try {
            animationMetadata = metadataParser.getMetadata(AnimationMetadataSection.SERIALIZER);
            textureMetadata = metadataParser.getMetadata(TextureMetadataSection.SERIALIZER);
        } catch (JsonParseException jsonError) {
            LOGGER.error("Unable to read texture metadata: {}", jsonError.toString());
        } finally {

            /* Use defaults if no metadata was read.
               The metadata parser can set these to null even if there was no error. */
            if (animationMetadata == null) {
                animationMetadata = AnimationMetadataSection.EMPTY;
            }

            if (textureMetadata == null) {
                textureMetadata = new TextureMetadataSection(false, false);
            }
        }

        boolean blur = textureMetadata.getTextureBlur();
        boolean clamp = textureMetadata.getTextureClamp();

        // Frames
        FrameReader<NativeImageFrame> frameReader = new FrameReader<>((frameData ->
                new NativeImageFrame(frameData, mipmaps, blur, clamp, false)));
        List<NativeImageFrame> frames = frameReader.read(image.getWidth(), image.getHeight(), animationMetadata);
        int frameWidth = frames.get(0).getWidth();
        int frameHeight = frames.get(0).getHeight();

        // Interpolation
        List<IRGBAImage.VisibleArea> visibleAreas = getInterpolatablePoints(image, frameWidth, frameHeight);
        NativeImageFrameInterpolator interpolator = new NativeImageFrameInterpolator(mipmaps, visibleAreas,
                frameWidth, frameHeight, blur, clamp);

        // Frame management
        AnimationFrameManager<NativeImageFrame> frameManager;
        if (animationMetadata.isInterpolate()) {
             frameManager = new AnimationFrameManager<>(frames, NativeImageFrame::getFrameTime, interpolator);
        } else {
            frameManager = new AnimationFrameManager<>(frames, NativeImageFrame::getFrameTime);
        }

        // Resource cleanup
        Runnable closeMipmaps = () -> {
            for (NativeImage mipmap : mipmaps) {
                mipmap.close();
            }

            interpolator.close();
        };

        return new AnimatedTexture<>(frameManager, frameWidth, frameHeight, MIPMAP, closeMipmaps);
    }

    /**
     * Gets the pixels that will change for every mipmap.
     * @param image         the original image to analyze
     * @param frameWidth    the width of a frame
     * @param frameHeight   the height of a frame
     * @return  pixels that change for every mipmap (starting with the default image)
     */
    private List<IRGBAImage.VisibleArea> getInterpolatablePoints(NativeImage image, int frameWidth, int frameHeight) {
        List<IRGBAImage.VisibleArea> visibleAreas = new ArrayList<>();

        // Find points in original image
        IRGBAImage.VisibleArea.Builder noMipmapBuilder = new IRGBAImage.VisibleArea.Builder();
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int frameX = x % frameWidth;
                int frameY = y % frameHeight;

                // We want to detect a point that changes in any frame
                if (image.getPixelRGBA(x, y) != image.getPixelRGBA(frameX, frameY)) {
                    noMipmapBuilder.addPixel(frameX, frameY);
                }

            }
        }
        visibleAreas.add(noMipmapBuilder.build());

        // Point coordinates will be different for all mipmap levels
        for (int level = 1; level <= MIPMAP; level++) {
            IRGBAImage.VisibleArea.Builder mipmapBuilder = new IRGBAImage.VisibleArea.Builder();

            for (Pair<Integer, Integer> point : visibleAreas.get(0)) {
                mipmapBuilder.addPixel(point.getFirst() >> level, point.getSecond() >> level);
            }

            visibleAreas.add(mipmapBuilder.build());
        }

        return visibleAreas;
    }

    /**
     * Interpolates between {@link NativeImageFrame}s. All interpolated frames share a {@link NativeImage},
     * which has its pixels replaced when interpolation occurs.
     * @author soir20
     */
    @ParametersAreNonnullByDefault
    @MethodsReturnNonnullByDefault
    private class NativeImageFrameInterpolator implements IInterpolator<NativeImageFrame>, AutoCloseable {
        private final int FRAME_WIDTH;
        private final int FRAME_HEIGHT;
        private final boolean BLUR;
        private final boolean CLAMP;
        private final List<IRGBAImage.VisibleArea> VISIBLE_AREAS;
        private final RGBAInterpolator<NativeImageRGBAWrapper> INTERPOLATOR;
        private final NativeImage[] MIPMAPS;

        /**
         * Creates a new interpolator.
         * @param originalMipmaps   the original mipmaps to base interpolations off of (starting at the original)
         * @param visibleAreas      the visible areas for each mipmap (starting at the original's area)
         * @param frameWidth        the width of a frame in the original image
         * @param frameHeight       the height of a frame in the original image
         * @param blur              whether to blur the output
         * @param clamp             whether to clamp the output
         */
        public NativeImageFrameInterpolator(NativeImage[] originalMipmaps,
                                            List<IRGBAImage.VisibleArea> visibleAreas,
                                            int frameWidth, int frameHeight, boolean blur, boolean clamp) {
            FRAME_WIDTH = frameWidth;
            FRAME_HEIGHT = frameHeight;
            BLUR = blur;
            CLAMP = clamp;
            MIPMAPS = new NativeImage[originalMipmaps.length];

            // Directly convert mipmapped widths to their associated image
            HashMap<Integer, Pair<NativeImage, IRGBAImage.VisibleArea>> widthsToImage = new HashMap<>();
            for (int level = 0; level <= MIPMAP; level++) {
                int mipmappedWidth = FRAME_WIDTH >> level;
                int mipmappedHeight = FRAME_HEIGHT >> level;

                NativeImage mipmappedImage = new NativeImage(mipmappedWidth, mipmappedHeight, true);
                mipmappedImage.copyImageData(originalMipmaps[MIPMAP]);
                MIPMAPS[level] = mipmappedImage;

                widthsToImage.put(mipmappedWidth, new Pair<>(mipmappedImage, visibleAreas.get(level)));
            }

            VISIBLE_AREAS = visibleAreas;

            INTERPOLATOR = new RGBAInterpolator<>((width, height) -> {
                Pair<NativeImage, IRGBAImage.VisibleArea> imageAndPoints = widthsToImage.get(width);

                return new NativeImageRGBAWrapper(imageAndPoints.getFirst(), 0, 0, width, height,
                        imageAndPoints.getSecond());
            });
        }

        /**
         * Interpolates between a starting frame and an ending frame for all mipmap levels.
         * @param steps     total steps between the start and end frame
         * @param step      current step of the interpolation (between 1 and steps - 1)
         * @param start     the frame to start interpolation from
         * @param end       the frame to end interpolation at
         * @return  the interpolated frame at the given step
         */
        @Override
        public NativeImageFrame interpolate(int steps, int step, NativeImageFrame start, NativeImageFrame end) {
            NativeImage[] mipmaps = new NativeImage[MIPMAP + 1];

            for (int level = 0; level <= MIPMAP; level++) {
                NativeImageRGBAWrapper startImage = new NativeImageRGBAWrapper(
                        start.getImage(level),
                        start.getXOffset() >> level,
                        start.getYOffset() >> level,
                        FRAME_WIDTH >> level,
                        FRAME_HEIGHT >> level,
                        VISIBLE_AREAS.get(level)
                );
                NativeImageRGBAWrapper endImage = new NativeImageRGBAWrapper(
                        end.getImage(level),
                        end.getXOffset() >> level,
                        end.getYOffset() >> level,
                        FRAME_WIDTH >> level,
                        FRAME_HEIGHT >> level,
                        VISIBLE_AREAS.get(level)
                );

                NativeImageRGBAWrapper interpolated = INTERPOLATOR.interpolate(steps, step, startImage, endImage);

                mipmaps[level] = interpolated.getImage();
            }

            FrameReader.FrameData data = new FrameReader.FrameData(mipmaps[0].getWidth(), mipmaps[0].getHeight(),
                    0, 0, 1);
            return new NativeImageFrame(data, mipmaps, BLUR, CLAMP, false);
        }

        /**
         * Closes all mipmapped images where interpolated frames are uploaded.
         */
        @Override
        public void close() {
            for (NativeImage mipmap : MIPMAPS) {
                mipmap.close();
            }
        }
    }

}
