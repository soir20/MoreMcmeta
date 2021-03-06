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

import com.google.common.collect.ImmutableList;
import io.github.soir20.moremcmeta.client.animation.IInterpolator;
import io.github.soir20.moremcmeta.client.animation.RGBAInterpolator;
import io.github.soir20.moremcmeta.client.io.FrameReader;
import io.github.soir20.moremcmeta.math.Point;

import static java.util.Objects.requireNonNull;

/**
 * An animation frame based on a {@link IRGBAImage}.
 * @author soir20
 */
public class RGBAImageFrame {
    private final ImmutableList<? extends IRGBAImage> MIPMAPS;
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int FRAME_TIME;

    /**
     * Creates a new frame based on frame data.
     * @param frameData     general data associated with the frame
     * @param mipmaps       mipmapped images for this frame (starting with the original image)
     */
    public RGBAImageFrame(FrameReader.FrameData frameData, ImmutableList<? extends IRGBAImage> mipmaps) {
        requireNonNull(frameData, "Frame data cannot be null");
        MIPMAPS = requireNonNull(mipmaps, "Mipmaps cannot be null");
        if (mipmaps.size() == 0) {
            throw new IllegalArgumentException("At least one mipmap must be provided");
        }

        WIDTH = frameData.getWidth();
        HEIGHT = frameData.getHeight();
        X_OFFSET = frameData.getXOffset();
        Y_OFFSET = frameData.getYOffset();
        FRAME_TIME = frameData.getTime();
    }

    /**
     * Gets the length (time) of this frame.
     * @return  the length of this frame in ticks
     */
    public int getFrameTime() {
        return FRAME_TIME;
    }

    /**
     * Uploads this frame at a given position in the active texture.
     * @param point     point to upload the top-left corner of this frame at
     */
    public void uploadAt(Point point) {
        requireNonNull(point, "Point cannot be null");

        if (point.getX() < 0 || point.getY() < 0) {
            throw new IllegalArgumentException("Point coordinates must be greater than zero");
        }

        for (int level = 0; level < MIPMAPS.size(); level++) {
            int mipmappedX = point.getX() >> level;
            int mipmappedY = point.getY() >> level;

            IRGBAImage mipmap = MIPMAPS.get(level);
            int mipmappedWidth = mipmap.getWidth();
            int mipmappedHeight = mipmap.getHeight();

            if (mipmappedWidth > 0 && mipmappedHeight > 0) {
                mipmap.upload(mipmappedX, mipmappedY);
            }
        }
    }

    /**
     * Gets the width of this frame in pixels.
     * @return  the width of this frame in pixels
     */
    public int getWidth() {
        return WIDTH;
    }

    /**
     * Gets the height of this frame in pixels.
     * @return  the height of this frame in pixels
     */
    public int getHeight() {
        return HEIGHT;
    }

    /**
     * Gets the x-offset of the top-left corner of this frame in pixels.
     * @return  the x-offset of this frame in pixels
     */
    public int getXOffset() {
        return X_OFFSET;
    }

    /**
     * Gets the y-offset of the top-left corner of this frame in pixels.
     * @return  the y-offset of this frame in pixels
     */
    public int getYOffset() {
        return Y_OFFSET;
    }

    /**
     * Gets the mipmap level of this frame.
     * @return the mipmap level of this frame
     */
    public int getMipmapLevel() {
        return MIPMAPS.size() - 1;
    }

    /**
     * Gets the image associated with this frame at a certain mipmap level.
     * @param level     the mipmap level
     * @return  the image at the given mipmap level
     */
    public IRGBAImage getImage(int level) {
        if (level >= MIPMAPS.size()) {
            throw new IllegalArgumentException("There is no mipmap of level " + level);
        }

        return MIPMAPS.get(level);
    }

    /**
     * Interpolates between {@link RGBAImageFrame}s. The frames returned by this interpolator
     * are <em>not</em> unique; the mipmaps are overwritten.
     * @author soir20
     */
    public static class Interpolator implements IInterpolator<RGBAImageFrame> {
        private final RGBAInterpolator INTERPOLATOR;
        private final RGBAImageFrame FRAME;
        private int lastLevel;

        /**
         * Creates a new interpolator.
         * @param mipmaps       the mipmaps, which will be overwritten starting at (0, 0).
         *                      The mipmaps should contain only a copy of one animation frame
         *                      and be the same size as a mipmapped frame.
         */
        public Interpolator(ImmutableList<? extends IRGBAImage> mipmaps) {
            requireNonNull(mipmaps, "Mipmap list cannot be null");

            FrameReader.FrameData data = new FrameReader.FrameData(
                    mipmaps.get(0).getWidth(), mipmaps.get(0).getHeight(),
                    0, 0, 1
            );
            FRAME = new RGBAImageFrame(data, mipmaps);

            INTERPOLATOR = new RGBAInterpolator((width, height) -> mipmaps.get(lastLevel));
        }

        /**
         * Interpolates between a starting frame and an ending frame for all mipmap levels.
         * The mipmap level of the start and end frames must be greater than or equal to
         * the highest mipmap level of the provided mipmaps.
         * @param steps     total steps between the start and end frame
         * @param step      current step of the interpolation (between 1 and steps - 1)
         * @param start     the frame to start interpolation from
         * @param end       the frame to end interpolation at
         * @return  the interpolated frame at the given step
         */
        @Override
        public RGBAImageFrame interpolate(int steps, int step, RGBAImageFrame start, RGBAImageFrame end) {
            requireNonNull(start, "Start frame cannot be null");
            requireNonNull(end, "End frame cannot be null");

            if (start.getMipmapLevel() < FRAME.getMipmapLevel() || end.getMipmapLevel() < FRAME.getMipmapLevel()) {
                throw new IllegalArgumentException("The start or end frame has fewer mipmaps than the interpolator");
            }

            for (int level = 0; level <= FRAME.getMipmapLevel(); level++) {
                lastLevel = level;
                IRGBAImage startImage = start.getImage(level);
                IRGBAImage endImage = end.getImage(level);

                // We don't need to do anything with the result because the mipmaps are altered directly
                INTERPOLATOR.interpolate(steps, step, startImage, endImage);

            }

            return FRAME;
        }

    }

}
