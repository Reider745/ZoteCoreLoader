# ZotCoreLoader

ZotCoreLoader — серверное ядро предназначенное для inner core 1.16.201.
Основой серверного ядра послужил [Nukkit-Mot](https://github.com/MemoriesOfTime/Nukkit-MOT), ZotCoreLoader сделан таким образом, чтобы Nukkit-Mot внутри ядра было легко обновлять.

![ZotCoreLoader](/.github/logo.png)

## server.properties

**Настройки, добавляемые ядром**:

* legacy.workbench — по умолчанию true (другое значение на данный момент не поддерживается)
* inner_core.legacy_inventory — по умолчанию true (другое значение на данный момент не поддерживается)
* inner-core-version — числовая версия inner core
* inner-core-version-name — текстовая версия inner core
* inner-core-pack-name — имя пака inner core
* inner_core.runtime_exception — выдавать RuntimeException об использовании не поддерживаемых методов (по умолчанию false), рекомендуется только для отладки модов, во время игры выключайте
* eval-enable — вкл\выкл использование eval (по умолчанию true)
* develop-mode — режим разработки

## ПРЕДУПРЕЖДЕНИЕ

Использование `eval`, `requireGlobal` не рекомендуется!
Используйте эти методы с осторожностью!
Особенно не рекомендуется использвать в `Network` пакетах!
Это может использовать для получения полного контроля над сервером!
По умолчанию 'eval-enable' включён, лишь для обратной совместимости, при возможности рекомендуется держать его выключеным!

**Настройки, заблокированные ядром**:

* multiversion-min-protocol — ставит значение 422 (1.16.200)
* multiversion-max-protocol — ставит значение 422 (1.16.200)
* xbox-auth — ставит значение false
* save-player-data-by-uuid — ставит значение false

## Лицензия

![Licensing](/.github/license.jpg)
