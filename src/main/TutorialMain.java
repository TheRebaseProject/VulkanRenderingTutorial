package main;

import static org.lwjgl.glfw.GLFW.glfwPollEvents;

import renderer.Renderer;

public class TutorialMain {
	/*
	 * public class methods
	 */
    public static void main(String[] args) {new TutorialMain().run();}
    
    /*
     * private variables
     */
    private Renderer renderer;
    
    /*
     * constructors
     */
    public TutorialMain() {
    	renderer = new Renderer();
    }
    
    /*
     * private methods
     */
    private void pollInputs() {
    	glfwPollEvents();
    }
    
    private void run() {
    	WindowManager.createWindow();
    	renderer.create();
    	
    	while(!WindowManager.windowShouldClose()) {
    		pollInputs();
    	}
    	
    	renderer.waitForDeviceIdle();
    	renderer.destroy();
		WindowManager.destroyWindow();
		System.exit(0);
    }
}
