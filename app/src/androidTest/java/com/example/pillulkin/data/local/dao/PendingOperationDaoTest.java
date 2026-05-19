package com.example.pillulkin.data.local.dao;

import androidx.room.Room;
import androidx.test.core.app.ApplicationProvider;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import com.example.pillulkin.data.local.PillulkinDatabase;
import com.example.pillulkin.data.local.entity.PendingOperation;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

import static org.junit.Assert.*;

@RunWith(AndroidJUnit4.class)
public class PendingOperationDaoTest {

    private PillulkinDatabase db;
    private PendingOperationDao dao;

    @Before
    public void setUp() {
        db = Room.inMemoryDatabaseBuilder(
                ApplicationProvider.getApplicationContext(),
                PillulkinDatabase.class
        ).allowMainThreadQueries().build();
        dao = db.pendingOperationDao();
    }

    @After
    public void tearDown() {
        db.close();
    }

    private PendingOperation createOperation(String type, String status) {
        PendingOperation op = new PendingOperation();
        op.setType(type);
        op.setPayload("{\"test\":true}");
        op.setStatus(status);
        op.setRetryCount(0);
        op.setCreatedAt(System.currentTimeMillis());
        return op;
    }

    @Test
    public void insert_andReadPending() {
        PendingOperation op = createOperation(PendingOperation.TYPE_SYMPTOM_ADD, PendingOperation.STATUS_PENDING);
        long id = dao.insert(op);
        assertTrue(id > 0);

        List<PendingOperation> pending = dao.getPending();
        assertEquals(1, pending.size());
        assertEquals(PendingOperation.TYPE_SYMPTOM_ADD, pending.get(0).getType());
        assertEquals(PendingOperation.STATUS_PENDING, pending.get(0).getStatus());
    }

    @Test
    public void getPending_excludesFailed() {
        dao.insert(createOperation(PendingOperation.TYPE_SYMPTOM_ADD, PendingOperation.STATUS_PENDING));
        dao.insert(createOperation(PendingOperation.TYPE_MEDICINE_ADD, PendingOperation.STATUS_FAILED));
        dao.insert(createOperation(PendingOperation.TYPE_PROFILE_UPDATE, PendingOperation.STATUS_SYNCING));

        List<PendingOperation> pending = dao.getPending();
        assertEquals(2, pending.size());
        for (PendingOperation op : pending) {
            assertNotEquals(PendingOperation.STATUS_FAILED, op.getStatus());
        }
    }

    @Test
    public void getPending_orderedByCreatedAtAsc() throws InterruptedException {
        PendingOperation op1 = createOperation("TYPE_A", PendingOperation.STATUS_PENDING);
        op1.setCreatedAt(1000L);
        dao.insert(op1);
        Thread.sleep(10);

        PendingOperation op2 = createOperation("TYPE_B", PendingOperation.STATUS_PENDING);
        op2.setCreatedAt(2000L);
        dao.insert(op2);

        List<PendingOperation> pending = dao.getPending();
        assertEquals(2, pending.size());
        assertTrue(pending.get(0).getCreatedAt() <= pending.get(1).getCreatedAt());
    }

    @Test
    public void deleteById() {
        long id = dao.insert(createOperation(PendingOperation.TYPE_SYMPTOM_DELETE, PendingOperation.STATUS_PENDING));
        assertEquals(1, dao.getPending().size());

        dao.deleteById(id);
        assertTrue(dao.getPending().isEmpty());
    }

    @Test
    public void update() {
        long id = dao.insert(createOperation(PendingOperation.TYPE_SYMPTOM_ADD, PendingOperation.STATUS_PENDING));

        PendingOperation op = dao.getPending().get(0);
        op.setStatus(PendingOperation.STATUS_FAILED);
        op.setRetryCount(3);
        dao.update(op);

        assertTrue(dao.getPending().isEmpty());
        assertEquals(0, dao.getPendingCount());
    }

    @Test
    public void getPendingCount() {
        dao.insert(createOperation(PendingOperation.TYPE_SYMPTOM_ADD, PendingOperation.STATUS_PENDING));
        dao.insert(createOperation(PendingOperation.TYPE_MEDICINE_ADD, PendingOperation.STATUS_PENDING));
        dao.insert(createOperation(PendingOperation.TYPE_PROFILE_UPDATE, PendingOperation.STATUS_FAILED));

        assertEquals(2, dao.getPendingCount());
    }

    @Test
    public void getPendingCount_empty() {
        assertEquals(0, dao.getPendingCount());
    }

    @Test
    public void insert_preservesAllFields() {
        PendingOperation op = new PendingOperation();
        op.setType(PendingOperation.TYPE_MEDICINE_DELETE);
        op.setPayload("{\"medicineId\":42}");
        op.setEntityId(42L);
        op.setStatus(PendingOperation.STATUS_PENDING);
        op.setRetryCount(2);
        op.setCreatedAt(5000L);
        long id = dao.insert(op);

        PendingOperation read = dao.getPending().get(0);
        assertEquals(PendingOperation.TYPE_MEDICINE_DELETE, read.getType());
        assertEquals("{\"medicineId\":42}", read.getPayload());
        assertEquals(Long.valueOf(42L), read.getEntityId());
        assertEquals(PendingOperation.STATUS_PENDING, read.getStatus());
        assertEquals(2, read.getRetryCount());
        assertEquals(5000L, read.getCreatedAt());
    }

    @Test
    public void insert_returnsDifferentIds() {
        long id1 = dao.insert(createOperation("A", PendingOperation.STATUS_PENDING));
        long id2 = dao.insert(createOperation("B", PendingOperation.STATUS_PENDING));
        assertNotEquals(id1, id2);
    }

    @Test
    public void update_toSyncingStatus() {
        dao.insert(createOperation(PendingOperation.TYPE_SYMPTOM_ADD, PendingOperation.STATUS_PENDING));

        PendingOperation op = dao.getPending().get(0);
        op.setStatus(PendingOperation.STATUS_SYNCING);
        dao.update(op);

        List<PendingOperation> pending = dao.getPending();
        assertEquals(1, pending.size());
        assertEquals(PendingOperation.STATUS_SYNCING, pending.get(0).getStatus());
    }

    @Test
    public void deleteById_nonExistent_noError() {
        dao.deleteById(999);
        assertEquals(0, dao.getPendingCount());
    }
}
