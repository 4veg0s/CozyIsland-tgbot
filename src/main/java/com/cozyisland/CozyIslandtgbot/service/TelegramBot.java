package com.cozyisland.CozyIslandtgbot.service;

import com.cozyisland.CozyIslandtgbot.config.BotConfig;
import com.cozyisland.CozyIslandtgbot.model.entity.*;
import com.cozyisland.CozyIslandtgbot.model.entity.User;
import com.cozyisland.CozyIslandtgbot.model.repository.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.send.SendPhoto;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.*;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardRemove;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    private static final String DONATE_MONEY_TEXT = "Информация появится здесь позже";
    private static final String DONATE_FOOD_TEXT = "<b>Для собак:</b>\n" +
            "- <i>Chappi</i> говядина\n" +
            "- <i>Organix</i> консервы для собак (с различными вкусами)\n" +
            "- <i>Smart Dog</i> консервы для собак (с различными вкусами)\n" +
            "- <i>Organix Adult Dog Turkey</i> для собак с индейкой для чувствительного пищеварения\n" +
            "<b>Для кошек:</b>\n" +
            "- <i>Florida</i> сухой корм для взрослых кошек \n" +
            "- <i>Organix</i> консервы для взрослых кошек и котят (с различными вкусами)\n" +
            "- <i>Royal Canin Mother&Babycat</i> мусс для котят";

    private static final String DONATE_DRUGS_TEXT = "- Антибактериальные препараты\n" +
            "- Инфузионные растворы\n" +
            "- Нестероидные противовоспалительное\n" +
            "- Инъекционные растворы\n" +
            "- Салфетки, шприцы, лейкопластырь, перчатки, пеленки";
    private static final String DONATE_HOUSEKEEPING_TEXT = "- дезинфицирующее средство \n" +
            "- средства для мытья кафеля, полов, стен, столов\n" +
            "- средства от ржавчины\n" +
            "- жидкое мыло для рук\n" +
            "- средства для мытья стеклянных поверхностей\n" +
            "- средства для мытья посуды\n" +
            "- тряпки для мытья полов, стен\n" +
            "- губки для мытья мисок, посуды\n" +
            "- полиэтиленовые мешки для мусора\n" +
            "- перчатки х/б рабочие\n" +
            "- перчатки резиновые плотные\n" +
            "- веники\n" +
            "- метлы пластиковые\n" +
            "- совки (металлические с деревянной ручкой)\n" +
            "- грабли\n" +
            "- ведра\n" +
            "- швабры\n" +
            "- контейнеры для мусора\n" +
            "- канцелярия (бумага для принтера, ручки, прозрачные файлы, бумага для записей, большие папки)\n" +
            "- пледы, одеяла, покрывала, скатерти, ковролин, ковры";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private VolunteerApplicationRepository volunteerApplicationRepository;
    @Autowired
    private PetImageRepository petImageRepository;
    @Autowired
    private BinaryContentRepository binaryContentRepository;
    static final String START_TEXT = EmojiParser.parseToUnicode("Привет, %s!\n" +
            "Добро пожаловать на \n" +
            ":feet:<b>Островок тепла</b>:feet:!");

    // TODO: добавить стикеры из emojipedia
    static final String HELP_TEXT = EmojiParser.parseToUnicode("Мы предоставляем профессиональную ветеринарную помощь животным. " +
            "Осуществляем профилактику заболеваний, проводим регулярные осмотры и лечение животных" +
            ", которые нуждаются в медицинской помощи." +
            " Заботимся о заведении медицинской истории каждого питомца\n\n");
    static final String PET_DATA_TEMPLATE = "<b>Информация о питомце</b>%n%n" +
            "<b>Номер:</b> %d%n" +
            "<b>Категория:</b> %s%n" +
            "<b>Кличка:</b> %s%n" +
            "<b>Возраст:</b> %s%n" +
            "<b>Стерилизован(а):</b> %s%n" +
            "<b>Характер:</b> %s%n%n" +
            "<i>%d/%d</i>";
    //"Возраст: %s%n";
    static final String VOLUNTEER_APPLICATION_TEMPLATE = "<b>Заявка на волонтерство</b>%n%n" +
            "<b>ID пользователя:</b> %d%n" +
            "<b>Имя:</b> %s%n" +
            "<b>Телефон:</b> <code>%s</code>%n" +
            "<b>Имя пользователя:</b> @%s%n" +
            "<b>Дата и время подачи заявки:</b> %s%n" +
            "<b>Статус заявки:</b> %s%n%n" +
            "<i>%d/%d</i>";
    final BotConfig config;
    List<Long> superUsers;
    List<BotCommand> listOfCommands;
    List<Pet> petList;
    int currentPetIndex;
    boolean petAddMode = false;
    List<VolunteerApplication> volunteerApplicationList;
    int currentVolunteerApplicationIndex;


    public TelegramBot(BotConfig config) {
        this.config = config;

        superUsers = new ArrayList<>();
        superUsers.add((long) 416657716);   // Андрей

        listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "начать общение"));
        listOfCommands.add(new BotCommand("/menu", "вызвать главное меню"));
        listOfCommands.add(new BotCommand("/help", "справочная информация"));

        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        } catch (TelegramApiException e) {
            log.error("Error while setting up bot command list: " + e.getMessage());
        }
    }

    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getBotToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()) {
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageText) {
                case "/start" -> {
                    startCommandReceived(update);
                }
                case "/menu" -> {
                    menuCommandReceived(chatId);
                }
                case "/help" -> {
                    helpCommandReceived(update);
                }
                default -> {
                    unusualMessageReceived(update);
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            contactReceived(update);
        } else if (update.hasMessage() && update.getMessage().hasPhoto()) {
            photoReceived(update);
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            if (callbackData.startsWith("PETS")) {
                switch (callbackData) {
                    case CallbackConstants.PETS -> {
                        inlinePets(update);
                    }
                    case CallbackConstants.PETS_PREVIOUS -> {
                        previousPet(update);
                    }
                    case CallbackConstants.PETS_NEXT -> {
                        nextPet(update);
                    }
                    case CallbackConstants.PETS_DELETE -> {
                        deletePet(update);
                    }
                    case CallbackConstants.PETS_ADD -> {
                        addPet(update);
                    }
                    case CallbackConstants.PETS_ADD_CONFIRM -> {
                        saveNewPet(update);
                    }
                    case CallbackConstants.PETS_ADD_CANCEL -> {
                        addPet(update);
                    }
                }
            } else if (callbackData.startsWith("FEEDBACK")) {
                switch (callbackData) {
                    case CallbackConstants.FEEDBACK -> {
                        inlineFeedback(update);
                    }
                    case CallbackConstants.FEEDBACK_SHOW -> {
                        // TODO: просмотреть отзывы
                    }
                    case CallbackConstants.FEEDBACK_NEW -> {
                        inlineFeedbackNew(update);
                    }
                }
            } else if (callbackData.startsWith("VOLUNTEER")) {
                switch (callbackData) {
                    case CallbackConstants.VOLUNTEER -> {
                        inlineVolunteer(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS -> {
                        inlineVolunteerApplications(update);
                    }
                    case CallbackConstants.VOLUNTEER_SEND_CONTACT -> {
                        inlineVolunteerSendContact(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_PREVIOUS -> {
                        previousVolunteerApplication(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_NEXT -> {
                        nextVolunteerApplication(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_DELETE -> {
                        deleteVolunteerApplication(update);
                    }
                    /*case CallbackConstants.VOLUNTEER_APPLICATIONS_ADD -> {
                        addVolunteerApplication(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_ADD_CONFIRM -> {
                        saveNewVolunteerApplication(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_ADD_CANCEL -> {
                        addVolunteerApplication(update);
                    }*/
                }
            } else if (callbackData.startsWith("DONATE")) {
                switch (callbackData) {
                    case CallbackConstants.DONATE -> {
                        inlineDonate(update);
                    }
                    case CallbackConstants.DONATE_MONEY -> {
                        donateMoney(update);
                    }
                    case CallbackConstants.DONATE_FOOD -> {
                        donateFood(update);
                    }
                    case CallbackConstants.DONATE_DRUGS -> {
                        donateDrugs(update);
                    }
                    case CallbackConstants.DONATE_HOUSEKEEPING -> {
                        donateHousekeeping(update);
                    }
                }
            }
            switch (callbackData) {
                case CallbackConstants.RETURN_TO_MENU -> {
                    inlineReturnToMenu(update);
                }
            }
        }
    }

    private void donateMoney(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String textToSend = DONATE_MONEY_TEXT;

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, messageId, textToSend, keyboardMarkup);
        executeMessage(message);
    }


    private void donateFood(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String textToSend = DONATE_FOOD_TEXT;

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, messageId, textToSend, keyboardMarkup);
        executeMessage(message);
    }

    private void donateDrugs(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String textToSend = DONATE_DRUGS_TEXT;

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, messageId, textToSend, keyboardMarkup);
        executeMessage(message);
    }

    private void donateHousekeeping(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String textToSend = DONATE_HOUSEKEEPING_TEXT;

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, messageId, textToSend, keyboardMarkup);
        executeMessage(message);
    }

    private void saveNewPet(Update update) {
        String messageText = update.getCallbackQuery().getMessage().getText();
        String hiddenCommand = messageText.substring(0, messageText.indexOf("\n"));

        Pet transientNewPet = parsePet(hiddenCommand);

        saveToPetRepository(transientNewPet);

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String textToSend = "Новый питомец успешно добавлен";

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Питомцы", CallbackConstants.PETS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, keyboardMarkup);
        executeMessage(message);
    }

    private void saveToPetRepository(Pet transientNewPet) {
        petRepository.save(transientNewPet);
        log.info("Added new pet to petRepository: " + transientNewPet.toString());
    }

    private void addPet(Update update) {
        petAddMode = true;

        Pet defaultPet = new Pet();

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String textToSend = petTemplateInsert(defaultPet) +
                "\n\n(порядок аргументов:point_up_2:)\n" +
                "Введите данные нового питомца аналогично примеру:\n" +
                "Нажмите на текст ниже, чтобы скопировать\n\n" +
                "<code>/pet Кошка; Искорка; 6 месяцев; false; аккуратная, ласковая</code>";

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Отмена", CallbackConstants.PETS))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, keyboardMarkup);

        executeMessage(message);
    }

    private void deletePet(Update update) {
        Pet currentPet = petList.get(currentPetIndex);
        if (petRepository.findById(currentPet.getId()).isEmpty()) {
            log.error("Couldn't find pet in petList");
        } else {
            petRepository.deleteById(currentPet.getId());
            log.info("Successfully deleted from petList: " + currentPet.toString());
            if (currentPetIndex == 0) currentPetIndex++;
        }

        petList = reloadPetList();
        previousPet(update);
    }

    private void previousPet(Update update) {
        currentPetIndex = (currentPetIndex == 0) ? petList.size() - 1 : --currentPetIndex;
        showPet(petList, currentPetIndex, update);
    }

    private void nextPet(Update update) {
        currentPetIndex = (currentPetIndex == petList.size() - 1) ? 0 : ++currentPetIndex;
        showPet(petList, currentPetIndex, update);
    }

    private void inlinePets(Update update) {
        if (petAddMode)
            petAddMode = false;     // если ожидался новый питомец, но был выход из команды - отмена режима редактирования

        petList = reloadPetList();
        currentPetIndex = 0;

        showPet(petList, currentPetIndex, update);
    }

    private List<Pet> reloadPetList() {
        return new ArrayList<>(StreamSupport
                .stream(petRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }


    private void showPet(List<Pet> petList, int currentPetIndex, Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        if (petList.size() == 0) {
            String textToSend = "На данный момент в приюте нет ни одного питомца";
            EditMessageText message = editMessage(chatId, messageId, textToSend, petsMenuInlineMarkup(chatId));
            executeMessage(message);
        } else {
            Pet currentPet = petList.get(currentPetIndex);
            String petInfo = petTemplateInsert(currentPet);
            EditMessageText message = editMessage(chatId, messageId, petInfo, petsMenuInlineMarkup(chatId));
            executeMessage(message);
        }
    }

    private String petTemplateInsert(Pet currentPet) {
        return String.format(PET_DATA_TEMPLATE, currentPetIndex + 1,
                currentPet.getCategory(),
                currentPet.getName(),
                currentPet.getAge(),
                ((currentPet.isSterilized()) ? "да" : "нет"),
                currentPet.getAbout(),
                currentPetIndex + 1,
                petList.size()
        );
    }

    private InlineKeyboardMarkup petsMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (petList.size() > 1) {
            row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.PETS_PREVIOUS));
            row.add(createInlineButton(EmojiParser.parseToUnicode("Забрать"), CallbackConstants.PETS_TAKE));
            row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.PETS_NEXT));
        }
        rowsInline.add(row);

        if (superUsers.contains(chatId)) {
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.PETS_ADD))));
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Редактировать", CallbackConstants.PETS_EDIT))));
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.PETS_DELETE))));
        }

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineVolunteerApplications(Update update) {
        volunteerApplicationList = reloadVolunteerApplicationList();
        currentVolunteerApplicationIndex = 0;

        showVolunteerApplication(volunteerApplicationList, currentVolunteerApplicationIndex, update);
    }

    private InlineKeyboardMarkup volunteerApplicationsMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (volunteerApplicationList.size() > 1) {
            row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.VOLUNTEER_APPLICATIONS_PREVIOUS));
            row.add(createInlineButton(EmojiParser.parseToUnicode("Одобрить"), CallbackConstants.VOLUNTEER_APPLICATIONS_APPROVE));
            row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.VOLUNTEER_APPLICATIONS_NEXT));
        }
        rowsInline.add(row);

        if (superUsers.contains(chatId)) {
            //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.VOLUNTEER_APPLICATIONS_ADD))));
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Редактировать", CallbackConstants.VOLUNTEER_APPLICATIONS_EDIT))));
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.VOLUNTEER_APPLICATIONS_DELETE))));
        }

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.VOLUNTEER))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    /*private void addVolunteerApplication(Update update) {
        Pet defaultPet = new Pet();

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String textToSend = petTemplateInsert(defaultPet) +
                "\n\n(порядок аргументов:point_up_2:)\n" +
                "Введите данные нового питомца аналогично примеру:\n" +
                "Нажмите на текст ниже, чтобы скопировать\n\n" +
                "<code>/pet Кошка; Искорка; 6 месяцев; false; аккуратная, ласковая</code>";

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Отмена", CallbackConstants.PETS))));

        keyboardMarkup.setKeyboard(rowsInline);

        EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, keyboardMarkup);

        executeMessage(message);
    }*/

    private void deleteVolunteerApplication(Update update) {
        VolunteerApplication currentApplication = volunteerApplicationList.get(currentVolunteerApplicationIndex);
        if (volunteerApplicationRepository.findById(currentApplication.getChatId()).isEmpty()) {
            log.error("Couldn't find application in volunteerApplicationList");
        } else {
            volunteerApplicationRepository.deleteById(currentApplication.getChatId());
            log.info("Successfully deleted from volunteerApplicationList: " + currentApplication);
            if (currentVolunteerApplicationIndex == 0) currentVolunteerApplicationIndex++;
        }

        volunteerApplicationList = reloadVolunteerApplicationList();
        previousVolunteerApplication(update);
    }

    private void previousVolunteerApplication(Update update) {
        currentVolunteerApplicationIndex = (currentVolunteerApplicationIndex == 0) ? volunteerApplicationList.size() - 1 : --currentVolunteerApplicationIndex;
        showVolunteerApplication(volunteerApplicationList, currentVolunteerApplicationIndex, update);
    }

    private void nextVolunteerApplication(Update update) {
        currentVolunteerApplicationIndex = (currentVolunteerApplicationIndex == volunteerApplicationList.size() - 1) ? 0 : ++currentVolunteerApplicationIndex;
        showVolunteerApplication(volunteerApplicationList, currentVolunteerApplicationIndex, update);
    }

    private List<VolunteerApplication> reloadVolunteerApplicationList() {
        return new ArrayList<>(StreamSupport
                .stream(volunteerApplicationRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }

    private void showVolunteerApplication(List<VolunteerApplication> volunteerApplicationList, int currentVolunteerApplicationIndex, Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        if (volunteerApplicationList.size() == 0) {
            String textToSend = "На данный момент нет ни одной заявки на волонтерство";
            EditMessageText message = editMessage(chatId, messageId, textToSend, volunteerApplicationsMenuInlineMarkup(chatId));
            executeMessage(message);
        } else {
            VolunteerApplication currentApplication = volunteerApplicationList.get(currentVolunteerApplicationIndex);
            String volunteerApplicationInfo = volunteerApplicationTemplateInsert(currentApplication);
            EditMessageText message = editMessage(chatId, messageId, volunteerApplicationInfo, volunteerApplicationsMenuInlineMarkup(chatId));
            executeMessage(message);
        }
    }

    private String volunteerApplicationTemplateInsert(VolunteerApplication currentApplication) {
        return String.format(VOLUNTEER_APPLICATION_TEMPLATE,
                currentApplication.getChatId(),
                currentApplication.getFirstname(),
                currentApplication.getPhoneNumber(),
                currentApplication.getUserName(),
                currentApplication.getAppliedAt(),
                currentApplication.getStatus(),
                currentVolunteerApplicationIndex + 1,
                volunteerApplicationList.size()
        );
    }

    private InlineKeyboardMarkup volunteerMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Продолжить", CallbackConstants.VOLUNTEER_SEND_CONTACT))));

        if (superUsers.contains(chatId)) {
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Заявки", CallbackConstants.VOLUNTEER_APPLICATIONS))));
        }

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineVolunteerSendContact(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        // TODO: текст для волонтера
        String textToSend = "Чтобы отправить свой контакт, нажмите на кнопку внизу:point_down:";

        DeleteMessage menuMessage = new DeleteMessage();
        menuMessage.setChatId(chatId);
        menuMessage.setMessageId(userRepository.findById(chatId).get().getMenuMessageId());
        deleteMessage(menuMessage);

        ReplyKeyboardMarkup replyKeyboardMarkup = new ReplyKeyboardMarkup();
        replyKeyboardMarkup.setResizeKeyboard(true);
        replyKeyboardMarkup.setSelective(true);
        replyKeyboardMarkup.setIsPersistent(false);
        replyKeyboardMarkup.setOneTimeKeyboard(false);

        KeyboardButton contactButton = new KeyboardButton();
        contactButton.setRequestContact(true);
        contactButton.setText(EmojiParser.parseToUnicode("Отправить свой контакт :telephone:"));

        KeyboardButton mainMenuButton = new KeyboardButton();
        mainMenuButton.setText(EmojiParser.parseToUnicode("Главное меню"));


        List<KeyboardRow> keyboardRows = new ArrayList<>();
        KeyboardRow row = new KeyboardRow();
        row.add(contactButton);

        keyboardRows.add(row);

        row = new KeyboardRow();
        row.add(mainMenuButton);

        keyboardRows.add(row);

        replyKeyboardMarkup.setKeyboard(keyboardRows);

        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(replyKeyboardMarkup);

        sendMessage(chatId, textToSend, replyKeyboardMarkup);
    }

    private void photoReceived(Update update) {
        BinaryContent transientBinaryContent = BinaryContent.builder()
                .fileAsArrayOfBytes(update.getMessage().getPhoto().get(1).getFileId().getBytes())
                .build();

        BinaryContent persistantBinaryContent = binaryContentRepository.save(transientBinaryContent);

        PetImage transientPetImage = PetImage.builder()
                .binaryContent(persistantBinaryContent)
                .build();

        PetImage persistantPetImage = petImageRepository.save(transientPetImage);
        String petImageFile = "petImage" + persistantPetImage.getImageId();
        try {
            FileOutputStream fileOutputStream = new FileOutputStream(petImageFile);
            BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream);
            bufferedOutputStream.write(persistantPetImage.getBinaryContent().getFileAsArrayOfBytes());
        } catch (IOException e) {
            log.error("Error while trying to write array of bytes to file: " + e.getMessage());
        }

        InputFile inputFile = new InputFile().setMedia(petImageFile);
        SendPhoto photo = SendPhoto.builder()
                .chatId(update.getMessage().getChatId())
                .caption(petImageFile)
                .photo(inputFile)
                .build();
        try {
            execute(photo);
        } catch (TelegramApiException e) {
            log.error("Error while executing SendPhoto: " + e.getMessage());
        }
    }

    private void inlineFeedbackNew(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "Оставьте Ваш отзыв о нашем приюте";
        EditMessageText message = editMessage(chatId, messageId, textToSend, newFeedbackInlineMarkup());
        executeMessage(message);
    }

    private void inlineReturnToMenu(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        EditMessageText message = editMessage(chatId, messageId, "<b>Главное меню</b> нашего приюта", mainMenuInlineMarkup());
        executeMessage(message);
    }

    private void inlineVolunteer(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел волонтерства</b>\n" +
                "Чтобы оставить заявку на волонтерскую помощь нашему приюту, отправьте нам свой контакт, и мы свяжемся с Вами";

        EditMessageText message = editMessage(chatId, messageId, textToSend, volunteerMenuInlineMarkup(chatId));

        executeMessage(message);
    }

    private void contactReceived(Update update) {
        registerVolunteerApplication(update);
    }

    private void registerVolunteerApplication(Update update) {
        Contact contact = update.getMessage().getContact();
        long chatId = contact.getUserId();

        String textToSend;
        if (volunteerApplicationRepository.findById(chatId).isEmpty()) {
            VolunteerApplication application = VolunteerApplication.builder()
                    .chatId(chatId)
                    .firstname(contact.getFirstName())
                    .userName(update.getMessage().getChat().getUserName())
                    .phoneNumber(contact.getPhoneNumber())
                    .status("на рассмотрении")
                    .build();

            volunteerApplicationRepository.save(application);

            textToSend = "Ваш контакт (" + contact.getPhoneNumber() + ") передан менеджеру\n" +
                    "В ближайший рабочий день мы обработаем Вашу заявку и перезвоним Вам\n" +
                    "Ожидайте, пожалуйста";
        } else {
            textToSend = "Ваша предыдущая заявка на волонтерство уже находится в обработке\n" +
                    "Ожидайте, пожалуйста";
        }
        sendMessage(contact.getUserId(), textToSend, new ReplyKeyboardRemove(true, null));

        DeleteMessage message = new DeleteMessage();
        message.setChatId(contact.getUserId());
        message.setMessageId(userRepository.findById(contact.getUserId()).get().getMenuMessageId());
        deleteMessage(message);

        sendMessage(contact.getUserId(), "Переход в главное меню");
        menuCommandReceived(contact.getUserId());
    }


    private void inlineFeedback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел отзывов</b> :speech_balloon:";

        EditMessageText message = editMessage(chatId, messageId, textToSend, feedbackMenuInlineMarkup());

        executeMessage(message);
    }

    private InlineKeyboardMarkup feedbackMenuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Просмотреть отзывы", CallbackConstants.FEEDBACK_SHOW))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Оставить отзыв", CallbackConstants.FEEDBACK_NEW))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup newFeedbackInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_1));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_2));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_3));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_4));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_5));
        rowsInline.add(row);

        //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.BACK))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineDonate(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел пожертвований</b>\n" +
                "Мы принимаем:";

        EditMessageText message = editMessage(chatId, messageId, textToSend, donateMenuInlineMarkup());

        executeMessage(message);
    }

    private InlineKeyboardMarkup donateMenuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Деньги :money_with_wings:", CallbackConstants.DONATE_MONEY))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Корм и консервы :stew:", CallbackConstants.DONATE_FOOD))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Лекарства :pill:", CallbackConstants.DONATE_DRUGS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Хозяйственные нужды :broom:", CallbackConstants.DONATE_HOUSEKEEPING))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }


    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getChat().getFirstName();

        if (userRepository.findById(chatId).isEmpty()) {
            User user = User.builder()
                    .chatId(chatId)
                    .build();
            userRepository.save(user);
        }

        String startResponse = String.format(START_TEXT, name);

        sendMessage(chatId, startResponse);

        menuCommandReceived(chatId);
    }

    private void menuCommandReceived(long chatId) {
        DeleteMessage message = new DeleteMessage();
        message.setChatId(chatId);
        User currentUser = userRepository.findById(chatId).get();
        message.setMessageId(currentUser.getMenuMessageId());
        deleteMessage(message);

        // TODO: реализовать изменение в базе айди сообщения с главным меню
        currentUser.setMenuMessageId(sendMessage(chatId, "<b>Главное меню</b> нашего приюта", mainMenuInlineMarkup()));
        userRepository.save(currentUser);
    }

    private void helpCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();

        sendMessage(chatId, HELP_TEXT);
        log.info("Replied to HELP command from user https://t.me/" + userName + " with chatId = " + chatId);
    }

    private int sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(textToSend));
        message.setParseMode(ParseMode.HTML);

        return executeMessage(message);
    }

    private int sendMessage(long chatId, String textToSend, ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(textToSend));
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(keyboardMarkup);

        return executeMessage(message);
    }


    private void deleteMessage(DeleteMessage message) {
        try {
            execute(message);
            log.info("Message was successfully deleted");
        } catch (TelegramApiException e) {
            log.error("Error occurred while attempting to delete message");
        }
    }

    private int executeMessage(SendMessage message) {
        try {
            Message response = execute(message);
            log.info("Message sent successfully to user with chatId = " + message.getChatId());
            return response.getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred while attempting to send a message to user with chatId = " + message.getChatId() + ": " + e.getMessage());
        }
        return -1;  // Произошла ошибка см. лог
    }

    private int executeMessage(EditMessageText message) {
        try {
            Message response = (Message) execute(message);
            log.info("Message sent successfully to user with chatId = " + message.getChatId());
            return response.getMessageId();
        } catch (TelegramApiException e) {
            log.error("Error occurred while attempting to send a message to user with chatId = " + message.getChatId() + ": " + e.getMessage());
        }
        return -1;
    }

    private EditMessageText editMessage(long chatId, int messageId, String textToSend) {
        EditMessageText message = new EditMessageText();

        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.setText(EmojiParser.parseToUnicode(textToSend));

        return message;
    }

    private EditMessageText editMessage(long chatId, int messageId, String textToSend, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageText message = new EditMessageText();

        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setParseMode(ParseMode.HTML);
        message.setText(EmojiParser.parseToUnicode(textToSend));
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    private InlineKeyboardButton createInlineButton(String name, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(EmojiParser.parseToUnicode(name));
        button.setCallbackData(callbackData);

        return button;
    }

    private InlineKeyboardMarkup mainMenuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Питомцы", CallbackConstants.PETS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Пожертвовать", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Волонтерство", CallbackConstants.VOLUNTEER))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Отзывы", CallbackConstants.FEEDBACK))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup twoButtonInlineMarkup(String buttonName1, String buttonName2, String callbackName1, String callbackName2) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        row.add(createInlineButton(buttonName1, callbackName1));
        row.add(createInlineButton(buttonName2, callbackName2));

        rowsInline.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }

    private void unusualMessageReceived(Update update) {
        String messageText = update.getMessage().getText();
        long chatId = update.getMessage().getChatId();

        if (messageText.contains("/pets_db_init")) {
            if (superUsers.contains(chatId)) {
                initPetDb(chatId);
            } else {
                sendMessage(chatId, "Вы не имеете доступа к этой команде");
                menuCommandReceived(chatId);
            }
        } else if (messageText.contains("/pet")) {
            if (superUsers.contains(chatId)) {
                if (petAddMode) {
                    initAddPet(chatId, messageText);
                } else {
                    sendMessage(chatId, "Для включения режима добавления нажмите <b>Добавить</b> в разделе <b>Питомцы</b>");
                    menuCommandReceived(chatId);
                }
            } else {
                sendMessage(chatId, "Вы не имеете доступа к этой команде");
                menuCommandReceived(chatId);
            }
        } else if (messageText.equals("Главное меню")) {
            if (userRepository.findById(chatId).get().getMenuMessageId() != 0) {
                sendMessage(chatId, "Переход в главное меню", new ReplyKeyboardRemove(true, null));
            }
            menuCommandReceived(chatId);
        } else {
            sendMessage(chatId, "Команда не распознана");
            menuCommandReceived(chatId);
        }

    }

    private void initAddPet(long chatId, String messageText) {
        Pet transientNewPet = parsePet(messageText);

        String textToSend = messageText + "\n" + petTemplateInsert(transientNewPet) +
                "\n\n<b>Проверьте заполнение полей</b>";

        EditMessageText message = editMessage(chatId,
                userRepository.findById(chatId).get().getMenuMessageId(),
                textToSend,
                twoButtonInlineMarkup(
                        "Подтвердить",
                        "Отмена",
                        CallbackConstants.PETS_ADD_CONFIRM,
                        CallbackConstants.PETS_ADD_CANCEL
                )
        );
        executeMessage(message);
    }

    private Pet parsePet(String messageText) {
        String petObject[] = messageText.split("/pet ")[1].split("; ");
        Pet transientNewPet = new Pet();

        //              0               1                 2                          3               4
        // "category": "Кошка", "name": "Искорка", "age": "6 месяцев", "sterilized": false, "about": "аккуратная, ласковая", "imageId": 7

        transientNewPet.setCategory(petObject[0]);
        transientNewPet.setName(petObject[1]);
        transientNewPet.setAge(petObject[2]);
        transientNewPet.setSterilized(Boolean.parseBoolean(petObject[3]));
        transientNewPet.setAbout(petObject[4]);
        transientNewPet.setImageId((long) petList.size());

        return transientNewPet;
    }

    private void initPetDb(long chatId) {
        //FIXME
        petRepository.deleteAll();
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            TypeFactory typeFactory = objectMapper.getTypeFactory();

            List<Pet> petList = objectMapper.readValue(new File("db/petsDb.json"), typeFactory.constructCollectionType(List.class, Pet.class));
            petRepository.saveAll(petList);
            sendMessage(chatId, "Инициализация БД питомцев прошла успешно");
            this.petList = reloadPetList();
            menuCommandReceived(chatId);
        } catch (Exception e) {
            log.error(e.getMessage());
            sendMessage(chatId, "Ошибка инициализации БД питомцев");
        }
    }
}
