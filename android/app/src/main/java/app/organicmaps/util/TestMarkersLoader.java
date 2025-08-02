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
import java.util.List;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Set;
import java.util.HashSet;
import java.util.ArrayList;
import java.util.zip.ZipOutputStream;
import java.util.zip.ZipEntry;

public class TestMarkersLoader {
    private static final String TEST_JSON_FILE = "test_markers.json";
    private static final String TEST_MARKERS_FILE = "test_markers.json"; // Added missing constant
    private static final String TEST_KMZ_FILE = "test_markers.kmz"; // Changed to KMZ
    private static final String TAG = "TestMarkersLoader";
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
            // First, let's check the current category state
            Logger.d(TAG, "=== CATEGORY STATE ANALYSIS ===");
            List<BookmarkCategory> existingCategories = BookmarkManager.INSTANCE.getCategories();
            Logger.d(TAG, "Total existing categories: " + existingCategories.size());
            
            for (int i = 0; i < existingCategories.size(); i++) {
                BookmarkCategory cat = existingCategories.get(i);
                Logger.d(TAG, "Category " + i + ": ID=" + cat.getId() + ", Name='" + cat.getName() + "', Visible=" + cat.isVisible());
            }
            
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
            
            // Also try loading KML file using native import
            loadKMLFile(context);
            
        } catch (JSONException e) {
            Logger.e(TAG, "Error parsing test markers JSON", e);
        } catch (Exception e) {
            Logger.e(TAG, "Unexpected error loading test markers", e);
        }
    }
    
    public static void loadKMLFile(@NonNull Context context) {
        try {
            Logger.d(TAG, "=== SIMPLIFIED KMZ TEST ===");
            
            // Check if BookmarkManager is available first
            if (BookmarkManager.INSTANCE == null) {
                throw new RuntimeException("BookmarkManager.INSTANCE is null - core system not ready");
            }
            
            // Create a simple listener to track the callback from native code
            BookmarkManager.BookmarksLoadingListener testListener = new BookmarkManager.BookmarksLoadingListener() {
                @Override
                public void onBookmarksLoadingStarted() {
                    Logger.d(TAG, "� Native callback: Loading started");
                }
                
                @Override
                public void onBookmarksFileImportSuccessful() {
                    Logger.d(TAG, "✅ Native callback: Import SUCCESS!");
                    // This proves native C++ processed our KMZ and called back to Java
                    
                    // Now show the imported categories
                    List<BookmarkCategory> categories = BookmarkManager.INSTANCE.getCategories();
                    Logger.d(TAG, "📊 Total categories after import: " + categories.size());
                    
                    // Find and show the new "Test Places" category
                    for (BookmarkCategory cat : categories) {
                        if (cat.getName().contains("Test Places")) {
                            Logger.d(TAG, "🎯 Found Test Places category: " + cat.getName());
                            BookmarkManager.INSTANCE.showBookmarkCategoryOnMap(cat.getId());
                            Logger.d(TAG, "�️ Made Test Places visible on map");
                            return;
                        }
                    }
                }
                
                @Override
                public void onBookmarksFileImportFailed() {
                    Logger.e(TAG, "❌ Native callback: Import FAILED!");
                    // This tells us native C++ rejected our KMZ format
                }
            };
            
            // Register listener to get callbacks from native C++
            BookmarkManager.INSTANCE.addLoadingListener(testListener);
            
            // Get existing categories before import for comparison
            List<BookmarkCategory> categoriesBeforeImport = BookmarkManager.INSTANCE.getCategories();
            Logger.d(TAG, "� Categories before import: " + categoriesBeforeImport.size());
            
            // Create KMZ file with proper structure
            File tempDir = new File(context.getCacheDir(), "temp_kmz_test");
            if (tempDir.exists()) {
                deleteRecursively(tempDir);
            }
            tempDir.mkdirs();
            
            File tempKmzFile = new File(tempDir, TEST_KMZ_FILE);
            Logger.d(TAG, "🔨 Creating test KMZ: " + tempKmzFile.getAbsolutePath());
            
            // Create the KMZ file with exact structure from your discovery
            createTestKmzFile(tempKmzFile);
            
            Logger.d(TAG, "� KMZ created: exists=" + tempKmzFile.exists() + ", size=" + tempKmzFile.length());
            
            if (!tempKmzFile.exists() || tempKmzFile.length() == 0) {
                throw new RuntimeException("Failed to create KMZ file");
            }
            
            // Create URI and trigger the import (this goes to native C++)
            android.net.Uri kmzUri = android.net.Uri.fromFile(tempKmzFile);
            Logger.d(TAG, "� Triggering native import with KMZ...");
            
            boolean importStarted = BookmarkManager.INSTANCE.importBookmarksFile(
                context.getContentResolver(), 
                kmzUri, 
                tempDir
            );
            
            Logger.d(TAG, "📤 Import call result: " + importStarted);
            
            // The native callback will happen asynchronously
            // onBookmarksFileImportSuccessful() or onBookmarksFileImportFailed() will be called
            
            // Clean up listener after a delay
            new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                BookmarkManager.INSTANCE.removeLoadingListener(testListener);
                Logger.d(TAG, "🧹 Cleaned up test listener");
            }, 5000); // 5 second delay
            
        } catch (Exception e) {
            Logger.e(TAG, "💥 KMZ test failed with exception", e);
        }
    }
    
    private static void deleteRecursively(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteRecursively(file);
                } else {
                    file.delete();
                }
            }
        }
        dir.delete();
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
        // Check for an existing bookmark with the same name and coordinates in the category
        List<Bookmark> bookmarks = category.getBookmarks();
        for (Bookmark bookmark : bookmarks) {
            if (name.equals(bookmark.getName())
                && Double.compare(latitude, bookmark.getLat()) == 0
                && Double.compare(longitude, bookmark.getLon()) == 0) {
                return true;
            }
        }
        return false;
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
    
    private static void createTestKmzFile(@NonNull File kmzFile) throws IOException {
        Logger.d(TAG, "🔨 Creating KMZ file with proper structure...");
        
        try (FileOutputStream fos = new FileOutputStream(kmzFile);
             ZipOutputStream zos = new ZipOutputStream(fos)) {
            
            // Create doc.kml (root document with NetworkLinks)
            ZipEntry docEntry = new ZipEntry("doc.kml");
            zos.putNextEntry(docEntry);
            String docKml = createDocKml();
            zos.write(docKml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            Logger.d(TAG, "  ✅ Added doc.kml to KMZ");
            
            // Create files/Test Places.kml
            ZipEntry testPlacesEntry = new ZipEntry("files/Test Places.kml");
            zos.putNextEntry(testPlacesEntry);
            String testPlacesKml = createTestPlacesKml();
            zos.write(testPlacesKml.getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
            Logger.d(TAG, "  ✅ Added files/Test Places.kml to KMZ");
            
            zos.finish();
        }
        Logger.d(TAG, "🎉 KMZ file created successfully");
    }
    
    private static String createDocKml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<kml xmlns=\"http://earth.google.com/kml/2.0\">\n" +
               "<Document>\n" +
               "<name>CoMaps Test Bookmarks</name>\n" +
               "<NetworkLink><name>Test Places</name><Link><href>files/Test Places.kml</href></Link></NetworkLink>\n" +
               "</Document>\n" +
               "</kml>";
    }
    
    private static String createTestPlacesKml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
               "<kml xmlns=\"http://www.opengis.net/kml/2.2\" xmlns:gx=\"http://www.google.com/kml/ext/2.2\">\n" +
               "<Document>\n" +
               "  <Style id=\"placemark-red\">\n" +
               "    <IconStyle>\n" +
               "      <Icon>\n" +
               "        <href>https://comaps.at/placemarks/placemark-red.png</href>\n" +
               "      </Icon>\n" +
               "    </IconStyle>\n" +
               "  </Style>\n" +
               "  <Style id=\"placemark-blue\">\n" +
               "    <IconStyle>\n" +
               "      <Icon>\n" +
               "        <href>https://comaps.at/placemarks/placemark-blue.png</href>\n" +
               "      </Icon>\n" +
               "    </IconStyle>\n" +
               "  </Style>\n" +
               "  <name>Test Places</name>\n" +
               "  <visibility>1</visibility>\n" +
               "  <ExtendedData xmlns:mwm=\"https://comaps.app\">\n" +
               "    <mwm:name>\n" +
               "      <mwm:lang code=\"default\">Test Places</mwm:lang>\n" +
               "    </mwm:name>\n" +
               "    <mwm:annotation>Test markers created programmatically</mwm:annotation>\n" +
               "    <mwm:description>Test locations for bookmark import testing</mwm:description>\n" +
               "    <mwm:lastModified>2025-08-02T18:30:00Z</mwm:lastModified>\n" +
               "    <mwm:accessRules>Local</mwm:accessRules>\n" +
               "  </ExtendedData>\n" +
               "  \n" +
               "  <Placemark>\n" +
               "    <name>Test Location Pune</name>\n" +
               "    <description>Test marker in Pune</description>\n" +
               "    <TimeStamp><when>2025-08-02T18:30:00Z</when></TimeStamp>\n" +
               "    <styleUrl>#placemark-red</styleUrl>\n" +
               "    <Point><coordinates>73.8567,18.5204,0</coordinates></Point>\n" +
               "    <ExtendedData xmlns:mwm=\"https://comaps.app\">\n" +
               "      <mwm:name><mwm:lang code=\"default\">Test Location Pune</mwm:lang></mwm:name>\n" +
               "      <mwm:featureTypes><mwm:value>tourism-attraction</mwm:value></mwm:featureTypes>\n" +
               "      <mwm:scale>16</mwm:scale>\n" +
               "      <mwm:icon>Building</mwm:icon>\n" +
               "      <mwm:visibility>1</mwm:visibility>\n" +
               "    </ExtendedData>\n" +
               "  </Placemark>\n" +
               "  \n" +
               "  <Placemark>\n" +
               "    <name>Test Location Mumbai</name>\n" +
               "    <description>Test marker in Mumbai</description>\n" +
               "    <TimeStamp><when>2025-08-02T18:30:00Z</when></TimeStamp>\n" +
               "    <styleUrl>#placemark-blue</styleUrl>\n" +
               "    <Point><coordinates>72.8777,19.0760,0</coordinates></Point>\n" +
               "    <ExtendedData xmlns:mwm=\"https://comaps.app\">\n" +
               "      <mwm:name><mwm:lang code=\"default\">Test Location Mumbai</mwm:lang></mwm:name>\n" +
               "      <mwm:featureTypes><mwm:value>tourism-attraction</mwm:value></mwm:featureTypes>\n" +
               "      <mwm:scale>16</mwm:scale>\n" +
               "      <mwm:icon>Building</mwm:icon>\n" +
               "      <mwm:visibility>1</mwm:visibility>\n" +
               "    </ExtendedData>\n" +
               "  </Placemark>\n" +
               "  \n" +
               "  <Placemark>\n" +
               "    <name>Test Location Delhi</name>\n" +
               "    <description>Test marker in Delhi</description>\n" +
               "    <TimeStamp><when>2025-08-02T18:30:00Z</when></TimeStamp>\n" +
               "    <styleUrl>#placemark-red</styleUrl>\n" +
               "    <Point><coordinates>77.1025,28.7041,0</coordinates></Point>\n" +
               "    <ExtendedData xmlns:mwm=\"https://comaps.app\">\n" +
               "      <mwm:name><mwm:lang code=\"default\">Test Location Delhi</mwm:lang></mwm:name>\n" +
               "      <mwm:featureTypes><mwm:value>tourism-attraction</mwm:value></mwm:featureTypes>\n" +
               "      <mwm:scale>16</mwm:scale>\n" +
               "      <mwm:icon>Building</mwm:icon>\n" +
               "      <mwm:visibility>1</mwm:visibility>\n" +
               "    </ExtendedData>\n" +
               "  </Placemark>\n" +
               "</Document>\n" +
               "</kml>";
    }
}