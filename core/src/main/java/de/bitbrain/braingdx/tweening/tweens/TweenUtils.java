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

package de.bitbrain.braingdx.tweening.tweens;

import com.badlogic.gdx.graphics.Color;

import de.bitbrain.braingdx.tweening.Tween;
import de.bitbrain.braingdx.tweening.TweenEquation;
import de.bitbrain.braingdx.tweening.TweenEquations;
import de.bitbrain.braingdx.tweening.TweenManager;

/**
 * Provides animation utilities
 *
 * @since 1.0.0
 * @version 1.0.0
 * @author Miguel Gonzalez <miguel-gonzalez@gmx.de>
 */
public final class TweenUtils {

    private static TweenManager tweenManager = SharedTweenManager.getInstance();

    /**
     * Fades the source color object into the target color in the given time
     *
     * @param sourceColor
     *            the source color
     * @param targetColor
     *            the target color
     * @param time
     *            given decimal time (in seconds)
     * @param equation
     *            tween equation
     */
    public static void toColor(Color sourceColor, Color targetColor, float time, TweenEquation equation) {
	tweenManager.killTarget(sourceColor, ColorTween.R);
	tweenManager.killTarget(sourceColor, ColorTween.G);
	tweenManager.killTarget(sourceColor, ColorTween.B);
	Tween.to(sourceColor, ColorTween.R, time).ease(equation).target(targetColor.r).start(tweenManager);
	Tween.to(sourceColor, ColorTween.G, time).ease(equation).target(targetColor.g).start(tweenManager);
	Tween.to(sourceColor, ColorTween.B, time).ease(equation).target(targetColor.b).start(tweenManager);
    }

    /**
     * Fades the source color object into the target color in the given time
     *
     * @param sourceColor
     *            the source color
     * @param targetColor
     *            the target color
     * @param time
     *            given decimal time (in seconds)
     */
    public static void toColor(Color sourceColor, Color targetColor, float time) {
	toColor(sourceColor, targetColor, time, TweenEquations.easeNone);
    }

    /**
     * Fades the source color object into the target color in 1 second
     *
     * @param sourceColor
     *            the source color
     * @param targetColor
     *            the target color
     */
    public static void toColor(Color sourceColor, Color targetColor) {
	toColor(sourceColor, targetColor, 1f, TweenEquations.easeNone);
    }
}