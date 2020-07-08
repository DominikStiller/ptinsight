# Flink

Flink is used as stream processing platform.

## Upgrading
As of now, the setup needs to be destroyed before an update.

1. Bump every occurence of the Flink version number in `../ansible/roles/flink/tasks` to the new version.
2. Diff the [new Flink config](https://github.com/apache/flink/blob/master/flink-dist/src/main/resources/flink-conf.yaml) with our version in `../ansible/roles/flink/templates/flink-conf.yaml.j2` and possibly update it.
3. Upgrade the Flink version for dependencies in `processing/build.gradle` and change dependency versions based on [suggested Gradle build file](https://ci.apache.org/projects/flink/flink-docs-master/dev/project-configuration.html#gradle).
4. Adapt your Job code if there were breaking API changes.
5. Recreate the infrastructure and setup using `make destroy all`.
