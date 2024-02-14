# Zote Core Loader

__Zote Core__ — серверное ядро, основанное на Inner Core 1.16.200, созданное для создания уникального геймплея с модами.

Основой серверного ядра послужил [Nukkit-MOT](https://github.com/MemoriesOfTime/Nukkit-MOT), его можно легко обновить, заменив соответствующую библиотеку.

![Zote Core](/.github/logo.png)

## Установка

Поддерживается любая платформа, поддерживающая Java 17 или выше. Загрузите последний релиз серверного ядра, либо соберите репозиторий с помощью Gradle.

Запустите собранное ядро, открыв консоль в папке с ним:

```sh
java -jar ZoteCore-SNAPSHOT.jar
```

## Конфигурация

Настройки производятся посредством двух основных файлов: *server.properties* и *zotecore.yml*. Они будут созданы автоматически сразу же после запуска ядра, документация по первому доступна в репозитории Nukkit-MOT.

Некоторые настройки Nukkit заблокированы загрузчиком, среди которых *multiversion-min/max-protocol* (422), *xbox-auth* (авторизация с помощью него не доступна) и *save-player-data-by-uuid* (false).

Чтобы сокеты клиентов отваливались, не крашая сервер, отключите Watchdog. В текущей реализации альтернативные варианты исправления недоступны, необходимая настройка `thread-watchdog=on`.

### zotecore.yml

Стандартные настройки ядра, которые можно использовать как основу:

```yml zotecore.yml
# Изменение информации пака, который клиент должен иметь для входа на сервер.
# Устаревшие или не реализованные методы, однако, добавлены не будут.
pack: Inner Core Test
pack-version: 2.3.0b115 test
pack-version-code: 152

# Устанавливает список модов и конфигураций для загрузки, вы можете
# указать название, имя папки модпака из /modpacks или путь.
modpack: innercore

# Использование ванильного интерфейса верстака, вместо кастомного.
# Внимание: в настоящее время не рекомендуется использовать,
# так как некоторые рецепты отображаются с проблемами.
use-legacy-workbench-override: on

# Отключение исправления инвентаря для подключенных клиентов,
# устаревшие версии не работают должным образом с ним,
# не позволяя перемещать предметы по инвентарю.
use-legacy-inventory: on

# Переключение режима отладки для неподдерживаемых методов (например, клиентских).
# Может быть одним из: none, debug (по умолчанию), warning, raise.
unsupported-method-handling: debug

# Могут ли моды использовать небезопасные функции (например, вызов eval) или нет.
# Внимание: это может создать риск безопасности, однако, например, его запрет
# влияет на requireGlobal и часто может нарушить работу модов.
allow-unsafe-scripting: on

# Режим разработчика позволяет получать дополнительную информацию
# о работе ядра, а также профайлинг методов.
developer-mode: off

# Время в секундах между запусками автосохранения данных модов (любое от 20),
# а также мира, если он включен в другой опции.
auto-save-period: 60

# Нужно ли помимо данных модов сохранять еще и миры.
auto-save-world: on

# Включен ли сокетный сервер или нет. Сокеты требуют дополнительный порт,
# подключение через серверные сокеты более нестабильное.
socket-server-enable: on

# Порт серверного сокета, обычно должен быть между 10000 и 24999.
# Но учтите, что по умолчанию клиент использует порт 2304.
socket-port: 2304

# Отправляет текстовую форму игрокам когда происходят ошибки тика,
# содержит только стактрейс скрипта (без потенциально опасных свойств).
display-ticking-errors-to-players: true

# Если серверный тик прерывается слишком часто, сервер будет остановлен;
# в противном случае тик будет пытаться продолжать перезапуски.
stop-on-critical-ticking-error: true
```

## Консольные команды

+ custom_items — выводит список кастумных предметов
+ custom_blocks — выводит список кастумных блоков
+ mods — выводит список модификаций на сервере
+ inner_core_network — выводит список подключенных к протоколу игроков на сервере
+ state — выводит стейты блока по рантайм идентификатору
+ profilecallback [enabled] [showParameters] — отладка и профайлинг калбеков

> Все команды предназначены для операторов и не могут быть вызваны игроками.

## Лицензия

![Licensing](/.github/license.jpg)

## Техническая информация

| Ключ | Значение |
|---|---|
| Версия протокола | 422 (1.16.200) |
| Коммит Apparatus | `73194cfd` |
| Коммит Instant Referrer | `a264591` |
| Коммит Nukkit-MOT | `9ebdd88` |
