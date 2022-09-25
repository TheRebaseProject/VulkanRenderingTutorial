package renderer.vulkan.swapChain;

import java.nio.LongBuffer;

import renderer.vulkan.LogicalDevice;

import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.vulkan.VK10.vkDestroyFence;
import static org.lwjgl.vulkan.VK10.vkDestroySemaphore;

public class Frame {
    /*
     * private variables
     */
    private final long fence;
    private final long imageAvailableSemaphore;
    private final long renderFinishedSemaphore;

    /*
     * constructors
     */
    public Frame(long imageAvailableSemaphore, long renderFinishedSemaphore, long fence) {
        this.fence = fence;
        this.imageAvailableSemaphore = imageAvailableSemaphore;
        this.renderFinishedSemaphore = renderFinishedSemaphore;
    }

    /*
     * public methods
     */
    public void destroy(LogicalDevice logicalDevice) {
        vkDestroySemaphore(logicalDevice.device(), renderFinishedSemaphore, null);
        vkDestroySemaphore(logicalDevice.device(), imageAvailableSemaphore, null);
        vkDestroyFence(logicalDevice.device(), fence, null);
    }
    
    public long imageAvailableSemaphore() {return imageAvailableSemaphore;}
    public LongBuffer pImageAvailableSemaphore() {return stackGet().longs(imageAvailableSemaphore);}
    public long renderFinishedSemaphore() {return renderFinishedSemaphore;}
    public LongBuffer pRenderFinishedSemaphore() {return stackGet().longs(renderFinishedSemaphore);}
    public long fence() {return fence;}
    public LongBuffer pFence() {return stackGet().longs(fence);}
}