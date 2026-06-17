package com.example.travelmemory;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PackingItem;
import com.example.travelmemory.model.Trip;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.util.List;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class DatabaseUserIsolationTest {

    private DatabaseHelper dbHelper;

    @Before
    public void setUp() {
        Context context = ApplicationProvider.getApplicationContext();
        context.deleteDatabase("travel_memory.db");
        dbHelper = new DatabaseHelper(context);
    }

    @Test
    public void tripsAreFilteredByOwnerUser() {
        int userAId = (int) dbHelper.registerUser("alice", "123456", "Alice");
        int userBId = (int) dbHelper.registerUser("bob", "123456", "Bob");

        Trip tripA = createTrip(userAId, "Alice Trip", "Xiamen");
        Trip tripB = createTrip(userBId, "Bob Trip", "Qingdao");
        dbHelper.addTrip(tripA);
        dbHelper.addTrip(tripB);

        List<Trip> aliceTrips = dbHelper.getTripsForUser(userAId);
        List<Trip> bobTrips = dbHelper.getTripsForUser(userBId);

        assertEquals(1, aliceTrips.size());
        assertEquals("Alice Trip", aliceTrips.get(0).getName());
        assertEquals(1, bobTrips.size());
        assertEquals("Bob Trip", bobTrips.get(0).getName());
    }

    @Test
    public void userProfileFieldsArePersistedAndDefaultSafely() {
        int userId = (int) dbHelper.registerUser("profile_user", "123456", "旅行者");

        assertEquals(0, dbHelper.getCurrentUser(userId).getAge());
        assertEquals("", dbHelper.getCurrentUser(userId).getGender());
        assertEquals("", dbHelper.getCurrentUser(userId).getTravelPreference());
        assertEquals(0, dbHelper.getCurrentUser(userId).getProfileCompleted());

        dbHelper.updateUserProfile(userId, "小夏", 24, "女", "自然风光", true);

        assertEquals("小夏", dbHelper.getCurrentUser(userId).getNickname());
        assertEquals(24, dbHelper.getCurrentUser(userId).getAge());
        assertEquals("女", dbHelper.getCurrentUser(userId).getGender());
        assertEquals("自然风光", dbHelper.getCurrentUser(userId).getTravelPreference());
        assertEquals(1, dbHelper.getCurrentUser(userId).getProfileCompleted());
    }

    @Test
    public void packingItemsUseNewCategoriesAndPersistPerTrip() {
        int userId = (int) dbHelper.registerUser("packing_user", "123456", "Packing");
        int tripAId = (int) dbHelper.addTrip(createTrip(userId, "Trip A", "Xiamen"));
        int tripBId = (int) dbHelper.addTrip(createTrip(userId, "Trip B", "Qingdao"));

        dbHelper.ensurePackingSelectionPool(tripAId);
        dbHelper.ensurePackingSelectionPool(tripBId);

        List<PackingItem> tripAItems = dbHelper.getPackingItemsForTrip(tripAId);
        assertContainsCategory(tripAItems, "洗漱类");
        assertContainsCategory(tripAItems, "衣物类");
        assertContainsCategory(tripAItems, "证件类");
        assertContainsCategory(tripAItems, "数码类");
        assertContainsCategory(tripAItems, "出行类");
        assertContainsCategory(tripAItems, "药品类");
        assertContainsCategory(tripAItems, "其他类");

        PackingItem custom = new PackingItem();
        custom.setTripId(tripAId);
        custom.setCategory("其他类");
        custom.setItemName("防水袋");
        custom.setIsSelected(1);
        custom.setIsPacked(0);
        custom.setIsCustom(1);
        custom.setCreatedAt("2026-06-09");
        long customId = dbHelper.addPackingItem(custom);
        dbHelper.updatePackingItemPacked((int) customId, true);

        assertPackedState(dbHelper.getPackingItemsForTrip(tripAId), "防水袋", 1);
        assertItemAbsent(dbHelper.getPackingItemsForTrip(tripBId), "防水袋");
    }

    private Trip createTrip(int userId, String name, String destination) {
        Trip trip = new Trip();
        trip.setUserId(userId);
        trip.setName(name);
        trip.setDestination(destination);
        trip.setStartDate("2026-06-01");
        trip.setEndDate("2026-06-02");
        trip.setCoverPath("");
        trip.setCreatedAt("2026-06-08");
        return trip;
    }

    private void assertContainsCategory(List<PackingItem> items, String category) {
        for (PackingItem item : items) {
            if (category.equals(item.getCategory())) {
                return;
            }
        }
        org.junit.Assert.fail("Missing packing category: " + category);
    }

    private void assertPackedState(List<PackingItem> items, String itemName, int expectedPacked) {
        for (PackingItem item : items) {
            if (itemName.equals(item.getItemName())) {
                assertEquals(expectedPacked, item.getIsPacked());
                return;
            }
        }
        org.junit.Assert.fail("Missing packing item: " + itemName);
    }

    private void assertItemAbsent(List<PackingItem> items, String itemName) {
        for (PackingItem item : items) {
            if (itemName.equals(item.getItemName())) {
                org.junit.Assert.fail("Unexpected packing item in another trip: " + itemName);
            }
        }
    }
}
