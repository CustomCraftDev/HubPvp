import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemStack;

public class HubPvp_enchant extends EnchantmentWrapper{
 
	public HubPvp_enchant(int id) {
		super(id);
	}
	 
	@Override
	public boolean canEnchantItem(ItemStack item) {
		return true;
	}
	 
	@Override
	public EnchantmentTarget getItemTarget() {
		return null;
	}
	 
	@Override
	public int getMaxLevel() {
		return 2;
	}
	 
	@Override
	public String getName() {
		return "HubPvp";
	}
	 
	@Override
	public int getStartLevel() {
		return 1;
	}
 
}
