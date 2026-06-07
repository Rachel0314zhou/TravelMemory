package com.example.travelmemory.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.travelmemory.model.User;
import com.example.travelmemory.model.Trip;
import com.example.travelmemory.model.DayRecord;
import com.example.travelmemory.model.Photo;
import com.example.travelmemory.model.Spot;
import com.example.travelmemory.model.Expense;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "travel_memory.db";
    private static final int DATABASE_VERSION = 1;

    // Table names
    public static final String TABLE_USER = "user";
    public static final String TABLE_TRIP = "trip";
    public static final String TABLE_DAY_RECORD = "day_record";
    public static final String TABLE_PHOTO = "photo";
    public static final String TABLE_SPOT = "spot";
    public static final String TABLE_EXPENSE = "expense";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        // Create user table
        db.execSQL("CREATE TABLE " + TABLE_USER + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "nickname TEXT, " +
                "avatar_path TEXT, " +
                "created_at TEXT)");

        // Create trip table
        db.execSQL("CREATE TABLE " + TABLE_TRIP + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "name TEXT, " +
                "destination TEXT, " +
                "start_date TEXT, " +
                "end_date TEXT, " +
                "cover_path TEXT, " +
                "created_at TEXT)");

        // Create day_record table
        db.execSQL("CREATE TABLE " + TABLE_DAY_RECORD + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, " +
                "date TEXT, " +
                "note TEXT, " +
                "mood TEXT, " +
                "created_at TEXT)");

        // Create photo table
        db.execSQL("CREATE TABLE " + TABLE_PHOTO + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, " +
                "day_record_id INTEGER, " +
                "image_path TEXT, " +
                "created_at TEXT)");

        // Create spot table
        db.execSQL("CREATE TABLE " + TABLE_SPOT + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, " +
                "name TEXT, " +
                "type TEXT, " +
                "note TEXT, " +
                "is_favorite INTEGER, " +
                "created_at TEXT)");

        // Create expense table
        db.execSQL("CREATE TABLE " + TABLE_EXPENSE + " (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "trip_id INTEGER, " +
                "category TEXT, " +
                "amount REAL, " +
                "note TEXT, " +
                "date TEXT)");

        // Insert Default User
        db.execSQL("INSERT INTO " + TABLE_USER + " (nickname, avatar_path, created_at) VALUES ('阿迹', '', '2026-06-01')");

        // Insert Demo Trip
        db.execSQL("INSERT INTO " + TABLE_TRIP + " (id, name, destination, start_date, end_date, cover_path, created_at) " +
                "VALUES (1, '毕业前的厦门之旅', '厦门', '2026-06-01', '2026-06-04', '', '2026-06-01')");

        // Insert Demo Day Records
        db.execSQL("INSERT INTO " + TABLE_DAY_RECORD + " (trip_id, date, note, mood, created_at) " +
                "VALUES (1, '2026-06-01', '第一天到达厦门，海风很舒服，傍晚去了沙坡尾。看着五彩斑斓的闽南渔船，晚霞格外温柔，真的太喜欢这个城市了。', '开心 😊', '2026-06-01')");
        db.execSQL("INSERT INTO " + TABLE_DAY_RECORD + " (trip_id, date, note, mood, created_at) " +
                "VALUES (1, '2026-06-02', '今天去了鼓浪屿，拍了很多照片，也吃到了很喜欢的小吃，感觉时间过得特别快。沙滩、绿树加上古老的洋房让人流连忘返。', '兴奋 🤩', '2026-06-02')");
        db.execSQL("INSERT INTO " + TABLE_DAY_RECORD + " (trip_id, date, note, mood, created_at) " +
                "VALUES (1, '2026-06-03', '沿着海边慢慢散步，吹着咸咸的海风，感觉时间都变慢了。坐在环岛路的礁石上听海浪起伏，心里十分安宁。', '平静 😐', '2026-06-03')");

        // Insert Demo Spots
        db.execSQL("INSERT INTO " + TABLE_SPOT + " (trip_id, name, type, note, is_favorite, created_at) " +
                "VALUES (1, '鼓浪屿', '景点', '红砖洋房和优美海景，推荐小吃！', 1, '2026-06-02')");
        db.execSQL("INSERT INTO " + TABLE_SPOT + " (trip_id, name, type, note, is_favorite, created_at) " +
                "VALUES (1, '沙坡尾', '景点', '文艺避风港，傍晚晚霞倒影超美。', 0, '2026-06-01')");
        db.execSQL("INSERT INTO " + TABLE_SPOT + " (trip_id, name, type, note, is_favorite, created_at) " +
                "VALUES (1, '曾厝垵', '餐饮', '小吃街人超级多，热闹非凡！', 0, '2026-06-02')");

        // Insert Demo Expenses
        db.execSQL("INSERT INTO " + TABLE_EXPENSE + " (trip_id, category, amount, note, date) VALUES (1, '餐饮', 168.5, '沙坡尾海鲜与网红奶茶', '2026-06-01')");
        db.execSQL("INSERT INTO " + TABLE_EXPENSE + " (trip_id, category, amount, note, date) VALUES (1, '交通', 86.0, '打车和轮渡往返船票', '2026-06-02')");
        db.execSQL("INSERT INTO " + TABLE_EXPENSE + " (trip_id, category, amount, note, date) VALUES (1, '住宿', 520.0, '曾厝垵海景温馨民宿', '2026-06-01')");
        db.execSQL("INSERT INTO " + TABLE_EXPENSE + " (trip_id, category, amount, note, date) VALUES (1, '门票', 80.0, '日光岩门票', '2026-06-02')");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TRIP);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DAY_RECORD);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PHOTO);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SPOT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EXPENSE);
        onCreate(db);
    }

    // ==================== USER Operations ====================

    public User getUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_USER, null, null, null, null, null, "id DESC", "1");
        User user = null;
        if (cursor != null && cursor.moveToFirst()) {
            user = new User(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("nickname")),
                cursor.getString(cursor.getColumnIndexOrThrow("avatar_path")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            );
            cursor.close();
        }
        return user;
    }

    public long saveNickname(String nickname) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("nickname", nickname);
        values.put("created_at", "2026-06-07");

        User existing = getUser();
        if (existing == null) {
            return db.insert(TABLE_USER, null, values);
        } else {
            return db.update(TABLE_USER, values, "id=?", new String[]{String.valueOf(existing.getId())});
        }
    }

    // ==================== TRIP Operations ====================

    public long addTrip(Trip trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", trip.getName());
        values.put("destination", trip.getDestination());
        values.put("start_date", trip.getStartDate());
        values.put("end_date", trip.getEndDate());
        values.put("cover_path", trip.getCoverPath());
        values.put("created_at", trip.getCreatedAt());
        return db.insert(TABLE_TRIP, null, values);
    }

    public List<Trip> getAllTrips() {
        List<Trip> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRIP, null, null, null, null, null, "id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Trip trip = new Trip(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("destination")),
                        cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("cover_path")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    list.add(trip);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public Trip getTripById(int id) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_TRIP, null, "id=?", new String[]{String.valueOf(id)}, null, null, null);
        Trip trip = null;
        if (cursor != null && cursor.moveToFirst()) {
            trip = new Trip(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getString(cursor.getColumnIndexOrThrow("name")),
                cursor.getString(cursor.getColumnIndexOrThrow("destination")),
                cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                cursor.getString(cursor.getColumnIndexOrThrow("cover_path")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            );
            cursor.close();
        }
        return trip;
    }

    public int updateTrip(Trip trip) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("name", trip.getName());
        values.put("destination", trip.getDestination());
        values.put("start_date", trip.getStartDate());
        values.put("end_date", trip.getEndDate());
        values.put("cover_path", trip.getCoverPath());
        return db.update(TABLE_TRIP, values, "id=?", new String[]{String.valueOf(trip.getId())});
    }

    public void deleteTrip(int tripId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TRIP, "id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_DAY_RECORD, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_PHOTO, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_SPOT, "trip_id=?", new String[]{String.valueOf(tripId)});
        db.delete(TABLE_EXPENSE, "trip_id=?", new String[]{String.valueOf(tripId)});
    }

    // ==================== DAY RECORD Operations ====================

    public long addOrUpdateDayRecord(DayRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("trip_id", record.getTripId());
        values.put("date", record.getDate());
        values.put("note", record.getNote());
        values.put("mood", record.getMood());
        values.put("created_at", record.getCreatedAt());

        // Check if there is already a record for this trip and this date
        Cursor cursor = db.query(TABLE_DAY_RECORD, new String[]{"id"}, "trip_id=? AND date=?",
                new String[]{String.valueOf(record.getTripId()), record.getDate()}, null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            int existingId = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
            cursor.close();
            return db.update(TABLE_DAY_RECORD, values, "id=?", new String[]{String.valueOf(existingId)});
        }
        if (cursor != null) {
            cursor.close();
        }
        return db.insert(TABLE_DAY_RECORD, null, values);
    }

    public List<DayRecord> getDayRecordsForTrip(int tripId) {
        List<DayRecord> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAY_RECORD, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date ASC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    DayRecord record = new DayRecord(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getString(cursor.getColumnIndexOrThrow("mood")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    list.add(record);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public DayRecord getDayRecordById(int recordId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAY_RECORD, null, "id=?", new String[]{String.valueOf(recordId)}, null, null, null);
        DayRecord record = null;
        if (cursor != null && cursor.moveToFirst()) {
            record = new DayRecord(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("date")),
                cursor.getString(cursor.getColumnIndexOrThrow("note")),
                cursor.getString(cursor.getColumnIndexOrThrow("mood")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            );
            cursor.close();
        }
        return record;
    }

    public DayRecord getDayRecordByTripAndDate(int tripId, String date) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_DAY_RECORD, null, "trip_id=? AND date=?", new String[]{String.valueOf(tripId), date}, null, null, null);
        DayRecord record = null;
        if (cursor != null && cursor.moveToFirst()) {
            record = new DayRecord(
                cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                cursor.getString(cursor.getColumnIndexOrThrow("date")),
                cursor.getString(cursor.getColumnIndexOrThrow("note")),
                cursor.getString(cursor.getColumnIndexOrThrow("mood")),
                cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
            );
            cursor.close();
        }
        return record;
    }

    public void deleteDayRecord(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_DAY_RECORD, "id=?", new String[]{String.valueOf(id)});
    }

    // ==================== PHOTO Operations ====================

    public long addPhoto(Photo photo) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("trip_id", photo.getTripId());
        values.put("day_record_id", photo.getDayRecordId());
        values.put("image_path", photo.getImagePath());
        values.put("created_at", photo.getCreatedAt());
        return db.insert(TABLE_PHOTO, null, values);
    }

    public List<Photo> getPhotosForTrip(int tripId) {
        List<Photo> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PHOTO, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Photo photo = new Photo(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("day_record_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("image_path")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    list.add(photo);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public void deletePhoto(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_PHOTO, "id=?", new String[]{String.valueOf(id)});
    }

    // ==================== SPOT Operations ====================

    public long addSpot(Spot spot) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("trip_id", spot.getTripId());
        values.put("name", spot.getName());
        values.put("type", spot.getType());
        values.put("note", spot.getNote());
        values.put("is_favorite", spot.getIsFavorite());
        values.put("created_at", spot.getCreatedAt());
        return db.insert(TABLE_SPOT, null, values);
    }

    public List<Spot> getSpotsForTrip(int tripId) {
        List<Spot> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_SPOT, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Spot spot = new Spot(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("type")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("is_favorite")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    list.add(spot);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public int updateSpotFavoriteStatus(int spotId, int isFavorite) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("is_favorite", isFavorite);
        return db.update(TABLE_SPOT, values, "id=?", new String[]{String.valueOf(spotId)});
    }

    public void deleteSpot(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SPOT, "id=?", new String[]{String.valueOf(id)});
    }

    // ==================== EXPENSE Operations ====================

    public long addExpense(Expense expense) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("trip_id", expense.getTripId());
        values.put("category", expense.getCategory());
        values.put("amount", expense.getAmount());
        values.put("note", expense.getNote());
        values.put("date", expense.getDate());
        return db.insert(TABLE_EXPENSE, null, values);
    }

    public List<Expense> getExpensesForTrip(int tripId) {
        List<Expense> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_EXPENSE, null, "trip_id=?", new String[]{String.valueOf(tripId)}, null, null, "date DESC, id DESC");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Expense exp = new Expense(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getInt(cursor.getColumnIndexOrThrow("trip_id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("category")),
                        cursor.getDouble(cursor.getColumnIndexOrThrow("amount")),
                        cursor.getString(cursor.getColumnIndexOrThrow("note")),
                        cursor.getString(cursor.getColumnIndexOrThrow("date"))
                    );
                    list.add(exp);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public void deleteExpense(int id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_EXPENSE, "id=?", new String[]{String.valueOf(id)});
    }

    // ==================== STATISTICS & ANALYTICS ====================

    public int countPhotosForTrip(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PHOTO + " WHERE trip_id=?", new String[]{String.valueOf(tripId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public int countSpotsForTrip(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SPOT + " WHERE trip_id=?", new String[]{String.valueOf(tripId)});
        int count = 0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        return count;
    }

    public double getTotalExpenseForTrip(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE trip_id=?", new String[]{String.valueOf(tripId)});
        double total = 0.0;
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                total = cursor.getDouble(0);
            }
            cursor.close();
        }
        return total;
    }

    public String getHighestExpenseCategory(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String cat = "";
        Cursor cursor = db.rawQuery(
                "SELECT category, SUM(amount) as s FROM " + TABLE_EXPENSE +
                " WHERE trip_id=? GROUP BY category ORDER BY s DESC LIMIT 1",
                new String[]{String.valueOf(tripId)}
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                cat = cursor.getString(0);
            }
            cursor.close();
        }
        return cat;
    }

    public double getCategoryExpenseAmount(int tripId, String category) {
        SQLiteDatabase db = this.getReadableDatabase();
        double amount = 0.0;
        Cursor cursor = db.rawQuery("SELECT SUM(amount) FROM " + TABLE_EXPENSE + " WHERE trip_id=? AND category=?",
                new String[]{String.valueOf(tripId), category});
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                amount = cursor.getDouble(0);
            }
            cursor.close();
        }
        return amount;
    }

    public String getMostFrequentMood(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String mood = "开心 😊"; // default
        Cursor cursor = db.rawQuery(
                "SELECT mood, COUNT(mood) as cnt FROM " + TABLE_DAY_RECORD +
                " WHERE trip_id=? GROUP BY mood ORDER BY cnt DESC LIMIT 1",
                new String[]{String.valueOf(tripId)}
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                mood = cursor.getString(0);
            }
            cursor.close();
        }
        return mood;
    }

    public String getFavoriteSpot(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String spotName = "";
        Cursor cursor = db.query(TABLE_SPOT, new String[]{"name"}, "trip_id=? AND is_favorite=1",
                new String[]{String.valueOf(tripId)}, null, null, "id ASC", "1");
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                spotName = cursor.getString(0);
            }
            cursor.close();
        }
        if (spotName.isEmpty()) {
            Cursor anyCursor = db.query(TABLE_SPOT, new String[]{"name"}, "trip_id=?",
                    new String[]{String.valueOf(tripId)}, null, null, "id ASC", "1");
            if (anyCursor != null) {
                if (anyCursor.moveToFirst()) {
                    spotName = anyCursor.getString(0);
                }
                anyCursor.close();
            }
        }
        return spotName;
    }

    public String getLongestNote(int tripId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String longestNote = "";
        Cursor cursor = db.rawQuery(
                "SELECT note FROM " + TABLE_DAY_RECORD +
                " WHERE trip_id=? ORDER BY LENGTH(note) DESC LIMIT 1",
                new String[]{String.valueOf(tripId)}
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                longestNote = cursor.getString(0);
            }
            cursor.close();
        }
        return longestNote;
    }

    // ==================== SEARCH & FILTERS ====================

    public List<Trip> searchTrips(String text) {
        List<Trip> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "%" + text + "%";
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT t.* FROM " + TABLE_TRIP + " t " +
                "LEFT JOIN " + TABLE_DAY_RECORD + " d ON t.id = d.trip_id " +
                "WHERE t.name LIKE ? OR t.destination LIKE ? OR d.note LIKE ? " +
                "ORDER BY t.id DESC",
                new String[]{query, query, query}
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Trip trip = new Trip(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("destination")),
                        cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("cover_path")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    list.add(trip);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }

    public List<Trip> filterTripsByMood(String mood) {
        List<Trip> list = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(
                "SELECT DISTINCT t.* FROM " + TABLE_TRIP + " t " +
                "INNER JOIN " + TABLE_DAY_RECORD + " d ON t.id = d.trip_id " +
                "WHERE d.mood = ? " +
                "ORDER BY t.id DESC",
                new String[]{mood}
        );
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    Trip trip = new Trip(
                        cursor.getInt(cursor.getColumnIndexOrThrow("id")),
                        cursor.getString(cursor.getColumnIndexOrThrow("name")),
                        cursor.getString(cursor.getColumnIndexOrThrow("destination")),
                        cursor.getString(cursor.getColumnIndexOrThrow("start_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("end_date")),
                        cursor.getString(cursor.getColumnIndexOrThrow("cover_path")),
                        cursor.getString(cursor.getColumnIndexOrThrow("created_at"))
                    );
                    list.add(trip);
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return list;
    }
}
