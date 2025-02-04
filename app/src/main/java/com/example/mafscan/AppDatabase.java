package com.example.mafscan;

import android.content.Context;

import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

@Database(
        entities = {ScanRecord.class, ScanSession.class},
        version = 3
//        autoMigrations = {
//                @AutoMigration(from = 1, to = 2)
//        }
)

public abstract class AppDatabase extends RoomDatabase {
    public abstract ScanRecordDao scanRecordDao();
    public abstract ScanSessionDao scanSessionDao();

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
                            .addMigrations(MIGRATION_1_3)
                            .build();
                }
            }
        }
        return INSTANCE;
    }

    static final Migration MIGRATION_1_3 = new Migration(1, 3) {
        @Override
        public void migrate(SupportSQLiteDatabase database) {
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
}

