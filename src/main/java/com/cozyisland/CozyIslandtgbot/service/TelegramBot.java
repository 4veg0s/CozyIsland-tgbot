package com.cozyisland.CozyIslandtgbot.service;

import com.cozyisland.CozyIslandtgbot.config.BotConfig;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.ParseMode;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    static final String START_TEXT = "Привет, %s! Добро пожаловать на <b>Островок тепла</b>!";

    // TODO: добавить стикеры из emoji java
    static final String HELP_TEXT = "Мы предоставляем профессиональную ветеринарную помощь животным." +
            "Осуществляем профилактику заболеваний, проводим регулярные осмотры и лечение животных" +
            ", которые нуждаются в медицинской помощи." +
            " Заботимся о заведении медицинской истории каждого питомца\n\n";
    final BotConfig config;
    List<Long> superUsers;
    List<BotCommand> listOfCommands;

    public TelegramBot(BotConfig config) {
        this.config = config;

        superUsers = new ArrayList<>();
        superUsers.add((long) 416657716);   // Андрей

        listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "начать общение"));
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
                case "/help" -> {
                    helpCommandReceived(update);
                }
            }
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
                case CallbackConstants.RETURN_TO_MENU -> {
                    inlineReturnToMenu(update);
                }
            }
        }
    }

    private void inlineReturnToMenu(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        int messageId = update.getMessage().getMessageId();
        EditMessageText message = editMessage(chatId, messageId, "Возврат в главное меню");
        executeMessage(message);
        startCommandReceived(update);
    }

    private void inlineVolunteer(Update update) {
    }

    private void inlineFeedback(Update update) {
        long chatId = update.getCallbackQuery().getMessage().getChatId();
        sendMessage(chatId, "Раздел отзывов", myFeedbackMenuInlineMarkup());
    }

    private ReplyKeyboard feedbackMenuInlineMarkup() {

        return null;
    }

    private void inlinePets(Update update) {

    }

    private void inlineDonate(Update update) {
    }

    private void startCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String name = update.getMessage().getChat().getFirstName();

        String startResponse = String.format(START_TEXT, name);

        sendMessage(chatId, startResponse, menuInlineMarkup());
    }

    private void helpCommandReceived(Update update) {
        long chatId = update.getMessage().getChatId();
        String userName = update.getMessage().getChat().getUserName();

        sendMessage(chatId, EmojiParser.parseToUnicode(HELP_TEXT));
        log.info("Replied to HELP command from user https://t.me/" + userName + " with chatId = " + chatId);
    }

    private void sendMessage(long chatId, String textToSend) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);

        executeMessage(message);
    }

    private void sendMessage(long chatId, String textToSend, ReplyKeyboard keyboardMarkup) {
        SendMessage message = new SendMessage();
        message.setChatId(chatId);
        message.setText(textToSend);
        message.setParseMode(ParseMode.HTML);
        message.setReplyMarkup(keyboardMarkup);

        executeMessage(message);
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
        message.setText(textToSend);

        return message;
    }

    private EditMessageText editMessage(long chatId, int messageId, String textToSend, InlineKeyboardMarkup inlineKeyboardMarkup) {
        EditMessageText message = new EditMessageText();

        message.setChatId(chatId);
        message.setMessageId(messageId);
        message.setText(textToSend);
        message.setReplyMarkup(inlineKeyboardMarkup);

        return message;
    }

    private InlineKeyboardButton createInlineButton(String name, String callbackData) {
        InlineKeyboardButton button = new InlineKeyboardButton();
        button.setText(name);
        button.setCallbackData(callbackData);

        return button;
    }

    private InlineKeyboardMarkup menuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Питомцы", CallbackConstants.PETS))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Пожертвовать", CallbackConstants.DONATE))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Хочу стать волонтером", CallbackConstants.VOLUNTEER))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Оставить отзыв", CallbackConstants.FEEDBACK))));

        keyboardMarkup.setKeyboard(rowsInline);

        return keyboardMarkup;
    }

    private InlineKeyboardMarkup myFeedbackMenuInlineMarkup() {
        InlineKeyboardMarkup keyboardMarkup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsInline = new ArrayList<>();
        List<InlineKeyboardButton> row = new ArrayList<>();
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_1));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_2));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_3));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_4));
        row.add(createInlineButton(EmojiParser.parseToUnicode(":star:"), CallbackConstants.FEEDBACK_5));
        rowsInline.add(row);

        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("Назад", CallbackConstants.BACK))));
        rowsInline.add(new ArrayList<>(Arrays.asList(createInlineButton("В главное меню", CallbackConstants.RETURN_TO_MENU))));

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
