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

Настройки производятся посредством трех основных файлов: *server.properties*, *nukkit.yml* и *zotecore.yml*. Они будут созданы автоматически сразу же после запуска ядра, документация по первым двум доступна в репозитории Nukkit.

> Некоторые настройки Nukkit заблокированы загрузчиком, среди которых *multiversion-min/max-protocol* (422), *xbox-auth* (авторизация с помощью него не доступна) и *save-player-data-by-uuid* (false).

### zotecore.yml

Стандартные настройки ядра, которые можно использовать как основу:

```yml zotecore.yml
# Изменение информации пака, который клиент должен иметь для входа на сервер.
# Устаревшие или не реализованные методы, однако, добавлены не будут.
pack: Inner Core Test
pack-version: 2.3.0b115 test
pack-version-code: 152

# Использование ванильного интерфейса верстака, вместо кастомного.
# Внимание: в настоящее время не поддерживается и пользовательские рецепты не будут отображаться.
use-legacy-workbench-override: on

# Отключение исправления инвентаря для подключенных клиентов, устаревшие версии не
# работают должным образом с ним, не позволяя перемещать предметы по инвентарю.
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

# Период сохранения данных inner core
auto-save-period: 60

# Обновлять вместе с сохранениями inner core сохранения мира
auto-save-world: on
```

## Лицензия

![Licensing](/.github/license.jpg)
