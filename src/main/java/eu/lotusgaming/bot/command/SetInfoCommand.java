//Created by Chris Wille at 15.02.2024
package eu.lotusgaming.bot.command;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class SetInfoCommand extends ListenerAdapter{
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
		if(event.getName().equals("setinfo")) {
			if(event.getOption("channel") != null) {
				if(event.getOption("channel").getAsChannel().getType() == ChannelType.TEXT) {
					TextChannel infoChannel = event.getOption("channel").getAsChannel().asTextChannel();
					event.reply("Infos are sent into " + infoChannel.getAsMention()).queue();
					infoChannel.sendMessage(transform(info_block())).queue();
					info_blockSocialMedia(infoChannel, event.getGuild());
					info_blockTeam(infoChannel, event.getGuild());
				}else {
					event.deferReply(true).addContent("The Channel must be a text channel!").queue();
				}
			}
		}else if(event.getName().equals("updateinfo")) {
			String targetMessage = event.getOption("messageid").getAsString();
			if(!targetMessage.matches("^[0-9]-$")){
				event.deferReply(true).addContent("The Message ID must be a valid number!").queue();
				return;
			}
			if(event.getOption("channel").getAsChannel().getType() == ChannelType.TEXT) {
				TextChannel targetChannel = event.getOption("channel").getAsChannel().asTextChannel();
				info_updateBlockTeam(event.getGuild(), Long.parseLong(targetMessage), targetChannel);
			}else {
				event.deferReply(true).addContent("The Channel must be a text channel!").queue();
			}
		}
	}
	
	List<String> info_block() {
		List<String> info = new ArrayList<>();
		info.add("Question: How can I add my friends in here?\nAnswer: That's easy, just use this invite link: <https://discord.gg/7XZ2AR9A9z>");
		info.add("Question: Do you guys accept partnerships?\nAnswer: As of now, no, we don't. That might be a subject for later.\nStill want to give it a shot? Create a ticket in <#1203720766366027866> under the category **General Support**");
		info.add("Question: I want to apply! What do I have to do now?\nAnswer: Currently, we do recruit people, however we can do it only via ticket system. Create a ticket in <#1203720766366027866> under the category **General Support**");
		info.add("Question: Beta-Tester - I want to be one too!\nAnswer: Glad you'd like to help us, we however don't look for Beta Testers as of now.");
		return info;
	}
	
	void info_blockSocialMedia(TextChannel targetChannel, Guild guild) {
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.decode("#e53c50"));
		eb.setThumbnail(guild.getIconUrl());
		List<String> info = new ArrayList<>();
		info.add("<:facebook:1298613148689109042> [Lotus Gaming Community](https://www.facebook.com/profile.php?id=61567189271322)");
		info.add("<:instagram:1298613152866631680> [@lotuscommunityeu](https://www.instagram.com/lotuscommunityeu/)");
		info.add("<:x_twitter:1298613146537300009> [@lotusgamingeu](https://x.com/lotusgamingeu)");
		info.add("<:threads:1298613145035604059> [@lotuscommunityeu](https://www.threads.net/@lotuscommunityeu)");
		info.add("<:bsky:1363469765846503534> [@lotusgamingeu.bsky.social](https://bsky.app/profile/lotusgamingeu.bsky.social)");
		info.add("<:whatsapp:1403810214347800638> [Lotus Gaming Community WhatsApp Channel](https://whatsapp.com/channel/0029Vb9HjskKgsNkm3V06t2d)");
		StringBuilder sb = new StringBuilder();
		for(String s : info) {
			sb.append(s);
			sb.append("\n");
		}
		String result = sb.toString();
		eb.setDescription(result);
		eb.setTitle("Our Socials");
		targetChannel.sendMessageEmbeds(eb.build()).queue();
	}
	
	void info_blockTeam(TextChannel targetChannel, Guild guild) {
		List<Long> staffRoles = getAllStaffRoles();
		
		EmbedBuilder eb = new EmbedBuilder();
		eb.setColor(Color.decode("#e53c50"));
		eb.setThumbnail(guild.getIconUrl());
		eb.setTitle("Our Staff Team");
		for(long l : staffRoles) {
			Role role = guild.getRoleById(l);
			if(role != null) {
				List<Member> members = guild.getMembersWithRoles(role);
				if(!members.isEmpty()) {
					eb.addField(role.getName() + " (" + members.size() + ")", getFromList(members), false);
				}
			}
		}
		targetChannel.sendMessageEmbeds(eb.build()).queue();
	}
	
	void info_updateBlockTeam(Guild guild, long messageId, TextChannel targetChannel) {
		targetChannel.retrieveMessageById(messageId).queue(message -> {
			EmbedBuilder eb = new EmbedBuilder(message.getEmbeds().get(0));
			eb.clearFields();
			List<Long> staffRoles = getAllStaffRoles();
			
			for(long l : staffRoles) {
				Role role = guild.getRoleById(l);
				if(role != null) {
					List<Member> members = guild.getMembersWithRoles(role);
					if(!members.isEmpty()) {
						eb.addField(role.getName() + " (" + members.size() + ")", getFromList(members), false);
					}
				}
			}
			message.editMessageEmbeds(eb.build()).queue();
		});
    }
	
	List<Long> getAllStaffRoles() {
		List<Long> staffRoles = new ArrayList<>();
		staffRoles.add(1155572809356038154L); //project leader
		staffRoles.add(1155573840651485246L); //vice project leader
		staffRoles.add(1155573847270113321L); //human resources
		staffRoles.add(1155573844199870574L); //staff manager
		staffRoles.add(1412151205534240859L); //dev mgr
		staffRoles.add(1155573850776535061L); //quality assurance manager
		staffRoles.add(1412151327298949160L); //lfm mgr
		staffRoles.add(1301224974773653534L); //Ads, PR & Social Media Manager
		staffRoles.add(1208893851033407539L); //service data analyst
		staffRoles.add(1155573860343746630l); //staff supervisor
		//-- Upper Staff End / Staff Start --
		staffRoles.add(1155573857130905650L); //developer
		staffRoles.add(1201834197149552640L); //addon team
		staffRoles.add(1155573863015514153L); //administrator
		staffRoles.add(1155573867029467316L); //moderator
		staffRoles.add(1412151344739127406L); //lfm live presenter
		staffRoles.add(1412151350795440340L); //lfm dj
		staffRoles.add(1412151355614822520L); //lfm playlist curator
		staffRoles.add(1412151427631157360L); //lfm redactor
		staffRoles.add(1230974344277131396L); //social media team
		staffRoles.add(1155573869827072022L); //support
		staffRoles.add(1155573873216069632L); //translator
		staffRoles.add(1155573876579905696L); //designer
		staffRoles.add(1155573879964700802L); //builder
		staffRoles.add(1412152146014502972L); //event
		return staffRoles;
	}
	
	String getFromList(List<Member> input) {
		StringBuilder sb = new StringBuilder();
		for(Member member : input) {
			sb.append(member.getAsMention());
			sb.append("\n");
		}
		return sb.toString();
	}
	
	String transform(List<String> input) {
		StringBuilder sb = new StringBuilder();
		for(String s : input) {
			sb.append(s);
			sb.append("\n \n");
		}
		return sb.toString();
	}
}