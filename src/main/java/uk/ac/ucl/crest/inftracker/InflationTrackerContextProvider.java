package uk.ac.ucl.crest.inftracker;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

public class InflationTrackerContextProvider extends AbstractJiraContextProvider {

	@Override
	public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
		Map<String, Object> contextMap = new HashMap<String, Object>();
		contextMap.put("reputationScore", getReputationScore());
		return contextMap;
	}

	private Double getReputationScore() {
		// TODO (cgavidia): This is a sample value for testing
		return 0.8;
	}

}
