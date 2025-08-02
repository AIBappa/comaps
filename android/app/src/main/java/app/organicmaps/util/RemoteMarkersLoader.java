package app.organicmaps.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import app.organicmaps.sdk.bookmarks.data.Bookmark;
import app.organicmaps.sdk.bookmarks.data.BookmarkCategory;
import app.organicmaps.sdk.bookmarks.data.BookmarkManager;
import app.organicmaps.sdk.util.log.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RemoteMarkersLoader {
    private static final String TAG = RemoteMarkersLoader.class.getSimpleName();
    
    // Replace with your actual Cloudflare server URL
    private static final String REMOTE_MARKERS_URL = "https://your-cloudflare-worker.your-domain.workers.dev/api/markers";
    
    private static final ExecutorService executor = Executors.newSingleThreadExecutor();
    
    public static void loadAndShowRemoteMarkers(@NonNull Context context) {
        executor.execute(() -> {
            try {
                String jsonData = fetchMarkersFromServer();
                if (jsonData != null) {
                    parseAndDisplayMarkers(jsonData);
                } else {
                    Logger.e(TAG, "Failed to fetch markers from server");
                }
            } catch (Exception e) {
                Logger.e(TAG, "Error loading remote markers", e);
            }
        });
    }
    
    @Nullable
    private static String fetchMarkersFromServer() {
        try {
            URL url = new URL(REMOTE_MARKERS_URL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(10000); // 10 seconds
            connection.setReadTimeout(15000);    // 15 seconds
            connection.setRequestProperty("Accept", "application/json");
            
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_OK) {
                InputStream inputStream = connection.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
                StringBuilder response = new StringBuilder();
                String line;
                
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                
                reader.close();
                inputStream.close();
                connection.disconnect();
                
                return response.toString();
            } else {
                Logger.e(TAG, "Server returned response code: " + responseCode);
                connection.disconnect();
                return null;
            }
        } catch (IOException e) {
            Logger.e(TAG, "Network error while fetching markers", e);
            return null;
        }
    }
    
    private static void parseAndDisplayMarkers(@NonNull String jsonData) {
        try {
            JSONObject jsonObject = new JSONObject(jsonData);
            
            // Clear existing categories first (optional)
            clearPreviousMarkers();
            
            // Assuming the server returns categories with markers
            JSONArray categories = jsonObject.getJSONArray("categories");
            
            for (int i = 0; i < categories.length(); i++) {
                JSONObject categoryObj = categories.getJSONObject(i);
                String categoryName = categoryObj.getString("name");
                JSONArray markers = categoryObj.getJSONArray("markers");
                
                // Create or get category
                BookmarkCategory category = getOrCreateCategory(categoryName);
                if (category != null) {
                    addMarkersToCategory(category, markers);
                    Logger.d(TAG, "Added " + markers.length() + " markers to " + categoryName);
                }
            }
            
        } catch (JSONException e) {
            Logger.e(TAG, "Error parsing remote markers JSON", e);
        }
    }
    
    @Nullable
    private static BookmarkCategory getOrCreateCategory(@NonNull String categoryName) {
        // First check if category already exists
        for (BookmarkCategory category : BookmarkManager.INSTANCE.getCategories()) {
            if (categoryName.equals(category.getName())) {
                return category;
            }
        }
        
        // Create new category if it doesn't exist
        long categoryId = BookmarkManager.INSTANCE.createCategory(categoryName);
        if (categoryId > 0) {
            return BookmarkManager.INSTANCE.getCategoryById(categoryId);
        }
        
        Logger.e(TAG, "Failed to create category: " + categoryName);
        return null;
    }
    
    private static void addMarkersToCategory(@NonNull BookmarkCategory category, @NonNull JSONArray markers) {
        try {
            for (int i = 0; i < markers.length(); i++) {
                JSONObject marker = markers.getJSONObject(i);
                String name = marker.getString("name");
                String description = marker.getString("description");
                double latitude = marker.getDouble("latitude");
                double longitude = marker.getDouble("longitude");
                
                // Add bookmark
                Bookmark bookmark = BookmarkManager.INSTANCE.addNewBookmark(latitude, longitude);
                
                if (bookmark != null) {
                    // Move bookmark to the desired category if it's not already there
                    if (bookmark.getCategoryId() != category.getId()) {
                        BookmarkManager.INSTANCE.changeBookmarkCategory(
                            bookmark.getCategoryId(), 
                            category.getId(), 
                            bookmark.getBookmarkId()
                        );
                    }
                    
                    // Set bookmark name and description
                    BookmarkManager.INSTANCE.setBookmarkParams(
                        bookmark.getBookmarkId(),
                        name,
                        BookmarkManager.INSTANCE.getLastEditedColor(),
                        description
                    );
                    
                    Logger.d(TAG, "Created remote bookmark: " + name);
                } else {
                    Logger.e(TAG, "Failed to create bookmark: " + name);
                }
            }
            
            // Ensure the category is visible on the map
            BookmarkManager.INSTANCE.setCategoryVisibility(category.getId(), true);
            
        } catch (JSONException e) {
            Logger.e(TAG, "Error parsing marker data", e);
        }
    }
    
    private static void clearPreviousMarkers() {
        // Remove specific remote categories if they exist
        // You can customize this based on your category naming convention
        for (BookmarkCategory category : BookmarkManager.INSTANCE.getCategories()) {
            String categoryName = category.getName();
            if (categoryName.startsWith("Remote ") || categoryName.contains("KrishiKraft")) {
                BookmarkManager.INSTANCE.deleteCategory(category.getId());
                Logger.d(TAG, "Removed previous category: " + categoryName);
            }
        }
    }
}
