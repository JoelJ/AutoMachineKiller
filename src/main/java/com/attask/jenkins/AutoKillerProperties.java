package com.attask.jenkins;

import hudson.Extension;
import hudson.model.Result;
import hudson.util.ListBoxModel;
import org.kohsuke.stapler.DataBoundConstructor;
import jenkins.model.GlobalConfiguration;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

import java.util.regex.Pattern;

/**
 * User: Joel Johnson
 * Date: 2/22/13
 * Time: 1:18 PM
 */
@Extension
public class AutoKillerProperties extends GlobalConfiguration {
	private boolean enabled;
	private String pattern;
	private String worseThan;

	private transient Pattern compiledPattern;

	@SuppressWarnings("UnusedDeclaration")
	public AutoKillerProperties() {
		load();
	}

	@Override
	public boolean configure(StaplerRequest req, JSONObject json) throws FormException {
		this.enabled = json.getBoolean("enabled");
		this.pattern = json.getString("pattern");
		this.compiledPattern = Pattern.compile(pattern);
		this.worseThan = json.getString("worseThan");
		save();
		return true;
	}

	/**
	 * @return Whether or not the plugin should be used.
	 */
	public boolean getEnabled() {
		return enabled;
	}

	/**
	 * @return The raw regular expression pattern as specified by the user that
	 * 			is used to determine if a machine should be torn down. If the
	 * 			machine's name matches this pattern, that machine will be torn
	 * 			down if the result of the build it ran is worse than or equal
	 * 			to {@link #getWorseThan}
	 */
	public String getPattern() {
		return pattern;
	}

	/**
	 * @return The raw value of the status that a finished build needs to have
	 * 			to determine if the running machine needs to be torn down.
	 */
	public String getWorseThan() {
		return worseThan;
	}

	/**
	 * @return Returns the {@link Result} object represented by {@link #getWorseThan()}.
	 * 			Defaults to FAILURE.
	 */
	public Result findWorseThan() {
		return Result.fromString(getWorseThan());
	}

	/**
	 * @return Returns a compiled pattern of {@link #getPattern()}.
	 * 			The result is stored in a transient field so the string only needs to
	 * 			be compiled once.
	 */
	public Pattern compilePattern() {
		if (compiledPattern == null) {
			if (pattern == null) {
				return null;
			}
			//Don't need to lock here. Not a big deal if it gets recompiled.
			compiledPattern = Pattern.compile(getPattern());
		}
		return compiledPattern;
	}

	public static AutoKillerProperties get() {
		return GlobalConfiguration.all().get(AutoKillerProperties.class);
	}

	@SuppressWarnings("UnusedDeclaration")
	public ListBoxModel doFillWorseThanItems() {
		ListBoxModel model = new ListBoxModel();
		model.add(Result.ABORTED.toString());
		model.add(Result.NOT_BUILT.toString());
		model.add(Result.FAILURE.toString());
		model.add(Result.UNSTABLE.toString());
		model.add(Result.SUCCESS.toString());
		return model;
	}

	@Override
	public String getDisplayName() {
		return Messages.AutoKillerDisplayName();
	}
}
