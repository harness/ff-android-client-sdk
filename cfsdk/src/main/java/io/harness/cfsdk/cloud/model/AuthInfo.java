package io.harness.cfsdk.cloud.model;

public class AuthInfo {

    private final String project;
    private final String environment;
    private final String projectIdentifier;
    private final String environmentIdentifier;
    private final String accountID;
    private final String organization;
    private final String clusterIdentifier;

    public AuthInfo(

            String project,
            String environment,
            String projectIdentifier,
            String environmentIdentifier,
            String accountID,
            String organization,
            String clusterIdentifier
    ) {

        this.project = project;
        this.environment = environment;
        this.projectIdentifier = projectIdentifier;
        this.environmentIdentifier = environmentIdentifier;
        this.accountID = accountID;
        this.organization = organization;
        this.clusterIdentifier = clusterIdentifier;
    }

    public String getProject() {
        return project;
    }

    public String getEnvironment() {
        return environment;
    }

    public String getProjectIdentifier() {
        return projectIdentifier;
    }

    public String getEnvironmentIdentifier() {
        return environmentIdentifier;
    }

    public String getAccountID() {
        return accountID;
    }

    public String getOrganization() {
        return organization;
    }

    public String getCluster() {

        return clusterIdentifier;
    }
}
