package commands.moderation;

import java.util.ArrayList;
import java.util.List;

import components.base.ConfigLoader;
import components.base.LanguageEngine;
import components.commands.CommandEventHandler;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;
import net.dv8tion.jda.api.interactions.commands.build.Commands;

public class Move implements CommandEventHandler {

	@Override
	public void execute(SlashCommandInteractionEvent event) {
		final Guild guild = event.getGuild();
		final User user = event.getUser();
		if (ConfigLoader.getGuildConfig(guild).getLong("supporttalk") == 0) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nochannel").convert()).queue();
			return;
		}
		if (!guild.getMember(event.getOption("member").getAsUser()).getVoiceState().inAudioChannel()) {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "memnotconn").convert()).queue();
			return;
		}
		long vcid = ConfigLoader.getGuildConfig(guild).getLong("supporttalk");
		VoiceChannel st = guild.getVoiceChannelById(vcid);
		if (st == null) {
			ConfigLoader.getGuildConfig(guild).put("supporttalk", 0L);
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "nochannel").convert()).queue();
			return;
		}
		if (st.getMembers().contains(event.getMember())) {
			guild.moveVoiceMember(guild.getMember(event.getOption("member").getAsUser()), st).queue();
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "success").convert()).queue();
		} else {
			event.replyEmbeds(LanguageEngine.fetchMessage(guild, user, this, "notconnected").convert()).queue();
		}
	}

	@Override
	public CommandData initialize() {
		CommandData command = Commands.slash("move", "Move a member into the support talk")
									  .addOption(OptionType.USER, "member", "The member you want to move", true);
		command.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_MOVE_OTHERS))
		   	   .setGuildOnly(true);
		return command;
	}
	
	@Override
	public List<Role> additionalWhitelistedRoles(Guild guild) {
		List<Role> roles = new ArrayList<>();
		Role supportrole = guild.getRoleById(ConfigLoader.getGuildConfig(guild).getLong("supportrole"));
		roles.add(supportrole);
		return roles;
	}
}