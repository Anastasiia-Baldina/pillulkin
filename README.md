# Умная аптечка Пилюлькин

Android-приложение для ведения домашней аптечки пациента и предоставления врачу временного доступа к аптечке.

## Архитектура

Приложение построено по архитектуре MVVM с использованием:
- **Java** — основной язык разработки
- **Room** — локальная база данных
- **Navigation Component** — навигация между экранами
- **LiveData + ViewModel** — реактивное обновление UI
- **Material Design 3** — дизайн-система
- **Repository Pattern** — абстракция доступа к данным

## Структура проекта

```
com.example.pillulkin/
├── data/
│   ├── local/
│   │   ├── dao/           # DAO интерфейсы Room
│   │   └── entity/        # Room сущности
│   └── repository/        # Репозитории
├── domain/
│   ├── model/             # Доменные модели
│   └── usecase/           # Use cases
├── ui/
│   ├── patient/           # Экраны пациента
│   ├── doctor/            # Экраны врача
│   ├── common/            # Общие компоненты
│   └── adapter/           # RecyclerView адаптеры
└── utils/                 # Утилиты
```

## Экраны приложения

### Общие
- Выбор роли (Пациент/Врач)

### Для пациента
- Список лекарств (с поиском и сортировкой)
- Добавление лекарства
- Редактирование лекарства
- Карточка лекарства
- Профиль пациента
- Генерация кода доступа для врача

### Для врача
- Вход по временному коду
- Просмотр аптечки пациента
- Подбор лекарств по диагнозу/названию
- Результаты рекомендаций

## Сборка проекта

### Требования
- Android Studio Ladybug или новее
- JDK 11+ (используется JDK из Android Studio)
- Android SDK 36

### Сборка APK
```bash
./gradlew assembleDebug
```

APK будет создан в: `app/build/outputs/apk/debug/app-debug.apk`

### Сборка Release
```bash
./gradlew assembleRelease
```

## Запуск тестов

### Unit тесты
```bash
./gradlew testDebugUnitTest
```

### UI тесты (Espresso)
```bash
./gradlew connectedDebugAndroidTest
```
Примечание: Требуется запущенный эмулятор или подключенное устройство.

### Все тесты
```bash
./gradlew test
```

## Проверка покрытия тестами

### Генерация отчета JaCoCo
```bash
./gradlew jacocoTestReport
```

Отчет будет создан в: `app/build/reports/jacoco/jacocoTestReport/html/index.html`

### Проверка минимального покрытия (80%)
```bash
./gradlew jacocoTestCoverageVerification
```

## Линтеры

### Android Lint
```bash
./gradlew lintDebug
```

Отчет будет создан в: `app/build/reports/lint-results-debug.html`

## Quality Gates

Перед завершением этапа разработки необходимо выполнить:

1. **Сборка**: `./gradlew assembleDebug` — успешная сборка
2. **Unit тесты**: `./gradlew testDebugUnitTest` — 100% pass
3. **Lint**: `./gradlew lintDebug` — 0 критических ошибок

## Цветовая палитра

Приложение использует мягкую пастельную палитру:
- **Лавандовый** (#B8A9C9) — основной цвет
- **Пастельный бирюзовый** (#88C9BF) — вторичный цвет
- **Пастельный зелёный** (#A8D5A2) — третичный цвет

## Демо-данные

При первом запуске база данных автоматически заполняется:
- Словарём аналогов лекарств (21 запись)
- Маппингом диагнозов на препараты (21 запись)

## Сущности данных

- **PatientProfile** — профиль пациента
- **Medicine** — лекарство
- **DoctorAccessCode** — временный код доступа
- **AnalogueDictionaryEntry** — словарь аналогов
- **DiagnosisMappingEntry** — маппинг диагнозов
- **RecommendationItem** — элемент рекомендации
- **RecommendationResult** — результат подбора

## Лицензия

Учебный проект. Не предназначен для реального медицинского использования.
