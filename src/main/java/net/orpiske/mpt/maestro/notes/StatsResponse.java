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

package net.orpiske.mpt.maestro.notes;

import org.msgpack.core.MessageUnpacker;

import java.io.IOException;

public class StatsResponse extends MaestroResponse {
    private String timestamp;
    private long count;
    private double rate;
    private double latency;

    public StatsResponse(MessageUnpacker unpacker) throws IOException {
        super(MaestroCommand.MAESTRO_NOTE_STATS, unpacker);

        timestamp = unpacker.unpackString();
        count = unpacker.unpackLong();
        rate = unpacker.unpackDouble();
        latency = unpacker.unpackDouble();
    }

    public String getTimestamp() {
        return timestamp;
    }

    public long getCount() {
        return count;
    }

    public double getRate() {
        return rate;
    }

    public double getLatency() {
        return latency;
    }
}
