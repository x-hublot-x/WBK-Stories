# WBK Stories

Аудиоплеер страшных историй для Android на базе Jetpack Compose.

## Скачать

Готовый установочный файл доступен в разделе [Releases](https://github.com/x-hublot-x/WBK-Stories/releases/tag/v1.0) или напрямую из репозитория: [WBK_Stories_v1.0.apk](WBK_Stories_v1.0.apk).

## Возможности

- Фоновое воспроизведение (Media3 / ExoPlayer)
- Управление через шторку уведомлений и экран блокировки
- Темный интерфейс (Pure Onyx) со световыми эффектами вокруг обложки
- Компактный мини-плеер в каталоге
- Каталог историй с фильтрацией по длительности и поиском
- Сохранение прогресса прослушивания

## Стек технологий

- Kotlin
- Jetpack Compose, Material 3
- AndroidX Media3 (ExoPlayer)
- Supabase (каталог и медиа-хранилище)
- Coil (загрузка обложек)

## Сборка проекта

```bash
git clone https://github.com/x-hublot-x/WBK-Stories.git
cd WBK-Stories/android
./gradlew assembleDebug
```

Собранный APK будет находиться в `android/app/build/outputs/apk/debug/app-debug.apk`.
