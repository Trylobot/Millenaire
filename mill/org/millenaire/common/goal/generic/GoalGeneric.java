package org.millenaire.common.goal.generic;

import java.io.File;
import java.util.Vector;

import net.minecraft.item.ItemStack;

import org.millenaire.common.Building;
import org.millenaire.common.MLN;
import org.millenaire.common.MillVillager;
import org.millenaire.common.core.MillCommonUtilities.ExtFileFilter;
import org.millenaire.common.forge.Mill;
import org.millenaire.common.goal.Goal;
import org.millenaire.common.item.Goods;

public abstract class GoalGeneric extends Goal {

	public String buildingTag=null;

	public String requiredTag=null;
	public boolean townHallGoal=false;

	public int priority=50;
	public int duration=5000;

	public String sentenceKey=null,labelKey=null;

	

	public ItemStack[] heldItems=null;

	public String sound=null;



	public static boolean readGenericGoalConfigLine(GoalGeneric g, String key, String value,File file,String line) {
		if (key.equals("buildingtag")) {
			g.buildingTag=value.toLowerCase();
		} else if (key.equals("townhallgoal")) {
			g.townHallGoal=Boolean.parseBoolean(value);
		} else if (key.equals("requiredtag")) {
			g.requiredTag=value.toLowerCase();
		} else if (key.equals("sentencekey")) {
			g.sentenceKey=value.toLowerCase();
		} else if (key.equals("labelkey")) {
			g.labelKey=value.toLowerCase();
		} else if (key.equals("priority")) {
			g.priority=Integer.parseInt(value);
		} else if (key.equals("maxsimultaneousinbuilding")) {
			g.maxSimultaneousInBuilding=Integer.parseInt(value);
		} else if (key.equals("maxsimultaneoustotal")) {
			g.maxSimultaneousTotal=Integer.parseInt(value);
		} else if (key.equals("duration")) {
			g.duration=Integer.parseInt(value);
		} else if (key.equals("sound")) {
			g.sound=value;
		} else if (key.equals("helditems")) {
			final String[] temp2=value.split(",");

			g.heldItems=new ItemStack[temp2.length];

			for (int i=0;i<temp2.length;i++) {
				if (Goods.goodsName.containsKey(temp2[i])) {
					g.heldItems[i]=Goods.goodsName.get(temp2[i]).getItemStack();
				} else {
					g.heldItems[i]=null;
					MLN.error(null, "Unknown held item in generic goal "+file.getName()+": "+line);
				}
			}

		}  else if (key.equals("buildinglimit")) {
			final String[] temp2=value.split(",");

			if (temp2.length!=2) {
				MLN.error(null, "buildinglimits must take the form of buildinglimit=goodname,goodquatity in generic goal "+file.getName()+": "+line);
			} else {
				if (Goods.goodsName.containsKey(temp2[0])) {
					g.buildingLimit.put(Goods.goodsName.get(temp2[0]), Integer.parseInt(temp2[1]));
				} else {
					MLN.error(null, "Unknown buildinglimits item in generic goal "+file.getName()+": "+line);
				}
			}
		}  else if (key.equals("townhalllimit")) {
			final String[] temp2=value.split(",");

			if (temp2.length!=2) {
				MLN.error(null, "townhalllimits must take the form of townhalllimit=goodname,goodquatity in generic goal "+file.getName()+": "+line);
			} else {
				if (Goods.goodsName.containsKey(temp2[0])) {
					g.townhallLimit.put(Goods.goodsName.get(temp2[0]), Integer.parseInt(temp2[1]));
				} else {
					MLN.error(null, "Unknown townhalllimits item in generic goal "+file.getName()+": "+line);
				}
			}
		} else if (key.equals("itemsbalance")) {

			final String[] temp2=value.split(",");

			if (temp2.length!=2) {
				MLN.error(null, "itemsbalance must take the form of itemsbalance=firstgood,secondgood in generic goal "+file.getName()+": "+line);
			} else {
				if (Goods.goodsName.containsKey(temp2[0]) || Goods.goodsName.containsKey(temp2[1])) {
					g.balanceOutput1=Goods.goodsName.get(temp2[0]);
					g.balanceOutput2=Goods.goodsName.get(temp2[1]);
				} else {
					MLN.error(null, "Unknown itemsbalance item in generic goal "+file.getName()+": "+line);
				}
			}
		} else {
			return false;
		}
		return true;
	}

	@Override
	public int priority(MillVillager villager) throws Exception {
		return priority+villager.getRNG().nextInt(10);
	}

	@Override
	public String sentenceKey() {
		if (sentenceKey==null)
			return key;

		return sentenceKey;
	}

	@Override
	public String labelKey(MillVillager villager) {
		if (labelKey==null)
			return key;

		return labelKey;
	}

	@Override
	public String labelKeyWhileTravelling(MillVillager villager) {
		if (labelKey==null)
			return key;

		return labelKey;
	}

	@Override
	public int actionDuration(MillVillager villager) throws Exception {
		return duration;
	}

	@Override
	public final boolean isPossibleSpecific(MillVillager villager) throws Exception {
		
		if (!isPossibleGenericGoal(villager))
			return false;

		final Vector<Building> buildings=getBuildings(villager);

		boolean destFound=false;

		if (!buildings.isEmpty()) {
			for (Building dest : buildings) {
				if (!destFound) {
					destFound=isDestPossible(villager,dest);
				}
			}
			
			return destFound;
		} else {
			return false;
		}
		
	}
	
	public final boolean isDestPossible(MillVillager villager, Building dest) {
		return (validateDest(villager,dest) && isDestPossibleSpecific(villager,dest));
	}

	public abstract boolean isDestPossibleSpecific(MillVillager villager, Building b);

	public abstract boolean isPossibleGenericGoal(MillVillager villager) throws Exception;

	public Vector<Building> getBuildings(MillVillager villager) {
		Vector<Building> buildings=new Vector<Building>();

		if (townHallGoal) {
			if (requiredTag==null || villager.getTownHall().location.tags.contains(requiredTag))
				buildings.add(villager.getTownHall());
		} else if (buildingTag==null) {
			if (requiredTag==null || villager.getHouse().location.tags.contains(requiredTag))
				buildings.add(villager.getHouse());
		} else {

			for (final Building b : villager.getTownHall().getBuildingsWithTag(buildingTag)) {
				if (requiredTag==null || b.location.tags.contains(requiredTag))
					buildings.add(b);
			}
		}

		return buildings;
	}

	@Override
	public ItemStack[] getHeldItemsTravelling(MillVillager villager)
			throws Exception {
		return heldItems;
	}

	private static Vector<File> getGenericGoalFiles(String directoryName) {
		final Vector<File> genericGoalFile=new Vector<File>();

		for (final File loadDir : Mill.loadingDirs) {

			final File dir = new File(new File(loadDir,"goals"),directoryName);

			if (dir.exists()) {
				for (final File file : dir.listFiles(new ExtFileFilter("txt"))) {
					genericGoalFile.add(file);
				}
			}

		}

		return genericGoalFile;
	}

	public static void loadGenericGoals() {
		for (final File file : getGenericGoalFiles("genericcrafting")) {
			try {
				final GoalGenericCrafting goal=GoalGenericCrafting.loadGenericCraftingGoal(file);
				if (goal!=null) {
					if (MLN.LogGeneralAI>=MLN.MAJOR) {
						MLN.major(goal, "loaded crafting goal");
					}
					goals.put(goal.key, goal);
				}
			} catch (final Exception e) {
				MLN.printException(e);
			}
		}
		
		for (final File file : getGenericGoalFiles("genericcooking")) {
			try {
				final GoalGenericCooking goal=GoalGenericCooking.loadGenericCookingGoal(file);
				if (goal!=null) {
					if (MLN.LogGeneralAI>=MLN.MAJOR) {
						MLN.major(goal, "loaded cooking goal");
					}
					goals.put(goal.key, goal);
				}
			} catch (final Exception e) {
				MLN.printException(e);
			}
		}
		
		
		for (final File file : getGenericGoalFiles("genericslaughteranimal")) {
			try {
				final GoalGenericSlaughterAnimal goal=GoalGenericSlaughterAnimal.loadGenericSlaughterAnimalGoal(file);
				if (goal!=null) {
					if (MLN.LogGeneralAI>=MLN.MAJOR) {
						MLN.major(goal, "loaded slaughtering goal");
					}
					goals.put(goal.key, goal);
				}
			} catch (final Exception e) {
				MLN.printException(e);
			}
		}
		
		for (final File file : getGenericGoalFiles("genericplanting")) {
			try {
				final GoalGenericPlantCrop goal=GoalGenericPlantCrop.loadGenericPlantCropGoal(file);
				if (goal!=null) {
					if (MLN.LogGeneralAI>=MLN.MAJOR) {
						MLN.major(goal, "loaded planting goal");
					}
					goals.put(goal.key, goal);
				}
			} catch (final Exception e) {
				MLN.printException(e);
			}
		}
		
		for (final File file : getGenericGoalFiles("genericharvesting")) {
			try {
				final GoalGenericHarvestCrop goal=GoalGenericHarvestCrop.loadGenericHarvestCropGoal(file);
				if (goal!=null) {
					if (MLN.LogGeneralAI>=MLN.MAJOR) {
						MLN.major(goal, "loaded harvesting goal");
					}
					goals.put(goal.key, goal);
				}
			} catch (final Exception e) {
				MLN.printException(e);
			}
		}
	}
}
