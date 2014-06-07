package de.fau.cs.mad.fly;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.InputMultiplexer;
import com.badlogic.gdx.Screen;

/**
 * Provides a screen for the game itself.
 * 
 * @author Tobias Zangl
 */
public class GameScreen implements Screen{
	private final Fly game;
	
	private InputMultiplexer inputProcessor;
	

	public GameScreen(final Fly game) {
		this.game = game;

		inputProcessor = new InputMultiplexer(game.gameController.getCameraController(), new BackProcessor());
	}

	@Override
	public void render(float delta) {
		game.gameController.renderGame(delta);
	}

	@Override
	public void resize(int width, int height) {
		game.gameController.getStage().getViewport().update(width, height, true);
	}

	@Override
	public void show() {
		// delegate all inputs to the #inputProcessor
		inputProcessor.addProcessor(0, game.gameController.getStage());
		Gdx.input.setCatchBackKey(true);
		Gdx.input.setInputProcessor(inputProcessor);
		
		game.gameController.initGame();
	}

	@Override
	public void hide() {
		game.gameController.disposeGame();
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
		Assets.dispose();
	}
}
