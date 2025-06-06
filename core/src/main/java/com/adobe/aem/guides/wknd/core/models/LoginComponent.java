package com.adobe.aem.guides.wknd.core.models;

import static org.apache.sling.api.resource.ResourceResolver.PROPERTY_RESOURCE_TYPE;

import javax.annotation.PostConstruct;
import javax.jcr.RepositoryException;

import org.apache.sling.api.resource.Resource;
import org.apache.sling.api.resource.ResourceResolver;
import org.apache.sling.models.annotations.Default;
import org.apache.sling.models.annotations.Model;
import org.apache.sling.models.annotations.injectorspecific.InjectionStrategy;
import org.apache.sling.models.annotations.injectorspecific.SlingObject;
import org.apache.sling.models.annotations.injectorspecific.ValueMapValue;

import javax.jcr.Session;
import org.apache.jackrabbit.api.security.user.Authorizable;
import org.apache.jackrabbit.api.security.user.UserManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Model(adaptables = Resource.class)
public class LoginComponent {
	private static final Logger log = LoggerFactory.getLogger(LoginComponent.class);

    @ValueMapValue(name=PROPERTY_RESOURCE_TYPE, injectionStrategy=InjectionStrategy.OPTIONAL)
    @Default(values="No resourceType")
    protected String resourceType;

    @SlingObject
    private Resource currentResource;
    @SlingObject
    private ResourceResolver resourceResolver;

    private boolean isLoggedIn;
    private String userId;
    private String userName;
    private boolean isAdmin;

    @PostConstruct
    protected void init() {
    	Session session = resourceResolver.adaptTo(Session.class);
    	if (session != null) {
            try {
                UserManager userManager = resourceResolver.adaptTo(UserManager.class);
                userId = session.getUserID();
                isLoggedIn = !"anonymous".equals(userId);
                
                if (isLoggedIn && userManager != null) {
                    Authorizable authorizable = userManager.getAuthorizable(userId);
                    if (authorizable != null) {
                        userName = authorizable.getID();
                        isAdmin = authorizable.isGroup();
                    }
                }
            } catch (RepositoryException e) {
                log.error("Error getting user details", e);
            }
        }
    }
    
    public boolean isLoggedIn() {
        return isLoggedIn;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public boolean isAdmin() {
        return isAdmin;
    }

}