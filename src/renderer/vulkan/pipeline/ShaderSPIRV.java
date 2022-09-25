package renderer.vulkan.pipeline;

import java.io.IOException;
import java.nio.ByteBuffer;

import org.lwjgl.system.MemoryStack;
import org.lwjgl.util.shaderc.ShadercIncludeResolve;
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease;

import utilities.ResourceReader;

import static org.lwjgl.system.MemoryUtil.NULL;
import static org.lwjgl.util.shaderc.Shaderc.*;

public class ShaderSPIRV {
	/*
	 * public class constants
	 */
	public static final int TYPE_VERTEX_SHADER = shaderc_glsl_vertex_shader;
	public static final int TYPE_FRAGMENT_SHADER = shaderc_glsl_fragment_shader;
	
    /*
     * private variables
     */
    private long handle;
    private ByteBuffer bytecode;
    
    /*
     * constructors
     */
    public ShaderSPIRV() {
        handle = NULL;
        bytecode = null;
    }

	/*
	 * public methods
	 */
    public ByteBuffer bytecode() {return bytecode;}
    
    public void compileShaderFile(String shaderFile, int type) {
    	if(NULL == handle) {
            try {
                long compiler = shaderc_compiler_initialize();
                long options = shaderc_compile_options_initialize();               
                ShadercIncludeResolve resolver = new InclusionResolver();
                ShadercIncludeResultRelease releaser = new InclusionReleaser();
                
                shaderc_compile_options_set_target_env(options, shaderc_target_env_vulkan, shaderc_env_version_vulkan_1_1);                
                shaderc_compile_options_set_include_callbacks(options, resolver, releaser, 0L);

                if(NULL == compiler) {
                	throw new RuntimeException("Failed to create shader compiler");
                } else {
                	try(MemoryStack stack = MemoryStack.stackPush()) {
	                	ByteBuffer shaderSource = ResourceReader.ioResourceToByteBuffer(shaderFile, ResourceReader.RESOURCE_TO_BYTE_BUFFER_DEFAULT_SIZE);
	                	long result = shaderc_compile_into_spv(compiler, shaderSource, type, stack.UTF8(shaderFile), stack.UTF8("main"), options);
	                	
	                    if(NULL == result) {throw new RuntimeException("Failed to compile shader " + shaderFile + " into SPIR-V");}
	
	                    if(shaderc_compilation_status_success != shaderc_result_get_compilation_status(result)) {
	                        throw new RuntimeException("Failed to compile shader " + shaderFile + "into SPIR-V:\n " + shaderc_result_get_error_message(result));
	                    }
	
	                    shaderc_compiler_release(compiler);
	                    
	                    handle = result;
	                    bytecode = shaderc_result_get_bytes(result);
	                    
	                    releaser.free();
	                    resolver.free();
                	}
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
    	} else {
    		throw new RuntimeException("Only one shader can be compiled per instance");
    	} 	
    }

    public void free() {
        shaderc_result_release(handle);
        bytecode = null;
    }
}