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
    public final static int STREAM_IMAGE_WIDTH = 298;
    
    /**
     * Native height for the video stream image. If you want no scaling to happen,
     * supply images of this size
     */
    public final static int STREAM_IMAGE_HEIGHT = 81;
    
    // parent is a reference to the parent sketch
    private PApplet parent;
    private DatagramSocket socket;  
    
    // Network configuration
    private String host;
    private int port;
    
    // Scale mode (crop or stretch)
    private int scaleMode = STRETCH;

    /**
     * a Constructor, usually called in the setup() method in your sketch to
     * initialize and start the Library.
     * 
     * @example Hello
     * @param parent
     */
    public VideoWall(String host, int port, PApplet parent) {
        this.host = host;
        this.port = port;
        this.parent = parent;
        parent.registerMethod("dispose", this);
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
        
        // Initialize image
        PImage streamImage = parent.createImage(STREAM_IMAGE_WIDTH, STREAM_IMAGE_HEIGHT, PConstants.RGB);
        
        // Scale image (only when not native video stream size)
        if (image.width != STREAM_IMAGE_WIDTH || image.height != STREAM_IMAGE_HEIGHT) {
            
            switch (scaleMode) {
                case CROP:
                    streamImage.copy(image, 
                            image.width / 2 - STREAM_IMAGE_WIDTH / 2, 
                            image.height / 2 - STREAM_IMAGE_HEIGHT / 2, 
                            STREAM_IMAGE_WIDTH, 
                            STREAM_IMAGE_HEIGHT, 
                            0, 0, 
                            STREAM_IMAGE_WIDTH, STREAM_IMAGE_HEIGHT);
                    break;
                case STRETCH:
                    streamImage.copy(image, 0, 0, image.width, image.height, 0, 0, STREAM_IMAGE_WIDTH, STREAM_IMAGE_HEIGHT);
                    break;
                }
        }

        // Send image packet over the network
        try {

            // Initialize buffer
            byte[] buffer = imageToBuffer(image);

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
        e.printStackTrace();
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
     * Convert image to byte buffer
     * 
     * @param image
     * @return
     */
    private static byte[] imageToBuffer(PImage image) {
        
        // Calculate pixel and buffer size
        int pixelCount = image.width * image.height;
        int bufferCount = pixelCount * 4 ;
        
        // Initialize buffer
        byte[] buffer = new byte[bufferCount + 3];
        int bufferIndex = 0;

        // Set header (IMG)
        buffer[bufferIndex++] = 0x49; // I
        buffer[bufferIndex++] = 0x4D; // M
        buffer[bufferIndex++] = 0x47; // G

        // Load image data
        image.loadPixels();

        // Add image data to buffer
        for (int i = 0; i < image.width * image.height; i++) {

            // Get color (each pixel is 32b int ARGB)
            int b = (image.pixels[i]) & 0xFF;
            int g = (image.pixels[i] >> 8) & 0xFF;
            int r = (image.pixels[i] >> 16) & 0xFF;
            int a = (image.pixels[i] >> 24) & 0xFF;

            buffer[bufferIndex++] = (byte) a;
            buffer[bufferIndex++] = (byte) r;
            buffer[bufferIndex++] = (byte) g;
            buffer[bufferIndex++] = (byte) b;

        }
        
        return buffer;

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
