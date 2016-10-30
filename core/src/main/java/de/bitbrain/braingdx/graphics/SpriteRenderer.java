/* Copyright 2016 Miguel Gonzalez Sanchez
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package de.bitbrain.braingdx.graphics;

import com.badlogic.gdx.assets.AssetManager;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.Batch;
import com.badlogic.gdx.graphics.g2d.Sprite;

import de.bitbrain.braingdx.GameObject;
import de.bitbrain.braingdx.assets.SharedAssetManager;

/**
 * renderer implementation for sprites
 *
 * @since 1.0.0
 * @version 1.0.0
 * @author Miguel Gonzalez Sanchez
 */
public class SpriteRenderer implements GameObjectRenderManager.GameObjectRenderer {

    protected Sprite sprite;
    private final AssetManager assets = SharedAssetManager.getInstance();
    private String textureId;
    private Texture texture;

    public SpriteRenderer(String textureId) {
	this.textureId = textureId;
    }

    public SpriteRenderer(Texture texture) {
	this.texture = texture;
    }

    @Override
    public void init() {
	if (textureId != null) {
	    texture = assets.get(textureId, Texture.class);
	}
	sprite = new Sprite(texture);
	sprite.setFlip(false, true);
    }

    @Override
    public void render(GameObject object, Batch batch, float delta) {
	sprite.setPosition(object.getLeft() + object.getOffset().x, object.getTop() + object.getOffset().y);
	sprite.setSize(object.getWidth(), object.getHeight());
	sprite.setColor(object.getColor());
	sprite.setScale(object.getScale().x, object.getScale().y);
	sprite.draw(batch, 1f);
    }
}
