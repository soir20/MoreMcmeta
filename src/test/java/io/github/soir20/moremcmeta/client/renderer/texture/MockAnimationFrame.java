package io.github.soir20.moremcmeta.client.renderer.texture;

public class MockAnimationFrame implements IAnimationFrame {
    private final int WIDTH;
    private final int HEIGHT;
    private final int X_OFFSET;
    private final int Y_OFFSET;
    private final int TIME;

    public MockAnimationFrame(FrameReader.FrameData frameData) {
        WIDTH = frameData.getWidth();
        HEIGHT = frameData.getHeight();
        X_OFFSET = frameData.getXOffset();
        Y_OFFSET = frameData.getYOffset();
        TIME = frameData.getTime();
    }

    public int getWidth() {
        return WIDTH;
    }

    public int getHeight() {
        return HEIGHT;
    }

    public int getXOffset() {
        return X_OFFSET;
    }

    public int getYOffset() {
        return Y_OFFSET;
    }

    @Override
    public int getFrameTime() {
        return TIME;
    }

    @Override
    public void uploadAt(int x, int y) {}
}
