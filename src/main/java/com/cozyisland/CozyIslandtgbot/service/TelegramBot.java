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
import java.util.Calendar;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import java.sql.Timestamp;

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
    private static final String DONATE_HOUSEKEEPING_TEXT = "- Дезинфицирующее средство \n" +
            "- Средства для мытья кафеля, полов, стен, столов\n" +
            "- Средства от ржавчины\n" +
            "- Жидкое мыло для рук\n" +
            "- Средства для мытья стеклянных поверхностей\n" +
            "- Средства для мытья посуды\n" +
            "- Тряпки для мытья полов, стен\n" +
            "- Губки для мытья мисок, посуды\n" +
            "- Полиэтиленовые мешки для мусора\n" +
            "- Перчатки х/б рабочие\n" +
            "- Перчатки резиновые плотные\n" +
            "- Веники\n" +
            "- Метлы пластиковые\n" +
            "- Совки (металлические с деревянной ручкой)\n" +
            "- Грабли\n" +
            "- Ведра\n" +
            "- Швабры\n" +
            "- Контейнеры для мусора\n" +
            "- Канцелярия (бумага для принтера, ручки, прозрачные файлы, бумага для записей, большие папки)\n" +
            "- Пледы, одеяла, покрывала, скатерти, ковролин, ковры";
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PetRepository petRepository;
    @Autowired
    private VolunteerApplicationRepository volunteerApplicationRepository;
    @Autowired
    private PetClaimApplicationRepository petClaimApplicationRepository;
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
            "<b>Номер в списке:</b> %d%n" +
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
            "<b>Статус заявки:</b> %s%n" +
            "<b>Дата визита:</b> %s%n%n" +
            "<i>%d/%d</i>";
    static final String PET_CLAIM_APPLICATION_TEMPLATE = "<b>Заявка на питомца</b>%n%n" +
            "<b>Категория:</b> %s%n" +
            "<b>Кличка:</b> %s%n" +
            "<b>Возраст:</b> %s%n" +
            "<b>Стерилизован(а):</b> %s%n%n" +

            "<b>ID пользователя:</b> %d%n" +
            "<b>Имя:</b> %s%n" +
            "<b>Телефон:</b> <code>%s</code>%n" +
            "<b>Имя пользователя:</b> @%s%n" +
            "<b>Дата и время подачи заявки:</b> %s%n%n" +

            "<b>Статус заявки:</b> %s%n" +
            "<b>Дата визита:</b> %s%n%n" +
            "<i>%d/%d</i>";

    static final String PET_CLAIM_APPLICATION_APPROVED_TEMPLATE = "<b>Заявка на питомца</b>%n%n" +
            "<b>Категория:</b> %s%n" +
            "<b>Кличка:</b> %s%n" +
            "<b>Возраст:</b> %s%n" +
            "<b>Стерилизован(а):</b> %s%n%n" +

            "<b>Дата и время подачи заявки:</b> %s%n%n" +

            "<b>Статус заявки:</b> %s%n%n" +
            "Ваша заявка на данного питомца одобрена%n" +
            "Можете подъезжать на адрес ул. Пушкина, дом 1%n" +
            "%s в рабочее время";
    static final String VOLUNTEER_APPLICATION_APPROVED_TEMPLATE = "<b>Заявка на волонтерство</b>%n%n" +
            "<b>Дата и время подачи заявки:</b> %s%n%n" +

            "<b>Статус заявки:</b> %s%n%n" +
            "Ваша заявка на волонтерство одобрена%n" +
            "Можете подъезжать на адрес ул. Пушкина, дом 1%n" +
            "%s в рабочее время";
    final BotConfig config;
    List<Long> superUsers;
    List<BotCommand> listOfCommands;
    List<Pet> petList;
    boolean petAddMode = false;
    List<VolunteerApplication> volunteerApplicationList;
    List<PetClaimApplication> petClaimApplicationList;


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

            if (callbackData.startsWith("PETS_CLAIM")) {
                switch (callbackData) {
                    case CallbackConstants.PETS_CLAIM -> {
                        claimPet(update);
                    }
                    case CallbackConstants.PETS_CLAIM_APPLICATIONS -> {
                        inlinePetClaimApplications(update);
                    }
                    case CallbackConstants.PETS_CLAIM_PREVIOUS -> {
                        previousPetClaimApplication(update);
                    }
                    case CallbackConstants.PETS_CLAIM_NEXT -> {
                        nextPetClaimApplication(update);
                    }
                    case CallbackConstants.PETS_CLAIM_DELETE -> {
                        deletePetClaimApplication(update);
                    }
                    case CallbackConstants.PETS_CLAIM_APPROVE -> {
                        approvePetClaimApplication(update);
                    }
                    /*case CallbackConstants.PETS_CLAIM_ADD -> {
                        addPetClaimApplication(update);
                    }
                    case CallbackConstants.PETS_ADD_CONFIRM -> {
                        saveNewPet(update);
                    }
                    case CallbackConstants.PETS_ADD_CANCEL -> {
                        addPet(update);
                    }*/
                }
            } else if (callbackData.startsWith("PETS")) {
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
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_APPROVE -> {
                        approveVolunteerApplication(update);
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
                case CallbackConstants.APPLICATIONS -> {
                    inlineApplications(update);
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

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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
        String textToSend = petTemplateInsert(chatId, defaultPet) +
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
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        Pet currentPet = petList.get(userRepository.findById(chatId).get().getCurrentListIndex());
        if (petRepository.findById(currentPet.getId()).isEmpty()) {
            log.error("Couldn't find pet in petList");
        } else {
            petRepository.deleteById(currentPet.getId());
            log.info("Successfully deleted from petList: " + currentPet.toString());
            if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) incrementUserCurrentListIndex(chatId, 1);
        }

        petList = reloadPetList();
        previousPet(update);
    }

    private void previousPet(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
            setUserCurrentListIndex(chatId, petList.size() - 1);
        } else {
            incrementUserCurrentListIndex(chatId, -1);
        }
        showPet(chatId, petList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private void nextPet(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userRepository.findById(chatId).get().getCurrentListIndex() == petList.size() - 1) {
            setUserCurrentListIndex(chatId, 0);
        } else {
            incrementUserCurrentListIndex(chatId, 1);
        }
        showPet(chatId, petList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private void inlinePets(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (petAddMode)
            petAddMode = false;     // если ожидался новый питомец, но был выход из команды - отмена режима редактирования

        petList = reloadPetList();
        setUserCurrentListIndex(chatId, 0);

        showPet(chatId, petList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private List<Pet> reloadPetList() {
        return new ArrayList<>(StreamSupport
                .stream(petRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }


    private void showPet(long chatId, List<Pet> petList, int currentPetIndex) {
        if (petList.isEmpty()) {
            String textToSend = "На данный момент в приюте нет ни одного питомца";
            EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, petsMenuInlineMarkup(chatId));
            executeMessage(message);
        } else {
            Pet currentPet = petList.get(currentPetIndex);
            String petInfo = petTemplateInsert(chatId, currentPet);
            EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), petInfo, petsMenuInlineMarkup(chatId));
            executeMessage(message);
        }
    }

    private String petTemplateInsert(long chatId, Pet currentPet) {
        return String.format(PET_DATA_TEMPLATE, userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                currentPet.getCategory(),
                currentPet.getName(),
                currentPet.getAge(),
                ((currentPet.isSterilized()) ? "да" : "нет"),
                currentPet.getAbout(),
                userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                petList.size()
        );
    }

    private InlineKeyboardMarkup petsMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (petList.size() > 1) {
            row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.PETS_PREVIOUS));
            row.add(createInlineButton(EmojiParser.parseToUnicode("Забрать"), CallbackConstants.PETS_CLAIM));
            row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.PETS_NEXT));
        }
        rowsInline.add(row);

        if (superUsers.contains(chatId)) {
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Добавить", CallbackConstants.PETS_ADD))));
            if (petList.size() > 1) {
                //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Редактировать", CallbackConstants.PETS_EDIT))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Удалить", CallbackConstants.PETS_DELETE))));
            }
        }

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void claimPet(Update update) {
        registerPetClaimApplication(update);
    }

    private void inlinePetClaimApplications(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        petClaimApplicationList = reloadPetClaimApplicationList();
        setUserCurrentListIndex(chatId, 0);

        showPetClaimApplication(chatId, petClaimApplicationList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private InlineKeyboardMarkup petClaimApplicationMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (!petClaimApplicationList.isEmpty()) {
            if (petClaimApplicationList.size() > 1) row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.PETS_CLAIM_PREVIOUS));
            row.add(createInlineButton(EmojiParser.parseToUnicode("Одобрить"), CallbackConstants.PETS_CLAIM_APPROVE));
            if (petClaimApplicationList.size() > 1) row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.PETS_CLAIM_NEXT));
        }
        rowsInline.add(row);

        if (superUsers.contains(chatId)) {
            //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.PETS_CLAIM_ADD))));
            if (petClaimApplicationList.size() > 1) {
                //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Редактировать", CallbackConstants.PETS_CLAIM_EDIT))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.PETS_CLAIM_DELETE))));
            }
        }

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.APPLICATIONS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void deletePetClaimApplication(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        PetClaimApplication currentApplication = petClaimApplicationList.get(userRepository.findById(chatId).get().getCurrentListIndex());
        if (petClaimApplicationRepository.findById(currentApplication.getPk()).isEmpty()) {
            log.error("Couldn't find application in volunteerApplicationList");
        } else {
            petClaimApplicationRepository.deleteById(currentApplication.getPk());
            log.info("Successfully deleted from petClaimApplicationRepository: " + currentApplication);
            if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) incrementUserCurrentListIndex(chatId, 1);
        }

        petClaimApplicationList = reloadPetClaimApplicationList();
        previousPetClaimApplication(update);
    }

    private void previousPetClaimApplication(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
            setUserCurrentListIndex(chatId, petClaimApplicationList.size() - 1);
        } else {
            incrementUserCurrentListIndex(chatId, -1);
        }
        showPetClaimApplication(chatId, petClaimApplicationList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private void nextPetClaimApplication(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userRepository.findById(chatId).get().getCurrentListIndex() == petClaimApplicationList.size() - 1) {
            setUserCurrentListIndex(chatId, 0);
        } else {
            incrementUserCurrentListIndex(chatId, 1);
        }
        showPetClaimApplication(chatId, petClaimApplicationList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private List<PetClaimApplication> reloadPetClaimApplicationList() {
        return new ArrayList<>(StreamSupport
                .stream(petClaimApplicationRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }

    private void showPetClaimApplication(long chatId, List<PetClaimApplication> petClaimApplicationList, int currentPetClaimApplicationIndex) {
        if (petClaimApplicationList.isEmpty()) {
            String textToSend = "На данный момент нет ни одной заявки на питомца";
            EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, petClaimApplicationMenuInlineMarkup(chatId));
            executeMessage(message);
        } else {
            PetClaimApplication currentApplication = petClaimApplicationList.get(currentPetClaimApplicationIndex);
            String petClaimApplicationInfo = petClaimApplicationTemplateInsert(chatId, currentApplication);
            EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), petClaimApplicationInfo, petClaimApplicationMenuInlineMarkup(chatId));
            executeMessage(message);
        }
    }

    private String approvedPetClaimApplicationTemplateInsert(PetClaimApplication currentApplication) {
        Pet pet = petRepository.findById(currentApplication.getPk().getId()).get();
        return String.format(PET_CLAIM_APPLICATION_APPROVED_TEMPLATE,
                pet.getCategory(),
                pet.getName(),
                pet.getAge(),
                ((pet.isSterilized()) ? "да" : "нет"),

                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),

                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "<i>ошибка</i>" : currentApplication.getVisitDate().toString().substring(0, 10)
        );
    }

    private String approvedVolunteerApplicationTemplateInsert(VolunteerApplication currentApplication) {
        return String.format(VOLUNTEER_APPLICATION_APPROVED_TEMPLATE,
                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),

                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "<i>ошибка</i>" : currentApplication.getVisitDate().toString().substring(0, 10)
        );
    }

    private String petClaimApplicationTemplateInsert(long chatId, PetClaimApplication currentApplication) {
        Pet pet = petRepository.findById(currentApplication.getPk().getId()).get();
        return String.format(PET_CLAIM_APPLICATION_TEMPLATE,
                pet.getCategory(),
                pet.getName(),
                pet.getAge(),
                ((pet.isSterilized()) ? "да" : "нет"),

                currentApplication.getPk().getChatId(),
                currentApplication.getFirstname(),
                (currentApplication.getPhoneNumber() == null) ? "не указан" : currentApplication.getPhoneNumber(),
                currentApplication.getUserName(),
                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),

                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "не назначена" : currentApplication.getVisitDate().toString().substring(0, 10),
                userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                petClaimApplicationList.size()
        );
    }
    private void registerPetClaimApplication(Update update) {
        //Contact contact = update.getMessage().getContact();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        Pet chosenPet = petList.get(userRepository.findById(chatId).get().getCurrentListIndex());
        PetClaimApplicationPK petClaimApplicationPK = new PetClaimApplicationPK(chosenPet.getId(), chatId);
        String textToSend;

        if (petClaimApplicationRepository.findById(petClaimApplicationPK).isEmpty()) {
            PetClaimApplication petClaimApplication = PetClaimApplication.builder()
                    .pk(petClaimApplicationPK)
                    .firstname(update.getCallbackQuery().getMessage().getChat().getFirstName())
                    .userName(update.getCallbackQuery().getMessage().getChat().getUserName())
                    .status("на рассмотрении")
                    .build();

            petClaimApplicationRepository.save(petClaimApplication);

            petClaimApplicationList = reloadPetClaimApplicationList();
            adminNotification("PET_CLAIM");
            log.info("Pet claim application saved to repository: " + petClaimApplication);

            textToSend = "Ваша заявка на питомца по кличке " + chosenPet.getName() + " передана менеджеру\n" +
                    "В ближайший рабочий день мы обработаем Вашу заявку и пришлем дополнительную информацию\n" +
                    "Ожидайте, пожалуйста";
        } else {
            textToSend = "Ваша предыдущая заявка на питомца по кличке " + chosenPet.getName() + " уже находится в обработке\n" +
                    "Ожидайте, пожалуйста";
        }

        EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, petClaimApplicationInlineMarkup());
        executeMessage(message);
    }

    private void approvePetClaimApplication(Update update) {
        //Contact contact = update.getMessage().getContact();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        PetClaimApplication chosenApplication = petClaimApplicationList.get(userRepository.findById(chatId).get().getCurrentListIndex());
        PetClaimApplicationPK petClaimApplicationPK = chosenApplication.getPk();

        if (petClaimApplicationRepository.findById(petClaimApplicationPK).isPresent()) {
            if (petClaimApplicationRepository.findById(petClaimApplicationPK).get().getStatus().equals("на рассмотрении")) {
                chosenApplication.setStatus("одобрена");

                Calendar cal = Calendar.getInstance();
                cal.setTime(chosenApplication.getAppliedAt());
                cal.add(Calendar.DAY_OF_WEEK, 2);
                chosenApplication.setVisitDate(new Timestamp(cal.getTime().getTime()));

                petClaimApplicationRepository.save(chosenApplication);

                petClaimApplicationList = reloadPetClaimApplicationList();
                userNotificationPetClaim(petClaimApplicationPK.getChatId(), "PET_CLAIM", chosenApplication);
                log.info("Pet claim application updated to approved status in repository: " + chosenApplication);

                setUserCurrentListIndex(chatId, petClaimApplicationList.size() - 1);
                showPetClaimApplication(chatId, petClaimApplicationList, petClaimApplicationList.size() - 1);
            }
        } else {
            log.error("Couldn't find pet claim application in repository: " + chosenApplication);
        }
    }

    private void approveVolunteerApplication(Update update) {
        //Contact contact = update.getMessage().getContact();
        long chatId = update.getCallbackQuery().getMessage().getChatId();

        VolunteerApplication chosenApplication = volunteerApplicationList.get(userRepository.findById(chatId).get().getCurrentListIndex());

        if (volunteerApplicationRepository.findById(chatId).isPresent()) {
            if (volunteerApplicationRepository.findById(chatId).get().getStatus().equals("на рассмотрении")) {
                chosenApplication.setStatus("одобрена");

                Calendar cal = Calendar.getInstance();
                cal.setTime(chosenApplication.getAppliedAt());
                cal.add(Calendar.DAY_OF_WEEK, 1);
                chosenApplication.setVisitDate(new Timestamp(cal.getTime().getTime()));

                volunteerApplicationRepository.save(chosenApplication);

                volunteerApplicationList = reloadVolunteerApplicationList();
                userNotificationVolunteer(chatId, "VOLUNTEER", chosenApplication);
                log.info("Volunteer application updated to approved status in repository: " + chosenApplication);

                setUserCurrentListIndex(chatId, volunteerApplicationList.size() - 1);
                showVolunteerApplication(chatId, volunteerApplicationList, volunteerApplicationList.size() - 1);
            }
        } else {
            log.error("Couldn't find pet claim application in repository: " + chosenApplication);
        }
    }

    private InlineKeyboardMarkup petClaimApplicationInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.PETS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineVolunteerApplications(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        volunteerApplicationList = reloadVolunteerApplicationList();
        setUserCurrentListIndex(chatId, 0);

        showVolunteerApplication(chatId, volunteerApplicationList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private InlineKeyboardMarkup volunteerApplicationMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        if (!volunteerApplicationList.isEmpty()) {
            if (volunteerApplicationList.size() > 1) row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.VOLUNTEER_APPLICATIONS_PREVIOUS));
            row.add(createInlineButton(EmojiParser.parseToUnicode("Одобрить"), CallbackConstants.VOLUNTEER_APPLICATIONS_APPROVE));
            if (volunteerApplicationList.size() > 1) row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.VOLUNTEER_APPLICATIONS_NEXT));
        }
        rowsInline.add(row);

        if (superUsers.contains(chatId)) {
            //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.VOLUNTEER_APPLICATIONS_ADD))));
            if (!volunteerApplicationList.isEmpty()) {
                //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Редактировать", CallbackConstants.VOLUNTEER_APPLICATIONS_EDIT))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.VOLUNTEER_APPLICATIONS_DELETE))));
            }
        }

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.APPLICATIONS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        VolunteerApplication currentApplication = volunteerApplicationList.get(userRepository.findById(chatId).get().getCurrentListIndex());
        if (volunteerApplicationRepository.findById(currentApplication.getChatId()).isEmpty()) {
            log.error("Couldn't find application in volunteerApplicationList");
        } else {
            volunteerApplicationRepository.deleteById(currentApplication.getChatId());
            log.info("Successfully deleted from volunteerApplicationList: " + currentApplication);
            if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) incrementUserCurrentListIndex(chatId, 1);
        }

        volunteerApplicationList = reloadVolunteerApplicationList();
        previousVolunteerApplication(update);
    }

    private void previousVolunteerApplication(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
            setUserCurrentListIndex(chatId, volunteerApplicationList.size() - 1);
        } else {
            incrementUserCurrentListIndex(chatId, -1);
        }
        showVolunteerApplication(chatId, volunteerApplicationList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private void nextVolunteerApplication(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (userRepository.findById(chatId).get().getCurrentListIndex() == volunteerApplicationList.size() - 1) {
            setUserCurrentListIndex(chatId, 0);
        } else {
            incrementUserCurrentListIndex(chatId, 1);
        }
        showVolunteerApplication(chatId, volunteerApplicationList, userRepository.findById(chatId).get().getCurrentListIndex());
    }

    private List<VolunteerApplication> reloadVolunteerApplicationList() {
        return new ArrayList<>(StreamSupport
                .stream(volunteerApplicationRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }

    private void showVolunteerApplication(long chatId, List<VolunteerApplication> volunteerApplicationList, int currentVolunteerApplicationIndex) {
        if (volunteerApplicationList.isEmpty()) {
            String textToSend = "На данный момент нет ни одной заявки на волонтерство";
            EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, volunteerApplicationMenuInlineMarkup(chatId));
            executeMessage(message);
        } else {
            VolunteerApplication currentApplication = volunteerApplicationList.get(currentVolunteerApplicationIndex);
            String volunteerApplicationInfo = volunteerApplicationTemplateInsert(chatId, currentApplication);
            EditMessageText message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), volunteerApplicationInfo, volunteerApplicationMenuInlineMarkup(chatId));
            executeMessage(message);
        }
    }

    private String volunteerApplicationTemplateInsert(long chatId, VolunteerApplication currentApplication) {
        return String.format(VOLUNTEER_APPLICATION_TEMPLATE,
                currentApplication.getChatId(),
                currentApplication.getFirstname(),
                currentApplication.getPhoneNumber(),
                currentApplication.getUserName(),
                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),
                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "не назначена" : currentApplication.getVisitDate().toString().substring(0, 10),
                userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                volunteerApplicationList.size()
        );
    }

    private InlineKeyboardMarkup volunteerMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Продолжить", CallbackConstants.VOLUNTEER_SEND_CONTACT))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineVolunteerSendContact(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
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
        EditMessageText message = editMessage(chatId, messageId, "<b>Главное меню</b> нашего приюта", mainMenuInlineMarkup(chatId));
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

            volunteerApplicationList = reloadVolunteerApplicationList();
            setUserCurrentListIndex(chatId, volunteerApplicationList.size() - 1);
            adminNotification("VOLUNTEER");
            log.info("Volunteer's application saved to repository: " + application);

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

    private void setUserCurrentListIndex(long chatId, int index) {
        if (userRepository.findById(chatId).isPresent()) {
            User user = userRepository.findById(chatId).get();
            user.setCurrentListIndex(index);
            userRepository.save(user);
        } else {
            log.error("Couldn't find user with chatId = " + chatId + " in db");
        }
    }

    private void incrementUserCurrentListIndex(long chatId, int inc) {
        if (userRepository.findById(chatId).isPresent()) {
            User user = userRepository.findById(chatId).get();
            int currentIndex = user.getCurrentListIndex();
            user.setCurrentListIndex(currentIndex + inc);
            userRepository.save(user);
        } else {
            log.error("Couldn't find user with chatId = " + chatId + " in db");
        }
    }

    private void adminNotification(String mode) {
        switch (mode) {
            case "VOLUNTEER" -> {
                String firstText = ":bellhop_bell:<b>Новая заявка на волонтерство</b>:bellhop_bell:";
                for (long adminId : superUsers) {
                    sendMessage(adminId, firstText);
                    menuCommandReceived(adminId);
                    setUserCurrentListIndex(adminId, volunteerApplicationList.size() - 1);
                    showVolunteerApplication(adminId, volunteerApplicationList, volunteerApplicationList.size() - 1);
                    log.info("Notified admin with chatId = " + adminId + " about new volunteer application");
                }
            }
            case "PET_CLAIM" -> {
                String firstText = ":bellhop_bell:<b>Новая заявка на питомца</b>:bellhop_bell:";
                for (long adminId : superUsers) {
                    sendMessage(adminId, firstText);
                    menuCommandReceived(adminId);
                    setUserCurrentListIndex(adminId, petClaimApplicationList.size() - 1);
                    showPetClaimApplication(adminId, petClaimApplicationList, petClaimApplicationList.size() - 1);
                    log.info("Notified admin with chatId = " + adminId + " about new volunteer application");
                }
            }
            default -> {

            }
        }
    }

    //FIXME рефакторинг (унаследоваться от заявки)
    private void userNotificationPetClaim(long chatId, String mode, PetClaimApplication application) {
        switch (mode) {
            /*case "VOLUNTEER" -> {
                String firstText = ":bellhop_bell:<b>Одобрена заявка на волонтерство</b>:bellhop_bell:";
                String applicationText = approvedPetClaimApplicationTemplateInsert(application);
                sendMessage(application.getPk().getChatId(), firstText);
                sendMessage(application.getPk().getChatId(), applicationText);
                menuCommandReceived(application.getPk().getChatId());
                log.info("Notified user with chatId = " + chatId + " about approval on volunteer application");

            }*/
            case "PET_CLAIM" -> {
                String firstText = ":bellhop_bell:<b>Одобрена заявка на питомца</b>:bellhop_bell:";
                String applicationText = approvedPetClaimApplicationTemplateInsert(application);
                sendMessage(application.getPk().getChatId(), firstText);
                sendMessage(application.getPk().getChatId(), applicationText);
                menuCommandReceived(application.getPk().getChatId());
                log.info("Notified user with chatId = " + chatId + " about approval on pet claim application");
            }
        }
    }
    private void userNotificationVolunteer(long chatId, String mode, VolunteerApplication application) {
        switch (mode) {
            case "VOLUNTEER" -> {
                String firstText = ":bellhop_bell:<b>Одобрена заявка на волонтерство</b>:bellhop_bell:";
                String applicationText = approvedVolunteerApplicationTemplateInsert(application);
                sendMessage(application.getChatId(), firstText);
                sendMessage(application.getChatId(), applicationText);
                menuCommandReceived(application.getChatId());
                log.info("Notified user with chatId = " + chatId + " about approval on volunteer application");

            }
            /*case "PET_CLAIM" -> {
                String firstText = ":bellhop_bell:<b>Одобрена заявка на питомца</b>:bellhop_bell:";
                String applicationText = approvedPetClaimApplicationTemplateInsert(application);
                sendMessage(chatId, firstText);
                sendMessage(chatId, applicationText);
                menuCommandReceived(chatId);
                log.info("Notified user with chatId = " + chatId + " about approval on pet claim application");
            }*/
        }
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
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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

        //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.BACK))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

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
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineApplications(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел заявок</b>";

        EditMessageText message = editMessage(chatId, messageId, textToSend, applicationsMenuInlineMarkup());

        executeMessage(message);
    }
    private InlineKeyboardMarkup applicationsMenuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Питомцы:dog:", CallbackConstants.PETS_CLAIM_APPLICATIONS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Волонтеры:handshake:", CallbackConstants.VOLUNTEER_APPLICATIONS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getChat().getFirstName();

        if (userRepository.findById(chatId).isEmpty()) {
            User user = User.builder()
                    .chatId(chatId)
                    .currentListIndex(0)
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

        currentUser.setMenuMessageId(sendMessage(chatId, "<b>Главное меню</b> нашего приюта", mainMenuInlineMarkup(chatId)));
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

    private InlineKeyboardMarkup mainMenuInlineMarkup(long chatId) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Питомцы", CallbackConstants.PETS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Пожертвовать", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Волонтерство", CallbackConstants.VOLUNTEER))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Отзывы", CallbackConstants.FEEDBACK))));
        if (superUsers.contains(chatId)) {
            rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Заявки", CallbackConstants.APPLICATIONS))));
        }
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

        String textToSend = messageText + "\n" + petTemplateInsert(chatId, transientNewPet) +
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
        String[] petObject = messageText.split("/pet ")[1].split("; ");
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
