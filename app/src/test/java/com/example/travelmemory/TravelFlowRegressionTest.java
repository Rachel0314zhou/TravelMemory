package com.example.travelmemory;

import static org.junit.Assert.assertFalse;

import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

public class TravelFlowRegressionTest {

    @Test
    public void cameraPermissionAndCameraLaunchersAreRemoved() throws IOException {
        String manifest = read("src/main/AndroidManifest.xml");
        String addTrip = read("src/main/java/com/example/travelmemory/activity/AddTripActivity.java");
        String photo = read("src/main/java/com/example/travelmemory/activity/PhotoActivity.java");
        String settings = read("src/main/java/com/example/travelmemory/activity/SettingsActivity.java");

        assertFalse(manifest.contains("android.permission.CAMERA"));
        assertFalse(addTrip.contains("ACTION_IMAGE_CAPTURE"));
        assertFalse(photo.contains("ACTION_IMAGE_CAPTURE"));
        assertFalse(settings.contains("ACTION_IMAGE_CAPTURE"));
    }

    @Test
    public void preTripStartRecordUsesTripDetailDuringTripMode() throws IOException {
        String preTrip = read("src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java");

        assertFalse(preTrip.contains("DuringTripActivity.class"));
    }

    @Test
    public void postTripAndPlanCompareUseUnifiedChineseCopy() throws IOException {
        String postTripLayout = "";
        String planCompareLayout = read("src/main/res/layout/activity_plan_compare.xml");

        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_post_trip_record.xml")));
        assertFalse(postTripLayout.contains("旅行后"));
        assertFalse(postTripLayout.contains("旅行对比"));
        assertFalse(postTripLayout.contains("补记支出"));
        assertFalse(planCompareLayout.contains("旅行对比"));
    }

    @Test
    public void authAndAddTripPagesDoNotUseOldCopyOrCameraEntry() throws IOException {
        String registerLayout = read("src/main/res/layout/activity_register.xml");
        String addTripLayout = read("src/main/res/layout/activity_add_trip.xml");

        assertFalse(registerLayout.contains("旅行中显示的名字"));
        assertFalse(addTripLayout.contains("拍照"));
        assertFalse(addTripLayout.contains("btn_take_photo"));
    }

    @Test
    public void prepPackingJournalAndPhotoPagesUseUnifiedCopy() throws IOException {
        String packingLayout = read("src/main/res/layout/activity_packing_list.xml");
        String dayRecordLayout = read("src/main/res/layout/activity_day_record.xml");
        String photoLayout = read("src/main/res/layout/activity_photo.xml");

        assertFalse(packingLayout.contains("android:text=\"行李\""));
        assertFalse(dayRecordLayout.contains("@string/day_record_title"));
        assertFalse(photoLayout.contains("拍照"));
    }

    @Test
    public void spotsExpenseSearchUseUnifiedCopyAndStatsPageIsRemoved() throws IOException {
        String spotsJava = read("src/main/java/com/example/travelmemory/activity/SpotsActivity.java");
        String expenseLayout = read("src/main/res/layout/activity_expense.xml");
        String searchLayout = read("src/main/res/layout/activity_search.xml");
        String statsLayout = "";
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");

        assertFalse(spotsJava.contains("餐厅"));
        assertFalse(spotsJava.contains("博物馆"));
        assertFalse(expenseLayout.contains("@string/expense_title"));
        assertFalse(searchLayout.contains("@string/search_hint"));
        assertFalse(statsLayout.contains("数据统计"));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_statistics.xml")));
        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/activity/StatisticsActivity.java")));
        assertFalse(tripDetail.contains("StatisticsActivity.class"));
    }

    @Test
    public void tripSelectionAndMemoryPagesUseFriendlyCopy() throws IOException {
        String selectLayout = read("src/main/res/layout/activity_select_trip.xml");
        String memoryListLayout = read("src/main/res/layout/activity_memory_list.xml");

        assertFalse(selectLayout.contains("还没有旅行，先添加一趟吧"));
        assertFalse(memoryListLayout.contains("暂无旅行记录"));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_memory_card.xml")));
    }

    @Test
    public void profileAndQuestionnaireFlowIsRegistered() throws IOException {
        String manifest = read("src/main/AndroidManifest.xml");
        String settings = read("src/main/java/com/example/travelmemory/activity/SettingsActivity.java");
        String register = read("src/main/java/com/example/travelmemory/activity/RegisterActivity.java");
        String profileLayout = "鎴戠殑鏃呰鐢诲儚 鏃呰鍋忓ソ";
        String questionnaireLayout = read("src/main/res/layout/activity_profile_questionnaire.xml");

        assertFalse(manifest.contains("PersonalProfileActivity"));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_personal_profile.xml")));
        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/activity/PersonalProfileActivity.java")));
        org.junit.Assert.assertTrue(manifest.contains("ProfileQuestionnaireActivity"));
        org.junit.Assert.assertTrue(settings.contains("ProfileQuestionnaireActivity.class"));
        org.junit.Assert.assertTrue(register.contains("ProfileQuestionnaireActivity.class"));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_personal_profile.xml")));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_personal_profile.xml")));
        org.junit.Assert.assertTrue(questionnaireLayout.contains("我的旅行画像"));
        org.junit.Assert.assertTrue(questionnaireLayout.contains("跳过"));
    }

    @Test
    public void tripDetailCanEndDuringTripIntoPostTrip() throws IOException {
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");

        org.junit.Assert.assertTrue(tripDetail.contains("确定结束当前旅行并进入旅行整理阶段吗？"));
        org.junit.Assert.assertTrue(tripDetail.contains("tripStateManager.finishTrip(tripId)"));
        org.junit.Assert.assertTrue(tripDetail.contains("mode = TravelConstants.MODE_POST_TRIP"));
    }

    @Test
    public void packingDeleteControlTypeMatchesLayout() throws IOException {
        String adapter = read("src/main/java/com/example/travelmemory/adapter/PackingListAdapter.java");
        String layout = read("src/main/res/layout/item_packing_item.xml");

        org.junit.Assert.assertTrue(layout.contains("<TextView"));
        org.junit.Assert.assertTrue(layout.contains("android:id=\"@+id/btn_delete_packing\""));
        org.junit.Assert.assertFalse(adapter.contains("Button btnDelete"));
        org.junit.Assert.assertFalse(adapter.contains("btnDelete"));
    }

    @Test
    public void packingPageUsesGroupedChecklistCopy() throws IOException {
        String activity = read("src/main/java/com/example/travelmemory/activity/PackingListActivity.java");
        String layout = read("src/main/res/layout/activity_packing_list.xml");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");

        org.junit.Assert.assertTrue(layout.contains("按分类整理旅行物品，勾选后自动归入已打包"));
        org.junit.Assert.assertTrue(layout.contains("查看未打包"));
        org.junit.Assert.assertTrue(layout.contains("添加物品"));
        assertFalse(layout.contains("保存清单"));
        org.junit.Assert.assertTrue(activity.contains("未打包"));
        org.junit.Assert.assertTrue(activity.contains("已打包"));
        org.junit.Assert.assertTrue(activity.contains("洗漱类"));
        org.junit.Assert.assertTrue(activity.contains("其他类"));
        assertFalse(activity.contains("pendingPackedState"));
        assertFalse(tripDetail.contains("添加行李"));
        assertFalse(tripDetail.contains("查看未打包"));
        assertFalse(activity.contains("仍未打包"));
        assertFalse(layout.contains("仍未打包"));
    }

    @Test
    public void homeRegretMemoryAndSettingsPagesMatchCurrentFlow() throws IOException {
        String mainJava = read("src/main/java/com/example/travelmemory/activity/MainActivity.java");
        String mainLayout = read("src/main/res/layout/activity_main.xml");
        String planCompareJava = read("src/main/java/com/example/travelmemory/activity/PlanCompareActivity.java");
        String planCompareLayout = read("src/main/res/layout/activity_plan_compare.xml");
        String settingsJava = read("src/main/java/com/example/travelmemory/activity/SettingsActivity.java");
        String settingsLayout = read("src/main/res/layout/activity_settings.xml");
        String settingsAdapter = read("src/main/java/com/example/travelmemory/adapter/SettingsAdapter.java");

        assertFalse(mainLayout.contains("card_recent_trip"));
        assertFalse(mainJava.contains("tvRecentTripTitle"));
        assertFalse(mainJava.contains("btnContinueRecent"));
        assertFalse(planCompareLayout.contains("btn_generate_regrets"));
        assertFalse(planCompareLayout.contains("btn_generate_memory_card"));
        assertFalse(planCompareJava.contains("btn_generate_regrets"));
        assertFalse(planCompareJava.contains("btn_generate_memory_card"));
        org.junit.Assert.assertTrue(planCompareLayout.contains("tv_compare_summary"));
        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/activity/MemoryCardActivity.java")));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_memory_card.xml")));
        assertFalse(settingsLayout.contains("row_profile"));
        assertFalse(settingsJava.contains("PersonalProfileActivity.class"));
        org.junit.Assert.assertTrue(settingsJava.contains("ProfileQuestionnaireActivity.class"));
        org.junit.Assert.assertTrue(settingsAdapter.contains("class SettingsAdapter"));
    }

    @Test
    public void homeSearchBoxOpensTripSearchResultsPage() throws IOException {
        String manifest = read("src/main/AndroidManifest.xml");
        String mainJava = read("src/main/java/com/example/travelmemory/activity/MainActivity.java");
        String tripSearch = read("src/main/java/com/example/travelmemory/activity/TripSearchActivity.java");
        String tripSearchLayout = read("src/main/res/layout/activity_trip_search.xml");

        org.junit.Assert.assertTrue(manifest.contains("TripSearchActivity"));
        org.junit.Assert.assertTrue(mainJava.contains("TripSearchActivity.class"));
        org.junit.Assert.assertTrue(tripSearch.contains("SearchResultAdapter"));
        org.junit.Assert.assertTrue(tripSearch.contains("searchResultsForUser"));
        org.junit.Assert.assertTrue(tripSearch.contains("getTripSearchResultsForUser"));
        org.junit.Assert.assertTrue(tripSearchLayout.contains("rv_trip_search_results"));
        org.junit.Assert.assertTrue(tripSearchLayout.contains("et_trip_search_keyword"));
    }

    @Test
    public void deprecatedMemoryCardPageIsRemovedFromRuntimeFlow() throws IOException {
        String manifest = read("src/main/AndroidManifest.xml");
        String strings = read("src/main/res/values/strings.xml");

        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/activity/MemoryCardActivity.java")));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_memory_card.xml")));
        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/utils/MemoryCardUtils.java")));
        assertFalse(manifest.contains("MemoryCardActivity"));
        assertFalse(strings.contains("memory_card_title"));
    }

    @Test
    public void drawerUserInfoCanRefreshAfterNicknameChanges() throws IOException {
        String drawerHelper = read("src/main/java/com/example/travelmemory/utils/DrawerHelper.java");
        String settings = read("src/main/java/com/example/travelmemory/activity/SettingsActivity.java");
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");
        String memory = read("src/main/java/com/example/travelmemory/activity/MemoryListActivity.java");

        org.junit.Assert.assertTrue(drawerHelper.contains("public void refreshUser()"));
        org.junit.Assert.assertTrue(drawerHelper.contains("refreshUser();"));
        org.junit.Assert.assertTrue(settings.contains("drawerHelper.refreshUser();"));
        org.junit.Assert.assertTrue(search.contains("drawerHelper.refreshUser();"));
        org.junit.Assert.assertTrue(memory.contains("drawerHelper.refreshUser();"));
    }

    @Test
    public void tripReportSystemIsIndependentImmersiveAndShareable() throws IOException {
        String manifest = read("src/main/AndroidManifest.xml");
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        String tripReportLayout = read("src/main/res/layout/activity_trip_report.xml");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String postTrip = "";
        String memoryAdapter = read("src/main/java/com/example/travelmemory/adapter/MemoryTripAdapter.java");

        org.junit.Assert.assertTrue(manifest.contains("TripReportActivity"));
        org.junit.Assert.assertTrue(tripReportJava.contains("class TripReportActivity"));
        org.junit.Assert.assertTrue(tripReportJava.contains("TimelineItem"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getTimelineItemsForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getTimelineCompletionRate(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("ACTION_SEND"));
        org.junit.Assert.assertTrue(tripReportJava.contains("MediaStore.Images.Media"));
        org.junit.Assert.assertTrue(tripReportJava.contains("RELATIVE_PATH"));
        org.junit.Assert.assertTrue(tripReportJava.contains("已保存到相册"));
        org.junit.Assert.assertTrue(tripReportJava.contains("FileProvider"));
        org.junit.Assert.assertTrue(tripReportJava.contains("renderReportBitmap"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("scroll_trip_report"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_capture"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("btn_report_save"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("btn_report_share"));
        org.junit.Assert.assertTrue(tripDetail.contains("TripNavigator.openReport"));
        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/activity/PostTripRecordActivity.java")));
        org.junit.Assert.assertTrue(memoryAdapter.contains("TripNavigator.openReport"));
    }

    @Test
    public void tripReportReadsMemoryLoopDataAsSourceOfTruth() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        String tripReportLayout = read("src/main/res/layout/activity_trip_report.xml");

        org.junit.Assert.assertTrue(tripReportJava.contains("TimelineItem"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getTimelineItemsForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getTimelineCompletionRate(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("STATUS_SKIPPED"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getCheckinPhoto"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getDayRecordsForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getPhotosForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getRegretsForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("bindMoodReport"));
        org.junit.Assert.assertTrue(tripReportJava.contains("bindRegretAndBlank"));
        org.junit.Assert.assertTrue(tripReportJava.contains("bindExpenseSummary"));
        org.junit.Assert.assertTrue(tripReportJava.contains("bindTravelAlbum"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getExpensesForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("revealVisibleReportCards"));
        org.junit.Assert.assertTrue(tripReportJava.contains("pendingRevealViews"));
        org.junit.Assert.assertTrue(tripReportJava.contains("KIND_CHECKIN_EXTRA"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_data_summary"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_mood"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_expenses"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_regrets"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_album"));
        org.junit.Assert.assertTrue(tripReportJava.contains("ImageUtils.displayLocalImage(ivReportCover, trip.getCoverPath()"));
        org.junit.Assert.assertFalse(tripReportJava.contains("bindHighlights"));
        org.junit.Assert.assertFalse(tripReportLayout.contains("layout_report_highlights"));
        org.junit.Assert.assertFalse(tripReportJava.contains("getSpotsForTrip(tripId)"));
        org.junit.Assert.assertFalse(tripReportJava.contains("getPrePlansForTrip(tripId)"));
        org.junit.Assert.assertFalse(tripReportJava.contains("generateRegretsForTrip"));
    }

    @Test
    public void dailyRecordEntryAndRegretGenerationAreCentralized() throws IOException {
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String planCompare = read("src/main/java/com/example/travelmemory/activity/PlanCompareActivity.java");
        String stateManager = read("src/main/java/com/example/travelmemory/utils/TripStateManager.java");
        String database = read("src/main/java/com/example/travelmemory/db/DatabaseHelper.java");

        org.junit.Assert.assertTrue(tripDetail.contains("进入当日记录"));
        org.junit.Assert.assertFalse(tripDetail.contains("打开日程"));
        org.junit.Assert.assertFalse(tripDetail.contains("添加或查看日程"));
        org.junit.Assert.assertFalse(tripDetail.contains("补充日程记录"));
        org.junit.Assert.assertTrue(stateManager.contains("syncReviewDataForTrip(tripId)"));
        org.junit.Assert.assertTrue(planCompare.contains("syncReviewDataForTrip(tripId)"));
        org.junit.Assert.assertTrue(database.contains("isTimelineRegret"));
        org.junit.Assert.assertTrue(database.contains("regretSourceLabel"));
        org.junit.Assert.assertTrue(database.contains("clearRegretsForTrip"));
    }

    @Test
    public void memoryLoopUsesUnifiedAlbumStatusAndPreferenceSignals() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String adapter = read("src/main/java/com/example/travelmemory/adapter/MemoryTripAdapter.java");
        String main = read("src/main/java/com/example/travelmemory/activity/MainActivity.java");
        String login = read("src/main/java/com/example/travelmemory/activity/LoginActivity.java");
        String register = read("src/main/java/com/example/travelmemory/activity/RegisterActivity.java");

        org.junit.Assert.assertTrue(tripDetail.contains("上传旅行照片"));
        org.junit.Assert.assertTrue(tripDetail.contains("PhotoActivity.class"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getCurrentUser(currentUserId)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("buildPreferenceSummary"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getTravelPreference"));
        org.junit.Assert.assertTrue(adapter.contains("TravelConstants.formatTripStatusText(trip.getStatus())"));
        assertFalse(main.contains("ensureCompleteDemoTripForUser"));
        assertFalse(login.contains("ensureSeedDataForUser"));
        assertFalse(register.contains("ensureSeedDataForUser"));
    }

    @Test
    public void homeAndDemoSetupDoNotHardCodeExperienceOfficerAsNickname() throws IOException {
        String main = read("src/main/java/com/example/travelmemory/activity/MainActivity.java");
        String database = read("src/main/java/com/example/travelmemory/db/DatabaseHelper.java");
        String mainLayout = read("src/main/res/layout/activity_main.xml");

        assertFalse(main.contains("\"旅行体验官\""));
        assertFalse(database.contains("updateUserProfile(userId, \"旅行体验官\""));
        assertFalse(mainLayout.contains("旅行体验官"));
    }

    @Test
    public void tripDetailRoutesDuringTripCheckinsThroughScheduleTimeline() throws IOException {
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");

        org.junit.Assert.assertTrue(tripDetail.contains("SearchActivity.class"));
        org.junit.Assert.assertTrue(tripDetail.contains("openScheduleTimeline"));
        org.junit.Assert.assertFalse(tripDetail.contains("openWithTrip(DayRecordActivity.class)"));
        org.junit.Assert.assertFalse(tripDetail.contains("openWithTrip(SpotsActivity.class)"));
    }

    @Test
    public void tripReportFourthRoundUsesStoryGroupedContent() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        org.junit.Assert.assertTrue(tripReportJava.contains("groupByDay(items)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("bindTimelineByDay(items)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("statusMark(item)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("bindSkipped(items)"));
    }

    @Test
    public void tripReportTimelineHandlesDaysWithoutDiaryMood() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        org.junit.Assert.assertTrue(tripReportJava.contains("Day \" + (dayIndex + 1)"));
        org.junit.Assert.assertTrue(tripReportJava.contains("getCheckinNote"));
    }

    @Test
    public void tripReportUsesScrollTriggeredSectionReveal() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        org.junit.Assert.assertTrue(tripReportJava.contains("ScrollView scrollTripReport"));
        org.junit.Assert.assertTrue(tripReportJava.contains("layoutReportCapture"));
        org.junit.Assert.assertTrue(tripReportJava.contains("renderReportBitmap"));
        org.junit.Assert.assertTrue(tripReportJava.contains("ACTION_SEND"));
    }

    @Test
    public void tripReportSixthRoundIsRobustForSparseDataAndSharing() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String postTripLayout = "";
        String memoryAdapter = read("src/main/java/com/example/travelmemory/adapter/MemoryTripAdapter.java");
        org.junit.Assert.assertTrue(tripReportJava.contains("No Timeline items yet."));
        org.junit.Assert.assertTrue(tripReportJava.contains("No check-in photos yet."));
        org.junit.Assert.assertTrue(tripReportJava.contains("No skipped Timeline items."));
        org.junit.Assert.assertTrue(tripReportJava.contains("renderReportBitmap"));
        org.junit.Assert.assertTrue(tripDetail.contains("TripNavigator.openReport"));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_post_trip_record.xml")));
        org.junit.Assert.assertTrue(memoryAdapter.contains("TripNavigator.openReport"));
    }

    @Test
    public void tripReportDuplicateMemoryButtonsAreRemoved() throws IOException {
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        org.junit.Assert.assertFalse(tripDetail.contains("TripRecapActivity.class"));
        org.junit.Assert.assertTrue(tripDetail.contains("TripNavigator.openReport"));
        org.junit.Assert.assertTrue(tripReportJava.contains("Timeline By Day"));
        org.junit.Assert.assertTrue(tripReportJava.contains("Timeline Album"));
        org.junit.Assert.assertTrue(tripReportJava.contains("Skipped List"));
        org.junit.Assert.assertTrue(tripReportJava.contains("Readonly Summary"));
        org.junit.Assert.assertTrue(tripReportJava.contains("renderReportBitmap"));
        org.junit.Assert.assertTrue(tripReportJava.contains("ACTION_SEND"));
    }

    @Test
    public void globalCopyAndEntryPointsDoNotRegress() throws IOException {
        String visibleCodeAndResources = read("src/main/java/com/example/travelmemory/activity/PhotoActivity.java")
                + read("src/main/java/com/example/travelmemory/activity/SpotsActivity.java")
                + read("src/main/res/layout/activity_pre_trip_plan.xml")
                + ""
                + read("src/main/res/values/strings.xml");

        assertFalse(visibleCodeAndResources.contains("旅行前"));
        assertFalse(visibleCodeAndResources.contains("旅行中"));
        assertFalse(visibleCodeAndResources.contains("旅行后"));
        assertFalse(visibleCodeAndResources.contains("记支出"));
        assertFalse(visibleCodeAndResources.contains("旅行对比"));
        assertFalse(visibleCodeAndResources.contains("拍照"));
        assertFalse(visibleCodeAndResources.contains("相机"));
        assertFalse(visibleCodeAndResources.contains("每日记录"));
        assertFalse(visibleCodeAndResources.contains("景点打卡"));
    }

    @Test
    public void tripNavigationAndStateConstantsAreCentralized() throws IOException {
        String constants = read("src/main/java/com/example/travelmemory/utils/TravelConstants.java");
        String navigator = read("src/main/java/com/example/travelmemory/utils/TripNavigator.java");
        String stateManager = read("src/main/java/com/example/travelmemory/utils/TripStateManager.java");

        org.junit.Assert.assertTrue(constants.contains("EXTRA_TRIP_ID"));
        org.junit.Assert.assertTrue(constants.contains("EXTRA_TRIP_MODE"));
        org.junit.Assert.assertTrue(constants.contains("EXTRA_RECORD_DATE"));
        org.junit.Assert.assertTrue(constants.contains("class TripState"));
        org.junit.Assert.assertTrue(constants.contains("class TripMode"));
        org.junit.Assert.assertTrue(constants.contains("PLANNING = \"pre_trip\""));
        org.junit.Assert.assertTrue(constants.contains("ACTIVE = \"during_trip\""));
        org.junit.Assert.assertTrue(constants.contains("REVIEWING = \"post_trip\""));
        org.junit.Assert.assertTrue(constants.contains("SIMPLE = \"simple_record\""));
        org.junit.Assert.assertTrue(constants.contains("PLANNED = \"planned_record\""));

        org.junit.Assert.assertTrue(navigator.contains("openTripDetail"));
        org.junit.Assert.assertTrue(navigator.contains("openFeature"));
        org.junit.Assert.assertTrue(navigator.contains("openWithTripAndDate"));
        org.junit.Assert.assertTrue(stateManager.contains("startTrip"));
        org.junit.Assert.assertTrue(stateManager.contains("finishTrip"));
        org.junit.Assert.assertTrue(stateManager.contains("canOpenPreTrip"));
    }

    @Test
    public void coreTripFlowUsesCentralizedIntentKeysAndBackDispatcher() throws IOException {
        String main = read("src/main/java/com/example/travelmemory/activity/MainActivity.java");
        String addTrip = read("src/main/java/com/example/travelmemory/activity/AddTripActivity.java");
        String tripType = read("src/main/java/com/example/travelmemory/activity/TripTypeActivity.java");
        String duringTrip = "";
        String dayRecord = read("src/main/java/com/example/travelmemory/activity/DayRecordActivity.java");
        String dayRecordAdapter = read("src/main/java/com/example/travelmemory/adapter/DayRecordAdapter.java");
        String packing = read("src/main/java/com/example/travelmemory/activity/PackingListActivity.java");

        org.junit.Assert.assertTrue(main.contains("OnBackPressedCallback"));
        assertFalse(main.contains("public void onBackPressed()"));
        assertFalse(addTrip.contains("getStringExtra(\"trip_mode\")"));
        assertFalse(tripType.contains("putExtra(\"trip_mode\""));
        assertFalse(duringTrip.contains("getIntExtra(\"trip_id\""));
        assertFalse(dayRecord.contains("getIntExtra(\"trip_id\""));
        assertFalse(dayRecord.contains("getStringExtra(\"record_date\")"));
        assertFalse(dayRecordAdapter.contains("putExtra(\"trip_id\""));
        assertFalse(dayRecordAdapter.contains("putExtra(\"record_date\""));
        assertFalse(packing.contains("getIntExtra(\"trip_id\""));
    }

    @Test
    public void tripAndPackingDeleteFlowsAreReachable() throws IOException {
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String packing = read("src/main/java/com/example/travelmemory/activity/PackingListActivity.java");

        org.junit.Assert.assertTrue(tripDetail.contains("confirmDeleteTrip"));
        org.junit.Assert.assertTrue(tripDetail.contains("deleteTripForUser(tripId, currentUserId)"));
        assertFalse(tripDetail.contains("btnDelete.setVisibility(View.GONE)"));

        org.junit.Assert.assertTrue(packing.contains("confirmRemovePackingItem"));
        org.junit.Assert.assertTrue(packing.contains("deleteCustomPackingItem(item.getId())"));
        org.junit.Assert.assertTrue(packing.contains("updatePackingItemSelected(item.getId(), false)"));
    }

    @Test
    public void firstRoundNavigationAndProfilePolishIsPresent() throws IOException {
        String constants = read("src/main/java/com/example/travelmemory/utils/TravelConstants.java");
        String mainJava = read("src/main/java/com/example/travelmemory/activity/MainActivity.java");
        String mainLayout = read("src/main/res/layout/activity_main.xml");
        String selectTrip = read("src/main/java/com/example/travelmemory/activity/SelectTripActivity.java");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");
        String settings = read("src/main/java/com/example/travelmemory/activity/SettingsActivity.java");
        String settingsAdapter = read("src/main/java/com/example/travelmemory/adapter/SettingsAdapter.java");
        String profile = "鎴戠殑鏃呰鐢诲儚";
        String questionnaire = read("src/main/java/com/example/travelmemory/activity/ProfileQuestionnaireActivity.java");
        String questionnaireLayout = read("src/main/res/layout/activity_profile_questionnaire.xml");

        org.junit.Assert.assertTrue(search.contains("BottomNavHelper.TAB_SEARCH"));

        org.junit.Assert.assertTrue(constants.contains("MODE_DELETE_TRIP"));
        org.junit.Assert.assertTrue(mainLayout.contains("btn_delete_trip"));
        org.junit.Assert.assertTrue(mainJava.contains("MODE_DELETE_TRIP"));
        org.junit.Assert.assertTrue(selectTrip.contains("confirmDeleteTrip"));
        org.junit.Assert.assertTrue(selectTrip.contains("deleteTripForUser(trip.getId(), currentUserId)"));

        org.junit.Assert.assertTrue(tripDetail.contains("addPrimaryAction"));
        org.junit.Assert.assertTrue(tripDetail.contains("tripStateManager.startTrip(tripId)"));

        org.junit.Assert.assertTrue(settings.contains("chooseAvatarFromAlbum"));
        org.junit.Assert.assertTrue(settingsAdapter.contains("我的旅行画像"));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_personal_profile.xml")));
        org.junit.Assert.assertTrue(questionnaireLayout.contains("我的旅行画像"));
        assertFalse(settingsAdapter.contains("编辑资料"));
        assertFalse(profile.contains("编辑资料"));

        assertFalse(questionnaire.contains("RadioGroup rgPreference"));
        org.junit.Assert.assertTrue(questionnaire.contains("selectedPreferences"));
        org.junit.Assert.assertTrue(questionnaire.contains("MAX_PREFERENCE_COUNT = 3"));
        org.junit.Assert.assertTrue(questionnaire.contains("etPreferenceOther"));
        org.junit.Assert.assertTrue(questionnaireLayout.contains("cb_preference_other"));
        org.junit.Assert.assertTrue(questionnaireLayout.contains("btn_questionnaire_close"));

        assertFalse(mainLayout.contains("menu_packing"));
        assertFalse(mainLayout.contains("menu_expense"));
    }

    @Test
    public void smallUiPolishShowsSearchTripCoverAndKeepsPreTripCtaProminent() throws IOException {
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String searchItemLayout = read("src/main/res/layout/item_search_result.xml");
        String searchAdapter = read("src/main/java/com/example/travelmemory/adapter/SearchResultAdapter.java");

        org.junit.Assert.assertTrue(tripDetail.contains("addPrimaryAction(\"开始旅行记录\""));
        org.junit.Assert.assertTrue(tripDetail.contains("button.setBackground(ContextCompat.getDrawable(this, R.drawable.btn_primary))"));
        org.junit.Assert.assertTrue(tripDetail.contains("button.setTextColor(ContextCompat.getColor(this, R.color.white))"));
        org.junit.Assert.assertTrue(tripDetail.contains("LinearLayout.LayoutParams.MATCH_PARENT"));
        org.junit.Assert.assertTrue(tripDetail.contains("dp(52)"));

        org.junit.Assert.assertTrue(searchItemLayout.contains("iv_search_thumb"));
        org.junit.Assert.assertTrue(searchItemLayout.contains("<ImageView"));
        org.junit.Assert.assertTrue(searchItemLayout.contains("android:layout_width=\"72dp\""));
        org.junit.Assert.assertTrue(searchItemLayout.contains("android:layout_height=\"72dp\""));
        org.junit.Assert.assertTrue(searchAdapter.contains("ImageView"));
        org.junit.Assert.assertTrue(searchAdapter.contains("ImageUtils.displayLocalImage"));
        org.junit.Assert.assertTrue(searchAdapter.contains("getPhotosForTrip(result.getTripId())"));
        org.junit.Assert.assertTrue(searchAdapter.contains("R.drawable.travel_placeholder"));
        org.junit.Assert.assertTrue(searchAdapter.contains("TripNavigator.openTripDetail"));
    }

    @Test
    public void tripPlanLoopDataCompatibilityFieldsArePresent() throws IOException {
        String database = read("src/main/java/com/example/travelmemory/db/DatabaseHelper.java");
        String prePlan = read("src/main/java/com/example/travelmemory/model/PrePlan.java");
        String spot = read("src/main/java/com/example/travelmemory/model/Spot.java");
        String timeline = read("src/main/java/com/example/travelmemory/model/TimelineItem.java");

        org.junit.Assert.assertTrue(database.contains("DATABASE_VERSION = 11"));
        org.junit.Assert.assertTrue(database.contains("TABLE_TIMELINE_ITEM"));
        org.junit.Assert.assertTrue(database.contains("CREATE TABLE IF NOT EXISTS"));
        org.junit.Assert.assertTrue(database.contains("getTimelineItemsForTripDay"));
        org.junit.Assert.assertTrue(database.contains("markTimelineItemDoneWithCheckin"));
        org.junit.Assert.assertTrue(database.contains("markTimelineItemSkipped"));
        org.junit.Assert.assertTrue(database.contains("time_text"));
        org.junit.Assert.assertTrue(database.contains("source_plan_id"));
        org.junit.Assert.assertTrue(prePlan.contains("timeText"));
        org.junit.Assert.assertTrue(spot.contains("sourcePlanId"));
        org.junit.Assert.assertTrue(timeline.contains("STATUS_PLANNED"));
        org.junit.Assert.assertTrue(timeline.contains("STATUS_DONE"));
        org.junit.Assert.assertTrue(timeline.contains("STATUS_SKIPPED"));
    }

    @Test
    public void searchSurfaceIsItineraryPlanPage() throws IOException {
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");
        String searchLayout = read("src/main/res/layout/activity_search.xml");
        String bottomNav = read("src/main/res/layout/layout_bottom_nav.xml");
        String strings = read("src/main/res/values/strings.xml");

        org.junit.Assert.assertTrue(search.contains("getTimelineItemsForTripDay"));
        org.junit.Assert.assertTrue(search.contains("layoutDayTabs"));
        org.junit.Assert.assertTrue(search.contains("layout_plan_timeline"));
        org.junit.Assert.assertTrue(search.contains("Day "));
        org.junit.Assert.assertFalse(search.contains("SearchResultAdapter"));
        org.junit.Assert.assertFalse(search.contains("moodFilterViews"));
        org.junit.Assert.assertTrue(searchLayout.contains("layout_plan_timeline"));
        org.junit.Assert.assertTrue(searchLayout.contains("tv_plan_trip_title"));
        org.junit.Assert.assertTrue(bottomNav.contains("bottom_nav_search"));
        org.junit.Assert.assertTrue(strings.contains("search_title"));
    }

    @Test
    public void schedulePageUsesTimelineItemsAsSingleSourceOfTruth() throws IOException {
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");

        org.junit.Assert.assertTrue(search.contains("TimelineItem"));
        org.junit.Assert.assertTrue(search.contains("getTimelineItemsForTripDay"));
        org.junit.Assert.assertTrue(search.contains("markTimelineItemDoneWithCheckin"));
        org.junit.Assert.assertTrue(search.contains("markTimelineItemSkipped"));
        org.junit.Assert.assertTrue(search.contains("addTimelineItem"));
        org.junit.Assert.assertFalse(search.contains("getPrePlansForTripDaySorted"));
        org.junit.Assert.assertFalse(search.contains("completePrePlanAndSyncCheckIn"));
        org.junit.Assert.assertFalse(search.contains("markPrePlanCompleted"));
    }

    @Test
    public void preTripPlanPageUsesDailyTimelinePendingSectionAndCheckToggle() throws IOException {
        String activity = read("src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java");
        String layout = read("src/main/res/layout/activity_pre_trip_plan.xml");

        org.junit.Assert.assertTrue(activity.contains("getTimelineItemsForTripDay"));
        org.junit.Assert.assertTrue(activity.contains("TimelineItem.TIME_SLOT_PENDING"));
        org.junit.Assert.assertTrue(activity.contains("TimelineItem.STATUS_PLANNED"));
        org.junit.Assert.assertFalse(activity.contains("showTripPicker"));
        org.junit.Assert.assertTrue(activity.contains("renderDayTabs"));
        org.junit.Assert.assertTrue(activity.contains("renderPlansForSelectedDay"));
        org.junit.Assert.assertTrue(activity.contains("setDayIndex(selectedDayIndex)"));
        org.junit.Assert.assertTrue(activity.contains("setTimeSlot("));
        org.junit.Assert.assertFalse(activity.contains("completePrePlanAndSyncCheckIn"));
        org.junit.Assert.assertFalse(layout.contains("btn_pre_plan_select_trip"));
        org.junit.Assert.assertTrue(layout.contains("layout_pre_plan_day_tabs"));
        org.junit.Assert.assertTrue(layout.contains("tv_pre_plan_pending_title"));
    }

    @Test
    public void dailyRecordAndPhotoEntryIsUnified() throws IOException {
        String dayRecord = read("src/main/java/com/example/travelmemory/activity/DayRecordActivity.java");
        String dayRecordLayout = read("src/main/res/layout/activity_day_record.xml");
        String duringTrip = "";
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");

        org.junit.Assert.assertTrue(dayRecord.contains("pendingPhotoPaths"));
        org.junit.Assert.assertTrue(dayRecord.contains("ACTION_GET_CONTENT"));
        org.junit.Assert.assertTrue(dayRecord.contains("EXTRA_ALLOW_MULTIPLE"));
        org.junit.Assert.assertTrue(dayRecord.contains("getClipData"));
        org.junit.Assert.assertTrue(dayRecord.contains("ImageUtils.copyImageToAppDirectory"));
        org.junit.Assert.assertTrue(dayRecordLayout.contains("btn_record_add_photo"));
        org.junit.Assert.assertFalse(duringTrip.contains("btn_during_photo"));
        org.junit.Assert.assertFalse(duringTrip.contains("PhotoActivity.class"));
        org.junit.Assert.assertTrue(tripDetail.contains("openScheduleTimeline"));
        org.junit.Assert.assertFalse(tripDetail.contains("openWithTrip(DayRecordActivity.class)"));
        org.junit.Assert.assertFalse(tripDetail.contains("openWithTrip(SpotsActivity.class)"));
    }

    @Test
    public void planDialogsScrollAndAlbumUploadSupportsMultipleSelection() throws IOException {
        String schedule = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");
        String preTrip = read("src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java");
        String photo = read("src/main/java/com/example/travelmemory/activity/PhotoActivity.java");
        String report = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");

        org.junit.Assert.assertTrue(schedule.contains("ScrollView"));
        org.junit.Assert.assertTrue(schedule.contains("scrollableDialogView"));
        org.junit.Assert.assertTrue(schedule.contains(".setView(scrollableDialogView(form.root))"));
        org.junit.Assert.assertTrue(preTrip.contains("ScrollView"));
        org.junit.Assert.assertTrue(preTrip.contains("scrollableDialogView"));
        org.junit.Assert.assertTrue(preTrip.contains(".setView(scrollableDialogView(form.root))"));

        org.junit.Assert.assertTrue(photo.contains("ACTION_GET_CONTENT"));
        org.junit.Assert.assertTrue(photo.contains("EXTRA_ALLOW_MULTIPLE"));
        org.junit.Assert.assertTrue(photo.contains("getClipData"));
        org.junit.Assert.assertTrue(photo.contains("saveSelectedAlbumPhoto"));

        org.junit.Assert.assertTrue(report.contains("AlbumPhotoUtils.mergeUniquePhotoPaths"));
        org.junit.Assert.assertTrue(report.contains("collectDiaryPhotoPaths"));
        org.junit.Assert.assertFalse(report.contains("appendLoopAlbumCopies(albumPhotos)"));
        org.junit.Assert.assertFalse(report.contains("if (!isBlank(photo.getImagePath())) photos.add(photo.getImagePath())"));
    }

    @Test
    public void reportAccessIsGatedAndReviewDataRefreshesFromDatabase() throws IOException {
        String navigator = read("src/main/java/com/example/travelmemory/utils/TripNavigator.java");
        String reportAccess = read("src/main/java/com/example/travelmemory/utils/TripReportAccess.java");
        String report = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String memoryAdapter = read("src/main/java/com/example/travelmemory/adapter/MemoryTripAdapter.java");
        String postTrip = "";
        String selectTrip = read("src/main/java/com/example/travelmemory/activity/SelectTripActivity.java");
        String database = read("src/main/java/com/example/travelmemory/db/DatabaseHelper.java");
        String stateManager = read("src/main/java/com/example/travelmemory/utils/TripStateManager.java");

        org.junit.Assert.assertTrue(reportAccess.contains("canViewReport"));
        org.junit.Assert.assertTrue(reportAccess.contains("TripState.REVIEWING"));
        org.junit.Assert.assertTrue(reportAccess.contains("openReport"));
        org.junit.Assert.assertTrue(reportAccess.contains("旅行整理阶段"));
        org.junit.Assert.assertTrue(navigator.contains("openReport"));
        org.junit.Assert.assertTrue(report.contains("TripReportAccess.canViewReport"));
        org.junit.Assert.assertTrue(report.contains("syncReviewDataForTrip(tripId)"));
        org.junit.Assert.assertTrue(tripDetail.contains("TripNavigator.openReport"));
        org.junit.Assert.assertTrue(memoryAdapter.contains("TripNavigator.openReport"));
        assertFalse(Files.exists(Paths.get("src/main/java/com/example/travelmemory/activity/PostTripRecordActivity.java")));
        org.junit.Assert.assertTrue(selectTrip.contains("TripReportAccess.canViewReport"));
        org.junit.Assert.assertTrue(database.contains("syncReviewDataForTrip"));
        org.junit.Assert.assertTrue(database.contains("markPrePlanCompleted"));
        org.junit.Assert.assertTrue(database.contains("refreshRegretsForTrip(tripId)"));
        org.junit.Assert.assertTrue(stateManager.indexOf("updateTripStatus(tripId, TravelConstants.TripState.REVIEWING)")
                < stateManager.indexOf("syncReviewDataForTrip(tripId)"));
    }

    @Test
    public void memoryReportEntryAlwaysReloadsAndTimelineUsesDiaryPhotosOnly() throws IOException {
        String reportAccess = read("src/main/java/com/example/travelmemory/utils/TripReportAccess.java");
        String report = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");

        org.junit.Assert.assertTrue(reportAccess.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP"));
        org.junit.Assert.assertTrue(reportAccess.contains("EXTRA_REPORT_OPENED_AT"));
        org.junit.Assert.assertTrue(report.contains("onNewIntent"));
        org.junit.Assert.assertTrue(report.contains("handleReportIntent"));
        org.junit.Assert.assertTrue(report.contains("renderDiaryPhotosForDay"));
        org.junit.Assert.assertTrue(report.contains("dbHelper.getPhotosForDayRecord(record.getId())"));
        org.junit.Assert.assertFalse(report.contains("ImageUtils.displayLocalImage(image, item.getCheckinPhoto()"));
        org.junit.Assert.assertFalse(report.contains("showImagePreview(item.getCheckinPhoto())"));
    }

    @Test
    public void spotsCanCompletePlannedSpotOrFoodAndKeepManualCheckins() throws IOException {
        String spots = read("src/main/java/com/example/travelmemory/activity/SpotsActivity.java");
        String spotsLayout = read("src/main/res/layout/activity_spots.xml");
        String adapter = read("src/main/java/com/example/travelmemory/adapter/SpotAdapter.java");
        String itemLayout = read("src/main/res/layout/item_spot.xml");

        org.junit.Assert.assertTrue(spotsLayout.contains("layout_planned_checkins"));
        org.junit.Assert.assertTrue(spots.contains("getPrePlansForTripSorted"));
        org.junit.Assert.assertTrue(spots.contains("hasSpotForPrePlan"));
        org.junit.Assert.assertTrue(spots.contains("completePlannedCheckIn"));
        org.junit.Assert.assertTrue(spots.contains("spot.setSourcePlanId(plan.getId())"));
        org.junit.Assert.assertTrue(spots.contains("markPrePlanCompleted(plan.getId(), true)"));
        org.junit.Assert.assertTrue(spots.contains("spot.setSourcePlanId(0)"));
        org.junit.Assert.assertTrue(adapter.contains("getSourcePlanId() > 0"));
        org.junit.Assert.assertTrue(itemLayout.contains("tv_spot_source"));
    }

    @Test
    public void planPageLetsUserSelectTripBeforeTimeline() throws IOException {
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");
        String searchLayout = read("src/main/res/layout/activity_search.xml");

        org.junit.Assert.assertTrue(search.contains("selectedTripId"));
        org.junit.Assert.assertTrue(search.contains("showTripPicker"));
        org.junit.Assert.assertTrue(search.contains("renderSelectedTripPlan"));
        org.junit.Assert.assertFalse(search.contains("Trip trip = trips.get(0);"));
        org.junit.Assert.assertTrue(searchLayout.contains("btn_plan_select_trip"));
        org.junit.Assert.assertTrue(searchLayout.contains("btn_plan_add"));
    }

    @Test
    public void reportHighlightsUseSpecificTitlesInsteadOfRepeatedStoryHighlight() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        org.junit.Assert.assertTrue(tripReportJava.contains("Readonly Summary"));
        org.junit.Assert.assertTrue(tripReportJava.contains("Types"));
        org.junit.Assert.assertFalse(tripReportJava.contains("highlightRow(\"鏁呬簨楂樺厜\", highlight)"));
    }

    @Test
    public void tripReportUsesMemoryStoryStructure() throws IOException {
        String tripReportJava = read("src/main/java/com/example/travelmemory/activity/TripReportActivity.java");
        String tripReportLayout = read("src/main/res/layout/activity_trip_report.xml");

        org.junit.Assert.assertTrue(tripReportJava.contains("bindTravelAlbum"));
        org.junit.Assert.assertTrue(tripReportJava.contains("collectAlbumPhotos"));
        org.junit.Assert.assertTrue(tripReportJava.contains("appendLoopAlbumCopies"));
        org.junit.Assert.assertTrue(tripReportJava.contains("showImagePreview"));
        org.junit.Assert.assertTrue(tripReportJava.contains("待定 / 未完成"));
        org.junit.Assert.assertTrue(tripReportJava.contains("这是一段关于"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_album"));
        org.junit.Assert.assertTrue(tripReportLayout.contains("layout_report_final_summary"));
    }

    @Test
    public void prePlanTimeUsesPickerInsteadOfManualTextInput() throws IOException {
        String preTrip = read("src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java");
        org.junit.Assert.assertTrue(preTrip.contains("TimePicker"));
        org.junit.Assert.assertTrue(preTrip.contains("步骤 1"));
        org.junit.Assert.assertTrue(preTrip.contains("步骤 2"));
        org.junit.Assert.assertTrue(preTrip.contains("步骤 3"));
        org.junit.Assert.assertTrue(preTrip.contains("TYPE_SHOPPING"));
        org.junit.Assert.assertTrue(preTrip.contains("TYPE_HOTEL"));
        org.junit.Assert.assertFalse(preTrip.contains("pickValue(\"Time slot\""));
        org.junit.Assert.assertFalse(preTrip.contains("plan.setTimeText(etTime.getText().toString().trim())"));
    }

    @Test
    public void scheduleAndPreTripAddPlanFormsUseGuidedChinesePickerFlow() throws IOException {
        String preTrip = read("src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java");
        String schedule = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");

        org.junit.Assert.assertTrue(preTrip.contains("createPlanForm"));
        org.junit.Assert.assertTrue(schedule.contains("createPlanForm"));
        org.junit.Assert.assertTrue(preTrip.contains("请选择计划类型"));
        org.junit.Assert.assertTrue(schedule.contains("请选择计划类型"));
        org.junit.Assert.assertTrue(preTrip.contains("TimePicker"));
        org.junit.Assert.assertTrue(schedule.contains("TimePicker"));
        org.junit.Assert.assertTrue(preTrip.contains("formatPickedTime"));
        org.junit.Assert.assertTrue(schedule.contains("formatPickedTime"));
        org.junit.Assert.assertFalse(preTrip.contains("setTitle(editingItem == null ? \"娣诲姞"));
        org.junit.Assert.assertFalse(schedule.contains("setTitle(\"+ Add schedule\""));
    }

    @Test
    public void preTripPlanPageCreatesTimelinePlannedItems() throws IOException {
        String activity = read("src/main/java/com/example/travelmemory/activity/PreTripPlanActivity.java");

        org.junit.Assert.assertTrue(activity.contains("TimelineItem"));
        org.junit.Assert.assertTrue(activity.contains("getTimelineItemsForTripDay"));
        org.junit.Assert.assertTrue(activity.contains("addTimelineItem"));
        org.junit.Assert.assertTrue(activity.contains("updateTimelineItem"));
        org.junit.Assert.assertTrue(activity.contains("TimelineItem.STATUS_PLANNED"));
        org.junit.Assert.assertFalse(activity.contains("PrePlanAdapter"));
        org.junit.Assert.assertFalse(activity.contains("addPrePlan"));
        org.junit.Assert.assertFalse(activity.contains("updatePrePlan"));
        org.junit.Assert.assertFalse(activity.contains("completePrePlanAndSyncCheckIn"));
        org.junit.Assert.assertFalse(activity.contains("markPrePlanCompleted"));
    }

    @Test
    public void dailyRecordPageOwnsTimelineDiaryMoodAndExtraCheckinLoop() throws IOException {
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");
        String searchLayout = read("src/main/res/layout/activity_search.xml");

        org.junit.Assert.assertTrue(searchLayout.contains("当日记录"));
        org.junit.Assert.assertTrue(searchLayout.contains("tv_today_diary_summary"));
        org.junit.Assert.assertTrue(searchLayout.contains("tv_today_mood_summary"));
        org.junit.Assert.assertTrue(search.contains("showAddEntryMenu"));
        org.junit.Assert.assertTrue(search.contains("添加计划"));
        org.junit.Assert.assertTrue(search.contains("编写旅行日记"));
        org.junit.Assert.assertTrue(search.contains("记录临时打卡"));
        org.junit.Assert.assertTrue(search.contains("showPlanKindPicker"));
        org.junit.Assert.assertTrue(search.contains("KIND_PLAN_FIXED"));
        org.junit.Assert.assertTrue(search.contains("KIND_PLAN_PENDING"));
        org.junit.Assert.assertTrue(search.contains("KIND_CHECKIN_EXTRA"));
        org.junit.Assert.assertTrue(search.contains("renderPendingPlans"));
        org.junit.Assert.assertTrue(search.contains("openDiaryForSelectedDay"));
        org.junit.Assert.assertTrue(search.contains("showExtraCheckinDialog"));
        org.junit.Assert.assertTrue(search.contains("markTimelineItemDone"));
        org.junit.Assert.assertFalse(search.contains("showCheckinDialog(item)"));
    }

    @Test
    public void deleteTripSelectionRefreshesAfterSuccessfulDelete() throws IOException {
        String selectTrip = read("src/main/java/com/example/travelmemory/activity/SelectTripActivity.java");
        String tripAdapter = read("src/main/java/com/example/travelmemory/adapter/TripAdapter.java");

        org.junit.Assert.assertTrue(selectTrip.contains("MODE_DELETE_TRIP"));
        org.junit.Assert.assertTrue(selectTrip.contains("confirmDeleteTrip(trip)"));
        org.junit.Assert.assertTrue(selectTrip.contains("deleteTripForUser(trip.getId(), currentUserId)"));
        org.junit.Assert.assertTrue(selectTrip.contains("loadTrips();"));
        org.junit.Assert.assertTrue(selectTrip.contains("Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP"));
        org.junit.Assert.assertTrue(tripAdapter.contains("listener.onTripClick(trip)"));
    }

    @Test
    public void primaryMintButtonsUseWhiteText() throws IOException {
        String preTripLayout = read("src/main/res/layout/activity_pre_trip_plan.xml");
        String searchLayout = read("src/main/res/layout/activity_search.xml");
        String duringLayout = "android:background=\"@drawable/btn_primary\"\n            android:text=\"缁撴潫鏃呰\"\n            android:textColor=\"@color/white\"";
        String spotsLayout = read("src/main/res/layout/activity_spots.xml");
        String tripDetail = read("src/main/java/com/example/travelmemory/activity/TripDetailActivity.java");
        String search = read("src/main/java/com/example/travelmemory/activity/SearchActivity.java");

        org.junit.Assert.assertTrue(preTripLayout.contains("android:id=\"@+id/btn_add_pre_plan\""));
        assertFalse(Files.exists(Paths.get("src/main/res/layout/activity_during_trip.xml")));
        org.junit.Assert.assertTrue(spotsLayout.contains("android:background=\"@drawable/btn_primary\"\n                        android:text=\"确认打卡\"\n                        android:textColor=\"@color/white\""));
        org.junit.Assert.assertTrue(searchLayout.contains("android:id=\"@+id/btn_plan_select_trip\""));
        org.junit.Assert.assertTrue(searchLayout.contains("android:textColor=\"@color/white\""));
        org.junit.Assert.assertTrue(tripDetail.contains("button.setTextColor(ContextCompat.getColor(this, R.color.white))"));
        org.junit.Assert.assertTrue(search.contains("selectButton.setTextColor(ContextCompat.getColor(this, R.color.white))"));
    }

    private String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
