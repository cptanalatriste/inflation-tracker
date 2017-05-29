package uk.ac.ucl.crest.inftracker.service;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.user.ApplicationUser;

import uk.ac.ucl.crest.inftracker.data.JiraIssueDataAccess;

public class InflationTrackerService {

	private static final Logger log = Logger.getLogger(InflationTrackerService.class);

	private JiraIssueDataAccess issueDataAccess;

	public InflationTrackerService(SearchService searchService, ChangeHistoryManager changeHistoryManager) {
		this.issueDataAccess = new JiraIssueDataAccess(searchService, changeHistoryManager);
	}

	public Double getReputationScore(ApplicationUser applicationUser, Issue currentIssue, String resolvedStatus,
			Double initialReputation, Double inflationPenalty) {

		// TODO (cgavidia): This is a sample value for testing
		Double reputationScore = null;

		List<Issue> changedPriorityIssues = issueDataAccess.getIssuesWithChangedPriorities(applicationUser,
				currentIssue, resolvedStatus);

		if (changedPriorityIssues != null) {
			// TODO(cgavidia): Logging to warn temporarily.
			log.warn("changedPriorityIssues.size(): " + changedPriorityIssues.size());

			int inflatedIssues = 0;
			for (Issue issue : changedPriorityIssues) {
				if (isInflated(issue, resolvedStatus)) {
					inflatedIssues += 1;
				}
			}

			// TODO(cgavidia): Logging to warn temporarly.
			log.warn("inflatedIssues: " + inflatedIssues);
			reputationScore = Math.max(0.0, initialReputation - inflatedIssues * inflationPenalty);
		}

		return reputationScore;
	}

	private boolean isInflated(Issue issue, String resolvedStatus) {
		// TODO(cgavidia): This logic should consider that the resolver is
		// different than the reporter.

		List<ChangeHistory> changeHistories = this.issueDataAccess.retrieveChangeHistory(issue);
		log.warn("changeHistories.size() : " + changeHistories.size());

		Collections.sort(changeHistories, new Comparator<ChangeHistory>() {

			@Override
			public int compare(ChangeHistory o1, ChangeHistory o2) {
				return o2.getTimePerformed().compareTo(o1.getTimePerformed());
			}
		});

		ApplicationUser resolver = this.issueDataAccess.getIssueResolver(changeHistories, resolvedStatus);
		String resolverPriority = this.issueDataAccess.getPriorityAssesmentByReporter(changeHistories, resolver);
		String originalPriority = this.issueDataAccess.getOriginalPriority(changeHistories);

		// TODO(cgavidia): Logging to warn temporarily.
		log.warn("originalPriority : " + originalPriority + " resolverPriority : " + resolverPriority);

		if (originalPriority != null && resolverPriority != null && !originalPriority.equals(resolverPriority)) {
			return true;
		}
		return false;
	}

}
