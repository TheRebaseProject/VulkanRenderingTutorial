package renderer.vulkan.pipeline.graphics.uniformColor;

import renderer.vulkan.pipeline.graphics.ShaderStagesGraphics;

public class ShaderStagesUniformColor extends ShaderStagesGraphics {
    /*
     * private class constants
     */
	private static final String SHADER_PATH_FRAGMENT = SHADER_PATH_BASE + "graphics/uniformColor/uniformColor.frag";
	private static final String SHADER_PATH_VERTEX = SHADER_PATH_BASE + "graphics/uniformColor/uniformColor.vert";
	
	/*
	 * constructors
	 */
	public ShaderStagesUniformColor() {
		super();
	}
	
	/*
	 * protected methods
	 */
	@Override
	protected String getShaderPathFragment() {return SHADER_PATH_FRAGMENT;}
	@Override
	protected String getShaderPathVertex() {return SHADER_PATH_VERTEX;}
}
