package com.example.pillulkin.data.repository;

import android.app.Application;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.dao.AnalogueDictionaryDao;
import com.example.pillulkin.data.local.dao.DiagnosisMappingDao;
import com.example.pillulkin.data.local.dao.MedicineDao;
import com.example.pillulkin.data.local.entity.AnalogueDictionaryEntryEntity;
import com.example.pillulkin.data.local.entity.DiagnosisMappingEntryEntity;
import com.example.pillulkin.data.local.entity.MedicineEntity;
import com.example.pillulkin.data.local.entity.PatientProfileEntity;
import com.example.pillulkin.domain.model.RecommendationItem;
import com.example.pillulkin.domain.model.RecommendationResult;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class RecommendationRepository {
    private final MedicineDao medicineDao;
    private final AnalogueDictionaryDao analogueDao;
    private final DiagnosisMappingDao diagnosisDao;
    private final PatientProfileRepository profileRepository;
    private final SimpleDateFormat dateFormat;

    public RecommendationRepository(Application application) {
        PillulkinDatabase db = PillulkinDatabase.getDatabase(application);
        medicineDao = db.medicineDao();
        analogueDao = db.analogueDictionaryDao();
        diagnosisDao = db.diagnosisMappingDao();
        profileRepository = new PatientProfileRepository(application);
        dateFormat = new SimpleDateFormat("dd.MM.yyyy", Locale.getDefault());
    }

    public RecommendationResult findByMedicineName(String medicineName) {
        List<RecommendationItem> results = new ArrayList<>();
        Set<String> processedMedicines = new HashSet<>();
        PatientProfileEntity profile = profileRepository.getProfileSync();

        MedicineEntity exactMatch = medicineDao.findByNameExact(medicineName);
        if (exactMatch != null) {
            String warning = buildWarning(exactMatch.getName(), profile);
            boolean isExpired = isMedicineExpired(exactMatch.getExpirationDate());
            String reason = "Точное совпадение";
            if (isExpired) {
                reason = reason + " (просрочено)";
            }
            results.add(new RecommendationItem(
                exactMatch.getId(),
                exactMatch.getName(),
                exactMatch.getDosage(),
                exactMatch.getExpirationDate(),
                isExpired,
                reason,
                warning
            ));
            processedMedicines.add(exactMatch.getName().toLowerCase());
        }

        List<AnalogueDictionaryEntryEntity> analogues = analogueDao.findAnalogues(medicineName);
        for (AnalogueDictionaryEntryEntity analogue : analogues) {
            String searchName = analogue.getSourceName().equalsIgnoreCase(medicineName)
                ? analogue.getAnalogueName()
                : analogue.getSourceName();

            if (processedMedicines.contains(searchName.toLowerCase())) {
                continue;
            }

            MedicineEntity analogueMedicine = medicineDao.findByNameExact(searchName);
            if (analogueMedicine != null) {
                String warning = buildWarning(analogueMedicine.getName(), profile);
                boolean isExpired = isMedicineExpired(analogueMedicine.getExpirationDate());
                String reason = "Аналог: " + medicineName + " (д.в.: " + analogue.getActiveSubstance() + ")";
                if (isExpired) {
                    reason = reason + " (просрочено)";
                }
                results.add(new RecommendationItem(
                    analogueMedicine.getId(),
                    analogueMedicine.getName(),
                    analogueMedicine.getDosage(),
                    analogueMedicine.getExpirationDate(),
                    isExpired,
                    reason,
                    warning
                ));
                processedMedicines.add(analogueMedicine.getName().toLowerCase());
            }
        }

        for (AnalogueDictionaryEntryEntity analogue : analogues) {
            List<MedicineEntity> bySubstance = medicineDao.findByNameContaining(analogue.getActiveSubstance());
            for (MedicineEntity med : bySubstance) {
                if (!processedMedicines.contains(med.getName().toLowerCase())) {
                    String warning = buildWarning(med.getName(), profile);
                    boolean isExpired = isMedicineExpired(med.getExpirationDate());
                    String reason = "Содержит действующее вещество: " + analogue.getActiveSubstance();
                    if (isExpired) {
                        reason = reason + " (просрочено)";
                    }
                    results.add(new RecommendationItem(
                        med.getId(),
                        med.getName(),
                        med.getDosage(),
                        med.getExpirationDate(),
                        isExpired,
                        reason,
                        warning
                    ));
                    processedMedicines.add(med.getName().toLowerCase());
                }
            }
        }

        return new RecommendationResult(medicineName, RecommendationResult.TYPE_MEDICINE, results);
    }

    public RecommendationResult findByDiagnosis(String diagnosis) {
        List<RecommendationItem> results = new ArrayList<>();
        Set<String> processedMedicines = new HashSet<>();
        PatientProfileEntity profile = profileRepository.getProfileSync();

        List<DiagnosisMappingEntryEntity> mappings = diagnosisDao.findByDiagnosis(diagnosis);

        for (DiagnosisMappingEntryEntity mapping : mappings) {
            MedicineEntity medicine = medicineDao.findByNameExact(mapping.getMedicineName());
            if (medicine != null && !processedMedicines.contains(medicine.getName().toLowerCase())) {
                String warning = buildWarning(medicine.getName(), profile);
                boolean isExpired = isMedicineExpired(medicine.getExpirationDate());
                String reason = "Рекомендуется при: " + mapping.getDiagnosisName() +
                    " (д.в.: " + mapping.getActiveSubstance() + ")";
                if (isExpired) {
                    reason = reason + " (просрочено)";
                }
                results.add(new RecommendationItem(
                    medicine.getId(),
                    medicine.getName(),
                    medicine.getDosage(),
                    medicine.getExpirationDate(),
                    isExpired,
                    reason,
                    warning
                ));
                processedMedicines.add(medicine.getName().toLowerCase());
            }

            List<AnalogueDictionaryEntryEntity> analogues = analogueDao.findByActiveSubstance(mapping.getActiveSubstance());
            for (AnalogueDictionaryEntryEntity analogue : analogues) {
                String[] names = {analogue.getSourceName(), analogue.getAnalogueName()};
                for (String name : names) {
                    if (!processedMedicines.contains(name.toLowerCase())) {
                        MedicineEntity analogueMedicine = medicineDao.findByNameExact(name);
                        if (analogueMedicine != null) {
                            String warning = buildWarning(analogueMedicine.getName(), profile);
                            boolean isExpired = isMedicineExpired(analogueMedicine.getExpirationDate());
                            String reason = "Аналог для " + mapping.getMedicineName() +
                                " при: " + mapping.getDiagnosisName();
                            if (isExpired) {
                                reason = reason + " (просрочено)";
                            }
                            results.add(new RecommendationItem(
                                analogueMedicine.getId(),
                                analogueMedicine.getName(),
                                analogueMedicine.getDosage(),
                                analogueMedicine.getExpirationDate(),
                                isExpired,
                                reason,
                                warning
                            ));
                            processedMedicines.add(analogueMedicine.getName().toLowerCase());
                        }
                    }
                }
            }
        }

        return new RecommendationResult(diagnosis, RecommendationResult.TYPE_DIAGNOSIS, results);
    }

    private boolean isMedicineExpired(String expirationDate) {
        if (expirationDate == null || expirationDate.isEmpty()) {
            return false;
        }
        try {
            Date expDate = dateFormat.parse(expirationDate);
            return expDate != null && expDate.before(new Date());
        } catch (ParseException e) {
            return false;
        }
    }

    private String buildWarning(String medicineName, PatientProfileEntity profile) {
        if (profile == null) {
            return null;
        }

        StringBuilder warning = new StringBuilder();
        String allergies = profile.getAllergies();
        String contraindications = profile.getContraindications();

        if (allergies != null && !allergies.isEmpty()) {
            String lowerName = medicineName.toLowerCase();
            String lowerAllergies = allergies.toLowerCase();
            if (lowerAllergies.contains(lowerName) || containsMedicineInAllergies(lowerName, lowerAllergies)) {
                warning.append("Возможна аллергия! ");
            }
        }

        if (contraindications != null && !contraindications.isEmpty()) {
            warning.append("Проверьте противопоказания: ").append(contraindications);
        }

        return warning.length() > 0 ? warning.toString().trim() : null;
    }

    private boolean containsMedicineInAllergies(String medicineName, String allergies) {
        String[] allergyItems = allergies.split("[,;]");
        for (String allergy : allergyItems) {
            if (medicineName.contains(allergy.trim().toLowerCase()) ||
                allergy.trim().toLowerCase().contains(medicineName)) {
                return true;
            }
        }
        return false;
    }
}
