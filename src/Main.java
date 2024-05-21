import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;

import java.io.IOException;
import java.io.File;
import java.time.LocalDateTime;
import java.util.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Duration;
import java.time.ZoneId;

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
        Map<String, List<Ticket>> tickets = objectMapper.readValue(new File("tickets.json"), new TypeReference<Map<String, List<Ticket>>>() {});
        List<Ticket> flights = tickets.get("tickets"); // Вытаскиваем список объектов (билетов) из Мап-а

        SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd.MM.yy HH:mm");  // Создаем обработчик формата даты и времени

        Map<String, List<Duration>> flightDurations = new HashMap<>(); // Создаем мап для записи времени полетов для каждого перевозчика
        List<Integer> prices = new ArrayList<>(); // Создаем список для хранения цен билетов
        for (Ticket ticket : flights) {
            if (ticket.origin.equals("VVO") && ticket.destination.equals("TLV")) {// Делаем отбор где только VVO и TLV
                // Преобразуем строчные даты + время в Date
                Date departure = dateTimeFormat.parse(ticket.departure_date + " " + ticket.departure_time);
                Date arrival = dateTimeFormat.parse(ticket.arrival_date + " " + ticket.arrival_time);
                // Преобразуем Date в LocalDateTime (возможно не самый оптимальный вариант, но главное рабочий)
                LocalDateTime departureDateTime = LocalDateTime.ofInstant(departure.toInstant(), ZoneId.systemDefault());
                LocalDateTime arrivalDateTime = LocalDateTime.ofInstant(arrival.toInstant(), ZoneId.systemDefault());
                // Вычисляем разницу по времени
                Duration duration = Duration.between(departureDateTime, arrivalDateTime);
                List<Duration> durations = flightDurations.get(ticket.carrier);
                if (durations == null) {
                    durations = new ArrayList<>();
                    flightDurations.put(ticket.carrier, durations);
                }
                durations.add(duration);
                prices.add(ticket.price);
            }
        }

        for (Map.Entry<String, List<Duration>> entry : flightDurations.entrySet()) {
            // Находим минимальное время полета среди всех для конкретного перевозчика
            List<Duration> durations = entry.getValue();
            Duration minTravelTime = durations.get(0);
            for (Duration duration : durations) {
                if (duration.compareTo(minTravelTime) < 0) {
                    minTravelTime = duration;
                }
            }
            long days = minTravelTime.toDaysPart();
            long hours = minTravelTime.toHoursPart();
            long minutes = minTravelTime.toMinutesPart();
            System.out.println(String.format("Минимальное время полета для %s: %d д. %d ч. %d м.", entry.getKey(), days, hours, minutes));
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