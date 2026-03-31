package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.data.repository.MedicineRepository;

public class AddEditMedicineViewModel extends AndroidViewModel {
    private final MedicineRepository repository;

    public AddEditMedicineViewModel(@NonNull Application application) {
        super(application);
        repository = new MedicineRepository(application);
    }

    public LiveData<MedicineEntity> getMedicine(long id) {
        return repository.getMedicineById(id);
    }

    public void saveMedicine(MedicineEntity medicine) {
        if (medicine.getId() == 0) {
            repository.insert(medicine);
        } else {
            repository.update(medicine);
        }
    }

    public void deleteMedicine(MedicineEntity medicine) {
        repository.delete(medicine);
    }
}
