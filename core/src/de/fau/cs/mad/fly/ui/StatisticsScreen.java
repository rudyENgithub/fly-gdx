package de.fau.cs.mad.fly.ui;

import java.util.Map;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.scenes.scene2d.Event;
import com.badlogic.gdx.scenes.scene2d.EventListener;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.ui.Dialog;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.SelectBox;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton.TextButtonStyle;
import com.badlogic.gdx.scenes.scene2d.ui.TextField;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.badlogic.gdx.utils.Array;

import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.I18n;
import de.fau.cs.mad.fly.HttpClient.FlyHttpResponseListener;
import de.fau.cs.mad.fly.HttpClient.PostHighscoreService;
import de.fau.cs.mad.fly.HttpClient.PostUserService;
import de.fau.cs.mad.fly.profile.LevelManager;
import de.fau.cs.mad.fly.profile.PlayerProfile;
import de.fau.cs.mad.fly.profile.PlayerProfileManager;
import de.fau.cs.mad.fly.profile.Score;
import de.fau.cs.mad.fly.profile.ScoreDetail;
import de.fau.cs.mad.fly.profile.ScoreManager;
import de.fau.cs.mad.fly.settings.AppSettingsManager;

/**
 * UI for checking scores of user, also for add and change user
 * 
 * 
 * @author Qufang Fan
 */
public class StatisticsScreen extends BasicScreen {

	private TextButton addUserButton;
	private TextButton uploadScoreButton;
	private TextButtonStyle textButtonStyle;
	private TextButton globalHighScoreButton;
	private Table infoTable;
	private Table userTable;
	private Table scoreTable;	
	private TextField newUserField;
	
	private PlayerProfile playerProfile;
	private int selectedUserindex = 0;
	private SelectBox<String> userList;
		
	private void initButtons() {
		textButtonStyle = skin.get(UI.Buttons.DEFAULT_STYLE, TextButtonStyle.class);
		addUserButton = new TextButton(I18n.t("addUserButtonText"), textButtonStyle);

		addUserButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				String name = newUserField.getText().trim();
				if (!"".equals(name))// todo more check
				{
					for(PlayerProfile playerProfile : PlayerProfileManager.getInstance().getAllPlayerProfiles())
					{
						if(playerProfile.getName().equals(name)){
							new Dialog("", skin) {
								{
									text("The user already exists!");
									button("OK");
								}
							}.show(stage);
							return;
						}
					}
					PlayerProfile playerProfile = new PlayerProfile();
					playerProfile.setName(name);

					PlayerProfileManager.getInstance().savePlayer(playerProfile);
					updateUserTable();
					new Dialog("", skin) {
						{
							text("User Added.");
							button("OK");
						}
					}.show(stage);
				}
				else {
					new Dialog("", skin) {
						{
							text("User name should not be null!");
							button("OK");
						}
					}.show(stage);
				}
			}
		});
		
		globalHighScoreButton = new TextButton("Global highscores",
				textButtonStyle);
		globalHighScoreButton.addListener(new ClickListener() {
			@Override
			public void clicked(InputEvent event, float x, float y) {
				((Fly) Gdx.app.getApplicationListener()).setGlobalHighScoreScreen();
			}
		});
	}
	
	private void genarateUserTable() {
		userTable.clear();
		// add user name and add user buttons
		String userName = playerProfile.getName();
		userTable.add(new Label(I18n.t("usernameLableText"), skin)).pad(6f);
		
		userList = new SelectBox<String>(skin);		
		Array<String> nameList = new Array<String>();
		java.util.List<PlayerProfile> playerList = PlayerProfileManager.getInstance().getAllPlayerProfiles();
		for (int i = 0; i < playerList.size(); i++) {
			PlayerProfile playerProfile = playerList.get(i);
			nameList.add(playerProfile.getName());
			if (userName.equals(playerProfile.getName())) {
				selectedUserindex = i;
			}
		}
		userList.setItems(nameList);
		userList.setSelectedIndex(selectedUserindex);
		userList.getSelection().setRequired(false);
		userList.getSelection().setToggle(true);
		userList.addListener(new EventListener() {			
			@Override
			public boolean handle(Event event) {
				if(userList.getSelectedIndex()>=0 && userList.getSelectedIndex() != selectedUserindex)
				{
					selectedUserindex = userList.getSelectedIndex();
					PlayerProfile temPlayerProfile = 
							PlayerProfileManager.getInstance().getAllPlayerProfiles().get(selectedUserindex);
					PlayerProfileManager.getInstance().setCurrentPlayer(temPlayerProfile);
					AppSettingsManager.Instance.setIntegerSetting(AppSettingsManager.CHOSEN_USER,
							temPlayerProfile.getId());
					genarateScoreTable();
				}
				return false;
			}
		});
		userTable.add(userList).width(800).pad(6f).uniform();
		userTable.row().expand();

		newUserField = new TextField("", skin);
		newUserField.setMessageText("User name");
		userTable.add(newUserField).width(1200f).height(200f).pad(6f).uniform();
		userTable.add(addUserButton).pad(6f).uniform();
		userTable.row().expand();
		userTable.layout();
	}
	
	private void updateUserTable() {
		String userName = playerProfile.getName();
		userList.clear();		
		Array<String> nameList = new Array<String>();
		java.util.List<PlayerProfile> playerList = PlayerProfileManager.getInstance().getAllPlayerProfiles();
		for (int i = 0; i < playerList.size(); i++) {
			PlayerProfile playerProfile = playerList.get(i);
			nameList.add(playerProfile.getName());
			if (userName.equals(playerProfile.getName())) {
				selectedUserindex = i;
			}
		}
		userList.setItems(nameList);
		userList.setSelectedIndex(selectedUserindex);
		userList.getSelection().setRequired(false);
		userList.getSelection().setToggle(true);		
		userList.layout();
	}
	private void genarateScoreTable(){	
		new Thread(new showScore()).start();
	}
	
	@Override
	protected void generateContent() {
		playerProfile = PlayerProfileManager.getInstance().getCurrentPlayerProfile();
		initButtons();
		stage.clear();
		final Table table = new Table();
		table.pad(Gdx.graphics.getWidth() * 0.1f);
		table.setFillParent(true);
		table.debug();
		stage.addActor(table);

		infoTable = new Table();		
		userTable = new Table();
		genarateUserTable();
		infoTable.add(userTable);
		infoTable.row();
		scoreTable = new Table();
		scoreTable.add(new Label("Loading...", skin));
		infoTable.add(scoreTable);

		final ScrollPane statisticsPane = new ScrollPane(infoTable, skin);
		statisticsPane.setFadeScrollBars(false);
		statisticsPane.setScrollingDisabled(true, false);
		statisticsPane.setStyle(skin.get(UI.Window.TRANSPARENT_SCROLL_PANE_STYLE,
				ScrollPane.ScrollPaneStyle.class));
		table.row().expand();
		table.add(statisticsPane);
	}
	
	@Override
	public void show() {
		super.show();
		genarateScoreTable();
	}
	
	public class showScore implements Runnable {
		Map<String, Score> scores;

		@Override
		public void run() {
			scores = ScoreManager.getInstance().getcurrentBestScores();			

			//it is faster if we remove this, but it may also bring in UI render exception
//			Gdx.app.postRunnable(new Runnable() {				
//				@Override
//				public void run() {		
					scoreTable.clear();
					// add scores details
					boolean haveScore = false;

					for (String levelID : scores.keySet()) {
						Score score = scores.get(levelID);
						if (score != null && score.getTotalScore() > 0) {
							haveScore = true;
							String levelname = LevelManager.getInstance().getLevelName(
									Integer.valueOf(levelID));
							scoreTable.add(new Label(levelname, skin)).pad(6f).right();

							scoreTable.add(new Label(score.getTotalScore() + "", skin)).pad(6f)
									.uniform();
							for (ScoreDetail detail : score.getScoreDetails()) {
								scoreTable.row().expand();
								scoreTable.add(new Label(I18n.t(detail.getDetailName()), skin))
										.pad(6f).right();
								scoreTable.add(new Label(detail.getValue(), skin)).pad(6f)
										.uniform();
							}

							final PostHighscoreService.RequestData requestData = new PostHighscoreService.RequestData();
							requestData.FlyID = PlayerProfileManager.getInstance().getCurrentPlayerProfile()
									.getFlyID();
							requestData.LevelID = Integer.valueOf(levelID);
							requestData.Score = score.getTotalScore();
							final FlyHttpResponseListener postScoreListener = new PostScoreHttpRespListener();
							final PostHighscoreService postHighscoreService = new PostHighscoreService(
									postScoreListener, requestData);

							uploadScoreButton = new TextButton(I18n.t("uploadScoreButtonText"),
									textButtonStyle);
							uploadScoreButton.addListener(new ClickListener() {
								@Override
								public void clicked(InputEvent event, float x, float y) {
									if (PlayerProfileManager.getInstance().getCurrentPlayerProfile().getFlyID() <= 0) {
										FlyHttpResponseListener listener = new PostUserHttpRespListener(
												requestData, postHighscoreService);
										PostUserService postUser = new PostUserService(listener);

										postUser.execute(PlayerProfileManager.getInstance()
												.getCurrentPlayerProfile().getName());
									} else {
										postHighscoreService.execute();
									}
								}
							});
							scoreTable.row().expand();
							scoreTable.add(uploadScoreButton).pad(6f).colspan(2);
						}
					}

					// if no score at all
					if (!haveScore) {
						scoreTable.row().expand();
						scoreTable.add(new Label(I18n.t("noScore"), skin)).pad(6f).uniform();
					}

					// global high score button
					scoreTable.row().expand();
					scoreTable.add(new Label("", skin)).pad(6f).uniform();
					scoreTable.row().expand();
				
					scoreTable.add(globalHighScoreButton).colspan(2);

					//scoreTable.setColor(0, 0, 0, 0f);
					//scoreTable.addAction(Actions.fadeIn(2f));
					
					Gdx.app.log("StaticScreen",
							"end generateContentDynamic " + System.currentTimeMillis());
					scoreTable.layout();
//				}
//			});

		}
	}

	public class PostUserHttpRespListener implements FlyHttpResponseListener {
		final PostHighscoreService.RequestData requestData;
		final PostHighscoreService postHighscoreService;

		public PostUserHttpRespListener(PostHighscoreService.RequestData data,
				PostHighscoreService service) {
			requestData = data;
			postHighscoreService = service;
		}

		@Override
		public void successful(Object obj) {

			int flyID = Integer.valueOf(obj.toString());
			PlayerProfileManager.getInstance().getCurrentPlayerProfile().setFlyID(flyID);
			PlayerProfileManager.getInstance().saveFlyID(PlayerProfileManager.getInstance().getCurrentPlayerProfile());
			requestData.FlyID = flyID;
			postHighscoreService.execute();
		}

		@Override
		public void failed(String msg) {
			final String msgg = msg;
			Gdx.app.postRunnable(new Runnable() {
				@Override
				public void run() {
					new Dialog("", skin) {
						{
							text(msgg.substring(0, 20) + "...");
							button("OK");
						}
					}.show(stage);				
					
				}
			});
		}

		@Override
		public void cancelled() {
		}
	}

	public class PostScoreHttpRespListener implements FlyHttpResponseListener {
		@Override
		public void successful(Object obj) {
//			Gdx.app.postRunnable(new Runnable() {
//				@Override
//				public void run() {
					new Dialog("", skin) {
						{
							text("Uploaded!");
							button("OK");
						}
					}.show(stage);
//				}
//			});
		}

		@Override
		public void failed(String msg) {
			final String msgg = msg;
			// Since we are downloading on a background thread, post
			// a runnable to touch ui
//			Gdx.app.postRunnable(new Runnable() {
//				@Override
//				public void run() {
					new Dialog("", skin) {
						{
							text(msgg.substring(0, 20) + "...");
							button("OK");
						}
					}.show(stage);
//				}
//			});
		}

		@Override
		public void cancelled() {
		}
	};	
		 
}
