import nl.sensorlab.videowall.*;

VideoWall videoWall;

void setup() {

  // Set the canvas to the native image size to prevent scaling
  // (this saves performance)
  size(VideoWall.STREAM_IMAGE_WIDTH, VideoWall.STREAM_IMAGE_HEIGHT);

  // Initialize the videoWall stream  
  videoWall = new VideoWall("192.168.1.20", 10233, this);
  videoWall.start();
  
}

void draw() {

  // Draw on the canvas as you normally would
  background(0);
  fill(255);

  // Stream the canvas to video wall  
  videoWall.streamImage(get());
  
}