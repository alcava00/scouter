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
 *
 */

package scouter.server.db.status;

import java.io.IOException;
import java.util.Hashtable;

import scouter.server.db.io.RealDataFile;
import scouter.util.FileUtil;
import scouter.util.IClose;

object StatusWriter {
    val table = new Hashtable[String, StatusWriter]();

    def open(file: String): StatusWriter = {
        table.synchronized {
            var reader = table.get(file);
            if (reader != null) {
                reader.refrence += 1;
            } else {
                reader = new StatusWriter(file);
                table.put(file, reader);
            }
            return reader;
        }
    }
}

class StatusWriter(file: String) extends IClose {
    var refrence = 0;
    val out = new RealDataFile(file + ".pstat");

    def write(bytes: Array[Byte]): Long = {
        this.synchronized {
            val point = out.getOffset();
            out.writeInt(bytes.length);
            out.write(bytes);
            out.flush();
            return point;
        }
    }

    override def close() {
        StatusWriter.table.synchronized {
            if (this.refrence == 0) {
                StatusWriter.table.remove(this.file)
                FileUtil.close(out);
            } else {
                this.refrence -= 1
            }
        }
    }
}