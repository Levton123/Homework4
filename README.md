ФИО: Еремеев Александр Николаевич
Группа: Б9123-09.03.01 цд
API: PokeAPI (https://pokeapi.co/) — бесплатный открытый RESTful API без ключей

Что хранится в Room

Таблица: favourites
- pokemonId (Int, Primary Key) — ID покемона
- pokemonName (String) — имя покемона
- addedAt (Long) — время добавления, для сортировки

Сценарий: Favourites — избранное переживает перезапуск приложения.
FavouriteRepository предоставляет Flow из Room DAO. Любое изменение в БД автоматически уведомляет PokemonListViewModel через подписку — без ручной синхронизации.

Как проверить

1. Запустить приложение
2. Добавить Pikachu (№25) и Charizard (№6) в избранное — нажать на сердечко в списке или на экране деталей
3. Перейти на экран Избранного — оба покемона отображаются
4. Полностью закрыть приложение
5. Запустить приложение снова
6. Открыть Избранное — Pikachu и Charizard остались

Архитектура (что изменилось в ДЗ4 по сравнению с ДЗ3)

- Hilt — все зависимости (Retrofit, OkHttp, Room, Repository) создаются в AppModule и инжектируются через @Inject constructor
- @HiltViewModel — PokemonListViewModel и PokemonDetailViewModel получают зависимости без фабрик
- SavedStateHandle в PokemonDetailViewModel — Hilt автоматически передаёт pokemonId из аргументов навигации
- FavouriteRepository — новый репозиторий, единственный источник правды для избранного, работает через Room Flow
- PokemonApplication — @HiltAndroidApp
- ViewModelFactories.kt — удалён (Hilt заменяет)
- RetrofitClient.kt — удалён (перенесён в AppModule)

Скриншоты
<img width="576" height="1280" alt="image" src="https://github.com/user-attachments/assets/0a07168a-85b8-4e38-9de6-c96d8cadacf9" />
(скриншот Loading)

(скриншот списка)
(скриншот детального экрана)
(скриншот избранного)
(скриншот ошибки с кнопкой Retry)
