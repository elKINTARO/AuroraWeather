# 🚀 Встановлення та налаштування Aurora Weather

Детальний посібник з встановлення та налаштування проекту Aurora Weather для розробників.

## 📋 Зміст

- [Вимоги системи](#вимоги-системи)
- [Встановлення Android Studio](#встановлення-android-studio)
- [Клонування проекту](#клонування-проекту)
- [Налаштування API ключів](#налаштування-api-ключів)
- [Збірка проекту](#збірка-проекту)
- [Встановлення на пристрій](#встановлення-на-пристрій)
- [Налаштування середовища розробки](#налаштування-середовища-розробки)
- [Вирішення проблем](#вирішення-проблем)

## 💻 Вимоги системи

### Мінімальні вимоги
- **ОС**: Windows 10/11, macOS 10.14+, або Linux (Ubuntu 18.04+)
- **RAM**: 8 GB (рекомендовано 16 GB)
- **Диск**: 10 GB вільного місця
- **Процесор**: Intel i5 або AMD Ryzen 5 (рекомендовано)

### Програмне забезпечення
- **Android Studio**: Arctic Fox 2020.3.1 або новіша версія
- **JDK**: 11 або 17
- **Android SDK**: API 26+ (Android 8.0+)
- **Git**: 2.30+ (для клонування репозиторію)

## 📱 Вимоги для пристрою

### Мінімальні вимоги
- **Android версія**: 8.0 (API 26) або новіша
- **RAM**: 2 GB
- **Диск**: 50 MB вільного місця
- **Дозволи**: Доступ до локації (опціонально), інтернет

### Рекомендовані вимоги
- **Android версія**: 11+ (API 30+)
- **RAM**: 4 GB+
- **Диск**: 100 MB+ вільного місця
- **Дозволи**: Всі дозволи для повної функціональності

## 🛠️ Встановлення Android Studio

### 1. Завантаження

1. Перейдіть на [developer.android.com](https://developer.android.com/studio)
2. Натисніть "Download Android Studio"
3. Виберіть версію для вашої ОС

### 2. Встановлення

#### Windows
1. Запустіть завантажений `.exe` файл
2. Дотримуйтесь інструкцій майстра встановлення
3. Виберіть компоненти:
   - ✅ Android Studio
   - ✅ Android SDK
   - ✅ Android Virtual Device
   - ✅ Performance (Intel HAXM)

#### macOS
1. Відкрийте завантажений `.dmg` файл
2. Перетягніть Android Studio в папку Applications
3. Запустіть Android Studio з Applications
4. Дотримуйтесь інструкцій майстра налаштування

#### Linux (Ubuntu)
```bash
# Встановлення залежностей
sudo apt update
sudo apt install openjdk-11-jdk

# Розпакування Android Studio
cd ~/Downloads
unzip android-studio-*.zip
sudo mv android-studio /opt/
sudo ln -s /opt/android-studio/bin/studio.sh /usr/local/bin/studio

# Запуск
studio
```

### 3. Перше налаштування

1. **Запустіть Android Studio**
2. **Виберіть "Do not import settings"** (якщо це перший запуск)
3. **Дотримуйтесь Setup Wizard**:
   - Виберіть "Standard" installation
   - Прийміть ліцензії
   - Дочекайтесь завантаження компонентів

## 📥 Клонування проекту

### 1. Отримання коду

```bash
# Клонування репозиторію
git clone https://github.com/yourusername/aurora-weather.git
cd aurora-weather

# Перевірка статусу
git status
```

### 2. Відкриття в Android Studio

1. **Запустіть Android Studio**
2. **Виберіть "Open an existing project"**
3. **Виберіть папку проекту** (`aurora-weather`)
4. **Дочекайтесь синхронізації Gradle**

### 3. Перевірка налаштувань

```bash
# Перевірка версії Java
java -version

# Перевірка Android SDK
ls $ANDROID_HOME/platforms

# Перевірка Git
git --version
```

## 🔑 Налаштування API ключів

### 1. Отримання OpenWeatherMap API ключа

1. **Зареєструйтесь** на [openweathermap.org](https://openweathermap.org/api)
2. **Перейдіть** в [API keys](https://home.openweathermap.org/api_keys)
3. **Скопіюйте** ваш API ключ
4. **Активуйте** ключ (може зайняти кілька хвилин)

### 2. Додавання API ключа в проект

#### Варіант 1: Пряме додавання (не рекомендується для production)

1. **Відкрийте** `app/src/main/java/com/example/auroraweather/api/OpenWeatherMapClient.java`
2. **Знайдіть** рядок з `API_KEY`
3. **Замініть** на ваш ключ:

```java
private static final String API_KEY = "your_actual_api_key_here";
```

#### Варіант 2: Використання BuildConfig (рекомендовано)

1. **Відкрийте** `app/build.gradle.kts`
2. **Додайте** в `android` блок:

```kotlin
android {
    // ... інші налаштування
    
    buildTypes {
        debug {
            buildConfigField "String", "API_KEY", "\"your_debug_api_key\""
        }
        release {
            buildConfigField "String", "API_KEY", "\"your_release_api_key\""
        }
    }
}
```

3. **Оновіть** `OpenWeatherMapClient.java`:

```java
private static final String API_KEY = BuildConfig.API_KEY;
```

#### Варіант 3: Використання local.properties (найкращий для розробки)

1. **Відкрийте** `local.properties`
2. **Додайте** рядок:

```properties
OPENWEATHER_API_KEY=your_api_key_here
```

3. **Оновіть** `app/build.gradle.kts`:

```kotlin
android {
    defaultConfig {
        // ... інші налаштування
        buildConfigField "String", "API_KEY", "\"${project.findProperty("OPENWEATHER_API_KEY") ?: ""}\""
    }
}
```

### 3. Перевірка API ключа

```bash
# Тестовий запит до API
curl "https://api.openweathermap.org/data/2.5/weather?q=Kyiv&appid=YOUR_API_KEY&units=metric"
```

## 🔨 Збірка проекту

### 1. Синхронізація Gradle

1. **Відкрийте** Android Studio
2. **Натисніть** "Sync Now" якщо з'явиться сповіщення
3. **Або** використайте `File → Sync Project with Gradle Files`

### 2. Очищення проекту

```bash
# Очищення build папки
./gradlew clean

# Або через Android Studio
Build → Clean Project
```

### 3. Збірка

```bash
# Debug збірка
./gradlew assembleDebug

# Release збірка
./gradlew assembleRelease

# Або через Android Studio
Build → Make Project (Ctrl+F9)
```

### 4. Перевірка збірки

```bash
# Перевірка APK файлу
ls app/build/outputs/apk/debug/
file app/build/outputs/apk/debug/app-debug.apk
```

## 📱 Встановлення на пристрій

### 1. Підготовка пристрою

#### Реальний пристрій
1. **Увімкніть** "Developer options":
   - `Settings → About phone → Build number` (натисніть 7 разів)
2. **Увімкніть** "USB Debugging":
   - `Settings → Developer options → USB debugging`
3. **Підключіть** пристрій через USB
4. **Дозвольте** USB debugging на пристрої

#### Емулятор
1. **Відкрийте** AVD Manager в Android Studio
2. **Створіть** новий Virtual Device
3. **Виберіть** пристрій (наприклад, Pixel 4)
4. **Виберіть** системний образ (API 30+)
5. **Запустіть** емулятор

### 2. Встановлення через Android Studio

1. **Виберіть** пристрій в dropdown
2. **Натисніть** "Run" (▶️) або `Shift+F10`
3. **Дочекайтесь** встановлення та запуску

### 3. Встановлення через командний рядок

```bash
# Встановлення debug APK
adb install app/build/outputs/apk/debug/app-debug.apk

# Встановлення release APK
adb install app/build/outputs/apk/release/app-release.apk

# Перевірка встановлених пакетів
adb shell pm list packages | grep aurora
```

### 4. Перевірка встановлення

1. **Знайдіть** "Aurora Weather" в списку додатків
2. **Запустіть** додаток
3. **Перевірте** основні функції:
   - Пошук міста
   - Відображення погоди
   - Налаштування

## ⚙️ Налаштування середовища розробки

### 1. Налаштування Git

```bash
# Налаштування користувача
git config --global user.name "Your Name"
git config --global user.email "your.email@example.com"

# Налаштування редактора
git config --global core.editor "code --wait"
```

### 2. Налаштування Android Studio

#### Корисні плагіни
1. **File → Settings → Plugins**
2. **Встановіть**:
   - Git Integration
   - ADB Idea
   - JSON Parser
   - Rainbow Brackets

#### Налаштування коду
1. **File → Settings → Editor → Code Style**
2. **Виберіть** "Java" або "Kotlin"
3. **Налаштуйте** відступи та форматування

### 3. Налаштування Gradle

#### Оптимізація збірки
```gradle
// gradle.properties
org.gradle.jvmargs=-Xmx2048m -XX:MaxPermSize=512m -XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8
org.gradle.parallel=true
org.gradle.daemon=true
org.gradle.configureondemand=true
```

### 4. Налаштування тестування

```bash
# Запуск unit тестів
./gradlew test

# Запуск UI тестів
./gradlew connectedAndroidTest

# Запуск всіх тестів
./gradlew check
```

## 🔧 Вирішення проблем

### Поширені проблеми

#### 1. "SDK location not found"
```bash
# Встановлення ANDROID_HOME
export ANDROID_HOME=$HOME/Android/Sdk
export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools

# Додавання в ~/.bashrc або ~/.zshrc
echo 'export ANDROID_HOME=$HOME/Android/Sdk' >> ~/.bashrc
echo 'export PATH=$PATH:$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools' >> ~/.bashrc
```

#### 2. "Gradle sync failed"
```bash
# Очищення Gradle кешу
./gradlew clean
rm -rf .gradle
rm -rf app/build

# Перезапуск Android Studio
```

#### 3. "API key not found"
- Перевірте правильність API ключа
- Переконайтеся, що ключ активний
- Перевірте налаштування BuildConfig

#### 4. "Device not found"
```bash
# Перевірка підключених пристроїв
adb devices

# Перезапуск ADB
adb kill-server
adb start-server

# Перевірка USB драйверів (Windows)
```

#### 5. "Out of memory"
```gradle
// app/build.gradle.kts
android {
    dexOptions {
        javaMaxHeapSize "4g"
    }
}

// gradle.properties
org.gradle.jvmargs=-Xmx4096m -XX:MaxPermSize=512m
```

### Логи та діагностика

#### Android Studio Logcat
1. **Відкрийте** Logcat в Android Studio
2. **Виберіть** ваш пристрій
3. **Фільтруйте** по тегу "AuroraWeather"

#### Командний рядок
```bash
# Логи пристрою
adb logcat | grep AuroraWeather

# Логи з фільтром по рівню
adb logcat *:E | grep AuroraWeather

# Очищення логів
adb logcat -c
```

#### Gradle логи
```bash
# Детальні логи збірки
./gradlew assembleDebug --info

# Логи з stack trace
./gradlew assembleDebug --stacktrace
```

## 📚 Додаткові ресурси

### Корисні посилання
- [Android Developer Guide](https://developer.android.com/guide)
- [OpenWeatherMap API Documentation](https://openweathermap.org/api)
- [Material Design Guidelines](https://material.io/design)
- [Volley Documentation](https://developer.android.com/training/volley)

### Підтримка
- **GitHub Issues**: [Створити issue](https://github.com/yourusername/aurora-weather/issues)
- **Telegram**: [@auroraweather](https://t.me/auroraweather)
- **Email**: your.email@example.com

---

Якщо у вас виникли проблеми з встановленням, будь ласка, створіть issue в GitHub репозиторії з детальним описом проблеми.
