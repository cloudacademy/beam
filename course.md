# Introduction to Google Cloud Dataflow
This file contains text you can copy and paste for the examples in Cloud Academy's _Introduction to Google Cloud Dataflow_ course.  

### Building and Running a Pipeline
Installing on your own computer: https://cloud.google.com/dataflow/docs/quickstarts  
Transforms: https://beam.apache.org/documentation/sdks/javadoc/2.0.0/org/apache/beam/sdk/transforms/package-summary.html

Note: Cloud Shell now uses Java 11 by default, so to get this demo to work, switch to Java 8 by running the following command.  
It will generate errors, but it will still work.
```
sudo update-java-alternatives -s java-1.8.0-openjdk-amd64 && export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
```

```
git clone https://github.com/cloudacademy/beam.git
cd beam/examples/java8
mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.MinimalLineCount
```
```
gsutil cat gs://dataflow-samples/shakespeare/kinglear.txt | wc
```

### Deploying a Pipeline on Cloud Dataflow
```
nano ~/.profile
    PROJECT=[Your Project ID]
    BUCKET=gs://dataflow-$PROJECT
gsutil mb $BUCKET
cd ~/beam/examples/java8
```
```
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=org.apache.beam.examples.MinimalLineCountArgs \
  -Dexec.args="--runner=DataflowRunner \
  --project=$PROJECT \
  --tempLocation=$BUCKET/temp \
  --region=us-central1"
```
```
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=org.apache.beam.examples.LineCount \
  -Dexec.args="--runner=DataflowRunner \
  --project=$PROJECT \
  --tempLocation=$BUCKET/temp \
  --output=$BUCKET/linecount \
  --region=us-central1"
```

### Custom Transforms
```
cd ~/beam/examples/java8
```
```
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=org.apache.beam.examples.MinimalWordCount \
  -Dexec.args="--runner=DataflowRunner \
  --project=$PROJECT \
  --tempLocation=$BUCKET/temp \
  --output=$BUCKET/wordcounts \
  --region=us-central1"
```

### Composite Transforms
```
cd ~/beam/examples/java8
```
```
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=org.apache.beam.examples.complete.game.UserScore \
-Dexec.args="--runner=DataflowRunner \
  --project=$PROJECT \
  --tempLocation=$BUCKET/temp/ \
  --output=$BUCKET/scores \
  --region=us-central1"
```

### Windowing
```
cd ~/beam/examples/java8
```
```
mvn -Pdataflow-runner compile exec:java -Dexec.mainClass=org.apache.beam.examples.complete.game.HourlyTeamScore \
-Dexec.args="--runner=DataflowRunner \
  --project=$PROJECT \
  --tempLocation=$BUCKET/temp/ \
  --output=$BUCKET/scores \
  --startMin=2015-11-16-16-00 \
  --stopMin=2015-11-17-16-00 \
  --region=us-central1"
```

### Running LeaderBoard
```
bq mk game
```
API Console Credentials: https://console.developers.google.com/projectselector/apis/credentials
```
export GOOGLE_APPLICATION_CREDENTIALS="[Path]/[Credentials file]"
cd ~/beam/examples/java8
```
```
mvn compile exec:java  -Dexec.mainClass=org.apache.beam.examples.complete.game.injector.Injector \
-Dexec.args="$PROJECT game none"
```
```
cd ~/beam/examples/java8
```
```
mvn compile exec:java -Dexec.mainClass=org.apache.beam.examples.complete.game.LeaderBoard \
-Dexec.args="--runner=DataflowRunner \
  --project=$PROJECT \
  --tempLocation=$BUCKET/temp/ \
  --output=$BUCKET/leaderboard \
  --dataset=game \
  --topic=projects/$PROJECT/topics/game \
  --region=us-central1"
```
