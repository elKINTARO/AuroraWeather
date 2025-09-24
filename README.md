# 🌅 Aurora Weather

[![Android](https://img.shields.io/badge/Android-3DDC84?style=for-the-badge&logo=android&logoColor=white)](https://www.android.com/)
[![Java](https://img.shields.io/badge/Java-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![API](https://img.shields.io/badge/API-26%2B-brightgreen?style=for-the-badge)](https://developer.android.com/about/versions/oreo)
[![License](https://img.shields.io/badge/License-MIT-yellow?style=for-the-badge)](LICENSE)

**Aurora Weather** - це сучасний Android додаток для прогнозу погоди з красивим інтерфейсом, анімаціями та повною підтримкою української та англійської мов.

## ✨ Особливості

### 🌤️ Погодні дані
- **Поточна погода** з детальними метриками
- **5-денний прогноз** з анімаціями
- **Погодинний прогноз** на 24 години
- **Розширені метрики**: вологість, тиск, видимість, UV-індекс
- **Автоматичне визначення локації** або пошук міст

### 🎨 Інтерфейс
- **Material Design 3** компоненти
- **Lottie анімації** для різних типів погоди
- **Адаптивні фони** залежно від погодних умов
- **Флип-анімація** між основним видом та погодинним прогнозом
- **Темна/світла тема** з автоматичною адаптацією

### ⚙️ Налаштування
- **Одиниці температури**: Цельсій, Фаренгейт, Кельвін
- **Одиниці швидкості вітру**: м/с, км/год, миль/год
- **Теми**: Світла, темна, системна
- **Мови**: Українська, англійська
- **Історія пошуку** останніх 10 міст

### 🌍 Локалізація
- Повна підтримка **української** та **англійської** мов
- Динамічна зміна мови без перезапуску
- Локалізація дат та днів тижня
- Локалізовані повідомлення про помилки

## 🚀 Встановлення

### Вимоги
- Android 8.0 (API 26) або вище
- Інтернет-з'єднання для отримання даних погоди
- Дозвіл на доступ до локації (опціонально)

### Збірка з вихідного коду

1. **Клонуйте репозиторій**
   ```bash
   git clone https://github.com/yourusername/aurora-weather.git
   cd aurora-weather
   ```

2. **Відкрийте проект в Android Studio**
   - Запустіть Android Studio
   - Виберіть "Open an existing project"
   - Виберіть папку проекту

3. **Налаштуйте API ключ**
   - Отримайте API ключ на [OpenWeatherMap](https://openweathermap.org/api)
   - Відкрийте `app/src/main/java/com/example/auroraweather/api/OpenWeatherMapClient.java`
   - Замініть `API_KEY` на ваш ключ:
   ```java
   private static final String API_KEY = "your_api_key_here";
   ```

4. **Зберіть проект**
   - Натисніть `Build` → `Make Project`
   - Або використайте `Ctrl+F9` (Windows/Linux) / `Cmd+F9` (Mac)

5. **Встановіть на пристрій**
   - Підключіть Android пристрій або запустіть емулятор
   - Натисніть `Run` → `Run 'app'`
   - Або використайте `Shift+F10`

## 🏗️ Архітектура

### Структура проекту
```
app/
├── src/main/java/com/example/auroraweather/
│   ├── MainActivity.java              # Головна активність
│   ├── SettingsActivity.java          # Налаштування
│   ├── SettingsManager.java           # Управління налаштуваннями
│   ├── adapters/                      # Адаптери для RecyclerView
│   │   ├── ForecastAdapter.java
│   │   └── HourlyAdapter.java
│   ├── api/                          # API клієнт
│   │   ├── OpenWeatherMapClient.java
│   │   └── WeatherCallback.java
│   ├── models/                       # Моделі даних
│   │   ├── CurrentWeather.java
│   │   ├── DailyForecast.java
│   │   └── HourlyForecast.java
│   └── utils/                        # Утиліти
│       ├── AnimationHelper.java
│       ├── BackgroundManager.java
│       ├── LocalizationManager.java
│       ├── SearchHistoryManager.java
│       └── WeatherIconMapper.java
├── src/main/res/                     # Ресурси
│   ├── layout/                       # Макети
│   ├── values/                       # Значення (кольори, рядки)
│   └── drawable/                     # Зображення та іконки
└── src/main/assets/                  # Активи
    ├── localization/                 # JSON файли локалізації
    └── weather_*.json               # Lottie анімації
```

### Ключові компоненти

#### 🌐 API Integration
- **OpenWeatherMap API** для отримання даних погоди
- **Geocoding API** для перетворення назв міст у координати
- **OneCall API** для UV-індексу
- **Volley** для HTTP-запитів

#### 🎨 UI Components
- **ConstraintLayout** для адаптивних макетів
- **RecyclerView** для списків прогнозів
- **CardView** для карток погоди
- **LottieAnimationView** для анімацій
- **Material Design** компоненти

#### 🔧 Utilities
- **SettingsManager** - Singleton для налаштувань
- **LocalizationManager** - Система локалізації
- **AnimationHelper** - Допоміжні анімації
- **BackgroundManager** - Управління фонами
- **WeatherIconMapper** - Мапінг іконок погоди

## 🛠️ Технології

### Основні залежності
```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'com.google.android.material:material:1.10.0'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'androidx.recyclerview:recyclerview:1.3.2'
implementation 'androidx.cardview:cardview:1.0.0'
implementation 'com.airbnb.android:lottie:6.3.0'
implementation 'com.android.volley:volley:1.2.1'
implementation 'com.google.android.gms:play-services-location:21.0.1'
implementation 'com.google.code.gson:gson:2.10.1'
```

### API Services
- [OpenWeatherMap](https://openweathermap.org/api) - Дані погоди
- [Google Play Services](https://developers.google.com/android/guides/overview) - Геолокація

## 📋 Функціональність

### Основні функції
- ✅ Відображення поточної погоди
- ✅ 5-денний прогноз
- ✅ Погодинний прогноз (24 години)
- ✅ Пошук міст
- ✅ Автоматичне визначення локації
- ✅ Історія пошуку
- ✅ Налаштування одиниць вимірювання
- ✅ Темна/світла тема
- ✅ Двомовна підтримка
- ✅ Анімації та переходи

### Розширені метрики
- 🌡️ Температура (C/F/K)
- 💧 Вологість
- 💨 Швидкість вітру (м/с, км/год, миль/год)
- 📊 Атмосферний тиск
- 👁️ Видимість
- ☀️ UV-індекс з кольоровим кодуванням

## 🎯 Плани розвитку

### Версія 2.0
- [ ] Офлайн кешування даних
- [ ] Push-сповіщення про зміни погоди
- [ ] Віджети для головного екрану
- [ ] Температурні графіки
- [ ] Карта погоди
- [ ] Експорт даних

### Версія 2.1
- [ ] Підтримка більше мов
- [ ] Темна тема з кастомними кольорами
- [ ] Голосове управління
- [ ] Інтеграція з календарем
- [ ] Статистика погоди

## 🤝 Внесок у проект

Ми вітаємо внески! Будь ласка, прочитайте [CONTRIBUTING.md](CONTRIBUTING.md) для деталей про процес внеску.

### Як допомогти
1. **Fork** репозиторій
2. Створіть **feature branch** (`git checkout -b feature/AmazingFeature`)
3. Зробіть **commit** змін (`git commit -m 'Add some AmazingFeature'`)
4. **Push** до branch (`git push origin feature/AmazingFeature`)
5. Відкрийте **Pull Request**

## 📄 Ліцензія

Цей проект ліцензовано під MIT License - дивіться файл [LICENSE](LICENSE) для деталей.

## 👨‍💻 Автор

**Aurora Weather Team**
- GitHub: [@elKINTARO](https://github.com/elKINTARO)
- Email: fedotovpoliyt@gmail.com

## 🙏 Подяки

- [OpenWeatherMap](https://openweathermap.org/) за безкоштовний API
- [Lottie](https://lottiefiles.com/) за красиві анімації
- [Material Design](https://material.io/) за дизайн-систему
- Всім контрибуторам та тестувальникам

## 📞 Підтримка

Якщо у вас є питання або проблеми:

1. Перевірте [Issues](https://github.com/yourusername/aurora-weather/issues)
2. Створіть новий issue з детальним описом
3. Зв'яжіться з нами через [Telegram](https://t.me/auroraweather)

---


