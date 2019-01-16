/*
 * Copyright (C) The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.androidtown.sleepdrive_cognition.googleapi;

import android.util.Log;

/**
 * Graphics class for rendering Googly Eyes on a graphic overlay given the current eye positions.
 */
class GooglyEyesGraphic  {

    // Keep independent physics state for each eye.
    private EyePhysics mLeftPhysics = new EyePhysics();
    private EyePhysics mRightPhysics = new EyePhysics();

    private volatile boolean mLeftOpen;
    private volatile boolean mRightOpen;



    //==============================================================================================
    // Methods
    //==============================================================================================

    /**
     * Updates the eye positions and state from the detection of the most recent frame.  Invalidates
     * the relevant portions of the overlay to trigger a redraw.
     */
    void updateEyes(boolean leftOpen,boolean rightOpen) {
        mLeftOpen = leftOpen;
        mRightOpen = rightOpen;
    }

    /**
     * Draws the current eye state to the supplied canvas.  This will draw the eyes at the last
     * reported position from the tracker, and the iris positions according to the physics
     * simulations for each iris given motion and other forces.
     */

    public void draw() {
        // Use the inter-eye distance to set the size of the eyes
        // Advance the current left iris position, and draw left eye.
        drawEye(mLeftOpen);
         //Advance the current right iris position, and draw right eye.
        drawEye(mRightOpen);
    }

    /**
     * Draws the eye, either closed or open with the iris in the current position.
     */
    private void drawEye(  boolean isOpen) {
        if (isOpen) {
            Log.d("D1","ON");
        } else { // 눈감을때
            //canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeLidPaint);
            Log.d("D1","OFF");
           // canvas.drawLine(start, y, end, y, mEyeOutlinePaint);
        }
         //canvas.drawCircle(eyePosition.x, eyePosition.y, eyeRadius, mEyeOutlinePaint);
    }
}
