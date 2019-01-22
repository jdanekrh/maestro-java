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

package org.maestro.common.duration;

import org.junit.Test;

import static org.junit.Assert.*;

public class DurationDrainTest {

    @Test
    public void testCanContinue() {
        DurationDrain durationDrain = new DurationDrain();

        assertEquals(-1, durationDrain.getNumericDuration());

        DurationCountTest.DurationCountTestProgress progress = new DurationCountTest.DurationCountTestProgress();

        for (int i = 0; i < 10; i++) {
            assertTrue(durationDrain.canContinue(progress));
            progress.increment();
        }


        for (int i = 0; i < 9; i++) {
            System.out.println("Current count " + i + " = " + progress.messageCount());
            assertTrue("Cannot continue", durationDrain.canContinue(progress));
        }

        assertFalse(durationDrain.canContinue(progress));

        progress.increment();
        assertTrue(durationDrain.canContinue(progress));
    }
}