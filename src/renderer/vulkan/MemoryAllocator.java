package renderer.vulkan;

import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.util.vma.Vma.*;
import static org.lwjgl.vulkan.VK10.*;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.vma.VmaAllocatorCreateInfo;
import org.lwjgl.util.vma.VmaVulkanFunctions;

import renderer.vulkan.physicalDevice.PhysicalDevice;

public class MemoryAllocator {
	/*
	 * private variables
	 */
    private long allocator;
	
    /*
     * constructors
     */
	public MemoryAllocator() {
		allocator = VK_NULL_HANDLE;
	}
	
	/*
	 * public methods
	 */
	public long allocator() {return allocator;}
	
	public void create(Instance instance, PhysicalDevice physicalDevice, LogicalDevice logicalDevice) {		
		try(MemoryStack stack = stackPush()) {			
			PointerBuffer pAllocator = stack.mallocPointer(1);
			VmaAllocatorCreateInfo allocatorInfo = VmaAllocatorCreateInfo.calloc(stack);
			VmaVulkanFunctions vmaVulkanFunctions = VmaVulkanFunctions.calloc(stack);
            
			vmaVulkanFunctions.set(instance.getInstance(), logicalDevice.device());
			
			allocatorInfo.instance(instance.getInstance());
			allocatorInfo.physicalDevice(physicalDevice.physicalDevice());
			allocatorInfo.device(logicalDevice.device());
			allocatorInfo.pVulkanFunctions(vmaVulkanFunctions);

			if(VK_SUCCESS == vmaCreateAllocator(allocatorInfo, pAllocator)) {
				allocator = pAllocator.get(0);
			} else {
				throw new RuntimeException("Failed to create VMA allocator");
			}
	    }
	}
	
    public void destroy() {vmaDestroyAllocator(allocator);}
}
