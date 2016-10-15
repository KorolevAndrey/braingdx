package de.bitbrain.braingdx.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.scenes.scene2d.InputEvent;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.utils.ClickListener;
import com.bitfire.postprocessing.effects.Bloom;

import de.bitbrain.braingdx.AbstractScreen;
import de.bitbrain.braingdx.GameObject;
import de.bitbrain.braingdx.apps.LightingManagerTest;
import de.bitbrain.braingdx.assets.Assets;
import de.bitbrain.braingdx.assets.SharedAssetManager;
import de.bitbrain.braingdx.behavior.RandomMovementBehavior;
import de.bitbrain.braingdx.graphics.SpriteRenderer;
import de.bitbrain.braingdx.graphics.lighting.PointLightBehavior;
import de.bitbrain.braingdx.graphics.pipeline.RenderPipe;
import de.bitbrain.braingdx.ui.Styles;

public class LightingManagerScreen extends AbstractScreen<LightingManagerTest> {

    private static final int OBJECTS = 8;

    private static final int TYPE = 1;

    public LightingManagerScreen(LightingManagerTest game) {
	super(game);
    }

    @Override
    protected void onCreateStage(Stage stage, int width, int height) {
	prepareResources();
	setupShaders();
	addRandomObjects();
	createButtonUI(stage);
    }

    private void prepareResources() {
	Styles.init();
	lightingManager.setAmbientLight(new Color(0.1f, 0f, 0.2f, 0.25f));
	Texture texture = SharedAssetManager.getInstance().get(Assets.TEX_BALL);
	world.registerRenderer(TYPE, new SpriteRenderer(texture));
    }

    private void setupShaders() {
	RenderPipe worldPipe = renderPipeline.getPipe(PIPE_WORLD);
	Bloom bloom = new Bloom(Math.round(Gdx.graphics.getWidth() / 1.5f),
		Math.round(Gdx.graphics.getHeight() / 1.5f));
	bloom.setBlurAmount(15f);
	bloom.setBloomIntesity(2.1f);
	bloom.setBlurPasses(4);
	worldPipe.addEffects(bloom);
	RenderPipe uiPipe = renderPipeline.getPipe(PIPE_UI);
	Bloom uiBloom = new Bloom(Math.round(Gdx.graphics.getWidth() / 1f), Math.round(Gdx.graphics.getHeight() / 1f));
	uiBloom.setBlurAmount(20f);
	uiBloom.setBloomIntesity(3.1f);
	uiBloom.setBlurPasses(8);
	uiPipe.addEffects(uiBloom);
    }

    private void addRandomObjects() {
	for (int i = 0; i < OBJECTS; ++i) {
	    GameObject object = world.addObject();
	    object.setDimensions(30, 30);
	    object.setPosition((int) (Gdx.graphics.getWidth() * Math.random()),
		    (int) (Gdx.graphics.getHeight() * Math.random()));
	    object.setType(TYPE);
	    Color randomColor = new Color((float) Math.random(), (float) Math.random(), (float) Math.random(), 0.85f);
	    world.applyBehavior(new PointLightBehavior(randomColor, 300f, lightingManager), object);
	    world.applyBehavior(new RandomMovementBehavior(), object);
	    Color objectColor = randomColor.cpy();
	    objectColor.a = 1f;
	    object.setColor(objectColor);
	}
    }

    private void createButtonUI(Stage stage) {
	Table group = new Table();
	group.setFillParent(true);
	for (String pipeId : renderPipeline.getPipeIds()) {
	    final RenderPipe renderPipe = renderPipeline.getPipe(pipeId);
	    final TextButton textButton = new TextButton("Disable " + pipeId, Styles.BUTTON_DEFAULT_ACTIVE);
	    textButton.getColor().a = 0.7f;
	    textButton.addListener(new ClickListener() {
		@Override
		public void clicked(InputEvent event, float x, float y) {
		    if (renderPipe.isEnabled()) {
			textButton.setStyle(Styles.BUTTON_DEFAULT_INACTIVE);
			renderPipe.setEnabled(false);
		    } else {
			textButton.setStyle(Styles.BUTTON_DEFAULT_ACTIVE);
			renderPipe.setEnabled(true);
		    }
		}
	    });
	    group.left().top().add(textButton).width(400f).padBottom(10f).row();
	}
	stage.addActor(group);
    }
}