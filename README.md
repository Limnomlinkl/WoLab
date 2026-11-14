# WoLab — Wake on LAN для Android

Приложение для удалённого включения ПК по технологии **Wake on LAN**: отправляет
magic packet в локальную сеть и проверяет, что компьютер действительно запустился.

## Возможности

- **Профили сетей и устройств** — несколько сетей, в каждой свой список устройств
  (имя, IP-адрес, MAC, порт проверки доступности).
- **Отправка magic packet** — UDP на порт 9, пакет дублируется 5 раз с интервалом
  200 мс; используется направленный broadcast подсети, а при неудаче — `255.255.255.255`.
- **Проверка запуска** — после отправки пакета приложение в течение 30 секунд
  опрашивает TCP-порт устройства (по умолчанию 3389) и показывает статус:
  отправка → ПК запускается → запустился / не удалось запустить.
- **Локальное хранение** — данные в Room, никакой отправки на сторонние серверы.
- **Локализация** — русский и английский языки, тёмная и светлая темы.
- **Архитектура** — MVVM, LiveData, Navigation, Hilt (DI), ViewBinding.

## Скриншоты

_Будут добавлены позже._

## Установка (APK)

Готовые сборки публикуются в разделе
[**Releases**](https://github.com/Limnomlinkl/WolApp/releases).
Скачайте файл `*.apk` из последнего релиза и установите его на устройство
(потребуется разрешить установку из неизвестных источников).

## Сборка из исходников

Требования: JDK 17, Android SDK (compileSdk 36), Gradle 8.13 (wrapper в комплекте).

```bash
# Debug-сборка
./gradlew assembleDebug

# Release-сборка (без подписи; готовый APK — app/build/outputs/apk/release/)
./gradlew assembleRelease
```

APK появится в `app/build/outputs/apk/<variant>/app-<variant>.apk`.

## Структура проекта

```
app/src/main/java/com/example/wol/
├── data/        # Room (сущности, DAO, база), репозитории
├── di/          # Модули Hilt
├── domain/      # LaunchOrchestrator — сценарий «отправить пакет и дождаться запуска»
├── network/     # WolSender, HostAvailabilityChecker
└── ui/          # Activity, фрагменты, ViewModel, адаптеры
```

## Технологии

- Java 17, Android Gradle Plugin 8.13, minSdk 32 / targetSdk 36
- AndroidX: AppCompat, Material 3, ConstraintLayout, RecyclerView, Navigation, Lifecycle
- Room 2.7, Hilt 2.57

## Лицензия

Лицензия не указана. Все права принадлежат автору.
