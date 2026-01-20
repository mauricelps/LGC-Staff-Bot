//Created by Maurice H. at 28.11.2024
package eu.lotusgaming.bot.handlers;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;

import javax.imageio.ImageIO;

import org.simpleyaml.configuration.file.YamlFile;

import eu.lotusgaming.bot.main.LotusManager;
import eu.lotusgaming.bot.main.Main;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.utils.FileUpload;

public class XMAS_NEWYEARScheduler extends TimerTask {
	
	JDA jda;

	public XMAS_NEWYEARScheduler(JDA jda) {
		this.jda = jda;
	}
	
	int width = 384;
	int height = 192;
	
	File runFile = new File(LotusManager.configFolderName + "/poster.yml");
	
	static boolean inited = false;

	@Override
	public void run() {
		if(!inited) {
			inited = true;
			initFile();
			Main.logger.info("New Year / XMAS Scheduler started.");
		}
		
		Guild guild = jda.getGuildById(1153419306789507125L);
		if (guild == null) {
			Main.logger.warning("Guild 1153419306789507125 not found or bot not connected; skipping scheduler run.");
			return;
		}
		TextChannel channel = guild.getTextChannelById(1201229752992809040L);
		if (channel == null) {
			Main.logger.warning("TextChannel 1201229752992809040 not found; skipping scheduler run.");
			return;
		}
		
		String time = new SimpleDateFormat("HH:mm:ss").format(new Date());
		String date = new SimpleDateFormat("dd.MM").format(new Date());
		
		File file = null;
		String text = "";
		
		if(!hasDayRun(Integer.parseInt(date.split("\\.")[0]))) {
			switch(date) {
			case "01.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas1.png"); text = "23 Days";
                        }
			case "02.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas4.png"); text = "22 Days";
                        }
			case "03.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas2.png"); text = "21 Days";
                        }
			case "04.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas1.png"); text = "20 Days";
                        }
			case "05.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas2.png"); text = "19 Days";
                        }
			case "06.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas4.png"); text = "18 Days";
                        }
			case "07.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas1.png"); text = "17 Days";
                        }
			case "08.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas3.png"); text = "16 Days";
                        }
			case "09.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas5.png"); text = "15 Days";
                        }
			case "10.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas4.png"); text = "14 Days";
                        }
			case "11.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas6.png"); text = "13 Days";
                        }
			case "12.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas2.png"); text = "12 Days";
                        }
			case "13.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas3.png"); text = "11 Days";
                        }
			case "14.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas1.png"); text = "10 Days";
                        }
			case "15.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas6.png"); text = "9 Days";
                        }
			case "16.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas5.png"); text = "8 Days";
                        }
			case "17.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas4.png"); text = "7 Days";
                        }
			case "18.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas1.png"); text = "6 Days";
                        }
			case "19.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas4.png"); text = "5 Days";
                        }
			case "20.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas5.png"); text = "4 Days";
                        }
			case "21.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas6.png"); text = "3 Days";
                        }
			case "22.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas3.png"); text = "2 Days";
                        }
			case "23.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas1.png"); text = "1 Day";
                        }
			case "24.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/christmas5.png"); text = "Merry Christmas!";
                        }
			case "25.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk1.png"); text = "6 Days";
                        }
			case "26.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk2.png"); text = "5 Days";
                        }
			case "27.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk3.png"); text = "4 Days";
                        }
			case "28.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk4.png"); text = "3 Days";
                        }
			case "29.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk5.png"); text = "2 Days";
                        }
			case "30.12" -> {
                            file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk6.png"); text = "1 Day";
                        }
			}
			setDayAsRun(Integer.parseInt(date.split("\\.")[0]));
			if (file != null) {
				File fileEdit = editFile(file, text);
				if (fileEdit != null) {
					channel.sendFiles(FileUpload.fromData(fileEdit)).queue();
					fileEdit.delete();
				}
			}
		}

		if(date.matches("31.12")) {
			switch(time) {
			case "00:00:10" -> channel.sendMessage("24 Hours until new year!").queue();
			case "01:00:00" -> channel.sendMessage("23 Hours until new year!").queue();
			case "02:00:00" -> channel.sendMessage("22 Hours until new year!").queue();
			case "02:59:30" -> channel.sendMessage("21 Hours until new year!").queue();
			case "04:00:00" -> channel.sendMessage("20 Hours until new year!").queue();
			case "05:00:00" -> channel.sendMessage("19 Hours until new year!").queue();
			case "06:00:00" -> channel.sendMessage("18 Hours until new year!").queue();
			case "07:00:00" -> channel.sendMessage("17 Hours until new year!").queue();
			case "08:00:00" -> channel.sendMessage("16 Hours until new year!").queue();
			case "09:00:00" -> channel.sendMessage("15 Hours until new year!").queue();
			case "10:00:00" -> channel.sendMessage("14 Hours until new year!").queue();
			case "11:00:00" -> channel.sendMessage("13 Hours until new year!").queue();
			case "12:00:00" -> channel.sendMessage("12 Hours until new year!").queue();
			case "13:00:00" -> channel.sendMessage("11 Hours until new year!").queue();
			case "14:00:00" -> channel.sendMessage("10 Hours until new year!").queue();
			case "15:00:00" -> channel.sendMessage("9 Hours until new year!").queue();
			case "16:00:00" -> channel.sendMessage("8 Hours until new year!").queue();
			case "17:00:00" -> channel.sendMessage("7 Hours until new year!").queue();
			case "18:00:00" -> channel.sendMessage("6 Hours until new year!").queue();
			case "19:00:00" -> channel.sendMessage("5 Hours until new year!").queue();
			case "20:00:00" -> channel.sendMessage("4 Hours until new year!").queue();
			case "21:00:00" -> channel.sendMessage("3 Hours until new year!").queue();
			case "22:00:00" -> channel.sendMessage("2 Hours until new year!").queue();
			case "23:00:00" -> channel.sendMessage("1 Hour until new year!").queue();
			case "23:15:00" -> channel.sendMessage("45 Minutes until new year!").queue();
			case "23:30:00" -> channel.sendMessage("30 Minutes until new year!").queue();
			case "23:45:00" -> channel.sendMessage("15 Minutes until new year!").queue();
			case "23:50:00" -> channel.sendMessage("10 Minutes until new year!").queue();
			case "23:55:00" -> channel.sendMessage("5 Minutes until new year!").queue();
			case "23:56:00" -> channel.sendMessage("4 Minutes until new year!").queue();
			case "23:57:00" -> channel.sendMessage("3 Minutes until new year!").queue();
			case "23:58:00" -> channel.sendMessage("2 Minutes until new year!").queue();
			case "23:59:00" -> channel.sendMessage("1 Minute until new year!").queue();
			case "23:59:15" -> channel.sendMessage("45 Seconds until new year!").queue();
			case "23:59:30" -> channel.sendMessage("30 Seconds until new year!").queue();
			case "23:59:45" -> channel.sendMessage("15 Seconds until new year!").queue();
			case "23:59:50" -> channel.sendMessage("10 Seconds until new year!").queue();
			case "23:59:51" -> channel.sendMessage("9 Seconds until new year!").queue();
			case "23:59:52" -> channel.sendMessage("8 Seconds until new year!").queue();
			case "23:59:53" -> channel.sendMessage("7 Seconds until new year!").queue();
			case "23:59:54" -> channel.sendMessage("6 Seconds until new year!").queue();
			case "23:59:55" -> channel.sendMessage("5 Seconds until new year!").queue();
			case "23:59:56" -> channel.sendMessage("4 Seconds until new year!").queue();
			case "23:59:57" -> channel.sendMessage("3 Seconds until new year!").queue();
			case "23:59:58" -> channel.sendMessage("2 Seconds until new year!").queue();
			case "23:59:59" -> channel.sendMessage("1 Second until new year!").queue();
			}
		}else if(date.matches("01.01")) {
			switch(time) {
			case "00:00:01" -> {
                            text = "Happy New Year!"; file = new File(LotusManager.configFolderName + "/assets/templates/feuerwerk7.png"); resetDays();
                        }
			}
			File out = (file != null ? editFile(file, text) : null);
			if(out != null) {
				channel.sendFiles(FileUpload.fromData(out)).queue();
				out.delete();
				resetDays();
			}
		}
	}
	
	File editFile(File input, String text) {
		BufferedImage image = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
		try {
			image = ImageIO.read(input);
		} catch (IOException e1) {
		}
		registerFont();
		//text wird unten-links angesetzt, bilder werden von oben links eingefügt
		//80, 141 für text pos
		BufferedImage tmp = new BufferedImage(image.getWidth(), image.getHeight(), BufferedImage.TYPE_INT_ARGB);
		Graphics graphics = tmp.getGraphics();
		graphics.drawImage(image, 0, 0, null);
		graphics.setFont(new Font("Minecraftia", Font.PLAIN, 16));
		FontMetrics fm = graphics.getFontMetrics(new Font("Minecraftia", Font.PLAIN, 16));
		Rectangle rect = new Rectangle(384, 16);
		int x = rect.x + (rect.width - fm.stringWidth(text)) / 2;
		graphics.setColor(Color.decode("#000000"));
		graphics.drawString(text, x, 141);
		graphics.dispose();
		File f = new File(LotusManager.configFolderName + "/tmp/fwout.png");
		try {
			ImageIO.write(tmp, "png", f);
		} catch (IOException e1) {
			f = null;
		}
		return f;
	}
	
	void registerFont() {
		try {
			Font mcfont = Font.createFont(Font.TRUETYPE_FONT, new File(LotusManager.configFolderName + "/assets/fonts/minecraftia.ttf"));
			GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
			ge.registerFont(mcfont);
		} catch (FontFormatException | IOException e1) {
		}
	}
	
	void setDayAsRun(int day) {
		YamlFile cfg = new YamlFile(runFile);
		try {
			cfg.load();
			cfg.set("Day." + day, true);
			cfg.save();
		} catch (IOException e) {
		}
	}
	
	boolean hasDayRun(int day) {
		YamlFile cfg = new YamlFile(runFile);
		try {
			cfg.load();
			return cfg.getBoolean("Day." + day);
		} catch (IOException e) {
		}
		return false;
	}

	void resetDays() {
		YamlFile cfg = new YamlFile(runFile);
		try {
			cfg.load();
			for(int i = 1; i <= 30; i++) {
				cfg.set("Day." + i, false);
			}
			Main.logger.info("Christmas Days has been reset.");
			cfg.save();
		} catch (IOException e) {
		}
	}
	
	void initFile() {
		if (!runFile.exists()) {
			try {
				runFile.createNewFile();
			} catch (IOException e) {
			}
		}
		
		YamlFile cfg = new YamlFile(runFile);
		try {
			cfg.load();
			for(int i = 1; i <= 30; i++) {
				if(!cfg.contains("Day." + i)) {
					cfg.addDefault("Day." + i, false);
				}
			}
			cfg.options().copyDefaults(true);
			cfg.save();
		} catch (IOException e) {
		}
	}
}