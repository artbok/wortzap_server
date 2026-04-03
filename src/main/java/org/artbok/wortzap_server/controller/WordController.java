package org.artbok.wortzap_server.controller;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.util.*;
import java.util.stream.Collectors;

import org.artbok.wortzap_server.dto.UserWordsResponse;
import org.artbok.wortzap_server.dto.WordsForLearningResponse;
import org.artbok.wortzap_server.model.User;
import org.artbok.wortzap_server.model.Word;
import org.artbok.wortzap_server.repository.UserRepository;
import org.artbok.wortzap_server.repository.WordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;

import lombok.RequiredArgsConstructor;
import org.springframework.web.server.ResponseStatusException;


@RestController
@RequiredArgsConstructor
public class WordController {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    @Value("${app.api-key}")
    String apiKey;


    @PostMapping("/words")
    public UserWordsResponse words(@RequestBody Map<String, String> data) throws IOException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        String language = data.get("language");
        List<Word> words;
        if (Objects.equals(language, "All")) {
            words = wordRepository.findByOwnerIdOrderByIdAsc(user.id);
        } else {
            words = wordRepository.findByOwnerIdAndWordLanguageOrderByIdAsc(user.id, data.get("language"));
        }
        return new UserWordsResponse(user.getStudiedLanguages(), words);
    }

    public static List<Word> sortWordsStream(List<Word> words) {
        return words.stream()
                .sorted(Comparator.comparingInt((Word word) -> word.masteryLevel).reversed()
                        .thenComparingLong(word -> word.id))
                .collect(Collectors.toList());
    }


    @PostMapping("/set-mastery-level")
    public String setMasteryLevel(@RequestBody Map<String, String> data) {
        Long id = Long.parseLong(data.get("id"));
        Word word = wordRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found with ID: " + id));
        word.masteryLevel = Integer.parseInt(data.get("masteryLevel"));
        word.lastTimeStudied = OffsetDateTime.now();
        wordRepository.save(word);
        return "OK";
    }

    @PostMapping("/delete")
    public String delete(@RequestBody Map<String, String> data) {
        Long id = Long.parseLong(data.get("id"));
        Word word = wordRepository.findById(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Word not found with ID: " + id));
        wordRepository.delete(word);
        return "OK";
    }

    @PostMapping("/learn")
    public WordsForLearningResponse getWordsForLearning() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        List<Word> words = wordRepository.findByOwnerId(user.id);
        List<Word> sortedWords = sortWordsStream(words);
        List<Word> response = new ArrayList<>();
        Map<Integer, Integer> requirements = Map.of(
                6, 12,
                8, 120,
                10, 240,
                11, 480
        );
        for (Word word : sortedWords) {
            if (requirements.containsKey(word.masteryLevel)) {
                int t = requirements.get(word.masteryLevel);
                OffsetDateTime newTime = word.lastTimeStudied.plusHours(t);
                if (OffsetDateTime.now().isAfter(newTime)) {
                    response.add(word);
                }
            } else if (word.masteryLevel < 12) {
                response.add(word);
            }
            if (response.size() == 10) {
                break;
            }
        }
        return new WordsForLearningResponse(response);



    }
    @CrossOrigin(origins = "*")
    @PostMapping("/translate")
    public String getTranslation(@RequestBody Map<String, String> data) throws IOException, InterruptedException {

        return sendToGemini(data);
    }


    public String sendToGemini(@RequestBody Map<String, String> data) throws IOException, InterruptedException {
        System.out.println("bebra");
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;
        String text = data.get("text");
        String fromLang = data.get("fromLang");
        String toLang = data.get("toLang");
        var requestSchema = """
    {
      "contents": [{
          "parts": [
              {
                  "text": "You will analyze the input word '%1$s' from the %2$s language and provide detailed linguistic information and translations into %3$s. The input word itself ('%1$s') should be stored in the 'inputText' field. Your response MUST be a JSON object adhering to the provided schema. All specified fields are mandatory. \\\\n\\\\n1. **Input Word Translation:** \\\\n   a. Directly translate the 'inputText' (which is '%1$s') from %2$s into %3$s. Store this translation in the 'translatedInputText' field. \\\\n\\\\n2. **Base Form Analysis in %2$s (Original Language):** \\\\n   a. Determine the base or dictionary form of the 'inputText' in %2$s. For nouns, this is typically the singular nominative form. If the 'inputText' is a compound noun (like 'Neubauwohnung' in German which is a base form itself), the 'baseForm' should be the full compound word. If the 'inputText' is already in its base form, the 'baseForm' field should be identical to 'inputText'. Store this in the 'baseForm' field. \\\\n   b. If the 'baseForm' is a noun in %2$s: \\\\n      i. Assess the grammatical features of the %2$s language (the original language of 'inputText'). If %2$s uses gender articles similar to German 'der, die, das' directly with nouns, then provide the correct gender article for the 'baseForm' in %2$s. Store this in 'baseFormGenderArticle'. However, if %2$s does NOT use such gender articles (e.g., languages like Russian, Polish, or English which do not prefix nouns with 'der/die/das' style articles to indicate gender), then 'baseFormGenderArticle' MUST be an empty string. For instance, for a German noun like 'Lehrerin', this field would be 'die'; for a Russian noun like 'кофта', this field MUST be an empty string because Russian does not use such articles. \\\\n      ii. Provide its %2$s plural form. Store this in 'baseFormPlural'. \\\\n   c. If the 'baseForm' is not a noun in %2$s, or if these properties (gender article, plural form) do not apply in %2$s for the 'baseForm', use an empty string for 'baseFormGenderArticle' and/or 'baseFormPlural' respectively. \\\\n\\\\n3. **Translations of Base Form and its Variants to %3$s (Target Language):** \\\\n   a. Translate the 'baseForm' (from 2.a) from %2$s into %3$s. Store this in 'translatedBaseForm'. \\\\n   b. If a 'baseFormPlural' was identified (in 2.b.ii), translate this %2$s 'baseFormPlural' into %3$s. Store this in 'translatedBaseFormPlural'. If no 'baseFormPlural' was applicable for the 'baseForm' (e.g., it is not a noun or has no plural), 'translatedBaseFormPlural' should be an empty string. \\\\n   c. For the 'translatedBaseFormGenderArticle' field (this concerns the 'translatedBaseForm' in %3$s): \\\\n      i. If %3$s uses gender articles for nouns that directly correspond to how %2$s uses articles like 'der, die, das' (e.g., French 'le/la/les' for the 'translatedBaseForm'), provide that specific gender article from %3$s. \\\\n      ii. In all other cases – including if %3$s has grammatical gender but does not use such corresponding articles (e.g., Russian, Polish), or if the 'baseForm' is not a noun, or if an article in %3$s is not applicable or ambiguous for the 'translatedBaseForm' – use an empty string for 'translatedBaseFormGenderArticle'. \\\\n\\\\n4. **Example Sentences:** \\\\n   a. Provide exactly three distinct example sentences using the 'inputText' ('%1$s', or its appropriate grammatical form reflecting its use in %2$s). Store these in the 'examplesInOriginalLang' field. \\\\n   b. Translate EACH of these %2$s sentences accurately and individually into %3$s. Store these %3$s translations in the 'examplesInTargetLang' field. \\\\n\\\\nEnsure all fields in the schema are populated as per these instructions, using empty strings where explicitly stated for non-applicable cases."
              }
          ]
      }],
      "generationConfig": {
          "response_mime_type": "application/json",
          "response_schema": {
              "type": "OBJECT",
              "properties": {
                  "inputText": {
                      "type": "STRING",
                      "description": "The original input word/text as provided, in the original language (%2$s)."
                  },
                  "translatedInputText": {
                      "type": "STRING",
                      "description": "The direct translation of the 'inputText' from the original language (%2$s) into the target language (%3$s)."
                  },
                  "baseForm": {
                      "type": "STRING",
                      "description": "The base or dictionary form of the 'inputText' in the original language (%2$s)."
                  },
                  "baseFormGenderArticle": {
                      "type": "STRING",
                      "description": "The gender article of the 'baseForm' in the original language (%2$s), IF AND ONLY IF the original language uses German-style articles (e.g., 'der', 'die', 'das'). MUST be an empty string if the original language (e.g. Russian, English) does not use such articles."
                  },
                  "baseFormPlural": {
                      "type": "STRING",
                      "description": "The plural of the 'baseForm' in the original language (%2$s), if applicable. Empty if 'baseForm' is not a noun or has no plural form."
                  },
                  "translatedBaseForm": {
                      "type": "STRING",
                      "description": "The translation of the 'baseForm' from the original language (%2$s) into the target language (%3$s)."
                  },
                  "translatedBaseFormPlural": {
                      "type": "STRING",
                      "description": "The translation of the 'baseFormPlural' into the target language (%3$s). Empty if 'baseFormPlural' is empty or not applicable."
                  },
                  "translatedBaseFormGenderArticle": {
                      "type": "STRING",
                      "description": "For the 'translatedBaseForm' in %3$s: its gender article if %3$s uses directly corresponding articles (e.g., French 'le'). Otherwise, an empty string (e.g., for Russian which has gender but no such articles)."
                  },
                  "examplesInOriginalLang": {
                      "type": "ARRAY",
                      "items": { "type": "STRING" },
                      "description": "Three distinct example sentences using the 'inputText' (or its appropriate grammatical form) in the original language (%2$s)."
                  },
                  "examplesInTargetLang": {
                      "type": "ARRAY",
                      "items": { "type": "STRING" },
                      "description": "Translations of the 'examplesInOriginalLang' into the target language (%3$s)."
                  }
              },
              "required": [
                  "inputText",
                  "translatedInputText",
                  "baseForm",
                  "baseFormGenderArticle",
                  "baseFormPlural",
                  "translatedBaseForm",
                  "translatedBaseFormPlural",
                  "translatedBaseFormGenderArticle",
                  "examplesInOriginalLang",
                  "examplesInTargetLang"
              ]
          }
      }
  }
""";
        
        String requestBody = String.format(requestSchema, text, fromLang, toLang);
        System.out.println(requestBody);
        try (HttpClient client = HttpClient.newBuilder().build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(apiUrl))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            System.out.println(response.body());
            JSONObject responseObject = new JSONObject(response.body());
            String responseText = responseObject.getJSONArray("candidates").getJSONObject(0).getJSONObject("content").getJSONArray("parts").getJSONObject(0).getString("text");
            System.out.println(responseText);
            JSONObject wordObject = new JSONObject(responseText);
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String email = authentication.getName();
            User user = userRepository.findByEmail(email);
            Word word;
            if (Objects.equals(user.nativeLanguage, fromLang)) {
                word = new Word(
                        user.id,
                        toLang,
                        wordObject.getString("translatedBaseFormGenderArticle"),
                        wordObject.getString("translatedBaseForm"),
                        wordObject.getString("translatedBaseFormPlural"),
                        fromLang,
                        wordObject.getString("baseFormGenderArticle"),
                        wordObject.getString("baseForm"),
                        wordObject.getString("baseFormPlural")
                );
                wordRepository.save(word);
            } else if (Objects.equals(user.nativeLanguage, toLang)) {
                word = new Word(
                        user.id,
                        fromLang,
                        wordObject.getString("baseFormGenderArticle"),
                        wordObject.getString("baseForm"),
                        wordObject.getString("baseFormPlural"),
                        toLang,
                        wordObject.getString("translatedBaseFormGenderArticle"),
                        wordObject.getString("translatedBaseForm"),
                        wordObject.getString("translatedBaseFormPlural")
                );
                wordRepository.save(word);
            }
            return responseText;

        }
    }

    
}
