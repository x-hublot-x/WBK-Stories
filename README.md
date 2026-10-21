# WBK Stories 🎧

> **Премиальный аудиоплеер страшных историй и крипипаст для Android на базе Jetpack Compose.**

[![Android](https://img.shields.io/badge/Platform-Android-green.svg)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Kotlin-2.0+-purple.svg)](https://kotlinlang.org)
[![Jetpack Compose](https://img.shields.io/badge/UI-Jetpack%20Compose-blue.svg)](https://developer.android.com/jetpack/compose)
[![Release](https://img.shields.io/badge/Release-v1.0-brightgreen.svg)](https://github.com/x-hublot-x/WBK-Stories/releases/tag/v1.0)

---

## 📱 О приложении

**WBK Stories** — это атмосферный мобильный плеер для прослушивания мистических рассказов и аудио-крипипаст. Приложение выполнено в строгой минималистичной эстетике **Pure Onyx**: глубокий черный фон, утонченная типографика и динамические световые эффекты, создающие ощущение погружения в темноту.

### ⬇️ Скачать APK (Версия 1.0)
Готовый файл для установки на любое Android-устройство (Android 8.0+):
- 📦 **[Скачать релиз WBK_Stories_v1.0.apk](https://github.com/x-hublot-x/WBK-Stories/releases/download/v1.0/WBK_Stories_v1.0.apk)** *(GitHub Releases)*
- 📁 **[Прямой файл из репозитория](WBK_Stories_v1.0.apk)**

---

## ✨ Ключевые особенности

### 🎨 Премиальный дизайн и анимации
- **Pure Onyx & Frosted Glass**: глубокие оттенки черного (`#0A0A0A`), мягкое размытие обложек (Haze blur) и полупрозрачные акриловые поверхности.
- **Оптические световые засветы (Camera Lens Leaks)**: вокруг обложки истории плавно и хаотично «гуляют» многослойные световые блики объектива камеры с эффектом дыхания, создающие живую глубину без визуального шума.
- **Плавный компактный мини-плеер**: быстрое управление треком из каталога с возможностью раскрыть полноэкранный плеер в один тап.

### 🔊 Аудио-движок
- **Фоновое воспроизведение**: мощный аудио-движок на базе **Media3 / ExoPlayer**. Воспроизведение продолжается при выключенном экране или переключении в другие приложения.
- **Системные уведомления**: интеграция с Media Notification и экраном блокировки Android для комфортного управления жестами и кнопками.
- **Умное управление**: таймлайн с плавной перемоткой, кнопки шага на ±15 секунд, сохранение последней позиции прослушивания.

### ☁️ Облачный каталог
- Интеграция с облачным каталогом **Supabase**.
- Коллекция из десятков озвученных страшных историй с качественными обложками и описаниями.
- Фильтры по хронометражу (короткие до 15 минут, длинные от 15 минут) и мгновенный поиск.

---

## 🛠 Технологический стек

- **Язык**: Kotlin
- **UI**: Jetpack Compose, Material 3
- **Архитектура**: MVVM (Model-View-ViewModel), StateFlow, Coroutines
- **Воспроизведение медиа**: AndroidX Media3 / ExoPlayer, Foreground MediaService
- **Сетевой слой & Хранилище**: Supabase REST API & Storage Client
- **Загрузка изображений**: Coil 3
- **Размытие & Эффекты**: Compose Canvas Drawing, Haze Blur, Dynamic Radial Gradients

---

## 🚀 Сборка из исходников

1. Клонируйте репозиторий:
   ```bash
   git clone https://github.com/x-hublot-x/WBK-Stories.git
   ```
2. Откройте директорию `android` в **Android Studio Ladybug** (или новее).
3. Скомпилируйте проект:
   ```bash
   cd android
   ./gradlew assembleDebug
   ```
4. Скомпилированный APK появится в:
   `android/app/build/outputs/apk/debug/app-debug.apk`

---

## 📄 Лицензия

Распространяется для личного ознакомления и использования. Все права на аудиоматериалы принадлежат их авторам и исполнителям.
