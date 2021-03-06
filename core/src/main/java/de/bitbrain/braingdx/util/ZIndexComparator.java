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

package de.bitbrain.braingdx.util;

import de.bitbrain.braingdx.world.GameObject;

import java.util.Comparator;

/**
 * Compares ZIndex of game objects
 *
 * @author Miguel Gonzalez Sanchez
 * @version 1.0.0
 * @since 1.0.0
 */
public class ZIndexComparator implements Comparator<GameObject> {

   @Override
   public int compare(GameObject o1, GameObject o2) {
      if (o1.getZIndex() > o2.getZIndex())
         return 1;
      else if (o1.getZIndex() < o2.getZIndex())
         return -1;
      else
         return 0;
   }
}
