package oliv.lambda;

import java.util.function.BiFunction;
import java.util.function.Function;

/**
 * Several lambda samples, with many generics
 */
public class Lambda101 {

	private static <T, R> R genericStuff(T id, Function<T, R> fn) {
		return fn.apply(id);
	}

	private static <T, U, R> R genericBiStuff(T id, U stuff, BiFunction<T, U, R> fn) {
		return fn.apply(id, stuff);
	}

	private static String echo(String s) {
		return s;
	}

	private static Double echoDouble(Double d) { return d; }

	private static Integer count(String s) {
		return s.length();
	}

	private static String reverse(String s) {
		StringBuffer sb = new StringBuffer();
		for (int i = s.length(); i > 0; i--) {
			sb.append(s.charAt(i - 1));
		}
		return sb.toString();
	}

	private static String betterReverse(String s) {
		return new StringBuilder(s).reverse().toString();
	}

	public static void main(String... args) {
		String str = genericStuff("Akeu", Lambda101::echo);
		System.out.println(str);

		Double pi = genericStuff(Math.PI, Lambda101::echoDouble);
		System.out.printf("\u03c0: %.15f%n", pi);

		int len = genericStuff("Akeu-coucou", Lambda101::count);
		System.out.println("String has " + len + " character(s).");

		str = genericStuff("Coucou", a -> {
			return a;
		});
		System.out.println(str);

		str = genericStuff("Tagada", a -> {
			return reverse(a);
		});
		System.out.println(str);

		str = genericStuff("Rha lovely!", a -> reverse(a));
		System.out.println(str);

		str = genericStuff("Pouet-pouet", Lambda101::reverse);
		System.out.println(str);

		str = genericStuff("This is so much better!", Lambda101::betterReverse);
		System.out.println(str);

		str = genericBiStuff(3, "Merde!", (a, b) -> {
			StringBuffer sb = new StringBuffer();
			for (int i = 0; i < a; i++) {
				sb.append(b + " ");
			}
			return sb.toString().trim();
		});
		System.out.println(str);
	}
}
