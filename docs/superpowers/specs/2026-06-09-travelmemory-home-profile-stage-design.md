# TravelMemory Home Profile Stage Polish Design

## Goal

Continue the TravelMemory MVP iteration without MVVM refactoring. Keep the current Java + XML + Activity + Adapter + Model + DatabaseHelper structure, simplify the home/settings flow, add a lightweight user profile questionnaire, fix trip stage transition, and polish list item visuals.

## Scope

This iteration changes only the existing MVP surface and adds two small profile screens:

- Home page: keep welcome, search, add trip, trip cover cards, trip list, footprint summary, and bottom navigation.
- Settings page: keep account summary, personal profile entry, nickname/password account actions, and prominent logout.
- Profile: show avatar, nickname, age, gender, travel preference, trip count, and diary count.
- Questionnaire: collect nickname, age, gender, and travel preference after registration or from profile edit.
- Trip detail: allow during-trip records to move into post-trip整理 with confirmation.
- Photo flows: keep album selection only and avoid camera permission/launchers.
- Item layouts: unify card radius, spacing, tags, hierarchy, and clickable feedback.

Out of scope:

- MVVM refactor.
- Deleting core Activity files.
- Destructive file operations.
- Database table rebuilds or incompatible schema changes.
- New third-party UI frameworks.

## Architecture

The app remains Activity-driven. Existing Activities keep direct DatabaseHelper calls and XML view binding through `findViewById`.

User profile data uses the existing nullable `user` columns:

- `age INTEGER DEFAULT 0`
- `gender TEXT`
- `travel_preference TEXT`
- `profile_completed INTEGER DEFAULT 0`

Old users with missing or empty profile fields render defaults such as `旅行者`, `还没有填写旅行偏好`, and `0`.

Trip stage uses existing `trip.status` values:

- `pre_trip`
- `during_trip`
- `post_trip`
- `completed` or existing finished status where already present

The stage change is implemented as a small action in `TripDetailActivity` during-trip mode. Confirmation updates the trip status to `post_trip` and re-renders post-trip mode.

## Home Design

`MainActivity` becomes a focused dashboard:

- Top welcome and avatar/menu.
- Search box.
- Primary add-trip button.
- `我的旅行足迹` summary card.
- `我的旅行` RecyclerView with cover image cards.
- Empty state when no trips exist.
- Bottom navigation.

The home page removes:

- `今日旅行灵感`
- `待办提醒`
- `回忆精选`

The footprint summary computes safe values from DatabaseHelper:

- Trip count.
- Diary count.
- Photo count.
- Spot count.
- Total expense.
- Recent trip name when available.

If a count cannot be found, it displays `0` rather than crashing.

## Settings Design

`SettingsActivity` becomes a compact account page:

- Header card with avatar, nickname, username, and profile hint.
- `个人主页` row that opens `PersonalProfileActivity`.
- `修改昵称` and `修改密码` rows where current features exist.
- A prominent `退出登录` action with warning styling.

Logout always shows a confirmation dialog:

- Message: `确定要退出当前账号吗？`
- Buttons: `取消`, `退出登录`

Removed settings groups:

- 旅行偏好
- 数据管理
- 关于应用
- 未来功能
- Other placeholder rows without working behavior

## Profile And Questionnaire Design

`PersonalProfileActivity` reads current user and aggregate stats from DatabaseHelper.

It displays:

- Avatar.
- Nickname.
- Age.
- Gender.
- Travel preference.
- Recorded trip count.
- Diary count.

Empty values use friendly fallback text.

`ProfileQuestionnaireActivity` provides a small form:

- Nickname.
- Age.
- Gender.
- Travel preference.

It supports save and skip. Save calls `updateUserProfile`. Skip returns to the main app and leaves `profile_completed = 0`.

Registration navigates to the questionnaire after account creation. Existing users are not blocked by missing questionnaire data.

## Trip Stage Design

In `TripDetailActivity` during-trip mode:

- Add a secondary action: `结束旅行，进入整理`.
- On click, show confirmation dialog.
- On confirmation, call `updateTripStatus(tripId, TravelConstants.MODE_POST_TRIP)`.
- Set `mode = TravelConstants.MODE_POST_TRIP`.
- Reload current trip and render post-trip content.

Overview mode shows the current stage label and gives a post-trip hint when status is `post_trip`.

All main record entry points continue to use `TripDetailActivity` with `mode=during_trip`; `DuringTripActivity` remains in the project but is not used as the main flow entry.

## Visual Design

Use existing resource-driven colors:

- Primary: `#35D0C5` or `#2ECFCB`
- Primary dark: `#18B8B0`
- Accent: `#FF7B61`
- Background: `#F8FAFA` or `#F6FBFB`
- Card: `#FFFFFF`
- Main text: `#1F2328`
- Secondary text: `#7B8794`
- Border: `#E8F0F0`
- Primary light: `#E9FAF8`
- Warning light: `#FFF0EB`

Item layouts use:

- White card backgrounds.
- 16dp to 20dp rounded corners.
- 14dp to 18dp inner padding.
- 10dp to 14dp vertical spacing.
- Light elevation or stroke.
- Tags for stage/type/status.
- Ripple or selectable feedback where clickable.

## Verification

Run these checks after implementation:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat assembleDebug
rg -n "今日旅行灵感|待办提醒|回忆精选" app/src/main
rg -n "旅行偏好|数据管理|关于应用|未来功能" app/src/main/res/layout/activity_settings.xml app/src/main/java/com/example/travelmemory/activity/SettingsActivity.java
rg -n "android.permission.CAMERA|ACTION_IMAGE_CAPTURE|拍照|相机|btnCamera|takePhoto" app/src/main
rg -n "new Intent\([^\n]*DuringTripActivity\.class" app/src/main/java
```

Expected:

- Unit tests pass.
- Debug build succeeds.
- Removed home/settings modules no longer appear in their main UI surfaces.
- No real camera permission or camera launchers remain.
- No main-flow `DuringTripActivity` launch remains.
