package de.bitbrain.braingdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.scenes.scene2d.Stage;

import de.bitbrain.braingdx.AbstractScreen;
import de.bitbrain.braingdx.GameObject;
import de.bitbrain.braingdx.apps.RPGTest;
import de.bitbrain.braingdx.assets.Assets;
import de.bitbrain.braingdx.assets.SharedAssetManager;
import de.bitbrain.braingdx.graphics.SpriteRenderer;
import de.bitbrain.braingdx.graphics.lighting.PointLightBehavior;
import de.bitbrain.braingdx.graphics.pipeline.RenderLayer;
import de.bitbrain.braingdx.graphics.pipeline.RenderPipe;
import de.bitbrain.braingdx.postprocessing.effects.Bloom;
import de.bitbrain.braingdx.postprocessing.effects.Fxaa;

public class RPGScreen extends AbstractScreen<RPGTest> {

    private static final int SOLDIER = 1;

    public RPGScreen(RPGTest rpgTest) {
	super(rpgTest);
    }

    @Override
    protected void onCreateStage(Stage stage, int width, int height) {
	prepareResources();
	addSoldier(100, 100, 350);
	setupShaders();
    }

    private void prepareResources() {
	lightingManager.setAmbientLight(new Color(0.05f, 0f, 0.5f, 0.15f));
	final Texture background = SharedAssetManager.getInstance().get(Assets.WALL, Texture.class);
	renderPipeline.add(PIPE_BACKGROUND, new RenderLayer() {

	    @Override
	    public void render(Batch batch, float delta) {
		batch.begin();
		batch.draw(background, 0f, 0f);
		batch.end();
	    }

	});
	Texture texture = SharedAssetManager.getInstance().get(Assets.SOLDIER);
	world.registerRenderer(SOLDIER, new SpriteRenderer(texture));
    }

    private void setupShaders() {
	RenderPipe worldPipe = renderPipeline.getPipe(PIPE_WORLD);
	Bloom bloom = new Bloom(Math.round(Gdx.graphics.getWidth() / 1.5f),
		Math.round(Gdx.graphics.getHeight() / 1.5f));
	bloom.setBlurAmount(15f);
	bloom.setBloomIntesity(1.1f);
	bloom.setBlurPasses(5);
	worldPipe.addEffects(bloom);
	Fxaa aliasing = new Fxaa(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
	worldPipe.addEffects(aliasing);
    }

    private void addSoldier(float x, float y, int size) {
	GameObject object = world.addObject();
	object.setPosition(x, y);
	object.setType(SOLDIER);
	object.setDimensions(size, size);
	world.applyBehavior(new PointLightBehavior(Color.valueOf("885522"), 700f, lightingManager), object);
    }
}