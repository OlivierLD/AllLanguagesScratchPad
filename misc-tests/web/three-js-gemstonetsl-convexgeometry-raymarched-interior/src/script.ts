import * as THREE from "three/webgpu";
import { DataTexture, RGFormat, NearestFilter, RepeatWrapping } from "three/webgpu";
import {
  Fn,
  Loop,
  uniform,
  float,
  vec2,
  vec3,
  vec4,
  normalView,
  cameraPosition,
  positionWorld,
  floor,
  fract,
  abs,
  min,
  max,
  sin,
  dot,
  step,
  mix,
  atan,
  time,
  texture,
  pow,
  smoothstep
} from "three/tsl";
import { OrbitControls } from "three/addons/controls/OrbitControls.js";
import { ConvexGeometry } from "three/addons/geometries/ConvexGeometry.js";

import { Pane } from "https://esm.sh/tweakpane";

console.clear();

/*
 *** White noise function
*/
const noiseSize = 512;
// Two channels (RG): R and G hold independent random values so adjacent
// z-slices can be fetched in a single texture lookup and interpolated.
const noiseData = new Uint8Array(noiseSize * noiseSize * 2);
for (let i = 0; i < noiseData.length; i++) {
  noiseData[i] = Math.floor(Math.random() * 256);
}
const noiseTex = new DataTexture(noiseData, noiseSize, noiseSize, RGFormat);
noiseTex.wrapS = RepeatWrapping;
noiseTex.wrapT = RepeatWrapping;
noiseTex.magFilter = NearestFilter;
noiseTex.minFilter = NearestFilter;
noiseTex.needsUpdate = true;
// 3-D value noise LUT — trilinear interpolation via a single texture sample.
// z-slices are offset by vec2(37, 17)*p.z; R and G hold the floor/ceil z values.
export const whiteNoiseTexture3D = Fn(([x]: [any]) => {
  const p = floor(x).toVar();
  const f = fract(x).toVar();
  f.assign(f.mul(f).mul(float(3).sub(f.mul(2))));
  const uv = p.xy.add(vec2(37.0, 17.0).mul(p.z)).add(f.xy);
  const rg = texture(noiseTex, uv.add(0.5).div(float(noiseSize))).yx.sub(0.5);
  return mix(rg.x, rg.y, f.z);
});

/*
 *** Simplex noise function - ported from McEwan / Gustavson (Ashima Arts)
*/
// Polynomial permutation hash — no texture needed
const mod289 = (x: any): any =>
  x.sub(floor(x.mul(1.0 / 289.0)).mul(289.0));
const permute = (x: any): any =>
  mod289(x.mul(34.0).add(10.0).mul(x));
const taylorInvSqrt = (r: any): any =>
  float(1.79284291400159).sub(r.mul(0.85373472095314));
// Output remapped to [0, 1]
export const simplexNoise = Fn(([v]: [any]) => {
  const C = vec2(1.0 / 6.0, 1.0 / 3.0);
  const D = vec4(0.0, 0.5, 1.0, 2.0);

  // First corner
  const i  = floor(vec3(v).add(dot(vec3(v), vec3(C.y, C.y, C.y))));
  const x0 = vec3(v).sub(i).add(dot(i, vec3(C.x, C.x, C.x)));

  // Which simplex cell — derive two corner offsets without conditionals
  const g  = step(vec3(x0.y, x0.z, x0.x), x0);
  const l  = vec3(1.0).sub(g);
  const i1 = min(g, vec3(l.z, l.x, l.y));
  const i2 = max(g, vec3(l.z, l.x, l.y));

  const x1 = x0.sub(i1).add(C.x);
  const x2 = x0.sub(i2).add(C.y);
  const x3 = x0.sub(0.5);

  // Permutations
  const im = mod289(i);
  const p  = permute(
    permute(
      permute(vec4(im.z, im.z, im.z, im.z).add(vec4(0.0, i1.z, i2.z, 1.0)))
        .add(vec4(im.y, im.y, im.y, im.y))
        .add(vec4(0.0, i1.y, i2.y, 1.0)),
    )
      .add(vec4(im.x, im.x, im.x, im.x))
      .add(vec4(0.0, i1.x, i2.x, 1.0)),
  );

  // Gradients: 7x7 over a square mapped onto an octahedron
  const n_  = float(1.0 / 7.0);
  const ns  = vec3(n_.mul(D.w), n_.mul(D.y).sub(1.0), n_.mul(D.z));

  const j   = p.sub(float(49.0).mul(floor(p.mul(ns.z).mul(ns.z))));
  const x_  = floor(j.mul(ns.z));
  const y_  = floor(j.sub(float(7.0).mul(x_)));
  const gx  = x_.mul(ns.x).add(ns.y);
  const gy  = y_.mul(ns.x).add(ns.y);
  const h   = float(1.0).sub(abs(gx)).sub(abs(gy));

  const b0  = vec4(gx.x, gx.y, gy.x, gy.y);
  const b1  = vec4(gx.z, gx.w, gy.z, gy.w);
  const s0  = floor(b0).mul(2.0).add(1.0);
  const s1  = floor(b1).mul(2.0).add(1.0);
  const sh  = step(h, vec4(0.0)).negate();

  const a0  = vec4(b0.x, b0.z, b0.y, b0.w).add(
    vec4(s0.x, s0.z, s0.y, s0.w).mul(vec4(sh.x, sh.x, sh.y, sh.y)),
  );
  const a1  = vec4(b1.x, b1.z, b1.y, b1.w).add(
    vec4(s1.x, s1.z, s1.y, s1.w).mul(vec4(sh.z, sh.z, sh.w, sh.w)),
  );

  const p0 = vec3(a0.x, a0.y, h.x);
  const p1 = vec3(a0.z, a0.w, h.y);
  const p2 = vec3(a1.x, a1.y, h.z);
  const p3 = vec3(a1.z, a1.w, h.w);

  // Normalise gradients
  const norm = taylorInvSqrt(
    vec4(dot(p0, p0), dot(p1, p1), dot(p2, p2), dot(p3, p3)),
  );
  const p0n = p0.mul(norm.x);
  const p1n = p1.mul(norm.y);
  const p2n = p2.mul(norm.z);
  const p3n = p3.mul(norm.w);

  // Mix contributions from four corners
  const m  = max(
    float(0.6).sub(vec4(dot(x0, x0), dot(x1, x1), dot(x2, x2), dot(x3, x3))),
    0.0,
  );
  const m2 = m.mul(m);

  return float(42.0)
    .mul(dot(m2.mul(m2), vec4(dot(p0n, x0), dot(p1n, x1), dot(p2n, x2), dot(p3n, x3))))
    .mul(0.5)
    .add(0.5);
});


/*
 *** FBM noise function
*/
interface FBMOptions {
  octaves?: number;
  lacunarity?: number;
  gain?: number;
}
// Returns a new Fn node that applies fractional Brownian motion to any noise function.
// TSL Loop emits a single native for-loop in the shader — only one copy of noiseFn
// in the node graph regardless of octave count, keeping compile time fast.
export const makeFBM = (
  noiseFn: (p: any) => any,
  { octaves = 6, lacunarity = 2.0, gain = 0.5 }: FBMOptions = {},
) =>
  Fn(([p]: [any]) => {
    const value = float(0).toVar();
    const amplitude = float(0.5).toVar();
    const pos = vec3(p).toVar();

    Loop(octaves, () => {
      value.addAssign(noiseFn(pos).mul(amplitude));
      pos.mulAssign(lacunarity);
      amplitude.mulAssign(gain);
    });

    return value;
  });












const simplexFBM = makeFBM(simplexNoise, {
  octaves: 3,
  lacunarity: 1.9,
  gain: 0.7,
});
const patternFBM = Fn(([v]: [any]) => {
  const a = vec3(simplexFBM(v), simplexFBM(v.add(10)), v.z);
  const b = vec3(
    simplexFBM(v.add(a.mul(2).add(1))),
    simplexFBM(v.add(a.mul(2).sub(20))),
    v.z,
  );
  return simplexFBM(v.add(b.mul(2).add(1)));
});

const scene = new THREE.Scene();

const camera = new THREE.PerspectiveCamera(
  75,
  window.innerWidth / window.innerHeight,
  0.1,
  10,
);
camera.position.z = 2;

const renderer = new THREE.WebGPURenderer({
  canvas: document.querySelector("#webgpu-canvas")!,
  antialias: true,
});
renderer.setSize(window.innerWidth, window.innerHeight);
renderer.setAnimationLoop(animate);

window.addEventListener("resize", function () {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
});

const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;

// PRNG
function mulberry32(seed: number) {
  return function () {
    seed |= 0;
    seed = (seed + 0x6d2b79f5) | 0;
    let t = Math.imul(seed ^ (seed >>> 15), 1 | seed);
    t = (t + Math.imul(t ^ (t >>> 7), 61 | t)) ^ t;
    return ((t ^ (t >>> 14)) >>> 0) / 4294967296;
  };
}

function generateGemPoints(
  count: number,
  seed: number,
  topPole: number,
  bottomPole: number,
  equatorBias: number,
  equatorRadius: number,
  radialVariance: number,
  yJitter: number,
): THREE.Vector3[] {
  const rng = mulberry32(seed);
  const points: THREE.Vector3[] = [];

  points.push(new THREE.Vector3(0, topPole, 0));
  points.push(new THREE.Vector3(0, bottomPole, 0));

  for (let i = 0; i < count - 2; i++) {
    const theta = rng() * Math.PI * 2;
    const raw = rng() * 2 - 1;
    const y =
      Math.sign(raw) * Math.pow(Math.abs(raw), 1 / equatorBias) * 0.85 +
      (rng() - 0.5) * yJitter;
    const r =
      equatorRadius *
      (1 - Math.abs(y)) *
      (1 - radialVariance + rng() * radialVariance);

    points.push(new THREE.Vector3(Math.cos(theta) * r, y, Math.sin(theta) * r));
  }

  return points;
}

const options = {
  seed: 3695,
  points: 128,
  topPole: 0.83,
  bottomPole: -1.3,
  equatorBias: 1.15,
  equatorRadius: 0.7,
  radialVariance: 0,
  yJitter: 1,
  // Exterior
  hueSpeed: 0.1,
  fresnelPower: 2.0,
  specPower: 256.0,
  noiseScale: 4,
  noiseStrength: 0.08,
  // Interior
  interiorSteps: 32,
  interiorStepSize: 0.025,
  interiorNoiseScale: 0.6,
  interiorBrightness: 5,
  interiorOffset: 0.65,
};

// Exterior uniforms
const noiseScaleUniform = uniform(options.noiseScale);
const noiseStrengthUniform = uniform(options.noiseStrength);
const hueSpeedUniform = uniform(options.hueSpeed);
const fresnelPowerUniform = uniform(options.fresnelPower);
const specPowerUniform = uniform(options.specPower);

// Interior uniforms
const interiorStepsUniform = uniform(options.interiorSteps);
const interiorStepSizeUniform = uniform(options.interiorStepSize);
const interiorNoiseScaleUniform = uniform(options.interiorNoiseScale);
const interiorBrightnessUniform = uniform(options.interiorBrightness);
const interiorOffsetUniform = uniform(options.interiorOffset);

// Exterior material (front faces, matcap)
const exteriorMat = new THREE.MeshBasicMaterial({ side: THREE.FrontSide });

exteriorMat.normalNode = Fn(() => {
  const n = normalView.toVar();
  const noise = simplexFBM(positionWorld.mul(noiseScaleUniform));
  n.addAssign(
    vec3(noise).mul(noiseStrengthUniform).sub(noiseStrengthUniform.mul(0.5)),
  );
  return vec4(n, 1);
})();

exteriorMat.colorNode = Fn(() => {
  const n = normalView.normalize().toVar();
  const nt = time.mul(hueSpeedUniform);
  const noise = simplexFBM(positionWorld.mul(noiseScaleUniform).add(nt));

  const angle = atan(n.y, n.x);
  const hue = fract(
    angle
      .div(Math.PI * 2)
      .add(noise.mul(noiseStrengthUniform))
      .add(0.5)
      .add(nt),
  );

  const tau = Math.PI * 2;
  const rainbow = vec3(
    sin(hue.mul(tau)).mul(0.5).add(0.5),
    sin(hue.mul(tau).add(2.094)).mul(0.5).add(0.5),
    sin(hue.mul(tau).add(4.189)).mul(0.5).add(0.5),
  );

  const nz = n.z.clamp(0, 1);
  const fresnel = nz.oneMinus().pow(fresnelPowerUniform);
  const spec = nz.pow(specPowerUniform);

  return vec4(rainbow.mul(fresnel).add(spec), 1);
})();

// Interior material (back faces, additive raymarch)
const interiorMat = new THREE.MeshBasicMaterial({
  side: THREE.BackSide,
  transparent: true,
  blending: THREE.AdditiveBlending,
  depthWrite: false,
  depthTest: false,
});

interiorMat.colorNode = Fn(() => {
  // Ray from back-face surface toward camera (through the interior)
  const ro = positionWorld.toVar();
  const rd = cameraPosition.sub(positionWorld).normalize().toVar();

  const accumulated = vec3(0).toVar();
  const t = float(0).toVar();

  Loop({ start: 0, end: interiorStepsUniform }, () => {
    const pos = ro.add(rd.mul(t)).toVar();
    const np = pos.mul(1024.);
    // ro.addAssign(vec3(
    //   whiteNoiseTexture3D(np),
    //   whiteNoiseTexture3D(np.add(1)),
    //   whiteNoiseTexture3D(np.add(2))
    // ).mul(.01));
    const nt = time.mul(hueSpeedUniform);

    // Sample FBM noise at this interior position
    // const n = simplexFBM(pos.mul(interiorNoiseScaleUniform)).pow(2).toVar();
    const n = patternFBM(pos.add(interiorOffsetUniform).mul(interiorNoiseScaleUniform))
      .pow(8)
      .mul(4)
      .toVar();

    // Rainbow hue from world-space position angle (different axis for variety)
    const hue = fract(
      atan(pos.z, n)
        .div(Math.PI * 2)
        .add(pos.y.mul(0.3))
        .add(0.5)
        .add(nt),
    );
    const tau = Math.PI * 2;
    const col = vec3(
      sin(hue.mul(tau)).mul(0.5).add(0.5),
      sin(hue.mul(tau).add(2.094)).mul(0.5).add(0.5),
      sin(hue.mul(tau).add(4.189)).mul(0.5).add(0.5),
    );

    // Accumulate: noise acts as density
    // const ss = smoothstep(.9, 0.5, n).add(.1);
    accumulated.addAssign(
      col
        .mul(pow(n.mul(interiorStepSizeUniform).mul(interiorBrightnessUniform), 1.5))
        .mul(float(2).sub(t)
        
            ),
    );
    t.addAssign(interiorStepSizeUniform);
  });

  return vec4(accumulated, 1);
})();

// Geometry & meshes
let exteriorMesh: THREE.Mesh | null = null;
let interiorMesh: THREE.Mesh | null = null;

function buildGem() {
  const pts = generateGemPoints(
    options.points,
    options.seed,
    options.topPole,
    options.bottomPole,
    options.equatorBias,
    options.equatorRadius,
    options.radialVariance,
    options.yJitter,
  );
  const geometry = new ConvexGeometry(pts);

  if (exteriorMesh) {
    exteriorMesh.geometry.dispose();
    exteriorMesh.geometry = geometry;
    interiorMesh!.geometry = geometry;
  } else {
    exteriorMesh = new THREE.Mesh(geometry, exteriorMat);
    interiorMesh = new THREE.Mesh(geometry, interiorMat);
    // Interior renders after exterior so additive glow composites on top
    exteriorMesh.renderOrder = 0;
    interiorMesh.renderOrder = 1;
    scene.add(interiorMesh, exteriorMesh);
  }
}

buildGem();

// Tweakpane
const pane = new Pane();

const shapeFolder = pane.addFolder({ title: "Shape" });
shapeFolder
  .addBinding(options, "points", { label: "Points", min: 6, max: 128, step: 1 })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "seed", { label: "Seed", min: 0, max: 9999, step: 1 })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "topPole", {
    label: "Top Pole",
    min: 0,
    max: 2,
    step: 0.01,
  })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "bottomPole", {
    label: "Bottom Pole",
    min: -2,
    max: 0,
    step: 0.01,
  })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "equatorBias", {
    label: "Equator Bias",
    min: 0.1,
    max: 5,
    step: 0.05,
  })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "equatorRadius", {
    label: "Equator Radius",
    min: 0.1,
    max: 2,
    step: 0.01,
  })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "radialVariance", {
    label: "Radial Variance",
    min: 0,
    max: 1,
    step: 0.01,
  })
  .on("change", () => buildGem());
shapeFolder
  .addBinding(options, "yJitter", {
    label: "Y Jitter",
    min: 0,
    max: 1,
    step: 0.01,
  })
  .on("change", () => buildGem());
shapeFolder.expanded = false;

const exteriorFolder = pane.addFolder({ title: "Exterior" });
exteriorFolder
  .addBinding(options, "hueSpeed", {
    label: "Hue Speed",
    min: -1,
    max: 1,
    step: 0.01,
  })
  .on("change", (v: any) => {
    hueSpeedUniform.value = v.value;
  });
exteriorFolder
  .addBinding(options, "fresnelPower", {
    label: "Fresnel Power",
    min: 0.5,
    max: 8,
    step: 0.1,
  })
  .on("change", (v: any) => {
    fresnelPowerUniform.value = v.value;
  });
exteriorFolder
  .addBinding(options, "specPower", {
    label: "Spec Power",
    min: 1,
    max: 256,
    step: 1,
  })
  .on("change", (v: any) => {
    specPowerUniform.value = v.value;
  });
exteriorFolder
  .addBinding(options, "noiseScale", {
    label: "Noise Scale",
    min: 0.1,
    max: 20,
    step: 0.1,
  })
  .on("change", (v: any) => {
    noiseScaleUniform.value = v.value;
  });
exteriorFolder
  .addBinding(options, "noiseStrength", {
    label: "Noise Strength",
    min: 0,
    max: 0.5,
    step: 0.01,
  })
  .on("change", (v: any) => {
    noiseStrengthUniform.value = v.value;
  });
exteriorFolder.expanded = false;

const interiorFolder = pane.addFolder({ title: "Interior" });
interiorFolder
  .addBinding(options, "interiorSteps", {
    label: "Steps",
    min: 4,
    max: 128,
    step: 1,
  })
  .on("change", (v: any) => {
    interiorStepsUniform.value = v.value;
  });
interiorFolder
  .addBinding(options, "interiorStepSize", {
    label: "Step Size",
    min: 0.01,
    max: 0.2,
    step: 0.005,
  })
  .on("change", (v: any) => {
    interiorStepSizeUniform.value = v.value;
  });
interiorFolder
  .addBinding(options, "interiorNoiseScale", {
    label: "Noise Scale",
    min: 0.1,
    max: 3,
    step: 0.01,
  })
  .on("change", (v: any) => {
    interiorNoiseScaleUniform.value = v.value;
  });
interiorFolder
  .addBinding(options, "interiorBrightness", {
    label: "Brightness",
    min: 0,
    max: 5,
    step: 0.05,
  })
  .on("change", (v: any) => {
    interiorBrightnessUniform.value = v.value;
  });
interiorFolder
  .addBinding(options, "interiorOffset", {
    label: "Offset",
    min: -2,
    max: 2,
    step: 0.05,
  })
  .on("change", (v: any) => {
    interiorOffsetUniform.value = v.value;
  });
interiorFolder.expanded=false;


function animate() {
  controls.update();
  if (exteriorMesh) {
    exteriorMesh.rotation.y += 0.001;
    interiorMesh!.rotation.y = exteriorMesh.rotation.y;
  }
  renderer.render(scene, camera);
}
