/*
 * Continuous Bezier Curves
 * See doc at https://processing.org/reference/bezier_.html
 */
 
int OPTION = 3;

void setup() {
  size(150, 150);
  background(255);
  smooth();
  // Don't show where control points are
  noFill();
  stroke(0);
  beginShape();
  if (OPTION == 1) {  
    vertex(50, 75); // first point
    bezierVertex(25, 25, 125, 25, 100, 75);
  } else if (OPTION == 2) {
    vertex(30, 70); // first point
    bezierVertex(25, 25, 100, 50, 50, 100);
    bezierVertex(50, 140, 75, 140, 120, 120);
  } else if (OPTION == 3) {
    vertex(30, 70); // first point
    bezierVertex(25, 25, 100, 50, 50, 100);
    bezierVertex(20, 130, 75, 140, 120, 120);  
  }
  endShape();
}
