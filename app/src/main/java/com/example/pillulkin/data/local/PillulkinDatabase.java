package com.example.pillulkin.data.local;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.example.pillulkin.data.local.dao.AnalogueDictionaryDao;
import com.example.pillulkin.data.local.dao.DiagnosisMappingDao;
import com.example.pillulkin.data.local.dao.DoctorAccessCodeDao;
import com.example.pillulkin.data.local.dao.MedicineDao;
import com.example.pillulkin.data.local.dao.PatientProfileDao;
import com.example.pillulkin.data.local.dao.SymptomDao;
import com.example.pillulkin.data.local.entity.AnalogueDictionaryEntryEntity;
import com.example.pillulkin.data.local.entity.DiagnosisMappingEntryEntity;
import com.example.pillulkin.data.local.entity.DoctorAccessCodeEntity;
import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.data.local.entity.PatientProfileEntity;
import com.example.pillulkin.data.local.entity.SymptomEntity;

import java.util.Arrays;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Database(
    entities = {
        PatientProfileEntity.class,
        MedicineEntity.class,
        DoctorAccessCodeEntity.class,
        AnalogueDictionaryEntryEntity.class,
        DiagnosisMappingEntryEntity.class,
        SymptomEntity.class
    },
    version = 2,
    exportSchema = false
)
public abstract class PillulkinDatabase extends RoomDatabase {
    public abstract PatientProfileDao patientProfileDao();
    public abstract MedicineDao medicineDao();
    public abstract DoctorAccessCodeDao doctorAccessCodeDao();
    public abstract AnalogueDictionaryDao analogueDictionaryDao();
    public abstract DiagnosisMappingDao diagnosisMappingDao();
    public abstract SymptomDao symptomDao();

    private static volatile PillulkinDatabase INSTANCE;
    private static final int NUMBER_OF_THREADS = 4;
    public static final ExecutorService databaseWriteExecutor = Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    static final Migration MIGRATION_1_2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL(
                "CREATE TABLE IF NOT EXISTS `symptoms` (" +
                "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                "`description` TEXT, " +
                "`timestamp` INTEGER NOT NULL)"
            );
        }
    };

    public static PillulkinDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (PillulkinDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(
                        context.getApplicationContext(),
                        PillulkinDatabase.class,
                        "pillulkin_database"
                    )
                    .allowMainThreadQueries()
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(new RoomDatabase.Callback() {
                        @Override
                        public void onCreate(@NonNull SupportSQLiteDatabase db) {
                            super.onCreate(db);
                            databaseWriteExecutor.execute(() -> {
                                PillulkinDatabase database = INSTANCE;
                                if (database != null) {
                                    SeedData.populate(database);
                                }
                            });
                        }
                    })
                    .build();
                }
            }
        }
        return INSTANCE;
    }

    static class SeedData {
        static void populate(PillulkinDatabase database) {
            AnalogueDictionaryDao analogueDao = database.analogueDictionaryDao();
            DiagnosisMappingDao diagnosisDao = database.diagnosisMappingDao();

            analogueDao.insertAll(Arrays.asList(
                new AnalogueDictionaryEntryEntity("Парацетамол", "Панадол", "Парацетамол"),
                new AnalogueDictionaryEntryEntity("Парацетамол", "Эффералган", "Парацетамол"),
                new AnalogueDictionaryEntryEntity("Парацетамол", "Тайленол", "Парацетамол"),
                new AnalogueDictionaryEntryEntity("Ибупрофен", "Нурофен", "Ибупрофен"),
                new AnalogueDictionaryEntryEntity("Ибупрофен", "Миг", "Ибупрофен"),
                new AnalogueDictionaryEntryEntity("Ибупрофен", "Ибуфен", "Ибупрофен"),
                new AnalogueDictionaryEntryEntity("Аспирин", "Ацетилсалициловая кислота", "Ацетилсалициловая кислота"),
                new AnalogueDictionaryEntryEntity("Аспирин", "Упсарин", "Ацетилсалициловая кислота"),
                new AnalogueDictionaryEntryEntity("Но-шпа", "Дротаверин", "Дротаверин"),
                new AnalogueDictionaryEntryEntity("Но-шпа", "Спазмол", "Дротаверин"),
                new AnalogueDictionaryEntryEntity("Мезим", "Панкреатин", "Панкреатин"),
                new AnalogueDictionaryEntryEntity("Мезим", "Креон", "Панкреатин"),
                new AnalogueDictionaryEntryEntity("Лоперамид", "Имодиум", "Лоперамид"),
                new AnalogueDictionaryEntryEntity("Лоперамид", "Лопедиум", "Лоперамид"),
                new AnalogueDictionaryEntryEntity("Энтеросгель", "Полисорб", "Сорбент"),
                new AnalogueDictionaryEntryEntity("Цетиризин", "Зиртек", "Цетиризин"),
                new AnalogueDictionaryEntryEntity("Цетиризин", "Зодак", "Цетиризин"),
                new AnalogueDictionaryEntryEntity("Лоратадин", "Кларитин", "Лоратадин"),
                new AnalogueDictionaryEntryEntity("Лоратадин", "Лорагексал", "Лоратадин"),
                new AnalogueDictionaryEntryEntity("Амброксол", "Лазолван", "Амброксол"),
                new AnalogueDictionaryEntryEntity("Амброксол", "Амбробене", "Амброксол")
            ));

            diagnosisDao.insertAll(Arrays.asList(
                new DiagnosisMappingEntryEntity("Простуда", "Парацетамол", "Парацетамол"),
                new DiagnosisMappingEntryEntity("Простуда", "Ибупрофен", "Ибупрофен"),
                new DiagnosisMappingEntryEntity("Грипп", "Парацетамол", "Парацетамол"),
                new DiagnosisMappingEntryEntity("Грипп", "Ибупрофен", "Ибупрофен"),
                new DiagnosisMappingEntryEntity("Головная боль", "Парацетамол", "Парацетамол"),
                new DiagnosisMappingEntryEntity("Головная боль", "Ибупрофен", "Ибупрофен"),
                new DiagnosisMappingEntryEntity("Головная боль", "Аспирин", "Ацетилсалициловая кислота"),
                new DiagnosisMappingEntryEntity("Мигрень", "Ибупрофен", "Ибупрофен"),
                new DiagnosisMappingEntryEntity("Температура", "Парацетамол", "Парацетамол"),
                new DiagnosisMappingEntryEntity("Температура", "Ибупрофен", "Ибупрофен"),
                new DiagnosisMappingEntryEntity("Спазмы", "Но-шпа", "Дротаверин"),
                new DiagnosisMappingEntryEntity("Боль в животе", "Но-шпа", "Дротаверин"),
                new DiagnosisMappingEntryEntity("Боль в животе", "Мезим", "Панкреатин"),
                new DiagnosisMappingEntryEntity("Аллергия", "Цетиризин", "Цетиризин"),
                new DiagnosisMappingEntryEntity("Аллергия", "Лоратадин", "Лоратадин"),
                new DiagnosisMappingEntryEntity("Насморк", "Лоратадин", "Лоратадин"),
                new DiagnosisMappingEntryEntity("Кашель", "Амброксол", "Амброксол"),
                new DiagnosisMappingEntryEntity("Диарея", "Лоперамид", "Лоперамид"),
                new DiagnosisMappingEntryEntity("Диарея", "Энтеросгель", "Сорбент"),
                new DiagnosisMappingEntryEntity("Отравление", "Энтеросгель", "Сорбент"),
                new DiagnosisMappingEntryEntity("Тошнота", "Мезим", "Панкреатин")
            ));
        }
    }
}
