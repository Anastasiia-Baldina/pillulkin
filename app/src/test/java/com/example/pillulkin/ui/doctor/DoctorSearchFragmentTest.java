package com.example.pillulkin.ui.doctor;

import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.RecommendationItem;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class DoctorSearchFragmentTest {

    private ReferenceMedicineResponse makeRef(long id, String name) {
        ReferenceMedicineResponse r = new ReferenceMedicineResponse();
        r.setId(id);
        r.setName(name);
        r.setDosage("500 мг");
        return r;
    }

    private PatientMedicineResponse makePatient(long medicineId, String name) {
        PatientMedicineResponse p = new PatientMedicineResponse();
        p.setMedicineId(medicineId);
        p.setMedicineName(name);
        return p;
    }

    private List<RecommendationItem> buildResults(
            List<ReferenceMedicineResponse> refs,
            List<PatientMedicineResponse> patientMeds) {

        List<RecommendationItem> items = new ArrayList<>();
        java.util.Set<Long> ids = new java.util.HashSet<>();
        java.util.Set<String> names = new java.util.HashSet<>();
        if (patientMeds != null) {
            for (PatientMedicineResponse pm : patientMeds) {
                if (pm.getMedicineId() != null) ids.add(pm.getMedicineId());
                if (pm.getMedicineName() != null) names.add(pm.getMedicineName().toLowerCase());
            }
        }

        List<RecommendationItem> cabinet = new ArrayList<>();
        List<RecommendationItem> other = new ArrayList<>();
        for (ReferenceMedicineResponse med : refs) {
            boolean inCabinet = false;
            if (med.getId() != null && ids.contains(med.getId())) inCabinet = true;
            if (!inCabinet && med.getName() != null) {
                String nl = med.getName().toLowerCase();
                for (String pn : names) {
                    if (nl.contains(pn) || pn.contains(nl)) { inCabinet = true; break; }
                }
            }
            if (inCabinet) cabinet.add(RecommendationItem.medicine(med, true));
            else other.add(RecommendationItem.medicine(med, false));
        }
        if (!cabinet.isEmpty()) { items.add(RecommendationItem.header("Cabinet")); items.addAll(cabinet); }
        if (!other.isEmpty()) { items.add(RecommendationItem.header("Other")); items.addAll(other); }
        return items;
    }

    @Test
    public void cabinetMatch_byMedicineId() {
        List<ReferenceMedicineResponse> refs = Arrays.asList(makeRef(1L, "Парацетамол"), makeRef(2L, "Ибупрофен"));
        List<PatientMedicineResponse> patientMeds = Arrays.asList(makePatient(1L, "Парацетамол"));

        List<RecommendationItem> items = buildResults(refs, patientMeds);

        assertEquals(4, items.size());
        assertEquals(RecommendationItem.TYPE_HEADER, items.get(0).getType());
        assertEquals("Cabinet", items.get(0).getHeaderText());
        assertEquals(RecommendationItem.TYPE_MEDICINE, items.get(1).getType());
        assertTrue(items.get(1).isFromCabinet());
        assertEquals("Парацетамол", items.get(1).getMedicine().getName());

        assertEquals(RecommendationItem.TYPE_HEADER, items.get(2).getType());
        assertEquals("Other", items.get(2).getHeaderText());
        assertFalse(items.get(3).isFromCabinet());
    }

    @Test
    public void cabinetMatch_byName() {
        List<ReferenceMedicineResponse> refs = Arrays.asList(makeRef(10L, "Парацетамол-форте"));
        List<PatientMedicineResponse> patientMeds = Arrays.asList(makePatient(99L, "Парацетамол"));

        List<RecommendationItem> items = buildResults(refs, patientMeds);

        assertEquals(2, items.size());
        assertTrue(items.get(1).isFromCabinet());
    }

    @Test
    public void noPatientMeds_allGoToOther() {
        List<ReferenceMedicineResponse> refs = Arrays.asList(makeRef(1L, "Аспирин"), makeRef(2L, "Нурофен"));
        List<RecommendationItem> items = buildResults(refs, null);

        assertEquals(3, items.size());
        assertEquals(RecommendationItem.TYPE_HEADER, items.get(0).getType());
        assertFalse(items.get(2).isFromCabinet());
    }

    @Test
    public void allInCabinet_noOtherSection() {
        List<ReferenceMedicineResponse> refs = Arrays.asList(makeRef(1L, "Аспирин"));
        List<PatientMedicineResponse> patientMeds = Arrays.asList(makePatient(1L, "Аспирин"));

        List<RecommendationItem> items = buildResults(refs, patientMeds);

        assertEquals(2, items.size());
        assertEquals("Cabinet", items.get(0).getHeaderText());
        assertTrue(items.get(1).isFromCabinet());
    }

    @Test
    public void emptyResults_returnsEmptyList() {
        List<RecommendationItem> items = buildResults(Collections.emptyList(), null);
        assertTrue(items.isEmpty());
    }

    @Test
    public void cabinetSortedFirst() {
        List<ReferenceMedicineResponse> refs = Arrays.asList(
                makeRef(3L, "Нурофен"), makeRef(1L, "Аспирин"), makeRef(2L, "Ибупрофен"));
        List<PatientMedicineResponse> patientMeds = Arrays.asList(makePatient(1L, "Аспирин"));

        List<RecommendationItem> items = buildResults(refs, patientMeds);

        assertEquals(5, items.size());
        assertTrue(items.get(1).isFromCabinet());
        assertEquals("Аспирин", items.get(1).getMedicine().getName());
        assertFalse(items.get(3).isFromCabinet());
        assertFalse(items.get(4).isFromCabinet());
    }
}
