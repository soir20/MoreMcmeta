package io.github.soir20.moremcmeta.client.texture;

import io.github.soir20.moremcmeta.client.resource.MockResourceManager;
import io.github.soir20.moremcmeta.math.Point;
import net.minecraft.server.packs.resources.ResourceManager;
import org.junit.Test;

import java.util.Collections;

import static org.junit.Assert.*;

/**
 * Tests the {@link SingleUploadComponent}. Coverage here is somewhat low
 * because of the render system call inside the component, which can't be
 * tested. It's a small class, so the single call has a large impact on
 * test coverage.
 * @author soir20
 */
public class SingleUploadComponentTest {
    private static final ResourceManager MOCK_RESOURCE_MANAGER = new MockResourceManager(Collections.emptyList(),
            Collections.emptyList(), false);

    @Test
    public void register_NotOnRenderThread_ImageNotPrepared() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent()).getListeners());
        builder.setImage(new MockRGBAImageFrame());
        EventDrivenTexture texture = builder.build();

        // No exception means the image wasn't prepared
        texture.load(MOCK_RESOURCE_MANAGER);

    }

    @Test
    public void upload_FirstUpload_FrameUploadedAtOrigin() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent()).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();

        assertEquals(1, frame.getUploadCount());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

    @Test
    public void upload_SecondUpload_FrameUploadedAtOriginAgain() {
        EventDrivenTexture.Builder builder = new EventDrivenTexture.Builder();
        builder.add(() -> (new SingleUploadComponent()).getListeners());

        MockRGBAImageFrame frame = new MockRGBAImageFrame();
        builder.setImage(frame);
        EventDrivenTexture texture = builder.build();

        texture.upload();
        texture.upload();

        assertEquals(2, frame.getUploadCount());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(0)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(1)).getLastUploadPoint());
        assertEquals(new Point(0, 0), ((MockRGBAImage) frame.getImage(2)).getLastUploadPoint());
    }

}