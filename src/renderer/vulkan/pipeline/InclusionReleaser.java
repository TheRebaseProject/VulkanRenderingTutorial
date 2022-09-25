package renderer.vulkan.pipeline;

import static org.lwjgl.system.MemoryUtil.memFree;

import org.lwjgl.util.shaderc.ShadercIncludeResult;
import org.lwjgl.util.shaderc.ShadercIncludeResultRelease;

public class InclusionReleaser extends ShadercIncludeResultRelease {
    /*
     * constructors
     */
	public InclusionReleaser() {}
	
	/*
	 * public methods
	 */
	@Override
	public void invoke(long user_data, long include_result) {
        ShadercIncludeResult result = ShadercIncludeResult.create(include_result);
        memFree(result.source_name());
        result.free();
    }
}
