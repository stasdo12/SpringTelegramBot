package com.example.springdemobot.service;

import com.example.springdemobot.config.BotConfig;
import com.example.springdemobot.model.User;
import com.example.springdemobot.model.UserRepo;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.bots.TelegramLongPollingBot;
import org.telegram.telegrambots.meta.api.methods.commands.SetMyCommands;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;
import org.telegram.telegrambots.meta.api.objects.commands.scope.BotCommandScopeDefault;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboard;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class TelegramBot extends TelegramLongPollingBot {
    @Autowired
    private UserRepo userRepo;

   final BotConfig config;

    static final String  HELP_TEXT =  "This bot is created to demo Spring \n " +
            "You can execute commands from the menu:\n\n" +
            "/start\n\n"+
            "/mydata\n\n"+
            "/deletedata\n\n"+
            "/help\n\n"+
            "/settings\n\n"+
            "This bot created StanD for Happy new Year";

    public TelegramBot(BotConfig config) {
        this.config = config;
        List<BotCommand> listOfCommands = new ArrayList<>();
        listOfCommands.add(new BotCommand("/start", "start App"));
        listOfCommands.add(new BotCommand("/mydata", "show my data"));
        listOfCommands.add(new BotCommand("/deletedata", "delete my data"));
        listOfCommands.add(new BotCommand("/help", "how to use bot"));
        listOfCommands.add(new BotCommand("/settings", "properties"));


        try {
            this.execute(new SetMyCommands(listOfCommands, new BotCommandScopeDefault(), null));
        }
        catch (TelegramApiException e){
            log.error("Error occurrence: " + e.getMessage());
        }


    }


    @Override
    public String getBotUsername() {
        return config.getBotName();
    }

    @Override
    public String getBotToken() {
        return config.getToken();
    }

    @Override
    public void onUpdateReceived(Update update) {
        if (update.hasMessage() && update.getMessage().hasText()){
            String messageText = update.getMessage().getText();
            long chatId = update.getMessage().getChatId();

            if (messageText.contains("/send")&& config.getOwner() == chatId){
                var textToSend = EmojiParser.parseToUnicode(messageText.substring(messageText.indexOf(" ")));
                var users = userRepo.findAll();
                for (User user : users){
                    sendMessage(user.getChatId(), textToSend);
                }
            }


            switch (messageText){
                case "/start":

                    registerUser(update.getMessage());
                    startCommandReceived(chatId, update.getMessage().getChat().getFirstName() );
                    break;

                case "/help":
                    startCommandReceived(chatId, HELP_TEXT);
                    break;

                case "register":
                    register(chatId);
                    break;

                case "/send" :
                    break;


                default:sendMessage(chatId, "Sorry command was not recognized ");
            }
        } else if (update.hasCallbackQuery()) {
            String callBackData = update.getCallbackQuery().getData();
            long messageId = update.getCallbackQuery().getMessage().getMessageId();
            long chatId = update.getCallbackQuery().getMessage().getChatId();

            if (callBackData.equals( "YES_BUT")){
                String text = "You press YES button";
                EditMessageText message = new EditMessageText();
                message.setChatId(chatId);
                message.setText(text);
                message.setMessageId((int) messageId);
                try{
                    execute(message);
                }catch (TelegramApiException e){
                    log.error("Error Occurred : " + e);
                }
                
            } else if (callBackData.equals("NO_BUT")) {
                String text = "You press NO  button";
                EditMessageText message = new EditMessageText();
                message.setChatId(chatId);
                message.setText(text);
                message.setMessageId((int) messageId);
                try{
                    execute(message);
                }catch (TelegramApiException e){
                    log.error("Error Occurred : " + e);
                }
            }

        }
    }

    private void register(long chatId) {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText("Do you really want to register");

        InlineKeyboardMarkup markup = new InlineKeyboardMarkup();
        List<List<InlineKeyboardButton>> rowsLine = new ArrayList<>();
        List<InlineKeyboardButton> rowInLine = new ArrayList<>();
        var button = new InlineKeyboardButton();
        button.setText("Yes");
        button.setCallbackData("YES_BUT");

        var buttonNo = new InlineKeyboardButton();
        buttonNo.setText("No");
        buttonNo.setCallbackData("NO_BUT");

        rowInLine.add(button);
        rowInLine.add(buttonNo);

        rowsLine.add(rowInLine);

        markup.setKeyboard(rowsLine);
        message.setReplyMarkup(markup);

        try{
            execute(message);
        }catch (TelegramApiException e){
            log.error("Error Occurred : " + e);
        }
    }

    private void registerUser(Message msg) {
        if (userRepo.findById(msg.getChatId()).isEmpty()){

            var chatId = msg.getChatId();
            var chat = msg.getChat();
            User user = new User();
            user.setChatId(chatId);
            user.setFirstName(chat.getFirstName());
            user.setLastName(chat.getLastName());
            user.setUserName(chat.getUserName());
            user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));


            userRepo.save(user);

            log.info("UserSaved " + user);
        }

    }

    private void startCommandReceived(long chatId, String firstName){

        String answer = EmojiParser.parseToUnicode("Hi, " + firstName + ", nice to meet you!!" + ":blush:");
//            String answer = "Hi, " + firstName + ", nice to meet you!!";
            log.info("Replied to user " + firstName);

            sendMessage(chatId, answer);

    }
    private void sendMessage(long chatId, String textToSend)  {
        SendMessage message = new SendMessage();
        message.setChatId(String.valueOf(chatId));
        message.setText(textToSend);

        ReplyKeyboardMarkup keyboardMarkup = new ReplyKeyboardMarkup();

        List<KeyboardRow> keyboardRows = new ArrayList<>();
         KeyboardRow row = new KeyboardRow();
         row.add("weather");
         row.add("get random Joke");

         keyboardRows.add(row);

         row = new KeyboardRow();
         row.add("register");
         row.add("check my data");
         row.add("delete my data");

         keyboardRows.add(row);

         keyboardMarkup.setKeyboard(keyboardRows);

         message.setReplyMarkup(keyboardMarkup);



        try {
            execute(message);
        }
        catch (TelegramApiException e){
            log.error("Error occurred: " + e.getMessage());

        }

    }
}
