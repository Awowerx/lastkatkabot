package com.senderman.lastkatkabot.Handlers;

import com.annimon.tgbotsmodule.api.methods.Methods;
import com.senderman.lastkatkabot.DBService;
import com.senderman.lastkatkabot.LastkatkaBot;
import com.senderman.lastkatkabot.LastkatkaBotHandler;
import com.senderman.lastkatkabot.Services;
import com.senderman.lastkatkabot.TempObjects.TgUser;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.meta.logging.BotLogger;

import java.util.ArrayList;
import java.util.List;

public class AdminHandler {

    private final LastkatkaBotHandler handler;

    public AdminHandler(LastkatkaBotHandler handler) {
        this.handler = handler;
    }

    public void addOwner(Message message) {
        if (!message.isReply())
            return;
        if (handler.admins.contains(message.getReplyToMessage().getFrom().getId()))
            return;

        Services.db().addTgUser(message.getReplyToMessage().getFrom().getId(),
                message.getReplyToMessage().getFrom().getFirstName(), DBService.COLLECTION_TYPE.ADMINS);
        handler.admins.add(message.getReplyToMessage().getFrom().getId());
        handler.sendMessage(message.getChatId(),
                String.format(Services.i18n().getString("ownerAdded", message), message.getReplyToMessage().getFrom().getFirstName()));
    }

    public void listOwners(Message message) {
        var locale = Services.i18n().getLocale(message);
        var ownersSet = Services.db().getTgUsers(DBService.COLLECTION_TYPE.ADMINS);
        var messageToSend = Methods.sendMessage().setChatId(message.getChatId());

        if (!message.getFrom().getId().equals(Services.botConfig().getMainAdmin()) || !message.isUserMessage()) {
            var adminList = new StringBuilder(Services.i18n().getString("adminList", locale) + "\n");
            for (TgUser owner : ownersSet) {
                adminList.append(owner.getLink()).append("\n");
            }
            messageToSend.setText(adminList.toString());
        } else {
            var markup = new InlineKeyboardMarkup();
            ArrayList<List<InlineKeyboardButton>> rows = new ArrayList<>();
            for (TgUser owner : ownersSet) {
                rows.add(List.of(new InlineKeyboardButton()
                        .setText(owner.getName())
                        .setCallbackData(LastkatkaBot.CALLBACK_DELETE_ADMIN + " " + owner.getId())));
            }
            rows.add(List.of(new InlineKeyboardButton()
                    .setText(Services.i18n().getString("closeMenu", locale))
                    .setCallbackData(LastkatkaBot.CALLBACK_CLOSE_MENU)));
            markup.setKeyboard(rows);
            messageToSend.setText(Services.i18n().getString("howToDeleteAdmin", locale))
                    .setReplyMarkup(markup);
        }
        handler.sendMessage(messageToSend);
    }

    public void badneko(Message message) {
        if (!message.isReply())
            return;
        if (handler.blacklist.contains(message.getReplyToMessage().getFrom().getId()))
            return;

        Services.db().addTgUser(message.getReplyToMessage().getFrom().getId(),
                message.getReplyToMessage().getFrom().getFirstName(), DBService.COLLECTION_TYPE.BLACKLIST);
        handler.blacklist.add(message.getReplyToMessage().getFrom().getId());
        handler.sendMessage(message.getChatId(),
                String.format(Services.i18n().getString("badneko", message), message.getReplyToMessage().getFrom().getUserName()));

    }

    public void addPremium(Message message) {
        if (!message.isReply())
            return;
        if (handler.premiumUsers.contains(message.getReplyToMessage().getFrom().getId()))
            return;

        Services.db().addTgUser(message.getReplyToMessage().getFrom().getId(),
                message.getReplyToMessage().getFrom().getFirstName(), DBService.COLLECTION_TYPE.PREMIUM);
        handler.premiumUsers.add(message.getReplyToMessage().getFrom().getId());
        handler.sendMessage(message.getChatId(),
                String.format(Services.i18n().getString("addPremium", message), message.getReplyToMessage().getFrom().getUserName()));
    }

    /* TODO public void listPremiums(Message message) {

    }*/

    // TODO remPremium in CallbackHandler

    public void goodneko(Message message) {
        if (!message.isReply())
            return;
        Services.db().removeTGUser(message.getReplyToMessage().getFrom().getId(), DBService.COLLECTION_TYPE.BLACKLIST);
        handler.blacklist.remove(message.getReplyToMessage().getFrom().getId());
        handler.sendMessage(message.getChatId(),
                String.format(Services.i18n().getString("goodneko", message), message.getReplyToMessage().getFrom().getUserName()));
    }

    public void nekos(Message message) {
        var badnekos = new StringBuilder().append(Services.i18n().getString("nekolist", message)).append("\n\n");
        var nekoSet = Services.db().getTgUsers(DBService.COLLECTION_TYPE.BLACKLIST);
        for (TgUser neko : nekoSet) {
            badnekos.append(neko.getLink()).append("\n");
        }
        handler.sendMessage(Methods.sendMessage(message.getChatId(), badnekos.toString())
                .disableNotification());
    }

    public void update(Message message) {
        var params = message.getText().split("\n");
        if (params.length < 2) {
            handler.sendMessage(message.getChatId(), Services.i18n().getString("argsError", message));
            return;
        }
        var update = new StringBuilder().append("\uD83D\uDCE3 <b>ВАЖНОЕ ОБНОВЛЕНИЕ:</b> \n\n");
        for (int i = 1; i < params.length; i++) {
            update.append("* ").append(params[i]).append("\n");
        }
        for (long chat : handler.allowedChats) {
            handler.sendMessage(chat, update.toString());
        }
    }

    public void chats(Message message) {
        var locale = Services.i18n().getLocale(message);
        if (!message.isUserMessage()) {
            handler.sendMessage(message.getChatId(), Services.i18n().getString("pmOnly", locale));
            return;
        }
        var markup = new InlineKeyboardMarkup();
        ArrayList<List<InlineKeyboardButton>> rows = new ArrayList<>();
        var chats = Services.db().getAllowedChats();
        for (long chatId : chats.keySet()) {
            rows.add(List.of(new InlineKeyboardButton()
                    .setText(chats.get(chatId))
                    .setCallbackData(LastkatkaBot.CALLBACK_DELETE_CHAT + " " + chatId)));
        }
        rows.add(List.of(new InlineKeyboardButton()
                .setText(Services.i18n().getString("closeMenu", locale))
                .setCallbackData(LastkatkaBot.CALLBACK_CLOSE_MENU)));
        markup.setKeyboard(rows);
        handler.sendMessage(Methods.sendMessage(message.getChatId(),
                Services.i18n().getString("howToDeleteChat", locale))
                .setReplyMarkup(markup));
    }

    public void announce(Message message) {
        var locale = Services.i18n().getLocale(message);
        handler.sendMessage(message.getChatId(), Services.i18n().getString("broadcastStarted", locale));
        var text = message.getText();
        text = "\uD83D\uDCE3 <b>Объявление</b>\n\n" + text.split("\\s+", 2)[1];
        var usersIds = Services.db().getAllUsersIds();
        var counter = 0;
        for (var userId : usersIds) {
            try {
                handler.execute(new SendMessage((long) userId, text).enableHtml(true));
                counter++;
            } catch (TelegramApiException e) {
                BotLogger.error("ANNOUNCE", e.toString());
            }
        }
        handler.sendMessage(message.getChatId(),
                String.format(Services.i18n().getString("broadcastResult", locale), counter, usersIds.size()));
    }

    public void setupHelp(Message message) {
        handler.sendMessage(Methods.sendMessage(message.getChatId(), Services.i18n().getString("setupHelp", message)));
    }
}
