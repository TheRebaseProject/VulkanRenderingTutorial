package renderer.vulkan.physicalDevice;

import java.nio.IntBuffer;

import org.lwjgl.vulkan.VkSurfaceCapabilitiesKHR;
import org.lwjgl.vulkan.VkSurfaceFormatKHR;

public class SwapChainSupportInfo {
	/*
	 * private variables
	 */
    private VkSurfaceCapabilitiesKHR capabilities;
    private VkSurfaceFormatKHR.Buffer formats;
    private IntBuffer presentModes;
    
    /*
     * constructors
     */
    public SwapChainSupportInfo() {
    	capabilities = null;
    	formats = null;
    	presentModes = null;
    }
    
    /*
     * public methods
     */
    public VkSurfaceCapabilitiesKHR getCapabilities() {return capabilities;}
    public VkSurfaceFormatKHR.Buffer getFormats() {return formats;}
    public IntBuffer getPresentModes() {return presentModes;}
    public void setCapabilities(VkSurfaceCapabilitiesKHR capabilities) {this.capabilities = capabilities;}
    public void setFormats(VkSurfaceFormatKHR.Buffer formats) {this.formats = formats;}
    public void setPresentModes(IntBuffer presentModes) {this.presentModes = presentModes;}
}
