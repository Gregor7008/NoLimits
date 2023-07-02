package engines.functions;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import base.Bot;
import engines.base.CentralTimer;
import engines.data.ConfigLoader;
import engines.data.ConfigManager;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.User;

public class ModController {
	
	public static ModController RUN;

	public ModController() {
		RUN = this;
		CentralTimer.get().schedule(() -> {
			List<Guild> guilds = Bot.getAPI().getGuilds();
			for (int i = 0; i < guilds.size(); i++) {
				guildModCheck(guilds.get(i));
			}
		}, TimeUnit.MINUTES, 5, TimeUnit.MINUTES, 5);
	}
	
	public void guildModCheck(Guild guild) {
		new Thread(() -> {
			ConcurrentHashMap<Long, JSONObject> usersCached = ConfigLoader.INSTANCE.manager.getUserCache();
			usersCached.forEach((id, obj) -> {
				User user = Bot.getAPI().retrieveUserById(id).complete();
				if (user != null) {
				    this.userModCheck(guild, user);
				}
			});
		}).start();
	}
	
	public void userModCheck(Guild guild, User user) {
		guild.retrieveMember(user).queue(member -> {
	        JSONObject memberConfig = ConfigLoader.INSTANCE.getMemberConfig(guild, user);
	        if (!user.isBot() && member != null) {
	            if (ConfigLoader.INSTANCE.getMemberConfig(guild, user).getBoolean("tempbanned")) {
	                OffsetDateTime tbuntil = OffsetDateTime.parse(memberConfig.getString("tempbanneduntil"), ConfigManager.DATA_TIME_SAVE_FORMAT);
	                OffsetDateTime now = OffsetDateTime.now();
	                long difference = Duration.between(now, tbuntil).toSeconds();
	                if (difference <= 0) {
	                    guild.unban(user).queue();
	                    memberConfig.put("tempbanned", false);
	                    memberConfig.put("tempbanneduntil", "");
	                }
	            }
	        }
		}, error -> {});
	}
	
	public void guildPenaltyCheck(Guild guild) {
		new Thread(() -> {
			List<Member> members = guild.loadMembers().get();
			for (int e = 0; e < members.size(); e++) {
				Member member = members.get(e);
				if (!member.getUser().isBot()) {
					this.userPenaltyCheck(guild, member);
				}
			}
		}).start();
	}
	
	public void userPenaltyCheck(Guild guild, Member member) {
		User user = member.getUser();
		JSONObject penalties = ConfigLoader.INSTANCE.getGuildConfig(guild).getJSONObject("penalties");
		if (penalties.isEmpty()) {
			return;
		}
		int warningcount = ConfigLoader.INSTANCE.getMemberConfig(guild, user).getJSONArray("warnings").length();
		boolean error = false;
		for (int a = warningcount; a > 0; a--) {
			try {
				penalties.getJSONArray(String.valueOf(a));
				warningcount = a;
				a = 0;
			} catch (JSONException ex) {
				error = true;
			}
		}
		if (!error && ConfigLoader.INSTANCE.getMemberConfig(guild, user).getInt("penaltycount") < warningcount) {
			JSONArray penalty = penalties.getJSONArray(String.valueOf(warningcount));
			ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("penaltycount", warningcount);
			//go through penaltys
			switch(penalty.getString(0)) {
				case ("rr"):
					guild.removeRoleFromMember(member, guild.getRoleById(penalty.getString(1)));
					ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("experience", 0);
					ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("level", 0);
					break;
				case ("tm"):
					ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("tempmuted", true);
					guild.getMember(user).timeoutFor(Integer.valueOf(penalty.getString(1)), TimeUnit.DAYS).queue();
					break;
				case ("pm"):
					ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("muted", true);
					ModController.RUN.guildModCheck(guild);
					break;
				case ("kk"):
					member.kick().queue();
					break;
				case ("tb"):
					OffsetDateTime until = OffsetDateTime.now().plusDays(Integer.valueOf(penalty.getString(1)));
					ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("tempbanneduntil", until.format(ConfigManager.DATA_TIME_SAVE_FORMAT));
					ConfigLoader.INSTANCE.getMemberConfig(guild, user).put("tempbanned", true);
					guild.getMember(user).ban(0, TimeUnit.DAYS).reason("Too many warnings").queue();
					this.userModCheck(guild, user);
					break;
				case ("pb"):
					member.ban(0, TimeUnit.DAYS).reason("Too many warnings").queue();
					break;
				default:
					return;
			}
		}
	}
}