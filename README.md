# ZotCoreLoader
ZotCoreLoader - серверное ядро предназначенное для inner core 1.16.201. 
Основой серверного ядра послужил [Nukkit-Mot](https://github.com/MemoriesOfTime/Nukkit-MOT), ZotCoreLoader сделан таким образом, чтобы Nukkit-Mot внутри ядра было легко обновлять.

![](/ZotCoreLoader.png)

## server.properties
**Настройки которые добавляет ядро**
  * legacy.workbench - по умолчанию true(другое значение на данный момент не поддерживается)
  * inner_core.legacy_inventory - по умолчанию true(другое значение на данный момент не поддерживается)
  * inner-core-version - числовая версия inner core
  * inner-core-version-name - текстовая версия inner core
  * inner-core-pack-name - имя пака inner core
  * inner_core.runtime_exception - выдовать RuntimeException об использовании не поддерживаемых методов(по умолчанию false), рекомендуется только для отладки модов, во время игры выключайте
  * eval-enable - вкр\выкл использование eval(по умолчанию true)
  * develop-mode - режим разработки

## ВНИМАНИЕ!
Использование eval, requireGlobal не рекомендуется! 
Используйте эти методы с осторожностью!
Особенно не рекомендуется использвать в Network пакетах!
Это может использвать для получения полного контроля над сервером!
По умолчанию eval-enable включён, лишь для обратной совместимости, при возможности рекомендуется держать его выключеным!

**Настройки которые ядро блокирует**
  * multiversion-min-protocol - ставит значение 422
  * multiversion-max-protocol - ставит значение 422
  * xbox-auth - ставит значение false
  * save-player-data-by-uuid - ставит значение false

## Лицензия
![](/mem.jpg)