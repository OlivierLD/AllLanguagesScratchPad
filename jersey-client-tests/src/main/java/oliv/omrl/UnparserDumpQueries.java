package oliv.omrl;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Dump the queries from the trained model.
 */
public class UnparserDumpQueries {

    private final static String TRAINED_JSON = "/Users/olivierlediouris/repos/oracle/OMRL_v0_PoC/spider_sample_data/train_spider.json";

    public static void main(String... args) throws IOException {

        URL trainedResource = new File(TRAINED_JSON).toURI().toURL();
        ObjectMapper mapper = new ObjectMapper();

        List<Object> jsonTrainedMap = mapper.readValue(trainedResource.openStream(), List.class);

        AtomicInteger rank = new AtomicInteger(0);
        jsonTrainedMap.forEach(query -> {
            System.out.printf("Query #%d: %s\n", rank.getAndIncrement(), ((Map<String, Object>) query).get("query"));
        });

        System.out.println("Done");
    }
}
