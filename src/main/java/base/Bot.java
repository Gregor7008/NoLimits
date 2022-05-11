package base;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import javax.security.auth.login.LoginException;

import com.jagrosh.jdautilities.commons.waiter.EventWaiter;

import components.base.AnswerEngine;
import components.base.Configcheck;
import components.base.Configloader;
import components.base.ConsoleEngine;
import components.moderation.ModEngine;
import components.moderation.PenaltyEngine;
import components.moderation.ServerUtilities;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Bot {
	
	public static Bot INSTANCE;
	
	public JDA jda;
	private EventWaiter eventWaiter = new EventWaiter();
	private PenaltyEngine penaltyEngine;
	private ModEngine modEngine;
	public ConsoleEngine consoleEngine;
	private Timer timer = new Timer();
	public static String token, environment, homeID;
	
	public static void main(String[] args) {
		if (args.length <= 0) {
			System.out.println("You have to provide 1. a bot token, 2. the path to my resource folder!");
			System.exit(0);
		}
		token = args[0];
		environment = args[1];
		homeID = "708381749826289666";
		try {
			new Bot(token);
		} catch (LoginException | InterruptedException | IOException ex) {
			ex.printStackTrace();
		}
	}
	
	private Bot(String token) throws LoginException, InterruptedException, IOException {
		INSTANCE = this;
		JDABuilder builder = JDABuilder.createDefault(token);
		builder.addEventListeners(eventWaiter);
		builder.addEventListeners(new Processor());
		builder.setRawEventsEnabled(true);
		builder.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.GUILD_VOICE_STATES, GatewayIntent.GUILD_PRESENCES);
		builder.setMemberCachePolicy(MemberCachePolicy.ALL);
    	jda = builder.build().awaitReady();
		jda.getPresence().setStatus(OnlineStatus.ONLINE);	    
	    jda.getPresence().setActivity(Activity.playing("V1.3-beta"));
	    penaltyEngine = new PenaltyEngine();
	    modEngine = new ModEngine();
	    consoleEngine = new ConsoleEngine();
	    new ServerUtilities().controlChannels(true);
	    new AnswerEngine();
    	this.checkConfigs();
    	this.startTimer();
	}
	
	public void shutdown(Boolean delete) {
		new ServerUtilities().controlChannels(false);
		List<Guild> guilds = jda.getGuilds();
		for (int i = 0; i < guilds.size(); i++) {
    		Guild guild = guilds.get(i);
    		if (delete) {
    			String j2csraw = Configloader.INSTANCE.getGuildConfig(guild, "j2cs");
    			if (!j2csraw.equals("")) {
    				String[] j2cs = j2csraw.split(";");
    				for (int e = 0; e < j2cs.length; e++) {
        				String[] temp1 = j2cs[e].split("-");
        				guild.getVoiceChannelById(temp1[0]).delete().queue();
        			}
    			}
    			if (!Configloader.INSTANCE.getGuildConfig(guild, "levelmsgch").equals("")) {
    				String cid = Configloader.INSTANCE.getGuildConfig(guild, "levelmsgch");
    				String msgid = guild.getTextChannelById(cid).sendMessageEmbeds(AnswerEngine.ae.fetchMessage(guild, null, "/base/bot:offline").convert()).complete().getId();
        			Configloader.INSTANCE.setGuildConfig(guild, "offlinemsg", cid + "_" + msgid);
        		}
    		}
    		if (!Configloader.INSTANCE.getGuildConfig(guild, "supportchat").equals("")) {
    			guild.getTextChannelById(Configloader.INSTANCE.getGuildConfig(guild, "supportchat")).upsertPermissionOverride(guild.getPublicRole()).deny(Permission.VIEW_CHANNEL).queue();
    		}
    		new File(Bot.environment + "/levelcards/cache/temp.png").delete();
    		new File(Bot.environment + "/levelcards/cache/avatar.png").delete();
    	}
		jda.getPresence().setStatus(OnlineStatus.OFFLINE);
		jda.shutdown();
		consoleEngine.info(this, "Bot offline");
		this.wait(2000);
		System.exit(0);
	}
	
	private void checkConfigs() {
		for (int i = 0; i < jda.getGuilds().size(); i++) {
    		Guild guild = jda.getGuilds().get(i);
    		Configcheck.INSTANCE.checkGuildConfigs(guild);
    		Configcheck.INSTANCE.checkUserConfigs(guild);
		}
	}
	
	private void startTimer() {
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				List<Guild> guilds = jda.getGuilds();
				for (int i = 0; i < guilds.size(); i++) {
					Guild guild = guilds.get(i);
					Bot.INSTANCE.penaltyCheck(guild);
					Bot.INSTANCE.modCheck(guild);
				}
			}
		}, 0, 5*60*1000);
	}
	
	private void wait(int time) {
		try {
			Thread.sleep(time);
		} catch (InterruptedException e) {}
	}
	
	public EventWaiter getWaiter() {
		return eventWaiter;
	}
	
	public void penaltyCheck(Guild guild) {
		new Thread(() -> {
			this.penaltyEngine.run(guild);
		}).start();
	}
	
	public void modCheck(Guild guild) {
		new Thread(() -> {
			this.modEngine.run(guild);
		}).start();
	}
}