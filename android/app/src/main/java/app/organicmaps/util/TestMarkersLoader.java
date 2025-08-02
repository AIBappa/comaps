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
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

public class TestMarkersLoader {
    private static final String TAG = TestMarkersLoader.class.getSimpleName();
    private static final String TEST_MARKERS_FILE = "test_markers.json";
    private static final String PUNE_CATEGORY_NAME = "Pune Places";
    private static final String MUMBAI_CATEGORY_NAME = "Mumbai Places";
    
    public static class TestMarker {
        public final String name;
        public final String description;
        public final double latitude;
        public final double longitude;
        
        public TestMarker(String name, String description, double latitude, double longitude) {
            this.name = name;
            this.description = description;
            this.latitude = latitude;
            this.longitude = longitude;
        }
    }
    
    public static void loadAndShowTestMarkers(@NonNull Context context) {
        try {
            Logger.d(TAG, "Starting to load test markers...");
            
            // Check if BookmarkManager is available
            if (BookmarkManager.INSTANCE == null) {
                Logger.e(TAG, "BookmarkManager.INSTANCE is null!");
                return;
            }
            
            // Add a small delay to ensure map is fully initialized
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                loadTestMarkersDelayed(context);
            }, 500); // 500ms delay
            
        } catch (Exception e) {
            Logger.e(TAG, "Unexpected error starting test markers loading", e);
        }
    }
    
    private static void loadTestMarkersDelayed(@NonNull Context context) {
        try {
            String jsonString = loadJsonFromAssets(context, TEST_MARKERS_FILE);
            if (jsonString == null) {
                Logger.e(TAG, "Failed to load test markers JSON file");
                return;
            }
            
            Logger.d(TAG, "Loaded JSON file, parsing...");
            JSONObject jsonObject = new JSONObject(jsonString);
            
            // Create or get Pune category
            BookmarkCategory puneCategory = getOrCreateCategory(PUNE_CATEGORY_NAME);
            if (puneCategory != null) {
                Logger.d(TAG, "Created/found Pune category with ID: " + puneCategory.getId());
                JSONArray puneMarkers = jsonObject.getJSONArray("pune_markers");
                addMarkersToCategory(puneCategory, puneMarkers);
                Logger.d(TAG, "Added " + puneMarkers.length() + " Pune markers");
            } else {
                Logger.e(TAG, "Failed to create/get Pune category");
            }
            
            // Create or get Mumbai category
            BookmarkCategory mumbaiCategory = getOrCreateCategory(MUMBAI_CATEGORY_NAME);
            if (mumbaiCategory != null) {
                Logger.d(TAG, "Created/found Mumbai category with ID: " + mumbaiCategory.getId());
                JSONArray mumbaiMarkers = jsonObject.getJSONArray("mumbai_markers");
                addMarkersToCategory(mumbaiCategory, mumbaiMarkers);
                Logger.d(TAG, "Added " + mumbaiMarkers.length() + " Mumbai markers");
            } else {
                Logger.e(TAG, "Failed to create/get Mumbai category");
            }
            
            // Show all bookmark categories on map
            if (puneCategory != null) {
                Logger.d(TAG, "Showing Pune category on map");
                BookmarkManager.INSTANCE.showBookmarkCategoryOnMap(puneCategory.getId());
            }
            if (mumbaiCategory != null) {
                Logger.d(TAG, "Showing Mumbai category on map");
                BookmarkManager.INSTANCE.showBookmarkCategoryOnMap(mumbaiCategory.getId());
            }
            
            Logger.d(TAG, "Test markers loading completed");
            
        } catch (JSONException e) {
            Logger.e(TAG, "Error parsing test markers JSON", e);
        } catch (Exception e) {
            Logger.e(TAG, "Unexpected error loading test markers", e);
        }
    }
    
    @Nullable
    private static String loadJsonFromAssets(@NonNull Context context, @NonNull String fileName) {
        try {
            InputStream is = context.getAssets().open(fileName);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            return new String(buffer, StandardCharsets.UTF_8);
        } catch (IOException e) {
            Logger.e(TAG, "Error loading JSON from assets", e);
            return null;
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
        if (categoryId > 0) { // Assuming positive IDs are valid
            return BookmarkManager.INSTANCE.getCategoryById(categoryId);
        }
        
        Logger.e(TAG, "Failed to create category: " + categoryName);
        return null;
    }
    
    private static void addMarkersToCategory(@NonNull BookmarkCategory category, @NonNull JSONArray markers) {
        try {
            // Prepare the category for editing (this might set it as the active category)
            BookmarkManager.INSTANCE.prepareForSearch(category.getId());
            
            for (int i = 0; i < markers.length(); i++) {
                JSONObject marker = markers.getJSONObject(i);
                String name = marker.getString("name");
                String description = marker.getString("description");
                double latitude = marker.getDouble("latitude");
                double longitude = marker.getDouble("longitude");
                
                // Check if bookmark already exists to avoid duplicates
                if (!bookmarkExists(category, name, latitude, longitude)) {
                    // Add bookmark using the standard flow
                    Logger.d(TAG, "Attempting to create bookmark: " + name + " at " + latitude + ", " + longitude);
                    Bookmark bookmark = BookmarkManager.INSTANCE.addNewBookmark(latitude, longitude);
                    
                    if (bookmark != null) {
                        Logger.d(TAG, "Successfully created bookmark: " + name + " with ID: " + bookmark.getBookmarkId());
                        
                        // Move bookmark to the desired category if it's not already there
                        if (bookmark.getCategoryId() != category.getId()) {
                            Logger.d(TAG, "Moving bookmark from category " + bookmark.getCategoryId() + " to " + category.getId());
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
                        
                        Logger.d(TAG, "Created bookmark: " + name);
                    } else {
                        Logger.e(TAG, "Failed to create bookmark: " + name + " - addNewBookmark returned null");
                        Logger.e(TAG, "This might indicate that no category is set as 'last edited' or the map engine is not ready");
                    }
                }
            }
            
            // Ensure the category is visible on the map
            BookmarkManager.INSTANCE.setVisibility(category.getId(), true);
            
        } catch (JSONException e) {
            Logger.e(TAG, "Error parsing marker data", e);
        }
    }
    
    private static boolean bookmarkExists(@NonNull BookmarkCategory category, @NonNull String name, 
                                        double latitude, double longitude) {
        // Simple check to avoid duplicate bookmarks
        // In a real implementation, you might want a more sophisticated duplicate detection
        return false; // For now, always add new bookmarks
    }
    
    public static void clearTestMarkers() {
        // Remove test categories if they exist
        removeCategory(PUNE_CATEGORY_NAME);
        removeCategory(MUMBAI_CATEGORY_NAME);
    }
    
    private static void removeCategory(@NonNull String categoryName) {
        for (BookmarkCategory category : BookmarkManager.INSTANCE.getCategories()) {
            if (categoryName.equals(category.getName())) {
                BookmarkManager.INSTANCE.deleteCategory(category.getId());
                Logger.d(TAG, "Removed category: " + categoryName);
                break;
            }
        }
    }
}