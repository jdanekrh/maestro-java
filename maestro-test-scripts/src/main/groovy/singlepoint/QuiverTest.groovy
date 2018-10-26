/*
 * Copyright 2018 Otavio R. Piske <angusyoung@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package singlepoint


import org.maestro.client.Maestro
import org.maestro.client.exchange.MaestroTopics
import org.maestro.common.LogConfigurator
import org.maestro.common.Role
import org.maestro.common.agent.UserCommandData
import org.maestro.common.client.notes.Test
import org.maestro.common.client.notes.TestDetails
import org.maestro.tests.AbstractTestProfile

import org.maestro.tests.cluster.NonAssigningStrategy
import org.maestro.tests.flex.FlexibleTestExecutor
import org.maestro.tests.flex.singlepoint.FlexibleTestProfile

/**
 * This test executes tests via Maestro Agent using Quiver (https://github.com/ssorj/quiver/)
 */
class QuiverExecutor extends FlexibleTestExecutor {
    private Maestro maestro

    QuiverExecutor(Maestro maestro, AbstractTestProfile testProfile) {
        super(maestro, testProfile, new NonAssigningStrategy(maestro))

        this.maestro = maestro
    }

    void startServices() {
        UserCommandData userCommandData = new UserCommandData(0, "rhea");

        maestro.userCommand(MaestroTopics.peerTopic(Role.AGENT), userCommandData)
        // Wait for up to 2 minutes for the test to complete
        Thread.sleep(60*1000*2)
    }

    @Override
    boolean run(final String scriptName, final String description, final String comments) {
        final TestDetails testDetails = new TestDetails(description, comments);
        final Test test = new Test(Test.NEXT, Test.NEXT, "quiver", scriptName, testDetails);

        return run(test)
    }
}

/**
 * Get the maestro broker URL via the MAESTRO_BROKER environment variable
 */
maestroURL = System.getenv("MAESTRO_BROKER")
if (maestroURL == null) {
    println "Error: the maestro broker URL was not given"

    System.exit(1)
}

brokerURL = System.getenv("SEND_RECEIVE_URL")
if (brokerURL == null) {
    println "Error: the send/receive URL was not given"

    System.exit(1)
}

sourceURL = System.getenv("SOURCE_URL")
if (sourceURL == null) {
    println "Warning: the quiver URL was not given. Using default: https://github.com/maestro-performance/maestro-quiver-agent.git"

    sourceURL = "https://github.com/maestro-performance/maestro-quiver-agent.git"
}

logLevel = System.getenv("LOG_LEVEL")
LogConfigurator.configureLogLevel(logLevel)

println "Connecting to " + maestroURL
maestro = new Maestro(maestroURL)

println "Creating the profile"
FlexibleTestProfile testProfile = new FlexibleTestProfile()

testProfile.setSendReceiveURL(brokerURL)
testProfile.setSourceURL(sourceURL)

branch = System.getenv("SOURCE_BRANCH")
testProfile.setBranch(branch)

println "Creating the executor"
QuiverExecutor executor = new QuiverExecutor(maestro, testProfile)

description = System.getenv("TEST_DESCRIPTION")
comments = System.getenv("TEST_COMMENTS")

int ret = 0

try {
    println "Running the test"
    if (!executor.run(this.class.getSimpleName(), description, comments)) {
        ret = 1
    }
} finally {
    println "Stopping the workers on the cluster if they haven't already done so"
    maestro.stop()

    println "Plotting the data"
}

if (ret == 0) {
    println "Test completed successfully"
}
else {
    println "Test completed with errors"
}
System.exit(ret)
