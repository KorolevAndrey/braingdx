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

package de.bitbrain.braingdx.graphics;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import aurelienribon.tweenengine.TweenEquations;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Camera;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import de.bitbrain.braingdx.tweens.SharedTweenManager;
import de.bitbrain.braingdx.tweens.VectorTween;
import de.bitbrain.braingdx.util.math.BigDecimalVector2;
import de.bitbrain.braingdx.world.GameObject;
import de.bitbrain.braingdx.world.GameWorld;
import de.bitbrain.braingdx.world.GameWorld.WorldBounds;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Random;

import static de.bitbrain.braingdx.util.BitUtils.haveSameSign;
import static java.lang.Double.isInfinite;
import static java.lang.Double.isNaN;
import static java.math.BigDecimal.ZERO;

/**
 * Using underlying vectors to calculate the camera tracking.
 *
 * @author Miguel Gonzalez Sanchez
 * @version 1.0.0
 * @since 1.0.0
 */
public class VectorGameCamera implements GameCamera {

   private static final MathContext PRECISION = MathContext.DECIMAL32;
   private static final BigDecimal TWO = bigDecimalFromDouble(2);
   // Interval in miliseconds between each movement
   public static final float STEP_INTERVAL = 0.05f;

   private final OrthographicCamera camera;
   private final GameWorld world;
   private final BigDecimalVector2 tmp = new BigDecimalVector2();
   private BigDecimal velocityX, velocityY;
   private GameObject target;
   private BigDecimal speedX = new BigDecimal(0.001f, PRECISION);
   private BigDecimal speedY = new BigDecimal(0.001f, PRECISION);
   private BigDecimal zoomScale = new BigDecimal(0.0025f, PRECISION);
   private boolean focusRequested = false;
   private BigDecimal defaultZoom = new BigDecimal(1, PRECISION);
   private boolean worldBoundsStickiness = true;

   private final Rectangle previousWorldBounds = new Rectangle();
   private final Rectangle currentWorldBounds = new Rectangle();

   private final Vector2 shake = new Vector2(), lastShake = new Vector2();

   private int correctionX = 0;
   private int correctionY = 0;

   private final Random random = new Random();

   static {
      // it is important to tell Universal Tween Engine how
      // to translate the camera movement
      Tween.registerAccessor(Vector2.class, new VectorTween());
   }

   private float distanceThreshold = 0;

   public VectorGameCamera(OrthographicCamera camera, GameWorld world) {
      this.camera = camera;
      velocityX = new BigDecimal(0f);
      velocityY = new BigDecimal(0f);

      this.world = world;
   }

   @Override
   public void setTrackingTarget(GameObject target) {
      setTrackingTarget(target, true);
   }

   @Override
   public void setTrackingTarget(GameObject target, boolean focus) {
      this.target = target;
      if (focus) {
         focusCentered();
      }
   }

   @Override
   public void update(float delta) {
      camera.position.x -= lastShake.x;
      camera.position.y -= lastShake.y;
      if (target != null) {
         currentWorldBounds.set(
               world.getBounds().getWorldOffsetX(),
               world.getBounds().getWorldOffsetY(),
               world.getBounds().getWorldWidth(),
               world.getBounds().getWorldHeight()
         );
         if (!currentWorldBounds.equals(previousWorldBounds)) {
            correctionX = 0;
            correctionY = 0;
         }
         if (focusRequested) {
            focusCentered(target);
            focusRequested = false;
         } else {
            applyTrackingVelocityAndZoom(delta);
         }
         if (worldBoundsStickiness) {
            applyWorldBounds();
         }
         previousWorldBounds.set(
               world.getBounds().getWorldOffsetX(),
               world.getBounds().getWorldOffsetY(),
               world.getBounds().getWorldWidth(),
               world.getBounds().getWorldHeight()
         );
      }
      camera.position.x += shake.x;
      camera.position.y += shake.y;
      camera.update();
      lastShake.set(shake);
   }

   @Override
   public void setDistanceStoppingThreshold(float distanceThreshold) {
      this.distanceThreshold = distanceThreshold;
   }

   @Override
   public void resize(int width, int height) {
      focusRequested = true;
      camera.setToOrtho(false, width, height);
   }

   @Override
   public float getDefaultZoomFactor() {
      return defaultZoom.floatValue();
   }

   @Override
   public void setDefaultZoomFactor(float zoom) {
      this.defaultZoom = new BigDecimal(String.valueOf(zoom), PRECISION);
      if (target == null) {
         camera.zoom = zoom;
      }
   }

   @Override
   public void zoom(float amount) {
      defaultZoom = defaultZoom.multiply(new BigDecimal(String.valueOf(amount), PRECISION));
      if (target == null) {
         camera.zoom = defaultZoom.floatValue();
      }
   }

   @Override
   public void setTargetTrackingSpeed(float speed) {
      setTargetTrackingSpeed(speed, speed);
   }

   @Override
   public void setTargetTrackingSpeed(float speedX, float speedY) {
      this.speedX = new BigDecimal(Math.abs(speedX), PRECISION);
      this.speedY = new BigDecimal(Math.abs(speedY), PRECISION);
   }

   @Override
   public void setZoomScalingFactor(float zoomScale) {
      this.zoomScale = new BigDecimal(zoomScale, PRECISION);
   }

   @Override
   public void focusCentered(GameObject object) {
      camera.position.x = object.getLeft() + object.getOffsetX() + object.getWidth() / 2f;
      camera.position.y = object.getTop() + object.getOffsetY() + object.getHeight() / 2f;
      if (worldBoundsStickiness) {
         correctionX = 0;
         correctionY = 0;
         applyWorldBounds();
      }
   }

   @Override
   public void focusCentered() {
      focusRequested = true;
      if (target == null) {
         WorldBounds bounds = world.getBounds();
         camera.position.x = bounds.getWorldOffsetX() + bounds.getWorldWidth() / 2f;
         camera.position.y = bounds.getWorldOffsetY() + bounds.getWorldHeight() / 2f;
      }
      if (worldBoundsStickiness) {
         correctionX = 0;
         correctionY = 0;
         applyWorldBounds();
      }
   }

   @Override
   public Camera getInternalCamera() {
      return camera;
   }

   @Override
   public void setPosition(float x, float y) {
      this.camera.position.x = x;
      this.camera.position.y = y;
      if (worldBoundsStickiness) {
         applyWorldBounds();
      }
   }

   @Override
   public Vector3 getPosition() {
      return this.camera.position.cpy();
   }

   @Override
   public float getDistanceTo(float targetX, float targetY) {
      BigDecimalVector2 thisPosition = new BigDecimalVector2(this.camera.position.x, this.camera.position.y);
      BigDecimalVector2 targetPosition = new BigDecimalVector2(targetX, targetY);
      return targetPosition.sub(thisPosition).len().floatValue();
   }

   @Override
   public float getDistanceTo(GameObject gameObject) {
      return getDistanceTo(
            gameObject.getLeft() + gameObject.getWidth() / 2f,
            gameObject.getTop() + gameObject.getHeight() / 2f
      );
   }

   @Override
   public float getScaledCameraWidth() {
      BigDecimal cameraZoom = new BigDecimal(camera.zoom, PRECISION);
      return new BigDecimal(camera.viewportWidth, PRECISION).multiply(cameraZoom).floatValue();
   }

   @Override
   public float getScaledCameraHeight() {
      BigDecimal cameraZoom = new BigDecimal(camera.zoom, PRECISION);
      return new BigDecimal(camera.viewportHeight, PRECISION).multiply(cameraZoom).floatValue();
   }

   @Override
   public float getTargetTrackingSpeed() {
      return getTargetTrackingSpeedX();
   }

   @Override
   public float getTargetTrackingSpeedX() {
      return speedX.floatValue();
   }

   @Override
   public float getTargetTrackingSpeedY() {
      return speedY.floatValue();
   }

   @Override
   public void shake(float strength, float duration) {
// Calculate the number of steps to take until radius is 0
      final int STEPS = Math.round(duration / STEP_INTERVAL);
      // Radius reduction on each iteration
      final float STRENGTH_STEP = strength / STEPS;
      // Do not forget to kill previous animations!
      SharedTweenManager.getInstance().killTarget(shake);
      for (int step = 0; step < STEPS; ++step) {
         // Step 1: Let's find a random angle
         double angle = Math.toRadians(random.nextFloat() * 360f);
         float x = (float) Math.floor(strength * Math.cos(angle));
         float y = (float) Math.floor(strength * Math.sin(angle));

         final int finalStep = step;

         // Step 2: ease to the calculated point. Do not forget to set
         // delay!
         Tween.to(shake, VectorTween.POS_X, STEP_INTERVAL).delay(step * STEP_INTERVAL).target(x)
               .setCallback(new TweenCallback() {
                  @Override
                  public void onEvent(int i, BaseTween<?> baseTween) {
                     if (finalStep == STEPS - 1) {
                        shake.set(0f, 0f);
                     }
                  }
               })
               .setCallbackTriggers(TweenCallback.COMPLETE)
               .ease(TweenEquations.easeInOutCubic).start(SharedTweenManager.getInstance());
         Tween.to(shake, VectorTween.POS_Y, STEP_INTERVAL).delay(step * STEP_INTERVAL).target(y)
               .ease(TweenEquations.easeInOutCubic).start(SharedTweenManager.getInstance());

         // Step 3: reduce the radius of the screen shake circle
         strength -= STRENGTH_STEP;
      }
   }

   @Override
   public void setStickToWorldBounds(boolean enabled) {
      this.worldBoundsStickiness = enabled;
   }

   private void applyTrackingVelocityAndZoom(float delta) {
      BigDecimal preciseDelta = bigDecimalFromDouble(delta);

      BigDecimal targetLeft = bigDecimalFromDouble(target.getLeft() + target.getOffsetX());
      BigDecimal targetWidth = bigDecimalFromDouble(target.getWidth());
      BigDecimal targetTop = bigDecimalFromDouble(target.getTop() + target.getOffsetY());
      BigDecimal targetHeight =bigDecimalFromDouble(target.getHeight());

      BigDecimal cameraLeft = bigDecimalFromDouble(camera.position.x);
      BigDecimal cameraTop = bigDecimalFromDouble(camera.position.y);

      velocityX = targetLeft.add(targetWidth.divide(TWO, PRECISION)).subtract(cameraLeft);
      velocityY = targetTop.add(targetHeight.divide(TWO, PRECISION)).subtract(cameraTop);

      tmp.set(velocityX, velocityY);

      BigDecimal distance = tmp.len();

      if (distance.floatValue() < (distanceThreshold * camera.zoom)) {
         distance = ZERO;
      }

      BigDecimal overAllSpeedX = distance.multiply(speedX);
      BigDecimal overAllSpeedY = distance.multiply(speedY);
      BigDecimal deltaX = velocityX.multiply(overAllSpeedX).multiply(preciseDelta);
      BigDecimal deltaY = velocityY.multiply(overAllSpeedY).multiply(preciseDelta);

      camera.zoom = defaultZoom.add(zoomScale.multiply(distance)).floatValue();

      if (correctionX == 0 || !haveSameSign(deltaX.floatValue(), correctionX)) {
         camera.position.x = cameraLeft.add(deltaX).floatValue();
         correctionX = 0;
      }
      if (correctionY == 0 || !haveSameSign(deltaY.floatValue(), correctionY)) {
         camera.position.y = cameraTop.add(deltaY).floatValue();
         correctionY = 0;
      }
   }

   private void applyWorldBounds() {
      BigDecimal worldLeft = bigDecimalFromDouble(world.getBounds().getWorldOffsetX());
      BigDecimal worldTop = bigDecimalFromDouble(world.getBounds().getWorldOffsetY());
      BigDecimal worldWidth = bigDecimalFromDouble(world.getBounds().getWorldWidth());
      BigDecimal worldHeight = bigDecimalFromDouble(world.getBounds().getWorldHeight());

      BigDecimal cameraZoom = bigDecimalFromDouble(camera.zoom);

      BigDecimal cameraCenterX = bigDecimalFromDouble(camera.position.x);
      BigDecimal cameraCenterY = bigDecimalFromDouble(camera.position.y);
      BigDecimal cameraWidth = bigDecimalFromDouble(camera.viewportWidth);
      BigDecimal cameraHeight = bigDecimalFromDouble(camera.viewportHeight);
      BigDecimal cameraWidthScaled = bigDecimalFromDouble(camera.viewportWidth).multiply(cameraZoom);
      BigDecimal cameraHeightScaled = bigDecimalFromDouble(camera.viewportHeight).multiply(cameraZoom);

      BigDecimal cameraLeft = cameraCenterX.subtract(cameraWidthScaled.divide(TWO, PRECISION));
      BigDecimal cameraTop = cameraCenterY.subtract(cameraHeightScaled.divide(TWO, PRECISION));
      BigDecimal cameraRight = cameraCenterX.add(cameraWidthScaled.divide(TWO, PRECISION));
      BigDecimal cameraBottom = cameraCenterY.add(cameraHeightScaled.divide(TWO, PRECISION));

      if (cameraWidthScaled.floatValue() > worldWidth.floatValue()) {
         camera.zoom = worldWidth.divide(cameraWidth, PRECISION).floatValue();
         cameraZoom = bigDecimalFromDouble(camera.zoom);
         cameraWidthScaled = bigDecimalFromDouble(camera.viewportWidth).multiply(cameraZoom);
         cameraHeightScaled = bigDecimalFromDouble(camera.viewportHeight).multiply(cameraZoom);
         cameraRight = cameraCenterX.add(cameraWidthScaled.divide(TWO, PRECISION));
         cameraTop = cameraCenterY.subtract(cameraHeightScaled.divide(TWO, PRECISION));
         cameraLeft = cameraCenterX.subtract(cameraWidthScaled.divide(TWO, PRECISION));
         cameraBottom = cameraCenterY.add(cameraHeightScaled.divide(TWO, PRECISION));
         setDefaultZoomFactor(camera.zoom);
      }
      if (cameraHeightScaled.floatValue() > worldHeight.floatValue()) {
         float newZoom = worldHeight.divide(cameraHeight, PRECISION).floatValue();
         if (newZoom < camera.zoom) {
            camera.zoom = newZoom;
            cameraZoom = bigDecimalFromDouble(camera.zoom);
            cameraWidthScaled = bigDecimalFromDouble(camera.viewportWidth).multiply(cameraZoom);
            cameraHeightScaled = bigDecimalFromDouble(camera.viewportHeight).multiply(cameraZoom);
            cameraRight = cameraCenterX.add(cameraWidthScaled.divide(TWO, PRECISION));
            cameraTop = cameraCenterY.subtract(cameraHeightScaled.divide(TWO, PRECISION));
            cameraLeft = cameraCenterX.subtract(cameraWidthScaled.divide(TWO, PRECISION));
            cameraBottom = cameraCenterY.add(cameraHeightScaled.divide(TWO, PRECISION));
            setDefaultZoomFactor(camera.zoom);
         }
      }

      // 2. adjust camera position
      if (cameraLeft.floatValue() < worldLeft.floatValue()) {
         camera.position.x = worldLeft.add(cameraWidthScaled.divide(TWO, PRECISION)).floatValue();
         correctionX = -1;
      } else if (cameraRight.floatValue() > worldLeft.add(worldWidth).floatValue()) {
         camera.position.x = worldLeft.add(worldWidth).subtract(cameraWidthScaled.divide(TWO, PRECISION)).floatValue();
         correctionX = 1;
      }
      if (cameraTop.floatValue() < worldTop.floatValue()) {
         camera.position.y = worldTop.add(cameraHeightScaled.divide(TWO, PRECISION)).floatValue();
         correctionY = -1;
      } else if (cameraBottom.floatValue() > worldTop.add(worldHeight).floatValue()) {
         camera.position.y = worldTop.add(worldHeight).subtract(cameraHeightScaled.divide(TWO, PRECISION)).floatValue();
         correctionY = 1;
      }
   }

   private static BigDecimal bigDecimalFromDouble(double value) {
      if (isInfinite(value) || isNaN(value)) {
         Gdx.app.log("WARN", "target tracking speed of GameCamera is too high! This may cause rendering issues.");
         return ZERO;
      }
      return new BigDecimal(Double.toString(value), PRECISION);
   }
}
