
void setup() {
  size(400,400);
  noFill();
  // 1 - the bezier curve
  bezier(340, 80, 
         40, 40, 
         360, 360, 
         60, 320);
  // 2 - the control points, linked
  stroke(255, 102, 0); // Orange
  fill(255, 102, 0); // Orange
  // Plot
  ellipse(340, 80, 12, 12);
  ellipse(40, 40, 12, 12);    
  ellipse(360, 360, 12, 12);    
  ellipse(60, 320, 12, 12);    
  // Draw
  line(340, 80, 40, 40);    
  line(40, 40, 360, 360);    
  line(360, 360, 60, 320);    
  // 3 - 10 points, on the curve  
  fill(255);
  stroke(0); // Black
  int steps = 10;
  for (int i = 0; i <= steps; i++) {
    float t = i / float(steps); // t in [0..1]
    float x = bezierPoint(340, 40, 360, 60, t);
    float y = bezierPoint(80, 40, 360, 320, t);
    ellipse(x, y, 10, 10);
  }
}
