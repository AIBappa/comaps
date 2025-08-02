package app.organicmaps.util;

import android.content.Context;
import androidx.annotation.NonNull;
import app.organicmaps.sdk.bookmarks.data.Bookmark;
import app.organicmaps.sdk.bookmarks.data.BookmarkCategory;
import app.organicmaps.sdk.bookmarks.data.BookmarkManager;
import app.organicmaps.sdk.util.log.Logger;
import java.util.List;

public class SimpleBookmarkCreator {
    private static final String TAG = "SimpleBookmarkCreator";
    
    public static void createTestBookmarks(@NonNull Context context) {
        try {
            Logger.d(TAG, "🚀 Creating test bookmarks directly...");
            
            // Check if BookmarkManager is available
            if (BookmarkManager.INSTANCE == null) {
                throw new RuntimeException("BookmarkManager.INSTANCE is null");
            }
            
            // Create a test category
            Logger.d(TAG, "📁 Creating test category...");
            long categoryId = BookmarkManager.INSTANCE.createCategory("Direct Test Markers");
            BookmarkCategory category = BookmarkManager.INSTANCE.getCategoryById(categoryId);
            
            if (category == null) {
                throw new RuntimeException("Failed to create category");
            }
            
            Logger.d(TAG, "✅ Created category: " + category.getName() + " (ID: " + categoryId + ")");
            
            // Define test locations
            double[][] testLocations = {
                {18.5204, 73.8567}, // Pune
                {19.0760, 72.8777}, // Mumbai
                {28.7041, 77.1025}, // Delhi
                {12.9716, 77.5946}, // Bangalore
                {13.0827, 80.2707}  // Chennai
            };
            
            String[] locationNames = {
                "Pune Test Point",
                "Mumbai Test Point", 
                "Delhi Test Point",
                "Bangalore Test Point",
                "Chennai Test Point"
            };
            
            // Create bookmarks directly
            int successCount = 0;
            for (int i = 0; i < testLocations.length; i++) {
                try {
                    Logger.d(TAG, "🎯 Creating bookmark: " + locationNames[i]);
                    
                    // Create bookmark at location
                    Bookmark bookmark = BookmarkManager.INSTANCE.addNewBookmark(
                        testLocations[i][0], // latitude
                        testLocations[i][1]  // longitude
                    );
                    
                    if (bookmark != null) {
                        // Move bookmark to our category
                        BookmarkManager.INSTANCE.changeBookmarkCategory(
                            bookmark.getCategoryId(), 
                            bookmark.getBookmarkId(), 
                            categoryId
                        );
                        
                        // Set bookmark name and description
                        BookmarkManager.INSTANCE.setBookmarkParams(
                            bookmark.getBookmarkId(),
                            locationNames[i],
                            BookmarkManager.INSTANCE.getLastEditedColor(),
                            "Auto-generated test marker"
                        );
                        
                        successCount++;
                        Logger.d(TAG, "✅ Created bookmark: " + locationNames[i]);
                    } else {
                        Logger.e(TAG, "❌ Failed to create bookmark: " + locationNames[i]);
                    }
                } catch (Exception e) {
                    Logger.e(TAG, "❌ Error creating bookmark " + locationNames[i], e);
                }
            }
            
            if (successCount > 0) {
                // Show the category on map
                BookmarkManager.INSTANCE.showBookmarkCategoryOnMap(categoryId);
                Logger.d(TAG, "🗺️ Made category visible on map");
                Logger.d(TAG, "🎉 SUCCESS: Created " + successCount + " test bookmarks!");
            } else {
                throw new RuntimeException("Failed to create any bookmarks");
            }
            
        } catch (Exception e) {
            Logger.e(TAG, "💥 Failed to create test bookmarks", e);
            throw e;
        }
    }
}
