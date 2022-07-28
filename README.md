# DataCopierEngine
https://wiki.eduuni.fi/display/CSCdatamanagementoffice/Project+Plan+for+the+DataCopier+API

This project uses Quarkus.
## Running the application in dev mode

You can run your application in dev mode that enables live coding using:
```shell script
./mvn compile quarkus:dev
```

## Packaging and running the application

The application can be packaged using:
```shell script
./mvn package
```
It produces the `quarkus-run.jar` file in the `target/quarkus-app/` directory.
Be aware that it’s not an _über-jar_ as the dependencies are copied into the `ta
rget/quarkus-app/lib/` directory.


## Creating a native executable

You can create a native executable using: 
```shell script
./mvn package -Pnative
```
https://github.com/CSCfi/DataCopierEngine/blob/main/src/main/docker/Dockerfile.native
has further actually podman build instructions

## Provided Code

### Config

https://github.com/CSCfi/DataCopierEngine/tree/main/src/main/resources

## Author

Pekka Järveläinen

