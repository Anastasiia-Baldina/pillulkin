package com.example.pillulkin.ui.adapter;

import androidx.recyclerview.widget.DiffUtil;

import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.spy;

import com.example.pillulkin.data.local.entity.Reminder;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.example.pillulkin.data.remote.model.RecommendationItem;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

public class AdapterLogicTest {

    private DiffUtil.ItemCallback<PatientMedicineResponse> medicineDiffCallback;
    private DiffUtil.ItemCallback<ReferenceMedicineResponse> referenceDiffCallback;
    private DiffUtil.ItemCallback<PatientSymptomResponse> symptomsDiffCallback;

    @Before
    public void setUp() throws Exception {
        Field medicineField = MedicineAdapter.class.getDeclaredField("DIFF_CALLBACK");
        medicineField.setAccessible(true);
        medicineDiffCallback = (DiffUtil.ItemCallback<PatientMedicineResponse>) medicineField.get(null);

        Field referenceField = ReferenceMedicineAdapter.class.getDeclaredField("DIFF_CALLBACK");
        referenceField.setAccessible(true);
        referenceDiffCallback = (DiffUtil.ItemCallback<ReferenceMedicineResponse>) referenceField.get(null);

        Field symptomsField = SymptomsAdapter.class.getDeclaredField("DIFF_CALLBACK");
        symptomsField.setAccessible(true);
        symptomsDiffCallback = (DiffUtil.ItemCallback<PatientSymptomResponse>) symptomsField.get(null);
    }

    private PatientMedicineResponse createMedicine(Long id, String name, String dosage,
                                                    String expirationDate, String quantity) {
        PatientMedicineResponse m = new PatientMedicineResponse();
        m.setId(id);
        m.setMedicineName(name);
        m.setDosage(dosage);
        m.setExpirationDate(expirationDate);
        m.setQuantity(quantity);
        return m;
    }

    private ReferenceMedicineResponse createReference(Long id, String name, String dosage) {
        ReferenceMedicineResponse r = new ReferenceMedicineResponse();
        r.setId(id);
        r.setName(name);
        r.setDosage(dosage);
        return r;
    }

    private PatientSymptomResponse createSymptom(Long id, String symptom, String timestamp) {
        PatientSymptomResponse s = new PatientSymptomResponse();
        s.setId(id);
        s.setSymptom(symptom);
        s.setTimestamp(timestamp);
        return s;
    }

    private void setField(Object obj, String fieldName, Object value) {
        try {
            Field f = obj.getClass().getDeclaredField(fieldName);
            f.setAccessible(true);
            f.set(obj, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    public void medicineDiff_areItemsTheSame_sameId() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", "2026-12-01", "10");
        PatientMedicineResponse b = createMedicine(1L, "Nurofen", "200mg", "2027-01-01", "20");
        assertTrue(medicineDiffCallback.areItemsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areItemsTheSame_differentId() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, null);
        PatientMedicineResponse b = createMedicine(2L, "Aspirin", "100mg", null, null);
        assertFalse(medicineDiffCallback.areItemsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_identical() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", "2026-12-01", "10");
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", "2026-12-01", "10");
        assertTrue(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_differentName() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, null);
        PatientMedicineResponse b = createMedicine(1L, "Nurofen", "100mg", null, null);
        assertFalse(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_differentDosage() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, null);
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "200mg", null, null);
        assertFalse(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_differentExpirationDate() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", "2026-12-01", null);
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", "2027-01-01", null);
        assertFalse(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_bothNullExpirationDate() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, null);
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", null, null);
        assertTrue(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_nullVsNonNullExpirationDate() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, null);
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", "2026-12-01", null);
        assertFalse(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_differentQuantity() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, "10");
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", null, "20");
        assertFalse(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_bothNullQuantity() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, null);
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", null, null);
        assertTrue(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void medicineDiff_areContentsTheSame_nullVsNonNullQuantity() {
        PatientMedicineResponse a = createMedicine(1L, "Aspirin", "100mg", null, "10");
        PatientMedicineResponse b = createMedicine(1L, "Aspirin", "100mg", null, null);
        assertFalse(medicineDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void referenceDiff_areItemsTheSame_sameId() {
        ReferenceMedicineResponse a = createReference(1L, "Aspirin", "100mg");
        ReferenceMedicineResponse b = createReference(1L, "Nurofen", "200mg");
        assertTrue(referenceDiffCallback.areItemsTheSame(a, b));
    }

    @Test
    public void referenceDiff_areItemsTheSame_differentId() {
        ReferenceMedicineResponse a = createReference(1L, "Aspirin", "100mg");
        ReferenceMedicineResponse b = createReference(2L, "Aspirin", "100mg");
        assertFalse(referenceDiffCallback.areItemsTheSame(a, b));
    }

    @Test
    public void referenceDiff_areContentsTheSame_identical() {
        ReferenceMedicineResponse a = createReference(1L, "Aspirin", "100mg");
        ReferenceMedicineResponse b = createReference(1L, "Aspirin", "100mg");
        assertTrue(referenceDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void referenceDiff_areContentsTheSame_differentName() {
        ReferenceMedicineResponse a = createReference(1L, "Aspirin", "100mg");
        ReferenceMedicineResponse b = createReference(1L, "Nurofen", "100mg");
        assertFalse(referenceDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void referenceDiff_areContentsTheSame_differentDosage() {
        ReferenceMedicineResponse a = createReference(1L, "Aspirin", "100mg");
        ReferenceMedicineResponse b = createReference(1L, "Aspirin", "200mg");
        assertFalse(referenceDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areItemsTheSame_sameId() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", "2026-01-01T10:00:00");
        PatientSymptomResponse b = createSymptom(1L, "Fever", "2026-02-01T10:00:00");
        assertTrue(symptomsDiffCallback.areItemsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areItemsTheSame_differentId() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", null);
        PatientSymptomResponse b = createSymptom(2L, "Headache", null);
        assertFalse(symptomsDiffCallback.areItemsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areContentsTheSame_identical() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", "2026-01-01T10:00:00");
        PatientSymptomResponse b = createSymptom(1L, "Headache", "2026-01-01T10:00:00");
        assertTrue(symptomsDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areContentsTheSame_differentSymptom() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", null);
        PatientSymptomResponse b = createSymptom(1L, "Fever", null);
        assertFalse(symptomsDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areContentsTheSame_differentTimestamp() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", "2026-01-01T10:00:00");
        PatientSymptomResponse b = createSymptom(1L, "Headache", "2026-02-01T10:00:00");
        assertFalse(symptomsDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areContentsTheSame_bothNullTimestamp() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", null);
        PatientSymptomResponse b = createSymptom(1L, "Headache", null);
        assertTrue(symptomsDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void symptomsDiff_areContentsTheSame_nullVsNonNullTimestamp() {
        PatientSymptomResponse a = createSymptom(1L, "Headache", null);
        PatientSymptomResponse b = createSymptom(1L, "Headache", "2026-01-01T10:00:00");
        assertFalse(symptomsDiffCallback.areContentsTheSame(a, b));
    }

    @Test
    public void symptoms_isSymptomOutdated_nullTimestamp() {
        assertFalse(SymptomsAdapter.isSymptomOutdated(null));
    }

    @Test
    public void symptoms_isSymptomOutdated_emptyTimestamp() {
        assertFalse(SymptomsAdapter.isSymptomOutdated(""));
    }

    @Test
    public void prescriptionAdapter_emptyInitially() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(p -> {}));
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void prescriptionAdapter_submitList() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(p -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        PrescriptionResponse p = new PrescriptionResponse();
        p.setId(1L);
        setField(p, "medicineName", "Aspirin");
        setField(p, "dosage", "100mg");
        adapter.submitList(Collections.singletonList(p));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void prescriptionAdapter_submitEmptyList() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(p -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        PrescriptionResponse p = new PrescriptionResponse();
        adapter.submitList(Collections.singletonList(p));
        assertEquals(1, adapter.getItemCount());
        adapter.submitList(Collections.emptyList());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void prescriptionAdapter_submitMultipleItems() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(p -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<PrescriptionResponse> items = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            PrescriptionResponse p = new PrescriptionResponse();
            p.setId((long) i);
            items.add(p);
        }
        adapter.submitList(items);
        assertEquals(5, adapter.getItemCount());
    }

    @Test
    public void prescriptionAdapter_submitListReplacesPrevious() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(p -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        PrescriptionResponse p1 = new PrescriptionResponse();
        p1.setId(1L);
        adapter.submitList(Collections.singletonList(p1));
        assertEquals(1, adapter.getItemCount());
        List<PrescriptionResponse> newList = new ArrayList<>();
        PrescriptionResponse p2 = new PrescriptionResponse();
        p2.setId(2L);
        PrescriptionResponse p3 = new PrescriptionResponse();
        p3.setId(3L);
        newList.add(p2);
        newList.add(p3);
        adapter.submitList(newList);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void reminderAdapter_emptyInitially() {
        ReminderAdapter adapter = spy(new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override public void onToggle(Reminder reminder) {}
            @Override public void onDelete(Reminder reminder) {}
            @Override public void onClick(Reminder reminder) {}
        }));
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void reminderAdapter_submitList() {
        ReminderAdapter adapter = spy(new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override public void onToggle(Reminder reminder) {}
            @Override public void onDelete(Reminder reminder) {}
            @Override public void onClick(Reminder reminder) {}
        }));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1);
        r.setHour(8);
        r.setMinute(30);
        r.setMedicineName("Aspirin");
        adapter.submitList(Collections.singletonList(r));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void reminderAdapter_submitEmptyList() {
        ReminderAdapter adapter = spy(new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override public void onToggle(Reminder reminder) {}
            @Override public void onDelete(Reminder reminder) {}
            @Override public void onClick(Reminder reminder) {}
        }));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        adapter.submitList(Collections.singletonList(r));
        assertEquals(1, adapter.getItemCount());
        adapter.submitList(Collections.emptyList());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void reminderAdapter_submitMultipleItems() {
        ReminderAdapter adapter = spy(new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override public void onToggle(Reminder reminder) {}
            @Override public void onDelete(Reminder reminder) {}
            @Override public void onClick(Reminder reminder) {}
        }));
        doNothing().when(adapter).notifyDataSetChanged();
        List<Reminder> items = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            Reminder r = new Reminder();
            r.setId(i);
            r.setHour(i + 8);
            r.setMinute(0);
            items.add(r);
        }
        adapter.submitList(items);
        assertEquals(3, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapter_emptyInitially() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(medicine -> {}));
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapter_submitItems() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(medicine -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> items = new ArrayList<>();
        items.add(RecommendationItem.header("Recommended"));
        ReferenceMedicineResponse med = createReference(1L, "Aspirin", "100mg");
        items.add(RecommendationItem.medicine(med, false));
        adapter.submitItems(items);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapter_getItemViewType_header() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(medicine -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> items = new ArrayList<>();
        items.add(RecommendationItem.header("Header"));
        adapter.submitItems(items);
        assertEquals(RecommendationItem.TYPE_HEADER, adapter.getItemViewType(0));
    }

    @Test
    public void recommendationAdapter_getItemViewType_medicine() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(medicine -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> items = new ArrayList<>();
        items.add(RecommendationItem.header("Header"));
        ReferenceMedicineResponse med = createReference(1L, "Aspirin", "100mg");
        items.add(RecommendationItem.medicine(med, false));
        adapter.submitItems(items);
        assertEquals(RecommendationItem.TYPE_MEDICINE, adapter.getItemViewType(1));
    }

    @Test
    public void recommendationAdapter_submitEmptyList() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(medicine -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> items = new ArrayList<>();
        items.add(RecommendationItem.header("Header"));
        adapter.submitItems(items);
        assertEquals(1, adapter.getItemCount());
        adapter.submitItems(Collections.emptyList());
        assertEquals(0, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapter_submitItemsReplacesPrevious() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(medicine -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> first = new ArrayList<>();
        first.add(RecommendationItem.header("A"));
        adapter.submitItems(first);
        assertEquals(1, adapter.getItemCount());
        List<RecommendationItem> second = new ArrayList<>();
        second.add(RecommendationItem.header("B"));
        second.add(RecommendationItem.medicine(createReference(1L, "X", "10mg"), true));
        adapter.submitItems(second);
        assertEquals(2, adapter.getItemCount());
    }

    @Test
    public void recommendationItem_headerType() {
        RecommendationItem item = RecommendationItem.header("Test Header");
        assertEquals(RecommendationItem.TYPE_HEADER, item.getType());
        assertEquals("Test Header", item.getHeaderText());
    }

    @Test
    public void recommendationItem_medicineType() {
        ReferenceMedicineResponse med = createReference(5L, "Ibuprofen", "400mg");
        RecommendationItem item = RecommendationItem.medicine(med, true);
        assertEquals(RecommendationItem.TYPE_MEDICINE, item.getType());
        assertEquals(med, item.getMedicine());
        assertTrue(item.isFromCabinet());
    }

    @Test
    public void recommendationItem_medicineType_notFromCabinet() {
        ReferenceMedicineResponse med = createReference(5L, "Ibuprofen", "400mg");
        RecommendationItem item = RecommendationItem.medicine(med, false);
        assertFalse(item.isFromCabinet());
    }

    @Test
    public void isSymptomOutdated_exactlySevenDays_notOutdated() {
        String sevenDaysAgo = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date(System.currentTimeMillis() - 7L * 24 * 60 * 60 * 1000));
        assertFalse(SymptomsAdapter.isSymptomOutdated(sevenDaysAgo));
    }

    @Test
    public void isSymptomOutdated_eightDays_outdated() {
        String eightDaysAgo = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date(System.currentTimeMillis() - 8L * 24 * 60 * 60 * 1000));
        assertTrue(SymptomsAdapter.isSymptomOutdated(eightDaysAgo));
    }

    @Test
    public void isSymptomOutdated_sixDays_notOutdated() {
        String sixDaysAgo = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date(System.currentTimeMillis() - 6L * 24 * 60 * 60 * 1000));
        assertFalse(SymptomsAdapter.isSymptomOutdated(sixDaysAgo));
    }

    @Test
    public void isSymptomOutdated_futureDate_notOutdated() {
        String future = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date(System.currentTimeMillis() + 7L * 24 * 60 * 60 * 1000));
        assertFalse(SymptomsAdapter.isSymptomOutdated(future));
    }

    @Test
    public void isSymptomOutdated_malformedDate_notOutdated() {
        assertFalse(SymptomsAdapter.isSymptomOutdated("not-a-date"));
    }

    @Test
    public void isSymptomOutdated_thirtyDays_outdated() {
        String thirtyDaysAgo = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss",
                java.util.Locale.getDefault())
                .format(new java.util.Date(System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000));
        assertTrue(SymptomsAdapter.isSymptomOutdated(thirtyDaysAgo));
    }

    @Test
    public void prescriptionAdapter_listenerNull_submitList() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        PrescriptionResponse p = new PrescriptionResponse();
        p.setId(1L);
        adapter.submitList(Collections.singletonList(p));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void reminderAdapter_listenerNull_submitList() {
        ReminderAdapter adapter = spy(new ReminderAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1);
        adapter.submitList(Collections.singletonList(r));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapter_listenerNull_submitItems() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> items = new ArrayList<>();
        items.add(RecommendationItem.header("Test"));
        adapter.submitItems(items);
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void prescriptionAdapter_submitListReplacesAndShrinks() {
        PrescriptionAdapter adapter = spy(new PrescriptionAdapter(p -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        PrescriptionResponse p1 = new PrescriptionResponse();
        PrescriptionResponse p2 = new PrescriptionResponse();
        PrescriptionResponse p3 = new PrescriptionResponse();
        p1.setId(1L);
        p2.setId(2L);
        p3.setId(3L);
        adapter.submitList(Arrays.asList(p1, p2, p3));
        assertEquals(3, adapter.getItemCount());
        adapter.submitList(Collections.singletonList(p1));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void reminderAdapter_submitListReplacesAndShrinks() {
        ReminderAdapter adapter = spy(new ReminderAdapter(new ReminderAdapter.OnReminderActionListener() {
            @Override public void onToggle(Reminder reminder) {}
            @Override public void onDelete(Reminder reminder) {}
            @Override public void onClick(Reminder reminder) {}
        }));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r1 = new Reminder(); r1.setId(1);
        Reminder r2 = new Reminder(); r2.setId(2);
        Reminder r3 = new Reminder(); r3.setId(3);
        adapter.submitList(Arrays.asList(r1, r2, r3));
        assertEquals(3, adapter.getItemCount());
        adapter.submitList(Collections.singletonList(r1));
        assertEquals(1, adapter.getItemCount());
    }

    @Test
    public void recommendationAdapter_mixedItems_count() {
        RecommendationSectionAdapter adapter = spy(new RecommendationSectionAdapter(m -> {}));
        doNothing().when(adapter).notifyDataSetChanged();
        List<RecommendationItem> items = new ArrayList<>();
        items.add(RecommendationItem.header("Section 1"));
        items.add(RecommendationItem.medicine(createReference(1L, "A", "10mg"), false));
        items.add(RecommendationItem.medicine(createReference(2L, "B", "20mg"), true));
        items.add(RecommendationItem.header("Section 2"));
        items.add(RecommendationItem.medicine(createReference(3L, "C", "30mg"), false));
        adapter.submitItems(items);
        assertEquals(5, adapter.getItemCount());
        assertEquals(RecommendationItem.TYPE_HEADER, adapter.getItemViewType(0));
        assertEquals(RecommendationItem.TYPE_MEDICINE, adapter.getItemViewType(1));
        assertEquals(RecommendationItem.TYPE_MEDICINE, adapter.getItemViewType(2));
        assertEquals(RecommendationItem.TYPE_HEADER, adapter.getItemViewType(3));
        assertEquals(RecommendationItem.TYPE_MEDICINE, adapter.getItemViewType(4));
    }
}
