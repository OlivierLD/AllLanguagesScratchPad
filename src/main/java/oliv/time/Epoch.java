package oliv.time;

import java.text.NumberFormat;
import java.util.Date;

public class Epoch {

    public static void main(String... args) {
        long now = System.currentTimeMillis();
        NumberFormat fmt = NumberFormat.getInstance();
        System.out.printf("Epoch in ms: %s, is s: %s\n", fmt.format(now), fmt.format(now / 1_000L));

        Date date = new Date(now);
        System.out.printf("Date : %s\n", date);

        date = new Date(1_638_827_550 * 1_000L);
        System.out.printf("Date : %s\n", date);
    }

}
