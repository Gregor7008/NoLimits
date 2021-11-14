package components.moderation;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Random;

import base.Bot;
import components.base.AnswerEngine;
import components.base.Configloader;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.priv.PrivateMessageReceivedEvent;

public class ModMail {

	public ModMail(PrivateMessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		if (event.getAuthor().isBot()) {
			return;
		}
		if (guild.retrieveMember(event.getAuthor()).complete() == null) {
			event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().buildMessage("No support!", "As you are not a member of the NoLimits Server, we aren't able to provide you any support."
					+ "\nWe hope for your understanding.\nYou may also join us with the link at the end of this message!")).queue();
			return;
		}
		if (Configloader.INSTANCE.getMailConfig2(event.getAuthor().getId()) != null) {
			TextChannel channel = guild.getTextChannelsByName(Configloader.INSTANCE.getMailConfig2(event.getAuthor().getId()), true).get(0);
			channel.sendMessage(event.getMessage().getContentRaw()).queue();
			return;
		}
		if (!event.getMessage().getContentRaw().contains("anonym")) {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 300) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, event.getAuthor(), "/components/moderation/modmail:success")).queue();
				Configloader.INSTANCE.setUserConfig(guild.retrieveMember(event.getAuthor()).complete(), "lastmail", OffsetDateTime.now().toString());
				this.processMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(
						AnswerEngine.getInstance().getTitle(guild, event.getAuthor(), "/components/moderation/modmail:timelimit"),
						AnswerEngine.getInstance().getDescription(guild, event.getAuthor(),"/components/moderation/modmail:timelimit").replace("{timeleft}", String.valueOf(timeleft)))).queue();
			}
		} else {
			OffsetDateTime lastmail = OffsetDateTime.parse(Configloader.INSTANCE.getUserConfig(guild, event.getAuthor(), "lastmail"));
			if (Duration.between(lastmail, OffsetDateTime.now()).toSeconds() > 1) {
				event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage(guild, event.getAuthor(), "/components/moderation/modmail:successanonym")).queue();
				Configloader.INSTANCE.setUserConfig(guild.getMember(event.getAuthor()), "lastmail", OffsetDateTime.now().toString());
				this.processAnonymousMessage(event);
			} else {
				int timeleft = (int) (300 - Duration.between(lastmail, OffsetDateTime.now()).toSeconds());
				event.getChannel().sendMessageEmbeds(AnswerEngine.getInstance().buildMessage(
						AnswerEngine.getInstance().getTitle(guild, event.getAuthor(), "/components/moderation/modmail:timelimit"),
						AnswerEngine.getInstance().getDescription(guild, event.getAuthor(),"/components/moderation/modmail:timelimit").replace("{timeleft}", String.valueOf(timeleft)))).queue();
			}
		}
	}
	
	private void processMessage(PrivateMessageReceivedEvent event) {
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		TextChannel nc = guild.createTextChannel(event.getAuthor().getName().toLowerCase(), guild.getCategoryById("896011407303270402")).complete();
		nc.sendMessage(event.getMessage().getContentRaw()).queue();
		Configloader.INSTANCE.setMailConfig(String.valueOf(event.getAuthor().getName().toLowerCase()), event.getAuthor().getId());
	}
	
	private void processAnonymousMessage(PrivateMessageReceivedEvent event) {
		Random rng = new Random();
		Guild guild = Bot.INSTANCE.jda.getGuildById(Bot.INSTANCE.getBotConfig("NoLiID"));
		int rn = rng.nextInt(100);
		while (Configloader.INSTANCE.getMailConfig1(String.valueOf(rn)) != null) {
			rn = rng.nextInt(100);
		}
		TextChannel nc = guild.createTextChannel(String.valueOf(rn), guild.getCategoryById("896011407303270402")).complete();
		String message = event.getMessage().getContentDisplay().replaceAll("#anonymous", "");
		nc.sendMessage(message).queue();
		Configloader.INSTANCE.setMailConfig(String.valueOf(rn), event.getAuthor().getId());
	}
}
