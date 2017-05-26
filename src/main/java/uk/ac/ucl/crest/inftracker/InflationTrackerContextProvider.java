package uk.ac.ucl.crest.inftracker;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.search.SearchService.ParseResult;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.plugin.webfragment.contextproviders.AbstractJiraContextProvider;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;

public class InflationTrackerContextProvider extends AbstractJiraContextProvider {

	private static final Logger log = Logger.getLogger(InflationTrackerContextProvider.class);

	// TODO(cgavidia): We should be able to tune this parameter.
	private static final Double INFLATION_PENALTY = 0.1;
	private static final Double INITIAL_REPUTATION = 1.0;
	private static final String INFLATION_JQL = "reporter=\"%s\" AND priority CHANGED FROM \"Medium\" TO \"Lowest\" ";

	@Override
	public Map<String, Object> getContextMap(ApplicationUser applicationUser, JiraHelper jiraHelper) {
		Map<String, Object> contextMap = new HashMap<String, Object>();

		Issue currentIssue = (Issue) jiraHelper.getContextParams().get("issue");
		contextMap.put("reputationScore", getReputationScore(applicationUser, currentIssue));
		return contextMap;
	}

	private Double getReputationScore(ApplicationUser applicationUser, Issue currentIssue) {
		JqlQueryBuilder builder = JqlQueryBuilder.newBuilder();

		// TODO (cgavidia): This is a sample value for testing
		Double reputationScore = 0.8;
		builder.where().reporter().eq(currentIssue.getReporter().getDisplayName());

		SearchService searchService = ComponentAccessor.getComponent(SearchService.class);

		String jqlQuery = String.format(INFLATION_JQL, currentIssue.getReporter().getDisplayName());
		ParseResult parseResult = searchService.parseQuery(applicationUser, jqlQuery);

		if (parseResult.isValid()) {
			try {
				SearchResults results = searchService.search(applicationUser, parseResult.getQuery(),
						PagerFilter.getUnlimitedFilter());
				List<Issue> inflatedIssues = results.getIssues();

				// TODO(cgavidia): Logging to warn temporarly.
				log.warn("inflatedIssues: " + inflatedIssues.size());
				reputationScore = Math.max(0.0, INITIAL_REPUTATION - inflatedIssues.size() * INFLATION_PENALTY);

			} catch (SearchException e) {
				log.warn("Unable to perform JQL search", e);
			}
		} else {
			// TODO(cgavidia): Logging to warn temporarly.
			log.warn("The JQL query built is not valid: " + jqlQuery);
		}

		return reputationScore;
	}

}
