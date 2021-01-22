package io.github.soir20.moremcmeta.client.renderer.texture;

import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.texture.MipmapGenerator;
import net.minecraft.client.renderer.texture.NativeImage;
import net.minecraft.client.resources.data.AnimationMetadataSection;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.function.Function;

public class AnimatedTextureReader {
    private final int MIPMAP;
    private final Logger LOGGER;

    public AnimatedTextureReader(int mipmap, Logger logger) {
        MIPMAP = mipmap;
        LOGGER = logger;
    }

    public AnimatedTexture<NativeImageFrame> read(InputStream inputStream, AnimationMetadataSection metadata) {
        NativeImage image;
        try {
            image = NativeImage.read(inputStream);
        } catch (IOException e) {
            LOGGER.error("Unable to read animated image from input stream");
            return null;
        }

        NativeImage[] mipmaps = MipmapGenerator.generateMipmaps(image, MIPMAP);

        FrameReader<NativeImageFrame> frameReader = new FrameReader<>((frameData ->
                new NativeImageFrame(frameData, mipmaps, false, false, false)));

        List<NativeImageFrame> frames = frameReader.read(image.getWidth(), image.getHeight(), metadata);
        int frameWidth = frames.get(0).getWidth();
        int frameHeight = frames.get(0).getHeight();

        List<IRGBAImage.VisibleArea> visibleAreas = getInterpolatablePoints(image, frameWidth, frameHeight);

        AnimationFrameManager<NativeImageFrame> frameManager = new AnimationFrameManager<>(
                frames,
                getFrameTimeCalculator(metadata.getFrameTime()),
                new NativeImageFrameInterpolator(mipmaps, visibleAreas, frameWidth, frameHeight)
        );

        return new AnimatedTexture<>(frameManager, frameWidth, frameHeight, MIPMAP);
    }

    private List<IRGBAImage.VisibleArea> getInterpolatablePoints(NativeImage image,
                                                                 int frameWidth, int frameHeight) {
        List<IRGBAImage.VisibleArea> visibleAreas = new ArrayList<>();

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

    private Function<NativeImageFrame, Integer> getFrameTimeCalculator(int metadataFrameTime) {
        return (frame) -> {
            int singleFrameTime = frame.getFrameTime();
            return singleFrameTime == -1 ? metadataFrameTime : singleFrameTime;
        };
    }

    private class NativeImageFrameInterpolator implements IInterpolator<NativeImageFrame>, AutoCloseable {
        private final int FRAME_WIDTH;
        private final int FRAME_HEIGHT;
        private final List<IRGBAImage.VisibleArea> VISIBLE_AREAS;
        private final RGBAInterpolator<NativeImageRGBAWrapper> INTERPOLATOR;
        private final NativeImage[] MIPMAPS;

        public NativeImageFrameInterpolator(NativeImage[] originalMipmaps,
                                            List<IRGBAImage.VisibleArea> visibleAreas,
                                            int frameWidth, int frameHeight) {
            FRAME_WIDTH = frameWidth;
            FRAME_HEIGHT = frameHeight;
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
            return new NativeImageFrame(data, mipmaps, false, false, false);
        }

        @Override
        public void close() {
            for (NativeImage mipmap : MIPMAPS) {
                mipmap.close();
            }
        }
    }

}