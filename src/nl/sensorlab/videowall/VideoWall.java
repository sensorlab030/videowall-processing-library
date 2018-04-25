package nl.sensorlab.videowall;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import processing.core.PApplet;
import processing.core.PConstants;
import processing.core.PImage;

/**
 * Library for streaming images to the Sensor Lab Video Wall; a simple way to stream
 * the output of Processing sketches over the network to the video wall  
 * 
 * @example SimpleCanvasStreaming
 */
public class VideoWall {
    
    // Library version
    public final static String VERSION = "##library.prettyVersion##";
    
    // Scaling constants
    /**
     * Resize the image by taking a rectangle of the native stream size from the
     * center of the image
     */
    public static final int CROP = 1;
    
    /**
     * Resize the image by stretching the source image to the size of the native
     * stream image size
     */
    public static final int STRETCH = 2;
    
    // Stream size constants
    /**
     * Native width for the video stream image. If you want no scaling to happen,
     * supply images of this size
     */
    public final static int STREAM_IMAGE_WIDTH = 280;
    
    /**
     * Native height for the video stream image. If you want no scaling to happen,
     * supply images of this size
     */
    public final static int STREAM_IMAGE_HEIGHT = 76;
    
    /**
     * Length of the data buffer calculated as:
     * number of pixels * 3 (3 bytes for 3 colors, RGB) + 3 bytes of the IMG start of the packet
     */
    private final static int BUFFER_LENGTH = STREAM_IMAGE_WIDTH * STREAM_IMAGE_HEIGHT * 3 + 3;
    
    /**
     * The parent sketch
     */
    private PApplet parent;
    
    /**
     * The UDP socket, will be connected to host:port
     */ 
    private DatagramSocket socket;  
    
    /**
     * The host (host name or IP) to send the UPD packets to
     */     
    private String host;
    
    /**
     * The port to send the UDP packets to
     */
    private int port;
    
    // Scale mode (crop or stretch)
    private int scaleMode = STRETCH;
    
    private byte[] buffer;

    /**
     * a Constructor, usually called in the setup() method in your sketch to
     * initialize and start the Library.
     * 
     * @example SimpleCanvasStreaming
     * 
     * @param host      the network host (hostname or IP) to send the UDP packets to
     * @param port      the network port to send the UDP packets to
     * @param parent    the parent sketch
     */
    public VideoWall(String host, int port, PApplet parent) {
        this.host = host;
        this.port = port;
        this.parent = parent;
        parent.registerMethod("dispose", this);
        
        // Initialize buffer with packet header
        buffer = new byte[BUFFER_LENGTH];
        buffer[0] = 0x49; // I
        buffer[1] = 0x4D; // M
        buffer[2] = 0x47; // G
    }
    
    /**
     * Set the scale mode for scaling supplied images to the correct stream image size.
     * 
     * @param scaleMode either VideoWall.NOSCALE, VideoWall.CROP or VideoWall.STRETCH
     */
    public void setScaleMode(int scaleMode) {
        if (scaleMode >= CROP && scaleMode <= STRETCH) {
            this.scaleMode = scaleMode;
        } else {
            System.err.println("Unrecognized scale mode: " + scaleMode);
        }
    }

    /**
     * Get the scale mode for scaling the supplied images to the correct stream image size.
     */
    public int scaleMode() {
        return scaleMode;
    }
    
    /**
     * return the version of the Library.
     * 
     * @return String
     */
    public static String version() {
        return VERSION;
    }
    
    /**
     * Stream image to the video wall
     * 
     * @param image
     */
    public void streamImage(PImage image) {
        
        // Handle disconnected socket
        if (!socket.isConnected()) {
            System.err.println("Failed to stream image, socket is not connected");
            return;
        }
        
        // Create stream image from supplied image
        PImage streamImage = parent.createImage(STREAM_IMAGE_WIDTH, STREAM_IMAGE_HEIGHT, PConstants.RGB);        
        if (image.width == STREAM_IMAGE_WIDTH && image.height == STREAM_IMAGE_HEIGHT) {
          
          // Native size, no scaling needed
          streamImage = image.copy();
          
        } else if (scaleMode == CROP) {
          
          // Crop center portion of the iamge
          streamImage.copy(image, 
                        image.width / 2 - STREAM_IMAGE_WIDTH / 2, 
                        image.height / 2 - STREAM_IMAGE_HEIGHT / 2, 
                        STREAM_IMAGE_WIDTH, 
                        STREAM_IMAGE_HEIGHT, 
                        0, 0, 
                        STREAM_IMAGE_WIDTH, STREAM_IMAGE_HEIGHT);
                        
        } else if (scaleMode == STRETCH) {
          
          // Stretch source image to stream image
          streamImage.copy(image, 0, 0, image.width, image.height, 0, 0, STREAM_IMAGE_WIDTH, STREAM_IMAGE_HEIGHT);
          
        }
        
        // Update buffer with new image data
        int bufferIndex = 3;
        streamImage.loadPixels();
        for (int i = 0; i < streamImage.pixels.length; i++) {

            // Add colors to the buffer (each pixel is 32b int ARGB)
            //buffer[bufferIndex++] = (byte) ((streamImage.pixels[i] >> 24) & 0xFF);  // A (We don't send alpha values to save bandwidth)
            buffer[bufferIndex++] = (byte) ((streamImage.pixels[i] >> 16) & 0xFF);    // R
            buffer[bufferIndex++] = (byte) ((streamImage.pixels[i] >> 8) & 0xFF);     // G
            buffer[bufferIndex++] = (byte) ((streamImage.pixels[i]) & 0xFF);          // B

        }

        try {

            // Create and send packet
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            socket.send(packet);

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Open a UDP socket to the host
     */
    private void openSocket() {
        
      try {
        socket = new DatagramSocket();
        socket.connect(InetAddress.getByName(host), port);
      } catch (SocketException e) {
        e.printStackTrace();
      } catch (UnknownHostException e) {
        System.err.println("The host could not be found: " + host);
      }
          
    }
    
    /**
     * Close the socket
     */
    private void closeSocket() {
        socket.disconnect();
    }
    
    /**
     * Open the socket
     */
    public void start() {
        openSocket();
    }
    
    /**
     * Close the socket
     */
    public void dispose() {
        closeSocket();
    }

}
