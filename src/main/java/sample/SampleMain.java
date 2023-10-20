package sample;

import java.util.Arrays;
import java.util.function.BiConsumer;

public class SampleMain {
    private final static String HELP_PRM = "--help";
    private final static String FIRST_PRM = "--first-prm:";
    private final static String SECOND_PRM = "--second-prm:";
    private final static String THIRD_PRM = "--third-prm:";

    public static class CLIPrm {
        private final String prefix;
        private final String desc;
        private final BiConsumer<String, CLIPrm> action;
        public CLIPrm(String prefix, String desc, BiConsumer<String, CLIPrm> action) {
            this.prefix = prefix;
            this.desc = desc;
            this.action = action;
        }

        public String getPrefix() {
            return prefix;
        }

        public String getDesc() {
            return desc;
        }

        public BiConsumer<String, CLIPrm> getAction() {
            return action;
        }
    }

    private static String valueOne;
    private static int valueTwo = 0;
    private static boolean valueThree = false;

    private final static CLIPrm[] CLI_PRMS = {
            new CLIPrm(HELP_PRM, "Help!!!", (str, prm) -> {
                System.out.println("CLI Parameters:");
                displayHelp();
                System.exit(0);
            }),
            new CLIPrm(FIRST_PRM, "String example.", (str, prm) -> {
                valueOne = str.substring(prm.getPrefix().length());
                System.out.printf("Managed %s, %s, %s\n", prm.getPrefix(), prm.getDesc(), str);
            }),
            new CLIPrm(SECOND_PRM, "Int example.", (str, prm) -> {
                try {
                    valueTwo = Integer.parseInt(str.substring(prm.getPrefix().length()));
                } catch (NumberFormatException nfe) {
                    nfe.printStackTrace();
                }
                System.out.printf("Managed %s, %s, %s\n", prm.getPrefix(), prm.getDesc(), str);
            }),
            new CLIPrm(THIRD_PRM, "Boolean example.", (str, prm) -> {
                valueThree = "true".equals(str.substring(prm.getPrefix().length()));
                System.out.printf("Managed %s, %s, %s\n", prm.getPrefix(), prm.getDesc(), str);
            }),
    };

    private static void displayHelp() {
        Arrays.asList(CLI_PRMS).forEach(prm -> System.out.printf("%s, %s\n", prm.getPrefix(), prm.getDesc()));
    }

    public static void main(String... args) {
        for (String arg : args) {
            final CLIPrm cliPrm = Arrays.asList(CLI_PRMS).stream()
                    .filter(prm -> arg.startsWith(prm.getPrefix()))
                    .findFirst()
                    .orElse(null);
            if (cliPrm != null) {
                cliPrm.getAction().accept(arg, cliPrm);
            } else {
                System.out.printf("CLI Prm [%s] not managed.\n", arg);
            }
        }
        // Now, move on !
        System.out.printf("Value One: %s\n", valueOne);
        System.out.printf("Value Two: %d\n", valueTwo);
        System.out.printf("Value Three: %s\n", valueThree);

        // . . .
    }

}
