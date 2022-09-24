package utilities;

import static org.lwjgl.system.MemoryStack.stackGet;

import java.nio.ByteBuffer;

import org.lwjgl.PointerBuffer;
import org.lwjgl.system.MemoryStack;

public class MemoryUtilities {
    /*
     * public class methods
     */
    public static PointerBuffer asPointerBuffer(String[] strings) {
        MemoryStack stack = stackGet();
        PointerBuffer pointerBuffer = stack.mallocPointer(strings.length);
        
        for(int i = 0; i < strings.length; i++) {
        	ByteBuffer byteBuffer = stack.UTF8(strings[i]);
        	pointerBuffer.put(byteBuffer);
        }

        return pointerBuffer.rewind();
    }
}
