package de.fau.cs.mad.fly.profile;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.sql.DatabaseCursor;

import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.HttpClient.PutUserHttpRespListener;
import de.fau.cs.mad.fly.HttpClient.PutUserService;
import de.fau.cs.mad.fly.db.FlyDBManager;
import de.fau.cs.mad.fly.settings.AppSettingsManager;

/**
 * Manages the player profiles.
 * 
 * @author Qufang Fan
 */
public class PlayerProfileManager {
    
    private PlayerProfile currentPlayerProfile;
    private List<PlayerProfile> playerProfiles;
    private static PlayerProfileManager Instance = new PlayerProfileManager();
    private List<ChangeListener<PlayerProfile>> playerProfileChangeListener;
    
    public PlayerProfile getCurrentPlayerProfile() {
        return currentPlayerProfile;
    }
    
    /**
     * Sets the current player and saves them during the app is running in the
     * {@link AppSettingsManager}. If the new {@link PlayerProfile} is an other
     * profile than the current, it calls {@link #currentPlayerProfileChanged().
     */
    public void setCurrentPlayer(PlayerProfile currentPlayerProfile) {
        if (this.currentPlayerProfile != currentPlayerProfile) {
            this.currentPlayerProfile = currentPlayerProfile;
            AppSettingsManager.Instance.setIntegerSetting(AppSettingsManager.CHOSEN_USER, currentPlayerProfile.getId());
            currentPlayerProfileChanged();
        }
        
    }
    
    /**
     * Adds a new {@link PlayerProfile} with the defined name and makes it the
     * new {@link #currentPlayerProfile} with {@link #setCurrentPlayer(PlayerProfile)}.
     * 
     * @param newPlayerProfileName
     */
    public void addNewPlayerProfile(String newPlayerProfileName) {
        PlayerProfile newPlayerProfile = new PlayerProfile();
        newPlayerProfile.setName(newPlayerProfileName);
        playerProfiles.add(newPlayerProfile);
        addNewPlayerToDatabase(newPlayerProfile);
        setCurrentPlayer(newPlayerProfile);
    }
    
    /** Calls every listener of the current {@link PlayerProfile} */
    private void currentPlayerProfileChanged() {
        for (int i = playerProfileChangeListener.size() - 1; i >= 0; i--) {
            playerProfileChangeListener.get(i).changed(currentPlayerProfile);
        }
    }
    
    /**
     * Add a new listener, that is called whenever the current
     * {@link PlayerProfile} is changed by
     * {@link #setCurrentPlayer(PlayerProfile)}.
     * 
     * @param listener
     */
    public void addPlayerChangedListener(ChangeListener<PlayerProfile> listener) {
        playerProfileChangeListener.add(listener);
    }
    
    /**
     * Initializes the default player profile, creates the default player
     * profile when there is no player already existing.
     */
    private PlayerProfileManager() {
        setPlayers();
        
        playerProfileChangeListener = new ArrayList<ChangeListener<PlayerProfile>>();
        
        int userID = AppSettingsManager.Instance.getIntegerSetting(AppSettingsManager.CHOSEN_USER, 0);
        PlayerProfile player = getPlayerfromList(userID);
        if (player == null) {
            addNewPlayerProfile(I18n.t("default.playerName"));
        }
        else {
            setCurrentPlayer(player);
        }
    }
    
    public static PlayerProfileManager getInstance() {
        return Instance;
    }
    
    private PlayerProfile getPlayerfromList(int userID) {
        for (PlayerProfile player : playerProfiles) {
            if (player.getId() == userID) {
                return player;
            }
        }
        return null;
    }
    
    public PlayerProfile getPlayerfromDB(int userID) {
        final String selectSQL = "select player_id, fly_id,name from player where player_id =" + userID;
        PlayerProfile playerProfile = null;
        
        DatabaseCursor cursor = FlyDBManager.getInstance().selectData(selectSQL);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.next();
            playerProfile = new PlayerProfile();
            playerProfile.setId(cursor.getInt(0));
            playerProfile.setFlyID(cursor.getInt(1));
            playerProfile.setName(cursor.getString(2));
            cursor.close();
        }
        
        return playerProfile;
    }
    
    public List<PlayerProfile> getAllPlayerProfiles() {
        return playerProfiles;
    }
    
    private void setPlayers() {
        playerProfiles = new ArrayList<PlayerProfile>();
        final String selectSQL = "select player_id,fly_id,name,total_score,total_geld,current_levelgroup_id,current_level_id,passed_levelgroup_id,passed_level_id,secret_key,is_newname_uploaded from player";
        
        DatabaseCursor cursor = FlyDBManager.getInstance().selectData(selectSQL);
        
        if (cursor != null && cursor.getCount() > 0) {
            while (cursor.next()) {
                PlayerProfile playerProfile = new PlayerProfile();
                playerProfile.setId(cursor.getInt(0));
                playerProfile.setFlyID(cursor.getInt(1));
                playerProfile.setName(cursor.getString(2));
                playerProfile.setMoney(cursor.getInt(3));
                playerProfile.setCurrentLevelGroup(LevelGroupManager.getInstance().getLevelGroup(cursor.getInt(5)));
                playerProfile.setCurrentLevelProfile(cursor.getInt(6));
                playerProfile.setPassedLevelgroupID(cursor.getInt(7));
                playerProfile.setPassedLevelID(cursor.getInt(8));
				playerProfile.setSecretKey(cursor.getString(9));
				playerProfile.setNewnameUploaded(cursor.getInt(10)>0);

                playerProfiles.add(playerProfile);
            }
            cursor.close();
        }
    }
    
    private void addNewPlayerToDatabase(PlayerProfile playerProfile) {
        int newID = getMaxPlayerID() + 1;
        playerProfile.setId(newID);
        final String insertSQL = "insert into player (player_id, name) values (" + playerProfile.getId() + " , '" + playerProfile.getName() + "')";
        FlyDBManager.getInstance().execSQL(insertSQL);
    }
    
    private int getMaxPlayerID() {
        final String selectSQL = "select max(player_id) from player";
        
        DatabaseCursor cursor = FlyDBManager.getInstance().selectData(selectSQL);
        if (cursor != null && cursor.getCount() > 0) {
            cursor.next();
            int ret = cursor.getInt(0);
            cursor.close();
            return ret;
        }
        return 0;
    }
    
    public void saveFlyID(PlayerProfile playerProfile) {

        final String sql = "update player set fly_id=" + playerProfile.getFlyID() + " where player_id=" + playerProfile.getId();

        FlyDBManager.getInstance().execSQL(sql);
    }


	public void saveSecretKey(PlayerProfile profile) {
		final String sql = "update player set secret_key='" + profile.getSecretKey() + "' where player_id=" + profile.getId();

		FlyDBManager.getInstance().execSQL(sql);
	}
    
    public void updateIntColumn(PlayerProfile playerProfile, String colname, int newValue) {
        final String sql = "update player set " + colname + "=" + newValue + " where player_id=" + playerProfile.getId();
        FlyDBManager.getInstance().execSQL(sql);
    }
    
    /**
     * Changes the name of the current {@link PlayerProfile} to the defined
     * name. In case it is a new name {@link #currentPlayerProfileChanged()} is
     * called.
     * 
     * @param newPlayerProfileName
     */
    public void editCurrentPlayerName(String newPlayerProfileName) {
        if (!currentPlayerProfile.getName().equals(newPlayerProfileName)) {
            updateStringColumn(currentPlayerProfile, "name", newPlayerProfileName);
            currentPlayerProfile.setName(newPlayerProfileName);
           
            //change name on server
            if(currentPlayerProfile.getFlyID() > 0 ){
            	updateIntColumn(currentPlayerProfile, "is_newname_uploaded", 0);
            	currentPlayerProfile.setNewnameUploaded(false);
            	new PutUserService( new PutUserHttpRespListener(currentPlayerProfile)).execute(currentPlayerProfile);
            }
            
            currentPlayerProfileChanged();
        }
    }
    
    public void updateStringColumn(PlayerProfile playerProfile, String colname, String newValue) {
        final String sql = "update player set " + colname + "='" + newValue + "' where player_id=" + playerProfile.getId();
        FlyDBManager.getInstance().execSQL(sql);
    }
    
    public void deletePlayerProfile() {
        this.deletePlayerProfile(this.getCurrentPlayerProfile());
    }
    
    public void deletePlayerProfile(PlayerProfile playerProfile) {
        final String sql = "delete from player where player_id=" + playerProfile.getId();
        final String sql1 = "delete from score where player_id=" + playerProfile.getId();
        final String sql2 = "delete from fly_plane_Equiped where player_id=" + playerProfile.getId();
        final String sql3 = "delete from fly_plane_upgrade where player_id=" + playerProfile.getId();
        FlyDBManager.getInstance().execSQL(sql);
        FlyDBManager.getInstance().execSQL(sql1);
        FlyDBManager.getInstance().execSQL(sql2);
        FlyDBManager.getInstance().execSQL(sql3);
        
        if (playerProfile == this.getCurrentPlayerProfile()) {
            this.getAllPlayerProfiles().remove(playerProfile);
            playerProfile.clearSettingManager();
            this.setCurrentPlayer(this.getAllPlayerProfiles().get(0));
        } else {
            this.getAllPlayerProfiles().remove(playerProfile);
            playerProfile.clearSettingManager();
        }
    }

}
