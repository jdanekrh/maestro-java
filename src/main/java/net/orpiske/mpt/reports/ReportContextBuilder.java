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

import java.io.File;
import java.util.*;

public class ReportContextBuilder {
    private ReportContextBuilder() {}

    public static Map<String, Object> toContext(List<ReportFile> reportFiles, File baseDir) {
        Map<String, Object> context = new HashMap<>();

        Set<String> nodes = new HashSet<>();
        Set<String> nodeTypes = new HashSet<>();
        Set<Integer> tests = new HashSet<>();

        // This one needs to be ordered
        Set<ReportDirInfo> reportDirs = new LinkedHashSet<>();

        for (ReportFile reportFile : reportFiles) {
            nodes.add(reportFile.getNodeHost());

            String nodeType = reportFile.getNodeType().getValue();
            nodeTypes.add(nodeType);
            tests.add(reportFile.getTestNum());

            ReportDirInfo reportDirInfo =
                    new ReportDirInfo(baseDir.getPath(), reportFile.getReportDir(), nodeType);


            reportDirs.add(reportDirInfo);
        }



        context.put("nodes", nodes);
        context.put("nodeTypes", nodeTypes);
        context.put("tests", tests);
        context.put("reportFiles", reportFiles);
        context.put("reportDirs", reportDirs);

        return context;
    }


}
