package commands.moderation;

import org.json.JSONObject;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
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
		JSONObject userconfig = ConfigLoader.run.getUserConfig(guild, cuser);
		if (!userconfig.getBoolean("muted") && !userconfig.getBoolean("tempmuted")) {
			event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/unmute:nomute").convert()).queue();
			return;
		}
		userconfig.put("muted", false);
		userconfig.put("tempmuted", false);
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