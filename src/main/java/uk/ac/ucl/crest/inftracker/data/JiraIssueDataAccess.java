package uk.ac.ucl.crest.inftracker.data;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.apache.log4j.Logger;

import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.bc.issue.search.SearchService.ParseResult;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.web.bean.PagerFilter;

public class JiraIssueDataAccess {

	private static final Logger log = Logger.getLogger(JiraIssueDataAccess.class);

	private static final String CHANGED_PRIORITY_JQL = "reporter=\"%s\" AND priority CHANGED AND (status WAS \"%s\")";
	private static final String STATUS_FIELD = "status";
	private static final String PRIORITY_FIELD = "priority";

	private SearchService searchService;
	private ChangeHistoryManager changeHistoryManager;

	public JiraIssueDataAccess(SearchService searchService, ChangeHistoryManager changeHistoryManager) {
		this.searchService = searchService;
		this.changeHistoryManager = changeHistoryManager;
	}

	public List<Issue> getResolvedIssuesWithChangedPriorities(ApplicationUser applicationUser, Issue currentIssue,
			String resolvedStatus) {

		String jqlQuery = String.format(CHANGED_PRIORITY_JQL, currentIssue.getReporter().getDisplayName(),
				resolvedStatus);
		ParseResult parseResult = searchService.parseQuery(applicationUser, jqlQuery);

		if (parseResult.isValid()) {
			try {

				SearchResults results = searchService.search(applicationUser, parseResult.getQuery(),
						PagerFilter.getUnlimitedFilter());
				List<Issue> changedPriorityIssues = results.getIssues();

				return changedPriorityIssues;
			} catch (SearchException e) {
				log.warn("Unable to perform JQL search", e);
			}

		} else {
			log.warn("The JQL query built is not valid: " + jqlQuery);
		}

		return null;
	}

	public List<ChangeHistory> retrieveChangeHistory(Issue issue) {
		List<ChangeHistory> changeHistories = changeHistoryManager.getChangeHistories(issue);
		return changeHistories;
	}

	public ApplicationUser getIssueResolver(List<ChangeHistory> changeHistories, String resolvedStatus) {
		for (ChangeHistory changeHistory : changeHistories) {
			// TODO(cgavidia): Logging to warn temporarily.
			log.warn("changeHistory.toString(): " + changeHistory.toString());

			for (ChangeItemBean changeItemBean : changeHistory.getChangeItemBeans()) {
				// TODO(cgavidia): Logging to warn temporarily.
				log.warn("changeItemBean.toString(): " + changeItemBean.toString());
				log.warn("changeItemBean.getFieldType() : " + changeItemBean.getFieldType()
						+ "changeItemBean.getField() : " + changeItemBean.getField() + " changeItemBean.getToString(): "
						+ changeItemBean.getToString());

				if (changeItemBean.getField().equals(STATUS_FIELD)
						&& changeItemBean.getToString().equals(resolvedStatus)) {
					ApplicationUser resolver = changeHistory.getAuthorObject();
					return resolver;
				}
			}
		}
		return null;
	}

	public String getPriorityAssesmentByReporter(List<ChangeHistory> changeHistories, ApplicationUser user) {
		for (ChangeHistory changeHistory : changeHistories) {
			for (ChangeItemBean changeItemBean : changeHistory.getChangeItemBeans()) {
				if (changeItemBean.getField().equals(PRIORITY_FIELD) && changeHistory.getAuthorObject().equals(user)) {
					String resolverPriority = changeItemBean.getTo();
					return resolverPriority;
				}
			}
		}

		return null;
	}

	public String getOriginalPriority(List<ChangeHistory> changeHistories) {
		Collections.sort(changeHistories, new Comparator<ChangeHistory>() {

			@Override
			public int compare(ChangeHistory o1, ChangeHistory o2) {
				return o1.getTimePerformed().compareTo(o2.getTimePerformed());
			}
		});

		for (ChangeHistory changeHistory : changeHistories) {
			for (ChangeItemBean changeItemBean : changeHistory.getChangeItemBeans()) {
				if (PRIORITY_FIELD.equals(changeItemBean.getField())) {
					String originalPriority = changeItemBean.getFrom();
					return originalPriority;
				}
			}
		}
		return null;
	}

}
