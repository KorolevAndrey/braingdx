/* Copyright 2017 Miguel Gonzalez Sanchez
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

package de.bitbrain.braingdx.tmx;

import com.badlogic.gdx.maps.tiled.TiledMap;
import de.bitbrain.braingdx.world.GameObject;

/**
 * Adapter implementation of {@link TiledMapListener}
 *
 * @since 1.0.0
 * @version 1.0.0
 * @author Miguel Gonzalez Sanchez
 */
public class TiledMapListenerAdapter implements TiledMapListener {

   @Override
   public void onLoadGameObject(GameObject object, TiledMapAPI api) {
   }

   @Override
   public void beforeLoad(TiledMap map) {
   }

   @Override
   public void afterLoad(TiledMap map, TiledMapAPI api) {
   }

   @Override
   public void beforeUnload(TiledMapAPI api) {
   }

   @Override
   public void afterUnload() {
   }

   @Override
   public void onEnterCell(int xIndex, int yIndex, GameObject object, TiledMapAPI api) {
   }

   @Override
   public void onLayerChange(int previousLayer, int newLayer, GameObject object, TiledMapAPI api) {
   }
}