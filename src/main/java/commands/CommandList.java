package commands;

import java.util.concurrent.ConcurrentHashMap;

import commands.moderation.Autopunish;
import commands.moderation.Autorole;
import commands.moderation.Botautorole;
import commands.moderation.Clear;
import commands.moderation.Close;
import commands.moderation.Goodbye;
import commands.moderation.Ignorechannel;
import commands.moderation.Join2Create;
import commands.moderation.Levelreward;
import commands.moderation.Mute;
import commands.moderation.Rolecheck;
import commands.moderation.Rolesorting;
import commands.moderation.Setlevelchannel;
import commands.moderation.Setmodrole;
import commands.moderation.Setreportchannel;
import commands.moderation.Setsuggestionchannel;
import commands.moderation.Tempban;
import commands.moderation.Tempmute;
import commands.moderation.Warning;
import commands.moderation.Welcome;
import commands.music.Nowplaying;
import commands.music.Play;
import commands.music.Queue;
import commands.music.Skip;
import commands.music.Stop;
import commands.utilities.Embed;
import commands.utilities.Help;
import commands.utilities.Level;
import commands.utilities.Levelbackground;
import commands.utilities.Poll;
import commands.utilities.Report;
import commands.utilities.Serverinfo;
import commands.utilities.Suggest;
import commands.utilities.Userinfo;
import components.utilities.Test;

public class CommandList {
	
	public ConcurrentHashMap<String, Command> utilitycmds = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, Command> moderationcmds = new ConcurrentHashMap<>();
	public ConcurrentHashMap<String, Command> musiccmds = new ConcurrentHashMap<>();
	
	public CommandList() {
		//Utilities
		this.utilitycmds.put("embed", new Embed());
		this.utilitycmds.put("help", new Help());
		this.utilitycmds.put("level", new Level());
		this.utilitycmds.put("levelbackground", new Levelbackground());
		this.utilitycmds.put("suggest", new Suggest());
		this.utilitycmds.put("userinfo", new Userinfo());
		this.utilitycmds.put("poll", new Poll());
		this.utilitycmds.put("report", new Report());
		this.utilitycmds.put("serverinfo", new Serverinfo());
		//this.utilitycommands.put("setlanguage", new Setlanguage());
		
		//Moderation
		this.moderationcmds.put("autopunish", new Autopunish());
		this.moderationcmds.put("autorole", new Autorole());
		this.moderationcmds.put("botautorole", new Botautorole());
		this.moderationcmds.put("clear", new Clear());
		this.moderationcmds.put("goodbye", new Goodbye());
		this.moderationcmds.put("join2create", new Join2Create());
		this.moderationcmds.put("levelreward", new Levelreward());
		this.moderationcmds.put("mute", new Mute());
		this.moderationcmds.put("rolecheck", new Rolecheck());
		this.moderationcmds.put("rolesort", new Rolesorting());
		this.moderationcmds.put("tempban", new Tempban());
		this.moderationcmds.put("tempmute", new Tempmute());
		this.moderationcmds.put("warning", new Warning());
		this.moderationcmds.put("welcome", new Welcome());
		this.moderationcmds.put("close", new Close());
		this.moderationcmds.put("setsuggestionchannel", new Setsuggestionchannel());
		this.moderationcmds.put("setlevelchannel", new Setlevelchannel());
		this.moderationcmds.put("ignorechannel", new Ignorechannel());
		this.moderationcmds.put("setmodrole", new Setmodrole());
		this.moderationcmds.put("setreportchannel", new Setreportchannel());
		//Developement
		this.moderationcmds.put("test", new Test());
		
		//Music
		this.musiccmds.put("nowplaying", new Nowplaying());
		this.musiccmds.put("play", new Play());
		this.musiccmds.put("queue", new Queue());
		this.musiccmds.put("skip", new Skip());
		this.musiccmds.put("stop", new Stop());
		
		
	}

}
