import nl.sensorlab.videowall.*;

VideoWall videoWall;

void setup() {
  size(800, 600);

  // Initialize the videoWall stream  
  videoWall = new VideoWall("192.168.1.49", 10233, this);
  videoWall.setScaleMode(VideoWall.STRETCH); // or VideoWall.CROP to crop
  videoWall.start();
  
}

void draw() {

  // Draw on the canvas as you normally would
  background(0);
  fill(255);

  // Stream the canvas to video wall  
  videoWall.streamImage(get());
  
}