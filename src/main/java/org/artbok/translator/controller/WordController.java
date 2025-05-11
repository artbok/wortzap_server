package org.artbok.translator.controller;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class WordController {

    public record Test(String id, String word) {}

    //private final WordRepository wordRepository;

    @Value("${app.api-key}")
    String apiKey;


//    public void addToDB(String word, List<String> translations) {
//        String translationsSplitted = String.join(", ", translations);
//        var wordObj = Word.builder().owner(1L).word(word).translation(translationsSplitted).build();
//        wordRepository.save(wordObj);
//    }

    @PostMapping("/translate")
    public String getTranslation(@RequestBody Map<String, String> data) throws IOException, InterruptedException {

        return sendToGemini(data);
    }


    public String sendToGemini(@RequestBody Map<String, String> data) throws IOException, InterruptedException {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash-lite:generateContent?key=" + apiKey;
        String text = data.get("text");
        String fromLang = data.get("fromLang");
        String toLang = data.get("toLang");
        String schema = """
        {"contents": [{
              "parts": [
                {"text": "Translate the word '%1$s' from %2$s to %3$s. Your response MUST be a JSON object adhering to the provided schema. Populate the 'translations' field with the translation of the word. Populate the 'examples' field with exactly three example sentences using the word '%1$s' in %2$s. Populate the 'exampleTranslations' field with the corresponding translations of these sentences into %3$s. All fields ('translations', 'examples', 'exampleTranslations') are mandatory and must be included in your response."}
              ]
            }],
            "generationConfig": {
                "response_mime_type": "application/json",
                "response_schema": {
                     "type": "OBJECT",
                     "properties": {
                         "translations": {
                             "type": "ARRAY",
                             "items": { "type": "STRING" },
                             "description": "The translation(s) of the input word."
                         },
                         "examples": {
                             "type": "ARRAY",
                             "items": { "type": "STRING" },
                             "description": "Exactly three example sentences using the original word in its original language (%2$s)."
                         },
                         "exampleTranslations": {
                             "type": "ARRAY",
                             "items": { "type": "STRING" },
                             "description": "The translations of the three example sentences into the target language (%3$s), corresponding to the 'examples' array."
                         }
                     },
                     "required": ["translations", "examples", "exampleTranslations"]
                 }
            }
            }""";
        String requestBody = String.format(schema, text, fromLang, toLang);
        System.out.println(requestBody);
        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            return response.body();

        }
    }
}
