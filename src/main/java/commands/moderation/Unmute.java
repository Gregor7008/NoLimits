package commands.moderation;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Unmute implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user =  event.getUser();
		final User cuser = event.getOption("user").getAsUser();
		if (!event.getMember().hasPermission(Permission.MESSAGE_MANAGE)) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/unmute:nopermission").convert()).queue();
			return;
		}
		if (!Boolean.parseBoolean(Configloader.INSTANCE.getUserConfig(guild, cuser, "muted"))) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/unmute:nomute").convert()).queue();
			return;
		}
		Configloader.INSTANCE.setUserConfig(guild, cuser, "muted", "false");
		Configloader.INSTANCE.setUserConfig(guild, cuser, "tempmuted", "false");
		Configloader.INSTANCE.setUserConfig(guild, cuser, "tmuntil", "");
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/unmute:success").convert()).queue();
		Bot.INSTANCE.modCheck(guild);
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("unmute", "Unmute a user").addOption(OptionType.USER, "user", "The user that should be unmuted", true);
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/unmute:help");
	}
}