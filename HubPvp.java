import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType.SlotType;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class HubPvp extends JavaPlugin implements Listener{
	private String noperm;
	protected String prefix;
	
	protected boolean toggle = true;
	protected boolean debug = false;
	protected boolean update = false;
	
	protected FileConfiguration config;
	private ArrayList<Player> pvp;
	private ItemStack item;
	private int slot;
	private List<String> enable;
	private List<String> disable;
	private HubPvp_enchant enchant;
	private ArrayList<Player> disabled;
	private ItemStack[] armour;

	
	// PLUGIN MAIN FUNCTIONS ------------------------------------------------------------------------------------------------------------------
	
	
	public void onEnable() {
		loadconfig();
		resetEnchants();
		new Updater(this);
		
		pvp = new ArrayList<Player>();
		disabled = new ArrayList<Player>();
    	getServer().getPluginManager().registerEvents(this, this);
    	
    	try {
    	    Field f = Enchantment.class.getDeclaredField("acceptingNew");
    	    f.setAccessible(true);
    	    f.set(null, true);
    	} catch (Exception e1) {}
    	
    	try {  	    
    		enchant = new HubPvp_enchant(121);
    		Enchantment.registerEnchantment(enchant);
    	} catch (Exception e2){}
	}


	private void resetEnchants() {
		try {
			Field byIdField = Enchantment.class.getDeclaredField("byId");
			Field byNameField = Enchantment.class.getDeclaredField("byName");
			 
			byIdField.setAccessible(true);
			byNameField.setAccessible(true);
			 
			@SuppressWarnings("unchecked")
			HashMap<Integer, Enchantment> byId = (HashMap<Integer, Enchantment>) byIdField.get(null);
			@SuppressWarnings("unchecked")
			HashMap<String, Enchantment> byName = (HashMap<String, Enchantment>) byNameField.get(null);
			 
			if(byId.containsKey(121))
			byId.remove(121);
			 
			if(byName.containsKey("HubPvp"))
			byName.remove("HubPvp");
		} catch (Exception e) {}
	}
	
	
	private void loadconfig(){
		config = getConfig();
		config.options().copyDefaults(true);
		saveConfig();
		
		debug = config.getBoolean("debug");
		noperm = ChatColor.translateAlternateColorCodes('&', config.getString("msg.noperm"));
		prefix = ChatColor.translateAlternateColorCodes('&', config.getString("msg.prefix"));
		
		enable = config.getStringList("msg.enable");
		disable = config.getStringList("msg.disable");
				
		List<String> lore = config.getStringList("sword.lore");
		slot = config.getInt("sword.slot");
		
		armour = new ItemStack[4];
		armour[3] = new ItemStack(Material.DIAMOND_HELMET, 1);
		armour[2] = new ItemStack(Material.DIAMOND_CHESTPLATE, 1);
		armour[1] = new ItemStack(Material.DIAMOND_LEGGINGS, 1);
		armour[0] = new ItemStack(Material.DIAMOND_BOOTS, 1);
		
		List<String> helmet = config.getStringList("enchants.helmet");
		for(String enchant : helmet) {
			String[] liste = enchant.split(",");
			Enchantment e = Enchantment.getByName(liste[0]);
			armour[3].addUnsafeEnchantment(e, Integer.parseInt(liste[1]));
		}
		List<String> chestplate = config.getStringList("enchants.chestplate");
		for(String enchant : chestplate) {
			String[] liste = enchant.split(",");
			Enchantment e = Enchantment.getByName(liste[0]);
			armour[2].addUnsafeEnchantment(e, Integer.parseInt(liste[1]));
		}
		List<String> leggings = config.getStringList("enchants.leggings");
		for(String enchant : leggings) {
			String[] liste = enchant.split(",");
			Enchantment e = Enchantment.getByName(liste[0]);
			armour[1].addUnsafeEnchantment(e, Integer.parseInt(liste[1]));
		}
		List<String> boots = config.getStringList("enchants.boots");
		for(String enchant : boots) {
			String[] liste = enchant.split(",");
			Enchantment e = Enchantment.getByName(liste[0]);
			armour[0].addUnsafeEnchantment(e, Integer.parseInt(liste[1]));
		}
		
		item = new ItemStack(Material.DIAMOND_SWORD, 1);
			ItemMeta meta = item.getItemMeta();
			meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', config.getString("sword.name")));
			meta.setLore(lore);
		item.setItemMeta(meta);
	}
	
	
	// ON COMMAND ------------------------------------------------------------------------------------------------------------------
	
	
	public boolean onCommand(CommandSender sender, Command cmd, String alias, String[] args) {
		boolean isplayer = false;
		Player p = null;
		
		if ((sender instanceof Player)) {
			p = (Player)sender;
			isplayer = true;
		}
			
		if(cmd.getName().equalsIgnoreCase("hubpvp") && args.length == 1){
								
			// reload
			if(args[0].equalsIgnoreCase("reload")){
				if(isplayer){
					if(p.hasPermission("hubpvp.reload")){
						loadconfig();
					return true;
				}
					else{
						p.sendMessage(noperm);
						return true;
					}
				}else {
					loadconfig();
					return true;
				}
			}			
		}
		
		// nothing to do here \o/
		return false;
	}
	
	
	// EVENTS ------------------------------------------------------------------------------------------------------------------
	
	
	@EventHandler
	public void onJoin(PlayerJoinEvent e) {
		e.getPlayer().getInventory().setItem(slot, item);
	}
	
	
	@EventHandler
	public void onQuit(PlayerQuitEvent e) {
		PlayerInventory inv = e.getPlayer().getInventory();
		inv.setArmorContents(new ItemStack[4]);
		pvp.remove(e.getPlayer());
	}
	
	
	@EventHandler
	public void onDeath(PlayerDeathEvent e) {
		PlayerInventory inv = e.getEntity().getInventory();
		inv.setArmorContents(new ItemStack[4]);
		e.getDrops().clear();
		pvp.remove(e.getEntity());
	}
	
	
	@EventHandler
	public void onRespawn(PlayerRespawnEvent e){
		e.getPlayer().getInventory().setItem(slot, item);
	}
	
	
	@SuppressWarnings("deprecation")
	@EventHandler
	public void onMove(InventoryClickEvent e){
		if(e.getCurrentItem().getType().equals(Material.DIAMOND_SWORD)) {
			e.setCancelled(true);
			e.setCursor(null);
			e.getWhoClicked().closeInventory();
			return;
		}
		if(e.getSlotType().equals(SlotType.ARMOR)) {
			e.setCancelled(true);
			e.setCursor(null);
			e.getWhoClicked().closeInventory();
			return;
		}
	}
	

	@EventHandler
	public void onDrop(PlayerDropItemEvent e){
		if(e.getItemDrop().getItemStack().getType().equals(Material.DIAMOND_SWORD)) {
			e.setCancelled(true);
			e.getPlayer().sendMessage(noperm);
		}
	}
	
	
	@EventHandler
	public void onUse(PlayerInteractEvent e) {	
		Player p = e.getPlayer();
		if (p.isSneaking()) {
			if(e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) {
				if(p.hasPermission("hubpvp.toggle")) {
					if(e.hasItem()) {
						ItemStack sword = e.getPlayer().getInventory().getItem(slot);
						if(e.getPlayer().getItemInHand().getType().equals(Material.DIAMOND_SWORD)) {
							if(!disabled.contains(p)) {
								disabled.add(p);
								if(hasEnchant(sword.getEnchantments())) {
									togglepvp(true, p, sword);
									p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', disable.get(0)));
								}else {
									togglepvp(false, p, sword);
									p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', enable.get(0)));
								}
								return;
							}
						}
					}
				}
			}
		}
	}
	
	
	@EventHandler
	public void onPVP(EntityDamageByEntityEvent e) {
		if(e.getDamager() instanceof Player && e.getEntity() instanceof Player) {
			if(pvp.contains((Player) e.getDamager())) {
				if(pvp.contains((Player) e.getEntity())) {
					return;
				}else {
					e.setCancelled(true);
				}
			}else {
				e.setCancelled(true);
			}
		}
	}
	

	// FUNCTIONS ------------------------------------------------------------------------------------------------------------------

	
	protected void togglearmour(Player p, boolean b) {
		if(b) {
			p.getInventory().setArmorContents(armour);
		}else {
			p.getInventory().setArmorContents(new ItemStack[4]);
		}	
	}
	
	
	protected boolean hasEnchant(Map<Enchantment, Integer> enchantments) {
		for (Enchantment e : enchantments.keySet()) {
			if(e.equals(enchant)) {return true;}
		}
		return false;
	}
	
	
	private void togglepvp(final boolean b, final Player p, final ItemStack sword) {
		new Thread(new Runnable() {
						
			  public void run() {
			   	 try {
					Thread.sleep(1500);
						if(b) {
							p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', disable.get(1)));
						}else {
							p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', enable.get(1)));
						}
					Thread.sleep(1500);
						if(b) {
							p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', disable.get(2)));
						}else {
							p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', enable.get(2)));
						}
					Thread.sleep(1500);
						if(b) {
							p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', disable.get(3)));
							sword.removeEnchantment(enchant);
							togglearmour(p, false);
							pvp.remove(p);
						}else {
							p.sendMessage(prefix + " " + ChatColor.translateAlternateColorCodes('&', enable.get(3)));
							togglearmour(p, true);
							sword.addUnsafeEnchantment(enchant, 1);
							pvp.add(p);
						}
					p.playSound(p.getLocation(), Sound.ORB_PICKUP, 1F, 1F);
					disabled.remove(p);
				}catch(Exception e5) {}
			}
		}).start();
	    return;
	}
	
	
	// UPDATER ------------------------------------------------------------------------------------------------------------------
	
	
	protected void say(Player p, boolean b) {
		if(b) {
			System.out.println(ChatColor.stripColor(prefix + "------------------------------------------------"));
			System.out.println(ChatColor.stripColor(prefix + " HubPVP is outdated. Get the new version here:"));
			System.out.println(ChatColor.stripColor(prefix + " http://www.pokemon-online.xyz/plugin"));
			System.out.println(ChatColor.stripColor(prefix + "------------------------------------------------"));
		}else {
		   	p.sendMessage(prefix + "------------------------------------------------");
		   	p.sendMessage(prefix + " HubPVP is outdated. Get the new version here:");
		   	p.sendMessage(prefix + " http://www.pokemon-online.xyz/plugin");
		   	p.sendMessage(prefix + "------------------------------------------------");
		}
	}

	
}
