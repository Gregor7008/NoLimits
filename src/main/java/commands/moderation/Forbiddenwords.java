package commands.moderation;

import commands.Command;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class Forbiddenwords implements Command{

	@Override
	public void perform(SlashCommandEvent event) {
		Guild guild = event.getGuild();
		User user = event.getUser();
		if (!event.getMember().hasPermission(Permission.MANAGE_SERVER)) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/forbiddenwords:nopermission")).queue();
			return;
		}
		switch (event.getSubcommandName()) {
		case "add":
			String rawadd = event.getOption("words").getAsString();
			String[] splitadd = rawadd.split(";\\s");
			for (int i = 0; i < splitadd.length; i++) {
				Configloader.INSTANCE.addGuildConfig(guild, "forbidden", splitadd[i]);
			}
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/forbiddenwords:addsuccess")).queue();
			break;
		case "remove":
			String rawremove = event.getOption("words").getAsString();
			String[] splitremove = rawremove.split(";\\s");
			for (int i = 0; i < splitremove.length; i++) {
				Configloader.INSTANCE.deleteGuildConfig(guild, "forbidden", splitremove[i]);
			}
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/forbiddenwords:removesuccess")).queue();
			break;
		case "set":
			String rawset = event.getOption("words").getAsString();
			String finalset = rawset.replaceAll(";\\s", ";");
			Configloader.INSTANCE.setGuildConfig(guild, "forbidden", finalset);
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/forbiddenwords:setsuccess")).queue();
			break;
		case "clear":
			Configloader.INSTANCE.setGuildConfig(guild, "forbidden", "");
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/forbiddenwords:clearsuccess")).queue();
			break;
		case "list":
			String rawlist = Configloader.INSTANCE.getGuildConfig(guild, "forbidden");
			String finallist = rawlist.replaceAll(";", ", ");
			String title = AnswerEngine.getInstance().getTitle(guild, user, "/commands/moderation/forbiddenwords:list");
			String description = AnswerEngine.getInstance().getDescription(guild, user, "/commands/moderation/forbiddenwords:list");
			event.replyEmbeds(AnswerEngine.getInstance().buildMessage(title, description + finallist));
			break;
		default:
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage(guild, user, "/commands/moderation/forbiddenwords:error")).queue() ;
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("forbiddenwords", "Configures the list of forbidden words for your guild")
									.addSubcommands(new SubcommandData("add", "Adds words"))
										.addOptions(new OptionData(OptionType.STRING, "words", "The words that should be added", true))
									.addSubcommands(new SubcommandData("remove", "Adds words"))
										.addOptions(new OptionData(OptionType.STRING, "words", "The words that should be removed", true))
									.addSubcommands(new SubcommandData("set", "Adds words"))
										.addOptions(new OptionData(OptionType.STRING, "words", "The words that should be set", true))
									.addSubcommands(new SubcommandData("clear", "Adds words"))
									.addSubcommands(new SubcommandData("list", "Adds words"));
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getInstance().getRaw(guild, user, "/commands/moderation/forbiddenwords:help");
	}

}
