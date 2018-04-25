import nl.sensorlab.videowall.*;

VideoWall videoWall;

// Ball members
float x = 0;
float speed = 2;
float radius;

void settings() {
  size(800, 600);
}

void setup() {

 // Initialize the videoWall stream  
  videoWall = new VideoWall("192.168.1.90", 10233, this);
  videoWall.setScaleMode(VideoWall.CROP); // Or VideoWall.STRETCH
  videoWall.start();
  
  // Setup drawing variables
  radius = height / 2;
  ellipseMode(RADIUS);
  noStroke();
  fill(255);
  
}

void draw() {

  // Update the ball's x position
  x += speed;
  if (x > (width + radius)) {
    x = -radius;
  }

  // Draw on the canvas as you normally would
  background(255, 0, 255);
  fill(255);
  ellipse(x, radius, radius, radius);

  // Stream the canvas to video wall  
  videoWall.streamImage(get());
  
}