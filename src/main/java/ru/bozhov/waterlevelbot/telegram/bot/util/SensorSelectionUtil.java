package ru.bozhov.waterlevelbot.telegram.bot.util;

import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.bozhov.waterlevelbot.sensor.model.Sensor;
import ru.bozhov.waterlevelbot.sensor.model.SensorAddress;
import ru.bozhov.waterlevelbot.sensor.repository.SensorRepository;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Component
public class SensorSelectionUtil {
    private final SensorRepository sensorRepo;
    private final Map<Long, Sensor> userSelection = new ConcurrentHashMap<>();

    // Кэши по шагам
    private final Map<Long, List<Sensor>>   unaddrCache  = new ConcurrentHashMap<>();
    private final Map<Long, List<Sensor>>   filtCache    = new ConcurrentHashMap<>();
    private final Map<Long, List<String>>   regionsCache = new ConcurrentHashMap<>();
    private final Map<Long, List<String>>   areaCache    = new ConcurrentHashMap<>();
    private final Map<Long, List<String>>   typeCache    = new ConcurrentHashMap<>();
    private final Map<Long, List<String>>   nameCache    = new ConcurrentHashMap<>();
    private final Map<Long, List<String>>   cityCache    = new ConcurrentHashMap<>();
    private final Map<Long, List<String>>   descCache    = new ConcurrentHashMap<>();

    // Текущие выборы
    private final Map<Long, String> currentRegion = new ConcurrentHashMap<>();
    private final Map<Long, String> currentArea   = new ConcurrentHashMap<>();
    private final Map<Long, String> currentType   = new ConcurrentHashMap<>();
    private final Map<Long, String> currentName   = new ConcurrentHashMap<>();
    private final Map<Long, String> currentCity   = new ConcurrentHashMap<>();
    private final Map<Long, String> currentDesc   = new ConcurrentHashMap<>();

    public SensorSelectionUtil(SensorRepository sensorRepo) {
        this.sensorRepo = sensorRepo;
    }

    public InlineKeyboardMarkup keyboardForUnaddressed(long chatId) {
        List<Sensor> list = sensorRepo.findByAddressIsNull();
        unaddrCache.put(chatId, list);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(list.get(i).getSensorName())
                            .callbackData("U" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("По адресу")
                        .callbackData("B0")
                        .build()
        ));
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Отмена")
                        .callbackData("G0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 2. Регионы
    public InlineKeyboardMarkup keyboardForRegions(long chatId) {
        List<String> regions = sensorRepo.findAll().stream()
                .map(Sensor::getAddress)
                .filter(Objects::nonNull)
                .map(SensorAddress::getRegion)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        regionsCache.put(chatId, regions);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < regions.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(regions.get(i))
                            .callbackData("R" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Отмена")
                        .callbackData("G0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 3. Районы
    public InlineKeyboardMarkup keyboardForLocalAreas(long chatId, String region) {
        currentRegion.put(chatId, region);
        List<String> areas = sensorRepo.findByAddressRegion(region).stream()
                .map(Sensor::getAddress)
                .filter(Objects::nonNull)
                .map(SensorAddress::getLocalArea)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        areaCache.put(chatId, areas);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < areas.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(areas.get(i))
                            .callbackData("A" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData("B0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 4. Типы водоёмов
    public InlineKeyboardMarkup keyboardForFeatureTypes(long chatId, String region, String localArea) {
        currentArea.put(chatId, localArea);
        List<String> types = sensorRepo.findByAddressRegionAndAddressLocalArea(region, localArea).stream()
                .map(Sensor::getAddress)
                .filter(Objects::nonNull)
                .map(SensorAddress::getWaterFeatureType)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        typeCache.put(chatId, types);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < types.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(types.get(i))
                            .callbackData("T" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData("B0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 5. Названия водоёмов
    public InlineKeyboardMarkup keyboardForFeatureNames(long chatId, String region, String localArea, String featureType) {
        currentType.put(chatId, featureType);
        List<String> names = sensorRepo.findByAddressRegionAndAddressLocalAreaAndAddressWaterFeatureType(region, localArea, featureType).stream()
                .map(Sensor::getAddress)
                .filter(Objects::nonNull)
                .map(SensorAddress::getWaterFeatureName)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        nameCache.put(chatId, names);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < names.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(names.get(i))
                            .callbackData("N" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData("B0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 6. Города
    public InlineKeyboardMarkup keyboardForCities(long chatId, String region, String localArea, String featureType, String featureName) {
        currentName.put(chatId, featureName);
        Map<String, String> filters = Map.of(
                "region", region,
                "localArea", localArea,
                "waterFeatureType", featureType,
                "waterFeatureName", featureName
        );
        List<String> cities = sensorRepo.findByFilters(filters).stream()
                .map(Sensor::getAddress)
                .filter(Objects::nonNull)
                .map(SensorAddress::getNearestCity)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        cityCache.put(chatId, cities);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < cities.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(cities.get(i))
                            .callbackData("C" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData("B0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 7. Описания
    public InlineKeyboardMarkup keyboardForDescriptions(long chatId, String region, String localArea, String featureType, String featureName, String city) {
        currentCity.put(chatId, city);
        Map<String, String> filters = new HashMap<>();
        filters.put("region", region);
        filters.put("localArea", localArea);
        filters.put("waterFeatureType", featureType);
        filters.put("waterFeatureName", featureName);
        filters.put("nearestCity", city);

        List<String> descs = sensorRepo.findByFilters(filters).stream()
                .map(Sensor::getAddress)
                .filter(Objects::nonNull)
                .map(SensorAddress::getDescription)
                .filter(Objects::nonNull)
                .distinct()
                .collect(Collectors.toList());
        descCache.put(chatId, descs);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < descs.size(); i++) {
            String d = descs.get(i);
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(d.length() > 20 ? d.substring(0, 20) + "..." : d)
                            .callbackData("D" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData("B0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    // 8. Отфильтрованные сенсоры
    public InlineKeyboardMarkup keyboardForFilteredSensors(long chatId, Map<String, String> filters, String desc) {
        currentDesc.put(chatId, desc);
        if (desc != null) {
            filters.put("description", desc);
        }
        List<Sensor> list = sensorRepo.findByFilters(filters);
        filtCache.put(chatId, list);

        List<List<InlineKeyboardButton>> rows = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            rows.add(Collections.singletonList(
                    InlineKeyboardButton.builder()
                            .text(list.get(i).getSensorName())
                            .callbackData("S" + i)
                            .build()
            ));
        }
        rows.add(Collections.singletonList(
                InlineKeyboardButton.builder()
                        .text("Назад")
                        .callbackData("B0")
                        .build()
        ));
        return new InlineKeyboardMarkup(rows);
    }

    public EditMessageText handleSelection(Update update, String cb, int messageId) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String text;
        InlineKeyboardMarkup markup;

        // Обработка CONFIGURE_SENSOR перед однобуквенным разбором
        if ("CONFIGURE_SENSOR".equals(cb) || "SHOW_STATS".equals(cb) || "CURRENT_DATA".equals(cb)) {
            text = "Выберите датчик (без адреса) или по адресу:";
            markup = keyboardForUnaddressed(chatId);
        }
        else {
            switch (cb.charAt(0)) {
                case 'U' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    Sensor s = unaddrCache.get(chatId).get(idx);
                    userSelection.put(chatId, s);
                    return null;
                }
                case 'S' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    Sensor s = filtCache.get(chatId).get(idx);
                    userSelection.put(chatId, s);
                    text = "Выбран: " + s.getSensorName();
                    markup = new InlineKeyboardMarkup(
                            Collections.singletonList(
                                    Collections.singletonList(
                                            InlineKeyboardButton.builder()
                                                    .text("Назад")
                                                    .callbackData("G0")
                                                    .build()
                                    )
                            )
                    );
                }
                case 'B' -> {
                    text = "Выберите регион:";
                    markup = keyboardForRegions(chatId);
                }
                case 'R' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    String region = regionsCache.get(chatId).get(idx);
                    text = "Районы в " + region + ":";
                    markup = keyboardForLocalAreas(chatId, region);
                }
                case 'A' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    String region = currentRegion.get(chatId);
                    String area = areaCache.get(chatId).get(idx);
                    text = "Типы в " + area + ":";
                    markup = keyboardForFeatureTypes(chatId, region, area);
                }
                case 'T' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    String region = currentRegion.get(chatId);
                    String area = currentArea.get(chatId);
                    String type = typeCache.get(chatId).get(idx);
                    text = "Названия типа " + type + ":";
                    markup = keyboardForFeatureNames(chatId, region, area, type);
                }
                case 'N' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    String region = currentRegion.get(chatId);
                    String area = currentArea.get(chatId);
                    String type = currentType.get(chatId);
                    String name = nameCache.get(chatId).get(idx);
                    text = "Города у " + name + ":";
                    markup = keyboardForCities(chatId, region, area, type, name);
                }
                case 'C' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    String region = currentRegion.get(chatId);
                    String area = currentArea.get(chatId);
                    String type = currentType.get(chatId);
                    String name = currentName.get(chatId);
                    String city = cityCache.get(chatId).get(idx);
                    text = "Описания в " + city + ":";
                    markup = keyboardForDescriptions(chatId, region, area, type, name, city);
                }
                case 'D' -> {
                    int idx = Integer.parseInt(cb.substring(1));
                    String region = currentRegion.get(chatId);
                    String area = currentArea.get(chatId);
                    String type = currentType.get(chatId);
                    String name = currentName.get(chatId);
                    String city = currentCity.get(chatId);
                    String desc = descCache.get(chatId).get(idx);
                    text = "Сенсоры с этим описанием:";
                    Map<String, String> filters = new HashMap<>();
                    filters.put("region", region);
                    filters.put("localArea", area);
                    filters.put("waterFeatureType", type);
                    filters.put("waterFeatureName", name);
                    filters.put("nearestCity", city);
                    markup = keyboardForFilteredSensors(chatId, filters, desc);
                }
                case 'G', '0' -> {
                    text = "Выберите датчик (без адреса) или по адресу:";
                    markup = keyboardForUnaddressed(chatId);
                }
                default -> {
                    return null;
                }
            }
        }

        return EditMessageText.builder()
                .chatId(String.valueOf(chatId))
                .messageId(messageId)
                .text(text)
                .replyMarkup(markup)
                .build();
    }

    public Sensor getSelection(Long chatId) {
        return userSelection.get(chatId);
    }

    public void clearState(long chatId) {
        unaddrCache.remove(chatId);
        filtCache.remove(chatId);
        regionsCache.remove(chatId);
        areaCache.remove(chatId);
        typeCache.remove(chatId);
        nameCache.remove(chatId);
        cityCache.remove(chatId);
        descCache.remove(chatId);
        currentRegion.remove(chatId);
        currentArea.remove(chatId);
        currentType.remove(chatId);
        currentName.remove(chatId);
        currentCity.remove(chatId);
        currentDesc.remove(chatId);
        userSelection.remove(chatId);
    }

}
