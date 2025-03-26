const cursor = document.querySelector(".cursor");
const button = document.querySelector("button");
document.addEventListener("mousemove", (e) => {
  cursor.style.left = `${e.clientX}px`;
  cursor.style.top = `${e.clientY}px`;
});

document.addEventListener("click", (e) => {
  const svg = document.getElementById("cursorSvg");
  const chunk = svg.querySelector(".chunk-pair:not(.falling)");
  if (!chunk) return;

  const newSvg = document.createElementNS("http://www.w3.org/2000/svg", "svg");
  newSvg.setAttribute("width", "50");
  newSvg.setAttribute("height", "50");
  newSvg.setAttribute("viewBox", "0 0 669 1012");
  const clone = chunk.cloneNode(true);
  newSvg.appendChild(clone);

  newSvg.style.left = `${e.clientX}px`;
  newSvg.style.top = `${e.clientY}px`;
  newSvg.style.position = "fixed";
  newSvg.style.transform = "translate(-50%, -50%)";

  document.body.appendChild(newSvg);

  chunk.remove();

  const fallDistance = window.innerHeight - e.clientY + 50;
  const xOffset = (Math.random() - 0.5) * 200;
  const rotateDegree = (Math.random() - 0.5) * 90;

  gsap.to(newSvg, {
    duration: 1.2,
    y: fallDistance,
    x: xOffset,
    rotation: rotateDegree,
    opacity: 0,
    ease: "power2.in",
    onComplete: () => newSvg.remove()
  });
});

let clickCount = 0;
const texts = [
  "Click here...",
  "CLICK HERE",
  "CLICK! HERE!",
  "CLICK!!",
  "CLICK!!!!!!!!!"
];

button.addEventListener("click", () => {
  if (clickCount < texts.length) {
    text.textContent = texts[clickCount];
    clickCount++;
  }
});
