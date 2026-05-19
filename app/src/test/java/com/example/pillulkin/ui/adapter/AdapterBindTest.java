package com.example.pillulkin.ui.adapter;

import android.view.View;
import android.widget.TextView;

import com.example.pillulkin.data.local.entity.Reminder;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

import org.junit.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import com.example.pillulkin.R;
import com.google.android.material.button.MaterialButton;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdapterBindTest {

    private PrescriptionAdapter.ViewHolder createPrescriptionViewHolder() throws Exception {
        View itemView = mock(View.class);
        when(itemView.getContext()).thenReturn(mock(android.content.Context.class));
        TextView tvMedicineName = mock(TextView.class);
        TextView tvDosage = mock(TextView.class);
        TextView tvInstructions = mock(TextView.class);
        TextView tvDate = mock(TextView.class);
        MaterialButton btnMove = mock(MaterialButton.class);
        when(itemView.findViewById(R.id.tvMedicineName)).thenReturn(tvMedicineName);
        when(itemView.findViewById(R.id.tvDosage)).thenReturn(tvDosage);
        when(itemView.findViewById(R.id.tvInstructions)).thenReturn(tvInstructions);
        when(itemView.findViewById(R.id.tvDate)).thenReturn(tvDate);
        when(itemView.findViewById(R.id.btnMoveToCabinet)).thenReturn(btnMove);

        Class<?> vhClass = Class.forName("com.example.pillulkin.ui.adapter.PrescriptionAdapter$ViewHolder");
        Constructor<?> ctor = vhClass.getDeclaredConstructor(PrescriptionAdapter.class, View.class);
        ctor.setAccessible(true);
        PrescriptionAdapter adapter = new PrescriptionAdapter(null);
        return (PrescriptionAdapter.ViewHolder) ctor.newInstance(adapter, itemView);
    }

    private PrescriptionResponse createPrescription(String name, String dosage, String form,
                                                     String instructions, String prescribedAt, String status) {
        PrescriptionResponse p = new PrescriptionResponse();
        p.setId(1L);
        try {
            Field f;
            f = PrescriptionResponse.class.getDeclaredField("medicineName"); f.setAccessible(true); f.set(p, name);
            f = PrescriptionResponse.class.getDeclaredField("dosage"); f.setAccessible(true); f.set(p, dosage);
            f = PrescriptionResponse.class.getDeclaredField("form"); f.setAccessible(true); f.set(p, form);
            f = PrescriptionResponse.class.getDeclaredField("dosageInstructions"); f.setAccessible(true); f.set(p, instructions);
            f = PrescriptionResponse.class.getDeclaredField("prescribedAt"); f.setAccessible(true); f.set(p, prescribedAt);
            f = PrescriptionResponse.class.getDeclaredField("status"); f.setAccessible(true); f.set(p, status);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return p;
    }

    @Test
    public void prescriptionBind_withForm_appendsForm() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("Aspirin", "500mg", "tablet", "after meal", "2025-01-01T10:00:00", "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvMedicineName)).setText("Aspirin");
        verify((TextView) vh.itemView.findViewById(R.id.tvDosage)).setText("500mg • tablet");
    }

    @Test
    public void prescriptionBind_withoutForm_noAppend() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("Aspirin", "500mg", null, null, null, "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvDosage)).setText("500mg");
    }

    @Test
    public void prescriptionBind_emptyForm_noAppend() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("Aspirin", "500mg", "", null, null, "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvDosage)).setText("500mg");
    }

    @Test
    public void prescriptionBind_withInstructions_visible() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, "Take with water", null, "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvInstructions)).setText("Take with water");
        verify((TextView) vh.itemView.findViewById(R.id.tvInstructions)).setVisibility(View.VISIBLE);
    }

    @Test
    public void prescriptionBind_nullInstructions_gone() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, null, null, "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvInstructions)).setVisibility(View.GONE);
    }

    @Test
    public void prescriptionBind_emptyInstructions_gone() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, "", null, "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvInstructions)).setVisibility(View.GONE);
    }

    @Test
    public void prescriptionBind_withDate_visible() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, null, "2025-06-15T12:00:00", "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvDate)).setVisibility(View.VISIBLE);
    }

    @Test
    public void prescriptionBind_nullDate_gone() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, null, null, "ACTIVE");
        vh.bind(p);

        verify((TextView) vh.itemView.findViewById(R.id.tvDate)).setVisibility(View.GONE);
    }

    @Test
    public void prescriptionBind_movedStatus_hidesButton() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, null, null, "MOVED");
        vh.bind(p);

        verify((MaterialButton) vh.itemView.findViewById(R.id.btnMoveToCabinet)).setVisibility(View.GONE);
    }

    @Test
    public void prescriptionBind_activeStatus_showsButton() throws Exception {
        PrescriptionAdapter.ViewHolder vh = createPrescriptionViewHolder();
        PrescriptionResponse p = createPrescription("A", "10mg", null, null, null, "ACTIVE");
        vh.bind(p);

        verify((MaterialButton) vh.itemView.findViewById(R.id.btnMoveToCabinet)).setVisibility(View.VISIBLE);
    }
}
