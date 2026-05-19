package com.example.pillulkin.ui;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.pillulkin.R;
import com.example.pillulkin.ui.doctor.DoctorMedicineListFragment;
import com.example.pillulkin.ui.doctor.DoctorSymptomsFragment;
import com.example.pillulkin.ui.doctor.DoctorPrescriptionsFragment;
import com.example.pillulkin.ui.doctor.DoctorMedicineDetailFragment;
import com.example.pillulkin.ui.doctor.DoctorSearchFragment;
import com.example.pillulkin.ui.doctor.RecommendationResultsFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static org.junit.Assert.assertNotNull;

import androidx.test.espresso.matcher.ViewMatchers;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class DoctorFragmentTest extends FragmentTest {

    @Test
    public void doctorMedicineListFragment_launches() {
        setDoctorSession(true);
        FragmentScenario<DoctorMedicineListFragment> scenario =
                FragmentScenario.launchInContainer(DoctorMedicineListFragment.class);
        onView(withId(R.id.rvMedicines)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSearch)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorMedicineListFragment_hasPatientInfoSection() {
        setDoctorSession(true);
        FragmentScenario<DoctorMedicineListFragment> scenario =
                FragmentScenario.launchInContainer(DoctorMedicineListFragment.class);
        onView(withId(R.id.patientInfoScroll)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        onView(withId(R.id.btnSearch)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorSymptomsFragment_launches() {
        setDoctorSession(true);
        FragmentScenario<DoctorSymptomsFragment> scenario =
                FragmentScenario.launchInContainer(DoctorSymptomsFragment.class);
        onView(withId(R.id.rvSymptoms)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorPrescriptionsFragment_launches() {
        setDoctorSession(true);
        FragmentScenario<DoctorPrescriptionsFragment> scenario =
                FragmentScenario.launchInContainer(DoctorPrescriptionsFragment.class);
        onView(withId(R.id.rvPrescriptions)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorMedicineDetailFragment_launches() {
        setDoctorSession(true);
        Bundle args = new Bundle();
        args.putLong("medicineId", 1L);
        args.putString("medicineName", "Aspirin");
        args.putString("dosage", "500mg");
        args.putString("form", "tablet");
        args.putString("activeSubstance", "ASA");
        args.putLong("patientId", 1L);

        FragmentScenario<DoctorMedicineDetailFragment> scenario =
                FragmentScenario.launchInContainer(DoctorMedicineDetailFragment.class, args);
        onView(withId(R.id.tvMedicineName)).check(matches(isDisplayed()));
        onView(withId(R.id.tvDosage)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorMedicineDetailFragment_hasSpinners() {
        Bundle args = new Bundle();
        args.putLong("medicineId", 1L);
        args.putString("medicineName", "Test");
        args.putString("dosage", "10mg");
        args.putLong("patientId", 1L);

        FragmentScenario<DoctorMedicineDetailFragment> scenario =
                FragmentScenario.launchInContainer(DoctorMedicineDetailFragment.class, args);
        onView(withId(R.id.tvMedicineName)).check(matches(isDisplayed()));
        onView(withId(R.id.spinnerFrequency)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.spinnerMealTiming)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        onView(withId(R.id.btnAddPrescription)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.VISIBLE)));
        scenario.close();
    }

    @Test
    public void doctorMedicineDetailFragment_hasCustomInstructions() {
        setDoctorSession(true);
        Bundle args = new Bundle();
        args.putLong("medicineId", 2L);
        args.putString("medicineName", "Ibuprofen");
        args.putString("dosage", "400mg");
        args.putLong("patientId", 1L);

        FragmentScenario<DoctorMedicineDetailFragment> scenario =
                FragmentScenario.launchInContainer(DoctorMedicineDetailFragment.class, args);
        onView(withId(R.id.etCustomInstructions)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorSearchFragment_launches() {
        setDoctorSession(true);
        FragmentScenario<DoctorSearchFragment> scenario =
                FragmentScenario.launchInContainer(DoctorSearchFragment.class);
        onView(withId(R.id.etQuery)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.rvResults)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void doctorSearchFragment_hasChipGroup() {
        setDoctorSession(true);
        FragmentScenario<DoctorSearchFragment> scenario =
                FragmentScenario.launchInContainer(DoctorSearchFragment.class);
        onView(withId(R.id.chipGroup)).check(matches(isDisplayed()));
        onView(withId(R.id.chipDiagnosis)).check(matches(isDisplayed()));
        onView(withId(R.id.chipMedicine)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void recommendationResultsFragment_launches() {
        setDoctorSession(true);
        FragmentScenario<RecommendationResultsFragment> scenario =
                FragmentScenario.launchInContainer(RecommendationResultsFragment.class);
        onView(withId(R.id.rvResults)).check(matches(isDisplayed()));
        scenario.close();
    }
}
