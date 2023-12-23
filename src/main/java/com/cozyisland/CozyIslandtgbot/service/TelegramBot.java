package com.cozyisland.CozyIslandtgbot.service;

import com.cozyisland.CozyIslandtgbot.config.BotConfig;
import com.cozyisland.CozyIslandtgbot.enums.*;
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
    private FeedbackRepository feedbackRepository;
    @Autowired
    private PetImageRepository petImageRepository;
    @Autowired
    private BinaryContentRepository binaryContentRepository;
    static final String START_TEXT = EmojiParser.parseToUnicode("Привет, %s!\n" +
            "Добро пожаловать на \n" +
            ":feet:<b>Островок тепла</b>:feet:!");

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
    static final String VOLUNTEER_APPLICATION_TEMPLATE = "<b>Заявка на волонтерство</b>%n%n" +
            "<b>Имя:</b> %s%n" +
            "<b>Телефон:</b> <code>%s</code>%n" +
            "<b>Имя пользователя:</b> @%s%n" +
            "<b>Дата и время подачи заявки:</b> %s%n" +
            "<b>Статус заявки:</b> %s%n" +
            "<b>Дата визита:</b> %s%n%n" +
            "<i>%d/%d</i>";
    static final String PET_CLAIM_APPLICATION_TEMPLATE = "<b>Заявка на просмотр питомца</b>%n%n" +
            "<b>Категория:</b> %s%n" +
            "<b>Кличка:</b> %s%n" +
            "<b>Возраст:</b> %s%n" +
            "<b>Стерилизован(а):</b> %s%n%n" +

            "<b>Имя:</b> %s%n" +
            "<b>Телефон:</b> <code>%s</code>%n" +
            "<b>Имя пользователя:</b> @%s%n" +
            "<b>Дата и время подачи заявки:</b> %s%n%n" +

            "<b>Статус заявки:</b> %s%n" +
            "<b>Дата визита:</b> %s%n%n" +
            "<i>%d/%d</i>";

    static final String PET_CLAIM_APPLICATION_APPROVED_TEMPLATE = "<b>Заявка на просмотр питомца</b>%n%n" +
            "<b>Категория:</b> %s%n" +
            "<b>Кличка:</b> %s%n" +
            "<b>Возраст:</b> %s%n" +
            "<b>Стерилизован(а):</b> %s%n%n" +

            "<b>Дата и время подачи заявки:</b> %s%n%n" +

            "<b>Статус заявки:</b> %s%n%n" +
            "Ваша заявка на просмотр данного питомца одобрена%n" +
            "Можете подъезжать на адрес ул. Пушкина, дом 1%n" +
            "%s в рабочее время в назначенную дату";
    static final String VOLUNTEER_APPLICATION_APPROVED_TEMPLATE = "<b>Заявка на волонтерство</b>%n%n" +
            "<b>Дата и время подачи заявки:</b> %s%n%n" +

            "<b>Статус заявки:</b> %s%n%n" +
            "Ваша заявка на волонтерство одобрена%n" +
            "Можете подъезжать на адрес ул. Пушкина, дом 1%n" +
            "%s в рабочее время в назначенную дату";
    static final String FEEDBACK_USER_TEMPLATE = "<b>Отзыв пользователя</b> %s%n%n" +
            "<b>Оценка:</b> %d:star:%n" +
            "<b>Отзыв:</b> %s%n" +
            "<i>Отзыв от %s</i>%n%n" +
            "<i>%d/%d</i>";
    static final String FEEDBACK_ADMIN_TEMPLATE = "<b>Отзыв пользователя</b> %s%n%n" +
            "<b>ID пользователя:</b> %d%n" +
            "<b>Оценка:</b> %d:star:%n" +
            "<b>Отзыв:</b> %s%n" +
            "<i>Отзыв от %s</i>%n" +
            "<b>Статус заявки:</b> %s%n%n" +
            "<i>%d/%d</i>";
    final BotConfig config;
    List<Long> superUsers;
    List<BotCommand> listOfCommands;
    List<Pet> petList;
    List<VolunteerApplication> volunteerApplicationList;
    List<PetClaimApplication> petClaimApplicationList;
    List<Feedback> feedbackList;
    boolean petAddMode = false;


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
                        previousItem(update, ItemType.PET_CLAIM_APPLICATION);
                    }
                    case CallbackConstants.PETS_CLAIM_NEXT -> {
                        nextItem(update, ItemType.PET_CLAIM_APPLICATION);
                    }
                    case CallbackConstants.PETS_CLAIM_DELETE -> {
                        deleteItem(update, ItemType.PET_CLAIM_APPLICATION);
                    }
                    case CallbackConstants.PETS_CLAIM_APPROVE -> {
                        approveItem(update, ItemType.PET_CLAIM_APPLICATION);
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
                        previousItem(update, ItemType.PET);
                    }
                    case CallbackConstants.PETS_NEXT -> {
                        nextItem(update, ItemType.PET);
                    }
                    case CallbackConstants.PETS_DELETE -> {
                        deleteItem(update, ItemType.PET);
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
                        inlineFeedbackShow(update);
                    }
                    case CallbackConstants.FEEDBACK_SHOW_MY -> {
                        // FIXME список моих отзывов
                        // inlineMyFeedback(update);
                    }
                    case CallbackConstants.FEEDBACK_NEW -> {
                        contactRequestWarning(update, ItemType.FEEDBACK);
                    }
                    default -> {
                        if (callbackData.startsWith("FEEDBACK_NEW_RATE")) {
                            rateFeedback(update);
                        }
                    }
                }
            } else if (callbackData.startsWith("VOLUNTEER")) {
                switch (callbackData) {
                    case CallbackConstants.VOLUNTEER -> {
                        inlineVolunteer(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLY -> {
                        applyVolunteer(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS -> {
                        inlineVolunteerApplications(update);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_PREVIOUS -> {
                        previousItem(update, ItemType.VOLUNTEER_APPLICATION);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_NEXT -> {
                        nextItem(update, ItemType.VOLUNTEER_APPLICATION);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_DELETE -> {
                        deleteItem(update, ItemType.VOLUNTEER_APPLICATION);
                    }
                    case CallbackConstants.VOLUNTEER_APPLICATIONS_APPROVE -> {
                        approveItem(update, ItemType.VOLUNTEER_APPLICATION);
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
                case CallbackConstants.PROCEED -> {
                    inlineProceed(update);
                }
                case CallbackConstants.RETURN_TO_MENU -> {
                    inlineReturnToMenu(update);
                }
                case CallbackConstants.APPLICATIONS -> {
                    inlineApplications(update);
                }
            }
        }
    }

    private void rateFeedback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String callbackData = update.getCallbackQuery().getData();
        int rate = Integer.parseInt(callbackData.substring(callbackData.length() - 2, callbackData.length() - 1));

        registerItemApplication(update, ItemType.FEEDBACK);
    }

    private void inlineFeedbackShow(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел отзывов :speaking_head_in_silhouette:</b>";

        EditMessageText message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_SHOW));

        executeMessage(message);
    }

    private void inlineVolunteer(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String textToSend = "<b>Раздел волонтерства</b>\n" +
                "В нашем приюте волонтёры занимаются уходом за питомцами, а также помогают содержать приют в чистоте.\n" +
                "В обязанности волонтёра может входить:\n" +
                "- кормление животных\n" +
                "- уход за больными животными\n" +
                "- уборка помещений приюта\n" +
                "- ведение журнала о состоянии каждого питомца и т.д.\n\n" +
                "Вы можете помочь нам, став нашим волонтером. Для этого оставьте заявку";

        EditMessageText message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.VOLUNTEER_MENU));
        executeMessage(message);
    }

    private void inlineProceed(Update update) {
        sendContactRequest(update);
    }


    private void donateMoney(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();

        String textToSend = DONATE_MONEY_TEXT;

        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        //fixme
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
        //fixme
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
        //fixme
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
        //fixme
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
        String textToSend = itemTemplateInsert(chatId, defaultPet) +
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

    // TODO: удалить отзыв
    private void deleteItem(Update update, ItemType type) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        switch (type) {
            case PET -> {
                Pet currentPet = petList.get(userRepository.findById(chatId).get().getCurrentListIndex());
                if (petRepository.findById(currentPet.getId()).isEmpty()) {
                    log.error("Couldn't find pet in petList");
                } else {
                    petRepository.deleteById(currentPet.getId());
                    log.info("Successfully deleted from petList: " + currentPet.toString());
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0)
                        incrementUserCurrentListIndex(chatId, 1);
                }

                petList = reloadPetList();
                previousItem(update, ItemType.PET);
            }
            case PET_CLAIM_APPLICATION -> {
                PetClaimApplication currentApplication = petClaimApplicationList.get(userRepository.findById(chatId).get().getCurrentListIndex());
                if (petClaimApplicationRepository.findById(currentApplication.getPk()).isEmpty()) {
                    log.error("Couldn't find application in volunteerApplicationList");
                } else {
                    petClaimApplicationRepository.deleteById(currentApplication.getPk());
                    DeleteMessage message = DeleteMessage.builder()
                            .chatId(currentApplication.getPk().getChatId())
                            .messageId(currentApplication.getNotificationMessageId())
                            .build();
                    deleteMessage(message);
                    log.info("Successfully deleted from petClaimApplicationRepository: " + currentApplication);
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0)
                        incrementUserCurrentListIndex(chatId, 1);
                }

                petClaimApplicationList = reloadPetClaimApplicationList(chatId);
                previousItem(update, ItemType.PET_CLAIM_APPLICATION);
            }
            case VOLUNTEER_APPLICATION -> {
                VolunteerApplication currentApplication = volunteerApplicationList.get(userRepository.findById(chatId).get().getCurrentListIndex());
                if (volunteerApplicationRepository.findById(currentApplication.getChatId()).isEmpty()) {
                    log.error("Couldn't find application in volunteerApplicationList");
                } else {
                    volunteerApplicationRepository.deleteById(currentApplication.getChatId());
                    DeleteMessage message = DeleteMessage.builder()
                            .chatId(currentApplication.getChatId())
                            .messageId(currentApplication.getNotificationMessageId())
                            .build();
                    deleteMessage(message);
                    log.info("Successfully deleted from volunteerApplicationList: " + currentApplication);
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0)
                        incrementUserCurrentListIndex(chatId, 1);
                }

                volunteerApplicationList = reloadVolunteerApplicationList(chatId);
                previousItem(update, ItemType.VOLUNTEER_APPLICATION);
            }
        }
    }

    private void previousItem(Update update, ItemType type) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        switch (type) {
            case PET -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                    setUserCurrentListIndex(chatId, petList.size() - 1);
                } else {
                    incrementUserCurrentListIndex(chatId, -1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.PET);
            }
            case PET_CLAIM_APPLICATION -> {
                if (superUsers.contains(chatId)) {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                        setUserCurrentListIndex(chatId, petClaimApplicationList.size() - 1);
                    } else {
                        incrementUserCurrentListIndex(chatId, -1);
                    }
                } else {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                        setUserCurrentListIndex(chatId, reloadPetClaimApplicationList(chatId).size() - 1);
                    } else {
                        incrementUserCurrentListIndex(chatId, -1);
                    }
                }

                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.PET_CLAIM_APPLICATION);
            }
            case VOLUNTEER_APPLICATION -> {
                if (superUsers.contains(chatId)) {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                        setUserCurrentListIndex(chatId, volunteerApplicationList.size() - 1);
                    } else {
                        incrementUserCurrentListIndex(chatId, -1);
                    }
                } else {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                        setUserCurrentListIndex(chatId, reloadVolunteerApplicationList(chatId).size() - 1);
                    } else {
                        incrementUserCurrentListIndex(chatId, -1);
                    }
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.VOLUNTEER_APPLICATION);
            }
            case FEEDBACK_ALL -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                    setUserCurrentListIndex(chatId, reloadFeedbackList(chatId, FeedbackType.ALL).size() - 1);
                } else {
                    incrementUserCurrentListIndex(chatId, -1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.FEEDBACK_ALL);
            }
            case FEEDBACK_MY -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                    setUserCurrentListIndex(chatId, reloadFeedbackList(chatId, FeedbackType.MY).size() - 1);
                } else {
                    incrementUserCurrentListIndex(chatId, -1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.FEEDBACK_MY);
            }
            case FEEDBACK_TO_APPROVE -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == 0) {
                    setUserCurrentListIndex(chatId, reloadFeedbackList(chatId, FeedbackType.TO_APPROVE).size() - 1);
                } else {
                    incrementUserCurrentListIndex(chatId, -1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.FEEDBACK_TO_APPROVE);
            }
        }
    }

    private void nextItem(Update update, ItemType type) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        switch (type) {
            case PET -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == petList.size() - 1) {
                    setUserCurrentListIndex(chatId, 0);
                } else {
                    incrementUserCurrentListIndex(chatId, 1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.PET);
            }
            case PET_CLAIM_APPLICATION -> {
                if (superUsers.contains(chatId)) {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == petClaimApplicationList.size() - 1) {
                        setUserCurrentListIndex(chatId, 0);
                    } else {
                        incrementUserCurrentListIndex(chatId, 1);
                    }
                } else {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == reloadPetClaimApplicationList(chatId).size() - 1) {
                        setUserCurrentListIndex(chatId, 0);
                    } else {
                        incrementUserCurrentListIndex(chatId, 1);
                    }
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.PET_CLAIM_APPLICATION);
            }
            case VOLUNTEER_APPLICATION -> {
                if (superUsers.contains(chatId)) {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == volunteerApplicationList.size() - 1) {
                        setUserCurrentListIndex(chatId, 0);
                    } else {
                        incrementUserCurrentListIndex(chatId, 1);
                    }
                } else {
                    if (userRepository.findById(chatId).get().getCurrentListIndex() == reloadVolunteerApplicationList(chatId).size() - 1) {
                        setUserCurrentListIndex(chatId, 0);
                    } else {
                        incrementUserCurrentListIndex(chatId, 1);
                    }
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.VOLUNTEER_APPLICATION);
            }
            case FEEDBACK_ALL -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == reloadFeedbackList(chatId, FeedbackType.ALL).size() - 1) {
                    setUserCurrentListIndex(chatId, 0);
                } else {
                    incrementUserCurrentListIndex(chatId, 1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.FEEDBACK_ALL);
            }
            case FEEDBACK_MY -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == reloadFeedbackList(chatId, FeedbackType.MY).size() - 1) {
                    setUserCurrentListIndex(chatId, 0);
                } else {
                    incrementUserCurrentListIndex(chatId, 1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.FEEDBACK_MY);
            }
            case FEEDBACK_TO_APPROVE -> {
                if (userRepository.findById(chatId).get().getCurrentListIndex() == reloadFeedbackList(chatId, FeedbackType.TO_APPROVE).size() - 1) {
                    setUserCurrentListIndex(chatId, 0);
                } else {
                    incrementUserCurrentListIndex(chatId, 1);
                }
                showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.FEEDBACK_TO_APPROVE);
            }
        }
    }

    private void inlinePets(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        if (petAddMode)
            petAddMode = false;     // если ожидался новый питомец, но был выход из команды - отмена режима редактирования

        petList = reloadPetList();
        setUserCurrentListIndex(chatId, 0);

        showItem(chatId, userRepository.findById(chatId).get().getCurrentListIndex(), ItemType.PET);
    }

    private List<Pet> reloadPetList() {
        return new ArrayList<>(StreamSupport
                .stream(petRepository.findAll().spliterator(), false)
                .collect(Collectors.toList()));
    }

    private void showItem(long chatId, int currentIndex, ItemType itemType) {
        EditMessageText message = null;
        switch (itemType) {
            case PET -> {
                if (this.petList.isEmpty()) {
                    String textToSend = "На данный момент в приюте нет ни одного питомца";
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.PETS_MENU));
                } else {
                    Pet currentPet = this.petList.get(currentIndex);
                    String petInfo = itemTemplateInsert(chatId, currentPet);
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), petInfo, customInlineMarkup(chatId, InlineMarkupType.PETS_MENU));
                }
            }
            case VOLUNTEER_APPLICATION -> {
                if (reloadVolunteerApplicationList(chatId).isEmpty()) {
                    String textToSend = "На данный момент нет ни одной заявки на волонтерство";
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.VOLUNTEER_APPLICATION_MENU));
                } else {
                    VolunteerApplication currentApplication;
                    if (superUsers.contains(chatId)) {
                        currentApplication = this.volunteerApplicationList.get(currentIndex);
                    } else {
                        currentApplication = reloadVolunteerApplicationList(chatId).get(currentIndex);
                    }
                    String volunteerApplicationInfo = itemTemplateInsert(chatId, currentApplication);
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), volunteerApplicationInfo, customInlineMarkup(chatId, InlineMarkupType.VOLUNTEER_APPLICATION_MENU));
                }
            }
            case PET_CLAIM_APPLICATION -> {
                if (reloadPetClaimApplicationList(chatId).isEmpty()) {
                    String textToSend = "На данный момент нет ни одной заявки на питомца";
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.PET_CLAIM_APPLICATION_MENU));
                } else {
                    PetClaimApplication currentApplication;
                    if (superUsers.contains(chatId)) {
                        currentApplication = this.petClaimApplicationList.get(currentIndex);
                    } else {
                        currentApplication = reloadPetClaimApplicationList(chatId).get(currentIndex);
                    }
                    String petClaimApplicationInfo = itemTemplateInsert(chatId, currentApplication);
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), petClaimApplicationInfo, customInlineMarkup(chatId, InlineMarkupType.PET_CLAIM_APPLICATION_MENU));
                }
            }
            case FEEDBACK_ALL -> {
                if (reloadFeedbackList(chatId, FeedbackType.ALL).isEmpty()) {
                    String textToSend = "На данный момент у нашего приюта нет ни одного отзыва";
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_LIST_MENU));
                    executeMessage(message);
                } else {
                    Feedback currentFeedback;
                    currentFeedback = reloadFeedbackList(chatId, FeedbackType.ALL).get(currentIndex);
                    String textToSend = itemTemplateInsert(chatId, currentFeedback);
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_LIST_MENU));
                }
            }
            case FEEDBACK_MY -> {
                if (reloadFeedbackList(chatId, FeedbackType.MY).isEmpty()) {
                    String textToSend = "На данный момент у вас нет ни одного отзыва";
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_LIST_MENU));
                    executeMessage(message);
                } else {
                    Feedback currentFeedback;
                    currentFeedback = reloadFeedbackList(chatId, FeedbackType.MY).get(currentIndex);
                    String textToSend = itemTemplateInsert(chatId, currentFeedback);
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_LIST_MENU));
                }
            }
            case FEEDBACK_TO_APPROVE -> {
                if (reloadFeedbackList(chatId, FeedbackType.TO_APPROVE).isEmpty()) {
                    String textToSend = "На данный момент нет отзывов на рассмотрении";
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_LIST_MENU));
                    executeMessage(message);
                } else {
                    Feedback currentFeedback;
                    currentFeedback = reloadFeedbackList(chatId, FeedbackType.TO_APPROVE).get(currentIndex);
                    String textToSend = itemTemplateInsert(chatId, currentFeedback);
                    message = editMessage(chatId, userRepository.findById(chatId).get().getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_LIST_MENU));
                }
            }
        }
        executeMessage(message);
    }

    private String itemTemplateInsert(long chatId, Pet currentPet) {
        User user = userRepository.findById(chatId).get();
        return String.format(PET_DATA_TEMPLATE, user.getCurrentListIndex() + 1,
                currentPet.getCategory(),
                currentPet.getName(),
                currentPet.getAge(),
                ((currentPet.isSterilized()) ? "да" : "нет"),
                currentPet.getAbout(),
                user.getCurrentListIndex() + 1,
                petList.size()
        );
    }

    private String itemTemplateInsert(long chatId, VolunteerApplication currentApplication) {
        User user = userRepository.findById(currentApplication.getChatId()).get();
        return String.format(VOLUNTEER_APPLICATION_TEMPLATE,
                currentApplication.getChatId(),
                user.getFirstName(),
                (user.getPhoneNumber() == null) ? "не указан" : user.getPhoneNumber(),
                (user.getUserName() == null) ? "не указано" : user.getUserName(),
                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),
                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "не назначена" : currentApplication.getVisitDate().toString().substring(0, 10),
                userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                volunteerApplicationList.size()
        );
    }

    private String itemTemplateInsert(long chatId, PetClaimApplication currentApplication) {
        Pet pet = petRepository.findById(currentApplication.getPk().getId()).get();
        User user = userRepository.findById(currentApplication.getPk().getChatId()).get();
        return String.format(PET_CLAIM_APPLICATION_TEMPLATE,
                pet.getCategory(),
                pet.getName(),
                pet.getAge(),
                ((pet.isSterilized()) ? "да" : "нет"),

                currentApplication.getPk().getChatId(),
                user.getFirstName(),
                (user.getPhoneNumber() == null) ? "не указан" : user.getPhoneNumber(),
                (user.getUserName() == null) ? "не указано" : user.getUserName(),
                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),

                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "не назначена" : currentApplication.getVisitDate().toString().substring(0, 10),
                userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                petClaimApplicationList.size()
        );
    }

    private String itemTemplateInsert(long chatId, Feedback feedback) {
        User user = userRepository.findById(feedback.getPk().getChatId()).get();
        if (superUsers.contains(chatId)) {
            return String.format(FEEDBACK_USER_TEMPLATE,
                    (user.getUserName() == null) ? user.getFirstName() : "@".concat(user.getUserName()),
                    feedback.getRate(),
                    feedback.getFeedbackText(),
                    feedback.getAppliedAt().toString().substring(0, feedback.getAppliedAt().toString().length() - 7),

                    userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                    feedbackList.size()
            );
        } else {
            return String.format(FEEDBACK_ADMIN_TEMPLATE,
                    (user.getUserName() == null) ? user.getFirstName() : "@".concat(user.getUserName()),
                    user.getChatId(),
                    feedback.getRate(),
                    feedback.getFeedbackText(),
                    feedback.getAppliedAt().toString().substring(0, feedback.getAppliedAt().toString().length() - 7),
                    feedback.getStatus(),

                    userRepository.findById(chatId).get().getCurrentListIndex() + 1,
                    feedbackList.size()
            );
        }
    }

    private void claimPet(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        User user;
        if (userRepository.findById(chatId).isPresent()) {
            user = getUserFromRepo(chatId);
        } else {
            user = registerUser(update);
        }
        setUserState(chatId, UserState.PET_CLAIM_STATE);

        if (user.getPhoneNumber() == null) {
            contactRequestWarning(update, ItemType.PET_CLAIM_APPLICATION);
        } else {
            registerItemApplication(update, ItemType.PET_CLAIM_APPLICATION);
        }
    }

    private void contactRequestWarning(Update update, ItemType type) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        EditMessageText message = null;
        String textToSend = null;
        switch (type) {
            case PET_CLAIM_APPLICATION -> {
                textToSend = "<b>Заявка на просмотр питомца</b>\n" +
                        "Чтобы продолжить, отправьте нам свой контакт";
                message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.PROCEED_MENU));
            }
            case VOLUNTEER_APPLICATION -> {
                textToSend = "<b>Раздел волонтерства</b>\n" +
                        "Чтобы оставить заявку на волонтерскую помощь нашему приюту, отправьте нам свой контакт, и мы свяжемся с Вами";
                message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.PROCEED_MENU));
            }
            case FEEDBACK -> {
                textToSend = "<b>Раздел отзывов:speaking_head_in_silhouette:</b>\n" +
                        "Чтобы оставить отзыв о нашем приюте, отправьте нам свой контакт";
                message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.PROCEED_MENU));
            }
        }

        executeMessage(message);
    }

    private void inlinePetClaimApplications(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        petClaimApplicationList = reloadPetClaimApplicationList(chatId);
        setUserCurrentListIndex(chatId, 0);

        showItem(chatId, 0, ItemType.PET_CLAIM_APPLICATION);
    }

    private List<PetClaimApplication> reloadPetClaimApplicationList(long chatId) {
        List<PetClaimApplication> list;
        if (superUsers.contains(chatId)) {
            list = new ArrayList<>(StreamSupport
                    .stream(petClaimApplicationRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList()));
        } else {
            list = new ArrayList<>(StreamSupport
                    .stream(petClaimApplicationRepository.findByPkChatId(chatId).spliterator(), false)
                    .collect(Collectors.toList()));
        }

        return list;
    }

    private List<Feedback> reloadFeedbackList(long chatId, FeedbackType type) {
        List<Feedback> list = new ArrayList<>();
        switch (type) {
            case ALL -> {
                if (superUsers.contains(chatId)) {
                    list = new ArrayList<>(StreamSupport
                            .stream(feedbackRepository.findAll().spliterator(), false)
                            .collect(Collectors.toList()));
                } else {
                    list = new ArrayList<>(StreamSupport
                            .stream(feedbackRepository.findByStatusApproved().spliterator(), false)
                            .collect(Collectors.toList()));
                }
            }
            case MY -> {
                list = new ArrayList<>(StreamSupport
                        .stream(feedbackRepository.findByPkChatId(chatId).spliterator(), false)
                        .collect(Collectors.toList()));
            }
            case TO_APPROVE -> {
                list = new ArrayList<>(StreamSupport
                        .stream(feedbackRepository.findByStatusToApprove().spliterator(), false)
                        .collect(Collectors.toList()));
            }
        }
        return list;
    }

    /*private String approvedPetClaimApplicationTemplateInsert(PetClaimApplication currentApplication) {
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
    }*/

    /*private String approvedVolunteerApplicationTemplateInsert(VolunteerApplication currentApplication) {
        return String.format(VOLUNTEER_APPLICATION_APPROVED_TEMPLATE,
                currentApplication.getAppliedAt().toString().substring(0, currentApplication.getAppliedAt().toString().length() - 7),

                currentApplication.getStatus(),
                (currentApplication.getVisitDate() == null) ? "<i>ошибка</i>" : currentApplication.getVisitDate().toString().substring(0, 10)
        );
    }*/

    private void approveItem(Update update, ItemType type) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int currentIndex = userRepository.findById(chatId).get().getCurrentListIndex();
        switch (type) {
            case PET_CLAIM_APPLICATION -> {
                PetClaimApplication chosenApplication = petClaimApplicationList.get(currentIndex);
                PetClaimApplicationPK petClaimApplicationPK = chosenApplication.getPk();

                if (petClaimApplicationRepository.findById(petClaimApplicationPK).isPresent()) {
                    if (petClaimApplicationRepository.findById(petClaimApplicationPK).get().getStatus().equals("на рассмотрении")) {
                        chosenApplication.setStatus("одобрена");

                        Calendar cal = Calendar.getInstance();
                        cal.set(Calendar.HOUR_OF_DAY, 0);
                        cal.add(Calendar.DAY_OF_WEEK, 2);
                        chosenApplication.setVisitDate(new Timestamp(cal.getTime().getTime()));

                        petClaimApplicationRepository.save(chosenApplication);

                        userNotification(petClaimApplicationPK.getChatId(), chosenApplication);
                        log.info("Pet claim application updated to approved status in repository: " + chosenApplication);

                        petClaimApplicationList = reloadPetClaimApplicationList(chatId);
                        setUserCurrentListIndex(chatId, petClaimApplicationList.size() - 1);
                        showItem(chatId, petClaimApplicationList.size() - 1, ItemType.PET_CLAIM_APPLICATION);
                    }
                } else {
                    log.error("Couldn't find pet claim application in repository: " + chosenApplication);
                }
            }
            case VOLUNTEER_APPLICATION -> {
                VolunteerApplication chosenApplication = volunteerApplicationList.get(currentIndex);
                if (volunteerApplicationRepository.findById(chosenApplication.getChatId()).isPresent()) {
                    if (volunteerApplicationRepository.findById(chosenApplication.getChatId()).get().getStatus().equals("на рассмотрении")) {
                        chosenApplication.setStatus("одобрена");

                        Calendar cal = Calendar.getInstance();
                        cal.setTime(chosenApplication.getAppliedAt());
                        cal.add(Calendar.DAY_OF_WEEK, 1);
                        chosenApplication.setVisitDate(new Timestamp(cal.getTime().getTime()));

                        volunteerApplicationRepository.save(chosenApplication);

                        userNotification(chosenApplication);
                        log.info("Volunteer application updated to approved status in repository: " + chosenApplication);

                        volunteerApplicationList = reloadVolunteerApplicationList(chatId);

                        setUserCurrentListIndex(chatId, volunteerApplicationList.size() - 1);
                        showItem(chatId, volunteerApplicationList.size() - 1, ItemType.VOLUNTEER_APPLICATION);
                    }
                } else {
                    log.error("Couldn't find volunteer application in repository: " + chosenApplication);
                }
            }
        }
    }

    private void inlineVolunteerApplications(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        volunteerApplicationList = reloadVolunteerApplicationList(chatId);
        setUserCurrentListIndex(chatId, 0);

        showItem(chatId, 0, ItemType.VOLUNTEER_APPLICATION);
    }

    private InlineKeyboardMarkup customInlineMarkup(long chatId, InlineMarkupType type) {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        switch (type) {
            case VOLUNTEER_APPLICATION_MENU -> {
                if (!volunteerApplicationList.isEmpty()) {
                    if (volunteerApplicationList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.VOLUNTEER_APPLICATIONS_PREVIOUS));
                    if (superUsers.contains(chatId) && !volunteerApplicationList.isEmpty())
                        row.add(createInlineButton(EmojiParser.parseToUnicode("Одобрить"), CallbackConstants.VOLUNTEER_APPLICATIONS_APPROVE));
                    if (volunteerApplicationList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.VOLUNTEER_APPLICATIONS_NEXT));
                }
                rowsInline.add(row);

                /*if (superUsers.contains(chatId)) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.VOLUNTEER_APPLICATIONS_ADD))));
                    if (!volunteerApplicationList.isEmpty()) {
                        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Редактировать", CallbackConstants.VOLUNTEER_APPLICATIONS_EDIT))));
                    }
                }*/

                if (!volunteerApplicationList.isEmpty()) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.VOLUNTEER_APPLICATIONS_DELETE))));
                }

                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.APPLICATIONS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case MAIN_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Питомцы", CallbackConstants.PETS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Пожертвовать", CallbackConstants.DONATE))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Волонтерство", CallbackConstants.VOLUNTEER))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Отзывы", CallbackConstants.FEEDBACK))));
                if (superUsers.contains(chatId)) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Заявки", CallbackConstants.APPLICATIONS))));
                } else {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Мои заявки", CallbackConstants.APPLICATIONS))));
                }
                keyboardMarkup.setKeyboard(rowsInline);
            }
            case PET_CLAIM_APPLICATION -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.PETS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case PET_CLAIM_APPLICATION_MENU -> {
                if (!petClaimApplicationList.isEmpty()) {
                    if (petClaimApplicationList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.PETS_CLAIM_PREVIOUS));
                    if (superUsers.contains(chatId) && !petClaimApplicationList.isEmpty())
                        row.add(createInlineButton(EmojiParser.parseToUnicode("Одобрить"), CallbackConstants.PETS_CLAIM_APPROVE));
                    if (petClaimApplicationList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.PETS_CLAIM_NEXT));
                }
                rowsInline.add(row);

                /*if (superUsers.contains(chatId)) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.PETS_CLAIM_ADD))));
                }*/

                if (!petClaimApplicationList.isEmpty()) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.PETS_CLAIM_DELETE))));
                }

                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.APPLICATIONS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case PETS_MENU -> {
                if (!petList.isEmpty()) {
                    if (petList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.PETS_PREVIOUS));
                    row.add(createInlineButton(EmojiParser.parseToUnicode("Забрать"), CallbackConstants.PETS_CLAIM));
                    if (petList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.PETS_NEXT));
                }
                rowsInline.add(row);

                if (superUsers.contains(chatId)) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Добавить", CallbackConstants.PETS_ADD))));
                    if (!petList.isEmpty()) {
                        //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Редактировать", CallbackConstants.PETS_EDIT))));
                        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":o:Удалить", CallbackConstants.PETS_DELETE))));
                    }
                }

                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case DONATE_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Деньги :money_with_wings:", CallbackConstants.DONATE_MONEY))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Корм и консервы :stew:", CallbackConstants.DONATE_FOOD))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Лекарства :pill:", CallbackConstants.DONATE_DRUGS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Хозяйственные нужды :broom:", CallbackConstants.DONATE_HOUSEKEEPING))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case FEEDBACK_NEW -> {
                row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_RATE_1));
                row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_RATE_2));
                row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_RATE_3));
                row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_RATE_4));
                row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_NEW_RATE_5));
                rowsInline.add(row);

                //rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.BACK))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case FEEDBACK_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Просмотреть отзывы", CallbackConstants.FEEDBACK_SHOW))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Оставить отзыв", CallbackConstants.FEEDBACK_NEW))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case FEEDBACK_SHOW -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Все отзывы:busts_in_silhouette:", CallbackConstants.FEEDBACK_SHOW_ALL))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Мои отзывы:bust_in_silhouette:", CallbackConstants.FEEDBACK_SHOW_MY))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.FEEDBACK))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case FEEDBACK_LIST_MENU -> {
                if (!feedbackList.isEmpty()) {
                    if (feedbackList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_left:"), CallbackConstants.FEEDBACK_LIST_PREVIOUS));
                    if (superUsers.contains(chatId) && !feedbackList.isEmpty())
                        row.add(createInlineButton(EmojiParser.parseToUnicode("Одобрить"), CallbackConstants.FEEDBACK_LIST_APPROVE));
                    if (feedbackList.size() > 1)
                        row.add(createInlineButton(EmojiParser.parseToUnicode(":arrow_right:"), CallbackConstants.FEEDBACK_LIST_NEXT));
                }
                rowsInline.add(row);

                /*if (superUsers.contains(chatId)) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Добавить", CallbackConstants.PETS_CLAIM_ADD))));
                }*/

                if (!feedbackList.isEmpty()) {
                    rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Удалить", CallbackConstants.FEEDBACK_LIST_DELETE))));
                }
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":arrow_left:Назад", CallbackConstants.FEEDBACK))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case VOLUNTEER_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Оставить заявку", CallbackConstants.VOLUNTEER_APPLY))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case PROCEED_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Продолжить", CallbackConstants.PROCEED))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case APPLICATIONS_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("на питомцев:dog:", CallbackConstants.PETS_CLAIM_APPLICATIONS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("на волонтерство:handshake:", CallbackConstants.VOLUNTEER_APPLICATIONS))));
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
            case RETURN_TO_MENU -> {
                rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton(":leftwards_arrow_with_hook:Главное меню", CallbackConstants.RETURN_TO_MENU))));

                keyboardMarkup.setKeyboard(rowsInline);
            }
        }
        return keyboardMarkup;
    }

    /*private void addVolunteerApplication(Update update) {
        Pet defaultPet = new Pet();

        long chatId = update.getCallbackQuery().getMessage().getChatId();
        String textToSend = itemTemplateInsert(defaultPet) +
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

    private List<VolunteerApplication> reloadVolunteerApplicationList(long chatId) {
        List<VolunteerApplication> list = new ArrayList<>();
        if (superUsers.contains(chatId)) {
            list = new ArrayList<>(StreamSupport
                    .stream(volunteerApplicationRepository.findAll().spliterator(), false)
                    .collect(Collectors.toList()));
        } else {
            if (volunteerApplicationRepository.findById(chatId).isPresent()) {
                list = new ArrayList<>(Arrays.asList(volunteerApplicationRepository.findById(chatId).get()));
            }
        }

        return list;
    }

    private void sendContactRequest(Update update) {
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

        KeyboardButton backButton = new KeyboardButton();
        backButton.setText(EmojiParser.parseToUnicode("Назад"));

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
        EditMessageText message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_NEW));
        executeMessage(message);
    }

    private void inlineReturnToMenu(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        User user = getUserFromRepo(chatId);
        user.setState(UserState.BASIC_STATE);
        EditMessageText message = editMessage(chatId, messageId, "<b>Главное меню</b> нашего приюта", customInlineMarkup(chatId, InlineMarkupType.MAIN_MENU));
        user.setMenuMessageId(executeMessage(message));
        userRepository.save(user);
    }

    private void applyVolunteer(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        setUserState(chatId, UserState.VOLUNTEER_STATE);
        User user;
        if (userRepository.findById(chatId).isPresent()) {
            user = getUserFromRepo(chatId);
        } else {
            user = registerUser(update);
        }
        setUserState(chatId, UserState.PET_CLAIM_STATE);

        if (user.getPhoneNumber() == null) {
            contactRequestWarning(update, ItemType.VOLUNTEER_APPLICATION);
        } else {
            registerItemApplication(update, ItemType.VOLUNTEER_APPLICATION);
        }
    }

    private User getUserFromRepo(long chatId) {
        return (userRepository.findById(chatId).isPresent()) ? userRepository.findById(chatId).get() : null;
    }

    private void contactReceived(Update update) {
        registerContact(update);
    }

    private void registerContact(Update update) {
        Contact contact = update.getMessage().getContact();
        long chatId = contact.getUserId();
        User user = userRepository.findById(chatId).get();

        if (user.getPhoneNumber() == null) {
            user.setPhoneNumber(contact.getPhoneNumber());

            userRepository.save(user);

            log.info("Saved user's phone number: " + user);
        }

        int deleteMarkupMessageId = sendMessage(chatId, "Загрузка...", new ReplyKeyboardRemove(true, null));

        DeleteMessage message = new DeleteMessage();
        message.setChatId(chatId);
        message.setMessageId(deleteMarkupMessageId);
        deleteMessage(message);

        if (user.getState() == UserState.VOLUNTEER_STATE) {
            registerItemApplication(update, ItemType.VOLUNTEER_APPLICATION);
        } else if (user.getState() == UserState.PET_CLAIM_STATE) {
            registerItemApplication(update, ItemType.PET_CLAIM_APPLICATION);
        } else if (user.getState() == UserState.FEEDBACK_STATE) {
            registerItemApplication(update, ItemType.PET_CLAIM_APPLICATION);
        }
    }


    private void registerItemApplication(Update update, ItemType type) {
        long chatId;
        if (update.hasCallbackQuery()) {
            chatId = update.getCallbackQuery().getMessage().getChatId();
        } else {
            chatId = update.getMessage().getChatId();
        }

        User user = userRepository.findById(chatId).get();
        String textToSend = null;

        switch (type) {
            case VOLUNTEER_APPLICATION -> {
                if (volunteerApplicationRepository.findById(chatId).isEmpty()) {
                    VolunteerApplication application = VolunteerApplication.builder()
                            .chatId(chatId)
                            .status("на рассмотрении")
                            .build();

                    volunteerApplicationRepository.save(application);

                    volunteerApplicationList = reloadVolunteerApplicationList(chatId);
                    setUserCurrentListIndex(chatId, volunteerApplicationList.size() - 1);
                    adminNotification(NotificationType.VOLUNTEER);
                    log.info("Volunteer's application saved to repository: " + application);

                    textToSend = "Ваша заявка на волонтерство передана менеджеру\n" +
                            "В ближайший рабочий день мы обработаем Вашу заявку и уведомим об этом Вас\n" +
                            "Ожидайте, пожалуйста\n\n" +
                            "<i>Просмотреть свои заявки и взаимодействовать с ними Вы можете в разделе \"Мои заявки\" главного меню</i>";
                } else {
                    textToSend = "Ваша предыдущая заявка на волонтерство уже находится в обработке\n\n" +
                            "<i>Просмотреть свои заявки и взаимодействовать с ними Вы можете в разделе \"Мои заявки\" главного меню</i>";
                }
            }
            case PET_CLAIM_APPLICATION -> {
                petList = reloadPetList();
                Pet chosenPet = petList.get(user.getCurrentListIndex());
                PetClaimApplicationPK petClaimApplicationPK = new PetClaimApplicationPK(chosenPet.getId(), chatId);

                if (petClaimApplicationRepository.findById(petClaimApplicationPK).isEmpty()) {
                    PetClaimApplication petClaimApplication = PetClaimApplication.builder()
                            .pk(petClaimApplicationPK)
                            .status("на рассмотрении")
                            .build();

                    petClaimApplicationRepository.save(petClaimApplication);

                    petClaimApplicationList = reloadPetClaimApplicationList(chatId);
                    adminNotification(NotificationType.PET_CLAIM);
                    log.info("Pet claim application saved to repository: " + petClaimApplication);

                    textToSend = "Ваша заявка на просмотр питомца по кличке " + chosenPet.getName() + " передана менеджеру\n" +
                            "В ближайший рабочий день мы обработаем Вашу заявку и уведомим об этом Вас\n" +
                            "Ожидайте, пожалуйста\n\n" +
                            "<i>Просмотреть свои заявки и взаимодействовать с ними Вы можете в разделе \"Мои заявки\" главного меню</i>";
                } else {
                    textToSend = "Ваша предыдущая заявка на просмотр питомца по кличке " + chosenPet.getName() + " уже находится в обработке\n" +
                            "Ожидайте, пожалуйста\n\n" +
                            "<i>Просмотреть свои заявки и взаимодействовать с ними Вы можете в разделе \"Мои заявки\" главного меню</i>";
                }
            }
            case FEEDBACK -> {
                feedbackList = reloadFeedbackList(chatId, FeedbackType.ALL);
                FeedbackPK feedbackPK = FeedbackPK.builder()
                        .chatId(chatId)
                        .build();
                if (feedbackRepository.findByChatIdAndStatusToApprove(feedbackPK.getChatId()).size() < 3) {
                    Feedback feedback = Feedback.builder()
                            .pk(feedbackPK)
                            .status("на рассмотрении")
                            .build();

                    feedbackRepository.save(feedback);

                    feedbackList = reloadFeedbackList(chatId, FeedbackType.ALL);
                    adminNotification(NotificationType.FEEDBACK);
                    log.info("New feedback saved to repository: " + feedback);

                    textToSend = "Ваш отзыв отправлен на рассмотрение модератором и будет опубликован после одобрения\n" +
                            "Ожидайте, пожалуйста\n\n" +
                            "<i>Просмотреть свои отзывы и взаимодействовать с ними Вы можете в подразделе \"Мои отзывы\" раздела \"Отзывы\"</i>";
                } else {
                    textToSend = "К сожалению, вы не можете оставить отзыв, если 3 ваших отзыва находится на рассмотрении\n" +
                            "Ожидайте, пожалуйста\n\n" +
                            "<i>Просмотреть свои отзывы и взаимодействовать с ними Вы можете в подразделе \"Мои отзывы\" раздела \"Отзывы\"</i>";
                }
            }
        }

        if (update.hasCallbackQuery()) {
            EditMessageText message = editMessage(chatId, user.getMenuMessageId(), textToSend, customInlineMarkup(chatId, InlineMarkupType.RETURN_TO_MENU));
            executeMessage(message);
        } else {
            setUserMenuMessageId(chatId, sendMessage(chatId, textToSend, customInlineMarkup(chatId, InlineMarkupType.RETURN_TO_MENU)));
        }
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

    private void adminNotification(NotificationType type) {
        switch (type) {
            case VOLUNTEER -> {
                String firstText = ":bellhop_bell:<b>Новая заявка на волонтерство</b>:bellhop_bell:";
                for (long adminId : superUsers) {
                    sendMessage(adminId, firstText);
                    menuCommandReceived(adminId);
                    setUserCurrentListIndex(adminId, volunteerApplicationList.size() - 1);
                    showItem(adminId, volunteerApplicationList.size() - 1, ItemType.VOLUNTEER_APPLICATION);
                    log.info("Notified admin with chatId = " + adminId + " about new volunteer application");
                }
            }
            case PET_CLAIM -> {
                String firstText = ":bellhop_bell:<b>Новая заявка на просмотр питомца</b>:bellhop_bell:";
                for (long adminId : superUsers) {
                    sendMessage(adminId, firstText);
                    menuCommandReceived(adminId);
                    setUserCurrentListIndex(adminId, petClaimApplicationList.size() - 1);
                    showItem(adminId, petClaimApplicationList.size() - 1, ItemType.PET_CLAIM_APPLICATION);
                    log.info("Notified admin with chatId = " + adminId + " about new volunteer application");
                }
            }
            case FEEDBACK -> {
                String firstText = ":bellhop_bell:<b>Новый отзыв ждет одобрения</b>:bellhop_bell:";
                for (long adminId : superUsers) {
                    sendMessage(adminId, firstText);
                    menuCommandReceived(adminId);
                    setUserCurrentListIndex(adminId, feedbackList.size() - 1);
                    showItem(adminId, feedbackList.size() - 1, ItemType.FEEDBACK);
                    log.info("Notified admin with chatId = " + adminId + " about new feedback application");
                }
            }
            default -> {

            }
        }
    }

    private void userNotification(long chatId, PetClaimApplication application) {
        String firstText = ":bellhop_bell:<b>Одобрена заявка на просмотр питомца</b>:bellhop_bell:";
        Pet pet = petRepository.findById(application.getPk().getId()).get();
        String applicationText = String.format(PET_CLAIM_APPLICATION_APPROVED_TEMPLATE,
                pet.getCategory(),
                pet.getName(),
                pet.getAge(),
                ((pet.isSterilized()) ? "да" : "нет"),

                application.getAppliedAt().toString().substring(0, application.getAppliedAt().toString().length() - 7),

                application.getStatus(),
                (application.getVisitDate() == null) ? "<i>ошибка</i>" : application.getVisitDate().toString().substring(0, 10)
        );
        //String applicationText = approvedPetClaimApplicationTemplateInsert(application);
        sendMessage(application.getPk().getChatId(), firstText);

        setNotificationMessageIdPetClaim(application, sendMessage(application.getPk().getChatId(), applicationText));
        setUserMenuMessageId(application.getPk().getChatId(), sendMessage(application.getPk().getChatId(), "<b>Главное меню</b>", customInlineMarkup(application.getPk().getChatId(), InlineMarkupType.RETURN_TO_MENU)));
        log.info("Notified user with chatId = " + chatId + " about approval on pet claim application");
    }

    private void userNotification(VolunteerApplication application) {
        String firstText = ":bellhop_bell:<b>Одобрена заявка на волонтерство</b>:bellhop_bell:";
        String applicationText = String.format(VOLUNTEER_APPLICATION_APPROVED_TEMPLATE,
                application.getAppliedAt().toString().substring(0, application.getAppliedAt().toString().length() - 7),

                application.getStatus(),
                (application.getVisitDate() == null) ? "<i>ошибка</i>" : application.getVisitDate().toString().substring(0, 10)
        );  // approvedVolunteerApplicationTemplateInsert(application);
        sendMessage(application.getChatId(), firstText);

        setNotificationMessageIdVolunteer(application, sendMessage(application.getChatId(), applicationText));
        setUserMenuMessageId(application.getChatId(), sendMessage(application.getChatId(), "<b>Главное меню</b>", customInlineMarkup(application.getChatId(), InlineMarkupType.RETURN_TO_MENU)));
        log.info("Notified user with chatId = " + application.getChatId() + " about approval on volunteer application");
    }

    private void inlineFeedback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел отзывов</b> :speaking_head_in_silhouette:";

        EditMessageText message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.FEEDBACK_MENU));

        executeMessage(message);
    }

    private void inlineDonate(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел пожертвований</b>\n" +
                "Мы принимаем:";

        EditMessageText message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.DONATE_MENU));

        executeMessage(message);
    }

    private void inlineApplications(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend;
        if (superUsers.contains(chatId)) {
            textToSend = "<b>Все заявки</b>";
        } else {
            textToSend = "<b>Мои заявки</b>";
        }
        EditMessageText message = editMessage(chatId, messageId, textToSend, customInlineMarkup(chatId, InlineMarkupType.APPLICATIONS_MENU));

        executeMessage(message);
    }

    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String firstName = update.getMessage().getChat().getFirstName();

        registerUser(update);

        String startResponse = String.format(START_TEXT, firstName);

        sendMessage(chatId, startResponse);

        menuCommandReceived(chatId);
    }

    private User registerUser(Update update) {
        long chatId = update.getMessage().getChatId();
        User transientUser = null;
        if (userRepository.findById(chatId).isEmpty()) {
            transientUser = User.builder()
                    .chatId(chatId)
                    .firstName(update.getMessage().getChat().getFirstName())
                    .userName(update.getMessage().getChat().getUserName())
                    .currentListIndex(0)
                    .state(UserState.BASIC_STATE)
                    .build();
        } else {
            transientUser = userRepository.findById(chatId).get();
            transientUser.setFirstName(update.getMessage().getChat().getFirstName());
            transientUser.setUserName(update.getMessage().getChat().getUserName());
            transientUser.setState(UserState.BASIC_STATE);
        }
        return userRepository.save(transientUser);
    }

    private void setUserState(long chatId, UserState state) {
        User user = null;
        if (userRepository.findById(chatId).isPresent()) {
            user = userRepository.findById(chatId).get();
        } else {
            log.error("Couldn't find user with chatId = " + chatId + " in userRepository");
        }
        user.setState(state);
        userRepository.save(user);
    }

    private void setUserMenuMessageId(long chatId, int messageId) {
        User user = null;
        if (userRepository.findById(chatId).isPresent()) {
            user = userRepository.findById(chatId).get();
        } else {
            log.error("Couldn't find user with chatId = " + chatId + " in userRepository");
        }
        user.setMenuMessageId(messageId);
        userRepository.save(user);
    }

    private void setNotificationMessageIdVolunteer(VolunteerApplication application, int messageId) {
        if (volunteerApplicationRepository.findById(application.getChatId()).isPresent()) {
            application.setNotificationMessageId(messageId);
            volunteerApplicationRepository.save(application);
        } else {
            log.error("Couldn't find volunteer application in repo, chatId = " + application.getChatId());
        }
    }

    private void setNotificationMessageIdPetClaim(PetClaimApplication application, int messageId) {
        PetClaimApplicationPK pk = new PetClaimApplicationPK(application.getPk().getId(), application.getPk().getChatId());
        if (petClaimApplicationRepository.findById(pk).isPresent()) {
            application.setNotificationMessageId(messageId);
            petClaimApplicationRepository.save(application);
        } else {
            log.error("Couldn't find pet claim application in repo, chatId = " + application.getPk().getChatId());
        }
    }

    private void menuCommandReceived(long chatId) {
        DeleteMessage message = new DeleteMessage();
        message.setChatId(chatId);
        User currentUser = userRepository.findById(chatId).get();
        message.setMessageId(currentUser.getMenuMessageId());
        deleteMessage(message);

        currentUser.setMenuMessageId(sendMessage(chatId, "<b>Главное меню</b> нашего приюта", customInlineMarkup(chatId, InlineMarkupType.MAIN_MENU)));
        currentUser.setState(UserState.BASIC_STATE);
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
            log.error("Error occurred while attempting to delete message: " + e.getMessage());
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

        String textToSend = messageText + "\n" + itemTemplateInsert(chatId, transientNewPet) +
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
