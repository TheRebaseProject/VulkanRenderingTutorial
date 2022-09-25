package renderer.vulkan.swapChain;

import static org.lwjgl.glfw.GLFW.glfwGetFramebufferSize;
import static org.lwjgl.system.MemoryStack.stackGet;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.vulkan.KHRSurface.*;
import static org.lwjgl.vulkan.KHRSwapchain.*;
import static org.lwjgl.vulkan.VK10.*;

import java.nio.IntBuffer;
import java.nio.LongBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.vulkan.VkCommandBuffer;
import org.lwjgl.vulkan.VkExtent2D;
import org.lwjgl.vulkan.VkFenceCreateInfo;
import org.lwjgl.vulkan.VkPresentInfoKHR;
import org.lwjgl.vulkan.VkSemaphoreCreateInfo;
import org.lwjgl.vulkan.VkSubmitInfo;
import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;
import org.lwjgl.vulkan.VkSwapchainCreateInfoKHR;

import main.WindowManager;
import renderer.vulkan.CommandBuffer;
import renderer.vulkan.CommandPool;
import renderer.vulkan.LogicalDevice;
import renderer.vulkan.framebuffer.Framebuffer;
import renderer.vulkan.image.ImageView;
import renderer.vulkan.physicalDevice.PhysicalDevice;
import renderer.vulkan.physicalDevice.QueueFamilyIndices;
import renderer.vulkan.physicalDevice.SwapChainSupportInfo;
import renderer.vulkan.renderPass.RenderPassPresentation;

public class SwapChain {
	/*
	 * private class constants
	 */
    private static final int MAX_FRAMES_IN_FLIGHT = 2;
    private static final int UINT32_MAX = 0xFFFFFFFF;
    private static final long UINT64_MAX = 0xFFFFFFFFFFFFFFFFL;
    
	/*
	 * private variables
	 */
    private List<CommandBuffer> commandBuffers;
    private CommandPool commandPool;
	private List<Framebuffer> framebuffers;
    private VkExtent2D imageExtent;
	private int imageFormat;
    private List<Long> images;
    private Map<Integer, Frame> imagesInFlight;
	private List<ImageView> imageViews;
	private int inFlightFrameIndex;
	private List<Frame> inFlightFrames;
	private int nextImageIndex;
	private RenderPassPresentation renderPassPresentation;
	private long swapChain;
	
    /*
     * constructors
     */
	public SwapChain() {
        commandBuffers = new ArrayList<>();
        commandPool = new CommandPool();
		framebuffers = new ArrayList<>();
		imageExtent = null;
		imageFormat = 0;
		images = new ArrayList<>();
		imagesInFlight = null;
		imageViews = new ArrayList<>();
		inFlightFrameIndex = 0;
		inFlightFrames = null;
		nextImageIndex = -1;
		renderPassPresentation = new RenderPassPresentation();
		swapChain = VK_NULL_HANDLE;		
	}
	
	/*
	 * public methods
	 */
	public int acquireNextImage(LogicalDevice logicalDevice) {
		try(MemoryStack stack = stackPush()) {
			IntBuffer pImageIndex = stack.mallocInt(1);
			Frame nextFrame = inFlightFrames.get(inFlightFrameIndex);
			int vkResult = VK_SUCCESS;
			
	        vkWaitForFences(logicalDevice.device(), nextFrame.pFence(), true, UINT64_MAX);

	        vkResult = vkAcquireNextImageKHR(logicalDevice.device(),
	        		swapChain,
	        		UINT64_MAX,
	        		nextFrame.imageAvailableSemaphore(),
	        		VK_NULL_HANDLE,
	        		pImageIndex);
	        
	        if(VK_SUCCESS == vkResult) {
	        	nextImageIndex = pImageIndex.get(0);
	        } else {
	        	throw new RuntimeException("Failed to acquire next image from swap chain");
	        }
	        
	        return vkResult;
		}
	}
	
	public void create(PhysicalDevice physicalDevice, LogicalDevice logicalDevice) {
        try(MemoryStack stack = stackPush()) {
        	long surface = WindowManager.getSurface();
            SwapChainSupportInfo supportInfo = physicalDevice.querySwapChainSupport(physicalDevice.physicalDevice(), surface, stack);
            VkSurfaceFormatKHR surfaceFormat = chooseSurfaceFormat(supportInfo.getFormats());
            int presentMode = choosePresentMode(supportInfo.getPresentModes());
            VkExtent2D extent = chooseImageExtent(supportInfo.getCapabilities(), WindowManager.getWindow());
            IntBuffer imageCount = stack.ints(supportInfo.getCapabilities().minImageCount() + 1);
            VkSwapchainCreateInfoKHR createInfo = VkSwapchainCreateInfoKHR.calloc(stack);
            QueueFamilyIndices indices = physicalDevice.getQueueFamilyIndicies();
            LongBuffer pSwapChain = stack.longs(VK_NULL_HANDLE);
            LongBuffer pSwapchainImages;

            if(supportInfo.getCapabilities().maxImageCount() > 0 && imageCount.get(0) > supportInfo.getCapabilities().maxImageCount()) {
                imageCount.put(0, supportInfo.getCapabilities().maxImageCount());
            }

            createInfo.sType(VK_STRUCTURE_TYPE_SWAPCHAIN_CREATE_INFO_KHR);
            createInfo.surface(surface);
            createInfo.minImageCount(imageCount.get(0));
            createInfo.imageFormat(surfaceFormat.format());
            createInfo.imageColorSpace(surfaceFormat.colorSpace());
            createInfo.imageExtent(extent);
            createInfo.imageArrayLayers(1);
            createInfo.imageUsage(VK_IMAGE_USAGE_COLOR_ATTACHMENT_BIT);

            if(!indices.getGraphicsFamily().equals(indices.getPresentFamily())) {
                createInfo.imageSharingMode(VK_SHARING_MODE_CONCURRENT);
                createInfo.pQueueFamilyIndices(stack.ints(indices.getGraphicsFamily(), indices.getPresentFamily()));
            } else {
                createInfo.imageSharingMode(VK_SHARING_MODE_EXCLUSIVE);
            }

            createInfo.preTransform(supportInfo.getCapabilities().currentTransform());
            createInfo.compositeAlpha(VK_COMPOSITE_ALPHA_OPAQUE_BIT_KHR);
            createInfo.presentMode(presentMode);
            createInfo.clipped(true);
            createInfo.oldSwapchain(VK_NULL_HANDLE);

            if(VK_SUCCESS == vkCreateSwapchainKHR(logicalDevice.device(), createInfo, null, pSwapChain)) {
                swapChain = pSwapChain.get(0);

                if(VK_SUCCESS == vkGetSwapchainImagesKHR(logicalDevice.device(), swapChain, imageCount, null)) {
                	pSwapchainImages = stack.mallocLong(imageCount.get(0));
                	
                    if(VK_SUCCESS != vkGetSwapchainImagesKHR(logicalDevice.device(), swapChain, imageCount, pSwapchainImages)) {
                    	throw new RuntimeException("Failed to get swap chain images");
                    }

                    for(int i = 0; i < pSwapchainImages.capacity(); i++) {images.add(pSwapchainImages.get(i));}

                    imageFormat = surfaceFormat.format();
                    imageExtent = VkExtent2D.create().set(extent);
                    
                    createImageViews(logicalDevice);
                    renderPassPresentation.create(logicalDevice, imageFormat);
                    createFramebuffers(logicalDevice);
                    commandPool.create(physicalDevice, logicalDevice, VK_COMMAND_POOL_CREATE_RESET_COMMAND_BUFFER_BIT);
                    allocateCommandBuffers(logicalDevice);
                    createSyncObjects(logicalDevice);
                } else {
                	throw new RuntimeException("Failed to get swap chain image count");
                }
            } else {
                throw new RuntimeException("Failed to create swap chain");
            }
        }
    }
    
    public void destroy(LogicalDevice logicalDevice) {
    	destroySyncObjects(logicalDevice);
        destroyFramebuffers(logicalDevice);
        freeCommandBuffers(logicalDevice);
        commandPool.destroy(logicalDevice);
    	renderPassPresentation.destroy(logicalDevice);
        destroyImageViews(logicalDevice);
        vkDestroySwapchainKHR(logicalDevice.device(), swapChain, null);
        images.clear();
    }

    public List<CommandBuffer> getCommandBuffers() {return commandBuffers;}
    public List<Framebuffer> getFramebuffers() {return framebuffers;}
    public VkExtent2D getImageExtent() {return imageExtent;}
    public int getImageFormat() {return imageFormat;}
    public List<Long> getImages() {return images;}
    public int getNextImageIndex() {return nextImageIndex;}
    public RenderPassPresentation getRenderPass() {return renderPassPresentation;}
	public long getSwapChain() {return swapChain;}
    
    public int present(LogicalDevice logicalDevice) {
    	int result = VK_ERROR_DEVICE_LOST;
    	
    	try(MemoryStack stack = stackPush()) {
    		Frame currentFrame = inFlightFrames.get(inFlightFrameIndex);
            VkPresentInfoKHR presentInfo = VkPresentInfoKHR.calloc(stack);
            IntBuffer pImageIndex = stack.ints(nextImageIndex);
            
            presentInfo.sType(VK_STRUCTURE_TYPE_PRESENT_INFO_KHR);
            presentInfo.pWaitSemaphores(currentFrame.pRenderFinishedSemaphore());
            presentInfo.swapchainCount(1);
            presentInfo.pSwapchains(stack.longs(swapChain));
            presentInfo.pImageIndices(pImageIndex);

            result = vkQueuePresentKHR(logicalDevice.getPresentQueue(), presentInfo);
    	}
    	
    	if(VK_SUCCESS == result || VK_ERROR_OUT_OF_DATE_KHR == result || VK_SUBOPTIMAL_KHR == result) {
    		inFlightFrameIndex = (inFlightFrameIndex + 1) % MAX_FRAMES_IN_FLIGHT;
    	}
        
    	return result;
    }
    
    public int submitDrawCommand(LogicalDevice logicalDevice, VkCommandBuffer commandBuffer) {
    	int result = VK_ERROR_DEVICE_LOST;
    	
    	try(MemoryStack stack = stackPush()) {
    		Frame currentFrame = inFlightFrames.get(inFlightFrameIndex);
    		VkSubmitInfo submitInfo = VkSubmitInfo.calloc(stack);
    		
            if(imagesInFlight.containsKey(nextImageIndex)) {
                vkWaitForFences(logicalDevice.device(), imagesInFlight.get(nextImageIndex).fence(), true, UINT64_MAX);
            }

            imagesInFlight.put(nextImageIndex, currentFrame);

            submitInfo.sType(VK_STRUCTURE_TYPE_SUBMIT_INFO);
            submitInfo.waitSemaphoreCount(1);
            submitInfo.pWaitSemaphores(currentFrame.pImageAvailableSemaphore());
            submitInfo.pWaitDstStageMask(stack.ints(VK_PIPELINE_STAGE_COLOR_ATTACHMENT_OUTPUT_BIT));
            submitInfo.pSignalSemaphores(currentFrame.pRenderFinishedSemaphore());
            submitInfo.pCommandBuffers(stack.pointers(commandBuffer));

            vkResetFences(logicalDevice.device(), currentFrame.pFence());

            result = vkQueueSubmit(logicalDevice.getGraphicsQueue(), submitInfo, currentFrame.fence());
    	}
    	
    	return result;
    }
	
	/*
	 * private methods
	 */
    private void allocateCommandBuffers(LogicalDevice logicalDevice) {        
        for(int i = 0; i < framebuffers.size(); i++) {
            CommandBuffer commandBuffer = new CommandBuffer(commandPool);
            commandBuffer.allocate(logicalDevice);
            commandBuffers.add(commandBuffer);
        }
    }
    
    private VkExtent2D chooseImageExtent(VkSurfaceCapabilitiesKHR capabilities, long window) {
    	try(MemoryStack stack = stackPush()) {
	    	VkExtent2D actualExtent = capabilities.currentExtent();
	
	        if(UINT32_MAX == capabilities.currentExtent().width()) {
	            IntBuffer width = stackGet().ints(0);
	            IntBuffer height = stackGet().ints(0);
	            VkExtent2D minExtent = capabilities.minImageExtent();
	            VkExtent2D maxExtent = capabilities.maxImageExtent();
	
	            glfwGetFramebufferSize(window, width, height);
	
	            actualExtent = VkExtent2D.malloc(stack).set(width.get(0), height.get(0));
	            actualExtent.width(clamp(minExtent.width(), maxExtent.width(), actualExtent.width()));
	            actualExtent.height(clamp(minExtent.height(), maxExtent.height(), actualExtent.height()));
	        }
	
	        return actualExtent;
    	}
    }
    
    private int choosePresentMode(IntBuffer availablePresentModes) {
    	int presentMode = VK_PRESENT_MODE_IMMEDIATE_KHR;
    	
        for(int i = 0; i < availablePresentModes.capacity(); i++) {
        	int availableMode = availablePresentModes.get(i);
        	
            if(VK_PRESENT_MODE_MAILBOX_KHR == availableMode) {
                presentMode = availableMode;
                break;
            }
        }

        return presentMode;
    }
    
    private VkSurfaceFormatKHR chooseSurfaceFormat(VkSurfaceFormatKHR.Buffer availableFormats) {
    	VkSurfaceFormatKHR chosenFormat = availableFormats.get(0);
    	
    	for(int i = 0; i < availableFormats.capacity(); i++) {
    		VkSurfaceFormatKHR availableFormat = availableFormats.get(i);
    		
    		if(availableFormat.format() == VK_FORMAT_B8G8R8A8_UNORM && availableFormat.colorSpace() == VK_COLOR_SPACE_SRGB_NONLINEAR_KHR) {
    			chosenFormat = availableFormat;
    			break;
    		}
    	}
        
        return chosenFormat;
    }
    
    private int clamp(int min, int max, int value) {return Math.max(min, Math.min(max, value));}
    
    private void createFramebuffers(LogicalDevice logicalDevice) {        
        for(ImageView imageView : imageViews) {
        	Framebuffer framebuffer = new Framebuffer();
        	framebuffer.create(logicalDevice, renderPassPresentation.renderPass(), imageExtent.height(), imageExtent.width(), imageView.getImageView());
        	framebuffers.add(framebuffer);
        }
    }
    
    private void createImageViews(LogicalDevice logicalDevice) {
        for(long image : images) {
        	ImageView imageView = new ImageView();
        	imageView.create(logicalDevice, image, VK_IMAGE_VIEW_TYPE_2D, 1, imageFormat, VK_IMAGE_ASPECT_COLOR_BIT, 1);
        	imageViews.add(imageView);
        }
    }
    
    private void createSyncObjects(LogicalDevice logicalDevice) {
        inFlightFrames = new ArrayList<>(MAX_FRAMES_IN_FLIGHT);
        imagesInFlight = new HashMap<>(images.size());

        try(MemoryStack stack = stackPush()) {
            VkSemaphoreCreateInfo semaphoreInfo = VkSemaphoreCreateInfo.calloc(stack);
            VkFenceCreateInfo fenceInfo = VkFenceCreateInfo.calloc(stack);
            LongBuffer pImageAvailableSemaphore = stack.mallocLong(1);
            LongBuffer pRenderFinishedSemaphore = stack.mallocLong(1);
            LongBuffer pFence = stack.mallocLong(1);
            
            semaphoreInfo.sType(VK_STRUCTURE_TYPE_SEMAPHORE_CREATE_INFO);

            fenceInfo.sType(VK_STRUCTURE_TYPE_FENCE_CREATE_INFO);
            fenceInfo.flags(VK_FENCE_CREATE_SIGNALED_BIT);

            for(int i = 0; i < MAX_FRAMES_IN_FLIGHT; i++) {
                if(VK_SUCCESS!= vkCreateSemaphore(logicalDevice.device(), semaphoreInfo, null, pImageAvailableSemaphore) ||
                		VK_SUCCESS != vkCreateSemaphore(logicalDevice.device(), semaphoreInfo, null, pRenderFinishedSemaphore) ||
                		VK_SUCCESS != vkCreateFence(logicalDevice.device(), fenceInfo, null, pFence)) {
                    throw new RuntimeException("Failed to create synchronization objects for the frame " + i);
                }

                inFlightFrames.add(new Frame(pImageAvailableSemaphore.get(0), pRenderFinishedSemaphore.get(0), pFence.get(0)));
            }
        }
    }
    
    private void freeCommandBuffers(LogicalDevice logicalDevice) {
        commandBuffers.forEach(commandBuffer -> commandBuffer.freeCommandBuffer(logicalDevice));
        commandBuffers.clear();
    }
    
    private void destroyFramebuffers(LogicalDevice logicalDevice) {
    	for(Framebuffer framebuffer : framebuffers) {framebuffer.destroy(logicalDevice);}    	
    	framebuffers.clear();
    }
    
    private void destroyImageViews(LogicalDevice logicalDevice) {
        imageViews.forEach(imageView -> imageView.destroy(logicalDevice));
        imageViews.clear();
    }
    
    private void destroySyncObjects(LogicalDevice logicalDevice) {for(Frame frame : inFlightFrames) {frame.destroy(logicalDevice);}}
}
