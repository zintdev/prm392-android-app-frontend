package com.example.prm392_android_app_frontend.data.repository;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.prm392_android_app_frontend.data.dto.BlogDto;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BlogRepository {

    private static final String TAG = "FetchBlogListData";
    private static final String API_URL = "https://www.nogizaka46.com/s/n46/api/list/blog?ima=4204&rw=32&st=0&callback=res";

    public interface BlogDataListener {
        void onBlogDataFetched(List<BlogDto> blogDtos);
        void onError(String errorMessage);
    }

    public void fetchData(BlogDataListener listener) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler mainHandler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            String result = "";
            HttpURLConnection urlConnection = null;
            try {
                URL url = new URL(API_URL);
                urlConnection = (HttpURLConnection) url.openConnection();
                InputStream in = urlConnection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(in));
                StringBuilder stringBuilder = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    stringBuilder.append(line).append("\n");
                }
                result = stringBuilder.toString();
            } catch (IOException e) {
                Log.e(TAG, "Error fetching data: " + e.getMessage());
                mainHandler.post(() -> {
                    if (listener != null) {
                        listener.onError("Error fetching data: " + e.getMessage());
                    }
                });
                return;
            } finally {
                if (urlConnection != null) {
                    urlConnection.disconnect();
                }
            }

            // Process the result on background thread
            List<BlogDto> blogDtos = null;
            String error = null;

            try {
                // The response is JSONP, so we need to remove the callback function wrapper "res(...);"
                String jsonString = result.substring(result.indexOf("(") + 1, result.lastIndexOf(")"));
                JSONObject jsonObject = new JSONObject(jsonString);

                // Get the data array from the JSON response
                JSONArray dataArray = null;
                if (jsonObject.has("data")) {
                    dataArray = jsonObject.getJSONArray("data");
                } else {
                    // If there's no "data" field, try using the whole object as an array
                    // or look for other potential field names
                    Log.w(TAG, "No 'data' field found in JSON response");
                    // Try common alternatives
                    if (jsonObject.has("items")) {
                        dataArray = jsonObject.getJSONArray("items");
                    } else if (jsonObject.has("blogs")) {
                        dataArray = jsonObject.getJSONArray("blogs");
                    } else {
                        // If no known field found, try to treat the entire object as an array
                        Log.e(TAG, "Unknown JSON structure: " + jsonObject.toString());
                        error = "Unknown JSON structure";
                    }
                }

                if (dataArray != null) {
                    blogDtos = new ArrayList<>();

                    for (int i = 0; i < dataArray.length(); i++) {
                        JSONObject itemObj = dataArray.getJSONObject(i);

                        // Extract data from JSON - adjust these keys based on your actual JSON structure
                        String id = itemObj.optString("id", "" + i);  // Use index as fallback id
                        String title = itemObj.optString("title", "No Title");
                        String date = itemObj.optString("date", itemObj.optString("created_at", itemObj.optString("updated_at", "No Date")));
                        String author = itemObj.optString("author", itemObj.optString("name", "Unknown Author"));
                        String summary = itemObj.optString("summary", itemObj.optString("content", itemObj.optString("text", "No Summary")));
                        String imageUrl = itemObj.optString("image", itemObj.optString("image_url", itemObj.optString("img", ""))); // Look for common image field names

                        // Clean HTML tags from summary if needed
                        String cleanSummary = cleanHtmlTags(summary);

                        BlogDto blogDto = new BlogDto(id, title, date, author, cleanSummary, cleanSummary, imageUrl);
                        blogDtos.add(blogDto);
                    }

                    Log.d(TAG, "Fetched " + blogDtos.size() + " blog items");
                }
            } catch (JSONException | StringIndexOutOfBoundsException e) {
                Log.e(TAG, "Error parsing JSON: " + e.getMessage());
                error = "Error parsing JSON: " + e.getMessage();
            }

            // Send result back to main thread
            final List<BlogDto> finalBlogDtos = blogDtos;
            final String finalError = error;

            mainHandler.post(() -> {
                if (finalError != null && listener != null) {
                    listener.onError(finalError);
                } else if (finalBlogDtos != null && listener != null) {
                    listener.onBlogDataFetched(finalBlogDtos);
                }
            });
        });
    }

    private String cleanHtmlTags(String htmlString) {
        if (htmlString == null) return "";

        // Basic HTML tag cleaning - this is a simple approach
        // For more complex HTML cleaning, you might want to use Jsoup library
        return htmlString
                .replaceAll("<br/?>", "\n")  // Replace <br> tags with newlines
                .replaceAll("<p/?>", "\n")   // Replace <p> tags with newlines
                .replaceAll("</p>", "")      // Remove closing </p> tags
                .replaceAll("<[^>]*>", "")   // Remove other HTML tags
                .replaceAll("&nbsp;", " ")   // Replace non-breaking spaces
                .replaceAll("&lt;", "<")     // Decode HTML entities
                .replaceAll("&gt;", ">")     // Decode HTML entities
                .replaceAll("&amp;", "&")    // Decode HTML entities
                .replaceAll("&quot;", "\"")  // Decode HTML entities
                .replaceAll("&#39;", "'");   // Decode HTML entities
    }
}