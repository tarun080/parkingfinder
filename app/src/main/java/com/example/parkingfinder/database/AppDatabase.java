package com.example.parkingfinder.database;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.parkingfinder.database.dao.BookingDao;
import com.example.parkingfinder.database.dao.ParkingAreaDao;
import com.example.parkingfinder.database.dao.ParkingSpotDao;
import com.example.parkingfinder.database.dao.UserDao;
import com.example.parkingfinder.database.entities.BookingEntity;
import com.example.parkingfinder.database.entities.ParkingAreaEntity;
import com.example.parkingfinder.database.entities.ParkingSpotEntity;
import com.example.parkingfinder.database.entities.UserEntity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(entities = {
        UserEntity.class,
        ParkingAreaEntity.class,
        ParkingSpotEntity.class,
        BookingEntity.class
}, version = 1, exportSchema = false)
public abstract class AppDatabase extends RoomDatabase {

    // DAOs
    public abstract UserDao userDao();
    public abstract ParkingAreaDao parkingAreaDao();
    public abstract ParkingSpotDao parkingSpotDao();
    public abstract BookingDao bookingDao();

    // Singleton instance
    private static volatile AppDatabase INSTANCE;

    // Thread pool for database operations
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    // Get database instance
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "parking_finder_database")
                            .addCallback(sRoomDatabaseCallback)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    // Callback for database creation
    private static final RoomDatabase.Callback sRoomDatabaseCallback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            // If you want to prepopulate the database, you can do it here
            databaseWriteExecutor.execute(() -> {
                // Example of prepopulating the database with sample data
                // This would be done in a real app if you wanted to have initial data

                // ParkingAreaDao dao = INSTANCE.parkingAreaDao();
                // dao.insert(new ParkingAreaEntity(...));
            });
        }
    };

    // Clear the database instance (for testing or when signing out)
    public static void destroyInstance() {
        INSTANCE = null;
    }
}