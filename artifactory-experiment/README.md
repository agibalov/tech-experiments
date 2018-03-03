# artifactory-experiment

Start Artifactory: `./start.sh`. Go to `http://localhost:8081/` and perform the basic setup. Make sure to set admin's password to `qwerty`.

Stop Artifactory: `./stop.sh`.

#### Maven publish and consume the library

Add credentials to Maven's `settings.xml`:

```xml
...
<servers>
    <server>
        <id>my-central</id>
        <username>admin</username>
        <password>qwerty</password>
    </server>
    <server>
        <id>my-snapshots</id>
        <username>admin</username>
        <password>qwerty</password>
    </server>
</servers>
...
```

Go to `calculator-lib` and run `./publish-release.sh`. This will build the library (1.0) and publish it to Artifactory's `libs-release` repository (there's also `./publish-snapshot.sh` to publish it to `libs-snapshot` as 1.0-SNAPSHOT)

Go to `calculator-app` and do `mvn test` - this will download `calculator-lib-1.0` from Artifactory before running the tests.

#### Plain file publish and consume

* `./upload.sh` to upload `data1.txt` to Artifactory.
* `./download.sh` to download `data1.txt` as `download-data1.txt`.
