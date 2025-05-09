package com.apcs.ljaag.nodes.controller;

import com.apcs.disunity.input.Inputs;
import com.apcs.disunity.math.Vector2;
import com.apcs.disunity.nodes.controller.Controller;
import com.apcs.disunity.signals.Signals;

/** 
 * A controller that is controlled by player inputs
 * 
 * @author Qinzhao Li
 */
public class PlayerController extends Controller {

    public static final int SPEED = 10;

    /* ================ [ NODE ] ================ */

    /**
     * Updates the node and triggers all necessary actions
     * 
     * @param delta The time since the last update
     */
    @Override
    public void update(double delta) {
        // Trigger walking
        int axis = (Inputs.getAction("left") ? -1 : 0) + (Inputs.getAction("right") ? 1 : 0);
        Signals.trigger(Signals.getSignal(getId(), "move"), Vector2.of(axis * SPEED, 0));
    }
    
}
