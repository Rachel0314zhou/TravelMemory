package com.example.travelmemory;

import static org.junit.Assert.assertEquals;

import android.content.Context;

import androidx.test.core.app.ApplicationProvider;

import com.example.travelmemory.db.DatabaseHelper;
import com.example.travelmemory.model.PackingItem;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.Spot;
import com.example.travelmemory.model.TimelineItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.utils.TripStateManager;
import com.example.travelmemory.utils.TravelConstants;

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
    public void tripSearchResultsUseCurrentUserAndKeyword() {
        int userAId = (int) dbHelper.registerUser("search_alice", "123456", "Alice");
        int userBId = (int) dbHelper.registerUser("search_bob", "123456", "Bob");

        dbHelper.addTrip(createTrip(userAId, "Beach Weekend", "Xiamen"));
        dbHelper.addTrip(createTrip(userAId, "Mountain Walk", "Huangshan"));
        dbHelper.addTrip(createTrip(userBId, "Beach Weekend", "Qingdao"));

        List<com.example.travelmemory.model.SearchResult> aliceBeachResults =
                dbHelper.searchResultsForUser(userAId, "Beach");
        List<com.example.travelmemory.model.SearchResult> bobBeachResults =
                dbHelper.searchResultsForUser(userBId, "Beach");

        assertEquals(1, aliceBeachResults.size());
        assertEquals("Beach Weekend", aliceBeachResults.get(0).getTitle());
        assertEquals("Xiamen", aliceBeachResults.get(0).getSubtitle());
        assertEquals(1, bobBeachResults.size());
        assertEquals("Qingdao", bobBeachResults.get(0).getSubtitle());
        assertEquals(2, dbHelper.getTripSearchResultsForUser(userAId).size());
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
    public void nicknameSaveUsesExplicitCurrentUserOnly() {
        int userAId = (int) dbHelper.registerUser("alice_nickname", "123456", "Alice");
        int userBId = (int) dbHelper.registerUser("bob_nickname", "123456", "Bob");

        int rows = dbHelper.saveNicknameForUser(userAId, "Alice New");

        assertEquals(1, rows);
        assertEquals("Alice New", dbHelper.getCurrentUser(userAId).getNickname());
        assertEquals("Bob", dbHelper.getCurrentUser(userBId).getNickname());
    }

    @Test
    public void seedSetupDoesNotCreateFixedTripsForNewUsers() {
        int userId = (int) dbHelper.registerUser("seedless_user", "123456", "Seedless");

        dbHelper.ensureSeedDataForUser(userId);

        org.junit.Assert.assertTrue(dbHelper.getTripsForUser(userId).isEmpty());
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

    @Test
    public void prePlansPersistPendingStatusAndSyncCompletedCheckinsOnce() {
        int userId = (int) dbHelper.registerUser("plan_user", "123456", "Plan");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Plan Trip", "Xiamen"));

        PrePlan pendingFood = new PrePlan();
        pendingFood.setTripId(tripId);
        pendingFood.setType("想吃的美食");
        pendingFood.setTitle("沙茶面");
        pendingFood.setLocation("中山路");
        pendingFood.setTargetDate("2026-06-01");
        pendingFood.setTimeText("12:00");
        pendingFood.setNote("午餐");
        pendingFood.setStatus(PrePlan.STATUS_PENDING);
        pendingFood.setIsCompleted(0);
        pendingFood.setCreatedAt("2026-06-09");
        long planId = dbHelper.addPrePlan(pendingFood);

        PrePlan saved = dbHelper.getPrePlanById((int) planId);
        assertEquals(PrePlan.STATUS_PENDING, saved.getStatus());

        dbHelper.completePrePlanAndSyncCheckIn(saved);
        dbHelper.completePrePlanAndSyncCheckIn(saved);

        List<Spot> spots = dbHelper.getSpotsForTrip(tripId);
        assertEquals(1, spots.size());
        assertEquals("沙茶面", spots.get(0).getName());
        assertEquals("美食", spots.get(0).getType());
        assertEquals((int) planId, spots.get(0).getSourcePlanId());
        assertEquals(1, dbHelper.getPrePlanById((int) planId).getIsCompleted());
    }

    @Test
    public void prePlansPersistDayIndexAndSortByTimeSectionWithinDay() {
        int userId = (int) dbHelper.registerUser("daily_plan_user", "123456", "Daily Plan");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Daily Plan Trip", "Xiamen"));

        PrePlan night = createPlan(tripId, 1, PrePlan.TIME_SECTION_EVENING, "Night walk");
        PrePlan pending = createPlan(tripId, 1, PrePlan.TIME_SECTION_PENDING, "Buy postcard");
        PrePlan morning = createPlan(tripId, 1, PrePlan.TIME_SECTION_MORNING, "Museum");
        PrePlan afternoon = createPlan(tripId, 1, PrePlan.TIME_SECTION_AFTERNOON, "Coffee");
        PrePlan otherDay = createPlan(tripId, 0, PrePlan.TIME_SECTION_MORNING, "Arrival");

        dbHelper.addPrePlan(night);
        dbHelper.addPrePlan(pending);
        dbHelper.addPrePlan(morning);
        dbHelper.addPrePlan(afternoon);
        dbHelper.addPrePlan(otherDay);

        List<PrePlan> dayTwoPlans = dbHelper.getPrePlansForTripDaySorted(tripId, 1);

        assertEquals(4, dayTwoPlans.size());
        assertEquals("Museum", dayTwoPlans.get(0).getTitle());
        assertEquals("Coffee", dayTwoPlans.get(1).getTitle());
        assertEquals("Night walk", dayTwoPlans.get(2).getTitle());
        assertEquals("Buy postcard", dayTwoPlans.get(3).getTitle());
        assertEquals(1, dayTwoPlans.get(0).getDayIndex());
        assertEquals("2026-06-02", dayTwoPlans.get(0).getTargetDate());
        assertEquals(PrePlan.TIME_SECTION_PENDING, dayTwoPlans.get(3).getTimeSection());
    }

    @Test
    public void timelineItemsPersistSortAndUpdateCheckinInSingleRecord() {
        int userId = (int) dbHelper.registerUser("timeline_user", "123456", "Timeline");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Timeline Trip", "Xiamen"));

        TimelineItem morning = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_MORNING, "Museum");
        TimelineItem pending = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_PENDING, "Postcard");
        TimelineItem evening = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_EVENING, "Night walk");

        long morningId = dbHelper.addTimelineItem(morning);
        dbHelper.addTimelineItem(pending);
        dbHelper.addTimelineItem(evening);

        List<TimelineItem> items = dbHelper.getTimelineItemsForTripDay(tripId, 0);

        assertEquals(3, items.size());
        assertEquals("Museum", items.get(0).getTitle());
        assertEquals("Night walk", items.get(1).getTitle());
        assertEquals("Postcard", items.get(2).getTitle());

        dbHelper.markTimelineItemDoneWithCheckin((int) morningId, "photo.jpg", "Great place", "happy", "2026-06-01 10:00");

        TimelineItem done = dbHelper.getTimelineItemById((int) morningId);
        assertEquals(TimelineItem.STATUS_DONE, done.getStatus());
        assertEquals("photo.jpg", done.getCheckinPhoto());
        assertEquals("Great place", done.getCheckinNote());
        assertEquals("happy", done.getCheckinMood());
        assertEquals("2026-06-01 10:00", done.getCheckinTime());
        assertEquals(33, dbHelper.getTimelineCompletionRate(tripId));

        dbHelper.markTimelineItemSkipped(items.get(2).getId());
        assertEquals(1, dbHelper.getSkippedTimelineItemsForTrip(tripId).size());
    }

    @Test
    public void timelineItemsAcceptPickerTimesAndExpandedPlanTypes() {
        int userId = (int) dbHelper.registerUser("timeline_picker_user", "123456", "Timeline Picker");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Picker Trip", "Xiamen"));

        TimelineItem shopping = createTimelineItem(tripId, 0, "14:00", "Buy gifts");
        shopping.setType("购物");
        TimelineItem hotel = createTimelineItem(tripId, 0, "09:30", "Check hotel");
        hotel.setType("住宿");
        TimelineItem dinner = createTimelineItem(tripId, 0, "20:00", "Dinner");
        dinner.setType(TimelineItem.TYPE_FOOD);
        TimelineItem pending = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_PENDING, "Decide later");

        dbHelper.addTimelineItem(shopping);
        dbHelper.addTimelineItem(hotel);
        dbHelper.addTimelineItem(dinner);
        dbHelper.addTimelineItem(pending);

        List<TimelineItem> items = dbHelper.getTimelineItemsForTripDay(tripId, 0);

        assertEquals(4, items.size());
        assertEquals("Check hotel", items.get(0).getTitle());
        assertEquals("09:30", items.get(0).getTimeSlot());
        assertEquals("住宿", items.get(0).getType());
        assertEquals("Buy gifts", items.get(1).getTitle());
        assertEquals("14:00", items.get(1).getTimeSlot());
        assertEquals("购物", items.get(1).getType());
        assertEquals("Dinner", items.get(2).getTitle());
        assertEquals("20:00", items.get(2).getTimeSlot());
        assertEquals("Decide later", items.get(3).getTitle());
        assertEquals(TimelineItem.TIME_SLOT_PENDING, items.get(3).getTimeSlot());
    }

    @Test
    public void timelineItemsPersistKindAndExcludeExtraCheckinsFromPlanCompletion() {
        int userId = (int) dbHelper.registerUser("timeline_kind_user", "123456", "Timeline Kind");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Kind Trip", "Xiamen"));

        TimelineItem fixedPlan = createTimelineItem(tripId, 0, "09:00", "Museum");
        fixedPlan.setKind(TimelineItem.KIND_PLAN_FIXED);
        fixedPlan.setStatus(TimelineItem.STATUS_DONE);

        TimelineItem pendingPlan = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_PENDING, "Buy postcard");
        pendingPlan.setKind(TimelineItem.KIND_PLAN_PENDING);

        TimelineItem extraCheckin = createTimelineItem(tripId, 0, "15:30", "Street musician");
        extraCheckin.setKind(TimelineItem.KIND_CHECKIN_EXTRA);
        extraCheckin.setStatus(TimelineItem.STATUS_DONE);

        dbHelper.addTimelineItem(fixedPlan);
        dbHelper.addTimelineItem(pendingPlan);
        dbHelper.addTimelineItem(extraCheckin);

        List<TimelineItem> items = dbHelper.getTimelineItemsForTripDay(tripId, 0);

        assertEquals(3, items.size());
        assertEquals(TimelineItem.KIND_PLAN_FIXED, items.get(0).getKind());
        assertEquals(TimelineItem.KIND_CHECKIN_EXTRA, items.get(1).getKind());
        assertEquals(TimelineItem.KIND_PLAN_PENDING, items.get(2).getKind());
        assertEquals(50, dbHelper.getTimelineCompletionRate(tripId));
    }

    @Test
    public void finishingTripGeneratesRegretsForUnfinishedPrePlansAndTimelinePlansOnce() {
        int userId = (int) dbHelper.registerUser("regret_loop_user", "123456", "Regret Loop");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Regret Loop Trip", "Xiamen"));

        PrePlan oldPrePlan = createPlan(tripId, 0, PrePlan.TIME_SECTION_PENDING, "Old tea plan");
        oldPrePlan.setType("Food");
        dbHelper.addPrePlan(oldPrePlan);

        TimelineItem fixedPlan = createTimelineItem(tripId, 0, "10:00", "Missed museum");
        fixedPlan.setKind(TimelineItem.KIND_PLAN_FIXED);
        TimelineItem pendingPlan = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_PENDING, "Maybe night market");
        pendingPlan.setKind(TimelineItem.KIND_PLAN_PENDING);
        TimelineItem extraCheckin = createTimelineItem(tripId, 0, "18:00", "Unexpected street show");
        extraCheckin.setKind(TimelineItem.KIND_CHECKIN_EXTRA);

        dbHelper.addTimelineItem(fixedPlan);
        dbHelper.addTimelineItem(pendingPlan);
        dbHelper.addTimelineItem(extraCheckin);

        TripStateManager stateManager = new TripStateManager(dbHelper);
        stateManager.finishTrip(tripId);
        stateManager.finishTrip(tripId);

        List<RegretItem> regrets = dbHelper.getRegretsForTrip(tripId);

        assertEquals(3, regrets.size());
        assertContainsRegret(regrets, "Old tea plan");
        assertContainsRegret(regrets, "Missed museum");
        assertContainsRegret(regrets, "Maybe night market");
        assertRegretAbsent(regrets, "Unexpected street show");
    }

    @Test
    public void regeneratingRegretsClearsCompletedTimelinePlanRegrets() {
        int userId = (int) dbHelper.registerUser("regret_sync_user", "123456", "Regret Sync");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Regret Sync Trip", "Xiamen"));

        TimelineItem fixedPlan = createTimelineItem(tripId, 0, "10:00", "Museum");
        fixedPlan.setKind(TimelineItem.KIND_PLAN_FIXED);
        TimelineItem pendingPlan = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_PENDING, "Night market");
        pendingPlan.setKind(TimelineItem.KIND_PLAN_PENDING);

        int fixedId = (int) dbHelper.addTimelineItem(fixedPlan);
        int pendingId = (int) dbHelper.addTimelineItem(pendingPlan);

        dbHelper.generateRegretsForTrip(tripId);
        assertEquals(2, dbHelper.countRegretsForTrip(tripId));

        dbHelper.markTimelineItemDone(fixedId);
        dbHelper.markTimelineItemDone(pendingId);
        dbHelper.generateRegretsForTrip(tripId);

        assertEquals(100, dbHelper.getTimelineCompletionRate(tripId));
        assertEquals(0, dbHelper.countRegretsForTrip(tripId));
    }

    @Test
    public void reviewRegretsRefreshWhenPendingTimelineOrPrePlanCompletes() {
        int userId = (int) dbHelper.registerUser("review_sync_user", "123456", "Review Sync");
        int tripId = (int) dbHelper.addTrip(createTrip(userId, "Review Sync Trip", "Xiamen"));

        PrePlan prePlan = createPlan(tripId, 0, PrePlan.TIME_SECTION_PENDING, "Buy postcard");
        long prePlanId = dbHelper.addPrePlan(prePlan);

        TimelineItem pendingPlan = createTimelineItem(tripId, 0, TimelineItem.TIME_SLOT_PENDING, "Maybe night market");
        pendingPlan.setKind(TimelineItem.KIND_PLAN_PENDING);
        int timelineId = (int) dbHelper.addTimelineItem(pendingPlan);

        TripStateManager stateManager = new TripStateManager(dbHelper);
        stateManager.finishTrip(tripId);
        assertEquals(TravelConstants.TripState.REVIEWING, dbHelper.getTripForUser(tripId, userId).getStatus());
        assertEquals(2, dbHelper.countRegretsForTrip(tripId));

        dbHelper.markTimelineItemDone(timelineId);
        dbHelper.markPrePlanCompleted((int) prePlanId, true);

        assertEquals(100, dbHelper.getTimelineCompletionRate(tripId));
        assertEquals(0, dbHelper.countRegretsForTrip(tripId));
    }

    @org.junit.Ignore("Legacy demo trip removed from runtime")
    @Test
    public void completeDemoTripCoversMemoryLoopAndStartsInReviewing() {
        int userId = (int) dbHelper.registerUser("demo_loop_user", "123456", "Demo Loop");

        int tripId = dbHelper.ensureCompleteDemoTripForUser(userId);
        Trip trip = dbHelper.getTripForUser(tripId, userId);

        assertEquals("厦门之旅", trip.getName());
        assertEquals("post_trip", trip.getStatus());
        assertEquals(4, dbHelper.getDayRecordsForTrip(tripId).size());
        assertEquals(5, dbHelper.getPhotosForTrip(tripId).size());
        assertEquals(7, dbHelper.getTimelineItemsForTrip(tripId).size());
        assertEquals(7, dbHelper.countPackingItemsForTrip(tripId));
        assertEquals(6, dbHelper.getExpensesForTrip(tripId).size());
        assertEquals(2, dbHelper.countRegretsForTrip(tripId));
        assertEquals(67, dbHelper.getTimelineCompletionRate(tripId));

        int resetTripId = dbHelper.ensureCompleteDemoTripForUser(userId);
        assertEquals(tripId, resetTripId);
        assertEquals(2, dbHelper.countRegretsForTrip(tripId));
    }

    @org.junit.Ignore("Legacy demo trip removed from runtime")
    @Test
    public void refreshingDemoTripKeepsUserEditedCoverPath() {
        int userId = (int) dbHelper.registerUser("demo_cover_user", "123456", "Demo Cover");
        int tripId = dbHelper.ensureCompleteDemoTripForUser(userId);
        Trip trip = dbHelper.getTripForUser(tripId, userId);
        trip.setCoverPath("/tmp/user_selected_cover.jpg");
        dbHelper.updateTrip(trip);

        int refreshedTripId = dbHelper.ensureCompleteDemoTripForUser(userId);
        Trip refreshedTrip = dbHelper.getTripForUser(refreshedTripId, userId);

        assertEquals(tripId, refreshedTripId);
        assertEquals("/tmp/user_selected_cover.jpg", refreshedTrip.getCoverPath());
    }

    @Test
    public void newUsersStartWithoutInjectedTripsUntilTheyCreateOne() {
        int userId = (int) dbHelper.registerUser("empty_trip_user", "123456", "Empty Trip");

        org.junit.Assert.assertTrue(dbHelper.getTripsForUser(userId).isEmpty());
        org.junit.Assert.assertFalse(dbHelper.hasTripsForUser(userId));
    }

    @Test
    public void creatingTripStillWorksWithoutAnyInjectedSeedTrips() {
        int userId = (int) dbHelper.registerUser("manual_trip_user", "123456", "Manual Trip");
        Trip trip = createTrip(userId, "My First Trip", "Suzhou");

        int tripId = (int) dbHelper.addTrip(trip);

        org.junit.Assert.assertTrue(tripId > 0);
        assertEquals(1, dbHelper.getTripsForUser(userId).size());
        assertEquals("My First Trip", dbHelper.getTripsForUser(userId).get(0).getName());
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

    private PrePlan createPlan(int tripId, int dayIndex, String timeSection, String title) {
        PrePlan plan = new PrePlan();
        plan.setTripId(tripId);
        plan.setDayIndex(dayIndex);
        plan.setTimeSection(timeSection);
        plan.setType("Other");
        plan.setTitle(title);
        plan.setLocation("");
        plan.setNote("");
        plan.setIsCompleted(0);
        plan.setCreatedAt("2026-06-09");
        return plan;
    }

    private TimelineItem createTimelineItem(int tripId, int dayIndex, String timeSlot, String title) {
        TimelineItem item = new TimelineItem();
        item.setTripId(tripId);
        item.setDayIndex(dayIndex);
        item.setTimeSlot(timeSlot);
        item.setType(TimelineItem.TYPE_OTHER);
        item.setTitle(title);
        item.setStatus(TimelineItem.STATUS_PLANNED);
        item.setCreatedAt("2026-06-09");
        return item;
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

    private void assertContainsRegret(List<RegretItem> regrets, String title) {
        for (RegretItem item : regrets) {
            if (title.equals(item.getTitle())) {
                return;
            }
        }
        org.junit.Assert.fail("Missing regret: " + title);
    }

    private void assertRegretAbsent(List<RegretItem> regrets, String title) {
        for (RegretItem item : regrets) {
            if (title.equals(item.getTitle())) {
                org.junit.Assert.fail("Unexpected regret: " + title);
            }
        }
    }
}
