class Polygon {
	constructor(height, width) {
		this.height = height;
		this.width = width;
	};

	static blah() {
		console.log("(static) Interesting...");
	};

	test() {
		console.log("Height is " , this.height);
		console.log("Width is " , this.width);
	};
};

let polygon = new Polygon(10, 20);
polygon.test();

Polygon.blah();

console.log("Is polygon a Polygon? ", (polygon instanceof Polygon));


