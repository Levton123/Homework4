ФИО: Еремеев Александр Николаевич
Группа: Б9123-09.03.01 цд
Приложение PokeDex построено на базе PokeAPI (https://pokeapi.co/).
PokeAPI - это бесплатный RESTful API, который предоставляет полную информацию о покемонах из всех поколений игр Pokemon. API возвращает данные о:
1. Списке всех покемонов (имя, ID, изображение)
2. Детальной информации о каждом покемоне (характеристики, способности, типы, рост, вес)
3. Спрайтах и официальных артворках покемонов
4. Эволюциях, движениях и других игровых данных

Инструкция по запуску:

1. Клонировать репозиторий
git clone <repository-url>
  cd pokedex
   
2. Открыть проект в Android Studio
File → Open → выбрать папку проекта

3.Синхронизировать Gradle
Android Studio автоматически предложит синхронизировать
Или вручную: File → Sync Project with Gradle Files

4.Запустить приложение
Нажать кнопку "Run" (зеленый треугольник)
Выбрать эмулятор или подключенное устройство

API ключ не требуется - PokeAPI является полностью открытым и бесплатным сервисом без необходимости регистрации или получения ключей доступа.

<img width="359" height="624" alt="image" src="https://github.com/user-attachments/assets/5469b614-625d-4741-b0ed-1a656f08f543" /> (Скриншот Loading)

<img width="354" height="627" alt="image" src="https://github.com/user-attachments/assets/b24f6b97-6484-43c4-b332-3fe7a0fa24cd" /> (Скриншот Listscreen)

<img width="358" height="599" alt="image" src="https://github.com/user-attachments/assets/0792d778-9b26-4b39-8dd3-ce5a9dbc9f56" /> (Скриншот Detail)

<img width="351" height="600" alt="image" src="https://github.com/user-attachments/assets/57afc98d-f534-46b8-a0ed-516d8f145016" /> (Скриншот Favourite)

<img width="352" height="625" alt="image" src="https://github.com/user-attachments/assets/18d00956-2e38-4732-9342-5cf3df83e42a" /> (Скриншот Error, с кнопкой Retry)


Чеклист
Обязательное:
1. 3 экрана: List, Detail (с ID в route), Favourites
2. UiState (Loading, Success, Error, Empty)
3. ViewModel + viewModelScope
4. Stateless UI (state + onEvent)
5. Repository между ViewModel и Retrofit
6. Suspend функции для сетевых запросов
7. Все UI состояния реализованы
8. Избранное (add/remove)
9. Избранное сохраняется при повороте экрана
10. Compose + Material3
11. Navigation Compose
12. Retrofit + kotlinx.serialization
13. Debounce поиска (Job + delay 300ms)

Бонусы:
1. Экран Favourites как отдельный route
2. Кнопка Refresh
3. Кэш результата в памяти (Repository)
4. Логирование запросов (OkHttp logging interceptor)

Примечания:
1. Для работы приложения требуется интернет-соединение
2. При первом запуске приложение загрузит список первых 151 покемона (первое поколение)
3. Все запросы логируются в Logcat (тег: OkHttp)
