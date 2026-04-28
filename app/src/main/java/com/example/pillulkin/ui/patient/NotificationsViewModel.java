package com.example.pillulkin.ui.patient;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.ReminderDao;
import com.example.pillulkin.data.local.entity.Reminder;
import com.example.pillulkin.data.remote.NetworkModule;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.notifications.ReminderScheduler;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class NotificationsViewModel extends AndroidViewModel {

    private final ReminderDao reminderDao;
    private final LiveData<List<Reminder>> reminders;
    private final MutableLiveData<List<PatientMedicineResponse>> medicines = new MutableLiveData<>();

    public NotificationsViewModel(@NonNull Application application) {
        super(application);
        reminderDao = PillulkinDatabase.getDatabase(application).reminderDao();
        reminders = reminderDao.getAll();
        loadMedicines();
    }

    public LiveData<List<Reminder>> getReminders() {
        return reminders;
    }

    public LiveData<List<PatientMedicineResponse>> getMedicines() {
        return medicines;
    }

    private void loadMedicines() {
        NetworkModule nm = NetworkModule.getInstance(getApplication());
        if (!nm.isPatientLoggedIn()) return;
        nm.getMedicines().enqueue(new Callback<List<PatientMedicineResponse>>() {
            @Override
            public void onResponse(Call<List<PatientMedicineResponse>> call, Response<List<PatientMedicineResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    medicines.postValue(response.body());
                }
            }

            @Override
            public void onFailure(Call<List<PatientMedicineResponse>> call, Throwable t) {}
        });
    }

    public void addReminder(int hour, int minute, String medicineName, String customText) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            Reminder reminder = new Reminder();
            reminder.setHour(hour);
            reminder.setMinute(minute);
            reminder.setMedicineName(medicineName);
            reminder.setCustomText(customText);
            reminder.setEnabled(true);
            long id = reminderDao.insert(reminder);
            reminder.setId((int) id);
            ReminderScheduler.schedule(getApplication(), reminder);
        });
    }

    public void updateReminder(Reminder reminder, int hour, int minute, String medicineName, String customText) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            ReminderScheduler.cancel(getApplication(), reminder);
            reminder.setHour(hour);
            reminder.setMinute(minute);
            reminder.setMedicineName(medicineName);
            reminder.setCustomText(customText);
            reminderDao.update(reminder);
            ReminderScheduler.schedule(getApplication(), reminder);
        });
    }

    public void toggleReminder(Reminder reminder) {
        reminder.setEnabled(!reminder.isEnabled());
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            reminderDao.update(reminder);
            ReminderScheduler.reschedule(getApplication(), reminder);
        });
    }

    public void deleteReminder(Reminder reminder) {
        PillulkinDatabase.databaseWriteExecutor.execute(() -> {
            ReminderScheduler.cancel(getApplication(), reminder);
            reminderDao.delete(reminder);
        });
    }
}
