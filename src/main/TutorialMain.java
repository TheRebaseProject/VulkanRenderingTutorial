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
    private Application application;
    private Renderer renderer;
    
    /*
     * constructors
     */
    public TutorialMain() {
    	application = new Application();
    	renderer = new Renderer();
    }
    
    /*
     * private methods
     */
    private void pollInputs() {
    	glfwPollEvents();
    }
    
    private void render() {
        application.render(renderer);
    }
    
    private void run() {
    	WindowManager.createWindow();
    	renderer.create();
    	application.init(renderer);
    	
    	while(!WindowManager.windowShouldClose()) {
    		pollInputs();
    		render();
    	}
    	
    	renderer.waitForDeviceIdle();
    	application.deinit(renderer);
    	renderer.destroy();
		WindowManager.destroyWindow();
		System.exit(0);
    }
}
