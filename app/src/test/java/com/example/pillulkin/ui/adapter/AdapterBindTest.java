package com.example.pillulkin.ui.adapter;

import android.content.Context;
import android.view.View;
import android.widget.TextView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.google.android.material.card.MaterialCardView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.lang.reflect.Constructor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdapterBindTest {

    private MaterialCardView cardView;
    private TextView tvName;
    private TextView tvDosage;
    private TextView tvExpiration;
    private TextView tvQuantity;
    private TextView tvStatus;
    private Context context;
    private MockedStatic<androidx.core.content.ContextCompat> contextCompatMock;
    private MedicineAdapter.OnMedicineClickListener listener;
    private MedicineAdapter adapter;
    private MedicineAdapter.MedicineViewHolder holder;

    @Before
    public void setUp() throws Exception {
        cardView = mock(MaterialCardView.class);
        tvName = mock(TextView.class);
        tvDosage = mock(TextView.class);
        tvExpiration = mock(TextView.class);
        tvQuantity = mock(TextView.class);
        tvStatus = mock(TextView.class);
        context = mock(Context.class);

        when(cardView.getContext()).thenReturn(context);
        when(cardView.findViewById(R.id.tvMedicineName)).thenReturn(tvName);
        when(cardView.findViewById(R.id.tvDosage)).thenReturn(tvDosage);
        when(cardView.findViewById(R.id.tvExpiration)).thenReturn(tvExpiration);
        when(cardView.findViewById(R.id.tvQuantity)).thenReturn(tvQuantity);
        when(cardView.findViewById(R.id.tvStatus)).thenReturn(tvStatus);

        contextCompatMock = mockStatic(androidx.core.content.ContextCompat.class);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.card_background))).thenReturn(0x11111111);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.on_background))).thenReturn(0x22222222);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.hint))).thenReturn(0x33333333);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.expired))).thenReturn(0x44444444);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.expiration_warning))).thenReturn(0x55555555);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.warning_text))).thenReturn(0x66666666);
        contextCompatMock.when(() -> androidx.core.content.ContextCompat.getColor(
                eq(context), eq(R.color.expired_text))).thenReturn(0x77777777);

        when(context.getString(R.string.medicine_expiration)).thenReturn("Срок годности");

        listener = mock(MedicineAdapter.OnMedicineClickListener.class);
        adapter = new MedicineAdapter(listener);

        Constructor<MedicineAdapter.MedicineViewHolder> ctor =
                MedicineAdapter.MedicineViewHolder.class.getDeclaredConstructor(
                        MedicineAdapter.class, View.class);
        ctor.setAccessible(true);
        holder = ctor.newInstance(adapter, cardView);
    }

    @After
    public void tearDown() {
        contextCompatMock.close();
    }

    private PatientMedicineResponse createMedicine(String name, String dosage,
                                                    String expirationDate, String quantity) {
        PatientMedicineResponse m = new PatientMedicineResponse();
        m.setId(1L);
        m.setMedicineName(name);
        m.setDosage(dosage);
        m.setExpirationDate(expirationDate);
        m.setQuantity(quantity);
        return m;
    }

    @Test
    public void bind_normalExpiration_defaultColors_statusGone() {
        PatientMedicineResponse med = createMedicine("Aspirin", "100mg", "2099-12-31", "10");
        holder.bind(med);
        verify(tvName).setText("Aspirin");
        verify(cardView).setCardBackgroundColor(0x11111111);
        verify(tvName).setTextColor(0x22222222);
        verify(tvStatus).setVisibility(View.GONE);
    }

    @Test
    public void bind_expiredDate_expiredColors_statusVisible() {
        PatientMedicineResponse med = createMedicine("Aspirin", "100mg", "2020-01-01", "10");
        holder.bind(med);
        verify(cardView).setCardBackgroundColor(0x44444444);
        verify(tvName).setTextColor(0x77777777);
        verify(tvStatus).setText(R.string.medicine_expired_label);
        verify(tvStatus).setVisibility(View.VISIBLE);
    }

    @Test
    public void bind_nullExpiration_normalStatus() {
        PatientMedicineResponse med = createMedicine("Aspirin", "100mg", null, null);
        holder.bind(med);
        verify(cardView).setCardBackgroundColor(0x11111111);
        verify(tvStatus).setVisibility(View.GONE);
        verify(tvExpiration).setVisibility(View.GONE);
    }

    @Test
    public void bind_withQuantity_visible() {
        PatientMedicineResponse med = createMedicine("Aspirin", "100mg", "2099-12-31", "10 шт");
        holder.bind(med);
        verify(tvQuantity).setText("10 шт");
        verify(tvQuantity).setVisibility(View.VISIBLE);
    }

    @Test
    public void bind_withoutQuantity_gone() {
        PatientMedicineResponse med = createMedicine("Aspirin", "100mg", "2099-12-31", null);
        holder.bind(med);
        verify(tvQuantity).setVisibility(View.GONE);
    }

    @Test
    public void bind_click_callsListener() {
        PatientMedicineResponse med = createMedicine("Aspirin", "100mg", null, null);
        holder.bind(med);
        ArgumentCaptor<View.OnClickListener> captor =
                ArgumentCaptor.forClass(View.OnClickListener.class);
        verify(cardView).setOnClickListener(captor.capture());
        captor.getValue().onClick(cardView);
        verify(listener).onMedicineClick(med);
    }
}
