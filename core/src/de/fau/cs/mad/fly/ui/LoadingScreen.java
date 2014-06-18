package de.fau.cs.mad.fly.ui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ProgressBar;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.utils.viewport.FitViewport;
import de.fau.cs.mad.fly.Fly;
import de.fau.cs.mad.fly.res.Assets;

/**
 * Displays the loading screen with a progress bar.
 * <p>
 * If the value of the progress bar reaches 100f the game screen is loaded.
 * 
 * @author Tobias Zangl
 */
public class LoadingScreen implements Screen {
	private final Fly game;

	private final SpriteBatch batch;
	private final Texture splashImg;

	private final Skin skin;
	private final Stage stage;
	private Table table;

	private ProgressBar progressBar;

	boolean doneLoading = false;
	int framesToRenderAfter = 3;

	private float progress = 0f;

	public LoadingScreen(final Fly game) {
		this.game = game;
		skin = game.getSkin();

		batch = new SpriteBatch();
		splashImg = Assets.manager.get(Assets.flyTextureLoadingScreen);

		stage = new Stage(new FitViewport(Gdx.graphics.getWidth(), Gdx.graphics.getHeight()));
		
		addLoadingProgress();
	}
	
	/**
	 * Adds the progress bar to the loading screen.
	 */
	private void addLoadingProgress() {
		table = new Table();
		table.pad(Gdx.graphics.getWidth() * 0.2f);
		table.padTop(Gdx.graphics.getHeight() * 0.7f);
		table.setFillParent(true);
		stage.addActor(table);

		progressBar = new ProgressBar(0f, 100f, 1f, false, skin);
		progressBar.setValue(progress);
		progressBar.scaleBy(100.0f);

		table.row().expand();
		table.add(progressBar).fill().pad(10f);
	}

	/**
	 * Sets the value of the progress bar.
	 * 
	 * @param value
	 *            the new value for the progress bar.
	 */
	public void setProgress(float value) {
		progress = value;
		progressBar.setValue(progress);
	}

	/**
	 * Increments the value of the progress bar.
	 * 
	 * @param value
	 *            the incremental value.
	 */
	public void incProgress(float value) {
		progress += value;
		progressBar.setValue(progress);
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClearColor(0.0f, 0.0f, 0.0f, 1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		batch.begin();
		batch.draw(splashImg, 0, 0, Gdx.graphics.getWidth(),
				Gdx.graphics.getHeight());
		batch.end();

		stage.act(delta);
		stage.draw();

		if(!Assets.manager.update()) {
			float currentProgress = ((float)Assets.manager.getLoadedAssets() /(float)(Assets.manager.getLoadedAssets() + Assets.manager.getQueuedAssets())) * 100f;
			if(currentProgress > progress) {
				setProgress(currentProgress);
			}
			return;
		} else {
			setProgress(100f);
			doneLoading = true;
		}

		if(doneLoading) {
			if(framesToRenderAfter-- == 0) {
				progress = 0f;
				doneLoading = false;
				framesToRenderAfter = 3;
				game.getLoader().finishLoading();
			}
		}
	}

	@Override
	public void resize(int width, int height) {
		stage.getViewport().update(width, height, true);
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage);
	}

	@Override
	public void hide() {
		// TODO Auto-generated method stub

	}

	@Override
	public void pause() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resume() {
		// TODO Auto-generated method stub

	}

	@Override
	public void dispose() {
		splashImg.dispose();
		batch.dispose();
		skin.dispose();
		stage.dispose();
	}
}
