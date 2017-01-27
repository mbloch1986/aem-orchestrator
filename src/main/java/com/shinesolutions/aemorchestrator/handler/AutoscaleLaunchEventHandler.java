package com.shinesolutions.aemorchestrator.handler;

import java.util.Map;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.shinesolutions.aemorchestrator.actions.ScaleAction;
import com.shinesolutions.aemorchestrator.model.EventMessage;

@Component
public class AutoscaleLaunchEventHandler implements EventHandler {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource
    private Map<String, ScaleAction> scaleUpAutoScaleGroupMappings;

    public boolean handleEvent(EventMessage message) {
        boolean success = false;
        try {
            ScaleAction action = scaleUpAutoScaleGroupMappings.get(message.getAutoScalingGroupName());
            if (action == null) {
                logger.warn(
                    "Unable to find 'scale up' action for auto scaling group name: " + message.getAutoScalingGroupName()
                        + ". Please check the correct auto scaling group names were added to the application config");
            } else {
                success = action.execute(message.getEC2InstanceId());
            }
        } catch (Exception e) {
            logger.error("Failed to execute 'scale up' action for auto scaling group name: " + 
                message.getAutoScalingGroupName(), e);
        }

        return success;
    }

}
