# stratio-meta #

One LANGUAGE to rule them all

# Project description #

Stratio META brings batch and streaming queries into a single language. META provides a distributed fault-tolerant server architecture that has the unique ability to execute queries in Cassandra, Spark, and/or Stratio Streaming. From an architectural point of view, Stratio META is composed of:

   * meta-common : Definition of common classes used by other components.
   * meta-core : Grammar definition, statement parsing, validation, and execution.
   * meta-deep : Allows meta-server to support advanced capabilities such as SELECT with INNER JOIN.
   * meta-dist : Contains config files and scripts to start/stop meta-server and meta-sh in production environments.
   * meta-driver : Java API offered to client applications.
   * meta-server : Listens for client connections and executes requested commands.
   * meta-sh : The interactive shell; uses meta-driver to connect to the server.

## Full documentation ##

See the Wiki for full documentation, examples, operational details and other information.

See the [Javadoc] () and [Language reference] () for the internal details.

## Prerequisites ##

In order to execute META, the following elements are required:

1. A working installation of Cassandra. The Stratio-cassandra fork is required to support LUCENE indexes.
2. A working installation of Stratio Deep. Otherwise, advanced capabilities such as SELECT with INNER JOIN will not be available.

## Compiling META ##

Compiling META involves generating a set of files (.tokens, Lexers, and Parsers) from the different grammar files. To automatically build Stratio META execute the following command:

```
   > mvn clean compile install
```

## Running the META-server##

```
   > mvn exec:java -DskipTests -pl meta-server -Dexec.mainClass="com.stratio.meta.server.MetaApplication"
```

## Running the META-shell ##

The META-shell allows users to launch interactive queries against a set of META servers. 
Works both in Unix and Windows.
The shell features:

 - History support (arrow navigation)
 - History search (ctrl-r)
 - Token completion (tab)
 - Help command

```
   > mvn exec:java -pl meta-sh -Dexec.mainClass="com.stratio.meta.sh.Metash"
```

The shell also supports asynchronous query execution by means of the --async parameter. This execution mode is required for streaming queries.

```
   > mvn exec:java -pl meta-sh -Dexec.mainClass="com.stratio.meta.sh.Metash" -Dexec.args="--async"
```

Additionally, you can execute an script upon launching the shell. The script will be executed first, and the prompt will be shown afterwards.

```
   > mvn exec:java -pl meta-sh -Dexec.mainClass="com.stratio.meta.sh.Metash" -Dexec.args="--script /path/script.metaql"
```


## Packaging ##

```
   > mvn package
```
See [this link](https://github.com/Stratio/stratio-meta/edit/release/0.0.4/meta-dist/src/main/include/README.md) to know start/stop the server and the shell from the dist packages


## Useful commands ##

Once the shell is running, you can exit the program introducing the word **exit** or **quit** in the query prompt. A command help system is available by introducing the command **help**. A help entry is available per command, to check specify help topics use **help command**.

## More about the project ##

You can find more information about this project (tutorials, examples, etc...) in http://wordpress.dev.strat.io/

# License #

Stratio Meta is licensed as [Apache2](http://www.apache.org/licenses/LICENSE-2.0.txt)

Licensed to STRATIO (C) under one or more contributor license agreements.  
See the NOTICE file distributed with this work for additional information 
regarding copyright ownership.  The STRATIO (C) licenses this file
to you under the Apache License, Version 2.0 (the
"License"); you may not use this file except in compliance
with the License.  You may obtain a copy of the License at

  http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing,
software distributed under the License is distributed on an
"AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
KIND, either express or implied.  See the License for the
specific language governing permissions and limitations
under the License.
