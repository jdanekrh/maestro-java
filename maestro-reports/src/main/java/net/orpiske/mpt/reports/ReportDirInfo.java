/*
 *  Copyright 2017 Otavio R. Piske <angusyoung@gmail.com>
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package net.orpiske.mpt.reports;

import org.apache.commons.io.FilenameUtils;

import java.io.File;
import java.io.IOException;

import net.orpiske.mpt.common.test.TestProperties;

public class ReportDirInfo {
    private String reportDir;
    private String nodeType;

    private String nodeHost;
    private int testNum;
    private boolean testSuccessful = false;

    private TestProperties testProperties;

    public ReportDirInfo(String baseDir, String reportDir, String nodeType) throws IOException {
        this.reportDir = reportDir;
        this.nodeType = nodeType;

        File file = new File(reportDir);

        nodeHost = FilenameUtils.getBaseName(file.getName());

        File testNumDir = file.getParentFile();
        testNum = Integer.parseInt(FilenameUtils.getBaseName(testNumDir.getName()));

        File resultType = testNumDir.getParentFile();
        if (resultType.getName().contains("success")) {
            testSuccessful = true;
        }

        testProperties = new TestProperties();

        testProperties.load(new File(baseDir + File.separator + reportDir, "test.properties"));
    }

    public String getBrokerUri() {
        return testProperties.getBrokerUri();
    }

    public String getDurationType() {
        return testProperties.getDurationType();
    }

    public String getApiName() {
        return testProperties.getApiName();
    }

    public String getApiVersion() {
        return testProperties.getApiVersion();
    }

    public long getDuration() {
        return testProperties.getDuration();
    }

    public long getMessageSize() {
        return testProperties.getMessageSize();
    }

    public int getRate() {
        return testProperties.getRate();
    }

    public int getParallelCount() {
        return testProperties.getParallelCount();
    }

    public boolean isVariableSize() {
        return testProperties.isVariableSize();
    }

    public int getFcl() {
        return testProperties.getFcl();
    }

    public String getReportDir() {
        return reportDir;
    }

    public void setReportDir(String reportDir) {
        this.reportDir = reportDir;
    }

    public String getNodeType() {
        return nodeType;
    }

    public void setNodeType(String nodeType) {
        this.nodeType = nodeType;
    }

    public String getNodeHost() {
        return nodeHost;
    }

    public void setNodeHost(String nodeHost) {
        this.nodeHost = nodeHost;
    }

    public int getTestNum() {
        return testNum;
    }

    public void setTestNum(int testNum) {
        this.testNum = testNum;
    }

    public boolean isTestSuccessful() {
        return testSuccessful;
    }

    public void setTestSuccessful(boolean testSuccessful) {
        this.testSuccessful = testSuccessful;
    }

    @Override
    public int hashCode() {
        return reportDir != null ? reportDir.hashCode() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ReportDirInfo that = (ReportDirInfo) o;

        return reportDir != null ? reportDir.equals(that.reportDir) : that.reportDir == null;
    }
}
