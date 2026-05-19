package com.example.pillulkin.sync;

import android.content.Context;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedMedicine;
import com.example.pillulkin.data.local.entity.CachedProfile;
import com.example.pillulkin.data.local.entity.CachedSymptom;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientMedicineRequest;
import com.example.pillulkin.data.remote.model.PatientProfileRequest;
import com.example.pillulkin.data.remote.model.PatientSymptomRequest;

import java.util.List;

import retrofit2.Response;

public class LocalSyncHelper {

    public static void syncLocalToServer(Context context, long realPatientId) {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(context);
        NetworkModule networkModule = NetworkModule.getInstance(context);

        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            try {
                List<CachedMedicine> localMedicines = db.cachedMedicineDao().getMedicines(NetworkModule.LOCAL_PATIENT_ID);
                for (CachedMedicine med : localMedicines) {
                    if (med.getMedicineId() != null) {
                        PatientMedicineRequest request = new PatientMedicineRequest(
                                med.getMedicineId(), med.getExpirationDate(), med.getQuantity());
                        try {
                            networkModule.getApi().addMedicine(realPatientId, request).execute();
                        } catch (Exception ignored) {}
                    } else {
                        try {
                            networkModule.getApi().addCustomMedicine(realPatientId,
                                    new com.example.pillulkin.data.remote.model.CustomMedicineRequest(
                                            med.getMedicineName(), med.getDosage(), med.getForm(),
                                            med.getActiveSubstance(), null, null,
                                            med.getExpirationDate(), med.getQuantity())).execute();
                        } catch (Exception ignored) {}
                    }
                }

                List<CachedSymptom> localSymptoms = db.cachedSymptomDao().getSymptoms(NetworkModule.LOCAL_PATIENT_ID);
                for (CachedSymptom sym : localSymptoms) {
                    try {
                        networkModule.getApi().addSymptom(realPatientId,
                                new PatientSymptomRequest(sym.getSymptom())).execute();
                    } catch (Exception ignored) {}
                }

                CachedProfile localProfile = db.cachedProfileDao().getProfile(NetworkModule.LOCAL_PATIENT_ID);
                if (localProfile != null) {
                    PatientProfileRequest request = new PatientProfileRequest(
                            localProfile.getName(), localProfile.getAge(),
                            localProfile.getAllergies(), localProfile.getContraindications(),
                            localProfile.getNotes());
                    try {
                        networkModule.getApi().updateProfile(realPatientId, request).execute();
                    } catch (Exception ignored) {}
                }

                db.cachedMedicineDao().deleteByPatientId(NetworkModule.LOCAL_PATIENT_ID);
                db.cachedSymptomDao().deleteByPatientId(NetworkModule.LOCAL_PATIENT_ID);

                if (localProfile != null) {
                    localProfile.setPatientId(realPatientId);
                    localProfile.setCachedAt(System.currentTimeMillis());
                    db.cachedProfileDao().insert(localProfile);
                }

                networkModule.clearLocalMode();
            } catch (Exception ignored) {}
        });
    }
}
