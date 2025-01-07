package org.example;

import org.json.JSONObject;
import org.json.JSONArray;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class WikipediaBot extends TelegramLongPollingBot {

    private static final String WIKIPEDIA_API_URL_EN = "https://en.wikipedia.org/w/api.php?";
    private static final String WIKIPEDIA_API_URL_UZ = "https://uz.wikipedia.org/w/api.php?";

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String userMessage = update.getMessage().getText();
            String chatId = update.getMessage().getChatId().toString();

            String responseMessage;

            if (userMessage.equalsIgnoreCase("/start")) {
                responseMessage = "Wikipedia botiga xush kelibsiz! Qidiruv uchun mavzu yuboring.";
            } else {
                try {
                    responseMessage = searchWikipedia(userMessage);
                } catch (Exception e) {
                    responseMessage = "Uzr, ma'lumotni olishning imkoni bo'lmadi. Keyinroq urinib ko'ring.";
                    e.printStackTrace();
                }
            }

            sendMessage(chatId, responseMessage);
        }
    }

    @Override
    public String getBotUsername() {
        return "@Wikipedia_fs_uz_bot";
    }

    @Override
    public String getBotToken() {
        return "7590946013:AAFKTUHeGV7REwarIsLzCVs6UxnnUhSMfpE";
    }

    private void sendMessage(String chatId, String text) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(text);

        try {
            execute(message);
        } catch (TelegramApiException e) {
            e.printStackTrace();
        }
    }

    private String searchWikipedia(String query) throws Exception {
        String apiUrl;


        if (isUzbek(query)) {
            apiUrl = WIKIPEDIA_API_URL_UZ;
        } else {
            apiUrl = WIKIPEDIA_API_URL_EN;
        }

        String urlString = apiUrl + "action=query&list=search&srsearch=" + query.replace(" ", "%20") + "&format=json";
        URL url = new URL(urlString);

        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        JSONObject jsonResponse = new JSONObject(response.toString());
        JSONArray searchResults = jsonResponse.getJSONObject("query").getJSONArray("search");

        if (searchResults.length() > 0) {
            JSONObject firstResult = searchResults.getJSONObject(0);
            String title = firstResult.getString("title");
            String snippet = firstResult.getString("snippet").replaceAll("<[^>]+>", ""); // Remove HTML tags
            return "Sarlavha: " + title + "\nIzoh: " + snippet + "\nBatafsil: https://" + (apiUrl.contains("uz") ? "uz" : "en") + ".wikipedia.org/wiki/" + title.replace(" ", "_");
        } else {
            return "Natijalar topilmadi: " + query;
        }
    }

    private boolean isUzbek(String text) {

        return text.matches(".*[ўғқўқҳҳёюя].*");
    }
}