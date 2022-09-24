package renderer;

import static org.lwjgl.system.Configuration.DEBUG;

import renderer.vulkan.Instance;

public class Renderer {
	/*
	 * public class constants
	 */
    public static final boolean ENABLE_VALIDATION_LAYERS = DEBUG.get(true);
    public static final String[] VALIDATION_LAYERS = {"VK_LAYER_KHRONOS_validation"};
    
	/*
	 * private variables
	 */
	private Instance instance;
    
    /*
     * constructors
     */
	public Renderer() {
		instance = new Instance();
	}
	
	/*
	 * public methods
	 */
	public void create() {
		instance.create();
	}
	
	public void destroy() {
		instance.destroy();
	}
}
