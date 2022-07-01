package context.moderation;

import java.util.concurrent.TimeUnit;

import components.ResponseDetector;
import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.ModController;
import components.context.UserContextEventHandler;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.UserContextInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.Command;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class TempMute implements UserContextEventHandler {

	@Override
	public void execute(UserContextInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		final User target = event.getTarget();
		event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "defdays").convert()).queue();
		ResponseDetector.waitForMessage(guild, user, event.getMessageChannel(),
				d -> {try {Integer.parseInt(d.getMessage().getContentRaw());
						 return true;
				      } catch (NumberFormatException ex) {return false;}},
				d -> {int days = Integer.parseInt(d.getMessage().getContentRaw());
					  this.tempmute(days, guild, target);
					  event.getMessageChannel().sendMessageEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success")
							 .replaceDescription("{user}", target.getAsMention())
							 .replaceDescription("{time}", String.valueOf(days)).convert()).queue();
				});
	}

	@Override
	public CommandData initialize() {
		CommandData context = Commands.context(Command.Type.USER, "TempMute");
		return context;
	}
	
	private void tempmute(int days, Guild guild, User user) {
		ConfigLoader.getMemberConfig(guild, user).put("tempmuted", true);
		guild.getMember(user).timeoutFor(days, TimeUnit.DAYS).queue();
		ModController.run.userModCheck(guild, user);
	}
}