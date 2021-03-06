/*
 *  Copyright 2015 the original author or authors.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License"); 
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License. 
 */
package scouter.server;

import java.util.ArrayList

import scouter.server.util.EnumerScala
import scouter.util.IShutdown

object ShutdownManager {

    private val instances = new ArrayList[IShutdown]();

    def add(instance: IShutdown) {
        instances.add(instance);
    }

    def shutdown() {
        this.synchronized {
            if (instances.size() == 0) {
                return ;
            }
            Logger.println("S178", "Server Shutdown");
            EnumerScala.foreach(instances.iterator(), (inst: IShutdown) => {
                inst.shutdown();
                Logger.println("S179", "Shutdown " + inst + " ...");
            })
            instances.clear();
        }
    }
}
