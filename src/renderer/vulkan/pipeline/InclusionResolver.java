package renderer.vulkan.pipeline;

import static org.lwjgl.system.MemoryUtil.memUTF8;

import java.io.IOException;

import org.lwjgl.util.shaderc.ShadercIncludeResolve;
import org.lwjgl.util.shaderc.ShadercIncludeResult;

import utilities.ResourceReader;

public class InclusionResolver extends ShadercIncludeResolve {
	/*
	 * public class constants
	 */
	public static final String COMMON_SHADER_PATH = "renderer/vulkan/pipeline/commonShaders/";
	
    /*
     * constructors
     */
	public InclusionResolver() {}
	
	/*
	 * public methods
	 */
	@Override
	public long invoke(long user_data, long requested_source, int type, long requesting_source, long include_depth) { 
        ShadercIncludeResult res = ShadercIncludeResult.calloc();
        String requestedPath = COMMON_SHADER_PATH.substring(0, COMMON_SHADER_PATH.lastIndexOf('/')) + "/" + memUTF8(requested_source);
        
        try {            
            res.content(ResourceReader.ioResourceToByteBuffer(requestedPath, ResourceReader.RESOURCE_TO_BYTE_BUFFER_DEFAULT_SIZE));
            res.source_name(memUTF8(requestedPath));
            
            return res.address();
        } catch (IOException e) {
            throw new AssertionError("Failed to resolve include: " + requestedPath);
        }
    }
}
