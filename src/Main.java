import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.IOException;
import java.io.File;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;


class Main {
    public static class Ticket {
        //Структура обьектов файла
        public String origin;
        public String origin_name;
        public String destination;
        public String destination_name;
        public String departure_date;
        public String departure_time;
        public String arrival_date;
        public String arrival_time;
        public String carrier;
        public int stops;
        public int price;
    }

    public static void main(String[] args) throws IOException, ParseException {
        ObjectMapper objectMapper = new ObjectMapper();

        // Загружаем файл с данными формата JSON с помощью методов библиотеки Jackson
        Map<String, List<Ticket>> tickets = objectMapper.readValue(new File("tickets.json"), new TypeReference<Map<String, List<Ticket>>>(){});
        List<Ticket> flights = tickets.get("tickets"); // Вытаскиваем список объектов (билетов) из Мап-а
        SimpleDateFormat parser = new SimpleDateFormat("HH:mm");  // Создаем обработчик формата времени
        Map<String, List<Duration>> flightTimes = new HashMap<>(); // Создаем мап для записи времени полетов для каждого перевозчика
        List<Integer> prices = new ArrayList<>(); // Создаем список для хранения цен билетов

        for (Ticket ticket : flights) {
            if (ticket.origin.equals("VVO") && ticket.destination.equals("TLV")) {// Делаем отбор где только VVO и TLV
                Date departure = parser.parse(ticket.departure_time);
                Date arrival = parser.parse(ticket.arrival_time);
                long milliseconds = arrival.getTime() - departure.getTime();
                Duration duration = Duration.ofMillis(milliseconds);



                flightTimes.computeIfAbsent(ticket.carrier, k -> new ArrayList<>()).add(duration);  // Записываем время полета в map



                prices.add(ticket.price); // Записываем стоимость билета в список
            }
        }

        for (Map.Entry<String, List<Duration>> entry : flightTimes.entrySet()) {

            // Находим минимальное время полета и выводим результат
            Duration min = Collections.min(entry.getValue());
            System.out.println("Минимальное время полета для " + entry.getKey() + ": " + min.toHoursPart() + " часов и " + min.toMinutesPart() + " минут");
        }

        // Сортируем список цен для дальнейшего вычисления медианы
        Collections.sort(prices);

        double sumPrices = 0;
        int numPrises = prices.size();
        for(double price : prices){ //Находим среднюю цену

            sumPrices = sumPrices + price;
        }
        double average =   sumPrices/numPrises;

        double median;

        // Вычисляем медиану в зависимости от четности размера списка
        if (numPrises % 2 == 0)
            median = (prices.get(numPrises/2) + prices.get((numPrises-1)/2))/ 2; /*находим среднее число между двумя средними значениями,
             индекс минусуем, так как первое значение имеет индекс 0 */
        else
            median = prices.get(numPrises/2);

        System.out.println("Разница между средней ценой и медианой: " + (average - median));

    }
}