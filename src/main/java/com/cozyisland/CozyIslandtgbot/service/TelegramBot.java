package com.cozyisland.CozyIslandtgbot.service;

import com.cozyisland.CozyIslandtgbot.config.BotConfig;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Contact;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    static final String START_TEXT = EmojiParser.parseToUnicode("Привет, %s!\n" +
            "Добро пожаловать на \n" +
            ":feet:<b>Островок тепла</b>:feet:!");

    // TODO: добавить стикеры из emojipedia
    static final String HELP_TEXT = EmojiParser.parseToUnicode("Мы предоставляем профессиональную ветеринарную помощь животным. " +
            "Осуществляем профилактику заболеваний, проводим регулярные осмотры и лечение животных" +
            ", которые нуждаются в медицинской помощи." +
            " Заботимся о заведении медицинской истории каждого питомца\n\n");
    final BotConfig config;
    List<Long> superUsers;
    List<BotCommand> listOfCommands;
    int menuMessageId;



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
            String messageTest = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            switch (messageTest) {
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
                    if (messageTest.equals("Главное меню")) {
                        if (menuMessageId != 0) {
                            sendMessage(chatId, "Переход в главное меню", new ReplyKeyboardRemove(true, null));
                        }
                        menuCommandReceived(chatId);
                    }
                }
            }
        } else if (update.hasMessage() && update.getMessage().hasContact()) {
            contactReceived(update);
        } else if (update.hasCallbackQuery()) {
            String callbackData = update.getCallbackQuery().getData();

            switch (callbackData) {
                case CallbackConstants.DONATE -> {
                    inlineDonate(update);
                }
                case CallbackConstants.PETS -> {
                    inlinePets(update);
                }
                case CallbackConstants.FEEDBACK -> {
                    inlineFeedback(update);
                }
                case CallbackConstants.VOLUNTEER -> {
                    inlineVolunteer(update);
                }
                case CallbackConstants.VOLUNTEER_SEND_CONTACT -> {
                    inlineVolunteerSendContact(update);
                }
                case CallbackConstants.RETURN_TO_MENU -> {
                    inlineReturnToMenu(update);
                }
                case CallbackConstants.FEEDBACK_SHOW -> {
                    // TODO: просмотреть отзывы
                }
                case CallbackConstants.FEEDBACK_NEW -> {
                    inlineFeedbackNew(update);
                }
            }
        }
    }

    private void inlineFeedbackNew(Update update) {
        setMenuMessageId(update.getCallbackQuery().getMessage().getMessageId());
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "Оставьте Ваш отзыв о нашем приюте";
        EditMessageText message = editMessage(chatId, messageId, textToSend, newFeedbackInlineMarkup());
        executeMessage(message);
    }

    private void inlineReturnToMenu(Update update) {
        setMenuMessageId(update.getCallbackQuery().getMessage().getMessageId());
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        EditMessageText message = editMessage(chatId, messageId, "<b>Главное меню</b> нашего приюта", mainMenuInlineMarkup());
        executeMessage(message);
    }

    private void inlineVolunteer(Update update) {
        setMenuMessageId(update.getCallbackQuery().getMessage().getMessageId());
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = "<b>Раздел волонтерства</b>\n" +
                "Чтобы оставить заявку на волонтерскую помощь нашему приюту, отправьте нам свой контакт, и мы свяжемся с Вами";

        EditMessageText message = editMessage(chatId, messageId, textToSend, volunteerMenuInlineMarkup());

        executeMessage(message);
    }

    private void inlineVolunteerSendContact(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        // TODO: текст для волонтера
        String textToSend = "Чтобы отправить свой контакт, нажмите на кнопку внизу:point_down:";

        DeleteMessage menuMessage = new DeleteMessage();
        menuMessage.setChatId(chatId);
        menuMessage.setMessageId(menuMessageId);
        executeMessage(menuMessage);

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

    // TODO: обработать получение контакта пользователя
    private void contactReceived(Update update) {
        Contact contact = update.getMessage().getContact();
        String phoneNumber = contact.getPhoneNumber();

        String textToSend = "Ваш контакт (+" + phoneNumber + ") передан менеджеру\n" +
                "В ближайший рабочий день мы обработаем Вашу заявку и перезвоним Вам\n" +
                "Ожидайте, пожалуйста";

        sendMessage(contact.getUserId(), textToSend, new ReplyKeyboardRemove(true, null));

        DeleteMessage message = new DeleteMessage();
        message.setChatId(contact.getUserId());
        message.setMessageId(this.menuMessageId);
        executeMessage(message);

        sendMessage(contact.getUserId(), "Переход в главное меню");
        menuCommandReceived(contact.getUserId());
    }

    private InlineKeyboardMarkup volunteerMenuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Продолжить", CallbackConstants.VOLUNTEER_SEND_CONTACT))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlineFeedback(Update update) {
        setMenuMessageId(update.getCallbackQuery().getMessage().getMessageId());
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getCallbackQuery().getMessage().getMessageId();
        String textToSend = ":speech_balloon: <b>Раздел отзывов</b> :speech_balloon:";

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

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.BACK))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Главное меню", CallbackConstants.RETURN_TO_MENU))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private void inlinePets(Update update) {
        setMenuMessageId(update.getCallbackQuery().getMessage().getMessageId());
    }

    private void inlineDonate(Update update) {
        setMenuMessageId(update.getCallbackQuery().getMessage().getMessageId());
    }

    private void setMenuMessageId(int messageId) {
        if (this.menuMessageId != messageId) {
            this.menuMessageId = messageId;
        }
    }

    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getChat().getFirstName();

        String startResponse = String.format(START_TEXT, name);

        sendMessage(chatId, startResponse);

        menuCommandReceived(chatId);
    }

    private void menuCommandReceived(long chatId) {
        DeleteMessage message = new DeleteMessage();
        message.setChatId(chatId);
        message.setMessageId(this.menuMessageId);
        executeMessage(message);

        sendMessage(chatId, "<b>Главное меню</b> нашего приюта", mainMenuInlineMarkup());
    }

    private void helpCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();

        sendMessage(chatId, HELP_TEXT);
        log.info("Replied to HELP command from user https://t.me/" + userName + " with chatId = " + chatId);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(textToSend));
        message.setParseMode(ParseMode.HTML);

        executeMessage(message);
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(EmojiParser.parseToUnicode(textToSend));
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
    }


    private void executeMessage(DeleteMessage message) {
        try {
            execute(message);
            log.info("Message was successfully deleted");
        } catch (TelegramApiException e) {
            log.error("Error occurred while attempting to delete message");
        }
    }

    private void executeMessage(SendMessage message) {
        try {
            execute(message);
            log.info("Message sent successfully to user with chatId = " + message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Error occurred while attempting to send a message to user with chatId = " + message.getChatId() + ": " + e.getMessage());
        }
    }

    private void executeMessage(EditMessageText message) {
        try {
            execute(message);
            log.info("Message sent successfully to user with chatId = " + message.getChatId());
        } catch (TelegramApiException e) {
            log.error("Error occurred while attempting to send a message to user with chatId = " + message.getChatId() + ": " + e.getMessage());
        }
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
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Хочу стать волонтером", CallbackConstants.VOLUNTEER))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Отзывы", CallbackConstants.FEEDBACK))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup yesNoInlineMarkup(String callbackNameYes, String callbackNameNo) {
        InlineKeyboardMarkup inlineKeyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();

        var yesButton = new InlineKeyboardButton();
        yesButton.setText("Да");
        yesButton.setCallbackData(callbackNameYes);

        var noButton = new InlineKeyboardButton();
        noButton.setText("Нет");
        noButton.setCallbackData(callbackNameNo);

        row.add(yesButton);
        row.add(noButton);

        rowsInline.add(row);

        inlineKeyboardMarkup.setKeyboard(rowsInline);

        return inlineKeyboardMarkup;
    }
}
