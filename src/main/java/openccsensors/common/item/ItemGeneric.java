package openccsensors.common.item;

import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import openccsensors.OpenCCSensors;
import openccsensors.api.IItemMeta;
import openccsensors.api.IRequiresIconLoading;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

public class ItemGeneric extends Item {

	private HashMap<Integer, IItemMeta> metaitems = new HashMap<Integer, IItemMeta>();

	public ItemGeneric() {
		super();
		setHasSubtypes(true);
		setMaxDamage(0);
		setMaxStackSize(64);
		setCreativeTab(OpenCCSensors.tabOpenCCSensors);
	}
	
	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item item, CreativeTabs tab, List subItems) {
		OpenCCSensors.turtleUpgradeSensor.addTurtlesToCreative(subItems);
		for (Entry<Integer, IItemMeta> entry : metaitems.entrySet()) {
			if (entry.getValue().displayInCreative()) {
				subItems.add(new ItemStack(item, 1, entry.getKey()));
			}
		}
	}

	public void addMeta(IItemMeta meta) {
		metaitems.put(meta.getId(), meta);
	}

	public IItemMeta getMeta(ItemStack stack) {
		return getMeta(stack.getItemDamage());
	}
	
	public IItemMeta getMeta(int id) {
		return metaitems.get(id);
	}
	
	@Override
	public void registerIcons(IIconRegister iconRegister) {
		for (Entry<Integer, IItemMeta> entry : metaitems.entrySet()) {
			if (entry.getValue() instanceof IRequiresIconLoading) {
				((IRequiresIconLoading)entry.getValue()).loadIcon(iconRegister);
			}
		}
	}
	
	@Override
    public IIcon getIconFromDamage(int id)
    {
		IItemMeta meta = metaitems.get(id);
		if (meta == null) {
			return null;
		}
        return meta.getIcon();
    }

	@Override
    public String getUnlocalizedName(ItemStack itemStack)
    {
		IItemMeta meta = getMeta(itemStack);
		if  (meta == null) {
			return "";
		}
        return String.format("item.openccsensors.%s", meta.getName());
    }

}
