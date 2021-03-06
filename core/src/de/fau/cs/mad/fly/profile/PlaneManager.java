package de.fau.cs.mad.fly.profile;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.sql.DatabaseCursor;
import com.badlogic.gdx.utils.JsonReader;
import com.badlogic.gdx.utils.JsonValue;

import de.fau.cs.mad.fly.db.FlyDBManager;
import de.fau.cs.mad.fly.player.IPlane;
import de.fau.cs.mad.fly.player.IPlane.Head;
import de.fau.cs.mad.fly.res.PlaneUpgrade;
import de.fau.cs.mad.fly.settings.SettingManager;

/**
 * Manages the different Spaceships
 * 
 * @author Sebastian
 * 
 */
public class PlaneManager {
    
    private JsonReader reader = new JsonReader();
    // private List<IPlane.Head> planes;
    private Map<Integer, IPlane.Head> planes;
    private IPlane.Head chosenPlane;
    
    private static PlaneManager Instance = new PlaneManager();
    
    public static PlaneManager getInstance() {
        return Instance;
    }
    
    private PlaneManager(){
    	 PlayerProfileManager.getInstance().addPlayerChangedListener(new ChangeListener<PlayerProfile>() {
             
             @Override
             public void changed(PlayerProfile newPlayerProfile) {
            	 initPlayerUpdateAndEquiped(newPlayerProfile.getId());
            	 chosenPlane = null;
            	 getChosenPlane();
             }             
         });
    }
    
    public Map<Integer, IPlane.Head> getSpaceshipList() {
        if (planes == null) {
            
            // planes = new ArrayList<IPlane.Head>();
            planes = new HashMap<Integer, IPlane.Head>();
            FileHandle dirHandle = Gdx.files.internal("spaceships/json/");
            for (FileHandle file : dirHandle.list()) {
                JsonValue json = reader.parse(file);
                IPlane.Head planeHead = new IPlane.Head();
                
                int id = json.getInt("id");
                planeHead.id = id;
                planeHead.name = json.getString("name");
                planeHead.modelRef = json.getString("modelRef");
                planeHead.levelGroupDependency = json.getInt("levelGroupDependency");
                planeHead.speed = json.getFloat("speed");
                planeHead.basicSpeed = planeHead.speed;
                planeHead.setRollingSpeed(json.getFloat("rollingSpeed"));
                planeHead.azimuthSpeed = json.getFloat("azimuthSpeed");
                planeHead.basicAzimuthSpeed = planeHead.azimuthSpeed;
                planeHead.lives = json.getInt("lives");
                planeHead.basicLives = planeHead.lives;
                JsonValue rotation = json.get("rotation");
                if (rotation != null) {
                    Vector3 rotationVector = new Vector3(rotation.getFloat(0), rotation.getFloat(1), rotation.getFloat(2));
                    planeHead.rotationSpeed = rotationVector.len();
                    planeHead.rotation = rotationVector.nor();
                }
                JsonValue particleOffset = json.get("particleOffset");
                if (particleOffset != null) {
                    planeHead.particleOffset = new Vector3(particleOffset.getFloat(0), particleOffset.getFloat(1), particleOffset.getFloat(2));
                }
                
                planeHead.file = file;
                planes.put(id, planeHead);
            }
            initPlayerUpdateAndEquiped(PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId());
        }
        return planes;
    }
    
	public void initPlayerUpdateAndEquiped( int playerID) {
		if (planes == null) {
			getSpaceshipList();
		}
		
		for (IPlane.Head planeHead  : planes.values()) {
			planeHead.azimuthSpeed = planeHead.basicAzimuthSpeed;
			planeHead.speed = planeHead.basicSpeed;
			planeHead.lives = planeHead.basicLives;
			
			Collection<PlaneUpgrade> upgrades = PlaneUpgradeManager.getInstance().getUpgradeList().values();
			planeHead.getUpgradesBought().clear();
			planeHead.getUpgradesEquiped().clear();

			Map<String, Integer> upgradeDB = getUpgradesFromDB(planeHead.id);
			Map<String, Integer> equipedDB = getEquipedsFromDB(planeHead.id);


			for (PlaneUpgrade upgrade : upgrades) {
				Integer upgradeAmount = upgradeDB.get(upgrade.name);
				if (upgradeAmount != null)
					planeHead.getUpgradesBought().put(upgrade.name, upgradeAmount);
				else
					planeHead.getUpgradesBought().put(upgrade.name, 0);
				planeHead.getUpgradesEquiped().put(upgrade.name, 0);
				Integer equippedAmount = equipedDB.get(upgrade.name);
				if (equippedAmount != null)
					upgradePlane(planeHead, upgrade.name, equippedAmount);
			}
		}
	}
    
    public Map<String, Integer> getEquipedsFromDB(int planeID) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        String sql = "select equiped_name, _count from fly_plane_Equiped where player_id=" + PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId() + " and plane_id=" + planeID;
        DatabaseCursor cursor = FlyDBManager.getInstance().selectData(sql);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.next()) {
				result.put(cursor.getString(0), cursor.getInt(1));
			}
			cursor.close();
		}
        return result;
    }
    
    public Map<String, Integer> getUpgradesFromDB(int planeID) {
        Map<String, Integer> result = new HashMap<String, Integer>();
        String sql = "select update_name, _count from fly_plane_upgrade where player_id=" + PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId() + " and plane_id=" + planeID;
        DatabaseCursor cursor = FlyDBManager.getInstance().selectData(sql);
		if (cursor != null && cursor.getCount() > 0) {
			while (cursor.next()) {
				result.put(cursor.getString(0), cursor.getInt(1));
			}
			cursor.close();
		}
        return result;
    }
    
    public void updateEquiped(int planeID, String name, int newValue) {
        int playerId = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId();
        String sql = "delete from fly_plane_Equiped where player_id=" + playerId + " and equiped_name='" + name + "' and plane_id=" + planeID;
        String insert = "insert into fly_plane_Equiped(player_id, plane_id, equiped_name, _count ) values (" + playerId + ", " + planeID + ",'" + name + "'," + newValue + ")";
        FlyDBManager.getInstance().execSQL(sql);
        FlyDBManager.getInstance().execSQL(insert);
    }
    
    public void updateUpdate(int planeID, String name, int newValue) {
        int playerId = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getId();
        String sql = "delete from fly_plane_upgrade where player_id=" + playerId + " and update_name='" + name + "' and plane_id=" + planeID;
        String insert = "insert into fly_plane_upgrade(player_id, plane_id, update_name, _count ) values (" + playerId + ", " + planeID + ",'" + name + "'," + newValue + ")";
        FlyDBManager.getInstance().execSQL(sql);
        FlyDBManager.getInstance().execSQL(insert);
    }
    
    public int getUpgradeType(String name) {
        Collection<PlaneUpgrade> upgrades = PlaneUpgradeManager.getInstance().getUpgradeList().values();
        for (PlaneUpgrade upgrade : upgrades) {
            if (upgrade.name.equals(name)) {
                return upgrade.type;
            }
        }
        return 0;
    }
    
	public IPlane.Head getChosenPlane() {
		if (chosenPlane == null) {
			SettingManager settings = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getSettingManager();
			int planeID = settings.getInteger(SettingManager.CHOSEN_PLANE_ID, 0);
			for (IPlane.Head plane : getSpaceshipList().values()) {
				if (plane.id == planeID) {
					chosenPlane = plane;
				}
			}
			if (chosenPlane == null) {
				chosenPlane = getSpaceshipList().get(1);
			}
		}
		return chosenPlane;
	}
    
    public IPlane.Head getNextPlane(int left) {
        if (chosenPlane == null) {
            chosenPlane = getSpaceshipList().get(1);
        }
        
        int chosenPlaneId = chosenPlane.id;
        
        chosenPlaneId -= left;
        if (chosenPlaneId < 0) {
            chosenPlaneId += planes.size();
        } else if (chosenPlaneId >= planes.size()) {
            chosenPlaneId -= planes.size();
        }
        
        chosenPlane = getSpaceshipList().get(chosenPlaneId);
        
        // ((Fly)
        // Gdx.app.getApplicationListener()).getGameController().getPlayer().setPlane(new
        // Spaceship(chosenPlane));
        PlayerProfileManager.getInstance().getCurrentPlayerProfile().setPlane(chosenPlane);
        
        return chosenPlane;
    }
    
	public void setChosenPlane(IPlane.Head plane) {
		chosenPlane = plane;
		PlayerProfileManager.getInstance().getCurrentPlayerProfile().getSettingManager().set(SettingManager.CHOSEN_PLANE_ID, plane.id);
	}
    
    public IPlane.Head upgradePlane(String upgradeName, int signum) {
        return upgradePlane(chosenPlane, upgradeName, signum);
    }

	public IPlane.Head upgradePlane(IPlane.Head plane, String upgradeName, int signum) {
		PlaneUpgrade upgrade = PlaneUpgradeManager.getInstance().getUpgrade(upgradeName);

        /*
         * int size = upgrade.upgradeValues.length; for(int i = 0; i < size;
         * i++) { plane. }
         */
		int[] values = upgrade.upgradeValues;

		plane.speed += values[0] * signum;
		plane.setRollingSpeed(plane.getRollingSpeed() + values[1] * signum);
		plane.azimuthSpeed += values[2] * signum;
		plane.lives += values[3] * signum;

		int oldValue = plane.getUpgradesEquiped().get(upgradeName);
		plane.getUpgradesEquiped().put(upgradeName, oldValue + signum);
		this.updateEquiped(plane.id, upgradeName, oldValue + signum);

		planes.put(plane.id, plane);

		return plane;
	}
    
    public void buyUpgradeForPlane(String upgradeName) {
        int currentUpgradeBought = chosenPlane.getUpgradesBought().get(upgradeName);
        
        PlaneUpgrade upgrade = PlaneUpgradeManager.getInstance().getUpgrade(upgradeName);
        int maxUpgrade = upgrade.timesAvailable;
        
        if (currentUpgradeBought < maxUpgrade) {
            if (PlayerProfileManager.getInstance().getCurrentPlayerProfile().addMoney(-upgrade.price)) {
                chosenPlane.getUpgradesBought().put(upgradeName, currentUpgradeBought + 1);
                this.updateUpdate(chosenPlane.id, upgradeName, currentUpgradeBought + 1);
            }
        }
    }
    
    public boolean upgradeCanBeBought(PlaneUpgrade upgrade) {
        int money = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getMoney();
        int currentlyBought = chosenPlane.getUpgradesBought().get(upgrade.name);
        if (currentlyBought < upgrade.timesAvailable && upgrade.price <= money) {
            return true;
        }
        return false;
    }
}