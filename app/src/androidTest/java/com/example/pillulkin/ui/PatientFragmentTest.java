package com.example.pillulkin.ui;

import android.os.Bundle;

import androidx.fragment.app.testing.FragmentScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.example.pillulkin.R;
import com.example.pillulkin.ui.patient.SymptomsFragment;
import com.example.pillulkin.ui.patient.PatientProfileFragment;
import com.example.pillulkin.ui.patient.GenerateCodeFragment;
import com.example.pillulkin.ui.patient.AddMedicineFragment;
import com.example.pillulkin.ui.patient.MedicineListFragment;
import com.example.pillulkin.ui.patient.PatientAuthFragment;

import org.junit.Test;
import org.junit.runner.RunWith;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withEffectiveVisibility;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.junit.Assert.assertNotNull;

import androidx.test.espresso.matcher.ViewMatchers;

@RunWith(AndroidJUnit4.class)
@LargeTest
public class PatientFragmentTest extends FragmentTest {

    @Test
    public void symptomsFragment_launches() {
        FragmentScenario<SymptomsFragment> scenario =
                FragmentScenario.launchInContainer(SymptomsFragment.class);
        onView(withId(R.id.btnAdd)).check(matches(isDisplayed()));
        onView(withId(R.id.btnDiagnose)).check(matches(isDisplayed()));
        onView(withId(R.id.inputLayout)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void symptomsFragment_hasInputField() {
        FragmentScenario<SymptomsFragment> scenario =
                FragmentScenario.launchInContainer(SymptomsFragment.class);
        onView(withId(R.id.etSymptom)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void patientProfileFragment_launches() {
        FragmentScenario<PatientProfileFragment> scenario =
                FragmentScenario.launchInContainer(PatientProfileFragment.class);
        onView(withId(R.id.etName)).check(matches(isDisplayed()));
        onView(withId(R.id.etAge)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSave)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void patientProfileFragment_hasAllFields() {
        FragmentScenario<PatientProfileFragment> scenario =
                FragmentScenario.launchInContainer(PatientProfileFragment.class);
        onView(withId(R.id.etAllergies)).check(matches(isDisplayed()));
        onView(withId(R.id.etContraindications)).check(matches(isDisplayed()));
        onView(withId(R.id.etNotes)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void patientProfileFragment_localMode_showsGoogleSignIn() {
        setLocalMode(true);
        FragmentScenario<PatientProfileFragment> scenario =
                FragmentScenario.launchInContainer(PatientProfileFragment.class);
        onView(withId(R.id.btnGoogleSignIn)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void generateCodeFragment_launches() {
        FragmentScenario<GenerateCodeFragment> scenario =
                FragmentScenario.launchInContainer(GenerateCodeFragment.class);
        onView(withId(R.id.btnGenerate)).check(matches(isDisplayed()));
        onView(withId(R.id.cardCode)).check(matches(withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
        scenario.close();
    }

    @Test
    public void addMedicineFragment_launches() {
        FragmentScenario<AddMedicineFragment> scenario =
                FragmentScenario.launchInContainer(AddMedicineFragment.class);
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.rvSearchResults)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void addMedicineFragment_hasSearchButton() {
        FragmentScenario<AddMedicineFragment> scenario =
                FragmentScenario.launchInContainer(AddMedicineFragment.class);
        onView(withId(R.id.btnSearch)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void medicineListFragment_launches() {
        FragmentScenario<MedicineListFragment> scenario =
                FragmentScenario.launchInContainer(MedicineListFragment.class);
        onView(withId(R.id.fabAdd)).check(matches(isDisplayed()));
        onView(withId(R.id.searchLayout)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void medicineListFragment_hasSearchAndSort() {
        FragmentScenario<MedicineListFragment> scenario =
                FragmentScenario.launchInContainer(MedicineListFragment.class);
        onView(withId(R.id.etSearch)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSort)).check(matches(isDisplayed()));
        scenario.close();
    }

    @Test
    public void patientAuthFragment_launches() {
        prefs.edit().remove("patient_id").apply();
        FragmentScenario<PatientAuthFragment> scenario =
                FragmentScenario.launchInContainer(PatientAuthFragment.class);
        assertNotNull(scenario);
        scenario.close();
    }

    @Test
    public void patientAuthFragment_hasButtons() {
        prefs.edit().remove("patient_id").apply();
        FragmentScenario<PatientAuthFragment> scenario =
                FragmentScenario.launchInContainer(PatientAuthFragment.class);
        onView(withId(R.id.btnGoogleSignIn)).check(matches(isDisplayed()));
        onView(withId(R.id.btnSkip)).check(matches(isDisplayed()));
        scenario.close();
    }
}
