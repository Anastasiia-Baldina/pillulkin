package com.example.pillulkin.ui.doctor;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.CachedMedicine;
import com.example.pillulkin.data.local.entity.CachedProfile;
import com.example.pillulkin.data.local.entity.CachedSymptom;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.repository.DoctorAccessCodeRepository;

import java.util.ArrayList;
import java.util.List;

public class DoctorCodeEntryViewModel extends AndroidViewModel {
    private final DoctorAccessCodeRepository repository;
    private final MutableLiveData<List<PatientMedicineResponse>> medicinesData = new MutableLiveData<>();
    private final MutableLiveData<List<PatientSymptomResponse>> symptomsData = new MutableLiveData<>();
    private final MutableLiveData<DoctorFullDataResponse> patientFullData = new MutableLiveData<>();

    public DoctorCodeEntryViewModel(@NonNull Application application) {
        super(application);
        repository = new DoctorAccessCodeRepository(application);
    }

    public LiveData<Boolean> isLoading() {
        return repository.isLoading();
    }

    public LiveData<String> getError() {
        return repository.getError();
    }

    public LiveData<Boolean> getLoginSuccess() {
        return repository.getLoginSuccess();
    }

    public LiveData<DoctorFullDataResponse> getPatientData() {
        return patientFullData;
    }

    public LiveData<List<PatientMedicineResponse>> getMedicines() {
        return medicinesData;
    }

    public LiveData<List<PatientSymptomResponse>> getSymptoms() {
        return symptomsData;
    }

    public void loginWithCode(String code) {
        NetworkModule nm = NetworkModule.getInstance(getApplication());
        if (nm.validateLocalDoctorCode(code)) {
            loginAsLocalDoctor();
            return;
        }
        repository.loginAsDoctor(code);
    }

    private void loginAsLocalDoctor() {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(getApplication());
        NetworkModule nm = NetworkModule.getInstance(getApplication());
        nm.setLocalDoctorSession(true);
        nm.saveDoctorExpires(System.currentTimeMillis() + 60 * 60 * 1000L);

        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            List<CachedMedicine> cachedMeds = db.cachedMedicineDao().getMedicines(NetworkModule.LOCAL_PATIENT_ID);
            List<CachedSymptom> cachedSyms = db.cachedSymptomDao().getSymptoms(NetworkModule.LOCAL_PATIENT_ID);
            CachedProfile cachedProfile = db.cachedProfileDao().getProfile(NetworkModule.LOCAL_PATIENT_ID);

            List<PatientMedicineResponse> meds = new ArrayList<>();
            for (CachedMedicine cm : cachedMeds) {
                PatientMedicineResponse r = new PatientMedicineResponse();
                r.setId(cm.getId());
                r.setPatientId(cm.getPatientId());
                r.setMedicineId(cm.getMedicineId());
                r.setMedicineName(cm.getMedicineName());
                r.setDosage(cm.getDosage());
                r.setForm(cm.getForm());
                r.setAddedAt(cm.getAddedAt());
                r.setExpirationDate(cm.getExpirationDate());
                r.setQuantity(cm.getQuantity());
                meds.add(r);
            }

            List<PatientSymptomResponse> syms = new ArrayList<>();
            for (CachedSymptom cs : cachedSyms) {
                PatientSymptomResponse r = new PatientSymptomResponse();
                r.setId(cs.getId());
                r.setPatientId(cs.getPatientId());
                r.setSymptom(cs.getSymptom());
                r.setTimestamp(cs.getTimestamp());
                syms.add(r);
            }

            DoctorFullDataResponse fullData = new DoctorFullDataResponse();
            fullData.setPatientId(NetworkModule.LOCAL_PATIENT_ID);
            if (cachedProfile != null) {
                PatientProfileResponse profile = new PatientProfileResponse();
                profile.setPatientId(NetworkModule.LOCAL_PATIENT_ID);
                profile.setName(cachedProfile.getName());
                profile.setAge(cachedProfile.getAge());
                profile.setAllergies(cachedProfile.getAllergies());
                profile.setContraindications(cachedProfile.getContraindications());
                profile.setNotes(cachedProfile.getNotes());
                fullData.setProfile(profile);
            }
            fullData.setMedicines(meds);
            fullData.setSymptoms(syms);

            extractDataFromResponse(fullData);
            repository.notifyLoginSuccess();
        });
    }

    public void loadPatientData() {
        repository.loadPatientFullData();
        repository.getPatientData().observeForever(data -> {
            if (data != null) {
                extractDataFromResponse(data);
            }
        });
    }

    public void extractDataFromResponse(DoctorFullDataResponse data) {
        if (data != null) {
            patientFullData.postValue(data);
            medicinesData.postValue(data.getMedicines() != null ? data.getMedicines() : new ArrayList<>());
            symptomsData.postValue(data.getSymptoms() != null ? data.getSymptoms() : new ArrayList<>());
        }
    }

    public void logout() {
        NetworkModule nm = NetworkModule.getInstance(getApplication());
        nm.setLocalDoctorSession(false);
        repository.logout();
    }
}
