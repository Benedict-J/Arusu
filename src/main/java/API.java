import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class API {
    private String apiKey;

    public API(String apiName) {
        retrieveApiKey(apiName);
    }

    private void retrieveApiKey(String key) {
        try(BufferedReader bufferedReader = new BufferedReader(new FileReader("classifiedInfo.csv"))) {
            String line;

            while((line = bufferedReader.readLine()) != null) {
                String[] data = line.split(",");

                if(data[0].equals(key)) {
                    apiKey = data[1];
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getApiKey() {
        return apiKey;
    }
}
