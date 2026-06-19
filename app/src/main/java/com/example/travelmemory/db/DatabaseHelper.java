package com.example.travelmemory.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Expense;
import com.example.travelmemory.model.PackingItem;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.PrePlan;
import com.example.travelmemory.model.RegretItem;
import com.example.travelmemory.model.SearchResult;
import com.example.travelmemory.model.Spot;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.model.User;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "travel_memory.db";
    private static final int DATABASE_VERSION = 8;

    public static final String TABLE_USER = "user";
    public static final String TABLE_TRIP = "trip";
    public static final String TABLE_DAY_RECORD = "day_record";
    public static final String TABLE_PHOTO = "photo";
    public static final String TABLE_SPOT = "spot";
    public static final String TABLE_EXPENSE = "expense";
    public static final String TABLE_PRE_PLAN = "pre_plan";
    public static final String TABLE_PACKING_ITEM = "packing_item";
    public static final String TABLE_REGRET_ITEM = "regret_item";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
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
    }

    private void createTravelLoopTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PRE_PLAN + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, type TEXT, title TEXT, location TEXT, target_date TEXT, time_text TEXT, note TEXT, is_completed INTEGER DEFAULT 0, status TEXT DEFAULT 'normal', created_at TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_PACKING_ITEM + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, category TEXT, item_name TEXT, is_selected INTEGER DEFAULT 1, is_packed INTEGER DEFAULT 0, is_custom INTEGER DEFAULT 0, created_at TEXT)");
        db.execSQL("CREATE TABLE IF NOT EXISTS " + TABLE_REGRET_ITEM + " (id INTEGER PRIMARY KEY AUTOINCREMENT, trip_id INTEGER, pre_plan_id INTEGER, title TEXT, type TEXT, reason TEXT, created_at TEXT)");
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
        if (userId <= 0 || hasTripsForUser(userId)) return;
        insertSeedDataForUser(getWritableDatabase(), userId);
    }

    public boolean hasTripsForUser(int userId) {
        return intQueryWithArgs("SELECT COUNT(*) FROM " + TABLE_TRIP + " WHERE user_id=?", String.valueOf(userId)) > 0;
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
        values.put("nickname", nickname);
        return getWritableDatabase().update(TABLE_USER, values, "id=?", new String[]{String.valueOf(userId)});
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
        values.put("nickname", blankToDefault(nickname, "旅行者"));
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

    public long saveNickname(String nickname) {
        User existing = getUser();
        return existing == null ? registerUser("demo", "123456", nickname) : updateNickname(existing.getId(), nickname);
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
        return getWritableDatabase().insert(TABLE_PRE_PLAN, null, prePlanValues(plan));
    }

    public List<PrePlan> getPrePlansForTrip(int tripId) {
        List<PrePlan> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC");
        readPrePlans(cursor, list);
        return list;
    }

    public List<PrePlan> getPrePlansForTripSorted(int tripId) {
        List<PrePlan> list = new ArrayList<>();
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "CASE WHEN target_date IS NULL OR target_date='' THEN 1 ELSE 0 END, target_date ASC, time_text ASC, id ASC");
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
                            getOptionalString(cursor, "type"),
                            getOptionalString(cursor, "title"),
                            getOptionalString(cursor, "location"),
                            getOptionalString(cursor, "target_date"),
                            getOptionalString(cursor, "time_text"),
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
        return getWritableDatabase().update(TABLE_PRE_PLAN, prePlanValues(plan), "id=?", new String[]{String.valueOf(plan.getId())});
    }

    public void deletePrePlan(int id) {
        getWritableDatabase().delete(TABLE_PRE_PLAN, "id=?", new String[]{String.valueOf(id)});
    }

    public int markPrePlanCompleted(int id, boolean completed) {
        ContentValues values = new ContentValues();
        values.put("is_completed", completed ? 1 : 0);
        return getWritableDatabase().update(TABLE_PRE_PLAN, values, "id=?", new String[]{String.valueOf(id)});
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

    private ContentValues prePlanValues(PrePlan plan) {
        ContentValues values = new ContentValues();
        values.put("trip_id", plan.getTripId());
        values.put("type", plan.getType());
        values.put("title", plan.getTitle());
        values.put("location", plan.getLocation());
        values.put("target_date", plan.getTargetDate());
        values.put("time_text", plan.getTimeText());
        values.put("note", plan.getNote());
        values.put("is_completed", plan.getIsCompleted());
        values.put("status", plan.getStatus());
        values.put("created_at", plan.getCreatedAt());
        return values;
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
        int inserted = 0;
        Cursor cursor = getReadableDatabase().query(TABLE_PRE_PLAN, null, "trip_id=? AND is_completed=0", new String[]{String.valueOf(tripId)}, null, null, "id ASC");
        if (cursor != null) {
            SQLiteDatabase db = getWritableDatabase();
            if (cursor.moveToFirst()) {
                do {
                    int prePlanId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                    if (hasRegretForPrePlan(prePlanId)) continue;
                    ContentValues values = new ContentValues();
                    String type = getOptionalString(cursor, "type");
                    values.put("trip_id", tripId);
                    values.put("pre_plan_id", prePlanId);
                    values.put("title", getOptionalString(cursor, "title"));
                    values.put("type", type);
                    values.put("reason", regretReasonForType(type));
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
        return value == null || value.trim().isEmpty() ? fallback : value;
    }
}
