package com.example.travelmemory.utils;

import com.example.travelmemory.model.Photo;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public final class AlbumPhotoUtils {

    private AlbumPhotoUtils() {}

    public static List<String> mergeUniquePhotoPaths(List<Photo> albumPhotos, List<String> diaryPhotoPaths) {
        ArrayList<String> result = new ArrayList<>();
        LinkedHashSet<String> seenKeys = new LinkedHashSet<>();
        if (albumPhotos != null) {
            for (Photo photo : albumPhotos) {
                if (photo != null) addIfUnique(result, seenKeys, photo.getImagePath());
            }
        }
        if (diaryPhotoPaths != null) {
            for (String path : diaryPhotoPaths) {
                addIfUnique(result, seenKeys, path);
            }
        }
        return result;
    }

    private static void addIfUnique(List<String> result, Set<String> seenKeys, String imagePath) {
        String normalizedPath = imagePath == null ? "" : imagePath.trim();
        if (normalizedPath.isEmpty()) return;
        String key = normalizedPath.toLowerCase(Locale.ROOT);
        if (seenKeys.add(key)) {
            result.add(normalizedPath);
        }
    }
}
