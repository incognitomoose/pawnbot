package net.shelg.pawnbot.triggers.commands

import net.shelg.pawnbot.TextSender

abstract class AbstractChatSubcommand(cliParser: CommandLineParser, textSender: TextSender)
    : AbstractCommand(cliParser, textSender)
