package de.fau.cs.mad.fly.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Preferences;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all the settings in a HashMap
 * 
 * @author Tobias Zangl
 */
public class SettingManager {	
	private Skin skin;
	private Preferences prefs;
	private Map<String, Setting> settingMap;
	private List<String> settingList;
	
	public List<String> getSettingList() {
		return settingList;
	}
	
	public Map<String, Setting> getSettingMap() {
		return settingMap;
	}

	public SettingManager(String file, Skin skin) {
		prefs = Gdx.app.getPreferences(file);
		this.skin = skin;
		settingMap = new HashMap<String, Setting>();
		settingList = new ArrayList<String>();
	}

	
	/**
	 * Adds a new Setting with a TextField and a String value.
	 * @param id
	 *            the id of the Setting
	 * @param description
	 *            the description of the Setting
	 * @param value
	 *            the default value of the Setting
	 */
	public void addSetting(String id, String description, String value) {
		if(!prefs.contains(id)) {
			prefs.putString(id, value);
			prefs.flush();
		} else {
			value = prefs.getString(id);
		}
		
		Setting setting = new Setting(this, id, description, value, skin);
		settingMap.put(id, setting);
		settingList.add(id);
	}
	
	/**
	 * Adds a new Setting with a SelectionBox and an int value.
	 * @param id
	 *            the id of the Setting
	 * @param description
	 *            the description of the Setting
	 * @param value
	 *            the default value of the Setting
	 * @param selectionList
	 *            the possible selections of the SelectionBox
	 */
	public void addSetting(String id, String description, int value, String[] selectionList) {
		if(!prefs.contains(id)) {
			prefs.putInteger(id, value);
			prefs.flush();
		} else {
			value = prefs.getInteger(id);
		}
		
		Setting setting = new Setting(this, id, description, value, selectionList, skin);
		settingMap.put(id, setting);
		settingList.add(id);
	}
	
	/**
	 * Adds a new Setting with a CheckBox and a boolean value.
	 * @param id
	 *            the id of the Setting
	 * @param description
	 *            the description of the Setting
	 * @param value
	 *            the default value of the Setting
	 */
	public void addSetting(String id, String description, boolean value) {
		if(!prefs.contains(id)) {
			prefs.putBoolean(id, value);
			prefs.flush();
		} else {
			value = prefs.getBoolean(id);
		}
		
		Setting setting = new Setting(this, id, description, value, skin);
		settingMap.put(id, setting);
		settingList.add(id);
	}
	
	/**
	 * Adds a new Setting with a Slider and a float value.
	 * @param id
	 *            the id of the Setting
	 * @param description
	 *            the description of the Setting
	 * @param value
	 *            the default value of the Setting
	 * @param min
	 *            the minimum value of the Slider
	 * @param max
	 *            the maximum value of the Slider
	 * @param stepSize
	 *            the step size of the Slider
	 */
	public void addSetting(String id, String description, float value, float min, float max, float stepSize) {
		if(!prefs.contains(id)) {
			prefs.putFloat(id, value);
			prefs.flush();
		} else {
			value = prefs.getFloat(id);
		}
		
		Setting setting = new Setting(this, id, description, value, min, max, stepSize, skin);
		settingMap.put(id, setting);
		settingList.add(id);
	}
	
	/**
	 * Getter for the Preferences.
	 */
	public Preferences getPreferences() {
		return prefs;
	}
	
	/**
	 * Getter for the Text value.
	 */
	public String getTextValue(String id) {
		return settingMap.get(id).getText();
	}
	
	/**
	 * Getter for the Selection value.
	 */
	public int getSelectionValue(String id) {
		return settingMap.get(id).getSelection();
	}
	
	/**
	 * Getter for the CheckBox value.
	 */
	public boolean getCheckBoxValue(String id) {
		return settingMap.get(id).getCheckBox();
	}
	
	/**
	 * Getter for the Slider value.
	 */
	public float getSliderValue(String id) {
		return settingMap.get(id).getSlider();
	}
	
	/**
	 * Saves all settings in the preference-file.
	 */
	public void saveSettings() {
		for(Map.Entry<String, Setting> entry : settingMap.entrySet()) {
			entry.getValue().saveSetting();
		}
		prefs.flush();
	}

}
