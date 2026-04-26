package com.example.pillulkin.data.remote;

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
import com.example.pillulkin.data.remote.model.PrescriptionRequest;
import com.example.pillulkin.data.remote.model.PrescriptionResponse;
import com.example.pillulkin.data.remote.model.ReferenceMedicineResponse;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface PillulkinApi {

    @POST("api/v1/auth/patient/register")
    Call<AuthResponse> registerPatient(@Body PatientRegisterRequest request);

    @POST("api/v1/auth/patient/login")
    Call<AuthResponse> loginPatient(@Body PatientLoginRequest request);

    @POST("api/v1/auth/patient/google")
    Call<AuthResponse> loginWithGoogle(@Body GoogleAuthRequest request);

    @POST("api/v1/auth/doctor/generate-code")
    Call<DoctorCodeResponse> generateDoctorCode(@Body GenerateCodeRequest request);

    @POST("api/v1/auth/doctor/login")
    Call<AuthResponse> loginDoctor(@Body DoctorLoginRequest request);

    @GET("api/v1/patients/{patientId}/profile")
    Call<PatientProfileResponse> getProfile(@Path("patientId") long patientId);

    @PUT("api/v1/patients/{patientId}/profile")
    Call<PatientProfileResponse> updateProfile(
            @Path("patientId") long patientId,
            @Body PatientProfileRequest request);

    @GET("api/v1/patients/{patientId}/symptoms")
    Call<List<PatientSymptomResponse>> getSymptoms(@Path("patientId") long patientId);

    @POST("api/v1/patients/{patientId}/symptoms")
    Call<PatientSymptomResponse> addSymptom(
            @Path("patientId") long patientId,
            @Body PatientSymptomRequest request);

    @DELETE("api/v1/patients/{patientId}/symptoms/{symptomId}")
    Call<Void> deleteSymptom(
            @Path("patientId") long patientId,
            @Path("symptomId") long symptomId);

    @PUT("api/v1/patients/{patientId}/symptoms/{symptomId}/renew")
    Call<PatientSymptomResponse> renewSymptom(
            @Path("patientId") long patientId,
            @Path("symptomId") long symptomId);

    @GET("api/v1/patients/{patientId}/medicines")
    Call<List<PatientMedicineResponse>> getMedicines(@Path("patientId") long patientId);

    @POST("api/v1/patients/{patientId}/medicines")
    Call<PatientMedicineResponse> addMedicine(
            @Path("patientId") long patientId,
            @Body PatientMedicineRequest request);

    @DELETE("api/v1/patients/{patientId}/medicines/{medicineId}")
    Call<Void> deleteMedicine(
            @Path("patientId") long patientId,
            @Path("medicineId") long medicineId);

    @PUT("api/v1/patients/{patientId}/medicines/{patientMedicineId}")
    Call<PatientMedicineResponse> updateMedicine(
            @Path("patientId") long patientId,
            @Path("patientMedicineId") long patientMedicineId,
            @Body PatientMedicineRequest request);

    @GET("api/v1/medicines")
    Call<List<ReferenceMedicineResponse>> getReferenceMedicines(
            @Query("search") String query);

    @GET("api/v1/medicines/recommendations")
    Call<List<ReferenceMedicineResponse>> getRecommendations(
            @Query("symptoms") List<String> symptoms);

    @POST("api/v1/diagnose")
    Call<DiagnosisResponse> diagnose(@Body DiagnosisRequest request);

    @GET("api/v1/doctor/validate")
    Call<Map<String, Boolean>> validateDoctorToken(
            @Header("X-Doctor-Token") String token);

    @GET("api/v1/doctor/patients/{patientId}/full-data")
    Call<DoctorFullDataResponse> getPatientFullData(
            @Header("X-Doctor-Token") String token,
            @Path("patientId") long patientId);

    @POST("api/v1/patients/{patientId}/prescriptions")
    Call<PrescriptionResponse> createPrescription(
            @Path("patientId") long patientId,
            @Body PrescriptionRequest request);

    @GET("api/v1/patients/{patientId}/prescriptions")
    Call<List<PrescriptionResponse>> getPrescriptions(
            @Path("patientId") long patientId,
            @Query("status") String status);

    @PUT("api/v1/patients/{patientId}/prescriptions/{prescriptionId}/move-to-cabinet")
    Call<Void> moveToCabinet(
            @Path("patientId") long patientId,
            @Path("prescriptionId") long prescriptionId);
}
