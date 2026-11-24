from pathlib import Path

from docx import Document
from docx.enum.table import WD_TABLE_ALIGNMENT
from docx.enum.text import WD_ALIGN_PARAGRAPH
from docx.oxml import OxmlElement
from docx.oxml.ns import qn
from docx.shared import Inches, Pt, RGBColor

root = Path(__file__).resolve().parents[1]
output = root / "Отчет по лабораторным работам TrackerMoney.docx"
doc = Document()

def set_language(element, language="ru-RU"):
    """Set the proofing language so Word does not treat Russian text as English."""
    properties = element.get_or_add_rPr()
    for existing in properties.findall(qn("w:lang")):
        properties.remove(existing)
    lang = OxmlElement("w:lang")
    lang.set(qn("w:val"), language)
    properties.append(lang)

section = doc.sections[0]
section.top_margin = Inches(.7)
section.bottom_margin = Inches(.7)
section.left_margin = Inches(.8)
section.right_margin = Inches(.8)
for name, size, color in [("Normal", 10, "262A33"), ("Title", 25, "26345C"),
                          ("Heading 1", 18, "26345C"), ("Heading 2", 12, "5267C7")]:
    style = doc.styles[name]
    style.font.name = "Arial"
    style.font.size = Pt(size)
    style.font.color.rgb = RGBColor.from_string(color)
    style.paragraph_format.space_after = Pt(5)
    set_language(style._element)
code_style = doc.styles.add_style("CodeExcerpt", 1)
code_style.font.name = "Consolas"
code_style.font.size = Pt(8)
code_style.font.color.rgb = RGBColor.from_string("223248")
code_style.paragraph_format.left_indent = Inches(.15)
code_style.paragraph_format.space_after = Pt(7)
set_language(code_style._element, "en-US")

small_style = doc.styles.add_style("SmallNote", 1)
small_style.font.name = "Arial"
small_style.font.size = Pt(9)
small_style.font.italic = True
small_style.font.color.rgb = RGBColor.from_string("646B7A")
small_style.paragraph_format.space_after = Pt(6)
set_language(small_style._element)

header = section.header.paragraphs[0]
header.alignment = WD_ALIGN_PARAGRAPH.RIGHT
r = header.add_run("TrackerMoney | Лабораторные работы")
r.font.name, r.font.size, r.font.color.rgb = "Arial", Pt(8), RGBColor(115, 122, 137)
set_language(r._element)
footer = section.footer.paragraphs[0]
footer.alignment = WD_ALIGN_PARAGRAPH.CENTER
footer.add_run("Страница ")
set_language(footer.runs[-1]._element)
page = OxmlElement("w:fldSimple")
page.set(qn("w:instr"), "PAGE")
footer._p.append(page)

def shade(cell, color):
    shd = OxmlElement("w:shd")
    shd.set(qn("w:fill"), color)
    cell._tc.get_or_add_tcPr().append(shd)

def cell_text(cell, text, bold=False, color="262A33"):
    cell.text = ""
    run = cell.paragraphs[0].add_run(text)
    run.bold = bold
    run.font.name = "Arial"
    run.font.size = Pt(9)
    run.font.color.rgb = RGBColor.from_string(color)
    set_language(run._element)

def bullet(text):
    p = doc.add_paragraph(style="List Bullet")
    p.paragraph_format.space_after = Pt(3)
    p.add_run(text)

def code(text):
    p = doc.add_paragraph(style="CodeExcerpt")
    p.paragraph_format.keep_together = True
    p.add_run(text.strip())
    shd = OxmlElement("w:shd")
    shd.set(qn("w:fill"), "F1F4FA")
    p._p.get_or_add_pPr().append(shd)

lab_details = {
    3: [
        ("Что требовалось и почему выбрана MainActivity", [
            "В задании требуется нарисовать эскиз наиболее важной Activity. Для TrackerMoney такой точкой является MainActivity: Android запускает её первой, а она выбирает начальный экран и связывает навигацию приложения.",
            "Эскиз в этом отчёте — схема маршрутов экранов, а не макет внешнего вида. Она отвечает на вопрос «куда можно перейти». Расположение элементов и визуальный дизайн остаются в Compose-функциях экранов.",
        ]),
        ("Как читать фрагмент кода", [
            "uiState.currentScreen — строковый идентификатор текущего маршрута, который хранится в состоянии ViewModel.",
            "Ветка home рисует Compose-экран и передаёт ему данные и callback для кнопки добавления. Ветки history и analytics открывают контейнеры с Fragment.",
            "navigateTo(\"add\") не рисует новый экран сам: он меняет состояние ViewModel. Compose наблюдает uiState и повторно строит нужную ветку when.",
            "Это упрощённая навигация на состояниях. В проекте не подключена Navigation Compose-графовая маршрутизация; для текущего небольшого набора экранов сохранён существующий подход.",
        ]),
        ("Пошаговая проверка", [
            "Запустить приложение и убедиться, что сначала видна заставка, затем вход либо главная страница при действующей сессии.",
            "На главной нажать вкладки Аналитика, История и Настройки; затем кнопку добавления расхода. Проверить, что выбранный экран совпадает с пунктом схемы.",
            "Нарисовать схему из блока выше в тетради/редакторе, если преподавателю требуется отдельный рисунок вне отчёта. Проверить, что на ней подписаны все основные переходы.",
        ]),
        ("Итог и оговорка", [
            "Код маршрутизации уже существует, а схема добавлена в этот отчёт для выполнения графической части. В репозитории отдельный PNG/PDF-эскиз не создавался; если преподаватель собирает эскизы отдельно, схему нужно приложить отдельно.",
        ]),
    ],
    4: [
        ("Сценарий пользователя", [
            "Первый запуск: SplashScreen кратко показывает фирменный знак и переводит пользователя на экран входа. Из формы входа можно перейти к регистрации, а после регистрации вернуться к входу.",
            "При входе LoginScreen проверяет формат email и заполнение формы, затем вызывает onLogin. TrackerViewModel выполняет сетевую авторизацию Supabase в coroutine. При успехе открывается главная страница и загружаются расходы пользователя.",
            "При регистрации RegisterScreen проверяет email, длину пароля (не менее 8 символов), заполнение подтверждения и совпадение паролей. Далее ViewModel вызывает Supabase Auth. Если проект Supabase требует подтверждение email, пользователь должен подтвердить почту перед входом.",
            "Toast в формах показывают локальные ошибки валидации (например, пустое поле или несовпадение паролей). Сообщения о сетевом результате, передаваемые ViewModel, отображаются коротким Snackbar в MainActivity. Это два разных компонента уведомления; в отчёте их не следует называть одним и тем же.",
        ]),
        ("Пояснение к коду Toast", [
            "Toast.makeText получает Android-контекст, текст из strings.xml и длительность SHORT. show() выводит сообщение без отдельного диалога; Compose продолжает показывать ту же форму.",
            "Успешный вход обрабатывается иначе: ViewModel публикует текст в Flow messages, а MainActivity собирает этот Flow в coroutine и показывает Snackbar. Так UI-экран не обязан сам разбирать исключения Supabase.",
        ]),
        ("Пошаговая проверка", [
            "На экране регистрации отправить пустую форму и проверить краткое Toast.",
            "Ввести некорректный email; затем корректный email, короткий пароль; затем пароли, которые не совпадают. Каждая проверка должна остановить запрос и дать объясняющее уведомление.",
            "Зарегистрировать тестовую учётную запись с сетью, подтвердить email, войти и убедиться, что открывается главная. Проверить ошибку входа с неверным паролем.",
            "Закрыть и снова открыть приложение с активной сессией. ViewModel должен обнаружить сессию Supabase и показать данные без повторной регистрации.",
        ]),
        ("Ограничение", [
            "Вход и создание аккаунта требуют подключения к интернету. Локальная копия расходов не заменяет облачную учётную запись и не позволяет зарегистрировать нового пользователя офлайн.",
        ]),
    ],
    5: [
        ("Разбор пунктов задания", [
            "Ресурс цветов: цвета темы и категорий лежат в res/values/colors.xml. Благодаря этому цвет можно переиспользовать и менять централизованно.",
            "Ресурсные подписи: пользовательские тексты находятся в values/strings.xml (русский) и values-en/strings.xml (английский). UI получает их через stringResource или getString, а не хранит длинные надписи внутри логики.",
            "Темы: базовые Android-темы объявлены в values/themes.xml и values-night/themes.xml; Compose-палитра применяется через TrackerMoneyTheme. Это позволяет согласованно менять фон, текст и поверхности для тёмного режима.",
            "Анимация: SplashScreen плавно меняет прозрачность и масштаб знака. animateFloatAsState интерполирует значение, tween задаёт длительность 600 мс, а LaunchedEffect запускает показ и переход после задержки.",
            "Локализация: экран настроек меняет локаль приложения на ru или en через AppCompatDelegate. AppCompat сохраняет выбор локали по метаданным в AndroidManifest.xml.",
        ]),
        ("Честная оценка медиапункта", [
            "Векторный drawable ic_expense_receipt.xml и emoji-иконки категорий являются графическими ресурсами интерфейса. Однако они не являются фотографией, видео или GIF.",
            "По ранее согласованной концепции учёта расходов отдельные фотографии пользователю не нужны, поэтому в функциональность добавлены иконки категорий. Это разумная адаптация приложения, но формально буквальное требование задания про фото/видео/GIF она может не закрывать.",
            "Если преподаватель оценивает этот пункт строго, самый небольшой вариант доработки — добавить одну тематическую иллюстрацию в drawable и показать её, например, на заставке или пустом состоянии. Не нужно добавлять загрузку фотографий расходов, если этого не требует формулировка.",
        ]),
        ("Пошаговая проверка", [
            "Проверить светлую и тёмную темы: подписи должны оставаться читаемыми на своих поверхностях.",
            "В настройках переключить язык на английский, открыть историю и аналитику: названия категорий и системные подписи должны смениться вместе.",
            "Перезапустить приложение и проверить, что выбранные язык и тёмная тема сохранились.",
            "Открыть заставку и наблюдать появление логотипа: он плавно увеличивается и становится непрозрачным.",
        ]),
    ],
    6: [
        ("Как работает список расходов", [
            "item_expense.xml описывает одну карточку расхода: место под значок категории, название/заметку, дату и сумму. Это отделяет геометрию строки от Kotlin-кода.",
            "ExpenseRecyclerAdapter наследуется от ListAdapter. RecyclerView повторно использует ViewHolder при прокрутке, поэтому приложению не нужно создавать все строки заново. DiffUtil сравнивает старый и новый списки и обновляет изменившиеся элементы.",
            "В onBindViewHolder (через bind) одна модель Expense раскладывается по TextView: название категории локализуется через categoryNameResource, значок берётся по категории, дата форматируется как dd.MM.yyyy, HH:mm, сумма выводится со знаком минус.",
            "Нажатие по строке открывает PopupMenu с действиями «Изменить» и «Удалить». Колбэки onEdit/onDelete передают выбранный расход обратно ViewModel; сам адаптер не меняет Room или Supabase.",
            "CategoryLegendAdapter — второй адаптер, используемый в аналитике. Он показывает цветную точку, локализованное название и сумму категории. Так оба адаптера работают на конкретных данных приложения.",
        ]),
        ("Почему пример отличается от методички", [
            "Предоставленный PDF с заданиями не содержит изображения или текста примера 4.1. Поэтому невозможно сверить точное совпадение его макета. В отчёте указан этот факт, а строка реализована по реальной модели Expense и визуальному стилю TrackerMoney.",
        ]),
        ("Пошаговая проверка", [
            "Добавить несколько расходов разных категорий и с разными заметками. Убедиться, что у каждой строки есть категория, значок, дата/время и сумма.",
            "Изменить один расход через нажатие по строке; удалить другой через то же меню. Проверить, что список и суммы на главной/аналитике обновились.",
            "Открыть историю на английском: проверить локализацию названия категории. Прокрутить длинный список, чтобы убедиться в корректной переработке строк.",
            "Открыть аналитику и проверить, что легенда категорий показывает цвет и сумму, а при смене периода данные пересчитываются.",
        ]),
    ],
    7: [
        ("Явный Intent: переход в AboutActivity", [
            "Intent(this@MainActivity, AboutActivity::class.java) содержит конкретный класс-назначение. Android точно знает, какую Activity запустить; этот вызов используется для перехода из настройки «О приложении».",
            "AboutActivity зарегистрирована в манифесте с exported=false: её запускает само приложение, а внешним приложениям она не открыта. Кнопка внутри вызывает finish(), то есть закрывает текущую Activity и возвращает предыдущую.",
        ]),
        ("Неявный Intent: системное меню отправки", [
            "ACTION_SEND сообщает Android тип операции, но не назначает конкретное приложение. Поле type=text/plain ограничивает список подходящими обработчиками, EXTRA_TEXT передаёт текст о приложении.",
            "Intent.createChooser просит систему показать пользователю список подходящих приложений. Приложение не пытается самостоятельно выбрать мессенджер или почту.",
        ]),
        ("Пошаговая проверка", [
            "Открыть Настройки → О приложении. Убедиться, что открылась отдельная страница Activity.",
            "Нажать кнопку возврата и проверить возврат на настройки.",
            "Нажать Поделиться. Android должен показать системный chooser; выбрать установленное приложение или закрыть chooser.",
            "Проверить манифест: MainActivity экспортирована как launcher Activity, AboutActivity имеет android:exported=false.",
        ]),
    ],
    8: [
        ("Что именно считается Fragment в этой реализации", [
            "HistoryFragment и AnalyticsFragment наследуются от androidx.fragment.app.Fragment — это реальные классы AndroidX Fragment, а не только названия экранов.",
            "Каждый Fragment создаёт ComposeView и помещает в него уже существующий Compose UI. Такой мост позволяет выполнить требование лабораторной о Fragment без переписывания экранов и без смены Material 3 дизайна.",
            "Вызов viewModel(viewModelStoreOwner = requireActivity()) просит Activity-scoped ViewModel. Поэтому история и аналитика читают одно состояние TrackerViewModel, а не создают отдельные копии расходов.",
            "DisposeOnViewTreeLifecycleDestroyed очищает Compose-композицию вместе с жизненным циклом View. Это важно, чтобы при уходе с экрана не оставались активные наблюдатели состояния.",
            "MainActivity создаёт FragmentContainerView для нужного маршрута и добавляет соответствующий Fragment транзакцией. При уходе с destination текущий Fragment удаляется.",
        ]),
        ("Пошаговая проверка", [
            "Перейти на вкладку История: экран должен показывать расходы, переданные тем же ViewModel.",
            "Перейти на Аналитику: график должен строиться из состояния расходов; менять период и проверять обновление.",
            "Вернуться домой и изменить расход; снова открыть историю и проверить, что обновлённое значение видно.",
            "В Android Studio открыть классы HistoryFragment и AnalyticsFragment и убедиться, что оба наследуются от Fragment и оба подключены к MainActivity.",
        ]),
        ("Архитектурная оговорка", [
            "ComposeView внутри Fragment — переходный способ совместить два UI-подхода. Сами экраны написаны на Compose; лабораторная проверяет наличие и использование двух Fragment, а не то, что вся разметка обязана быть XML.",
        ]),
    ],
    9: [
        ("Зачем здесь две базы данных", [
            "Supabase — облачный источник данных: после входа тот же аккаунт может получать расходы на разных телефонах. Room — локальная SQLite-база на текущем устройстве, благодаря которой уже загруженные данные видны без сети.",
            "Room не заменяет Supabase Auth. Облачная авторизация определяет пользователя; его UUID записывается в ownerUserId и используется как часть ключа расхода. Запросы DAO всегда принимают userId, чтобы локальные строки разных аккаунтов не смешивались.",
            "ExpenseEntity хранит поля расхода плюс ownerUserId и pendingOperation. Значение null значит, что запись синхронизирована; UPSERT и DELETE означают ожидающее сетевое действие.",
            "MonthlyBudgetEntity хранит отдельный бюджет для каждого ownerUserId и флаг pendingUpload. Это та же offline-first идея для настройки бюджета.",
        ]),
        ("Путь добавления и синхронизации расхода", [
            "1. saveExpense проверяет сумму и категорию, формирует дату с выбранным календарным днём и текущим временем.",
            "2. ViewModel получает UUID текущей сессии Supabase и сначала записывает ExpenseEntity в Room с pendingOperation=UPSERT.",
            "3. ViewModel перечитывает локальный список, пересчитывает экранные итоги и сразу показывает результат. Поэтому интерфейс не ждёт ответа сервера.",
            "4. Приложение пробует upsert в Supabase. При успехе Room снимает флаг очереди; при ошибке расход остаётся на устройстве и пользователь получает сообщение об офлайн-сохранении.",
            "5. При запуске/повторе загрузки/возврате сети syncExpensesFromCloud отправляет ожидающие изменения, затем забирает облачный список и атомарно заменяет синхронизированную часть локальной копии.",
        ]),
        ("Как работает локальное сохранение", [
            "TrackerDatabase создаёт единственный экземпляр базы tracker_money.db через singleton. Room генерирует SQLite-реализацию таблиц и DAO на этапе сборки при помощи KSP.",
            "getVisible фильтрует по ownerUserId и скрывает локально удалённые записи. getPending выбирает строки с операцией для повтора. replaceSynced выполняет удаление старого синхронизированного набора и запись свежего набора в одной транзакции.",
            "ConnectivityManager регистрируется на время активной MainActivity. При появлении сети он вызывает syncPendingExpenses. Разрешение ACCESS_NETWORK_STATE добавлено в манифест.",
            "Схема Room сейчас версии 1 и экспорт Room-схемы отключён. Для будущего изменения полей таблицы понадобится миграция базы, чтобы обновление установленного приложения не потеряло локальные данные.",
        ]),
        ("Облачная изоляция и безопасность", [
            "Клиентское приложение использует авторизованную сессию Supabase и обращается к таблице expenses. Фильтрация на клиенте не является защитой от чужого аккаунта: обязательная граница доступа должна быть включена на сервере через Row Level Security.",
            "SupabaseRepository отправляет в таблицу расходов id, amount, category, note и spent_at; поле user_id в DTO расхода не сериализуется. Значит, схема облачной таблицы должна сама назначать владельца новой записи (например, DEFAULT auth.uid() или триггер). В приложенном проекте SQL-файла с созданием таблиц и RLS-политиками нет, поэтому эту настройку нельзя подтвердить только по Android-коду.",
            "Перед сдачей нужно открыть Supabase SQL Editor и убедиться, что RLS включён, политика SELECT ограничивает чтение строкой user_id = auth.uid(), а INSERT/UPDATE/DELETE проверяют того же владельца. Иначе лабораторная часть о данных разных пользователей не подтверждена одной лишь реализацией клиента.",
            "Опубликованный ключ клиента в Android-приложении не является секретом для администрирования. Запрещено помещать в приложение service_role key; контроль доступа должен обеспечивать RLS.",
        ]),
        ("Что офлайн работает, а что нет", [
            "При ранее выполненном входе и наличии Room-копии можно просматривать локально сохранённые расходы; новые добавления, изменения и удаления можно поставить в очередь для следующей синхронизации.",
            "Регистрация и первичный вход требуют интернет. Если пользователь очистит данные приложения/удалит приложение и на устройстве нет копии Room, локальный офлайн-архив восстановить нельзя; после входа данные загружаются из облака.",
            "В этом изменении добавлена Room-копия именно расходов и месячного бюджета. Токены/пароль остаются областью ответственности Supabase SDK и его хранилища сессии; Room не следует использовать для хранения пароля в открытом виде.",
        ]),
        ("Пошаговая проверка", [
            "Войти с интернетом, добавить расход и убедиться, что он есть в истории. Перезапустить приложение, чтобы проверить локальный кэш.",
            "Включить авиарежим, добавить расход и увидеть уведомление о локальном сохранении; закрыть и снова открыть приложение с сохранённой сессией и проверить наличие расхода.",
            "Вернуть сеть и дождаться автоматической синхронизации. Убедиться, что расход появился в Supabase один раз и очередь очистилась.",
            "Проверить удаление офлайн: запись исчезает из видимой истории и после восстановления сети удаляется из облачной таблицы.",
            "Войти вторым тестовым аккаунтом и проверить, что локальный список содержит только его записи. Затем проверить серверные RLS-политики в Supabase; это отдельная проверка от Room.",
        ]),
    ],
}

def lab(number, title, status, intro, files, excerpt=None, note=None):
    doc.add_page_break()
    doc.add_heading(f"Лабораторная работа №{number} {title}", 1)
    p = doc.add_paragraph()
    r = p.add_run(f"Статус: {status}")
    r.bold = True
    r.font.color.rgb = RGBColor.from_string("5267C7")
    doc.add_paragraph(intro)
    doc.add_heading("Где находится реализация", 2)
    for item in files:
        bullet(item)
    if excerpt:
        doc.add_heading("Фрагмент кода", 2)
        code(excerpt)
    if note:
        doc.add_paragraph(note)
    for heading, paragraphs in lab_details[number]:
        doc.add_heading(heading, 2)
        for text in paragraphs:
            doc.add_paragraph(text)

# Title page and project overview.
p = doc.add_paragraph(style="Title")
p.paragraph_format.space_before = Pt(85)
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.add_run("Отчёт по лабораторным работам")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
r = p.add_run("Мобильное приложение TrackerMoney")
r.bold, r.font.size, r.font.color.rgb = True, Pt(16), RGBColor.from_string("5267C7")
p = doc.add_paragraph()
p.alignment = WD_ALIGN_PARAGRAPH.CENTER
p.add_run("Лабораторные работы №3–9\nAndroid · Kotlin · Jetpack Compose · Material 3\n24 ноября 2026 года")
doc.add_paragraph()
doc.add_heading("О содержании", 1)
doc.add_paragraph(
    "В отчёте задания из предоставленного PDF сопоставлены с фактическим кодом TrackerMoney. "
    "Для каждой работы приведены требование, решение, пошаговое объяснение кода, список точек входа "
    "в исходниках, сценарий пользователя, ручная проверка и известные ограничения. "
    "В приложенном PDF присутствуют задания №3–9; формулировок №1 и №2 в нём нет, поэтому они не додумываются."
)
doc.add_paragraph(
    "TrackerMoney учитывает расходы по категориям, показывает историю и аналитику, хранит месячный бюджет, "
    "использует Supabase для аккаунта и облачных данных. Room сохраняет расходы и бюджет на устройстве "
    "пользователя и ставит офлайн-операции в очередь синхронизации."
)
doc.add_heading("Схема основных экранов", 2)
t = doc.add_table(rows=1, cols=1)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
shade(t.cell(0, 0), "F1F4FA")
cell_text(t.cell(0, 0),
          "Заставка → Вход ↔ Регистрация → Главная\n"
          "Главная → Добавление или изменение расхода\n"
          "Главная ↔ История (HistoryFragment)\n"
          "Главная ↔ Аналитика (AnalyticsFragment)\n"
          "Главная → Настройки → О приложении (AboutActivity) / Поделиться")
doc.add_paragraph(
    "Схема составлена по текущей навигации. Отдельного эскиза в репозитории нет; пользователь ранее сообщал, "
    "что лабораторная №3 уже сделана."
)
doc.add_heading("Содержание отчёта", 2)
for item in [
    "Карта проекта, поток данных, сборка и краткий словарь терминов.",
    "Лабораторная №3 — схема функционально важной MainActivity.",
    "Лабораторная №4 — заставка, авторизация, регистрация и уведомления.",
    "Лабораторная №5 — ресурсы, темы, анимация и русский/английский интерфейс; оговорка о медиа.",
    "Лабораторная №6 — разметка расхода и два адаптера списков.",
    "Лабораторная №7 — явный и неявный Intent, возврат из конечного экрана.",
    "Лабораторная №8 — два реальных Fragment, использующих общий ViewModel.",
    "Лабораторная №9 — Supabase, Room, разделение аккаунтов и сценарий работы без сети.",
    "Сводка соответствия, чек-лист приёмки и ограничения проверки.",
]:
    bullet(item)

doc.add_page_break()
doc.add_heading("Как устроен проект", 1)
doc.add_paragraph(
    "Ниже — краткая карта слоёв. Она помогает понять, почему экран, правила приложения и сохранение данных "
    "разнесены по разным файлам. Например, экран не должен самостоятельно отправлять SQL-запрос: он сообщает "
    "о действии ViewModel, а ViewModel выбирает локальное и облачное хранилище."
)
t = doc.add_table(rows=1, cols=3)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
for i, value in enumerate(["Слой", "За что отвечает", "Примеры в TrackerMoney"]):
    shade(t.rows[0].cells[i], "26345C")
    cell_text(t.rows[0].cells[i], value, True, "FFFFFF")
for values in [
    ("UI / представление", "Показывает формы, списки и диаграмму; передаёт нажатия callback-ами.", "MainActivity.kt, ui/*Screen.kt, HistoryFragment.kt, AnalyticsFragment.kt"),
    ("ViewModel", "Хранит состояние экранов, проверяет операции и запускает coroutine.", "viewmodel/TrackerViewModel.kt"),
    ("Локальные данные", "Сохраняет копию расходов/бюджета и ожидающие сетевые действия.", "data/TrackerDatabase.kt, ExpenseDao.kt, *Entity.kt"),
    ("Облачные данные", "Авторизация Supabase и запросы к таблицам пользователя.", "data/SupabaseRepository.kt, SupabaseClientProvider.kt"),
    ("Доменная логика", "Считает суммы за день, месяц и выбранные периоды диаграммы.", "domain/ExpenseStatistics.kt"),
    ("Модель", "Описывает расход как ID, сумму, категорию, заметку и дату.", "model/Expense.kt"),
]:
    cells = t.add_row().cells
    for i, value in enumerate(values):
        cell_text(cells[i], value, i == 0)

doc.add_heading("Маршрут данных расхода", 2)
t = doc.add_table(rows=1, cols=1)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
shade(t.cell(0, 0), "F1F4FA")
cell_text(
    t.cell(0, 0),
    "Пользователь нажал «Сохранить»\n"
    "↓\n"
    "AddExpenseScreen вызывает callback onSave\n"
    "↓\n"
    "TrackerViewModel.saveExpense проверяет сумму и категорию\n"
    "↓\n"
    "Room записывает локальный расход со статусом UPSERT\n"
    "↓\n"
    "UI обновляется сразу ← ViewModel ← Room\n"
    "↓\n"
    "Supabase получает расход; если сети нет, запись ожидает синхронизации"
)
doc.add_paragraph(
    "Проверено по исходникам. Термин «offline-first» здесь означает, что сначала меняется локальная база, "
    "а сеть обновляется следом. Это не означает, что первичная регистрация возможна без интернета."
)

doc.add_heading("Сборка и зависимости", 2)
for item in [
    "Язык и UI: Kotlin 2.0.21, Jetpack Compose и Material 3. Текущие экраны в основном Compose; RecyclerView и Fragment добавлены для соответствующих лабораторных.",
    "Android: compileSdk 36, targetSdk 36, minSdk 24. Проект поддерживает API 24 и новее.",
    "Java/Kotlin bytecode: Java source/target и Kotlin jvmTarget настроены на 11; включена coreLibraryDesugaring для библиотечных API, нужных при minSdk 24.",
    "Хранение: Room 2.7.2 + KSP для локальных таблиц; Supabase Auth/PostgREST для аккаунта и облачных записей; DataStore остаётся для небольших пользовательских настроек и миграции прежних расходов.",
    "Экранные компоненты: Fragment KTX 1.8.5 и RecyclerView 1.3.2 добавлены для заданий №6 и №8.",
    "Версии и алиасы находятся в gradle/libs.versions.toml; подключения модуля — в app/build.gradle.kts. Секретный service_role ключ не должен попадать в Android-клиент.",
]:
    bullet(item)

doc.add_heading("Небольшой словарь", 2)
for item in [
    "Activity — компонент Android, который предоставляет окно приложения. MainActivity — стартовое окно; AboutActivity — отдельное окно «О приложении».",
    "Fragment — часть интерфейса с собственным жизненным циклом, которую можно помещать в Activity. В работе №8 два фрагмента хостят существующие Compose-экраны.",
    "Composable — функция Compose, описывающая UI из текущего состояния. При изменении StateFlow Compose перечитывает новое состояние и перестраивает нужные элементы.",
    "ViewModel — объект состояния экрана, который переживает обычное пересоздание UI и запускает работу в viewModelScope.",
    "DAO — Data Access Object, набор операций чтения/записи Room, описанных Kotlin-функциями и SQL-аннотациями.",
    "Entity — Kotlin-класс, который Room сопоставляет с таблицей SQLite. ExpenseEntity — локальная копия одного расхода.",
    "Intent — сообщение Android о действии. Явный Intent указывает конкретный Activity-класс; неявный указывает действие и позволяет Android найти приложение-обработчик.",
    "RLS — Row Level Security, серверное правило Supabase/PostgreSQL, которое ограничивает строки, доступные текущему пользователю.",
]:
    bullet(item)

lab(3, "Эскиз функционально важной активности", "Реализована; схема приведена в отчёте",
    "Наиболее важная часть — MainActivity: она является точкой входа, наблюдает состояние TrackerViewModel "
    "и выбирает экран приложения. Схема выше показывает её основные маршруты.",
    ["app/src/main/java/com/example/trackermoney/MainActivity.kt — главный контейнер и нижняя навигация.",
     "app/src/main/java/com/example/trackermoney/viewmodel/TrackerViewModel.kt — состояние и переходы.",
     "app/src/main/java/com/example/trackermoney/ui/HomeScreen.kt — центральный экран расходов."],
    '''when (uiState.currentScreen) {
    "home" -> HomeScreen(
        summary = uiState.homeSummary,
        monthlyBudget = uiState.monthlyBudget,
        onAddExpense = { trackerViewModel.navigateTo("add") }
    )
    "history" -> FragmentDestination("history")
    "analytics" -> FragmentDestination("analytics")
}''',
    "Экраны входа, регистрации, главная, добавление расхода и настройки являются Compose-экранами одной "
    "Activity. История и аналитика размещены во Fragments.")

lab(4, "Заставка, вход, регистрация и Toast", "Реализована",
    "Приложение показывает стартовую заставку, экран входа и экран регистрации. Авторизация подключена к "
    "Supabase Auth. Формы показывают короткие Toast при успехе, ошибках ввода и ошибках сервера, что превышает "
    "минимум два сообщения из задания.",
    ["app/src/main/java/com/example/trackermoney/ui/SplashScreen.kt — заставка и переход.",
     "app/src/main/java/com/example/trackermoney/ui/LoginScreen.kt — вход и Toast.",
     "app/src/main/java/com/example/trackermoney/ui/RegistrScreen.kt — регистрация и Toast.",
     "app/src/main/java/com/example/trackermoney/data/SupabaseRepository.kt — запросы Supabase Auth."],
    '''Toast.makeText(
    context,
    context.getString(R.string.validation_fill_all),
    Toast.LENGTH_SHORT
).show()''',
    "Это три сценария Compose в одной MainActivity, а не отдельные Activity-классы для каждого экрана.")

lab(5, "Ресурсы, темы, анимация и локализация", "Частично: основной набор реализован, медиа-пункт адаптирован",
    "Палитра вынесена в ресурсы; русские и английские подписи находятся в отдельных наборах строк. "
    "Светлая и тёмная темы применяются через TrackerMoneyTheme, заставка анимирует прозрачность и масштаб, "
    "а язык переключается средствами AppCompat.",
    ["app/src/main/res/values/colors.xml — цвета темы, бренда и категорий.",
     "app/src/main/res/values/strings.xml и app/src/main/res/values-en/strings.xml — русский и английский тексты.",
     "app/src/main/res/values/themes.xml и app/src/main/res/values-night/themes.xml — ресурсы тем.",
     "app/src/main/java/com/example/trackermoney/ui/TrackerMoneyTheme.kt и SplashScreen.kt — применение схем и анимация.",
     "app/src/main/res/drawable/ic_expense_receipt.xml — векторная иллюстрация чека.",
     "app/src/main/AndroidManifest.xml — метаданные AppCompat для сохранения локали."],
    '''val alpha by animateFloatAsState(
    targetValue = if (started) 1f else 0f,
    animationSpec = tween(durationMillis = 600),
    label = "splashAlpha"
)''',
    "В задании буквально названы фото/видео/GIF. По согласованной концепции трекера вместо них использованы "
    "иконки категорий и векторная иллюстрация. Если преподаватель требует именно фото, видео или GIF, этот "
    "медиа-пункт нужно дополнить отдельным файлом.")

lab(6, "Своя строка списка и два адаптера", "Реализована на данных приложения",
    "История отображает каждый расход в собственной XML-разметке. ExpenseRecyclerAdapter связывает модель "
    "Expense с карточкой строки и меню изменения/удаления. CategoryLegendAdapter отображает категории, "
    "цвета и суммы в аналитике. Это два реально используемых адаптера.",
    ["app/src/main/res/layout/item_expense.xml — разметка строки расхода.",
     "app/src/main/java/com/example/trackermoney/ui/ExpenseRecyclerAdapter.kt — адаптер истории и меню действий.",
     "app/src/main/res/layout/item_category_legend.xml — строка легенды.",
     "app/src/main/java/com/example/trackermoney/ui/CategoryLegendAdapter.kt — второй адаптер.",
     "app/src/main/java/com/example/trackermoney/ui/HistoryScreen.kt и AnalyticsScreen.kt — подключение списков."],
    '''class ExpenseRecyclerAdapter(
    private val onEdit: (Expense) -> Unit,
    private val onDelete: (Expense) -> Unit
) : ListAdapter<Expense, ExpenseRecyclerAdapter.ExpenseViewHolder>(ExpenseDiffCallback)''',
    "PDF с заданием ссылается на пример 4.1 из файла лабораторной, но приложенный файл содержит только "
    "перечень заданий, без самого примера. Строка сделана под фактическую модель расхода и стиль приложения.")

lab(7, "Явный и неявный Intent", "Реализована",
    "Явный Intent из настроек открывает AboutActivity; кнопка возврата вызывает finish() и возвращает "
    "пользователя в родительский экран. Неявный ACTION_SEND открывает системный выбор приложения для отправки "
    "описания TrackerMoney.",
    ["app/src/main/java/com/example/trackermoney/MainActivity.kt — создание обоих Intent.",
     "app/src/main/java/com/example/trackermoney/AboutActivity.kt — отдельная Activity и кнопка возврата.",
     "app/src/main/AndroidManifest.xml — регистрация AboutActivity.",
     "app/src/main/java/com/example/trackermoney/ui/SettingsScreen.kt — кнопки перехода и отправки."],
    '''startActivity(Intent(this@MainActivity, AboutActivity::class.java))
val shareIntent = Intent(Intent.ACTION_SEND).apply {
    type = "text/plain"
    putExtra(Intent.EXTRA_TEXT, getString(R.string.about_share_text))
}''')

lab(8, "Страницы на основе Fragment", "Реализована для истории и аналитики",
    "HistoryFragment и AnalyticsFragment являются реальными пунктами навигации. Они создают ComposeView и "
    "используют Activity-scoped TrackerViewModel, поэтому UI сохраняет текущий Compose-дизайн и общее состояние "
    "расходов.",
    ["app/src/main/java/com/example/trackermoney/ui/HistoryFragment.kt — история.",
     "app/src/main/java/com/example/trackermoney/ui/AnalyticsFragment.kt — аналитика.",
     "app/src/main/java/com/example/trackermoney/MainActivity.kt — FragmentContainerView и навигация.",
     "app/build.gradle.kts — зависимость androidx.fragment:fragment-ktx."],
    '''class HistoryFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = ComposeView(requireContext()).apply {
        setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
        setContent {
            val trackerViewModel: TrackerViewModel =
                viewModel(viewModelStoreOwner = requireActivity())
            val state by trackerViewModel.uiState.collectAsState()
            TrackerMoneyTheme(isDarkTheme = state.isDarkTheme) {
                HistoryScreen(
                    sortedExpenses = state.sortedExpenses,
                    onEdit = trackerViewModel::editExpense,
                    onDelete = trackerViewModel::deleteExpense
                )
            }
        }
    }
}''')

lab(9, "База данных и раздельные данные пользователей", "Облако и офлайн Room реализованы",
    "Supabase Auth обслуживает аккаунт, а Supabase PostgREST хранит облачные расходы. Room хранит локальную "
    "копию расходов и месячного бюджета, в том числе очередь офлайн-операций. Поле ownerUserId и запросы DAO "
    "разделяют локальные записи по текущему пользователю.",
    ["app/src/main/java/com/example/trackermoney/data/TrackerDatabase.kt — база tracker_money.db.",
     "app/src/main/java/com/example/trackermoney/data/ExpenseEntity.kt и MonthlyBudgetEntity.kt — локальные таблицы.",
     "app/src/main/java/com/example/trackermoney/data/ExpenseDao.kt — выборки, кэш и очередь UPSERT/DELETE.",
     "app/src/main/java/com/example/trackermoney/viewmodel/TrackerViewModel.kt — local-first запись и синхронизация.",
     "app/src/main/java/com/example/trackermoney/data/SupabaseRepository.kt — облачные запросы и авторизация.",
     "gradle/libs.versions.toml и app/build.gradle.kts — Room 2.7.2, KSP, Fragment и RecyclerView."],
    '''@Entity(tableName = "expenses", primaryKeys = ["id", "ownerUserId"])
data class ExpenseEntity(
    val id: String, val ownerUserId: String, val amount: Double,
    val category: String, val note: String, val date: Long,
    val pendingOperation: String? = null
)''',
    "После восстановления сети синхронизация запускается автоматически через ConnectivityManager, а также "
    "при запуске и повторной загрузке. Вход и регистрация требуют интернет; офлайн-режим доступен для уже "
    "вошедшего пользователя с существующей Room-копией. SQL-схема и RLS-политики Supabase не лежат в репозитории. "
    "Для фактической облачной изоляции проверьте, что политики таблицы expenses используют auth.uid() для "
    "SELECT/INSERT/UPDATE/DELETE.")

# Closing summary and manual acceptance checklist.
doc.add_page_break()
doc.add_heading("Итог и проверка", 1)
doc.add_paragraph(
    "В приложение добавлены XML-строка расхода и два адаптера, страницы History и Analytics на Fragment, "
    "AboutActivity с возвратом, системная отправка текста через Intent и Room-кэш с повторной отправкой "
    "операций. Существующие Compose-экраны и внешний стиль сохранены."
)
doc.add_heading("Краткая сводка", 2)
t = doc.add_table(rows=1, cols=3)
t.alignment = WD_TABLE_ALIGNMENT.CENTER
for i, value in enumerate(["Лаба", "Статус", "Результат"]):
    shade(t.rows[0].cells[i], "26345C")
    cell_text(t.rows[0].cells[i], value, True, "FFFFFF")
for values in [
    ("№3", "Выполнена", "Схема MainActivity показана в отчёте."),
    ("№4", "Выполнена", "Заставка, вход, регистрация и Toast."),
    ("№5", "Частично / адаптирована", "Темы, ресурсы, RU/EN и анимация готовы; буквальный фото/видео/GIF-пункт не подтверждён."),
    ("№6", "Выполнена", "XML-строка и два используемых RecyclerView-адаптера."),
    ("№7", "Выполнена", "Явный и неявный Intent, возврат из AboutActivity."),
    ("№8", "Выполнена", "Два Fragment-экрана."),
    ("№9", "Выполнена с проверкой", "Supabase + Room; проверить внешние RLS-политики."),
]:
    cells = t.add_row().cells
    for i, value in enumerate(values):
        cell_text(cells[i], value, i == 0)
doc.add_heading("Ручная проверка", 2)
for item in [
    "Выполнить :app:compileDebugKotlin, выбрав в Gradle JDK Android Studio 21.",
    "В истории нажать расход и проверить пункты изменения и удаления.",
    "В аналитике проверить диаграмму, цветную легенду и фильтры периодов.",
    "В настройках открыть AboutActivity, вернуться назад и открыть системный chooser.",
    "После входа загрузить расходы, отключить сеть и создать расход; убедиться, что он виден из Room.",
    "Восстановить сеть, убедиться в синхронизации в Supabase и отдельно проверить доступ под вторым аккаунтом.",
]:
    bullet(item)
doc.add_paragraph(
    "Сборка :app:compileDebugKotlin в текущем окружении завершилась успешно. Проверка на физическом устройстве "
    "и проверка Supabase RLS требуют ручного прогона; они не выполнялись автоматически."
)
doc.core_properties.title = "Отчёт по лабораторным работам TrackerMoney"
doc.core_properties.subject = "Реализация лабораторных работ 3–9"
doc.core_properties.author = ""
doc.save(output)
print(output)
