package commands.utilities;

import java.awt.Color;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

import commands.Command;
import components.base.AnswerEngine;
import components.base.ConfigLoader;
import components.base.assets.ConfigManager;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;

public class Suggest implements Command{

	@Override
	public void perform(SlashCommandInteractionEvent event) {
		final User user = event.getUser();
		final Guild guild = event.getGuild();
		Long channelid = ConfigLoader.getGuildConfig(guild).getLong("suggest");
		if (channelid == 0) {
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/utilities/suggest:nochannelset").convert()).queue();
			return;
		}
		OffsetDateTime lastsuggestion = OffsetDateTime.parse(ConfigLoader.getMemberConfig(guild, user).getString("lastsuggestion"), ConfigManager.dateTimeFormatter);
		if (Duration.between(lastsuggestion, OffsetDateTime.now()).toSeconds() < 300) {
			event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/utilities/suggest:nospam").convert()).queue();
			return;
		}
		this.sendsuggestion(guild, event.getMember(), event.getOption("suggestion").getAsString());
		event.replyEmbeds(AnswerEngine.fetchMessage(guild, user,"/commands/utilities/suggest:success").convert()).queue();
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("suggest", "Suggest an idea!")
										.addOptions(new OptionData(OptionType.STRING, "suggestion", "Write down your suggestions!", true));	
		return command;
	}

	@Override
	public String getHelp(Guild guild, User user) {
		return AnswerEngine.getRaw(guild, user, "/commands/utilities/suggest:help");
	}
	
	public void sendsuggestion(Guild guild, Member member, String idea) {
		TextChannel channel = guild.getTextChannelById(ConfigLoader.getGuildConfig(guild).getLong("suggest"));
		EmbedBuilder eb = new EmbedBuilder();
		DateTimeFormatter formatter = DateTimeFormatter.ofPattern("HH:mm | dd.MM.yyyy");
		eb.setAuthor(member.getEffectiveName(), null, member.getUser().getAvatarUrl());
		eb.setColor(Color.YELLOW);
		eb.setFooter(OffsetDateTime.now().format(formatter));
		eb.setDescription(idea);
		Message message = channel.sendMessageEmbeds(eb.build()).complete();
		message.addReaction("U+1F44D").queue();
		message.addReaction("U+1F44E").queue();
	}
}