package com.senderman.lastkatkabot.admincommands

import com.senderman.CommandExecutor
import com.senderman.lastkatkabot.LastkatkaBotHandler
import com.senderman.lastkatkabot.Services
import org.telegram.telegrambots.meta.api.objects.Message

class SetupHelp constructor(private val handler: LastkatkaBotHandler) : CommandExecutor {

    override val forAllAdmins: Boolean
        get() = true;
    override val command: String
        get() = "/setuphelp"
    override val desc: String
        get() = "инфо о команде /setup"


    override fun execute(message: Message) {
        handler.sendMessage(message.chatId, Services.botConfig.setupHelp)
    }
}


        
