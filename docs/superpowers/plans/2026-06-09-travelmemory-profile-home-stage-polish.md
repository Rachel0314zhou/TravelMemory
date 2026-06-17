# TravelMemory Profile Home Stage Polish Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Continue the TravelMemory MVP iteration by simplifying the home and settings pages, adding profile questionnaire/profile display, fixing trip stage transition, and polishing item layouts without changing the Java + XML + Activity architecture.

**Architecture:** Keep the existing Activity, Adapter, Model, and DatabaseHelper structure. Extend the existing `user` table with nullable profile fields through SQLite upgrade compatibility, and reuse `trip.status` for phase transitions. UI changes stay in XML layouts and small Activity binding changes.

**Tech Stack:** Android Java, XML layouts, SQLiteOpenHelper, SharedPreferences session manager, Material/CardView resources, existing Gradle/Robolectric test setup.

---

### Task 1: User Profile Data Compatibility

**Files:**
- Modify: `app/src/main/java/com/example/travelmemory/model/User.java`
- Modify: `app/src/main/java/com/example/travelmemory/db/DatabaseHelper.java`
- Test: `app/src/test/java/com/example/travelmemory/DatabaseUserIsolationTest.java`

- [ ] **Step 1: Write failing profile persistence test**

Add a test that registers a user, saves profile fields, and reads them back:

```java
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
```

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.DatabaseUserIsolationTest`

Expected: FAIL because `getAge`, `getGender`, `getTravelPreference`, `getProfileCompleted`, or `updateUserProfile` do not exist yet.

- [ ] **Step 3: Implement user profile fields**

Update `User` with `age`, `gender`, `travelPreference`, and `profileCompleted`. Update `DatabaseHelper` to create/upgrade nullable columns, read optional values, and save profile fields.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.DatabaseUserIsolationTest`

Expected: PASS.

---

### Task 2: Questionnaire and Personal Profile Screens

**Files:**
- Create: `app/src/main/java/com/example/travelmemory/activity/ProfileQuestionnaireActivity.java`
- Create: `app/src/main/java/com/example/travelmemory/activity/PersonalProfileActivity.java`
- Create: `app/src/main/res/layout/activity_profile_questionnaire.xml`
- Create: `app/src/main/res/layout/activity_personal_profile.xml`
- Modify: `app/src/main/AndroidManifest.xml`
- Modify: `app/src/main/java/com/example/travelmemory/activity/RegisterActivity.java`
- Modify: `app/src/main/java/com/example/travelmemory/activity/LoginActivity.java`

- [ ] **Step 1: Add navigation test**

Extend `TravelFlowRegressionTest` to check the manifest contains both new Activities and registration/login reference the questionnaire.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: FAIL because new Activities and layout files do not exist.

- [ ] **Step 3: Implement screens and navigation**

Add a simple questionnaire form with nickname, age, gender, preference, save and skip actions. Add a personal profile page showing avatar, nickname, age, gender, travel preference, trip count, and diary count, with an edit button that opens the questionnaire.

- [ ] **Step 4: Run tests**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: PASS.

---

### Task 3: Home and Settings Simplification

**Files:**
- Modify: `app/src/main/java/com/example/travelmemory/activity/MainActivity.java`
- Modify: `app/src/main/res/layout/activity_main.xml`
- Modify: `app/src/main/java/com/example/travelmemory/activity/SettingsActivity.java`
- Modify: `app/src/main/res/layout/activity_settings.xml`

- [ ] **Step 1: Add regression checks for removed modules**

Extend `TravelFlowRegressionTest` to assert `activity_main.xml` does not contain `今日旅行灵感`, `待办提醒`, or `回忆精选`, and `activity_settings.xml` does not contain `旅行偏好`, `数据管理`, `关于应用`, or `未来功能`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: FAIL because the old modules still exist.

- [ ] **Step 3: Simplify layouts and bindings**

Remove old home cards and Settings rows. Keep welcome, search, add trip, footprint summary, trip list, bottom navigation, profile entry, password/nickname account actions, and prominent logout.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: PASS.

---

### Task 4: Trip Stage Transition

**Files:**
- Modify: `app/src/main/java/com/example/travelmemory/activity/TripDetailActivity.java`
- Test: `app/src/test/java/com/example/travelmemory/TravelFlowRegressionTest.java`

- [ ] **Step 1: Add stage transition regression check**

Add a source-level test that `TripDetailActivity.java` contains the visible copy `结束旅行，进入整理`, calls `updateTripStatus`, and uses `TravelConstants.MODE_POST_TRIP`.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: FAIL because the end-trip action does not exist yet.

- [ ] **Step 3: Implement end-trip action**

In during-trip mode, add an action card that shows a confirmation dialog. On confirmation, update `trip.status` to `post_trip`, set `mode=post_trip`, reload `currentTrip`, and render the post-trip mode.

- [ ] **Step 4: Run test to verify it passes**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: PASS.

---

### Task 5: Camera Removal and Entry Point Verification

**Files:**
- Modify only if needed after search: photo-related Activities/layouts and `AndroidManifest.xml`
- Test: `app/src/test/java/com/example/travelmemory/TravelFlowRegressionTest.java`

- [ ] **Step 1: Run source searches**

Run:

```powershell
rg -n "android.permission.CAMERA|ACTION_IMAGE_CAPTURE|拍照|相机|btnCamera|takePhoto" app/src/main
rg -n "new Intent\\([^\\n]*DuringTripActivity\\.class" app/src/main/java
```

Expected: No camera permission/camera launchers and no new main-flow `DuringTripActivity` intents. `layout_capture_card` is allowed because it means card screenshot capture, not camera.

- [ ] **Step 2: Fix any regressions found**

If a real camera entry appears, remove the button, permission, and launcher logic while preserving album selection.

---

### Task 6: Item Layout Visual Polish

**Files:**
- Modify: `app/src/main/res/layout/item_trip.xml`
- Modify: `app/src/main/res/layout/item_expense.xml`
- Modify: `app/src/main/res/layout/item_search_result.xml`
- Modify: `app/src/main/res/layout/item_regret.xml`
- Modify: `app/src/main/res/layout/item_day_record.xml`
- Modify: `app/src/main/res/layout/item_photo.xml`
- Modify: `app/src/main/res/layout/item_packing_item.xml`
- Modify: `app/src/main/res/layout/item_spot.xml`
- Modify/Create: reusable drawable resources for ripple/card/tag/danger/input backgrounds

- [ ] **Step 1: Add layout regression checks**

Extend `TravelFlowRegressionTest` to assert key item layouts use 16dp or larger card corners, have ripple/selectable backgrounds where clickable, and do not contain camera copy.

- [ ] **Step 2: Run test to verify it fails**

Run: `./gradlew.bat testDebugUnitTest --tests com.example.travelmemory.TravelFlowRegressionTest`

Expected: FAIL where current item resources still use smaller corners or lack expected resources.

- [ ] **Step 3: Polish item layouts**

Unify cards with white background, 16-20dp radius, 10-14dp margins, 14-18dp padding, light elevation/stroke, primary/warning tags, and clear text hierarchy.

- [ ] **Step 4: Run tests**

Run: `./gradlew.bat testDebugUnitTest`

Expected: PASS.

---

### Task 7: Build Verification

**Files:**
- No planned source changes unless compile errors reveal missing IDs/imports.

- [ ] **Step 1: Compile project**

Run: `./gradlew.bat assembleDebug`

Expected: BUILD SUCCESSFUL.

- [ ] **Step 2: Final source searches**

Run:

```powershell
rg -n "android.permission.CAMERA|ACTION_IMAGE_CAPTURE|拍照|相机|btnCamera|takePhoto" app/src/main
rg -n "new Intent\\([^\\n]*DuringTripActivity\\.class" app/src/main/java
```

Expected: No real camera functionality and no main-flow `DuringTripActivity` launch.

- [ ] **Step 3: Summarize modified files and remaining work**

Report modified Activities, new Activities, layouts, item layouts, drawables, profile data strategy, stage transition strategy, camera removal status, and remaining next-round polish items.
