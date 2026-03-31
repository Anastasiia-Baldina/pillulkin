package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.data.repository.MedicineRepository;

import java.util.List;

public class MedicineListViewModel extends AndroidViewModel {
    private final MedicineRepository repository;
    private LiveData<List<MedicineEntity>> medicines;

    public MedicineListViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
        medicines = repository.getAllMedicines();
    }

    public LiveData<List<MedicineEntity>> getMedicines() {
        return medicines;
    }

    public void setSortOrder(String order) {
        if ("expiration".equals(order)) {
            medicines = repository.getAllMedicinesSortedByExpiration();
        } else {
            medicines = repository.getAllMedicines();
        }
    }

    public void deleteMedicine(MedicineEntity medicine) {
        repository.delete(medicine);
    }

    public LiveData<List<MedicineEntity>> searchMedicines(String query) {
        return repository.searchMedicines(query);
    }
}
