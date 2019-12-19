package com.senderman.lastkatkabot

import com.annimon.tgbotsmodule.api.methods.Methods
import com.senderman.AbstractExecutorKeeper
import com.senderman.lastkatkabot.admincommands.*
import com.senderman.lastkatkabot.handlers.UsercommandsHandler
import com.senderman.lastkatkabot.tempobjects.BullsAndCowsGame
import com.senderman.lastkatkabot.tempobjects.Duel
import com.senderman.lastkatkabot.tempobjects.UserRow
import com.senderman.lastkatkabot.usercommands.*
import org.telegram.telegrambots.meta.api.objects.Message

internal class ExecutorKeeper constructor(private val handler: LastkatkaBotHandler) : AbstractExecutorKeeper() {

    private val usercommands = UsercommandsHandler(handler)

    init {
        registerCommands()
    }

    override fun registerCommands() {
        register(Action(handler))
        register(PayRespects(handler))
        register(Cake(handler))
        register(Help(handler, commandExecutors))
        register(Getinfo(handler))
        register(BncHelp(handler))

        // admin commands
        register(GoodNeko(handler))
        register(TransferStats(handler))
        register(Update(handler))
        register(CleanChats(handler))
        register(Announce(handler))
        register(SetupHelp(handler))
        register(Owners(handler))
        register(Prem(handler))
        register(Nekos(handler))
        register(Critical(handler))
        register(BadNeko(handler))
        register(AddPremium(handler))
        register(Owner(handler))
    }

    @Command(name = "/stats", desc = "статистика. Реплаем можно узнать статистику реплайнутого")
    fun stats(message: Message) = usercommands.stats(message)

    @Command(name = "/pinlist", desc = "ответьте этим на сообщение со списком игроков в верфульфа чтобы запинить его")
    fun pinlist(message: Message) = usercommands.pinList(message)

    @Command(name = "/weather", desc = "погода. Если не указать город, то покажет погоду в последнем введенном вами городе")
    fun weather(message: Message) = usercommands.weather(message)

    @Command(name = "/feedback", desc = "написать разрабу. Что написать, пишите через пробел. Или просто реплайните")
    fun feedback(message: Message) = usercommands.feedback(message)

    @Command(name = "/top", desc = "топ игроков в Быки и Коровы")
    fun bncTop(message: Message) = usercommands.bncTop(message)

    @Command(name = "/pair", desc = "пара дня")
    fun pair(message: Message) = usercommands.pair(message)

    @Command(name = "/lastpairs", desc = "последние 10 пар чата")
    fun lastpairs(message: Message) = usercommands.lastpairs(message)

    @Command(name = "/bnc", desc = "начать игру \"Быки и коровы\". Можно указать /bnc x, где x от 4 до 10 - длина числа")
    fun bnc(message: Message) {
        if (message.chatId !in handler.bullsAndCowsGames)
            handler.bullsAndCowsGames[message.chatId] = BullsAndCowsGame(message)
        else handler.sendMessage(message.chatId, "В этом чате игра уже идет!")
    }

    @Command(name = "/duel", desc = "начать дуэль (мини-игра на рандом)")
    fun duel(message: Message) {
        if (message.isUserMessage) return
        val duel = Duel(message)
        handler.duels[duel.duelId] = duel
    }

    @Command(name = "/bncinfo", desc = "информация о текущей игре")
    fun bncInfo(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.sendGameInfo(message)
    }

    @Command(name = "/bncstop", desc = "запустить опрос об остановке игры")
    fun bncStop(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.createStopPoll(message)
    }

    @Command(name = "/bncruin", desc = "вкл/выкл режим антируина (когда все цифры известны)")
    fun bncRuin(message: Message) {
        handler.bullsAndCowsGames[message.chatId]?.changeAntiRuin()
    }

    @Command(name = "/badneko", desc = "(reply) добавить юзера в чс бота", forAllAdmins = true)
    fun badneko(message: Message) = adminCommands.addUser(message, DBService.UserType.BLACKLIST)

    @Command(name = "/nekos", desc = "посмотреть чс бота. В лс работает как управление чс", forAllAdmins = true)
    fun nekos(message: Message) = adminCommands.listUsers(message, DBService.UserType.BLACKLIST)

    @Command(name = "/owners", desc = "управление/просмотр админами бота. Управление доступно только главному админу в лс", forAllAdmins = true)
    fun owners(message: Message) = adminCommands.listUsers(message, DBService.UserType.ADMINS)

    @Command(name = "/prem", desc = "управление/просмотр премиум-пользователями. Управление доступно только главному админу в лс", forAllAdmins = true)
    fun prem(message: Message) = adminCommands.listUsers(message, DBService.UserType.PREMIUM)

    @Command(name = "/owner", desc = "(reply) добавить админа бота", forMainAdmin = true)
    fun owner(message: Message) = adminCommands.addUser(message, DBService.UserType.ADMINS)

    @Command(name = "/addpremium", desc = "(reply) добавить премиум-пользователя", forMainAdmin = true)
    fun addPremium(message: Message) = adminCommands.addUser(message, DBService.UserType.PREMIUM)

    @Command(name = "/row", desc = "Рассчет юзеров, например няшек.\nСинтаксис: 1 строка - /row Список няшек\n" +
            "2 строка - няшка\n" +
            "3 строка - 5\n" +
            "(т.е. няшкой будет каждый пятый")
    fun row(message: Message) {
        if (!message.isGroupMessage && !message.isSuperGroupMessage) return
        try {
            handler.userRows[message.chatId] = UserRow(message)
        } catch (e: Exception) {
            handler.sendMessage(message.chatId, "Неверный формат!")
            return
        }
        Methods.deleteMessage(message.chatId, message.messageId).call(Services.handler)
    }

    @Command(name = "/getrow", desc = "Показать сообщение с рассчетом юзеров")
    fun getrow(message: Message) {
        if (!message.isGroupMessage && !message.isSuperGroupMessage) return
        if (message.chatId !in handler.userRows)
            handler.sendMessage(message.chatId, "У вас пока еще нет списка!")
        else
            handler.sendMessage(Methods.sendMessage(message.chatId, "Вот!")
                    .setReplyToMessageId(handler.userRows[message.chatId]?.messageId))
    }

    @Command(name = "/marryme", desc = "(reply) пожениться на ком-нибудь")
    fun marryme(message: Message) = usercommands.marryme(message)

    @Command(name = "/divorce", desc = "подать на развод")
    fun divorce(message: Message) = usercommands.divorce(message)

}
