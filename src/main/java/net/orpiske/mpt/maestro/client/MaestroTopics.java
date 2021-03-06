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

package net.orpiske.mpt.maestro.client;

public class MaestroTopics {
    public final static String MAESTRO_TOPIC = "/mpt/maestro";
    public final static String NOTIFICATION_TOPIC = "/mpt/notifications";

    public final static String ALL_DAEMONS = "/mpt/daemon";
    public final static String SENDER_DAEMONS = "/mpt/daemon/sender";
    public final static String RECEIVER_DAEMONS ="/mpt/daemon/receiver";
    public final static String BROKER_INSPECTOR_DAEMONS = "/mpt/daemon/brokerd";

    public final static String[] MAESTRO_TOPICS = {MAESTRO_TOPIC, NOTIFICATION_TOPIC};

    private MaestroTopics() {}
}
