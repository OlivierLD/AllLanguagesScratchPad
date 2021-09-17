/*
 * Continuous Bezier Curves
 * See doc at https://processing.org/reference/bezier_.html
 *
 * see bezierPoint(), curvePoint()...
 * https://processing.org/reference/curvePoint_.html
 *
 * Bezier Vertex are Control Points
 */
 
// Below: Change at will 
int OPTION = 3;
boolean showControlPoints = true;

void setup() {
  size(150, 150);
  background(255);
  smooth();
  noFill();

  // stroke(0);
  beginShape();
  if (OPTION == 1) {  
    int[][] points = {{ 50, 75},
                      { 25, 25}, 
                      {125, 25}, 
                      {100, 75}};
    if (showControlPoints) {
      stroke(255, 102, 0); // Orange
      for (int i=0; i<(points.length - 1); i++) {
        line(points[i][0], points[i][1], points[i+1][0], points[i+1][1]);    
      }
      // Circles on the points
      fill(255, 0, 0);
      for (int i=1; i<(points.length - 1); i++) {
        ellipse(points[i][0], points[i][1], 3, 3);
      }
      noFill();
    }
    stroke(0); // Black
    // vertex(50, 75); // first point
    // bezierVertex(25, 25, 125, 25, 100, 75);
    vertex(points[0][0], points[0][1]); // first point
    bezierVertex(points[1][0], points[1][1], 
                 points[2][0], points[2][1],
                 points[3][0], points[3][1]);
  } else if (OPTION == 2) {
    int[][] points = {{30, 70}, 
                      {25, 25}, {100, 50}, {50, 100}, 
                      {50, 140}, {75, 140}, {120, 120}};
    
    if (showControlPoints) {
      stroke(255, 102, 0); // Orange
      for (int i=0; i<(points.length - 1); i++) {
        line(points[i][0], points[i][1], points[i+1][0], points[i+1][1]);    
      }
      // Circles on the points
      fill(255, 0, 0);
      for (int i=1; i<(points.length - 1); i++) {
        ellipse(points[i][0], points[i][1], 3, 3);
      }
      noFill();
    }
    
    stroke(0); // Black
    vertex(points[0][0], points[0][1]); // first point
    bezierVertex(points[1][0], points[1][1], 
                 points[2][0], points[2][1],
                 points[3][0], points[3][1]);
    bezierVertex(points[4][0], points[4][1], 
                 points[5][0], points[5][1],
                 points[6][0], points[6][1]);
  } else if (OPTION == 3) {
    int[][] points = {{30, 70},
                      {25, 25}, {100, 50}, {50, 100},
                      {20, 130}, {75, 140}, {120, 120}};
    
    if (showControlPoints) {
      stroke(255, 102, 0); // Orange
      for (int i=0; i<(points.length - 1); i++) {
        line(points[i][0], points[i][1], points[i+1][0], points[i+1][1]);    
      }
      // Circles on the points
      fill(255, 0, 0);
      for (int i=1; i<(points.length - 1); i++) {
        ellipse(points[i][0], points[i][1], 3, 3);
      }
      noFill();
    }
    
    stroke(0); // Black
    vertex(points[0][0], points[0][1]); // first point
    bezierVertex(points[1][0], points[1][1], 
                 points[2][0], points[2][1],
                 points[3][0], points[3][1]);
    bezierVertex(points[4][0], points[4][1], 
                 points[5][0], points[5][1],
                 points[6][0], points[6][1]);
  }
  endShape();
}
