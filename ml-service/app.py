import joblib
import numpy as np
import pandas as pd

from fastapi import FastAPI
from pydantic import BaseModel


bundle = joblib.load("pillulkin_logistic_regression.joblib")

model = bundle["model"]
symptom_cols = bundle["symptom_cols"]
label_encoder = bundle["label_encoder"]
symptom_stats_by_disease = bundle["symptom_stats_by_disease"]

app = FastAPI(title="Pillulkin Logistic Regression Service")


class DiagnosisRequest(BaseModel):
    symptoms: list[str]
    top_k: int = 5
    max_questions: int = 8


class DiagnosisPrediction(BaseModel):
    disease: str
    probability: float


class DiagnosisResponse(BaseModel):
    predictions: list[DiagnosisPrediction]
    unknown_symptoms: list[str]
    suggested_questions: list[str]


def normalize_symptom(symptom: str) -> str:
    return symptom.strip().lower().replace(" ", "_")


def build_patient_vector(current_symptoms: list[str]):
    x = pd.Series(0, index=symptom_cols, dtype=int)
    unknown_symptoms = []

    for symptom in current_symptoms:
        symptom_norm = normalize_symptom(symptom)

        if symptom_norm in x.index:
            x[symptom_norm] = 1
        else:
            unknown_symptoms.append(symptom)

    return x, unknown_symptoms


def predict_top_diseases(current_symptoms: list[str], top_k: int):
    x, unknown_symptoms = build_patient_vector(current_symptoms)
    x_df = x.to_frame().T

    probabilities = model.predict_proba(x_df)[0]

    disease_scores = pd.Series(
        probabilities,
        index=label_encoder.classes_
    ).sort_values(ascending=False)

    predictions = [
        DiagnosisPrediction(
            disease=disease,
            probability=float(probability)
        )
        for disease, probability in disease_scores.head(top_k).items()
    ]

    return predictions, unknown_symptoms, disease_scores.head(top_k), x


def choose_clarifying_symptoms(initial_scores, current_symptoms, max_questions: int):
    known_symptoms = {normalize_symptom(s) for s in current_symptoms}
    top_diseases = initial_scores.index.tolist()

    if len(top_diseases) == 0:
        return []

    disease_weights = initial_scores.copy()

    if disease_weights.sum() > 0:
        disease_weights = disease_weights / disease_weights.sum()
    else:
        disease_weights[:] = 1 / len(disease_weights)

    candidate_rows = []

    for symptom in symptom_cols:
        if symptom in known_symptoms:
            continue

        probs = []

        for disease in top_diseases:
            if disease in symptom_stats_by_disease.index:
                probs.append(float(symptom_stats_by_disease.loc[disease, symptom]))
            else:
                probs.append(0.0)

        probs = np.array(probs)

        spread = probs.max() - probs.min()
        weighted_mean = np.dot(disease_weights.values, probs)

        score = 0.7 * spread + 0.3 * weighted_mean

        if weighted_mean > 0:
            candidate_rows.append((symptom, score, spread, weighted_mean))

    candidate_rows = sorted(
        candidate_rows,
        key=lambda row: (row[1], row[2], row[3]),
        reverse=True
    )

    return [row[0] for row in candidate_rows[:max_questions]]


@app.post("/predict", response_model=DiagnosisResponse)
def predict(request: DiagnosisRequest):
    predictions, unknown_symptoms, initial_scores, _ = predict_top_diseases(
        current_symptoms=request.symptoms,
        top_k=request.top_k
    )

    suggested_questions = choose_clarifying_symptoms(
        initial_scores=initial_scores,
        current_symptoms=request.symptoms,
        max_questions=request.max_questions
    )

    return DiagnosisResponse(
        predictions=predictions,
        unknown_symptoms=unknown_symptoms,
        suggested_questions=suggested_questions
    )
