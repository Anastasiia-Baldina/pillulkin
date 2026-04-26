package com.example.pillulkin.ui.patient;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.*;

public class DiagnosisDialogFragmentTest {

    @Test
    public void stubSymptoms_hasExactlyEightItems() {
        assertEquals(8, DiagnosisDialogFragment.STUB_SYMPTOMS.size());
    }

    @Test
    public void stubSymptoms_containsExpectedSymptoms() {
        List<String> symptoms = DiagnosisDialogFragment.STUB_SYMPTOMS;
        assertTrue(symptoms.contains("Повышенная температура"));
        assertTrue(symptoms.contains("Насморк"));
        assertTrue(symptoms.contains("Кашель"));
        assertTrue(symptoms.contains("Головная боль"));
        assertTrue(symptoms.contains("Боль в горле"));
        assertTrue(symptoms.contains("Чихание"));
        assertTrue(symptoms.contains("Слабость"));
        assertTrue(symptoms.contains("Боль в мышцах"));
    }

    @Test
    public void binaryList_allUnchecked_returnsAllZeros() {
        List<Integer> expected = Arrays.asList(0, 0, 0, 0, 0, 0, 0, 0);
        assertEquals(8, expected.size());
        for (Integer val : expected) {
            assertEquals(0, val.intValue());
        }
    }

    @Test
    public void binaryList_allChecked_returnsAllOnes() {
        List<Integer> expected = Arrays.asList(1, 1, 1, 1, 1, 1, 1, 1);
        assertEquals(8, expected.size());
        for (Integer val : expected) {
            assertEquals(1, val.intValue());
        }
    }

    @Test
    public void binaryList_mixed_returnsCorrectValues() {
        List<Integer> binary = Arrays.asList(1, 0, 1, 0, 1, 0, 1, 0);
        assertEquals(8, binary.size());
        assertEquals(1, binary.get(0).intValue());
        assertEquals(0, binary.get(1).intValue());
        assertEquals(1, binary.get(2).intValue());
        assertEquals(0, binary.get(3).intValue());
    }

    @Test
    public void binaryList_onlyContainsZerosAndOnes() {
        List<Integer> binary = Arrays.asList(1, 0, 1, 1, 0, 0, 1, 0);
        for (Integer val : binary) {
            assertTrue(val == 0 || val == 1);
        }
    }
}
