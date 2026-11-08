package com.example.salcity.data;

import android.os.Handler;
import android.os.Looper;

import com.example.salcity.BuildConfig;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Cliente HTTP do backend SalCity. A chave do Gemini existe somente no backend. */
public final class SalCityAiApi {
    private static final int TIMEOUT_MS = 25_000;
    private static final ExecutorService EXECUTOR = Executors.newFixedThreadPool(2);
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private SalCityAiApi() { }

    public interface Callback {
        void onSuccess(JSONObject data);
        void onError(String message);
    }

    public static void buscarEventos(JSONObject filtros, Callback callback) {
        post("/events/search", filtros, callback);
    }

    public static void gerarRoteiro(JSONObject selecao, Callback callback) {
        post("/itineraries/generate", selecao, callback);
    }

    private static void post(String path, JSONObject payload, Callback callback) {
        EXECUTOR.execute(() -> {
            HttpURLConnection connection = null;
            try {
                String base = BuildConfig.SALCITY_AI_BASE_URL.replaceAll("/+$", "");
                connection = (HttpURLConnection) new URL(base + path).openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(TIMEOUT_MS);
                connection.setReadTimeout(TIMEOUT_MS);
                connection.setDoOutput(true);
                connection.setRequestProperty("Content-Type", "application/json; charset=utf-8");
                connection.setRequestProperty("Accept", "application/json");
                try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(connection.getOutputStream(), StandardCharsets.UTF_8))) {
                    writer.write(payload.toString());
                }
                int status = connection.getResponseCode();
                InputStream stream = status >= 200 && status < 300 ? connection.getInputStream() : connection.getErrorStream();
                StringBuilder responseText = new StringBuilder();
                if (stream != null) {
                    try (BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8))) {
                        String line;
                        while ((line = reader.readLine()) != null) responseText.append(line);
                    }
                }
                if (status < 200 || status >= 300) {
                    String message = "API SalCity respondeu HTTP " + status;
                    try { message = new JSONObject(responseText.toString()).optString("error", message); }
                    catch (Exception ignored) { }
                    throw new IllegalStateException(message);
                }
                JSONObject response = new JSONObject(responseText.toString());
                MAIN.post(() -> callback.onSuccess(response));
            } catch (Exception error) {
                String message = error.getMessage();
                if (message == null || message.trim().isEmpty()) message = "Não foi possível conectar à API SalCity.";
                final String result = message;
                MAIN.post(() -> callback.onError(result));
            } finally {
                if (connection != null) connection.disconnect();
            }
        });
    }

    public static JSONArray strings(Iterable<String> values) {
        JSONArray array = new JSONArray();
        for (String value : values) array.put(value);
        return array;
    }
}
