package com.example.pillulkin.sync;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.PendingOperation;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientProfileRequest;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SyncManager {

    private static final int MAX_RETRIES = 3;

    private final Context context;
    private final PillulkinDatabase db;
    private final NetworkModule networkModule;
    private boolean isSyncing = false;

    public SyncManager(Context context) {
        this.context = context.getApplicationContext();
        this.db = PillulkinDatabase.getDatabase(context);
        this.networkModule = NetworkModule.getInstance(context);
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) return false;
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnectedOrConnecting();
    }

    public void syncAll() {
        if (isSyncing || !isOnline() || !networkModule.isPatientLoggedIn()) return;

        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            List<PendingOperation> pending = db.pendingOperationDao().getPending();
            if (pending.isEmpty()) return;

            isSyncing = true;
            for (PendingOperation op : pending) {
                executeOperation(op);
            }
            isSyncing = false;
        });
    }

    private void executeOperation(PendingOperation op) {
        if (op.getRetryCount() >= MAX_RETRIES) {
            op.setStatus(PendingOperation.STATUS_FAILED);
            db.pendingOperationDao().update(op);
            return;
        }

        op.setStatus(PendingOperation.STATUS_SYNCING);
        op.setRetryCount(op.getRetryCount() + 1);
        db.pendingOperationDao().update(op);

        try {
            boolean success = executeByType(op);
            if (success) {
                db.pendingOperationDao().deleteById(op.getId());
            } else {
                op.setStatus(PendingOperation.STATUS_PENDING);
                db.pendingOperationDao().update(op);
            }
        } catch (Exception e) {
            op.setStatus(PendingOperation.STATUS_PENDING);
            db.pendingOperationDao().update(op);
        }
    }

    private boolean executeByType(PendingOperation op) {
        switch (op.getType()) {
            case PendingOperation.TYPE_PROFILE_UPDATE:
                return syncProfileUpdate(op);
            case PendingOperation.TYPE_SYMPTOM_ADD:
                return syncSymptomAdd(op);
            case PendingOperation.TYPE_SYMPTOM_DELETE:
                return syncSymptomDelete(op);
            case PendingOperation.TYPE_MEDICINE_ADD:
                return syncMedicineAdd(op);
            case PendingOperation.TYPE_MEDICINE_DELETE:
                return syncMedicineDelete(op);
            default:
                db.pendingOperationDao().deleteById(op.getId());
                return true;
        }
    }

    private boolean syncProfileUpdate(PendingOperation op) {
        try {
            String[] parts = op.getPayload().split("\\|", -1);
            PatientProfileRequest request = new PatientProfileRequest(
                    nullIfEmpty(parts[0]),
                    parts.length > 1 && !parts[1].isEmpty() ? Integer.parseInt(parts[1]) : null,
                    nullIfEmpty(parts.length > 2 ? parts[2] : ""),
                    nullIfEmpty(parts.length > 3 ? parts[3] : ""),
                    nullIfEmpty(parts.length > 4 ? parts[4] : "")
            );

            Response<PatientProfileResponse> response = networkModule.getApi().updateProfile(
                    networkModule.getPatientId(), request).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean syncSymptomAdd(PendingOperation op) {
        try {
            String symptom = op.getPayload();
            Response<PatientSymptomResponse> response = networkModule.getApi().addSymptom(
                    networkModule.getPatientId(),
                    new com.example.pillulkin.data.remote.model.PatientSymptomRequest(symptom)
            ).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean syncSymptomDelete(PendingOperation op) {
        try {
            long symptomId = Long.parseLong(op.getPayload());
            Response<Void> response = networkModule.getApi().deleteSymptom(
                    networkModule.getPatientId(), symptomId).execute();
            return response.isSuccessful() || response.code() == 404;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean syncMedicineAdd(PendingOperation op) {
        try {
            long medicineId = Long.parseLong(op.getPayload());
            Response<?> response = networkModule.getApi().addMedicine(
                    networkModule.getPatientId(),
                    new com.example.pillulkin.data.remote.model.PatientMedicineRequest(medicineId, null, null)
            ).execute();
            return response.isSuccessful();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean syncMedicineDelete(PendingOperation op) {
        try {
            long medicineId = Long.parseLong(op.getPayload());
            Response<Void> response = networkModule.getApi().deleteMedicine(
                    networkModule.getPatientId(), medicineId).execute();
            return response.isSuccessful() || response.code() == 404;
        } catch (Exception e) {
            return false;
        }
    }

    private String nullIfEmpty(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }

    public int getPendingCount() {
        return db.pendingOperationDao().getPendingCount();
    }

    public boolean isSyncing() {
        return isSyncing;
    }
}
