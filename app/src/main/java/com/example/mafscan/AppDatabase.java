package com.example.mafscan;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
        entities = {ScanRecord.class, ScanSession.class},
        version = 4
//        autoMigrations = {
//                @AutoMigration(from = 1, to = 2)
//        }
)

public abstract class AppDatabase extends RoomDatabase {
    public abstract ScanRecordDao scanRecordDao();
    public abstract ScanSessionDao scanSessionDao();
    public abstract FailedOrSavedScanDao failedOrSavedScanDao();

    private static volatile AppDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "mafscan_database"
                            )
                            .addMigrations(MIGRATION_1_3, MIGRATION_3_4)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // Created the new ScanSession table
            database.execSQL("CREATE TABLE IF NOT EXISTS `ScanSession` (`sessionId` TEXT NOT NULL," +
                    " `sessionType` TEXT, `sessionNumber` TEXT, `sessionCreationDate` TEXT," +
                    " `sessionCreationBy` TEXT, `sessionUpdatedDate` TEXT, `sessionUpdatedBy` TEXT," +
                    " PRIMARY KEY(`sessionId`))");

            // Create the ScanRecord table with all columns and constraints
            database.execSQL("CREATE TABLE IF NOT EXISTS `ScanRecord` (`id` INTEGER" +
                    " PRIMARY KEY AUTOINCREMENT NOT NULL, `scannedData` TEXT NOT NULL," +
                    " `codeType` TEXT NOT NULL, `scanCount` REAL NOT NULL, `scanDate` TEXT," +
                    " `deviceSerialNumber` TEXT, `fromLocationId` TEXT, `toLocationId` TEXT," +
                    " `fromLocationName` TEXT, `toLocationName` TEXT, `fromLocationCode` TEXT," +
                    " `toLocationCode` TEXT, `saveType` INTEGER NOT NULL," +
                    " `isSentToServer` INTEGER NOT NULL DEFAULT 0," +
                    " `sendAttemptCount` INTEGER NOT NULL DEFAULT 0, `sessionId` TEXT," +
                    " `scanUser` TEXT DEFAULT 'Not defined', `lastSendAttemptDate` TEXT," +
                    " `sendError` TEXT)");

            // Create the indexes
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ScanRecord_sessionId` ON " +
                    "`ScanRecord` (`sessionId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ScanRecord_fromLocationId_toLocationId`" +
                    " ON `ScanRecord` (`fromLocationId`, `toLocationId`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ScanRecord_isSentToServer` ON" +
                    " `ScanRecord` (`isSentToServer`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ScanRecord_scanDate` ON " +
                    "`ScanRecord` (`scanDate`)");
            database.execSQL("CREATE INDEX IF NOT EXISTS `index_ScanRecord_scannedData` ON " +
                    "`ScanRecord` (`scannedData`)");
        }
    };

    static final Migration MIGRATION_3_4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            // No SQL statements needed for this migration
        }
    };
}

