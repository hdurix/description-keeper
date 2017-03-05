package fr.hippo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.mashape.unirest.http.HttpResponse;
import com.mashape.unirest.http.JsonNode;
import com.mashape.unirest.http.ObjectMapper;
import com.mashape.unirest.http.Unirest;
import com.mashape.unirest.http.exceptions.UnirestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;

/**
 * Created by hippolyte on 3/5/17.
 */
@Service
public class KvstoreService {

    private static final String KVSTORE_URL = "https://kvstore.p.mashape.com";

    @Value("${keeper.kvstore_token}")
    private String kvstoreToken;

    @Value("${keeper.kstore_collection_name:messages}")
    private String kvstoreCollectionName;

    public KvstoreService() {
        initUniRestObjectMapper();
    }

    private void initUniRestObjectMapper() {
        // Only one time
        Unirest.setObjectMapper(new ObjectMapper() {
            private com.fasterxml.jackson.databind.ObjectMapper jacksonObjectMapper
                    = new com.fasterxml.jackson.databind.ObjectMapper();

            public <T> T readValue(String value, Class<T> valueType) {
                try {
                    return jacksonObjectMapper.readValue(value, valueType);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }

            public String writeValue(Object value) {
                try {
                    return jacksonObjectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
            }
        });
    }

    public String getMessage(Long chatId) {
        final String url = KVSTORE_URL + "/collections/" + kvstoreCollectionName + "/items/" + chatId;
        try {
            final HttpResponse<KvstoreMessage> response = Unirest.get(url)
                    .header("X-Mashape-Key", kvstoreToken)
                    .asObject(KvstoreMessage.class);
            return response.getBody().getValue();
        } catch (UnirestException e) {
            System.err.println(e.getMessage());
            return null;
        }
    }

    public String putMessage(Long chatId, String newTitle) {
        try {
            final String url = KVSTORE_URL + "/collections/" + kvstoreCollectionName + "/items/" + chatId;
            HttpResponse<JsonNode> response = Unirest.put(url)
                    .header("X-Mashape-Key", kvstoreToken)
                    .body(newTitle)
                    .asJson();
            System.out.println(response.getBody());
        } catch (UnirestException e) {
            System.err.println(e.getMessage());
            return null;
        }
        return newTitle;
    }

}
