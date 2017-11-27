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

@GrabConfig(systemClassLoader=true)

@Grab(group='net.orpiske', module='maestro-java', version='1.1.1')

import net.orpiske.mpt.reports.ReportGenerator
import net.orpiske.mpt.utils.LogConfigurator

LogConfigurator.debug()
println "Creating report for directory: " + args[0]
ReportGenerator.generate(args[0])
