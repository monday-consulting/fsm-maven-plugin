package com.monday_consulting.maven.plugins.fsm.maven;

public class MavenCoordinate {

    private String groupId;

    private String artifactId;

    private String version;

    private String extension;

    private String classifier;

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getExtension() {
        return extension;
    }

    public void setExtension(String extension) {
        this.extension = extension;
    }

    public String getClassifier() {
        return classifier;
    }

    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder().append(groupId).append(':')
                .append(artifactId).append(':').append(getExtension());

        if (classifier != null) {
            sb.append(':').append(classifier);
        }

        sb.append(':').append(version);

        return sb.toString();
    }
}
