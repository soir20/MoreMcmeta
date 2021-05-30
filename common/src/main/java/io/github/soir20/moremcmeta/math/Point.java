package io.github.soir20.moremcmeta.math;

/**
 * A pair of coordinates in a plane.
 * @author soir20
 */
public class Point {
    private final int X_POS;
    private final int Y_POS;

    /**
     * Creates a new point.
     * @param xPos      horizontal coordinate of the point
     * @param yPos      vertical coordinate of the point
     */
    public Point(int xPos, int yPos) {
        X_POS = xPos;
        Y_POS = yPos;
    }

    /**
     * Gets the horizontal coordinate of the point.
     * @return x coordinate of the point
     */
    public int getX() {
        return X_POS;
    }

    /**
     * Gets the vertical coordinate of the point.
     * @return y coordinate of the point
     */
    public int getY() {
        return Y_POS;
    }

    /**
     * Determines if another object is the same as this point. Two points
     * are equal if their x and y coordinates are the same.
     * @param other     the other object to compare this point with
     * @return whether the this point and the other object are equal
     */
    @Override
    public boolean equals(Object other) {
        if (!(other instanceof Point)) {
            return false;
        }

        Point otherPoint = (Point) other;

        return X_POS == otherPoint.X_POS && Y_POS == otherPoint.Y_POS;
    }

    /**
     * Gets the hash code for this point.
     * @return the hash code for this point
     */
    @Override
    public int hashCode() {
        return 31 * X_POS + Y_POS;
    }

}
