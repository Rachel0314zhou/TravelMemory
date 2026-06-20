package com.example.travelmemory.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Expense;
import com.example.travelmemory.model.PackingItem;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.SearchResult;
import com.example.travelmemory.model.Spot;
import com.example.travelmemory.model.TimelineItem;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.model.User;
import com.example.travelmemory.utils.TravelConstants;

import java.util.ArrayList;
import java.io.File;
import java.io.FileOutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DEFAULT_NICKNAME = "\u65c5\u884c\u8005";
    private static final String DATABASE_NAME = "travel_memory.db";
    private static final int DATABASE_VERSION = 11;

    public static final String TABLE_USER = "user";
    public static final String TABLE_TRIP = "trip";
    public static final String TABLE_DAY_RECORD = "day_record";
    public static final String TABLE_PHOTO = "photo";
    public static final String TABLE_SPOT = "spot";
    public static final String TABLE_EXPENSE = "expense";
    public static final String TABLE_PRE_PLAN = "pre_plan";
    public static final String TABLE_PACKING_ITEM = "packing_item";
    public static final String TABLE_REGRET_ITEM = "regret_item";
    public static final String TABLE_TIMELINE_ITEM = "timeline_item";
    private final Context appContext;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        appContext = context.getApplicationContext();
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_USER + " (id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE, password TEXT, nickname TEXT, avatar_path TEXT, age INTEGER DEFAULT 0, gender TEXT, travel_preference TEXT, profile_completed INTEGER DEFAULT 0, created_at TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_TRIP + " (id INTEGER PRIMARY KEY AUTOINCREMENT, user_id INTEGER, name TEXT, destination TEXT, start_date TEXT, end_date TEXT, cover_path TEXT, mode TEXT DEFAULT 'simple_record', status TEXT DEFAULT 'during_trip', created_at TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_DAY_RECORD + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, date TEXT, note TEXT, mood TEXT, created_at TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_PHOTO + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, day_record_id INTEGER, image_path TEXT, created_at TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_SPOT + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, name TEXT, type TEXT, note TEXT, is_favorite INTEGER, source_plan_id INTEGER DEFAULT 0, created_at TEXT)");
        db.execSQL("CREATE TABLE " + TABLE_EXPENSE + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, category TEXT, amount REAL, note TEXT, date TEXT)");
        createTravelLoopTables(db);
        createTimelineTables(db);
    }

    private void createTravelLoopTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PRE_PLAN + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, day_index INTEGER DEFAULT 0, type TEXT, title TEXT, location TEXT, target_date TEXT, time_text TEXT, time_section TEXT DEFAULT '待定', note TEXT, is_completed INTEGER DEFAULT 0, status TEXT DEFAULT 'normal', created_at TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PACKING_ITEM + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, category TEXT, item_name TEXT, is_selected INTEGER DEFAULT 1, is_packed INTEGER DEFAULT 0, is_custom INTEGER DEFAULT 0, created_at TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REGRET_ITEM + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, pre_plan_id INTEGER, title TEXT, type TEXT, reason TEXT, created_at TEXT)");
    }

    private void createTimelineTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_TIMELINE_ITEM + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, day_index INTEGER DEFAULT 0, title TEXT, type TEXT DEFAULT '其他', time_slot TEXT DEFAULT '待定', status TEXT DEFAULT 'planned', kind TEXT DEFAULT 'plan_fixed', checkin_photo TEXT, checkin_note TEXT, checkin_mood TEXT, checkin_time TEXT, created_at TEXT)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 2) {
            addColumnIfMissing(db, TABLE_USER, "username", "TEXT");
            addColumnIfMissing(db, TABLE_USER, "password", "TEXT");
        }
        if (oldVersion < 3) {
            addColumnIfMissing(db, TABLE_TRIP, "user_id", "INTEGER DEFAULT 0");
        }
        if (oldVersion < 4) {
            createTravelLoopTables(db);
        }
        if (oldVersion < 5) {
            addColumnIfMissing(db, TABLE_TRIP, "mode", "TEXT DEFAULT 'simple_record'");
            addColumnIfMissing(db, TABLE_TRIP, "status", "TEXT DEFAULT 'during_trip'");
            addColumnIfMissing(db, TABLE_PRE_PLAN, "location", "TEXT");
            addColumnIfMissing(db, TABLE_PACKING_ITEM, "is_selected", "INTEGER DEFAULT 1");
            addColumnIfMissing(db, TABLE_PACKING_ITEM, "is_packed", "INTEGER DEFAULT 0");
            if (columnExists(db, TABLE_PACKING_ITEM, "is_checked")) {
                db.execSQL("UPDATE " + TABLE_PACKING_ITEM + " SET is_packed=is_checked WHERE is_packed=0");
            }
        }
        if (oldVersion < 6) {
            addColumnIfMissing(db, TABLE_USER, "age", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_USER, "gender", "TEXT");
            addColumnIfMissing(db, TABLE_USER, "travel_preference", "TEXT");
            addColumnIfMissing(db, TABLE_USER, "profile_completed", "INTEGER DEFAULT 0");
        }
        if (oldVersion < 7) {
            addColumnIfMissing(db, TABLE_PRE_PLAN, "time_text", "TEXT");
            addColumnIfMissing(db, TABLE_SPOT, "source_plan_id", "INTEGER DEFAULT 0");
        }
        if (oldVersion < 8) {
            addColumnIfMissing(db, TABLE_PRE_PLAN, "status", "TEXT DEFAULT 'normal'");
        }
        if (oldVersion < 9) {
            addColumnIfMissing(db, TABLE_PRE_PLAN, "day_index", "INTEGER DEFAULT 0");
            addColumnIfMissing(db, TABLE_PRE_PLAN, "time_section", "TEXT DEFAULT '待定'");
            backfillPrePlanDayIndexes(db);
        }
        if (oldVersion < 10) {
            createTimelineTables(db);
        }
        if (oldVersion < 11) {
            addColumnIfMissing(db, TABLE_TIMELINE_ITEM, "kind", "TEXT DEFAULT 'plan_fixed'");
        }
    }

    private boolean columnExists(SQLiteDatabase db, String tableName, String columnName) {
        Cursor cursor = db.rawQuery("PRAGMA table_info(" + tableName + ")", null);
        boolean exists = false;
        if (cursor != null) {
            while (cursor.moveToNext()) {
                if (columnName.equals(cursor.getString(cursor.getColumnIndexOrThrow("name")))) {
                    exists = true;
                    break;
                }
            }
            cursor.close();
        }
        return exists;
    }

    private void addColumnIfMissing(SQLiteDatabase db, String tableName, String columnName, String type) {
        if (!columnExists(db, tableName, columnName)) {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + type);
        }
    }

    private String nowDate() {
        return "2026-06-09";
    }

    public void ensureSeedDataForUser(int userId) {
        // Fixed initial demo trips were removed. New users start without injected travel data.
    }

    public boolean hasTripsForUser(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_TRIP + " WHERE user_id=?", String.valueOf(userId)) > 0;
    }

    public int ensureCompleteDemoTripForUser(int userId) {
        if (userId <= 0) return -1;
        ensureDemoProfileDefaults(userId);
        SQLiteDatabase db = getWritableDatabase();
        int tripId = findDemoTripId(userId);
        if (tripId <= 0) {
            ContentValues tripValues = new ContentValues();
            tripValues.put("user_id", userId);
            tripValues.put("name", "厦门之旅");
            tripValues.put("destination", "厦门");
            tripValues.put("start_date", "2026-07-01");
            tripValues.put("end_date", "2026-07-04");
            tripValues.put("cover_path", createDemoImage("demo_xiamen_cover.png", "厦门之旅", "#2E7D7A", "#F8D8A8"));
            tripValues.put("mode", TravelConstants.TripMode.PLANNED);
            tripValues.put("status", TravelConstants.TripState.REVIEWING);
            tripValues.put("created_at", "2026-07-01");
            tripId = (int) db.insert(TABLE_TRIP, null, tripValues);
        } else {
            ContentValues values = new ContentValues();
            values.put("destination", "厦门");
            values.put("start_date", "2026-07-01");
            values.put("end_date", "2026-07-04");
            values.put("mode", TravelConstants.TripMode.PLANNED);
            values.put("status", TravelConstants.TripState.REVIEWING);
            db.update(TABLE_TRIP, values, "id=? AND user_id=?", new String[]{String.valueOf(tripId), String.valueOf(userId)});
            resetTripMemoryData(db, tripId);
        }
        if (tripId > 0) {
            insertCompleteDemoData(db, tripId);
            generateRegretsForTrip(tripId);
        }
        return tripId;
    }

    private void ensureDemoProfileDefaults(int userId) {
        User user = getCurrentUser(userId);
        if (user == null) return;

        ContentValues values = new ContentValues();
        if (user.getAge() <= 0) {
            values.put("age", 24);
        }
        if (isBlank(user.getGender())) {
            values.put("gender", "未填写");
        }
        if (isBlank(user.getTravelPreference())) {
            values.put("travel_preference", "美食探店、自然风光、拍照记录");
        }
        if (user.getProfileCompleted() == 0) {
            values.put("profile_completed", 1);
        }
        if (values.size() > 0) {
            getWritableDatabase().update(TABLE_USER, values, "id=?", new String[]{String.valueOf(userId)});
        }
    }

    private int findDemoTripId(int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_TRIP, new String[]{"id"},
                "user_id=? AND name=?", new String[]{String.valueOf(userId), "厦门之旅"},
                null, null, "id DESC", "1");
        int id = -1;
        if (cursor != null) {
            if (cursor.moveToFirst()) id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
        }
        return id;
    }

    private void resetTripMemoryData(SQLiteDatabase db, int tripId) {
        db.delete(TABLE_DAY_RECORD, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PHOTO, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_SPOT, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_EXPENSE, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PRE_PLAN, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PACKING_ITEM, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_TIMELINE_ITEM, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_REGRET_ITEM, "trip_id=?", new String[]{String.valueOf(tripId)});
    }

    private void insertCompleteDemoData(SQLiteDatabase db, int tripId) {
        insertDemoPrePlan(db, tripId, 0, "景点", "沙坡尾傍晚散步", "沙坡尾", "2026-07-01", "18:30", TimelineItem.KIND_PLAN_FIXED, 1);
        insertDemoPrePlan(db, tripId, 1, "景点", "鼓浪屿半日游", "鼓浪屿", "2026-07-02", "09:00", TimelineItem.KIND_PLAN_FIXED, 1);
        insertDemoPrePlan(db, tripId, 1, "美食", "曾厝垵海鲜小吃", "曾厝垵", "2026-07-02", "19:00", TimelineItem.KIND_PLAN_FIXED, 1);
        insertDemoPrePlan(db, tripId, 2, "购物", "买一张厦门明信片", "中山路文创店", "2026-07-03", "待定", TimelineItem.KIND_PLAN_PENDING, 0);
        insertDemoPrePlan(db, tripId, 3, "美食", "再吃一次土笋冻", "八市", "2026-07-04", "待定", TimelineItem.KIND_PLAN_PENDING, 0);

        insertSeedPackingItem(db, tripId, "证件类", "身份证", 1, 1);
        insertSeedPackingItem(db, tripId, "数码类", "手机充电器", 1, 1);
        insertSeedPackingItem(db, tripId, "数码类", "相机备用电池", 1, 1);
        insertSeedPackingItem(db, tripId, "衣物类", "换洗衣物", 1, 1);
        insertSeedPackingItem(db, tripId, "洗漱类", "防晒霜", 1, 1);
        insertSeedPackingItem(db, tripId, "出行类", "折叠伞", 1, 0);
        insertSeedPackingItem(db, tripId, "其他类", "明信片邮票", 1, 0);

        int day1 = insertDemoDayRecord(db, tripId, "2026-07-01", "抵达厦门后先去了沙坡尾，傍晚的海风和街边灯光都很舒服。", "开心");
        int day2 = insertDemoDayRecord(db, tripId, "2026-07-02", "鼓浪屿比想象中更适合慢慢走，红砖洋房、转角小店和海边照片都很值得回看。", "兴奋");
        int day3 = insertDemoDayRecord(db, tripId, "2026-07-03", "上午去了南普陀，下午沿环岛路骑行，今天的节奏更放松。", "平静");
        int day4 = insertDemoDayRecord(db, tripId, "2026-07-04", "最后一天在八市吃了早餐，没来得及再去买明信片，有一点遗憾。", "遗憾");

        insertDemoPhoto(db, tripId, day1, "demo_xiamen_shapowei.png", "沙坡尾", "#315C72", "#F6C177", "2026-07-01");
        insertDemoPhoto(db, tripId, day2, "demo_xiamen_gulangyu.png", "鼓浪屿", "#3B7A57", "#F2D0A4", "2026-07-02");
        insertDemoPhoto(db, tripId, day2, "demo_xiamen_sea.png", "海边照片", "#2D6CDF", "#F9E2AF", "2026-07-02");
        insertDemoPhoto(db, tripId, day3, "demo_xiamen_road.png", "环岛路", "#3A6B35", "#B7E4C7", "2026-07-03");
        insertDemoPhoto(db, tripId, 0, "demo_xiamen_album.png", "旅行相册", "#7D4E57", "#F4A261", "2026-07-03");

        insertDemoTimelineItem(db, tripId, 0, "09:30", TimelineItem.TYPE_TRANSPORT, "高崎机场到酒店", TimelineItem.KIND_PLAN_FIXED, TimelineItem.STATUS_DONE, "", "顺利到达，放下行李后开始第一天。", "开心", "2026-07-01 09:30");
        insertDemoTimelineItem(db, tripId, 0, "18:30", TimelineItem.TYPE_SPOT, "沙坡尾傍晚散步", TimelineItem.KIND_PLAN_FIXED, TimelineItem.STATUS_DONE, createDemoImage("demo_timeline_shapowei.png", "沙坡尾", "#264653", "#E9C46A"), "傍晚的街区很适合散步和拍照。", "开心", "2026-07-01 18:30");
        insertDemoTimelineItem(db, tripId, 1, "09:00", TimelineItem.TYPE_SPOT, "鼓浪屿半日游", TimelineItem.KIND_PLAN_FIXED, TimelineItem.STATUS_DONE, createDemoImage("demo_timeline_gulangyu.png", "鼓浪屿", "#2A9D8F", "#F4A261"), "红砖洋房、海边小路和钢琴声都很有记忆点。", "兴奋", "2026-07-02 09:00");
        insertDemoTimelineItem(db, tripId, 1, "19:00", TimelineItem.TYPE_FOOD, "曾厝垵海鲜小吃", TimelineItem.KIND_PLAN_FIXED, TimelineItem.STATUS_DONE, "", "海蛎煎和沙茶面都很适合写进回忆。", "满足", "2026-07-02 19:00");
        insertDemoTimelineItem(db, tripId, 2, "16:20", TimelineItem.TYPE_SPOT, "环岛路临时看海", TimelineItem.KIND_CHECKIN_EXTRA, TimelineItem.STATUS_DONE, createDemoImage("demo_timeline_extra.png", "临时看海", "#1D3557", "#A8DADC"), "原本没有安排，结果成了最舒服的一段。", "平静", "2026-07-03 16:20");
        insertDemoTimelineItem(db, tripId, 2, TimelineItem.TIME_SLOT_PENDING, TimelineItem.TYPE_SHOPPING, "买一张厦门明信片", TimelineItem.KIND_PLAN_PENDING, TimelineItem.STATUS_PLANNED, "", "", "", "");
        insertDemoTimelineItem(db, tripId, 3, TimelineItem.TIME_SLOT_PENDING, TimelineItem.TYPE_FOOD, "再吃一次土笋冻", TimelineItem.KIND_PLAN_PENDING, TimelineItem.STATUS_PLANNED, "", "", "", "");

        insertSeedExpense(db, tripId, "餐饮", 238.5, "沙茶面、海蛎煎和小吃", "2026-07-02");
        insertSeedExpense(db, tripId, "交通", 126.0, "机场接驳、轮渡和公交", "2026-07-01");
        insertSeedExpense(db, tripId, "住宿", 880.0, "曾厝垵附近民宿两晚", "2026-07-01");
        insertSeedExpense(db, tripId, "门票", 90.0, "鼓浪屿景点联票", "2026-07-02");
        insertSeedExpense(db, tripId, "购物", 156.0, "伴手礼和冰箱贴", "2026-07-03");
        insertSeedExpense(db, tripId, "餐饮", 78.0, "八市早餐", "2026-07-04");
    }

    private void insertSeedDataForUser(SQLiteDatabase db, int userId) {
        ContentValues tripValues = new ContentValues();
        tripValues.put("user_id", userId);
        tripValues.put("name", "毕业前的厦门之旅");
        tripValues.put("destination", "厦门");
        tripValues.put("start_date", "2026-06-01");
        tripValues.put("end_date", "2026-06-04");
        tripValues.put("cover_path", "");
        tripValues.put("mode", "planned_record");
        tripValues.put("status", "post_trip");
        tripValues.put("created_at", "2026-06-01");
        long tripId = db.insert(TABLE_TRIP, null, tripValues);
        if (tripId == -1) return;

        insertSeedPrePlan(db, tripId, "想去的地方", "鼓浪屿", "鼓浪屿", "2026-06-02", "厦门最想去的一站", 1);
        insertSeedPrePlan(db, tripId, "想去的地方", "沙坡尾", "沙坡尾", "2026-06-01", "傍晚去散步", 1);
        insertSeedPrePlan(db, tripId, "想吃的美食", "土笋冻", "", "", "试试本地特色味道", 0);
        insertSeedPrePlan(db, tripId, "想做的事情", "在海边看一次日落", "海边", "2026-06-03", "安静坐到天色暗下来", 1);
        insertSeedPrePlan(db, tripId, "想买的东西", "一张厦门明信片", "文创店", "", "寄给毕业后的自己", 0);

        insertSeedPackingItem(db, tripId, "证件类", "身份证", 1, 1);
        insertSeedPackingItem(db, tripId, "数码类", "充电器", 1, 0);
        insertSeedPackingItem(db, tripId, "衣物类", "换洗衣物", 1, 1);
        insertSeedPackingItem(db, tripId, "洗漱类", "防晒霜", 1, 0);

        insertSeedDayRecord(db, tripId, "2026-06-01", "第一天到达厦门，海风很舒服，傍晚去了沙坡尾。", "开心");
        insertSeedDayRecord(db, tripId, "2026-06-02", "今天去了鼓浪屿，拍了很多照片，也吃到了很喜欢的小吃。", "兴奋");
        insertSeedDayRecord(db, tripId, "2026-06-03", "沿着海边慢慢散步，感觉时间都变慢了。", "平静");

        insertSeedSpot(db, tripId, "鼓浪屿", "景点", "红砖洋房和海景很适合散步。", 1, "2026-06-02");
        insertSeedSpot(db, tripId, "沙坡尾", "景点", "文艺街区，傍晚很适合散步。", 0, "2026-06-01");
        insertSeedSpot(db, tripId, "曾厝垵", "餐厅", "小吃选择很多。", 0, "2026-06-02");

        insertSeedExpense(db, tripId, "餐饮", 168.5, "沙坡尾小吃", "2026-06-01");
        insertSeedExpense(db, tripId, "交通", 86.0, "打车和轮渡", "2026-06-02");
        insertSeedExpense(db, tripId, "住宿", 520.0, "海景民宿", "2026-06-01");
        insertSeedExpense(db, tripId, "门票", 80.0, "景区门票", "2026-06-02");

        generateRegretsForTrip((int) tripId);

        ContentValues xiamenTripValues = new ContentValues();
        xiamenTripValues.put("user_id", userId);
        xiamenTripValues.put("name", "厦门之旅");
        xiamenTripValues.put("destination", "厦门");
        xiamenTripValues.put("start_date", "2026-07-01");
        xiamenTripValues.put("end_date", "2026-07-04");
        xiamenTripValues.put("cover_path", "");
        xiamenTripValues.put("mode", "planned_record");
        xiamenTripValues.put("status", "during_trip");
        xiamenTripValues.put("created_at", "2026-07-01");
        long xiamenTripId = db.insert(TABLE_TRIP, null, xiamenTripValues);
        if (xiamenTripId == -1) return;

        insertSeedTimelineItem(db, xiamenTripId, 0, TimelineItem.TIME_SLOT_MORNING, TimelineItem.TYPE_SPOT, "鼓浪屿游览", "2026-07-01");
        insertSeedTimelineItem(db, xiamenTripId, 0, TimelineItem.TIME_SLOT_AFTERNOON, TimelineItem.TYPE_SPOT, "中山路步行街", "2026-07-01");
        insertSeedTimelineItem(db, xiamenTripId, 0, TimelineItem.TIME_SLOT_EVENING, TimelineItem.TYPE_TRANSPORT, "环岛路骑行", "2026-07-01");
        insertSeedTimelineItem(db, xiamenTripId, 1, TimelineItem.TIME_SLOT_MORNING, TimelineItem.TYPE_SPOT, "厦门大学", "2026-07-02");
        insertSeedTimelineItem(db, xiamenTripId, 1, TimelineItem.TIME_SLOT_AFTERNOON, TimelineItem.TYPE_SPOT, "南普陀寺", "2026-07-02");
        insertSeedTimelineItem(db, xiamenTripId, 1, TimelineItem.TIME_SLOT_EVENING, TimelineItem.TYPE_FOOD, "曾厝垵美食", "2026-07-02");
    }

    private void insertSeedDayRecord(SQLiteDatabase db, long tripId, String date, String note, String mood) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("date", date);
        values.put("note", note);
        values.put("mood", mood);
        values.put("created_at", date);
        db.insert(TABLE_DAY_RECORD, null, values);
    }

    private void insertSeedSpot(SQLiteDatabase db, long tripId, String name, String type, String note, int isFavorite, String createdAt) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("name", name);
        values.put("type", type);
        values.put("note", note);
        values.put("is_favorite", isFavorite);
        values.put("created_at", createdAt);
        db.insert(TABLE_SPOT, null, values);
    }

    private void insertSeedExpense(SQLiteDatabase db, long tripId, String category, double amount, String note, String date) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("category", category);
        values.put("amount", amount);
        values.put("note", note);
        values.put("date", date);
        db.insert(TABLE_EXPENSE, null, values);
    }

    private void insertSeedPrePlan(SQLiteDatabase db, long tripId, String type, String title, String location, String targetDate, String note, int completed) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("type", type);
        values.put("title", title);
        values.put("location", location);
        values.put("target_date", targetDate);
        values.put("note", note);
        values.put("is_completed", completed);
        values.put("created_at", "2026-06-01");
        db.insert(TABLE_PRE_PLAN, null, values);
    }

    private void insertSeedPackingItem(SQLiteDatabase db, long tripId, String category, String itemName, int selected, int packed) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("category", category);
        values.put("item_name", itemName);
        values.put("is_selected", selected);
        values.put("is_packed", packed);
        values.put("is_custom", 0);
        values.put("created_at", "2026-06-01");
        db.insert(TABLE_PACKING_ITEM, null, values);
    }

    private void insertSeedTimelineItem(SQLiteDatabase db, long tripId, int dayIndex, String timeSlot, String type, String title, String createdAt) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("day_index", Math.max(0, dayIndex));
        values.put("title", title);
        values.put("type", type);
        values.put("time_slot", timeSlot);
        values.put("status", TimelineItem.STATUS_PLANNED);
        values.put("checkin_photo", "");
        values.put("checkin_note", "");
        values.put("checkin_mood", "");
        values.put("checkin_time", "");
        values.put("created_at", createdAt);
        db.insert(TABLE_TIMELINE_ITEM, null, values);
    }

    private void insertDemoPrePlan(SQLiteDatabase db, int tripId, int dayIndex, String type, String title,
                                   String location, String targetDate, String timeText, String kind, int completed) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("day_index", dayIndex);
        values.put("type", type);
        values.put("title", title);
        values.put("location", location);
        values.put("target_date", targetDate);
        values.put("time_text", timeText);
        values.put("time_section", TimelineItem.KIND_PLAN_PENDING.equals(kind) ? "待定" : "具体计划");
        values.put("note", TimelineItem.KIND_PLAN_PENDING.equals(kind) ? "旅行中待定安排" : "行前准备计划");
        values.put("is_completed", completed);
        values.put("status", completed == 1 ? "done" : "normal");
        values.put("created_at", "2026-07-01");
        db.insert(TABLE_PRE_PLAN, null, values);
    }

    private int insertDemoDayRecord(SQLiteDatabase db, int tripId, String date, String note, String mood) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("date", date);
        values.put("note", note);
        values.put("mood", mood);
        values.put("created_at", date);
        return (int) db.insert(TABLE_DAY_RECORD, null, values);
    }

    private void insertDemoPhoto(SQLiteDatabase db, int tripId, int dayRecordId, String filename,
                                 String label, String startColor, String endColor, String createdAt) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("day_record_id", dayRecordId);
        values.put("image_path", createDemoImage(filename, label, startColor, endColor));
        values.put("created_at", createdAt);
        db.insert(TABLE_PHOTO, null, values);
    }

    private void insertDemoTimelineItem(SQLiteDatabase db, int tripId, int dayIndex, String timeSlot, String type,
                                        String title, String kind, String status, String photo, String note,
                                        String mood, String checkinTime) {
        ContentValues values = new ContentValues();
        values.put("trip_id", tripId);
        values.put("day_index", dayIndex);
        values.put("title", title);
        values.put("type", type);
        values.put("time_slot", timeSlot);
        values.put("status", status);
        values.put("kind", kind);
        values.put("checkin_photo", photo);
        values.put("checkin_note", note);
        values.put("checkin_mood", mood);
        values.put("checkin_time", checkinTime);
        values.put("created_at", "2026-07-0" + Math.min(dayIndex + 1, 4));
        db.insert(TABLE_TIMELINE_ITEM, null, values);
    }

    private String createDemoImage(String filename, String label, String startColor, String endColor) {
        try {
            File dir = new File(appContext.getFilesDir(), "demo");
            if (!dir.exists() && !dir.mkdirs()) return "";
            File file = new File(dir, filename);
            if (file.exists()) return file.getAbsolutePath();

            int width = 960;
            int height = 640;
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
            paint.setColor(Color.parseColor(startColor));
            canvas.drawRect(0, 0, width, height, paint);
            paint.setColor(Color.parseColor(endColor));
            canvas.drawCircle(width * 0.78f, height * 0.25f, 220f, paint);
            paint.setColor(Color.argb(180, 255, 255, 255));
            canvas.drawRoundRect(72, 390, 888, 560, 34, 34, paint);
            paint.setColor(Color.rgb(45, 65, 78));
            paint.setTextSize(64);
            paint.setTypeface(android.graphics.Typeface.create(android.graphics.Typeface.DEFAULT, android.graphics.Typeface.BOLD));
            canvas.drawText(label, 120, 490, paint);
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, out);
            out.flush();
            out.close();
            bitmap.recycle();
            return file.getAbsolutePath();
        } catch (Exception ignored) {
            return "";
        }
    }

    public User getUser() {
        return readSingleUser(getReadableDatabase().query(TABLE_USER, null, null, null, null, null, "id DESC", "1"));
    }

    public User getUserById(int id) {
        return readSingleUser(getReadableDatabase().query(TABLE_USER, null, "id=?", new String[]{String.valueOf(id)}, null, null, null));
    }

    public User getCurrentUser(int userId) {
        return getUserById(userId);
    }

    public boolean isUsernameExists(String username) {
        Cursor cursor = getReadableDatabase().query(TABLE_USER, new String[]{"id"}, "username=?", new String[]{username}, null, null, null, "1");
        boolean exists = false;
        if (cursor != null) {
            exists = cursor.moveToFirst();
            cursor.close();
        }
        return exists;
    }

    public long registerUser(String username, String password, String nickname) {
        ContentValues values = new ContentValues();
        values.put("username", username);
        values.put("password", password);
        values.put("nickname", nickname);
        values.put("avatar_path", "");
        values.put("age", 0);
        values.put("gender", "");
        values.put("travel_preference", "");
        values.put("profile_completed", 0);
        values.put("created_at", nowDate());
        return getWritableDatabase().insert(TABLE_USER, null, values);
    }

    public User login(String username, String password) {
        return readSingleUser(getReadableDatabase().query(TABLE_USER, null, "username=? AND password=?", new String[]{username, password}, null, null, null, "1"));
    }

    private User readSingleUser(Cursor cursor) {
        User user = null;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                user = new User(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        getOptionalString(cursor, "username"),
                        getOptionalString(cursor, "password"),
                        getOptionalString(cursor, "nickname"),
                        getOptionalString(cursor, "avatar_path"),
                        getOptionalInt(cursor, "age"),
                        getOptionalString(cursor, "gender"),
                        getOptionalString(cursor, "travel_preference"),
                        getOptionalInt(cursor, "profile_completed"),
                        getOptionalString(cursor, "created_at"));
            }
            cursor.close();
        }
        return user;
    }

    private String getOptionalString(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getString(index) : "";
    }

    private int getOptionalInt(Cursor cursor, String columnName) {
        int index = cursor.getColumnIndex(columnName);
        return index >= 0 ? cursor.getInt(index) : 0;
    }

    public int updateNickname(int userId, String nickname) {
        ContentValues values = new ContentValues();
        values.put("nickname", blankToDefault(nickname, DEFAULT_NICKNAME));
        return getWritableDatabase().update(TABLE_USER, values, "id=?", new String[]{String.valueOf(userId)});
    }

    public int saveNicknameForUser(int userId, String nickname) {
        if (userId <= 0) return 0;
        return updateNickname(userId, nickname);
    }

    public int updatePassword(int userId, String newPassword) {
        ContentValues values = new ContentValues();
        values.put("password", newPassword);
        return getWritableDatabase().update(TABLE_USER, values, "id=?", new String[]{String.valueOf(userId)});
    }

    public int updateAvatarPath(int userId, String avatarPath) {
        ContentValues values = new ContentValues();
        values.put("avatar_path", avatarPath);
        return getWritableDatabase().update(TABLE_USER, values, "id=?", new String[]{String.valueOf(userId)});
    }

    public int updateUserProfile(int userId, String nickname, int age, String gender, String travelPreference, boolean completed) {
        ContentValues values = new ContentValues();
        values.put("nickname", blankToDefault(nickname, DEFAULT_NICKNAME));
        values.put("age", Math.max(age, 0));
        values.put("gender", gender == null ? "" : gender);
        values.put("travel_preference", travelPreference == null ? "" : travelPreference);
        values.put("profile_completed", completed ? 1 : 0);
        return getWritableDatabase().update(TABLE_USER, values, "id=?", new String[]{String.valueOf(userId)});
    }

    public boolean isUserProfileCompleted(int userId) {
        User user = getCurrentUser(userId);
        return user != null && user.getProfileCompleted() == 1;
    }

    public boolean checkPassword(int userId, String oldPassword) {
        Cursor cursor = getReadableDatabase().query(TABLE_USER, new String[]{"id"}, "id=? AND password=?", new String[]{String.valueOf(userId), oldPassword}, null, null, null, "1");
        boolean matched = false;
        if (cursor != null) {
            matched = cursor.moveToFirst();
            cursor.close();
        }
        return matched;
    }

    public long addTrip(Trip trip) {
        ContentValues values = tripValues(trip);
        values.put("created_at", trip.getCreatedAt());
        return getWritableDatabase().insert(TABLE_TRIP, null, values);
    }

    public List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();
        readTrips(getReadableDatabase().query(TABLE_TRIP, null, null, null, null, null, "id DESC"), list);
        return list;
    }

    public List<Trip> getTripsForUser(int userId) {
        List<Trip> list = new ArrayList<>();
        readTrips(getReadableDatabase().query(TABLE_TRIP, null, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "id DESC"), list);
        return list;
    }

    public Trip getTripById(int id) {
        List<Trip> trips = new ArrayList<>();
        readTrips(getReadableDatabase().query(TABLE_TRIP, null, "id=?", new String[]{String.valueOf(id)}, null, null, null), trips);
        return trips.isEmpty() ? null : trips.get(0);
    }

    public Trip getTripForUser(int id, int userId) {
        List<Trip> trips = new ArrayList<>();
        readTrips(getReadableDatabase().query(TABLE_TRIP, null, "id=? AND user_id=?", new String[]{String.valueOf(id), String.valueOf(userId)}, null, null, null), trips);
        return trips.isEmpty() ? null : trips.get(0);
    }

    public boolean isTripOwnedByUser(int tripId, int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_TRIP, new String[]{"id"}, "id=? AND user_id=?", new String[]{String.valueOf(tripId), String.valueOf(userId)}, null, null, null, "1");
        boolean owned = false;
        if (cursor != null) {
            owned = cursor.moveToFirst();
            cursor.close();
        }
        return owned;
    }

    public int updateTrip(Trip trip) {
        return getWritableDatabase().update(TABLE_TRIP, tripValues(trip), "id=?", new String[]{String.valueOf(trip.getId())});
    }

    public int updateTripStatus(int tripId, String status) {
        ContentValues values = new ContentValues();
        values.put("status", status);
        return getWritableDatabase().update(TABLE_TRIP, values, "id=?", new String[]{String.valueOf(tripId)});
    }

    private ContentValues tripValues(Trip trip) {
        ContentValues values = new ContentValues();
        values.put("user_id", trip.getUserId());
        values.put("name", trip.getName());
        values.put("destination", trip.getDestination());
        values.put("start_date", trip.getStartDate());
        values.put("end_date", trip.getEndDate());
        values.put("cover_path", trip.getCoverPath());
        values.put("mode", blankToDefault(trip.getMode(), "simple_record"));
        values.put("status", blankToDefault(trip.getStatus(), "during_trip"));
        return values;
    }

    private void readTrips(Cursor cursor, List<Trip> list) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Trip(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            getOptionalInt(cursor, "user_id"),
                            getOptionalString(cursor, "name"),
                            getOptionalString(cursor, "destination"),
                            getOptionalString(cursor, "start_date"),
                            getOptionalString(cursor, "end_date"),
                            getOptionalString(cursor, "cover_path"),
                            blankToDefault(getOptionalString(cursor, "mode"), "simple_record"),
                            blankToDefault(getOptionalString(cursor, "status"), "during_trip"),
                            getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void deleteTrip(int tripId) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_DAY_RECORD, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PHOTO, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_SPOT, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_EXPENSE, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PRE_PLAN, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PACKING_ITEM, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_REGRET_ITEM, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_TRIP, "id=?", new String[]{String.valueOf(tripId)});
    }

    public boolean deleteTripForUser(int tripId, int userId) {
        if (!isTripOwnedByUser(tripId, userId)) return false;
        deleteTrip(tripId);
        return true;
    }

    public long addOrUpdateDayRecord(DayRecord record) {
        ContentValues values = new ContentValues();
        values.put("trip_id", record.getTripId());
        values.put("date", record.getDate());
        values.put("note", record.getNote());
        values.put("mood", record.getMood());
        values.put("created_at", record.getCreatedAt());
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.query(TABLE_DAY_RECORD, new String[]{"id"}, "trip_id=? AND date=?", new String[]{String.valueOf(record.getTripId()), record.getDate()}, null, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                int existingId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                cursor.close();
                return db.update(TABLE_DAY_RECORD, values, "id=?", new String[]{String.valueOf(existingId)});
            }
            cursor.close();
        }
        return db.insert(TABLE_DAY_RECORD, null, values);
    }

    public List<DayRecord> getDayRecordsForTrip(int tripId) {
        List<DayRecord> list = new ArrayList<>();
        readDayRecords(getReadableDatabase().query(TABLE_DAY_RECORD, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date ASC"), list);
        return list;
    }

    public DayRecord getDayRecordById(int recordId) {
        List<DayRecord> list = new ArrayList<>();
        readDayRecords(getReadableDatabase().query(TABLE_DAY_RECORD, null, "id=?", new String[]{String.valueOf(recordId)}, null, null, null), list);
        return list.isEmpty() ? null : list.get(0);
    }

    public DayRecord getDayRecordByTripAndDate(int tripId, String date) {
        List<DayRecord> list = new ArrayList<>();
        readDayRecords(getReadableDatabase().query(TABLE_DAY_RECORD, null, "trip_id=? AND date=?", new String[]{String.valueOf(tripId), date}, null, null, null), list);
        return list.isEmpty() ? null : list.get(0);
    }

    private void readDayRecords(Cursor cursor, List<DayRecord> list) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new DayRecord(cursor.getInt(cursor.getColumnIndexOrThrow("id")), cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")), getOptionalString(cursor, "date"), getOptionalString(cursor, "note"), getOptionalString(cursor, "mood"), getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public void deleteDayRecord(int id) {
        getWritableDatabase().delete(TABLE_DAY_RECORD, "id=?", new String[]{String.valueOf(id)});
    }

    public long addPhoto(Photo photo) {
        ContentValues values = new ContentValues();
        values.put("trip_id", photo.getTripId());
        values.put("day_record_id", photo.getDayRecordId());
        values.put("image_path", photo.getImagePath());
        values.put("created_at", photo.getCreatedAt());
        return getWritableDatabase().insert(TABLE_PHOTO, null, values);
    }

    public List<Photo> getPhotosForTrip(int tripId) {
        List<Photo> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PHOTO, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Photo(cursor.getInt(cursor.getColumnIndexOrThrow("id")), cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")), cursor.getInt(cursor.getColumnIndexOrThrow("day_record_id")), getOptionalString(cursor, "image_path"), getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public List<Photo> getPhotosForDayRecord(int dayRecordId) {
        List<Photo> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PHOTO, null, "day_record_id=?",
                new String[]{String.valueOf(dayRecordId)}, null, null, "id ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Photo(cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("day_record_id")),
                            getOptionalString(cursor, "image_path"),
                            getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public void replacePhotosForDayRecord(int tripId, int dayRecordId, List<String> imagePaths, String createdAt) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_PHOTO, "day_record_id=?", new String[]{String.valueOf(dayRecordId)});
        if (imagePaths == null) return;
        for (String imagePath : imagePaths) {
            if (imagePath == null || imagePath.trim().isEmpty()) continue;
            ContentValues values = new ContentValues();
            values.put("trip_id", tripId);
            values.put("day_record_id", dayRecordId);
            values.put("image_path", imagePath);
            values.put("created_at", createdAt);
            db.insert(TABLE_PHOTO, null, values);
        }
    }

    public void deletePhoto(int id) {
        getWritableDatabase().delete(TABLE_PHOTO, "id=?", new String[]{String.valueOf(id)});
    }

    public long addSpot(Spot spot) {
        ContentValues values = new ContentValues();
        values.put("trip_id", spot.getTripId());
        values.put("name", spot.getName());
        values.put("type", spot.getType());
        values.put("note", spot.getNote());
        values.put("is_favorite", spot.getIsFavorite());
        values.put("source_plan_id", spot.getSourcePlanId());
        values.put("created_at", spot.getCreatedAt());
        return getWritableDatabase().insert(TABLE_SPOT, null, values);
    }

    public List<Spot> getSpotsForTrip(int tripId) {
        List<Spot> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_SPOT, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Spot(cursor.getInt(cursor.getColumnIndexOrThrow("id")), cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")), getOptionalString(cursor, "name"), getOptionalString(cursor, "type"), getOptionalString(cursor, "note"), cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")), getOptionalInt(cursor, "source_plan_id"), getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public int updateSpotFavoriteStatus(int spotId, int isFavorite) {
        ContentValues values = new ContentValues();
        values.put("is_favorite", isFavorite);
        return getWritableDatabase().update(TABLE_SPOT, values, "id=?", new String[]{String.valueOf(spotId)});
    }

    public void deleteSpot(int id) {
        getWritableDatabase().delete(TABLE_SPOT, "id=?", new String[]{String.valueOf(id)});
    }

    public long addExpense(Expense expense) {
        ContentValues values = new ContentValues();
        values.put("trip_id", expense.getTripId());
        values.put("category", expense.getCategory());
        values.put("amount", expense.getAmount());
        values.put("note", expense.getNote());
        values.put("date", expense.getDate());
        return getWritableDatabase().insert(TABLE_EXPENSE, null, values);
    }

    public List<Expense> getExpensesForTrip(int tripId) {
        List<Expense> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_EXPENSE, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date DESC, id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new Expense(cursor.getInt(cursor.getColumnIndexOrThrow("id")), cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")), getOptionalString(cursor, "category"), cursor.getDouble(cursor.getColumnIndexOrThrow("amount")), getOptionalString(cursor, "note"), getOptionalString(cursor, "date")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public void deleteExpense(int id) {
        getWritableDatabase().delete(TABLE_EXPENSE, "id=?", new String[]{String.valueOf(id)});
    }

    public long addPrePlan(PrePlan plan) {
        long id = getWritableDatabase().insert(TABLE_PRE_PLAN, null, prePlanValues(plan));
        if (id != -1) refreshRegretsForTrip(plan.getTripId());
        return id;
    }

    public List<PrePlan> getPrePlansForTrip(int tripId) {
        List<PrePlan> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC");
        readPrePlans(cursor, list);
        return list;
    }

    public List<PrePlan> getPrePlansForTripSorted(int tripId) {
        List<PrePlan> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "day_index ASC, " + timeSectionSortSql() + ", time_text ASC, id ASC");
        readPrePlans(cursor, list);
        return list;
    }

    public List<PrePlan> getPrePlansForTripDaySorted(int tripId, int dayIndex) {
        List<PrePlan> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=? AND day_index=?", new String[]{String.valueOf(tripId), String.valueOf(Math.max(0, dayIndex))}, null, null, timeSectionSortSql() + ", time_text ASC, id ASC");
        readPrePlans(cursor, list);
        return list;
    }

    public PrePlan getPrePlanById(int id) {
        List<PrePlan> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        readPrePlans(cursor, list);
        return list.isEmpty() ? null : list.get(0);
    }

    private void readPrePlans(Cursor cursor, List<PrePlan> list) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new PrePlan(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                            getOptionalInt(cursor, "day_index"),
                            getOptionalString(cursor, "type"),
                            getOptionalString(cursor, "title"),
                            getOptionalString(cursor, "location"),
                            getOptionalString(cursor, "target_date"),
                            getOptionalString(cursor, "time_text"),
                            getOptionalString(cursor, "time_section"),
                            getOptionalString(cursor, "note"),
                            cursor.getInt(cursor.getColumnIndexOrThrow("is_completed")),
                            getOptionalString(cursor, "status"),
                            getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public int updatePrePlan(PrePlan plan) {
        int updated = getWritableDatabase().update(TABLE_PRE_PLAN, prePlanValues(plan), "id=?", new String[]{String.valueOf(plan.getId())});
        if (updated > 0) refreshRegretsForTrip(plan.getTripId());
        return updated;
    }

    public void deletePrePlan(int id) {
        PrePlan plan = getPrePlanById(id);
        getWritableDatabase().delete(TABLE_PRE_PLAN, "id=?", new String[]{String.valueOf(id)});
        if (plan != null) refreshRegretsForTrip(plan.getTripId());
    }

    public int markPrePlanCompleted(int id, boolean completed) {
        PrePlan plan = getPrePlanById(id);
        ContentValues values = new ContentValues();
        values.put("is_completed", completed ? 1 : 0);
        int updated = getWritableDatabase().update(TABLE_PRE_PLAN, values, "id=?", new String[]{String.valueOf(id)});
        if (updated > 0 && plan != null) refreshRegretsForTrip(plan.getTripId());
        return updated;
    }

    public int completePrePlanAndSyncCheckIn(PrePlan plan) {
        if (plan == null) return 0;
        int updated = markPrePlanCompleted(plan.getId(), true);
        if (shouldSyncPlanToSpot(plan) && !hasSpotForPrePlan(plan.getId())) {
            Spot spot = new Spot();
            spot.setTripId(plan.getTripId());
            spot.setName(plan.getTitle());
            spot.setType(planCheckInType(plan.getType()));
            spot.setNote(plan.getNote());
            spot.setIsFavorite(0);
            spot.setSourcePlanId(plan.getId());
            spot.setCreatedAt(isBlank(plan.getTargetDate()) ? nowDate() : plan.getTargetDate());
            addSpot(spot);
        }
        return updated;
    }

    public boolean hasSpotForPrePlan(int prePlanId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_SPOT + " WHERE source_plan_id=?", String.valueOf(prePlanId)) > 0;
    }

    private boolean shouldSyncPlanToSpot(PrePlan plan) {
        String type = plan.getType();
        return type != null && (type.contains("美食") || type.contains("編椋") || type.contains("地方") || type.contains("鍦") || type.contains("景点"));
    }

    private String planCheckInType(String type) {
        if (type != null && (type.contains("美食") || type.contains("編椋"))) return "美食";
        return "景点";
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }

    public int countPrePlansForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_PRE_PLAN + " WHERE trip_id=?", tripId);
    }

    public int countCompletedPrePlansForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_PRE_PLAN + " WHERE trip_id=? AND is_completed=1", tripId);
    }

    public int getPlanCompletionRate(int tripId) {
        int total = countPrePlansForTrip(tripId);
        return total == 0 ? 0 : Math.round(countCompletedPrePlansForTrip(tripId) * 100f / total);
    }

    public long addTimelineItem(TimelineItem item) {
        return getWritableDatabase().insert(TABLE_TIMELINE_ITEM, null, timelineItemValues(item));
    }

    public int updateTimelineItem(TimelineItem item) {
        int updated = getWritableDatabase().update(TABLE_TIMELINE_ITEM, timelineItemValues(item), "id=?", new String[]{String.valueOf(item.getId())});
        if (updated > 0) {
            refreshRegretsForTrip(item.getTripId());
        }
        return updated;
    }

    public int deleteTimelineItem(int id) {
        TimelineItem item = getTimelineItemById(id);
        int tripId = item == null ? -1 : item.getTripId();
        int deleted = getWritableDatabase().delete(TABLE_TIMELINE_ITEM, "id=?", new String[]{String.valueOf(id)});
        if (tripId > 0) {
            refreshRegretsForTrip(tripId);
        }
        return deleted;
    }

    public TimelineItem getTimelineItemById(int id) {
        List<TimelineItem> list = new ArrayList<>();
        readTimelineItems(getReadableDatabase().query(TABLE_TIMELINE_ITEM, null, "id=?", new String[]{String.valueOf(id)}, null, null, null), list);
        return list.isEmpty() ? null : list.get(0);
    }

    public List<TimelineItem> getTimelineItemsForTrip(int tripId) {
        List<TimelineItem> list = new ArrayList<>();
        readTimelineItems(getReadableDatabase().query(TABLE_TIMELINE_ITEM, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "day_index ASC, " + timeSlotSortSql() + ", id ASC"), list);
        return list;
    }

    public List<TimelineItem> getTimelineItemsForTripDay(int tripId, int dayIndex) {
        List<TimelineItem> list = new ArrayList<>();
        readTimelineItems(getReadableDatabase().query(TABLE_TIMELINE_ITEM, null, "trip_id=? AND day_index=?", new String[]{String.valueOf(tripId), String.valueOf(Math.max(0, dayIndex))}, null, null, timeSlotSortSql() + ", id ASC"), list);
        return list;
    }

    public int markTimelineItemDoneWithCheckin(int id, String photo, String note, String mood, String time) {
        ContentValues values = new ContentValues();
        values.put("status", TimelineItem.STATUS_DONE);
        values.put("checkin_photo", photo);
        values.put("checkin_note", note);
        values.put("checkin_mood", mood);
        values.put("checkin_time", time);
        int updated = getWritableDatabase().update(TABLE_TIMELINE_ITEM, values, "id=?", new String[]{String.valueOf(id)});
        syncRegretsAfterTimelineChange(id);
        return updated;
    }

    public int markTimelineItemDone(int id) {
        ContentValues values = new ContentValues();
        values.put("status", TimelineItem.STATUS_DONE);
        values.put("checkin_time", nowDate());
        int updated = getWritableDatabase().update(TABLE_TIMELINE_ITEM, values, "id=?", new String[]{String.valueOf(id)});
        syncRegretsAfterTimelineChange(id);
        return updated;
    }

    public int markTimelineItemSkipped(int id) {
        ContentValues values = new ContentValues();
        values.put("status", TimelineItem.STATUS_SKIPPED);
        int updated = getWritableDatabase().update(TABLE_TIMELINE_ITEM, values, "id=?", new String[]{String.valueOf(id)});
        syncRegretsAfterTimelineChange(id);
        return updated;
    }

    private void syncRegretsAfterTimelineChange(int timelineItemId) {
        int tripId = intQueryWithArgs("SELECT trip_id FROM " + TABLE_TIMELINE_ITEM + " WHERE id=?",
                String.valueOf(timelineItemId));
        if (tripId > 0) {
            refreshRegretsForTrip(tripId);
        }
    }

    public int countTimelineItemsForTrip(int tripId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_TIMELINE_ITEM
                        + " WHERE trip_id=? AND kind IN (?,?)",
                String.valueOf(tripId), TimelineItem.KIND_PLAN_FIXED, TimelineItem.KIND_PLAN_PENDING);
    }

    public int countDoneTimelineItemsForTrip(int tripId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_TIMELINE_ITEM
                        + " WHERE trip_id=? AND status=? AND kind IN (?,?)",
                String.valueOf(tripId), TimelineItem.STATUS_DONE, TimelineItem.KIND_PLAN_FIXED, TimelineItem.KIND_PLAN_PENDING);
    }

    public int getTimelineCompletionRate(int tripId) {
        int total = countTimelineItemsForTrip(tripId);
        return total == 0 ? 0 : Math.round(countDoneTimelineItemsForTrip(tripId) * 100f / total);
    }

    public List<TimelineItem> getSkippedTimelineItemsForTrip(int tripId) {
        List<TimelineItem> list = new ArrayList<>();
        readTimelineItems(getReadableDatabase().query(TABLE_TIMELINE_ITEM, null, "trip_id=? AND status=?", new String[]{String.valueOf(tripId), TimelineItem.STATUS_SKIPPED}, null, null, "day_index ASC, id ASC"), list);
        return list;
    }

    private ContentValues timelineItemValues(TimelineItem item) {
        ContentValues values = new ContentValues();
        values.put("trip_id", item.getTripId());
        values.put("day_index", Math.max(0, item.getDayIndex()));
        values.put("title", item.getTitle());
        values.put("type", item.getType());
        values.put("time_slot", item.getTimeSlot());
        values.put("status", item.getStatus());
        values.put("kind", item.getKind());
        values.put("checkin_photo", item.getCheckinPhoto());
        values.put("checkin_note", item.getCheckinNote());
        values.put("checkin_mood", item.getCheckinMood());
        values.put("checkin_time", item.getCheckinTime());
        values.put("created_at", item.getCreatedAt());
        return values;
    }

    private void readTimelineItems(Cursor cursor, List<TimelineItem> list) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new TimelineItem(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                            getOptionalInt(cursor, "day_index"),
                            getOptionalString(cursor, "title"),
                            getOptionalString(cursor, "type"),
                            getOptionalString(cursor, "time_slot"),
                            getOptionalString(cursor, "status"),
                            getOptionalString(cursor, "kind"),
                            getOptionalString(cursor, "checkin_photo"),
                            getOptionalString(cursor, "checkin_note"),
                            getOptionalString(cursor, "checkin_mood"),
                            getOptionalString(cursor, "checkin_time"),
                            getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private String timeSlotSortSql() {
        return "CASE "
                + "WHEN time_slot GLOB '[0-2][0-9]:[0-5][0-9]' THEN 0 "
                + "WHEN time_slot = '上午' THEN 1 "
                + "WHEN time_slot = '下午' THEN 2 "
                + "WHEN time_slot = '晚上' THEN 3 "
                + "ELSE 4 END, time_slot";
    }

    private ContentValues prePlanValues(PrePlan plan) {
        ContentValues values = new ContentValues();
        values.put("trip_id", plan.getTripId());
        values.put("day_index", Math.max(0, plan.getDayIndex()));
        values.put("type", plan.getType());
        values.put("title", plan.getTitle());
        values.put("location", plan.getLocation());
        values.put("target_date", targetDateForPlan(plan));
        values.put("time_text", plan.getTimeText());
        values.put("time_section", plan.getTimeSection());
        values.put("note", plan.getNote());
        values.put("is_completed", plan.getIsCompleted());
        values.put("status", plan.getStatus());
        values.put("created_at", plan.getCreatedAt());
        return values;
    }

    private String timeSectionSortSql() {
        return "CASE time_section WHEN '上午' THEN 0 WHEN '下午' THEN 1 WHEN '晚上' THEN 2 ELSE 3 END";
    }

    private String targetDateForPlan(PrePlan plan) {
        Trip trip = getTripById(plan.getTripId());
        String calculatedDate = dateForTripDay(trip, plan.getDayIndex());
        return calculatedDate.isEmpty() ? plan.getTargetDate() : calculatedDate;
    }

    private String dateForTripDay(Trip trip, int dayIndex) {
        if (trip == null || isBlank(trip.getStartDate())) return "";
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date start = format.parse(trip.getStartDate());
            if (start == null) return "";
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(start);
            calendar.add(Calendar.DATE, Math.max(0, dayIndex));
            return format.format(calendar.getTime());
        } catch (ParseException e) {
            return "";
        }
    }

    private int dayIndexForDate(Trip trip, String targetDate) {
        if (trip == null || isBlank(trip.getStartDate()) || isBlank(targetDate)) return 0;
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
        try {
            Date start = format.parse(trip.getStartDate());
            Date target = format.parse(targetDate);
            if (start == null || target == null || target.before(start)) return 0;
            long diffMillis = target.getTime() - start.getTime();
            return (int) Math.max(0, diffMillis / (24L * 60L * 60L * 1000L));
        } catch (ParseException e) {
            return 0;
        }
    }

    private void backfillPrePlanDayIndexes(SQLiteDatabase db) {
        Cursor cursor = db.query(TABLE_PRE_PLAN, new String[]{"id", "trip_id", "target_date"}, null, null, null, null, null);
        if (cursor != null) {
            while (cursor.moveToNext()) {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                int tripId = cursor.getInt(cursor.getColumnIndexOrThrow("trip_id"));
                String targetDate = getOptionalString(cursor, "target_date");
                Trip trip = getTripByIdForDb(db, tripId);
                ContentValues values = new ContentValues();
                values.put("day_index", dayIndexForDate(trip, targetDate));
                if (isBlank(getOptionalString(cursor, "target_date"))) {
                    values.put("target_date", dateForTripDay(trip, 0));
                }
                values.put("time_section", PrePlan.TIME_SECTION_PENDING);
                db.update(TABLE_PRE_PLAN, values, "id=?", new String[]{String.valueOf(id)});
            }
            cursor.close();
        }
    }

    private Trip getTripByIdForDb(SQLiteDatabase db, int id) {
        List<Trip> trips = new ArrayList<>();
        readTrips(db.query(TABLE_TRIP, null, "id=?", new String[]{String.valueOf(id)}, null, null, null), trips);
        return trips.isEmpty() ? null : trips.get(0);
    }

    public void ensureDefaultPackingItems(int tripId) {
        if (countPackingItemsForTrip(tripId) > 0) return;
        insertDefaultPackingItems(getWritableDatabase(), tripId);
    }

    private void insertDefaultPackingItems(SQLiteDatabase db, int tripId) {
        insertPackingDefaults(db, tripId, "洗漱类", new String[]{"牙刷", "牙膏", "毛巾", "洗面奶", "防晒霜", "护肤品"});
        insertPackingDefaults(db, tripId, "衣物类", new String[]{"换洗衣物", "外套", "睡衣", "袜子", "舒适鞋", "帽子"});
        insertPackingDefaults(db, tripId, "证件类", new String[]{"身份证", "学生证", "银行卡", "少量现金", "交通票据"});
        insertPackingDefaults(db, tripId, "数码类", new String[]{"手机", "充电器", "充电宝", "耳机", "数据线", "转换插头"});
        insertPackingDefaults(db, tripId, "出行类", new String[]{"雨伞", "水杯", "纸巾", "湿巾", "收纳袋", "口罩"});
        insertPackingDefaults(db, tripId, "药品类", new String[]{"感冒药", "肠胃药", "创可贴", "晕车药", "个人常用药"});
        insertPackingDefaults(db, tripId, "其他类", new String[]{"自定义物品"});
    }

    private void insertPackingDefaults(SQLiteDatabase db, int tripId, String category, String[] names) {
        for (String name : names) {
            ContentValues values = new ContentValues();
            values.put("trip_id", tripId);
            values.put("category", category);
            values.put("item_name", name);
            values.put("is_selected", 1);
            values.put("is_packed", 0);
            values.put("is_custom", 0);
            values.put("created_at", nowDate());
            db.insert(TABLE_PACKING_ITEM, null, values);
        }
    }

    public List<PackingItem> getPackingItemsForTrip(int tripId) {
        List<PackingItem> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PACKING_ITEM, null, "trip_id=? AND is_selected=1", new String[]{String.valueOf(tripId)}, null, null, "category ASC, id ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new PackingItem(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                            getOptionalString(cursor, "category"),
                            getOptionalString(cursor, "item_name"),
                            getOptionalInt(cursor, "is_selected"),
                            getOptionalInt(cursor, "is_packed"),
                            getOptionalInt(cursor, "is_custom"),
                            getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public void ensurePackingSelectionPool(int tripId) {
        if (countAllPackingItemsForTrip(tripId) > 0) return;
        SQLiteDatabase db = getWritableDatabase();
        insertPackingSelectionDefaults(db, tripId, "洗漱类", new String[]{"牙刷", "牙膏", "毛巾", "洗面奶", "防晒霜", "护肤品"});
        insertPackingSelectionDefaults(db, tripId, "衣物类", new String[]{"换洗衣物", "外套", "睡衣", "袜子", "舒适鞋", "帽子"});
        insertPackingSelectionDefaults(db, tripId, "证件类", new String[]{"身份证", "学生证", "银行卡", "少量现金", "交通票据"});
        insertPackingSelectionDefaults(db, tripId, "数码类", new String[]{"手机", "充电器", "充电宝", "耳机", "数据线", "转换插头"});
        insertPackingSelectionDefaults(db, tripId, "出行类", new String[]{"雨伞", "水杯", "纸巾", "湿巾", "收纳袋", "口罩"});
        insertPackingSelectionDefaults(db, tripId, "药品类", new String[]{"感冒药", "肠胃药", "创可贴", "晕车药", "个人常用药"});
        insertPackingSelectionDefaults(db, tripId, "其他类", new String[]{"自定义物品"});
    }

    public int normalizePackingCategoriesForTrip(int tripId) {
        SQLiteDatabase db = getWritableDatabase();
        int updated = 0;
        updated += normalizePackingCategory(db, tripId, "证件财务", "证件类");
        updated += normalizePackingCategory(db, tripId, "衣物鞋帽", "衣物类");
        updated += normalizePackingCategory(db, tripId, "洗漱护肤", "洗漱类");
        updated += normalizePackingCategory(db, tripId, "电子设备", "数码类");
        updated += normalizePackingCategory(db, tripId, "药品健康", "药品类");
        updated += normalizePackingCategory(db, tripId, "旅行用品", "出行类");
        updated += normalizePackingCategory(db, tripId, "其他", "其他类");
        return updated;
    }

    private int normalizePackingCategory(SQLiteDatabase db, int tripId, String oldCategory, String newCategory) {
        ContentValues values = new ContentValues();
        values.put("category", newCategory);
        return db.update(TABLE_PACKING_ITEM, values, "trip_id=? AND category=?", new String[]{String.valueOf(tripId), oldCategory});
    }

    private void insertPackingSelectionDefaults(SQLiteDatabase db, int tripId, String category, String[] names) {
        for (String name : names) {
            ContentValues values = new ContentValues();
            values.put("trip_id", tripId);
            values.put("category", category);
            values.put("item_name", name);
            values.put("is_selected", 1);
            values.put("is_packed", 0);
            values.put("is_custom", 0);
            values.put("created_at", nowDate());
            db.insert(TABLE_PACKING_ITEM, null, values);
        }
    }

    public List<PackingItem> getAllPackingItemsForTrip(int tripId) {
        List<PackingItem> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PACKING_ITEM, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "category ASC, id ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new PackingItem(
                            cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                            cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                            getOptionalString(cursor, "category"),
                            getOptionalString(cursor, "item_name"),
                            getOptionalInt(cursor, "is_selected"),
                            getOptionalInt(cursor, "is_packed"),
                            getOptionalInt(cursor, "is_custom"),
                            getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public int countAllPackingItemsForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_PACKING_ITEM + " WHERE trip_id=?", tripId);
    }

    public long addPackingItem(PackingItem item) {
        ContentValues values = new ContentValues();
        values.put("trip_id", item.getTripId());
        values.put("category", canonicalPackingCategory(item.getCategory()));
        values.put("item_name", item.getItemName());
        values.put("is_selected", item.getIsSelected());
        values.put("is_packed", item.getIsPacked());
        values.put("is_custom", item.getIsCustom());
        values.put("created_at", item.getCreatedAt());
        return getWritableDatabase().insert(TABLE_PACKING_ITEM, null, values);
    }

    private String canonicalPackingCategory(String category) {
        if ("证件财务".equals(category)) return "证件类";
        if ("衣物鞋帽".equals(category)) return "衣物类";
        if ("洗漱护肤".equals(category)) return "洗漱类";
        if ("电子设备".equals(category)) return "数码类";
        if ("药品健康".equals(category)) return "药品类";
        if ("旅行用品".equals(category)) return "出行类";
        if ("其他".equals(category)) return "其他类";
        return category == null || category.trim().isEmpty() ? "其他类" : category;
    }

    public int updatePackingItemChecked(int id, boolean checked) {
        return updatePackingItemPacked(id, checked);
    }

    public int updatePackingItemPacked(int id, boolean packed) {
        ContentValues values = new ContentValues();
        values.put("is_packed", packed ? 1 : 0);
        return getWritableDatabase().update(TABLE_PACKING_ITEM, values, "id=?", new String[]{String.valueOf(id)});
    }

    public int updatePackingItemSelected(int id, boolean selected) {
        ContentValues values = new ContentValues();
        values.put("is_selected", selected ? 1 : 0);
        if (!selected) {
            values.put("is_packed", 0);
        }
        return getWritableDatabase().update(TABLE_PACKING_ITEM, values, "id=?", new String[]{String.valueOf(id)});
    }

    public int markAllSelectedPackingItemsPacked(int tripId, boolean packed) {
        ContentValues values = new ContentValues();
        values.put("is_packed", packed ? 1 : 0);
        return getWritableDatabase().update(TABLE_PACKING_ITEM, values, "trip_id=? AND is_selected=1", new String[]{String.valueOf(tripId)});
    }

    public void deleteCustomPackingItem(int id) {
        getWritableDatabase().delete(TABLE_PACKING_ITEM, "id=? AND is_custom=1", new String[]{String.valueOf(id)});
    }

    public int countPackingItemsForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_PACKING_ITEM + " WHERE trip_id=? AND is_selected=1", tripId);
    }

    public int countCheckedPackingItemsForTrip(int tripId) {
        return countPackedPackingItemsForTrip(tripId);
    }

    public int countPackedPackingItemsForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_PACKING_ITEM + " WHERE trip_id=? AND is_selected=1 AND is_packed=1", tripId);
    }

    public int getPackingCompletionRate(int tripId) {
        int total = countPackingItemsForTrip(tripId);
        return total == 0 ? 0 : Math.round(countPackedPackingItemsForTrip(tripId) * 100f / total);
    }

    public int generateRegretsForTrip(int tripId) {
        clearRegretsForTrip(tripId);
        int inserted = 0;
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=? AND is_completed=0", new String[]{String.valueOf(tripId)}, null, null, "id ASC");
        if (cursor != null) {
            SQLiteDatabase db = getWritableDatabase();
            if (cursor.moveToFirst()) {
                do {
                    int prePlanId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    String title = getOptionalString(cursor, "title");
                    String type = getOptionalString(cursor, "type");
                    if (hasRegretForPrePlan(prePlanId)) continue;
                    if (hasRegretForTitleAndType(tripId, title, type)) continue;
                    ContentValues values = new ContentValues();
                    values.put("trip_id", tripId);
                    values.put("pre_plan_id", prePlanId);
                    values.put("title", title);
                    values.put("type", type);
                    values.put("reason", regretReasonForType(type));
                    values.put("created_at", nowDate());
                    if (db.insert(TABLE_REGRET_ITEM, null, values) != -1) inserted++;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        inserted += generateTimelineRegretsForTrip(tripId);
        return inserted;
    }

    public int clearRegretsForTrip(int tripId) {
        return getWritableDatabase().delete(TABLE_REGRET_ITEM, "trip_id=?", new String[]{String.valueOf(tripId)});
    }

    public int refreshRegretsForTrip(int tripId) {
        if (!TravelConstants.TripState.REVIEWING.equals(tripStatus(tripId))) {
            return countRegretsForTrip(tripId);
        }
        return generateRegretsForTrip(tripId);
    }

    public int syncReviewDataForTrip(int tripId) {
        return refreshRegretsForTrip(tripId);
    }

    private String tripStatus(int tripId) {
        Cursor cursor = getReadableDatabase().query(TABLE_TRIP, new String[]{"status"},
                "id=?", new String[]{String.valueOf(tripId)}, null, null, null, "1");
        String status = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                status = getOptionalString(cursor, "status");
            }
            cursor.close();
        }
        return status;
    }

    private int generateTimelineRegretsForTrip(int tripId) {
        int inserted = 0;
        Cursor cursor = getReadableDatabase().query(TABLE_TIMELINE_ITEM, null,
                "trip_id=? AND status<>? AND kind IN (?,?)",
                new String[]{
                        String.valueOf(tripId),
                        TimelineItem.STATUS_DONE,
                        TimelineItem.KIND_PLAN_FIXED,
                        TimelineItem.KIND_PLAN_PENDING
                },
                null, null, "day_index ASC, " + timeSlotSortSql() + ", id ASC");
        if (cursor != null) {
            SQLiteDatabase db = getWritableDatabase();
            if (cursor.moveToFirst()) {
                do {
                    String title = getOptionalString(cursor, "title");
                    String type = getOptionalString(cursor, "type");
                    if (hasRegretForTimelineItem(tripId, title, type)) continue;
                    if (hasRegretForTitleAndType(tripId, title, type)) continue;
                    ContentValues values = new ContentValues();
                    values.put("trip_id", tripId);
                    values.put("pre_plan_id", 0);
                    values.put("title", title);
                    values.put("type", type);
                    values.put("reason", regretReasonForTimelineKind(getOptionalString(cursor, "kind")));
                    values.put("created_at", nowDate());
                    if (db.insert(TABLE_REGRET_ITEM, null, values) != -1) inserted++;
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return inserted;
    }

    private boolean hasRegretForPrePlan(int prePlanId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_REGRET_ITEM + " WHERE pre_plan_id=?", String.valueOf(prePlanId)) > 0;
    }

    private boolean hasRegretForTimelineItem(int tripId, String title, String type) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_REGRET_ITEM
                        + " WHERE trip_id=? AND pre_plan_id=0 AND title=? AND type=?",
                String.valueOf(tripId), title == null ? "" : title, type == null ? "" : type) > 0;
    }

    private boolean hasRegretForTitleAndType(int tripId, String title, String type) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_REGRET_ITEM
                        + " WHERE trip_id=? AND title=? AND type=?",
                String.valueOf(tripId), title == null ? "" : title, type == null ? "" : type) > 0;
    }

    private String regretReasonForTimelineKind(String kind) {
        if (TimelineItem.KIND_PLAN_PENDING.equals(kind)) {
            return "这个待定计划还没来得及完成，留给下一次旅程继续期待。";
        }
        return "这个具体计划还没完成，成为这次旅行里差一点抵达的留白。";
    }

    public String regretReasonForType(String type) {
        if ("想去的地方".equals(type)) return "这次没去成，下次一定要补上。";
        if ("想吃的美食".equals(type)) return "这份味道，留给下一次旅行。";
        if ("想买的东西".equals(type)) return "没有带走它，也算留下了再次出发的理由。";
        if ("想做的事情".equals(type)) return "未完成的体验，变成了下一次的期待。";
        return "这个小遗憾，值得下次重新安排。";
    }

    public List<RegretItem> getRegretsForTrip(int tripId) {
        List<RegretItem> list = new ArrayList<>();
        readRegrets(getReadableDatabase().query(TABLE_REGRET_ITEM, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC"), list);
        return list;
    }

    public List<RegretItem> getRegretsForUser(int userId) {
        List<RegretItem> list = new ArrayList<>();
        readRegrets(getReadableDatabase().rawQuery("SELECT r.* FROM " + TABLE_REGRET_ITEM + " r INNER JOIN " + TABLE_TRIP + " t ON r.trip_id=t.id WHERE t.user_id=? ORDER BY r.id DESC", new String[]{String.valueOf(userId)}), list);
        return list;
    }

    private void readRegrets(Cursor cursor, List<RegretItem> list) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new RegretItem(cursor.getInt(cursor.getColumnIndexOrThrow("id")), cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")), cursor.getInt(cursor.getColumnIndexOrThrow("pre_plan_id")), getOptionalString(cursor, "title"), getOptionalString(cursor, "type"), getOptionalString(cursor, "reason"), getOptionalString(cursor, "created_at")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    public int countRegretsForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_REGRET_ITEM + " WHERE trip_id=?", tripId);
    }

    public boolean isTimelineRegret(RegretItem item) {
        return item != null && item.getPrePlanId() == 0;
    }

    public String regretSourceLabel(RegretItem item) {
        return isTimelineRegret(item) ? "来自当日记录" : "来自行前准备";
    }

    public int countRegretsForUser(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_REGRET_ITEM + " r INNER JOIN " + TABLE_TRIP + " t ON r.trip_id=t.id WHERE t.user_id=?", String.valueOf(userId));
    }

    public int getTotalPrePlanCount(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_PRE_PLAN + " p INNER JOIN " + TABLE_TRIP + " t ON p.trip_id=t.id WHERE t.user_id=?", String.valueOf(userId));
    }

    public int getCompletedPrePlanCount(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_PRE_PLAN + " p INNER JOIN " + TABLE_TRIP + " t ON p.trip_id=t.id WHERE t.user_id=? AND p.is_completed=1", String.valueOf(userId));
    }

    public int getAveragePlanCompletionRate(int userId) {
        int total = getTotalPrePlanCount(userId);
        return total == 0 ? 0 : Math.round(getCompletedPrePlanCount(userId) * 100f / total);
    }

    public int getAveragePackingCompletionRate(int userId) {
        int total = intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_PACKING_ITEM + " p INNER JOIN " + TABLE_TRIP + " t ON p.trip_id=t.id WHERE t.user_id=? AND p.is_selected=1", String.valueOf(userId));
        int packed = intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_PACKING_ITEM + " p INNER JOIN " + TABLE_TRIP + " t ON p.trip_id=t.id WHERE t.user_id=? AND p.is_selected=1 AND p.is_packed=1", String.valueOf(userId));
        return total == 0 ? 0 : Math.round(packed * 100f / total);
    }

    public int countPhotosForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_PHOTO + " WHERE trip_id=?", tripId);
    }

    public int countSpotsForTrip(int tripId) {
        return intQuery("SELECT COUNT(*) FROM " + TABLE_SPOT + " WHERE trip_id=?", tripId);
    }

    private int intQuery(String sql, int tripId) {
        Cursor cursor = getReadableDatabase().rawQuery(sql, new String[]{String.valueOf(tripId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    private int intQueryWithArgs(String sql, String... args) {
        Cursor cursor = getReadableDatabase().rawQuery(sql, args);
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) count = cursor.getInt(0);
            cursor.close();
        }
        return count;
    }

    public double getTotalExpenseForTrip(int tripId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE trip_id=?", new String[]{String.valueOf(tripId)});
        double total = 0.0;
        if (cursor != null) {
            if (cursor.moveToFirst()) total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public String getHighestExpenseCategory(int tripId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT category, SUM(amount) AS total FROM " + TABLE_EXPENSE + " WHERE trip_id=? GROUP BY category ORDER BY total DESC LIMIT 1", new String[]{String.valueOf(tripId)});
        String category = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) category = cursor.getString(0);
            cursor.close();
        }
        return category;
    }

    public double getCategoryExpenseAmount(int tripId, String category) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE trip_id=? AND category=?", new String[]{String.valueOf(tripId), category});
        double amount = 0.0;
        if (cursor != null) {
            if (cursor.moveToFirst()) amount = cursor.getDouble(0);
            cursor.close();
        }
        return amount;
    }

    public String getMostFrequentMood(int tripId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT mood, COUNT(mood) AS cnt FROM " + TABLE_DAY_RECORD + " WHERE trip_id=? GROUP BY mood ORDER BY cnt DESC LIMIT 1", new String[]{String.valueOf(tripId)});
        String mood = "开心";
        if (cursor != null) {
            if (cursor.moveToFirst()) mood = cursor.getString(0);
            cursor.close();
        }
        return mood;
    }

    public String getFavoriteSpot(int tripId) {
        String spotName = "";
        Cursor cursor = getReadableDatabase().query(TABLE_SPOT, new String[]{"name"}, "trip_id=? AND is_favorite=1", new String[]{String.valueOf(tripId)}, null, null, "id ASC", "1");
        if (cursor != null) {
            if (cursor.moveToFirst()) spotName = cursor.getString(0);
            cursor.close();
        }
        if (spotName.isEmpty()) {
            Cursor anyCursor = getReadableDatabase().query(TABLE_SPOT, new String[]{"name"}, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id ASC", "1");
            if (anyCursor != null) {
                if (anyCursor.moveToFirst()) spotName = anyCursor.getString(0);
                anyCursor.close();
            }
        }
        return spotName;
    }

    public String getLongestNote(int tripId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT note FROM " + TABLE_DAY_RECORD + " WHERE trip_id=? ORDER BY LENGTH(note) DESC LIMIT 1", new String[]{String.valueOf(tripId)});
        String note = "";
        if (cursor != null) {
            if (cursor.moveToFirst()) note = cursor.getString(0);
            cursor.close();
        }
        return note;
    }

    public int getTotalTripCount(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_TRIP + " WHERE user_id=?", String.valueOf(userId));
    }

    public int getTotalTravelDays(int userId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(julianday(end_date) - julianday(start_date) + 1) FROM " + TABLE_TRIP + " WHERE user_id=?", new String[]{String.valueOf(userId)});
        int days = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) days = (int) cursor.getDouble(0);
            cursor.close();
        }
        return Math.max(days, 0);
    }

    public int getTotalPhotoCount(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_PHOTO + " p INNER JOIN " + TABLE_TRIP + " t ON p.trip_id=t.id WHERE t.user_id=?", String.valueOf(userId));
    }

    public int getTotalSpotCount(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_SPOT + " s INNER JOIN " + TABLE_TRIP + " t ON s.trip_id=t.id WHERE t.user_id=?", String.valueOf(userId));
    }

    public double getTotalExpense(int userId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT SUM(e.amount) FROM " + TABLE_EXPENSE + " e INNER JOIN " + TABLE_TRIP + " t ON e.trip_id=t.id WHERE t.user_id=?", new String[]{String.valueOf(userId)});
        double total = 0.0;
        if (cursor != null) {
            if (cursor.moveToFirst()) total = cursor.getDouble(0);
            cursor.close();
        }
        return total;
    }

    public String getMostCommonMood(int userId) {
        Cursor cursor = getReadableDatabase().rawQuery("SELECT d.mood, COUNT(*) AS cnt FROM " + TABLE_DAY_RECORD + " d INNER JOIN " + TABLE_TRIP + " t ON d.trip_id=t.id WHERE t.user_id=? GROUP BY d.mood ORDER BY cnt DESC LIMIT 1", new String[]{String.valueOf(userId)});
        String mood = "暂无记录";
        if (cursor != null) {
            if (cursor.moveToFirst()) mood = cursor.getString(0);
            cursor.close();
        }
        return mood;
    }

    public String getRecentDestination(int userId) {
        Cursor cursor = getReadableDatabase().query(TABLE_TRIP, new String[]{"destination"}, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "id DESC", "1");
        String destination = "暂无记录";
        if (cursor != null) {
            if (cursor.moveToFirst()) destination = cursor.getString(0);
            cursor.close();
        }
        return destination;
    }

    public List<Trip> searchTrips(String text) {
        List<Trip> list = new ArrayList<>();
        String query = "%" + text + "%";
        readTrips(getReadableDatabase().rawQuery("SELECT DISTINCT t.* FROM " + TABLE_TRIP + " t LEFT JOIN " + TABLE_DAY_RECORD + " d ON t.id=d.trip_id WHERE t.name LIKE ? OR t.destination LIKE ? OR d.note LIKE ? ORDER BY t.id DESC", new String[]{query, query, query}), list);
        return list;
    }

    public List<Trip> filterTripsByMood(String mood) {
        List<Trip> list = new ArrayList<>();
        readTrips(getReadableDatabase().rawQuery("SELECT DISTINCT t.* FROM " + TABLE_TRIP + " t INNER JOIN " + TABLE_DAY_RECORD + " d ON t.id=d.trip_id WHERE d.mood=? ORDER BY t.id DESC", new String[]{mood}), list);
        return list;
    }

    public List<Trip> searchTripsForUser(int userId, String text) {
        List<Trip> list = new ArrayList<>();
        String query = "%" + text + "%";
        readTrips(getReadableDatabase().rawQuery("SELECT DISTINCT t.* FROM " + TABLE_TRIP + " t LEFT JOIN " + TABLE_DAY_RECORD + " d ON t.id=d.trip_id WHERE t.user_id=? AND (t.name LIKE ? OR t.destination LIKE ? OR d.note LIKE ?) ORDER BY t.id DESC", new String[]{String.valueOf(userId), query, query, query}), list);
        return list;
    }

    public List<Trip> filterTripsByMoodForUser(int userId, String mood) {
        List<Trip> list = new ArrayList<>();
        readTrips(getReadableDatabase().rawQuery("SELECT DISTINCT t.* FROM " + TABLE_TRIP + " t INNER JOIN " + TABLE_DAY_RECORD + " d ON t.id=d.trip_id WHERE t.user_id=? AND d.mood=? ORDER BY t.id DESC", new String[]{String.valueOf(userId), mood}), list);
        return list;
    }

    public List<SearchResult> getTripSearchResultsForUser(int userId) {
        List<SearchResult> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_TRIP, null, "user_id=?", new String[]{String.valueOf(userId)}, null, null, "id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new SearchResult(cursor.getInt(cursor.getColumnIndexOrThrow("id")), getOptionalString(cursor, "name"), getOptionalString(cursor, "destination"), "旅行", getOptionalString(cursor, "start_date") + " ~ " + getOptionalString(cursor, "end_date")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public List<SearchResult> searchResultsForUser(int userId, String text) {
        List<SearchResult> list = new ArrayList<>();
        String query = "%" + text + "%";
        SQLiteDatabase db = getReadableDatabase();
        readSearchResults(db.rawQuery("SELECT t.id AS trip_id, t.name AS trip_name, t.destination AS subtitle, '旅行' AS source_type, t.start_date || ' ~ ' || t.end_date AS date_text FROM " + TABLE_TRIP + " t WHERE t.user_id=? AND (t.name LIKE ? OR t.destination LIKE ?) ORDER BY t.id DESC", new String[]{String.valueOf(userId), query, query}), list);
        readSearchResults(db.rawQuery("SELECT t.id AS trip_id, d.mood AS trip_name, d.note AS subtitle, '日记' AS source_type, d.date AS date_text FROM " + TABLE_DAY_RECORD + " d INNER JOIN " + TABLE_TRIP + " t ON d.trip_id=t.id WHERE t.user_id=? AND (d.mood LIKE ? OR d.note LIKE ?) ORDER BY d.date DESC", new String[]{String.valueOf(userId), query, query}), list);
        readSearchResults(db.rawQuery("SELECT t.id AS trip_id, s.name AS trip_name, s.note AS subtitle, '景点' AS source_type, s.created_at AS date_text FROM " + TABLE_SPOT + " s INNER JOIN " + TABLE_TRIP + " t ON s.trip_id=t.id WHERE t.user_id=? AND s.name LIKE ? ORDER BY s.id DESC", new String[]{String.valueOf(userId), query}), list);
        readSearchResults(db.rawQuery("SELECT t.id AS trip_id, p.title AS trip_name, p.note AS subtitle, '计划' AS source_type, p.target_date AS date_text FROM " + TABLE_PRE_PLAN + " p INNER JOIN " + TABLE_TRIP + " t ON p.trip_id=t.id WHERE t.user_id=? AND p.title LIKE ? ORDER BY p.id DESC", new String[]{String.valueOf(userId), query}), list);
        readSearchResults(db.rawQuery("SELECT t.id AS trip_id, r.title AS trip_name, r.reason AS subtitle, '遗憾' AS source_type, r.created_at AS date_text FROM " + TABLE_REGRET_ITEM + " r INNER JOIN " + TABLE_TRIP + " t ON r.trip_id=t.id WHERE t.user_id=? AND r.title LIKE ? ORDER BY r.id DESC", new String[]{String.valueOf(userId), query}), list);
        return list;
    }

    public List<SearchResult> moodSearchResultsForUser(int userId, String mood) {
        List<SearchResult> list = new ArrayList<>();
        readSearchResults(getReadableDatabase().rawQuery("SELECT t.id AS trip_id, t.name AS trip_name, d.mood || ' · ' || t.destination AS subtitle, '日记' AS source_type, d.date AS date_text FROM " + TABLE_DAY_RECORD + " d INNER JOIN " + TABLE_TRIP + " t ON d.trip_id=t.id WHERE t.user_id=? AND d.mood=? ORDER BY d.date DESC", new String[]{String.valueOf(userId), mood}), list);
        return list;
    }

    private void readSearchResults(Cursor cursor, List<SearchResult> list) {
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    list.add(new SearchResult(cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")), getOptionalString(cursor, "trip_name"), getOptionalString(cursor, "subtitle"), getOptionalString(cursor, "source_type"), getOptionalString(cursor, "date_text")));
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
    }

    private String blankToDefault(String value, String fallback) {
        return isBlank(value) ? fallback : value;
    }
}
