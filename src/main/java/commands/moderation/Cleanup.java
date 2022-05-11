package commands.moderation;

import java.io.File;

import base.Bot;
import commands.Command;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Cleanup implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		File guilddir = new File(Bot.environment + "/configs/user/" + guild.getId());
		File[] ufiles = guilddir.listFiles();
		for (int i = 0; i < ufiles.length; i++) {
			String[] name = ufiles[i].getName().split(".properties");
			if (event.getGuild().getMemberById(name[0]) == null) {
				ufiles[i].delete();
			}
		}
		event.replyEmbeds(AnswerEngine.ae.fetchMessage(guild, user, "/commands/moderation/cleanup:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("cleanup", "Deletes all progress of left members");
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.ae.getRaw(guild, user, "/commands/moderation/cleanup:help");
	}
}