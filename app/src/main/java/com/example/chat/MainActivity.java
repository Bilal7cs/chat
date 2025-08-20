package com.example.chat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.gson.JsonArray;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import okhttp3.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button sendButton;
    private Button geminiButton;
    private EditText userInput;
    private ListView chatDisplay;
    private ChatAdapter chatAdapter;
    private ArrayList<ChatMessage> chatMessages;

    private FirebaseAuth mAuth;

    private static final String API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-1.5-flash:generateContent?key=AIzaSyAesD3aCUXaoaqQrdB3HSttpf9n1np4BXs";

    private static class GeminiResponse {
        @SerializedName("candidates")
        public List<Candidate> candidates;

        public static class Candidate {
            @SerializedName("content")
            public Content content;
        }

        public static class Content {
            @SerializedName("parts")
            public List<Part> parts;
        }

        public static class Part {
            @SerializedName("text")
            public String text;
        }
    }

    private static class ChatMessage {
        private String sender;
        private String message;

        public ChatMessage(String sender, String message) {
            this.sender = sender;
            this.message = message;
        }

        public String getSender() {
            return sender;
        }

        public String getMessage() {
            return message;
        }
    }

    private class ChatAdapter extends ArrayAdapter<ChatMessage> {
        public ChatAdapter(Context context, ArrayList<ChatMessage> messages) {
            super(context, 0, messages);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ChatMessage message = getItem(position);
            if (convertView == null) {
                convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            }
            TextView senderText = convertView.findViewById(android.R.id.text1);
            TextView messageText = convertView.findViewById(android.R.id.text2);
            senderText.setText(message.getSender());
            messageText.setText(message.getMessage());
            return convertView;
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initializeViews();
        setupWindowInsets();
        setupSendButton();
        setupGeminiButton();
    }

    private void initializeViews() {
        sendButton = findViewById(R.id.sendButton);
        geminiButton = findViewById(R.id.geminiButton);
        userInput = findViewById(R.id.userInput);
        chatDisplay = findViewById(R.id.chatDisplay);
        mAuth = FirebaseAuth.getInstance();

        chatMessages = new ArrayList<>();
        chatAdapter = new ChatAdapter(this, chatMessages);
        chatDisplay.setAdapter(chatAdapter);
    }

    private void setupWindowInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
    }

    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String userInputText = userInput.getText().toString().trim();
            if (!userInputText.isEmpty()) {
                userInput.setText("");
                displayChatMessage("User", userInputText);
                sendToGeminiAPI(userInputText);
            }
        });
    }

    private void setupGeminiButton() {
        geminiButton.setOnClickListener(v -> {
            logoutUser();
        });
    }

    private void logoutUser() {
        mAuth.signOut();
        displayChatMessage("Gemini", "Logged out successfully");

        Intent intent = new Intent(MainActivity.this, Login.class);
        startActivity(intent);
        finish();

        Toast.makeText(MainActivity.this, "You have logged out.", Toast.LENGTH_SHORT).show();
    }

    private void displayChatMessage(String sender, String message) {
        runOnUiThread(() -> {
            chatMessages.add(new ChatMessage(sender, message));
            chatAdapter.notifyDataSetChanged();
            chatDisplay.smoothScrollToPosition(chatAdapter.getCount() - 1);
        });
    }

    private void sendToGeminiAPI(String userInputText) {
        OkHttpClient client = new OkHttpClient();

        JsonObject partObject = new JsonObject();
        partObject.addProperty("text", userInputText);

        JsonArray partsArray = new JsonArray();
        partsArray.add(partObject);

        JsonObject contentObject = new JsonObject();
        contentObject.add("parts", partsArray);

        JsonArray contentsArray = new JsonArray();
        contentsArray.add(contentObject);

        JsonObject requestObject = new JsonObject();
        requestObject.add("contents", contentsArray);

        RequestBody body = RequestBody.create(
                MediaType.get("application/json; charset=utf-8"),
                new Gson().toJson(requestObject)
        );

        Request request = new Request.Builder()
                .url(API_URL)
                .post(body)
                .addHeader("Content-Type", "application/json")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                displayChatMessage("Error", "Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                try {
                    if (!response.isSuccessful()) {
                        String errorBody = response.body() != null ? response.body().string() : "Unknown error";
                        handleErrorResponse(response.code(), errorBody);
                        return;
                    }

                    if (response.body() == null) {
                        displayChatMessage("Error", "Empty response from server");
                        return;
                    }

                    String jsonResponse = response.body().string();
                    GeminiResponse geminiResponse = new Gson().fromJson(jsonResponse, GeminiResponse.class);

                    if (geminiResponse != null && geminiResponse.candidates != null &&
                            !geminiResponse.candidates.isEmpty() &&
                            geminiResponse.candidates.get(0).content != null &&
                            geminiResponse.candidates.get(0).content.parts != null &&
                            !geminiResponse.candidates.get(0).content.parts.isEmpty()) {
                        String text = geminiResponse.candidates.get(0).content.parts.get(0).text;
                        displayChatMessage("Gemini", text);
                    } else {
                        displayChatMessage("Error", "Invalid response format");
                    }
                } catch (Exception e) {
                    displayChatMessage("Error", "Failed to process response: " + e.getMessage());
                } finally {
                    response.close();
                }
            }
        });
    }

    private void handleErrorResponse(int code, String errorBody) {
        String errorMessage;
        switch (code) {
            case 401:
                errorMessage = "Authentication failed. Please check your API key.";
                break;
            case 429:
                errorMessage = "Too many requests. Please try again later.";
                break;
            case 500:
                errorMessage = "Server error. Please try again later.";
                break;
            default:
                errorMessage = "Error " + code + ": " + errorBody;
        }
        displayChatMessage("Error", errorMessage);
    }
}