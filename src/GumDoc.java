/**
 * Gum.sh - скрипт для запуска тестов и компиляции решений
 * <p>
 * Необходимо узказать свои собственные пути к исходинкам и тестам.
 * Это можно сделать положив рядом со скриптом файлик с именем 'gump'.
 * Данные оттуда будут подгружаться при каждом запуске скрипта.
 * В нём нужно сохранить 6 строк:
 * {ваша фамилия}
 * {абсолютный путь к папке с тестами}
 * {абсолютный путь к папке с либами}
 * {абсолютный путь к пакету с решениями}
 * {номер домашки запускаемой по умолчанию}
 * {соль}
 * Пятая и шестая строки опционально
 * <p>
 * Запуск через терминал: ./Gum.sh
 * <p>
 * Список команд:
 * Для выбора номера задания введите соответствующее число от 1 до 8.
 * Начиная с этого момента и до ввода нового числа из диапазона вы
 * будете работать с выбранным заданием
 * <p>
 * Запуск тестов и компиляция.
 * r - запуск текущего задания
 * c - компиляция текущего задания
 * cr - компиляция и запуск текущего задания
 * <p>
 * При первом после выбора задания запуске будет запрошено имя
 * модификации (простая/сложная версия). Для смены модификации
 * (в частности если она была введена с ошибкой) необходимо выбрать
 * задание заново и ввести необходимую
 * <p>
 * Путь для создания *.class - по умолчанию
 * Перед компиляцией удаляются *.class, лежащие рядом с главным классом
 * <p>
 * Другие команды:
 * gump - считывает файл 'gump' заново
 * <p>
 * check - время последнего изменения тестов к текущей задаче
 * <p>
 * clone - клонит реп с тестами в текущую директорию, копирует небходимые
 * либы и кладёт их рядом со скриптом
 * <p>
 * info - выдаёт информацию о classpath и других переменных скрипта
 * <p>
 * help - выдаёт короткую справку
 * <p>
 * exit - завершает работу
 */
public class GumDoc {
}