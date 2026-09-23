package com.dedokok.utils.other;

import com.dedokok.DedTools;

import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

import static com.dedokok.DedTools.mc;

/**
 * Fires a fully non-blocking request to the Telegram Bot API's sendMessage
 * endpoint. Never touches the render/tick thread beyond kicking the async
 * call off, so it can't cause a game freeze even if Telegram is slow/down.
 */
public final class TelegramNotifier {

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private TelegramNotifier() {
    }

    public static void sendAsync(String message, String token, String chatId) {


        if (token == null || token.isBlank() || chatId == null || chatId.isBlank()) {
            DedTools.LOGGER.warn("Telegram bot token/chat id is not set, " +
                    "notification skipped. Use /pw telegram <token> <chatId> to configure it.");

            return;
        }

        String url = "https://api.telegram.org/bot" + token + "/sendMessage"
                + "?chat_id=" + URLEncoder.encode(chatId, StandardCharsets.UTF_8)
                + "&text=" + URLEncoder.encode(message, StandardCharsets.UTF_8);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofSeconds(10))
                .GET()
                .build();

        CLIENT.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    if (response.statusCode() != 200) {
                        DedTools.LOGGER.warn("Telegram API returned {}: {}",
                                response.statusCode(), response.body());
                    }
                })
                .exceptionally(ex -> {
                    DedTools.LOGGER.error("Failed to reach Telegram API", ex);
                    return null;
                });
    }
}
