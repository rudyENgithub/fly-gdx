package de.fau.cs.mad.fly.ui;

import java.util.ArrayList;
import java.util.List;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.assets.AssetDescriptor;
import com.badlogic.gdx.graphics.PerspectiveCamera;
import com.badlogic.gdx.graphics.g3d.Environment;
import com.badlogic.gdx.graphics.g3d.ModelBatch;
import com.badlogic.gdx.graphics.g3d.attributes.ColorAttribute;
import com.badlogic.gdx.graphics.g3d.environment.DirectionalLight;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.viewport.FillViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.game.GameModel;
import de.fau.cs.mad.fly.game.GameObject;
import de.fau.cs.mad.fly.graphics.shaders.FlyShaderProvider;
import de.fau.cs.mad.fly.player.IPlane;
import de.fau.cs.mad.fly.profile.PlaneManager;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;
import de.fau.cs.mad.fly.res.Assets;
import de.fau.cs.mad.fly.res.PlaneUpgrade;

/**
 * The Screen in which the Player can buy and equip upgrade for his Planes
 * 
 * @author Sebastian
 * 
 */
public class PlaneUpgradeDetailScreen extends BasicScreenWithBackButton implements Screen {

	private Skin skin;
    private Viewport viewport;
    
    private float screenHeight = Gdx.graphics.getHeight();
    private float screenWidth = Gdx.graphics.getWidth();
    
    private IPlane.Head currentPlane;
    
    private ModelBatch batch;
    private Environment environment;
    private PerspectiveCamera camera;   
    
    private GameObject currentSpaceship;
    private Vector3 xRotationAxis = new Vector3(1.f, 0.f, 0.f);
    private Vector3 yRotationAxis = new Vector3(0.f, 1.f, 0.f);
    
    private PlaneUpgrade chosenUpgrade;
    
    private Table upgradeDetailTable;
    final TextButton buyButton, upgradeButton, downgradeButton;
    final LabelStyle labelStyle;
    final Label upgradeNameLabel, upgradeCostLabel, currentMoneyLabel;
    final Label upgradeMaximumLabel, upgradeBoughtLabel, upgradeEquipedLabel;
    
    final String[] names = { "speed", "pitch", "turnSpeed", "lives" };
    
    /** Labels to show the currents status of the current plane*/
    private Label nameLabel, speedLabel, rollingSpeedLabel, azimuthSpeedLabel, livesLabel;
    
    /**
	 * @return the chosenUpgrade
	 */
	public PlaneUpgrade getChosenUpgrade() {
		return chosenUpgrade;
	}

	/**
	 * @param chosenUpgrade the chosenUpgrade to set
	 */
	public void setChosenUpgrade(PlaneUpgrade chosenUpgrade) {
		this.chosenUpgrade = chosenUpgrade;
	}

	public PlaneUpgradeDetailScreen(BasicScreen screenToGoBack, PlaneUpgrade upgrade) {
        super(screenToGoBack);
        this.chosenUpgrade = upgrade;      
        
        currentPlane = PlaneManager.getInstance().getChosenPlane();

        setUpEnvironment();
        setUpCamera();
        
        skin = SkinManager.getInstance().getSkin();
        
        buyButton = new TextButton(I18n.t("buy"), skin);
        upgradeButton = new TextButton(I18n.t("equip"), skin);
        downgradeButton = new TextButton(I18n.t("unequip"), skin);
        initButtonsListener();
        
        labelStyle = skin.get( LabelStyle.class);
        upgradeNameLabel = new Label("", labelStyle);
        upgradeCostLabel = new Label("", labelStyle);
        currentMoneyLabel = new Label("", labelStyle);
        upgradeMaximumLabel = new Label("", labelStyle);
        upgradeBoughtLabel = new Label("", labelStyle);
        upgradeEquipedLabel = new Label("", labelStyle);
        
        batch = new ModelBatch(null, new FlyShaderProvider(), null);
        
        float widthScalingFactor = UI.Window.REFERENCE_WIDTH / (float) screenWidth;
        float heightScalingFactor = UI.Window.REFERENCE_HEIGHT / (float) screenHeight;
        float scalingFactor = Math.max(widthScalingFactor, heightScalingFactor);
        viewport = new FillViewport(Gdx.graphics.getWidth() * scalingFactor, Gdx.graphics.getHeight() * scalingFactor, stage.getCamera());
        stage.setViewport(viewport);
        
        upgradeDetailTable = new Table();
       
        initChosenPlaneDetail();
        initUpgradeDetailTable();
        generateBackButton();       
    }
    
	private void initUpgradeDetailTable() {
		Table outTable = new Table();
		outTable.setFillParent(true);
		outTable.pad(0f);
		ScrollPane scrollPane = new ScrollPane(outTable, skin);
		scrollPane.setFillParent(true);
		scrollPane.setFadeScrollBars(false);

		updateUpgradeDetailTable();
		
		outTable.add().width(UI.Buttons.IMAGE_BUTTON_WIDTH);	
		outTable.add(upgradeDetailTable).right().top().padRight(50f).padTop(0f).expand();
		stage.addActor(scrollPane);
	}
   
    /**
     * Creates the Buttons for the Upgrades
     * 
     * @param plane
     *            the current Plane the Player has chosen and should be upgraded
     */
    private void initButtonsListener() {
        // Creating the button to buy the Upgrades
        buyButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                boolean canBeBought = PlaneManager.getInstance().upgradeCanBeBought(chosenUpgrade);
                
                if (canBeBought) {
                    // Gdx.app.log("upgrades", currentUpgrade.name +
                    // PlaneManager.getInstance().getChosenPlane().upgradesBought.get(currentUpgrade.name));
                    
                    PlaneManager.getInstance().buyUpgradeForPlane(chosenUpgrade.name);
                    updateUpdateDetails();
                    updateChosenPlaneDetail();
                    
                    upgradeButton.setText(I18n.t("equip"));
                    upgradeButton.setDisabled(false);//.setColor(Color.WHITE);
                    
                    if (!PlaneManager.getInstance().upgradeCanBeBought(chosenUpgrade)) {
                        int money = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getMoney();
                        if (money < chosenUpgrade.price) {
                            buyButton.setDisabled(true);//.setText(I18n.t("tooExpensive"));
                        } else {
                            buyButton.setDisabled(true);//.setText(I18n.t("allreadyMaximal"));
                        }
                        //buyButton.setColor(Color.GRAY);
                    }
                }
            }
        });
        
        // Creating the button to equip the Upgrades
        upgradeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String name = chosenUpgrade.name;
                int equiped = PlaneManager.getInstance().getChosenPlane().getUpgradesEquiped().get(name);
                int bought = PlaneManager.getInstance().getChosenPlane().getUpgradesBought().get(name);
                if (equiped != bought) {
                    if (equiped == 0) {
                        downgradeButton.setText(I18n.t("unequip"));
                        downgradeButton.setDisabled(false);//.setColor(Color.WHITE);
                    }
                    
                    PlaneManager.getInstance().upgradePlane(name, 1);
                    updateUpdateDetails();
                    updateChosenPlaneDetail();
                    
                    if (equiped + 1 == bought) {
                        upgradeButton.setDisabled(true);//.setText(I18n.t("allreadyMaximal"));
                        //upgradeButton.setColor(Color.GRAY);
                    }
                }
            }
        });
        
        // Creating the button to unequip the Upgrades
        downgradeButton.addListener(new ClickListener() {
            @Override
            public void clicked(InputEvent event, float x, float y) {
                String name = chosenUpgrade.name;
                int equiped = PlaneManager.getInstance().getChosenPlane().getUpgradesEquiped().get(name);
                
                if (equiped != 0) {
                    if (equiped == chosenUpgrade.timesAvailable) {
                        upgradeButton.setText(I18n.t("equip"));
                        upgradeButton.setDisabled(false);//.setColor(Color.WHITE);
                    }
                    
                    PlaneManager.getInstance().upgradePlane(name, -1);
                    updateUpdateDetails();
                    updateChosenPlaneDetail();
                    
                    if (equiped - 1 == 0) {
                        downgradeButton.setDisabled(true);//.setText(I18n.t("allreadyMinimal"));
                        //downgradeButton.setColor(Color.GRAY);
                    }
                }
            }
        });
    }
    
    private void updateUpdateDetails() {    	
        int equiped = PlaneManager.getInstance().getChosenPlane().getEquipedUpgradeCount(chosenUpgrade.name);        
        int bought = PlaneManager.getInstance().getChosenPlane().getBoughtUpgradeCount(chosenUpgrade.name);
        int money = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getMoney();
        setOptionButtonsState( money, bought, equiped);
        currentMoneyLabel.setText( money + "");
        upgradeBoughtLabel.setText(bought + "");
        upgradeEquipedLabel.setText(equiped + "");      
    }
    
    /**
     * checking if the Upgrade can be bought, equipped, unequipped
     * and then set the buttons status
     * @param money
     * @param bought
     * @param equiped
     */
	private void setOptionButtonsState( int money, int bought, int equiped) {
		
		if (!PlaneManager.getInstance().upgradeCanBeBought(chosenUpgrade)) {
			if (money < chosenUpgrade.price) {
				buyButton.setDisabled(true);
			} else {
				buyButton.setDisabled(true);
			}
		} else {
			buyButton.setText(I18n.t("buy"));
			buyButton.setDisabled(false);
		}

		if (equiped == bought) {
			upgradeButton.setDisabled(true);
		} else {
			upgradeButton.setDisabled(false);
		}

		if (equiped == 0) {
			downgradeButton.setDisabled(true);
		} else {
			downgradeButton.setText(I18n.t("unequip"));
			downgradeButton.setDisabled(false);
		}
	}
    
    /**
     * Adds all Buttons and Labels needed after selecting an Upgrade to
     * buy/equip
     */
	private void updateUpgradeDetailTable() {
		upgradeDetailTable.clear();
		if (chosenUpgrade == null)
			return;
		int equiped = PlaneManager.getInstance().getChosenPlane().getEquipedUpgradeCount(chosenUpgrade.name);
		int bought = PlaneManager.getInstance().getChosenPlane().getBoughtUpgradeCount(chosenUpgrade.name);
		int money = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getMoney();

		// setting up the Lables and Buttons according to current Upgrade
		setOptionButtonsState( money, bought, equiped);
		
		upgradeDetailTable.add(new Label(I18n.t("upgrade") + ": ", skin)).right();
		upgradeNameLabel.setText(I18n.t(chosenUpgrade.name));
		upgradeDetailTable.add(upgradeNameLabel).left();
		upgradeDetailTable.row();
		
		List<String> changes = getUpdateChanges();
		for (int i = 0; i < changes.size(); i++) {
			if (i == 0) {
				upgradeDetailTable.add(new Label(I18n.t("changes") + ": ", skin)).right();
				upgradeDetailTable.add(new Label(changes.get(i), skin)).left();
				upgradeDetailTable.row();
			} else {
				upgradeDetailTable.add();
				upgradeDetailTable.add(new Label(changes.get(i), skin)).left();
				upgradeDetailTable.row();
			}
		}

		upgradeDetailTable.add(new Label(I18n.t("cost") + ": ", skin)).right();
		upgradeCostLabel.setText(chosenUpgrade.price + "");
		upgradeDetailTable.add(upgradeCostLabel).left();
		upgradeDetailTable.row();

		upgradeDetailTable.add(new Label(I18n.t("currentMoney") + ": ", skin)).right();
		currentMoneyLabel.setText("" + money);
		upgradeDetailTable.add(currentMoneyLabel).left();
		upgradeDetailTable.row();
		
		upgradeDetailTable.add(new Label(I18n.t("maximum") + ": ", skin)).right();
		upgradeMaximumLabel.setText(chosenUpgrade.timesAvailable + "");
		upgradeDetailTable.add(upgradeMaximumLabel).left();
		upgradeDetailTable.row();
		
		upgradeDetailTable.add(new Label(I18n.t("bought") + ": ", skin)).right();
		upgradeBoughtLabel.setText(bought + "");
		upgradeDetailTable.add(upgradeBoughtLabel).left();
		upgradeDetailTable.row();
		
		upgradeDetailTable.add();
		upgradeDetailTable.add(buyButton).space(20).left();
		upgradeDetailTable.row();
		
		upgradeDetailTable.add(new Label(I18n.t("equiped") + ": ", skin)).right();
		upgradeEquipedLabel.setText(equiped + "");
		upgradeDetailTable.add(upgradeEquipedLabel).left();
		upgradeDetailTable.row();		
		
		upgradeDetailTable.add(downgradeButton).space(20).left();
		upgradeDetailTable.add(upgradeButton).space(20).left();
		upgradeDetailTable.row();
	}

    /**
     * Returns a String that describes the changes by the current Upgrade
     * 
     * @return The String that describes the changes by the current Upgrade
     */
    private List<String> getUpdateChanges() {
        ArrayList<String> changes = new ArrayList<String>();
        int[] values = chosenUpgrade.upgradeValues;        
        for (int i = 0; i < values.length; i++) {
            if (values[i] != 0) {
                changes.add( I18n.t(names[i]) + " " + I18n.t("by") + " " + values[i]);
            }
        }        
        return changes;
    }   
   
    /**
     * Initializes the overlay which contains the details of the current
     * spaceship
     */
    private void initChosenPlaneDetail() {
        LabelStyle labelStyle = skin.get(LabelStyle.class);
        Table outTable = new Table();
        outTable.setFillParent(true);
        Table planeDetailTable = new Table();
        //planeDetailTable.setFillParent(true);
        
        planeDetailTable.add(new Label(I18n.t("name") + ":", labelStyle)).pad(UI.Tables.PADDING).right();
        nameLabel = new Label("", labelStyle);
        planeDetailTable.add(nameLabel).pad(UI.Tables.PADDING).left();
        planeDetailTable.row().left().top().expand();
        
        planeDetailTable.add(new Label(I18n.t("speed") + ":", labelStyle)).pad(UI.Tables.PADDING).right();
        speedLabel = new Label("", labelStyle);
        planeDetailTable.add(speedLabel).pad(UI.Tables.PADDING).left();
        planeDetailTable.row().left().top().expand();
        
        planeDetailTable.add(new Label(I18n.t("pitch") + ":", labelStyle)).pad(UI.Tables.PADDING).right();
        rollingSpeedLabel = new Label("", labelStyle);
        planeDetailTable.add(rollingSpeedLabel).pad(UI.Tables.PADDING).left();
        planeDetailTable.row().left().top().expand();
        
        planeDetailTable.add(new Label(I18n.t("turnSpeed") + ":", labelStyle)).pad(UI.Tables.PADDING).right();
        azimuthSpeedLabel = new Label("", labelStyle);
        planeDetailTable.add(azimuthSpeedLabel).pad(UI.Tables.PADDING).left();
        planeDetailTable.row().left().top().expand();
        
        planeDetailTable.add(new Label(I18n.t("lives") + ":", labelStyle)).pad(UI.Tables.PADDING).right();
        livesLabel = new Label("", labelStyle);
        planeDetailTable.add(livesLabel).pad(UI.Tables.PADDING).left();
        planeDetailTable.row().left().top().expand();
        outTable.add(planeDetailTable).top().left().expand().pad(100f);
        stage.addActor(outTable);
    }
    
    /**
     * Updates the overlay with the details of the current plane
     */
    private void updateChosenPlaneDetail() {
        nameLabel.setText(currentPlane.name);
        speedLabel.setText(Float.toString(currentPlane.speed));
        rollingSpeedLabel.setText(Float.toString(currentPlane.rollingSpeed));
        azimuthSpeedLabel.setText(Float.toString(currentPlane.azimuthSpeed));
        livesLabel.setText(Integer.toString(currentPlane.lives));
    }
    
    private void loadCurrentPlane() {
    	 // adding the preview of the first plane
        String ref = "models/planes/" + currentPlane.modelRef + "/" + currentPlane.modelRef;        
        Assets.load(new AssetDescriptor<GameModel>(ref, GameModel.class));
        GameModel model = Assets.manager.get(ref, GameModel.class);
        
        currentSpaceship = new GameObject(model, "spaceship");
        
        currentSpaceship.transform.rotate(yRotationAxis, 180.f);
        xRotationAxis.rotate(yRotationAxis, -180.f);
        currentSpaceship.transform.rotate(xRotationAxis, -20.f);
        yRotationAxis.rotate(xRotationAxis, 20.f);
    }
    
    /**
     * Sets up the environment for the level with its light.
     */
    private void setUpEnvironment() {
    	environment = new Environment();
        environment.set(new ColorAttribute(ColorAttribute.AmbientLight, 0.4f, 0.4f, 0.4f, 1f));
        environment.add(new DirectionalLight().set(0.8f, 0.8f, 0.8f, -1f, -0.8f, -0.2f));
    }
    
    /**
     * Sets up the camera for the initial view.
     */
    private final void setUpCamera() {
        camera = new PerspectiveCamera(67, screenWidth, screenHeight);
        
        camera.position.set(0.f, 0.f, -2.f);
        camera.lookAt(0.f, 0.f, 0.f);
        camera.near = 0.1f;
        camera.far = 100.f;
        camera.update();
        
        //camVec = camera.position.cpy();
    }
    
    @Override
    public void render(float delta) {        
       super.render(delta);
        
        batch.begin(camera);
        currentSpaceship.render(batch, environment, camera);
        batch.end();
    }
    
    
    @Override
    public void resize(int width, int height) {
        stage.getViewport().update(width, height, true);
    }
    
	@Override
	public void show() {
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(inputProcessor);

		currentPlane = PlaneManager.getInstance().getChosenPlane();

		loadCurrentPlane();
		updateUpgradeDetailTable();
		updateChosenPlaneDetail();

	}
    
    @Override
    public void dispose() {
        stage.dispose();
    }    
}
