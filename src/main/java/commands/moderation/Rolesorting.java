package commands.moderation;

import java.util.List;
import java.util.concurrent.TimeUnit;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import base.Bot;
import commands.Command;
import components.Developerlist;
import components.base.AnswerEngine;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.interaction.SlashCommandEvent;
import net.dv8tion.jda.api.events.message.guild.GuildMessageReceivedEvent;
import net.dv8tion.jda.api.interactions.commands.build.CommandData;

public class Rolesorting implements Command{
	
	private Role grouprole;
	private List<Role> subroles;
	private List<Member> members;
	private Bot bot;
	private Guild guild;
	private Member member;
	private TextChannel channel;
	
	@Override
	public void perform(SlashCommandEvent event) {
		guild = event.getGuild();
		member = event.getMember();
		if (!member.hasPermission(Permission.MANAGE_ROLES) && !Developerlist.getInstance().developers.contains(event.getMember().getId())) {
			event.replyEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:nopermission")).queue();
			return;
		}
		channel = event.getTextChannel();
		this.definegroup();
	}

	@Override
	public CommandData initialize() {
		CommandData command = new CommandData("rolesort", "Use this command to sort groups by grouping roles!");
		return command;
	}

	@Override
	public String getHelp() {
		return "Use this command to sort groups by grouping roles. This is fully customizable so you can even use it in another way!";
	}

	private void definegroup() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/rolesorting:definegroup")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {grouprole = e.getMessage().getMentionedRoles().get(0);
								  this.definesub();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(2);
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout")).queue();});
	}
	
	private void definesub() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:definesub")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {subroles = e.getMessage().getMentionedRoles();
								  this.definemember();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(4);
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout")).queue();});
	}

	private void definemember() {
		EventWaiter waiter = bot.getWaiter();
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:definemember")).queue();
		waiter.waitForEvent(GuildMessageReceivedEvent.class,
							e -> {if(!e.getChannel().getId().equals(channel.getId())) {return false;} 
							  	  return e.getAuthor().getIdLong() == member.getUser().getIdLong();},
							e -> {members = e.getMessage().getMentionedMembers();
								  this.rolesorter();},
							1, TimeUnit.MINUTES,
							() -> {this.cleanup(6);
								   channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:timeout")).queue();});
	}
	
	private void rolesorter() {
		for (int e = 0; e<members.size(); e++) {
			this.sorter(guild, members.get(e), subroles, grouprole);
		}
		this.cleanup(7);
		channel.sendMessageEmbeds(AnswerEngine.getInstance().fetchMessage("/commands/moderation/rolesorting:success")).queue();
	}
	
	public void sorter(Guild iguild, Member mb, List<Role> sr, Role gr) {
			int size = mb.getRoles().size();
			int match = 0;
			for (int i = 1; i < size; i++) {
				if (sr.contains(mb.getRoles().get(i))) {
					iguild.addRoleToMember(mb, gr).queue();
					match++;
				}
			}
			if (match == 0 && mb.getRoles().contains(gr)) {
				iguild.removeRoleFromMember(mb, gr).queue();
			}
	}
	
	private void cleanup(int i) {
		List<Message> messages = channel.getHistory().retrievePast(i).complete();
		channel.deleteMessages(messages).queue();
	}
}
