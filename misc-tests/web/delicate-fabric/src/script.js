

////////////////////////////////////////////////
////////////     DELICATE FABRIC     ///////////
////////////////////////////////////////////////



///---INITIATION---///

//canvas variables
var container = document.getElementById("container_div");
var canvas = document.getElementById("canvas");
var ctx = canvas.getContext("2d");

//fabric components
var pointCount = 0;
var points = [];
var spans = [];

//settings
var displayPoints = false;
var displaySpans = true;
var fw = 66;  // fabric width (as percentage of canvas width)
var fh = 55;  // fabric height (as percentage of canvas height)
var htc = fw;  // fabric horizontal thread count
var vtc = fh;  // fabric vertical thread count
var rigidity = 15;  // (iterations of position-accuracy refinement)
var gravity = 0.5;  // (rate of y-velocity increase per frame)
var friction = 0.999;  // (proportion of previous velocity after frame refresh)
var wallBounceLoss = 0.9;  // (proportion of previous velocity after bouncing)
var skidLoss = 0.8;  // (proportion of previous velocity if touching the ground)

//scaling
scaleToWindow();
var canvasPositionLeft = canvas.getBoundingClientRect().left + window.scrollX;
var canvasPositionTop = canvas.getBoundingClientRect().top + window.scrollY;

//interaction
var mouseCanvasX;
var mouseCanvasY;
var grabRadius = canvas.width/25;
var fabricStrength = 25;  // (number of times a span's length can stretch before breaking)



///---OBJECTS---///

//point constructor
function Point(current_x, current_y) {
  this.cx = current_x;
  this.cy = current_y; 
  this.px = this.cx;  // previous x value
  this.py = this.cy;  // previous y value
  this.pinned = false;
  this.grabbed = false;
  this.mxd = null;  // mouse x distance (upon grab)
  this.myd = null;  // mouse y distance (upon grab)
  this.id = pointCount;
  pointCount += 1;
}

//creates points
for (i=0; i<vtc; i++) {
  //(assigns y values so top margin matches l/r margins)
  var y = (i*fh/(vtc-1))+(100-fw)/2; 
  for (j=0; j<htc; j++) {
    //(assigns x values so fabric is centered horizontally)
    var x = (j*fw/(htc-1))+(100-fw)/2;
    addPt(x,y);
  }
}
//(pin top row)
for (i=0; i<htc; i++) { points[i].pinned = true; }

//span constructor
function Span(point_1, point_2, visibility="visible") {
  this.p1 = point_1;
  this.p2 = point_2;
  this.l = distanceBetween(this.p1,this.p2); // length
  this.visibility = visibility;
}

//create spans
for (i=0; i<points.length-1; i++) {
  if ( (i+1)%htc !== 0 ) { addSp(i,i+1); }  // horizontal spans
  if ( i < points.length-htc) { addSp(i,i+htc); }  // vertical spans
}

//initial unfurl
var rx = randNumBetween(-50,50);
for (i=0; i<points.length/4; i++) {
  var rp = randNumBetween(Math.floor(points.length/3),points.length-1);
  points[rp].px += rx;
  points[rp].py += randNumBetween(10,30);
}
for (i=vtc*htc-vtc-1; i<vtc*htc-1; i++) {
  points[i].px += rx;
  points[i].py += randNumBetween(100,300);
}

//updates frame
update();



///---FUNCTIONS---///

//scaling
function scaleToWindow() {
  if (window.innerWidth > window.innerHeight) {
    container.style.height = window.innerHeight*0.8+"px";
    container.style.width = container.style.height;
  } else {
    container.style.width = window.innerWidth*0.8+"px";
    container.style.height = container.style.width;
  }
  canvas.width = document.getElementById("canvas_div").clientWidth;
  canvas.height = document.getElementById("canvas_div").clientHeight;
}

//converts percentage to canvas x value
function xPct(pct) {
  return pct * canvas.width / 100;
}

//converts percentage to canvas y value
function yPct(pct) {
  return pct * canvas.height / 100;
}

//gets a point by id number
function getPt(id) {
  for (var i=0; i<points.length; i++) { 
    if (points[i].id == id) { return points[i]; }
  }
}

//gets distance between two points (pythogorian theorum)
function distanceBetween(point_1, point_2) {
  var x_difference = point_2.cx - point_1.cx;
  var	y_difference = point_2.cy - point_1.cy;
  return Math.sqrt( x_difference*x_difference + y_difference*y_difference);
}

//generates random number between a minimum and maximum value
function randNumBetween(min,max) {
  return Math.floor(Math.random()*(max-min+1))+min;
}

//creates a point object instance
function addPt(xp,yp) {
  points.push( new Point( xPct(xp), yPct(yp) ) );
}

//creates a span object instance
function addSp(p1,p2,visibility="visible") {
  spans.push( new Span( getPt(p1), getPt(p2), visibility ) );
}

//updates coordinates and renders new frames continuously
function update() {
  updatePoints();
  //(refines point positions for position accuracy & shape rigidity)
  for (var i=0; i<rigidity; i++) {
    wallBounce();
    updateSpans();
  }
  clearCanvas();  // clears previous frame
  if (displayPoints) { renderPoints(); }
  if (displaySpans) { renderSpans(); }
  window.requestAnimationFrame(update);
}

//updates points based on verlet velocity (i.e., current coord minus previous coord)
function updatePoints() {
  for(var i=0; i<points.length; i++) {
    var p = points[i];  // point object
    if (!p.pinned) {
      var	xv = (p.cx - p.px) * friction;	// x velocity
      var	yv = (p.cy - p.py) * friction;	// y velocity
      if (p.py >= canvas.height-1 && p.py <= canvas.height) { xv *= skidLoss; }
      p.px = p.cx;  // updates previous x as current x
      p.py = p.cy;  // updates previous y as current y
      p.cx += xv;  // updates current x with new velocity
      p.cy += yv;  // updates current y with new velocity
      p.cy += gravity;  // add gravity to y
    }
  }	
}

// inverts velocity for bounce if a point reaches a wall
function wallBounce() {
  for (var i=0; i<points.length; i++) {
    var p = points[i];
    if (p.cx > canvas.width) {
      p.cx = canvas.width;
      p.px = p.cx + (p.cx - p.px) * wallBounceLoss;
    }
    if (p.cx < 0) {
      p.cx = 0;
      p.px = p.cx + (p.cx - p.px) * wallBounceLoss;
    }
    if (p.cy > canvas.height) {
      p.cy = canvas.height;
      p.py = p.cy + (p.cy - p.py) * wallBounceLoss;
    }
    if (p.cy < 0) {
      p.cy = 0;
      p.py = p.cy + (p.cy - p.py) * wallBounceLoss;
    }
  }
}

// sets spans between points
function updateSpans() {
  for (var i=0; i<spans.length; i++) {
    var s = spans[i];
    var dx = s.p2.cx - s.p1.cx;  // distance between x values
    var	dy = s.p2.cy - s.p1.cy;  // distance between y values
    var d = Math.sqrt( dx*dx + dy*dy);  // distance between the points
    if (d > s.l*fabricStrength) { spans.splice(i,1); }  // tear if over-stretched
    var	r = s.l / d;	// ratio (span length over distance between points)
    var	mx = s.p1.cx + dx / 2;  // midpoint between x values 
    var my = s.p1.cy + dy / 2;  // midpoint between y values
    var ox = dx / 2 * r;  // offset of each x value (compared to span length)
    var oy = dy / 2 * r;  // offset of each y value (compared to span length)
    if (s.p1.pinned === false) {
      s.p1.cx = mx - ox;  // updates span's first point x value
      s.p1.cy = my - oy;  // updates span's first point y value
    }
    if (s.p2.pinned === false) {
      s.p2.cx = mx + ox;  // updates span's second point x value
      s.p2.cy = my + oy;  // updates span's second point y value
    }
  }
}

// clears canvas frame
function clearCanvas() {
  ctx.clearRect(0, 0, canvas.width, canvas.height);
}

// displays points
function renderPoints() {
  for (var i=0; i<points.length; i++) {
    var p = points[i];
    ctx.beginPath();
    ctx.arc(p.cx,p.cy,3,0,2*Math.PI);
    ctx.fillStyle="green";
    ctx.fill();
  }
}

// displays spans
function renderSpans() {
  ctx.beginPath();
  for (var i=0; i<spans.length; i++) {
    var s = spans[i];
    if (s.visibility == "visible") {
      ctx.strokeStyle = "#552500";
      ctx.lineWidth = ".66";
      ctx.moveTo(s.p1.cx, s.p1.cy);
      ctx.lineTo(s.p2.cx, s.p2.cy);
    }
  }
  ctx.stroke(); 
}

//grabs fabric on click
function grabFabric(e) {
  mouseCanvasX = e.pageX - canvasPositionLeft;  //mouse canvas x
  mouseCanvasY = e.pageY - canvasPositionTop;  //mouse canvas y
  for (var i=0; i<points.length; i++) {
    var x_diff = points[i].cx - mouseCanvasX;
    var y_diff = points[i].cy - mouseCanvasY;
    var dist = Math.sqrt( x_diff*x_diff + y_diff*y_diff);
    if (dist <= grabRadius) {
      points[i].grabbed = true;
      points[i].mxd = x_diff;
      points[i].myd = y_diff;
    }
  }
}

//moves fabric
function moveFabric(e) {
  mouseCanvasX = e.pageX - canvasPositionLeft;  //mouse canvas x
  mouseCanvasY = e.pageY - canvasPositionTop;  //mouse canvas y
  //(drops fabric if mouse leaves canvas)
  if (  mouseCanvasX < 0 || mouseCanvasX > canvas.width || 
        mouseCanvasY < 0 || mouseCanvasY > canvas.height) { 
    dropFabric(); 
  }
  //updates grabbed points according to mouse position
  for (var i=0; i<points.length; i++) {
    if (points[i].grabbed === true && points[i].pinned === false) {
      points[i].cx = points[i].px = mouseCanvasX + points[i].mxd;
      points[i].cy = points[i].py = mouseCanvasY + points[i].myd;
    }
  }
}

//drops fabric
function dropFabric() {
  for (var i=0; i<points.length; i++) {
    points[i].grabbed = false;
  }
}



///---EVENTS---///

//scaling
window.addEventListener('resize', scaleToWindow);

//interaction
document.addEventListener("mousedown", grabFabric);
document.addEventListener("mousemove", moveFabric);
document.addEventListener("mouseup", dropFabric);

document.addEventListener("touchstart", grabFabric);
document.addEventListener("touchmove", moveFabric);
document.addEventListener("touchend", dropFabric);