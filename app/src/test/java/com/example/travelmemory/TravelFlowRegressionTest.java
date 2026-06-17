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
        String postTripLayout = read("src/main/res/layout/activity_post_trip_record.xml");
        String planCompareLayout = read("src/main/res/layout/activity_plan_compare.xml");

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
    public void spotsExpenseSearchAndStatsUseUnifiedCopy() throws IOException {
        String spotsJava = read("src/main/java/com/example/travelmemory/activity/SpotsActivity.java");
        String expenseLayout = read("src/main/res/layout/activity_expense.xml");
        String searchLayout = read("src/main/res/layout/activity_search.xml");
        String statsLayout = read("src/main/res/layout/activity_statistics.xml");

        assertFalse(spotsJava.contains("餐厅"));
        assertFalse(spotsJava.contains("博物馆"));
        assertFalse(expenseLayout.contains("@string/expense_title"));
        assertFalse(searchLayout.contains("@string/search_hint"));
        assertFalse(statsLayout.contains("数据统计"));
    }

    @Test
    public void tripSelectionAndMemoryPagesUseFriendlyCopy() throws IOException {
        String selectLayout = read("src/main/res/layout/activity_select_trip.xml");
        String memoryListLayout = read("src/main/res/layout/activity_memory_list.xml");
        String memoryCardLayout = read("src/main/res/layout/activity_memory_card.xml");

        assertFalse(selectLayout.contains("还没有旅行，先添加一趟吧"));
        assertFalse(memoryListLayout.contains("暂无旅行记录"));
        assertFalse(memoryCardLayout.contains("景点："));
    }

    @Test
    public void profileAndQuestionnaireFlowIsRegistered() throws IOException {
        String manifest = read("src/main/AndroidManifest.xml");
        String settings = read("src/main/java/com/example/travelmemory/activity/SettingsActivity.java");
        String register = read("src/main/java/com/example/travelmemory/activity/RegisterActivity.java");
        String profileLayout = read("src/main/res/layout/activity_personal_profile.xml");
        String questionnaireLayout = read("src/main/res/layout/activity_profile_questionnaire.xml");

        org.junit.Assert.assertTrue(manifest.contains("PersonalProfileActivity"));
        org.junit.Assert.assertTrue(manifest.contains("ProfileQuestionnaireActivity"));
        org.junit.Assert.assertTrue(settings.contains("ProfileQuestionnaireActivity.class"));
        org.junit.Assert.assertTrue(register.contains("ProfileQuestionnaireActivity.class"));
        org.junit.Assert.assertTrue(profileLayout.contains("我的旅行画像"));
        org.junit.Assert.assertTrue(profileLayout.contains("旅行偏好"));
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
        String memoryCardJava = read("src/main/java/com/example/travelmemory/activity/MemoryCardActivity.java");
        String memoryCardLayout = read("src/main/res/layout/activity_memory_card.xml");
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
        org.junit.Assert.assertTrue(planCompareJava.contains("generateRegretsForTrip(tripId)"));
        org.junit.Assert.assertTrue(planCompareLayout.contains("自动汇总当前旅行未完成内容"));

        org.junit.Assert.assertTrue(memoryCardLayout.contains("回忆卡片"));
        org.junit.Assert.assertTrue(memoryCardLayout.contains("旅行相册"));
        org.junit.Assert.assertTrue(memoryCardLayout.contains("当前旅行还没有添加照片"));
        org.junit.Assert.assertTrue(memoryCardJava.contains("getPhotosForTrip(tripId)"));
        org.junit.Assert.assertTrue(memoryCardJava.contains("showAlbumTab"));

        assertFalse(settingsLayout.contains("row_profile"));
        assertFalse(settingsJava.contains("PersonalProfileActivity.class"));
        org.junit.Assert.assertTrue(settingsLayout.contains("设置头像"));
        org.junit.Assert.assertFalse(settingsLayout.contains("编辑资料"));
        org.junit.Assert.assertTrue(settingsAdapter.contains("修改昵称"));
        org.junit.Assert.assertTrue(settingsAdapter.contains("修改密码"));
        org.junit.Assert.assertTrue(settingsLayout.contains("tv_settings_age"));
        org.junit.Assert.assertTrue(settingsLayout.contains("tv_settings_gender"));
        org.junit.Assert.assertTrue(settingsLayout.contains("tv_settings_preference"));
        org.junit.Assert.assertTrue(settingsJava.contains("ProfileQuestionnaireActivity.class"));
    }

    @Test
    public void globalCopyAndEntryPointsDoNotRegress() throws IOException {
        String visibleCodeAndResources = read("src/main/java/com/example/travelmemory/activity/PhotoActivity.java")
                + read("src/main/java/com/example/travelmemory/activity/SpotsActivity.java")
                + read("src/main/res/layout/activity_pre_trip_plan.xml")
                + read("src/main/res/layout/activity_during_trip.xml")
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
        String duringTrip = read("src/main/java/com/example/travelmemory/activity/DuringTripActivity.java");
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
        String profile = read("src/main/res/layout/activity_personal_profile.xml");
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
        org.junit.Assert.assertTrue(profile.contains("我的旅行画像"));
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

    private String read(String path) throws IOException {
        return new String(Files.readAllBytes(Paths.get(path)), StandardCharsets.UTF_8);
    }
}
