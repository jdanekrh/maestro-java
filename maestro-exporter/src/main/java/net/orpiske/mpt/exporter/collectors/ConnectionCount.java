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

package net.orpiske.mpt.exporter.collectors;

import io.prometheus.client.Collector;
import io.prometheus.client.GaugeMetricFamily;
import net.orpiske.mpt.maestro.notes.StatsResponse;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ConnectionCount extends Collector {
    private String type;
    private StatsResponse stats;

    private static GaugeMetricFamily labeledGauge;

    static {
        labeledGauge = new GaugeMetricFamily("maestro_connection_count",
                "Connection count", Arrays.asList("peer", "type"));
    }

    public ConnectionCount(final String type) {
        this.type = type;
    }

    public List<MetricFamilySamples> collect() {
        List<MetricFamilySamples> mfs = new ArrayList<MetricFamilySamples>();

        if (stats != null) {
            labeledGauge.addMetric(Arrays.asList(stats.getName(), type), stats.getChildCount());

            mfs.add(labeledGauge);
        }

        return mfs;
    }

    public void eval(StatsResponse stats) {
        this.stats = stats;
    }
}