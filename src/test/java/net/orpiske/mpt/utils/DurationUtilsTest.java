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

package net.orpiske.mpt.utils;

import org.junit.Assert;
import org.junit.Test;

/*
if (!convert_and_check("1d", 86400)) {
		return EXIT_FAILURE;
	}

	if (!convert_and_check("1d1h", 90000)) {
		return EXIT_FAILURE;
	}

	if (!convert_and_check("1d1h1m", 90060)) {
		return EXIT_FAILURE;
	}

	if (!convert_and_check("1d1h1m1s", 90061)) {
		return EXIT_FAILURE;
	}

	if (!convert_and_check("2d1m1s", 172861)) {
		return EXIT_FAILURE;
	}

	if (convert_and_check("2dm1s", 172861)) {
		return EXIT_FAILURE;
	}

	if (convert_and_check(NULL, 172861)) {
		return EXIT_FAILURE;
	}

	if (convert_and_check("1", 1)) {
		return EXIT_FAILURE;
	}
 */

public class DurationUtilsTest {

    private void parseAndCheck(final String str, long expected) throws DurationParseException {
        long seconds = DurationUtils.parse(str);

        Assert.assertEquals("Unable to parse " + str, expected, seconds);
    }

    @Test
    public void testParse() throws Exception {
        parseAndCheck("1d", 86400);
        parseAndCheck("1d1h", 90000);
        parseAndCheck("1d1h1m", 90060);
        parseAndCheck("1d1h1m1s", 90061);
        parseAndCheck("2d1m1s", 172861);
        parseAndCheck("1", 1);
    }
}
