package de.fau.cs.mad.fly.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input.Keys;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;

import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.HttpClient.FlyHttpResponseListener;
import de.fau.cs.mad.fly.HttpClient.GetLevelHighScoreService;
import de.fau.cs.mad.fly.HttpClient.GetLevelHighScoreService.LevelRecords;
import de.fau.cs.mad.fly.HttpClient.GetLevelHighScoreService.RecordItem;
import de.fau.cs.mad.fly.HttpClient.GetLevelHighScoreService.ResponseData;
import de.fau.cs.mad.fly.profile.LevelGroup;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;

/**
 * Screen to display the global highscores for a level group.
 * 
 * @author Qufang Fan, Lukas Hahmann <lukas.hahmann@gmail.com>
 * 
 */
public class GlobalHighscoreScreen extends BasicScreenWithBackButton {

    private Table outerTable;
    
    private ScrollPane loadingPane;
    
    /** space between the columns */
    private final float padding = 120;
    
    private LevelGroup levelGroup;
    
    public GlobalHighscoreScreen(BasicScreen screenToGoBack) {
        super(screenToGoBack);
    }
    
    public void setLevelGroup(LevelGroup group) {
        levelGroup = group;
    }
    
    public class GetLevelHighScoreListener implements FlyHttpResponseListener {
        
        final LevelGroup levelGroup;
        
        public GetLevelHighScoreListener(LevelGroup levelGroup) {
            this.levelGroup = levelGroup;
        }
        
        @Override
        public void successful(Object obj) {
            final ResponseData results = (ResponseData) obj;
            Gdx.app.postRunnable(new Runnable() {
                @Override
                public void run() {
                    Skin skin = SkinManager.getInstance().getSkin();
                    String styleName;
                    int currentFlyId = PlayerProfileManager.getInstance().getCurrentPlayerProfile().getFlyID();
                    loadingPane.remove();
                    
                    Table infoTable = new Table();
                    ScrollPane scrollPane;
                    scrollPane = new ScrollPane(infoTable, skin, "semiTransparentBackground");
                    scrollPane.setFadeScrollBars(false);
                    scrollPane.setScrollingDisabled(true, false);
                    
                    if (results != null && results.records.size() > 0) {
                        for (LevelRecords level : results.records) {
                            String levelname = levelGroup.getLevelName(level.levelID);
                            infoTable.row();
                            infoTable.add(new Label(I18n.t("level") + " " + levelname, skin)).left().height(UI.Buttons.TEXT_BUTTON_HEIGHT);
                            infoTable.row();
                            infoTable.add(new Label(I18n.t("rank"), skin)).left().height(UI.Buttons.TEXT_BUTTON_HEIGHT);
                            infoTable.add(new Label(I18n.t("player"), skin)).left().height(UI.Buttons.TEXT_BUTTON_HEIGHT).pad(0, padding, 0, padding);
                            infoTable.add(new Label(I18n.t("score"), skin)).left().height(UI.Buttons.TEXT_BUTTON_HEIGHT).pad(0, 0, 0, padding);
                            Gdx.app.log("GlobalHighscoreScreen", "level id:" + level.levelID + " count:" + level.records.size());
                            
                            for (RecordItem user : level.records) {
                                infoTable.row();
                                if(user.flyID == currentFlyId) {
                                    styleName = "default";
                                }
                                else {
                                    styleName = "darkGrey";
                                }
                                infoTable.add(new Label(user.rank + "", skin, styleName)).left().height(UI.Buttons.TEXT_BUTTON_HEIGHT);
                                infoTable.add(new Label(user.username + "#" + user.flyID, skin, styleName)).left().pad(0, padding, 0, padding);
                                infoTable.add(new Label(user.score + "", skin, styleName)).right().pad(0, 0, 0, padding);
                            }
                            infoTable.row();
                        }
                    } else {
                        infoTable.row();
                        infoTable.add(new Label(levelGroup.name, skin)).pad(0, 0, 0, padding).left().height(UI.Buttons.TEXT_BUTTON_HEIGHT);
                        infoTable.row();
                        infoTable.add(new Label(I18n.t("noScore"), skin, "darkGrey")).height(UI.Buttons.TEXT_BUTTON_HEIGHT);
                    }
                    
                    outerTable.clear();
                    outerTable.setFillParent(true);
                    outerTable.add(scrollPane);
                }
            });
            
        }
        
        @Override
        public void failed(String msg) {
            // debug output
            Gdx.app.log("PostScoreHttpRespListener", ".failed:" + msg);
            
            // show dialog with message for user
            Dialog uploadFailedMessage = new DialogWithOneButton(I18n.t("ConnectServerError"), I18n.t("ok")) {
                @Override
                public void result(Object result) {
                    // hide dialog
                    super.result(result);
                    // trigger the routine to go back to previous screen
                    backProcessor.keyDown(Keys.ESCAPE);
                }
            };
            uploadFailedMessage.show(stage);
        }
        
        @Override
        public void cancelled() {
            
        }
    }
    
    /**
     * generate Content
     * 
     * @see de.fau.cs.mad.fly.ui.BasicScreen#generateContent()
     */
    @Override
    protected void generateContent() {
        Skin skin = SkinManager.getInstance().getSkin();
        stage.clear();
        outerTable = new Table();
        outerTable.setFillParent(true);

        Table loadingInfoTable = new Table();
        loadingPane = new ScrollPane(loadingInfoTable, skin, "semiTransparentBackground");
        loadingPane.setFadeScrollBars(false);
        loadingPane.setScrollingDisabled(true, true);
        loadingInfoTable.add(new Label(I18n.t("StatusLoading"), skin)).height(UI.Buttons.TEXT_BUTTON_HEIGHT);
        outerTable.add(loadingPane);
        
        stage.addActor(outerTable);
        
        generateBackButton();
    }
    
    protected void generateContentDynamic() {
        final FlyHttpResponseListener listener = new GetLevelHighScoreListener(levelGroup);
        GetLevelHighScoreService getLevelHighScoreService = new GetLevelHighScoreService(listener);
        getLevelHighScoreService.execute(2, levelGroup.id);
    }
    
    @Override
    public void show() {
        super.show();
        generateContentDynamic();
    }
    
}
