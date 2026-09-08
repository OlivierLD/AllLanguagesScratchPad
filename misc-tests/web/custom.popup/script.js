
let doOnLoad = function() {

  if (document.getElementById("test1").addEventListener) {
    document.getElementById("test1").addEventListener('contextmenu', function(e) {
      // alert("You've tried to open context menu");
      e.preventDefault();

      document.getElementById("popup-menu").className = "show";
      document.getElementById("popup-menu").style.top = mouseY(event) + 'px';
      document.getElementById("popup-menu").style.left = mouseX(event) + 'px';

      // window.event.returnValue = false;

    });
  } else {

    alert("You've tried to open context menu... Not supported.");

  }

  // Hide the popup menu on a click outside the menu (could be replace by document.body)...
  document.getElementById("the-body").addEventListener("click", function(event) {
    console.log("Hiding Popup! (1)");
    document.getElementById("popup-menu").className = "hide";
  });

  document.body.onkeydown = function(evt) {
  // document.getElementById("popup-menu").onkeydown = function(evt) {
      evt = evt || window.event;
      var isEscape = false;
      if ("key" in evt) {
          isEscape = (evt.key === "Escape" || evt.key === "Esc");
      } else {
          isEscape = (evt.keyCode === 27);
      }
      if (isEscape) {
          // alert("Escape Key Was Pressed");
          console.log("Hiding Popup! (2)");
          document.getElementById("popup-menu").className = "hide";
      }
  };

};

// Get X pos of the mouse when clicked
function mouseX(evt) {
  if (evt.pageX) {
    return evt.pageX;
  } else if (evt.clientX) {
    return evt.clientX + (document.documentElement.scrollLeft ? document.documentElement.scrollLeft : document.body.scrollLeft);
  } else {
    return null;
  }
}

// Get Y pos of the mouse when clicked
function mouseY(evt) {
  if (evt.pageY) {
    return evt.pageY;
  } else if (evt.clientY) {
    return evt.clientY + (document.documentElement.scrollTop ? document.documentElement.scrollTop : document.body.scrollTop);
  } else {
    return null;
  }
}
