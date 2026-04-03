package org.artbok.wortzap_server.controller;


import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

import org.artbok.wortzap_server.dto.ExercisesRepsonse;
import org.artbok.wortzap_server.model.User;
import org.artbok.wortzap_server.model.Word;
import org.artbok.wortzap_server.repository.UserRepository;
import org.artbok.wortzap_server.repository.WordRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.json.JSONObject;

import lombok.RequiredArgsConstructor;


@RestController
@RequiredArgsConstructor
public class TasksController {

    private final UserRepository userRepository;
    private final WordRepository wordRepository;

    @Value("${app.api-key}")
    String apiKey;


    @PostMapping("/generate-tasks")
    public ExercisesRepsonse words(@RequestBody Map<String, String> data) throws IOException, InterruptedException {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        User user = userRepository.findByEmail(email);
        List<Word> words = wordRepository.findByOwnerIdAndWordLanguageOrderByIdAsc(user.id, data.get("toLang"));
        List<String> masteredWords = new ArrayList<>();;
        for (int i = 0; i < words.size(); i++) {
            if (words.get(i).masteryLevel == 6) {
                masteredWords.add(words.get(i).word);
                if (masteredWords.size() == 7) {
                    break;
                }
            }
        }
        if (masteredWords.size() < 7) {
            return new ExercisesRepsonse("NOT FOUND", "[]");
        }
        String exercises = sendToGemini(String.join(", ", masteredWords), data.get("fromLang"), data.get("toLang"), data.get("languageLevel"));
        return new ExercisesRepsonse("FOUND", exercises);
    }

    public static List<Word> sortWordsStream(List<Word> words) {
        return words.stream()
                .sorted(Comparator.comparingInt((Word word) -> word.masteryLevel).reversed()
                        .thenComparingLong(word -> word.id))
                .collect(Collectors.toList());
    }



    public String sendToGemini(String words, String fromLang, String toLang, String languageLevel) throws IOException, InterruptedException {
        String apiUrl = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash-lite:generateContent?key=" + apiKey;
        var requestSchema = """
                {
                    "contents": [{
                        "parts": [
                            {
                                "text": "You are an AI language tutor. Your task is to create a set of language learning exercises based on a list of input words. The target proficiency for these exercises is %4$s. \\\\n\\\\nFor EACH word in the provided list '%1$s', you must generate ONE unique exercise object. Each exercise must consist of: \\\\n1. A single sentence in the source language (%2$s) that correctly and naturally uses the word. The vocabulary, grammar, and complexity of this sentence MUST be appropriate for a %4$s language learner. \\\\n2. An accurate and direct translation of that sentence into the target language (%3$s). \\\\n\\\\nYour response MUST be a JSON object containing a single key 'exercises'. This key must hold an array of the generated exercise objects, strictly adhering to the provided schema. Do not include any introductory text or explanations outside of the JSON structure. Don't use any HTML parsings"
                            }
                        ]
                    }],
                    "generationConfig": {
                        "response_mime_type": "application/json",
                        "response_schema": {
                            "type": "OBJECT",
                            "properties": {
                                "exercises": {
                                    "type": "ARRAY",
                                    "description": "An array of exercise objects, one for each word from the input list.",
                                    "items": {
                                        "type": "OBJECT",
                                        "properties": {
                                            "sentence": {
                                                "type": "STRING",
                                                "description": "The exercise sentence in the source language (%2$s) using one of the input words, appropriate for a %4$s level."
                                            },
                                            "translation": {
                                                "type": "STRING",
                                                "description": "The accurate translation of the 'sourceSentence' into the target language (%3$s)."
                                            }
                                        },
                                        "required": [
                                            "sentence",
                                            "translation"
                                        ]
                                    }
                                }
                            },
                            "required": [
                                "exercises"
                            ]
                        }
                    }
                }
        """;

        String requestBody = String.format(requestSchema, words, fromLang, toLang, languageLevel);
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
            return responseText;

        }
    }


}
