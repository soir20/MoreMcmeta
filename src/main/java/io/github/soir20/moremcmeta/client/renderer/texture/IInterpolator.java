package io.github.soir20.moremcmeta.client.renderer.texture;

public interface IInterpolator<I> {

    I interpolate(int steps, int step, I start, I end);

}
