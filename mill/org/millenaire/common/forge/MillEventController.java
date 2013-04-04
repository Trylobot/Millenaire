package org.millenaire.common.forge;

import java.util.Vector;

import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.ai.EntityAIAttackOnCollide;
import net.minecraft.entity.ai.EntityAINearestAttackableTarget;
import net.minecraft.entity.ai.EntityAITasks;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.world.WorldServer;
import net.minecraft.world.WorldServerMulti;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.world.WorldEvent.Load;
import net.minecraftforge.event.world.WorldEvent.Save;
import net.minecraftforge.event.world.WorldEvent.Unload;

import org.millenaire.common.MLN;
import org.millenaire.common.MillVillager;
import org.millenaire.common.MillWorld;
import org.millenaire.common.core.MillCommonUtilities;

public class MillEventController {

	@ForgeSubscribe
	public void entityCreated(EntityJoinWorldEvent event) {

		if (Mill.startupError)
			return;

		if ((event.entity instanceof EntityZombie) ||
				(event.entity instanceof EntitySkeleton)) {
			final EntityMob mob=(EntityMob)event.entity;
			mob.tasks.addTask(3, new EntityAIAttackOnCollide(mob, MillVillager.class,
					mob.getAIMoveSpeed(), true));

			EntityAITasks targetTasks;
			try {
				targetTasks = (EntityAITasks)MillCommonUtilities.getPrivateValue(EntityLiving.class, mob, 60);
				targetTasks.addTask(3, new EntityAINearestAttackableTarget(mob, MillVillager.class, 16.0F, 0, false));

			} catch (final Exception e) {
				MLN.printException("Error when trying to make new mob "+mob+" target villagers:", e);
			}
		}
	}

	@ForgeSubscribe
	public void worldLoaded(Load event)
	{

		if (Mill.displayMillenaireLocationError && !Mill.proxy.isTrueServer()) {
			Mill.proxy.sendLocalChat(Mill.proxy.getTheSinglePlayer(),MLN.DARKRED, "ERREUR: Impossible de trouver le fichier de configuration "+Mill.proxy.getConfigFile().getAbsolutePath()+". V\u00e9rifiez que le dossier millenaire est bien dans minecraft/mods/");
			Mill.proxy.sendLocalChat(Mill.proxy.getTheSinglePlayer(),MLN.DARKRED, "ERROR: Could not find the config file at "+Mill.proxy.getConfigFile().getAbsolutePath()+". Check that the millenaire directory is in minecraft/mods/");
			return;
		}
		
		if (!(event.world instanceof WorldServer)) {
			Mill.clientWorld=new MillWorld(event.world);
			Mill.proxy.testTextureSize();
		} else {
			if (!(event.world instanceof WorldServerMulti)) {
				final MillWorld newWorld=new MillWorld(event.world);
				Mill.serverWorlds.add(newWorld);
				newWorld.loadData();
			}
		}
		
		
		
	}

	@ForgeSubscribe
	public void worldSaved(Save event)
	{

		if (Mill.startupError)
			return;

		if (event.world.getWorldInfo().getDimension()!=0)
			return;

		if (!(event.world instanceof WorldServer)) {
			Mill.clientWorld.saveEverything();
		} else {

			for (final MillWorld mw : Mill.serverWorlds) {
				if (mw.world==event.world) {
					mw.saveEverything();
				}
			}
		}
	}

	@ForgeSubscribe
	public void worldUnloaded(Unload event)
	{

		if (Mill.startupError)
			return;

		if (event.world.getWorldInfo().getDimension()!=0)
			return;

		if (!(event.world instanceof WorldServer)) {
			Mill.clientWorld=null;
		} else {

			final Vector<MillWorld> toDelete=new Vector<MillWorld>();

			for (final MillWorld mw : Mill.serverWorlds) {
				if (mw.world==event.world) {
					toDelete.add(mw);
				}
			}

			for (final MillWorld mw : toDelete) {
				Mill.serverWorlds.remove(mw);
			}
		}
	}

}
