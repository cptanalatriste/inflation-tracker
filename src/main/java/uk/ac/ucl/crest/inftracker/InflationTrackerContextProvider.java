package uk.ac.ucl.crest.inftracker;

import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;

import uk.ac.ucl.crest.inftracker.service.InflationTrackerService;

public class InflationTrackerContextProvider extends AbstractJiraContextProvider {

	// TODO(cgavidia): We should be able to tune this parameter.
	private static final Double INFLATION_PENALTY = 0.1;
	private static final Double INITIAL_REPUTATION = 1.0;
	private static final Double WARNING_THRESHOLD = 0.5;
	private static final Double OPTIMAL_THRESHOLD = 0.7;

	// TODO(cgavidia): We can try later with multiple Resolved status
	private static final String RESOLVED_STATUS = "Done";

	private InflationTrackerService inflationTrackerService;

	public InflationTrackerContextProvider() {
		super();
		this.inflationTrackerService = new InflationTrackerService(ComponentAccessor.getComponent(SearchService.class),
				ComponentAccessor.getChangeHistoryManager());

	}

	@Override
	public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
		Map<String, Object> contextMap = new HashMap<String, Object>();

		Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
		contextMap.put("reputationScore", getReputationScore(applicationUser, currentIssue));
		contextMap.put("warningThreshold", WARNING_THRESHOLD);
		contextMap.put("optimalThreshold", OPTIMAL_THRESHOLD);

		return contextMap;
	}

	private Double getReputationScore(ApplicationUser applicationUser, Issue currentIssue) {
		return this.inflationTrackerService.getReputationScore(applicationUser, currentIssue, RESOLVED_STATUS,
				INITIAL_REPUTATION, INFLATION_PENALTY);
	}

}
