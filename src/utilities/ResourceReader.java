package utilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

import org.lwjgl.BufferUtils;

public class ResourceReader extends Thread {
	/*
	 * public class constants
	 */
	public static final int RESOURCE_TO_BYTE_BUFFER_DEFAULT_SIZE = 1024;
	
	/*
	 * private class constants
	 */
	private static final int RESOURCE_TO_BYTE_BUFFER_BYTE_ARRAY_SIZE = 8192;
	
	/*
	 * public class methods
	 */
    public static ByteBuffer ioResourceToByteBuffer(String resource, int bufferSize) throws IOException {
        ByteBuffer buffer;
        URL url = Thread.currentThread().getContextClassLoader().getResource(resource);
        
        if(null != url) {
            File file = new File(url.getFile());
            
            if(file.isFile()) {
                FileInputStream fis = new FileInputStream(file);
                FileChannel fc = fis.getChannel();
                
                buffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
                fc.close();
                fis.close();
            } else {
                buffer = BufferUtils.createByteBuffer(bufferSize);
                InputStream source = url.openStream();
                
                if(null != source) {
                    try {
                        byte[] buf = new byte[RESOURCE_TO_BYTE_BUFFER_BYTE_ARRAY_SIZE];
                        
                        while(true) {
                            int bytes = source.read(buf, 0, buf.length);
                            
                            if (bytes == -1) {break;}
                            
                            if (buffer.remaining() < bytes) {
                                buffer = resizeBuffer(buffer, Math.max(buffer.capacity() * 2, buffer.capacity() - buffer.remaining() + bytes));
                            }
                            
                            buffer.put(buf, 0, bytes);
                        }
                        
                        buffer.flip();
                    } finally {
                        source.close();
                    }
                } else {
                	throw new FileNotFoundException(resource);
                }
            }
        } else {
        	throw new IOException("Classpath resource not found: " + resource);
        }
        
        return buffer;
    }
    
    /*
     * private class methods
     */
    private static ByteBuffer resizeBuffer(ByteBuffer buffer, int newCapacity) {
        ByteBuffer newBuffer = BufferUtils.createByteBuffer(newCapacity);
        buffer.flip();
        newBuffer.put(buffer);
        
        return newBuffer;
    }
}
