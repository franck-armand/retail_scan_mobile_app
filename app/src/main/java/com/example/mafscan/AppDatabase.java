package com.example.mafscan;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(
        entities = {ScanRecord.class},
        version = 1,
        exportSchema = true,
        autoMigrations = {
                @AutoMigration(from = 1, to = 2)
        }
)

public abstract class AppDatabase extends RoomDatabase {
    public abstract ScanRecordDao scanRecordDao();
    private static volatile AppDatabase INSTANCE;

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mafscan_database"
                            )
                            .addMigrations()
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}

