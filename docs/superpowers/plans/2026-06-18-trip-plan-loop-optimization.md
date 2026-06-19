# Trip Plan Loop Optimization Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Rework the existing search surface into a trip itinerary plan page and connect pre-trip plans to recording, spot/food check-ins, and post-trip organization with minimal architecture changes.

**Architecture:** Keep existing Activity names and navigation entry points, especially `SearchActivity`, to avoid broad Manifest and routing changes. Add only two compatibility columns: `pre_plan.time_text` for intra-day sorting and `spot.source_plan_id` for linking plan-origin check-ins.

**Tech Stack:** Java, Android Activities/XML layouts, SQLiteOpenHelper, RecyclerView, existing project drawables/colors/adapters/models.

---

## File Map

- Modify `app/src/main/java/com/example/travelmemory/db/DatabaseHelper.java`
  - Bump database version.
  - Add `pre_plan.time_text` and `spot.source_plan_id`.
  - Persist/read new model fields.
  - Add sorted plan queries and plan-derived spot helpers.
- Modify `app/src/main/java/com/example/travelmemory/model/PrePlan.java`
  - Add `timeText`.
- Modify `app/src/main/java/com/example/travelmemory/model/Spot.java`
  - Add `sourcePlanId`.
- Modify `app/src/main/java/com/example/travelmemory/activity/SearchActivity.java`
  - Keep class name.
  - Replace search behavior with itinerary timeline rendering.
- Modify `app/src/main/res/layout/activity_search.xml`
  - Replace search/mood UI with trip plan UI.
- Modify `app/src/main/res/layout/layout_bottom_nav.xml`
  - Rename bottom nav label from `搜索` to `计划`.
- Modify `app/src/main/res/values/strings.xml`
  - Rename search title/button strings where reused.
- Modify `app/src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java`
  - Add time input.
  - Preserve start-trip flow as secondary action.
- Modify `app/src/main/res/layout/activity_pre_trip_plan.xml`
  - Main CTA becomes add plan.
  - Remove top-right duplicate add button.
  - Secondary button starts travel record.
- Modify `app/src/main/java/com/example/travelmemory/adapter/PrePlanAdapter.java`
  - Show time and sorted itinerary-friendly information.
- Modify `app/src/main/res/layout/item_pre_plan.xml`
  - Add time display.
- Modify `app/src/main/java/com/example/travelmemory/activity/DayRecordActivity.java`
  - Add photo picker to unified record flow.
- Modify `app/src/main/res/layout/activity_day_record.xml`
  - Rename title to record today's trip.
  - Add photo selection button/status.
- Modify `app/src/main/java/com/example/travelmemory/activity/DuringTripActivity.java`
  - Merge diary/photo buttons into one entry.
- Modify `app/src/main/res/layout/activity_during_trip.xml`
  - Replace two buttons with one unified record button.
- Modify `app/src/main/java/com/example/travelmemory/activity/TripDetailActivity.java`
  - Merge during-trip action cards for diary/photo.
  - Keep post-trip supplement options stable unless compile requires small text changes.
- Modify `app/src/main/java/com/example/travelmemory/activity/SpotsActivity.java`
  - Show plan-origin spot/food candidates.
  - Completing a planned item inserts a `Spot` and marks the `PrePlan` complete.
  - Manual planned-outside check-in remains unchanged.
- Modify `app/src/main/java/com/example/travelmemory/adapter/SpotAdapter.java`
  - Show plan/manual source when available.
- Modify `app/src/main/res/layout/activity_spots.xml`
  - Add planned check-in section above manual form.
- Modify `app/src/main/res/layout/item_spot.xml`
  - Add source label if needed.

## Task 1: Data Compatibility Fields

**Files:**
- Modify `app/src/main/java/com/example/travelmemory/db/DatabaseHelper.java`
- Modify `app/src/main/java/com/example/travelmemory/model/PrePlan.java`
- Modify `app/src/main/java/com/example/travelmemory/model/Spot.java`

- [x] **Step 1: Bump database version and add columns**

Set `DATABASE_VERSION` from `6` to `7`.

In `onCreate`, create `spot` with `source_plan_id INTEGER DEFAULT 0` and `pre_plan` with `time_text TEXT`.

In `onUpgrade`, add:

```java
if (oldVersion < 7) {
    addColumnIfMissing(db, TABLE_PRE_PLAN, "time_text", "TEXT");
    addColumnIfMissing(db, TABLE_SPOT, "source_plan_id", "INTEGER DEFAULT 0");
}
```

- [x] **Step 2: Add `timeText` to `PrePlan`**

Add field, constructor parameter compatibility, getter, and setter:

```java
private String timeText;

public String getTimeText() { return timeText; }
public void setTimeText(String timeText) { this.timeText = timeText; }
```

Keep existing constructors working by defaulting `timeText` to `""`.

- [x] **Step 3: Add `sourcePlanId` to `Spot`**

Add field, getter, and setter:

```java
private int sourcePlanId;

public int getSourcePlanId() { return sourcePlanId; }
public void setSourcePlanId(int sourcePlanId) { this.sourcePlanId = sourcePlanId; }
```

Keep existing constructors working by defaulting `sourcePlanId` to `0`.

- [x] **Step 4: Read/write new columns**

Update `prePlanValues()` with:

```java
values.put("time_text", plan.getTimeText());
```

Update `addSpot()` with:

```java
values.put("source_plan_id", spot.getSourcePlanId());
```

Update `read Spot` creation using `getOptionalInt(cursor, "source_plan_id")`.

- [x] **Step 5: Add sorted plan and linked spot helpers**

Add helper methods:

```java
public List<PrePlan> getPrePlansForTripSorted(int tripId) {
    List<PrePlan> list = new ArrayList<>();
    Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=?",
            new String[]{String.valueOf(tripId)}, null, null,
            "CASE WHEN target_date IS NULL OR target_date='' THEN 1 ELSE 0 END, target_date ASC, time_text ASC, id ASC");
    readPrePlans(cursor, list);
    return list;
}

public boolean hasSpotForPrePlan(int prePlanId) {
    return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_SPOT + " WHERE source_plan_id=?", String.valueOf(prePlanId)) > 0;
}
```

If current code does not have `readPrePlans`, extract the repeated cursor loop from `getPrePlansForTrip`.

## Task 2: Itinerary Plan Page

**Files:**
- Modify `app/src/main/java/com/example/travelmemory/activity/SearchActivity.java`
- Modify `app/src/main/res/layout/activity_search.xml`
- Modify `app/src/main/res/layout/layout_bottom_nav.xml`
- Modify `app/src/main/res/values/strings.xml`

- [x] **Step 1: Replace bottom label**

Change bottom nav text:

```xml
android:text="计划"
```

- [x] **Step 2: Replace `SearchActivity` behavior**

Keep `SearchActivity` class name. Remove keyword and mood filtering fields. Load current user trips, choose newest trip by existing `getTripsForUser(currentUserId)` ordering, then fetch `getPrePlansForTripSorted(tripId)`.

Render into a vertical `LinearLayout` timeline to avoid creating a new adapter. Group by `targetDate`; calculate Day N relative to trip start when date is present.

- [x] **Step 3: Replace search layout**

Use existing colors and cards:

- Top bar title: `行程计划`
- Trip title/date summary
- Empty state: `还没有行前计划，先到行前准备中添加想去的地方和想吃的美食。`
- Scrollable timeline container with ID `layout_plan_timeline`

- [x] **Step 4: Keep navigation stable**

Keep `BottomNavHelper.setup(this, BottomNavHelper.TAB_SEARCH)` so the existing tab remains active.

## Task 3: Pre-Trip Plan Form and CTA

**Files:**
- Modify `app/src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java`
- Modify `app/src/main/res/layout/activity_pre_trip_plan.xml`
- Modify `app/src/main/java/com/example/travelmemory/adapter/PrePlanAdapter.java`
- Modify `app/src/main/res/layout/item_pre_plan.xml`

- [x] **Step 1: Change CTA layout**

Remove the top-right add button from the action bar.

Add a primary button above the list:

```xml
android:id="@+id/btn_add_pre_plan"
android:text="添加行前计划"
android:background="@drawable/btn_primary"
```

Keep `btn_skip_pre_trip`, restyle as secondary, and set text to `开始旅行记录`.

- [x] **Step 2: Add time input to plan dialog**

In `showPlanDialog`, add an `EditText etTime` after date:

```java
EditText etTime = new EditText(this);
etTime.setHint("计划时间，如 09:30");
layout.addView(etTime);
```

When editing, set `etTime.setText(editingPlan.getTimeText())`.

When saving, call `plan.setTimeText(etTime.getText().toString().trim())`.

- [x] **Step 3: Use sorted query**

Change `loadPlans()` to use:

```java
List<PrePlan> plans = dbHelper.getPrePlansForTripSorted(tripId);
```

- [x] **Step 4: Show time in item**

Add `tv_pre_plan_time` to item XML and bind:

```java
holder.tvTime.setText(isBlank(plan.getTimeText()) ? "时间：未设置" : "时间：" + plan.getTimeText());
```

## Task 4: Unified Diary and Photo Recording

**Files:**
- Modify `app/src/main/java/com/example/travelmemory/activity/DayRecordActivity.java`
- Modify `app/src/main/res/layout/activity_day_record.xml`
- Modify `app/src/main/java/com/example/travelmemory/activity/DuringTripActivity.java`
- Modify `app/src/main/res/layout/activity_during_trip.xml`
- Modify `app/src/main/java/com/example/travelmemory/activity/TripDetailActivity.java`

- [x] **Step 1: Add photo picker to `DayRecordActivity`**

Use the same album intent as `PhotoActivity`:

```java
private static final int REQUEST_CODE_ALBUM = 201;
private final List<String> pendingPhotoPaths = new ArrayList<>();
```

On button click:

```java
Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
startActivityForResult(intent, REQUEST_CODE_ALBUM);
```

In `onActivityResult`, copy image with `ImageUtils.copyImageToAppDirectory()` and store path in `pendingPhotoPaths`.

- [x] **Step 2: Save photos after day record**

After `addOrUpdateDayRecord(record)` succeeds, load record by trip/date and save photos:

```java
DayRecord savedRecord = dbHelper.getDayRecordByTripAndDate(tripId, selectedDate);
int dayRecordId = savedRecord == null ? -1 : savedRecord.getId();
for (String path : pendingPhotoPaths) {
    Photo photo = new Photo();
    photo.setTripId(tripId);
    photo.setDayRecordId(dayRecordId);
    photo.setImagePath(path);
    photo.setCreatedAt(selectedDate);
    dbHelper.addPhoto(photo);
}
```

- [x] **Step 3: Merge during trip buttons**

In `DuringTripActivity`, remove use of `btn_during_photo` and rename `btn_during_day_record` action text to `记录今日旅行`.

In XML, replace two diary/photo buttons with one button spanning the grid row if possible.

- [x] **Step 4: Merge TripDetail during action cards**

Replace the two cards `写旅行日记` and `添加旅行照片` with one card:

```java
addActionCard("记录今日旅行", "写下今天的故事、心情，并补充路上的照片。", () -> openWithTrip(DayRecordActivity.class));
```

Keep post-trip photo supplement route unchanged to avoid removing existing album management.

## Task 5: Plan-Linked Spot/Food Check-Ins

**Files:**
- Modify `app/src/main/java/com/example/travelmemory/activity/SpotsActivity.java`
- Modify `app/src/main/res/layout/activity_spots.xml`
- Modify `app/src/main/java/com/example/travelmemory/adapter/SpotAdapter.java`
- Modify `app/src/main/res/layout/item_spot.xml`

- [x] **Step 1: Add planned check-in section**

Add a horizontal/vertical container above manual form:

```xml
<LinearLayout
    android:id="@+id/layout_planned_checkins"
    android:layout_width="match_parent"
    android:layout_height="wrap_content"
    android:orientation="vertical" />
```

- [x] **Step 2: Render spot/food plans**

Load `dbHelper.getPrePlansForTripSorted(tripId)` and filter plan types matching existing Chinese labels for place/food. For each uncompleted or not-yet-linked item, add a small card with title, time/date, location, and button `完成打卡`.

- [x] **Step 3: Complete planned check-in**

On click, create `Spot`:

```java
spot.setTripId(tripId);
spot.setName(plan.getTitle());
spot.setType(planTypeToSpotType(plan.getType()));
spot.setNote(plan.getNote());
spot.setIsFavorite(0);
spot.setCreatedAt(plan.getTargetDate());
spot.setSourcePlanId(plan.getId());
dbHelper.addSpot(spot);
dbHelper.markPrePlanCompleted(plan.getId(), true);
```

Then reload planned cards and spot list.

- [x] **Step 4: Keep manual check-in**

When manual form saves a spot, explicitly set:

```java
spot.setSourcePlanId(0);
```

- [x] **Step 5: Show source in spot item**

Add a small text label:

```java
holder.tvSource.setText(spot.getSourcePlanId() > 0 ? "来自行前计划" : "计划外新增");
```

## Task 6: Verification

**Files:**
- No code edits expected unless verification reveals compile issues.

- [x] **Step 1: Run compile verification**

Run:

```powershell
.\gradlew.bat assembleDebug
```

Expected: `BUILD SUCCESSFUL`.

- [x] **Step 2: If compile fails, fix only directly related errors**

Allowed fixes:

- Missing imports from the changed files.
- XML ID mismatches from the changed layouts.
- Constructor mismatch caused by `PrePlan` or `Spot` field additions.

- [x] **Step 3: Final report**

Report:

- Modified files.
- How Search became the itinerary plan page.
- How PrePlan sorting works by Day/date/time.
- CTA change and preserved start-trip flow.
- Unified diary/photo save flow.
- Plan-linked check-in behavior.
- Map SDK intentionally not added.
- `assembleDebug` result.
