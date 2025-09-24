# 🤝 Внесок у проект Aurora Weather

Дякую за ваш інтерес до внеску в Aurora Weather! Цей документ містить інструкції та рекомендації для співавторів.

## 📋 Зміст

- [Кодекс поведінки](#кодекс-поведінки)
- [Як почати](#як-почати)
- [Процес внеску](#процес-внеску)
- [Стандарти коду](#стандарти-коду)
- [Тестування](#тестування)
- [Документація](#документація)
- [Питання та підтримка](#питання-та-підтримка)

## 📜 Кодекс поведінки

### Наші зобов'язання

Ми зобов'язуємося створювати відкрите та дружнє середовище для всіх учасників проекту, незалежно від:
- Віку, розміру тіла, інвалідності
- Етнічної приналежності, гендерної ідентичності та вираження
- Рівня досвіду, національності, зовнішності
- Раси, релігії, сексуальної орієнтації та соціально-економічного статусу

### Неприйнятна поведінка

- Використання сексуалізованої мови або образів
- Тролінг, образливі коментарі, особисті напади
- Публічні або приватні домагання
- Публікація приватної інформації без дозволу
- Інша поведінка, неприйнятна в професійному середовищі

## 🚀 Як почати

### Перед початком роботи

1. **Перевірте існуючі Issues**
   - Подивіться на [список відкритих issues](https://github.com/yourusername/aurora-weather/issues)
   - Знайдіть issue, який вас цікавить
   - Залиште коментар, що хочете над ним працювати

2. **Fork репозиторій**
   ```bash
   # Натисніть кнопку "Fork" на GitHub
   # Потім клонуйте ваш fork
   git clone https://github.com/yourusername/aurora-weather.git
   cd aurora-weather
   ```

3. **Налаштуйте середовище розробки**
   ```bash
   # Додайте upstream remote
   git remote add upstream https://github.com/originalusername/aurora-weather.git
   
   # Створіть нову гілку для вашого feature
   git checkout -b feature/your-feature-name
   ```

### Налаштування Android Studio

1. Відкрийте проект в Android Studio
2. Синхронізуйте з Gradle файлами
3. Встановіть API ключ OpenWeatherMap (див. README.md)
4. Запустіть проект на емуляторі або пристрої

## 🔄 Процес внеску

### 1. Створення Feature Branch

```bash
# Переконайтеся, що ви на main гілці
git checkout main

# Оновіть main гілку
git pull upstream main

# Створіть нову гілку
git checkout -b feature/your-feature-name
```

### 2. Внесення змін

- Внесіть необхідні зміни в код
- Додайте тести, якщо потрібно
- Оновіть документацію
- Переконайтеся, що код відповідає стандартам

### 3. Коміти

```bash
# Додайте зміни
git add .

# Створіть коміт з описовим повідомленням
git commit -m "feat: add new weather animation for snow"

# Push до вашого fork
git push origin feature/your-feature-name
```

### 4. Створення Pull Request

1. Перейдіть на GitHub
2. Натисніть "New Pull Request"
3. Виберіть вашу гілку
4. Заповніть опис змін
5. Додайте reviewers
6. Натисніть "Create Pull Request"

## 📝 Стандарти коду

### Java Style Guide

```java
// ✅ Хороший приклад
public class WeatherManager {
    private static final String TAG = "WeatherManager";
    private final Context context;
    
    public WeatherManager(Context context) {
        this.context = context.getApplicationContext();
    }
    
    public void getCurrentWeather(String city, WeatherCallback callback) {
        // Implementation
    }
}

// ❌ Поганий приклад
public class weathermanager {
    public void getcurrentweather(String city,weathercallback callback){
        // Implementation
    }
}
```

### Назви змінних та методів

- **Класи**: PascalCase (`WeatherManager`, `SettingsActivity`)
- **Методи**: camelCase (`getCurrentWeather`, `updateUI`)
- **Змінні**: camelCase (`cityName`, `temperature`)
- **Константи**: UPPER_SNAKE_CASE (`API_KEY`, `MAX_HISTORY_ITEMS`)

### Коментарі

```java
/**
 * Отримує поточну погоду для вказаного міста
 * @param city Назва міста
 * @param callback Callback для обробки результату
 */
public void getCurrentWeather(String city, WeatherCallback callback) {
    // Implementation
}
```

### XML Layouts

```xml
<!-- ✅ Хороший приклад -->
<TextView
    android:id="@+id/city_name"
    android:layout_width="wrap_content"
    android:layout_height="wrap_content"
    android:text="@string/city_name"
    android:textSize="18sp"
    android:textColor="@color/textColorPrimary" />

<!-- ❌ Поганий приклад -->
<TextView android:id="@+id/cityname" android:layout_width="wrap_content" android:layout_height="wrap_content" android:text="City" android:textSize="18sp" />
```

## 🧪 Тестування

### Перед створенням PR

1. **Запустіть unit тести**
   ```bash
   ./gradlew test
   ```

2. **Запустіть UI тести**
   ```bash
   ./gradlew connectedAndroidTest
   ```

3. **Перевірте lint**
   ```bash
   ./gradlew lint
   ```

4. **Тестування на різних пристроях**
   - Різні розміри екранів
   - Різні версії Android
   - Темна/світла тема
   - Різні мови

### Типи тестів

- **Unit тести** для бізнес-логіки
- **UI тести** для користувацького інтерфейсу
- **Integration тести** для API
- **Manual тести** для користувацького досвіду

## 📚 Документація

### Оновлення документації

При додаванні нових функцій:

1. **Оновіть README.md** якщо потрібно
2. **Додайте коментарі до коду**
3. **Оновіть CHANGELOG.md**
4. **Додайте скриншоти** якщо змінився UI

### Структура комітів

Використовуйте [Conventional Commits](https://www.conventionalcommits.org/):

```
feat: add new weather animation
fix: resolve crash on location permission
docs: update README with new features
style: format code according to guidelines
refactor: improve weather data parsing
test: add unit tests for WeatherManager
```

## 🐛 Звіти про помилки

### Створення Bug Report

Використовуйте [шаблон](https://github.com/yourusername/aurora-weather/issues/new?template=bug_report.md):

1. **Опис проблеми**
2. **Кроки для відтворення**
3. **Очікувана поведінка**
4. **Фактична поведінка**
5. **Скриншоти** (якщо потрібно)
6. **Інформація про пристрій**

### Створення Feature Request

Використовуйте [шаблон](https://github.com/yourusername/aurora-weather/issues/new?template=feature_request.md):

1. **Опис функції**
2. **Проблема, яку вона вирішує**
3. **Пропоноване рішення**
4. **Альтернативи**
5. **Додаткова інформація**

## 🔍 Code Review

### Як робити Review

1. **Перевірте функціональність**
2. **Перевірте відповідність стандартам**
3. **Перевірте тести**
4. **Перевірте документацію**
5. **Залиште конструктивні коментарі**

### Як отримати Review

1. **Додайте reviewers**
2. **Опишіть зміни в PR**
3. **Відповідайте на коментарі**
4. **Вносьте необхідні зміни**

## 📞 Питання та підтримка

### Де отримати допомогу

- **GitHub Issues** - для bug reports та feature requests
- **GitHub Discussions** - для загальних питань
- **Telegram** - [@kintxbwm](https://t.me/kintxbwm)
- **Email** - fedorovpoliyt@gmail.com

### Часті питання

**Q: Як додати нову мову?**
A: Додайте JSON файл в `assets/localization/` та оновіть `LocalizationManager`.

**Q: Як додати нову анімацію погоди?**
A: Додайте Lottie файл в `assets/` та оновіть `WeatherIconMapper`.

**Q: Як додати нову метрику погоди?**
A: Оновіть модель `CurrentWeather` та UI компоненти.

## 📄 Ліцензія

Вносячи внесок, ви погоджуєтеся, що ваш внесок буде ліцензовано під MIT License.

---

**Дякуємо за ваш внесок у Aurora Weather! 🌅**

Якщо у вас є питання, не соромтеся звертатися до нас.
