package com.attask.jenkins;

import hudson.Extension;
import hudson.model.*;
import hudson.model.listeners.RunListener;
import hudson.slaves.OfflineCause;

import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Automatically takes a machine offline if it fails a build.
 * User: Joel Johnson
 * Date: 2/22/13
 * Time: 1:03 PM
 */
@Extension
public class AutoKiller extends RunListener<AbstractBuild> {
	private static final Logger LOGGER = Logger.getLogger(AutoKiller.class.getName());

	@Override
	public void onCompleted(AbstractBuild abstractBuild, TaskListener listener) {
		AutoKillerProperties properties = AutoKillerProperties.get();
		if(!properties.getEnabled()) {
			return;
		}

		if(abstractBuild.getResult().isBetterThan(properties.findWorseThan())) {
			return;
		}

		Executor executor = abstractBuild.getExecutor();
		if(executor != null) {
			Computer owner = executor.getOwner();
			if(owner != null) {
				String name = owner.getName();
				if(name != null) {
					Pattern pattern = properties.compilePattern();
					if(pattern != null) {
						Matcher matcher = pattern.matcher(name);
						if(matcher.find()) {
							String errorMessage = "Machine failed a run a matched " + pattern + ". " + name + " getting torn down.";
							LOGGER.warning(errorMessage);
							listener.error(errorMessage);
							owner.setTemporarilyOffline(true, OfflineCause.create(Messages._AutoKillOfflineCause(abstractBuild.getFullDisplayName())));
						}
					}
				}
			}
		}
	}
}
