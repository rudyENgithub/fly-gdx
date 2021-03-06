package de.fau.cs.mad.fly.player;

import java.util.HashMap;
import java.util.Map;

import com.badlogic.gdx.files.FileHandle;
import com.badlogic.gdx.graphics.g3d.Model;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.math.Vector3;

import de.fau.cs.mad.fly.features.IFeatureDispose;
import de.fau.cs.mad.fly.features.IFeatureInit;
import de.fau.cs.mad.fly.features.IFeatureLoad;
import de.fau.cs.mad.fly.features.IFeatureRender;
import de.fau.cs.mad.fly.features.IFeatureUpdate;
import de.fau.cs.mad.fly.player.gravity.IGravity;

/**
 * Interface that has to implemented by everything that a user can steer in Fly.
 * 
 * @author Lukas Hahmann
 * 
 */
public interface IPlane extends IFeatureLoad, IFeatureInit, IFeatureUpdate, IFeatureRender, IFeatureDispose {
    public static class Head {
        public int id;
        public String name;
        public String modelRef;
        public int levelGroupDependency;
        public float speed;
        private float rollingSpeed;
		public float azimuthSpeed;
		public float basicAzimuthSpeed;
        public int lives;
        public int basicLives;
        public float rotationSpeed = 0.0f;
        public Vector3 rotation = null;
        public Vector3 particleOffset = null;
        public FileHandle file;
        
        public float basicSpeed;
        
        private Map<String, Integer> upgradesBought = new HashMap<String, Integer>();

		/**
		 * @return the rollingSpeed, which is actually the azimuthSpeed.
		 */
		public float getRollingSpeed() {
			return azimuthSpeed;
		}

		/**
		 * @param rollingSpeed
		 *            the rollingSpeed to set
		 */
		public void setRollingSpeed(float rollingSpeed) {
			this.rollingSpeed = rollingSpeed;
		}
      		
    	@Override
		public String toString() {
			return "Head{" +
					"id=" + id +
					", name='" + name + '\'' +
					", modelRef='" + modelRef + '\'' +
					", levelGroupDependency=" + levelGroupDependency +
					", speed=" + speed +
					", rollingSpeed=" + rollingSpeed +
					", azimuthSpeed=" + azimuthSpeed +
					", lives=" + lives +
					", rotationSpeed=" + rotationSpeed +
					", rotation=" + rotation +
					", particleOffset=" + particleOffset +
					", file=" + file +
					", upgradesBought=" + upgradesBought +
					", upgradesEquiped=" + upgradesEquiped +
					'}';
		}
    	
        /**
         * @return the upgradesBought
         */
        public Map<String, Integer> getUpgradesBought() {
            return upgradesBought;
        }
        
        private Map<String, Integer> upgradesEquiped = new HashMap<String, Integer>();
        
        /**
         * @return the upgradesEquiped
         */
        public Map<String, Integer> getUpgradesEquiped() {
            return upgradesEquiped;
        }
        
//        public void addUpgradeBought(String name, int value) {
//            upgradesBought.put(name, value);
//        }
//        
//        public void addUpgradeEquiped(String name, int value) {
//            upgradesEquiped.put(name, value);
//        }
        
		public int getEquipedUpgradeCount(String name) {
			if (upgradesEquiped.containsKey(name)) {
				return upgradesEquiped.get(name);
			}
			return 0;
		}

		public int getBoughtUpgradeCount(String name) {
			if (upgradesBought.containsKey(name)) {
				return upgradesBought.get(name);
			}
			return 0;

		}
    }
    
    /**
     * Getter for the head.
     * 
     * @return the head of the plane.
     */
    public IPlane.Head getHead();
    
    /**
     * Getter for the model.
     * 
     * @return Model
     */
    public Model getModel();
    
    /**
     * Setter for the current speed of the plane.
     * 
     * @param speed
     */
    public void setCurrentSpeed(float speed);
    
    /**
     * Getter for the current speed of the plane.
     * 
     * @return currentSpeed
     */
    public float getCurrentSpeed();
    
    /**
     * Setter for the normal speed of the plane.
     * 
     * @param speed
     */
    public void setBaseSpeed(float speed);
    
    /**
     * Getter for the normal speed of the plane.
     * 
     * @return planeSpeed
     */
    public float getBaseSpeed();
    
    /**
     * Setter for the gravity of the plane.
     * 
     * @param gravity
     */
    public void setGravity(IGravity gravity);
    
    /**
     * Getter for the gravity of the plane.
     * 
     * @return gravity
     */
    public IGravity getGravity();
    
    /**
     * Getter for the azimuth speed of the plane.
     * 
     * @return azimuth speed
     */
    public float getAzimuthSpeed();
    
    /**
     * Getter for the rolling speed of the plane.
     * 
     * @return rolling speed
     */
    public float getRollingSpeed();
    
    
    /**
     * Returns the transformation matrix.
     * 
     * @return transform
     */
    public Matrix4 getTransform();
    
    /**
     * Returns the position.
     * 
     * @return position
     */
    public Vector3 getPosition();
    
    /**
     * Rotates the plane with given roll and azimuth dir.
     * 
     * @param rollDir
     * @param azimuthDir
     */
    public void rotate(float rollDir, float azimuthDir, float deltaFactor);
    
    public void shift(Vector3 vector);
    
    public void resetOnRail(float railX, float railY, float railPos);
}