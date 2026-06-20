package com.example.travelmemory;

import com.example.travelmemory.model.Photo;
import com.example.travelmemory.utils.AlbumPhotoUtils;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class AlbumPhotoUtilsTest {

    @Test
    public void mergeUniquePhotoPathsKeepsAlbumAndDiaryPhotosWithoutDuplicates() {
        Photo albumPhoto = photo(1, -1, "/storage/emulated/0/DCIM/a.jpg");
        Photo diaryPhoto = photo(2, 9, "/storage/emulated/0/DCIM/b.jpg");
        Photo duplicateDiaryPhoto = photo(3, 10, " /storage/emulated/0/DCIM/a.jpg ");

        List<String> merged = AlbumPhotoUtils.mergeUniquePhotoPaths(
                Arrays.asList(albumPhoto, diaryPhoto, duplicateDiaryPhoto),
                Arrays.asList("/storage/emulated/0/DCIM/b.jpg", "content://media/external/images/media/11"));

        assertEquals(3, merged.size());
        assertEquals("/storage/emulated/0/DCIM/a.jpg", merged.get(0));
        assertEquals("/storage/emulated/0/DCIM/b.jpg", merged.get(1));
        assertEquals("content://media/external/images/media/11", merged.get(2));
    }

    private Photo photo(int id, int dayRecordId, String imagePath) {
        Photo photo = new Photo();
        photo.setId(id);
        photo.setTripId(7);
        photo.setDayRecordId(dayRecordId);
        photo.setImagePath(imagePath);
        photo.setCreatedAt("2026-06-20");
        return photo;
    }
}
