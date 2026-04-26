package com.example.pillulkin.data.remote;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.pillulkin.data.remote.model.AuthResponse;
import com.example.pillulkin.data.remote.model.DiagnosisRequest;
import com.example.pillulkin.data.remote.model.DiagnosisResponse;
import com.example.pillulkin.data.remote.model.DoctorCodeResponse;
import com.example.pillulkin.data.remote.model.DoctorFullDataResponse;
import com.example.pillulkin.data.remote.model.DoctorLoginRequest;
import com.example.pillulkin.data.remote.model.GenerateCodeRequest;
import com.example.pillulkin.data.remote.model.GoogleAuthRequest;
import com.example.pillulkin.data.remote.model.PatientLoginRequest;
import com.example.pillulkin.data.remote.model.PatientMedicineRequest;
import com.example.pillulkin.data.remote.model.PatientMedicineResponse;
import com.example.pillulkin.data.remote.model.PatientProfileRequest;
import com.example.pillulkin.data.remote.model.PatientProfileResponse;
import com.example.pillulkin.data.remote.model.PatientRegisterRequest;
import com.example.pillulkin.data.remote.model.PatientSymptomRequest;
import com.example.pillulkin.data.remote.model.PatientSymptomResponse;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class NetworkModule {
    private static final String BASE_URL = "http://10.0.2.2:8080/";
    private static final String PREFS_NAME = "pillulkin_prefs";
    private static final String KEY_PATIENT_ID = "patient_id";
    private static final String KEY_PATIENT_TOKEN = "patient_token";
    private static final String KEY_DOCTOR_TOKEN = "doctor_token";
    private static final String KEY_DOCTOR_PATIENT_ID = "doctor_patient_id";
    private static final String KEY_DOCTOR_EXPIRES = "doctor_expires";

    private static NetworkModule instance;
    private final PillulkinApi api;
    private final SharedPreferences prefs;

    private NetworkModule(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        OkHttpClient client = new OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(client)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        api = retrofit.create(PillulkinApi.class);
    }

    public static synchronized NetworkModule getInstance(Context context) {
        if (instance == null) {
            instance = new NetworkModule(context);
        }
        return instance;
    }

    public PillulkinApi getApi() {
        return api;
    }

    public long getPatientId() {
        return prefs.getLong(KEY_PATIENT_ID, -1);
    }

    public void savePatientId(long patientId) {
        prefs.edit().putLong(KEY_PATIENT_ID, patientId).apply();
    }

    public String getPatientToken() {
        return prefs.getString(KEY_PATIENT_TOKEN, null);
    }

    public void savePatientToken(String token) {
        prefs.edit().putString(KEY_PATIENT_TOKEN, token).apply();
    }

    public String getDoctorToken() {
        return prefs.getString(KEY_DOCTOR_TOKEN, null);
    }

    public void saveDoctorToken(String token) {
        prefs.edit().putString(KEY_DOCTOR_TOKEN, token).commit();
    }

    public long getDoctorPatientId() {
        return prefs.getLong(KEY_DOCTOR_PATIENT_ID, -1);
    }

    public void saveDoctorPatientId(long patientId) {
        prefs.edit().putLong(KEY_DOCTOR_PATIENT_ID, patientId).commit();
    }

    public long getDoctorExpires() {
        return prefs.getLong(KEY_DOCTOR_EXPIRES, 0);
    }

    public void saveDoctorExpires(long expiresAtMillis) {
        prefs.edit().putLong(KEY_DOCTOR_EXPIRES, expiresAtMillis).commit();
    }

    public boolean isPatientLoggedIn() {
        return getPatientId() > 0;
    }

    public boolean isDoctorLoggedIn() {
        return getDoctorToken() != null && getDoctorPatientId() > 0
                && System.currentTimeMillis() < getDoctorExpires();
    }

    public void clearPatientSession() {
        prefs.edit()
                .remove(KEY_PATIENT_ID)
                .remove(KEY_PATIENT_TOKEN)
                .apply();
    }

    public void clearDoctorSession() {
        prefs.edit()
                .remove(KEY_DOCTOR_TOKEN)
                .remove(KEY_DOCTOR_PATIENT_ID)
                .remove(KEY_DOCTOR_EXPIRES)
                .apply();
    }

    public Call<AuthResponse> register(String email, String password) {
        return api.registerPatient(new PatientRegisterRequest(email, password));
    }

    public Call<AuthResponse> login(String email, String password) {
        return api.loginPatient(new PatientLoginRequest(email, password));
    }

    public Call<AuthResponse> doctorLogin(String code) {
        return api.loginDoctor(new DoctorLoginRequest(code));
    }

    public Call<AuthResponse> loginWithGoogle(String idToken) {
        return api.loginWithGoogle(new GoogleAuthRequest(idToken));
    }

    public Call<DoctorCodeResponse> generateDoctorCode(long patientId, int expiresInMinutes) {
        return api.generateDoctorCode(new GenerateCodeRequest(patientId, expiresInMinutes));
    }

    public Call<PatientProfileResponse> getProfile() {
        return api.getProfile(getPatientId());
    }

    public Call<PatientProfileResponse> updateProfile(PatientProfileRequest request) {
        return api.updateProfile(getPatientId(), request);
    }

    public Call<List<PatientSymptomResponse>> getSymptoms() {
        return api.getSymptoms(getPatientId());
    }

    public Call<PatientSymptomResponse> addSymptom(String symptom) {
        return api.addSymptom(getPatientId(), new PatientSymptomRequest(symptom));
    }

    public Call<Void> deleteSymptom(long symptomId) {
        return api.deleteSymptom(getPatientId(), symptomId);
    }

    public Call<PatientSymptomResponse> renewSymptom(long symptomId) {
        return api.renewSymptom(getPatientId(), symptomId);
    }

    public Call<List<PatientMedicineResponse>> getMedicines() {
        return api.getMedicines(getPatientId());
    }

    public Call<PatientMedicineResponse> addMedicine(long medicineId, String expirationDate, String quantity) {
        return api.addMedicine(getPatientId(), new PatientMedicineRequest(medicineId, expirationDate, quantity));
    }

    public Call<Void> deleteMedicine(long medicineId) {
        return api.deleteMedicine(getPatientId(), medicineId);
    }

    public Call<PatientMedicineResponse> updateMedicine(long patientMedicineId, String expirationDate, String quantity) {
        return api.updateMedicine(getPatientId(), patientMedicineId,
                new PatientMedicineRequest(null, expirationDate, quantity));
    }

    public Call<List<ReferenceMedicineResponse>> searchMedicines(String query) {
        return api.getReferenceMedicines(query);
    }

    public Call<List<ReferenceMedicineResponse>> getRecommendations(List<String> symptoms) {
        return api.getRecommendations(symptoms);
    }

    public Call<DoctorFullDataResponse> getDoctorPatientData() {
        return api.getPatientFullData(getDoctorToken(), getDoctorPatientId());
    }

    public Call<DiagnosisResponse> diagnose(List<Integer> binarySymptoms) {
        return api.diagnose(new DiagnosisRequest(binarySymptoms));
    }

    @Deprecated
    public boolean isLoggedIn() {
        return isPatientLoggedIn();
    }

    @Deprecated
    public void saveToken(String token) {
        savePatientToken(token);
    }

    @Deprecated
    public String getToken() {
        return getPatientToken();
    }

    @Deprecated
    public void clearToken() {
        clearPatientSession();
    }
}
