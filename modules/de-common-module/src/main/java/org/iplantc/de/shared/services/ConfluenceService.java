package org.iplantc.de.shared.services;

import org.iplantc.de.shared.exceptions.ConfluenceException;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;

/**
 * A service interface for interfacing with Confluence (synchronous part).
 * 
 * @author hariolf
 * 
 */
@RemoteServiceRelativePath("confluence")
public interface ConfluenceService extends RemoteService {

    /**
     * Creates a new page in the iPlant wiki as a child of the "List of Applications" page.
     * 
     * @param toolName the name of the tool which is used as the page title
     * @param description a tool description
     * @return a URL pointing to the new page
     * @throws ConfluenceException
     */
    String addPage(String toolName, String description) throws ConfluenceException;

    /**
     * Updates a documentation page with an average app rating.
     * 
     * @param toolName the name of the tool which is used as the page title
     * @param avgRating the new average rating score
     * @throws ConfluenceException
     */
    void updatePage(String toolName, int avgRating) throws ConfluenceException;

    /**
     * Adds a user comment to a tool description page.
     * 
     * @param toolName the name of the tool which is also the page title
     * @param score the user's rating of the tool
     * @param username the DE user who rated the tool
     * @param comment a comment
     * @return the comment ID generated by Confluence
     * @throws ConfluenceException
     */
    String addComment(String toolName, int score, String username, String comment)
            throws ConfluenceException;

    /**
     * Removes a user comment from a tool description page.
     * 
     * @param toolName the name of the tool which is also the page title
     * @param commentId the comment ID in Confluence
     * @throws ConfluenceException
     */
    void removeComment(String toolName, Long commentId) throws ConfluenceException;

    /**
     * Changes an existing user comment on a tool description page.
     * 
     * @param toolName the name of the tool which is also the page title
     * @param score the user's rating of the tool
     * @param username the DE user who rated the tool
     * @param commentId the comment ID in Confluence
     * @param newComment the new comment text
     * @throws ConfluenceException
     */
    void editComment(String toolName, int score, String username, Long commentId, String newComment)
            throws ConfluenceException;

    /**
     * Retrieves a user comment from a tool description page.
     * 
     * @param commentId the comment ID in Confluence
     * @return the comment text
     * @throws ConfluenceException
     */
    String getComment(long commentId) throws ConfluenceException;

    /**
     * 
     * Move the wiki doc page for an app to new location under list of application with new title
     * 
     * @param oldAppName the old app name
     * @param newAppName the new app name
     * @throws ConfluenceException
     */
    String movePage(String oldAppName, String newAppName) throws ConfluenceException;
        
    
}
