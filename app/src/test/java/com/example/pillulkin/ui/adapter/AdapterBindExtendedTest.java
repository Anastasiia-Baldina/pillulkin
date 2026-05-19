package com.example.pillulkin.ui.adapter;

import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.pillulkin.R;
import com.example.pillulkin.data.local.entity.Reminder;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;
import com.google.android.material.switchmaterial.SwitchMaterial;

import org.junit.Test;

import java.lang.reflect.Constructor;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class AdapterBindExtendedTest {

    private View createMockItemView(int... viewIds) {
        View itemView = mock(View.class);
        when(itemView.getContext()).thenReturn(mock(android.content.Context.class));
        for (int id : viewIds) {
            Object v = id == R.id.switchEnabled ? mock(SwitchMaterial.class)
                    : id == R.id.btnDelete ? mock(ImageButton.class)
                    : mock(TextView.class);
            when(itemView.findViewById(id)).thenReturn(v);
        }
        return itemView;
    }

    private Object createStaticVh(Class<?> vhClass, View itemView, Object... outerArgs) throws Exception {
        Constructor<?>[] ctors = vhClass.getDeclaredConstructors();
        for (Constructor<?> ctor : ctors) {
            ctor.setAccessible(true);
            if (ctor.getParameterCount() == 1) {
                return ctor.newInstance(itemView);
            }
        }
        for (Constructor<?> ctor : ctors) {
            ctor.setAccessible(true);
            if (ctor.getParameterCount() == 2 && outerArgs.length == 1) {
                return ctor.newInstance(outerArgs[0], itemView);
            }
        }
        throw new Exception("No suitable constructor for " + vhClass.getName() +
                " ctors=" + java.util.Arrays.toString(ctors));
    }

    private Object createInnerVh(Class<?> outerClass, Object outerInstance, Class<?> vhClass, View itemView) throws Exception {
        String vhClassName = outerClass.getName() + "$" + vhClass.getSimpleName();
        Class<?> fullVhClass = Class.forName(vhClassName);
        Constructor<?>[] ctors = fullVhClass.getDeclaredConstructors();
        for (Constructor<?> ctor : ctors) {
            ctor.setAccessible(true);
            if (ctor.getParameterCount() == 2) {
                return ctor.newInstance(outerInstance, itemView);
            }
        }
        throw new RuntimeException("No suitable constructor found for " + fullVhClass.getName());
    }

    private void invokeBind(Object viewHolder, Object... args) throws Exception {
        java.lang.reflect.Method[] methods = viewHolder.getClass().getDeclaredMethods();
        for (java.lang.reflect.Method m : methods) {
            if (m.getName().equals("bind") && m.getParameterCount() == args.length) {
                m.setAccessible(true);
                m.invoke(viewHolder, args);
                return;
            }
        }
        throw new RuntimeException("bind method not found on " + viewHolder.getClass().getName());
    }

    @Test
    public void reminderAdapter_bind_setsTime() throws Exception {
        ReminderAdapter adapter = spy(new ReminderAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1); r.setHour(8); r.setMinute(30); r.setMedicineName("Aspirin"); r.setEnabled(true);
        adapter.submitList(java.util.Collections.singletonList(r));

        View itemView = createMockItemView(R.id.tvTime, R.id.tvText, R.id.switchEnabled, R.id.btnDelete);
        ReminderAdapter.ViewHolder vh = (ReminderAdapter.ViewHolder) createStaticVh(
                ReminderAdapter.ViewHolder.class, itemView);
        adapter.onBindViewHolder(vh, 0);

        verify((TextView) itemView.findViewById(R.id.tvTime)).setText("08:30");
        verify((TextView) itemView.findViewById(R.id.tvText)).setText("Не забудьте принять Aspirin");
    }

    @Test
    public void reminderAdapter_bind_checkedState() throws Exception {
        ReminderAdapter adapter = spy(new ReminderAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1); r.setHour(8); r.setMinute(30); r.setEnabled(true);
        adapter.submitList(java.util.Collections.singletonList(r));

        View itemView = createMockItemView(R.id.tvTime, R.id.tvText, R.id.switchEnabled, R.id.btnDelete);
        ReminderAdapter.ViewHolder vh = (ReminderAdapter.ViewHolder) createStaticVh(
                ReminderAdapter.ViewHolder.class, itemView);
        adapter.onBindViewHolder(vh, 0);

        verify((SwitchMaterial) itemView.findViewById(R.id.switchEnabled)).setChecked(true);
    }

    @Test
    public void reminderAdapter_bind_disabledState() throws Exception {
        ReminderAdapter adapter = spy(new ReminderAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1); r.setHour(8); r.setMinute(30); r.setEnabled(false);
        adapter.submitList(java.util.Collections.singletonList(r));

        View itemView = createMockItemView(R.id.tvTime, R.id.tvText, R.id.switchEnabled, R.id.btnDelete);
        ReminderAdapter.ViewHolder vh = (ReminderAdapter.ViewHolder) createStaticVh(
                ReminderAdapter.ViewHolder.class, itemView);
        adapter.onBindViewHolder(vh, 0);

        verify((SwitchMaterial) itemView.findViewById(R.id.switchEnabled)).setChecked(false);
    }

    @Test
    public void reminderAdapter_bind_customText() throws Exception {
        ReminderAdapter adapter = spy(new ReminderAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1); r.setHour(14); r.setMinute(0); r.setCustomText("После еды");
        adapter.submitList(java.util.Collections.singletonList(r));

        View itemView = createMockItemView(R.id.tvTime, R.id.tvText, R.id.switchEnabled, R.id.btnDelete);
        ReminderAdapter.ViewHolder vh = (ReminderAdapter.ViewHolder) createStaticVh(
                ReminderAdapter.ViewHolder.class, itemView);
        adapter.onBindViewHolder(vh, 0);

        verify((TextView) itemView.findViewById(R.id.tvText)).setText("После еды");
    }

    @Test
    public void reminderAdapter_bind_midnight() throws Exception {
        ReminderAdapter adapter = spy(new ReminderAdapter(null));
        doNothing().when(adapter).notifyDataSetChanged();
        Reminder r = new Reminder();
        r.setId(1); r.setHour(0); r.setMinute(0); r.setEnabled(true);
        adapter.submitList(java.util.Collections.singletonList(r));

        View itemView = createMockItemView(R.id.tvTime, R.id.tvText, R.id.switchEnabled, R.id.btnDelete);
        ReminderAdapter.ViewHolder vh = (ReminderAdapter.ViewHolder) createStaticVh(
                ReminderAdapter.ViewHolder.class, itemView);
        adapter.onBindViewHolder(vh, 0);

        verify((TextView) itemView.findViewById(R.id.tvTime)).setText("00:00");
    }

    @Test
    public void recommendationSectionAdapter_headerBind() throws Exception {
        View itemView = createMockItemView(R.id.tvSectionTitle);
        Object vh = createStaticVh(RecommendationSectionAdapter.HeaderViewHolder.class, itemView);

        invokeBind(vh, "Test Section");

        verify((TextView) itemView.findViewById(R.id.tvSectionTitle)).setText("Test Section");
    }

    @Test
    public void recommendationSectionAdapter_medicineBind_withForm() throws Exception {
        View itemView = createMockItemView(R.id.tvMedicineName, R.id.tvDosage, R.id.tvReason);
        RecommendationSectionAdapter adapter = new RecommendationSectionAdapter(null);
        Object vh = createInnerVh(RecommendationSectionAdapter.class, adapter,
                RecommendationSectionAdapter.MedicineViewHolder.class, itemView);

        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        med.setId(1L); med.setName("Aspirin"); med.setDosage("500mg"); med.setForm("tablet");
        invokeBind(vh, med, false);

        verify((TextView) itemView.findViewById(R.id.tvMedicineName)).setText("Aspirin");
        verify((TextView) itemView.findViewById(R.id.tvDosage)).setText("500mg • tablet");
    }

    @Test
    public void recommendationSectionAdapter_medicineBind_noForm() throws Exception {
        View itemView = createMockItemView(R.id.tvMedicineName, R.id.tvDosage, R.id.tvReason);
        RecommendationSectionAdapter adapter = new RecommendationSectionAdapter(null);
        Object vh = createInnerVh(RecommendationSectionAdapter.class, adapter,
                RecommendationSectionAdapter.MedicineViewHolder.class, itemView);

        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        med.setId(1L); med.setName("Ibuprofen"); med.setDosage("400mg");
        invokeBind(vh, med, false);

        verify((TextView) itemView.findViewById(R.id.tvDosage)).setText("400mg");
    }

    @Test
    public void recommendationSectionAdapter_medicineBind_fromCabinet_showsReason() throws Exception {
        View itemView = createMockItemView(R.id.tvMedicineName, R.id.tvDosage, R.id.tvReason);
        RecommendationSectionAdapter adapter = new RecommendationSectionAdapter(null);
        Object vh = createInnerVh(RecommendationSectionAdapter.class, adapter,
                RecommendationSectionAdapter.MedicineViewHolder.class, itemView);

        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        med.setId(1L); med.setName("Test"); med.setDosage("10mg");
        invokeBind(vh, med, true);

        verify((TextView) itemView.findViewById(R.id.tvReason)).setVisibility(View.VISIBLE);
    }

    @Test
    public void recommendationSectionAdapter_medicineBind_notFromCabinet_hidesReason() throws Exception {
        View itemView = createMockItemView(R.id.tvMedicineName, R.id.tvDosage, R.id.tvReason);
        RecommendationSectionAdapter adapter = new RecommendationSectionAdapter(null);
        Object vh = createInnerVh(RecommendationSectionAdapter.class, adapter,
                RecommendationSectionAdapter.MedicineViewHolder.class, itemView);

        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        med.setId(1L); med.setName("Test"); med.setDosage("10mg");
        invokeBind(vh, med, false);

        verify((TextView) itemView.findViewById(R.id.tvReason)).setVisibility(View.GONE);
    }

    @Test
    public void recommendationSectionAdapter_emptyForm() throws Exception {
        View itemView = createMockItemView(R.id.tvMedicineName, R.id.tvDosage, R.id.tvReason);
        RecommendationSectionAdapter adapter = new RecommendationSectionAdapter(null);
        Object vh = createInnerVh(RecommendationSectionAdapter.class, adapter,
                RecommendationSectionAdapter.MedicineViewHolder.class, itemView);

        ReferenceMedicineResponse med = new ReferenceMedicineResponse();
        med.setId(1L); med.setName("Test"); med.setDosage("20mg"); med.setForm("");
        invokeBind(vh, med, false);

        verify((TextView) itemView.findViewById(R.id.tvDosage)).setText("20mg");
    }
}
