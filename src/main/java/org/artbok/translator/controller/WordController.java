package org.artbok.translator.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.artbok.translator.repository.WordRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;


@RestController
@RequiredArgsConstructor
public class WordController {

    public record Test(String id, String word) {}

    private final WordRepository wordRepository;

    @Value("${app.api-key}")
    String apiKey;


//    public void addToDB(String word, List<String> translations) {
//        String translationsSplitted = String.join(", ", translations);
//        var wordObj = Word.builder().owner(1L).word(word).translation(translationsSplitted).build();
//        wordRepository.save(wordObj);
//    }

    @PostMapping("/translate")
    public String getTranslation(@RequestBody Map<String, String> data) throws IOException, InterruptedException {

        String response = sendToGemini(data);

        return response;
    }



    public String sendToGemini(@RequestBody Map<String, String> data) throws IOException, InterruptedException {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=" + apiKey;

        String requestBody = """
                {"contents": [{
                      "parts": [
                        {"text": "Translate word '""" + data.get("word") + """
                       ' from German to Russian and provide 5 example sentences using this word"}
                      ]
                    }],
                    "generationConfig": {
                        "response_mime_type": "application/json",
                        "response_schema": {
                            "type": "OBJECT",
                            "properties": {
                                "translations": {\s
                                    "type": "ARRAY",
                                    "description": "List of Russian translations of the word",
                                    "items": { "type": "STRING", "description": "Russian translation of the word", "nullable": false }
                                },
                                "examples": {\s
                                    "type": "ARRAY",
                                    "description": "List of example sentences with this word in German",
                                    "items": { "type": "STRING", "description": "Example sentence with this word in German", "nullable": false }
                                },
                                "translationsOfExamples": {\s
                                    "type": "ARRAY",
                                    "description": "List of translations into Russian of the example sentences",
                                    "items": { "type": "STRING", "description": "Russian translation of the sentence", "nullable": false }
                                }
                            }
                        }
                    }}""";
//"word": { "type": "STRING", "description": "if the word is a noun, add the correct German article (der, die, das) before it. Do not add unnecessary words. If the word is conjugated, provide the base form as well."
        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> json = objectMapper.readValue(body, new TypeReference<Map<String, Object>>() {});
            //List<String> translations = (List<String>) json.get("translations");
            //addToDB(data.get("word"), translations);
            return response.body();

        }
    }
}
