package eppic.rest.commons;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@ConfigurationProperties(prefix = "eppic-rest")
@ConfigurationPropertiesScan
public class EppicRestProperties {

    // pass a file at runtime with -Dspring.config.location=/var/www/application.properties

    private String mongoUri;
    private String mongoUriUserjobs;
    private String dbName;
    private String dbNameUserjobs;

    private String baseUserjobsDir;
    private String basePrecompDir;

    private String javaJreExec;
    private int numThreadsJobManager;
    private int numThreadsEppicProcess;
    private int memEppicProcess;
    private String eppicJarPath;
    private String cliConfigFile;

    private String emailReplytoAddress;
    private String emailUsername;
    private String emailPassword;
    private String emailHost;
    private int emailPort;

    private String emailBaseUrlJobRetrieval;

    private String emailJobSubmittedTitle;
    private String emailJobSubmittedMessage;
    private String emailJobSubmitErrorTitle;
    private String emailJobSubmitErrorMessage;
    private String emailJobErrorTitle;
    private String emailJobErrorMessage;
    private String emailJobFinishedTitle;
    private String emailJobFinishedMessage;

    public String getMongoUri() {
        return mongoUri;
    }

    public void setMongoUri(String mongoUri) {
        this.mongoUri = mongoUri;
    }

    public String getMongoUriUserjobs() {
        return mongoUriUserjobs;
    }

    public void setMongoUriUserjobs(String mongoUriUserjobs) {
        this.mongoUriUserjobs = mongoUriUserjobs;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbNameUserjobs() {
        return dbNameUserjobs;
    }

    public void setDbNameUserjobs(String dbNameUserjobs) {
        this.dbNameUserjobs = dbNameUserjobs;
    }

    public String getJavaJreExec() {
        return javaJreExec;
    }

    public void setJavaJreExec(String javaJreExec) {
        this.javaJreExec = javaJreExec;
    }

    public int getNumThreadsJobManager() {
        return numThreadsJobManager;
    }

    public void setNumThreadsJobManager(int numThreadsJobManager) {
        this.numThreadsJobManager = numThreadsJobManager;
    }

    public int getNumThreadsEppicProcess() {
        return numThreadsEppicProcess;
    }

    public void setNumThreadsEppicProcess(int numThreadsEppicProcess) {
        this.numThreadsEppicProcess = numThreadsEppicProcess;
    }

    public int getMemEppicProcess() {
        return memEppicProcess;
    }

    public void setMemEppicProcess(int memEppicProcess) {
        this.memEppicProcess = memEppicProcess;
    }

    public String getEppicJarPath() {
        return eppicJarPath;
    }

    public void setEppicJarPath(String eppicJarPath) {
        this.eppicJarPath = eppicJarPath;
    }

    public String getCliConfigFile() {
        return cliConfigFile;
    }

    public void setCliConfigFile(String cliConfigFile) {
        this.cliConfigFile = cliConfigFile;
    }

    public String getEmailReplytoAddress() {
        return emailReplytoAddress;
    }

    public void setEmailReplytoAddress(String emailReplytoAddress) {
        this.emailReplytoAddress = emailReplytoAddress;
    }

    public String getEmailUsername() {
        return emailUsername;
    }

    public void setEmailUsername(String emailUsername) {
        this.emailUsername = emailUsername;
    }

    public String getEmailPassword() {
        return emailPassword;
    }

    public void setEmailPassword(String emailPassword) {
        this.emailPassword = emailPassword;
    }

    public String getEmailHost() {
        return emailHost;
    }

    public void setEmailHost(String emailHost) {
        this.emailHost = emailHost;
    }

    public int getEmailPort() {
        return emailPort;
    }

    public void setEmailPort(int emailPort) {
        this.emailPort = emailPort;
    }

    public String getEmailBaseUrlJobRetrieval() {
        return emailBaseUrlJobRetrieval;
    }

    public void setEmailBaseUrlJobRetrieval(String emailBaseUrlJobRetrieval) {
        this.emailBaseUrlJobRetrieval = emailBaseUrlJobRetrieval;
    }

    public String getEmailJobSubmittedTitle() {
        return emailJobSubmittedTitle;
    }

    public void setEmailJobSubmittedTitle(String emailJobSubmittedTitle) {
        this.emailJobSubmittedTitle = emailJobSubmittedTitle;
    }

    public String getEmailJobSubmittedMessage() {
        return emailJobSubmittedMessage;
    }

    public void setEmailJobSubmittedMessage(String emailJobSubmittedMessage) {
        this.emailJobSubmittedMessage = emailJobSubmittedMessage;
    }

    public String getEmailJobSubmitErrorTitle() {
        return emailJobSubmitErrorTitle;
    }

    public void setEmailJobSubmitErrorTitle(String emailJobSubmitErrorTitle) {
        this.emailJobSubmitErrorTitle = emailJobSubmitErrorTitle;
    }

    public String getEmailJobSubmitErrorMessage() {
        return emailJobSubmitErrorMessage;
    }

    public void setEmailJobSubmitErrorMessage(String emailJobSubmitErrorMessage) {
        this.emailJobSubmitErrorMessage = emailJobSubmitErrorMessage;
    }

    public String getEmailJobErrorTitle() {
        return emailJobErrorTitle;
    }

    public void setEmailJobErrorTitle(String emailJobErrorTitle) {
        this.emailJobErrorTitle = emailJobErrorTitle;
    }

    public String getEmailJobErrorMessage() {
        return emailJobErrorMessage;
    }

    public void setEmailJobErrorMessage(String emailJobErrorMessage) {
        this.emailJobErrorMessage = emailJobErrorMessage;
    }

    public String getEmailJobFinishedTitle() {
        return emailJobFinishedTitle;
    }

    public void setEmailJobFinishedTitle(String emailJobFinishedTitle) {
        this.emailJobFinishedTitle = emailJobFinishedTitle;
    }

    public String getEmailJobFinishedMessage() {
        return emailJobFinishedMessage;
    }

    public void setEmailJobFinishedMessage(String emailJobFinishedMessage) {
        this.emailJobFinishedMessage = emailJobFinishedMessage;
    }

    public String getBaseUserjobsDir() {
        return baseUserjobsDir;
    }

    public void setBaseUserjobsDir(String baseUserjobsDir) {
        this.baseUserjobsDir = baseUserjobsDir;
    }

    public String getBasePrecompDir() {
        return basePrecompDir;
    }

    public void setBasePrecompDir(String basePrecompDir) {
        this.basePrecompDir = basePrecompDir;
    }
}
