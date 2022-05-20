package org.foo.functions;
import groovy.transform.Field

@Field String containerName   = "docker"
@Field String urlRegistry   = "https://index.docker.io/v1/"
@Field String login   = "dockerlogin"

def pushImage(Map args=[:]) {
    if (!args.containerName) args.containerName = containerName
    container(args.containerName) {
        docker.withRegistry(
            urlRegistry,
            login
        ) {
            args.imageName.push(args.version)
        }
    }
}

def getImage(Map args=[:]) {
    if (!args.containerName) args.containerName = containerName
    if (!args.version) args.version = "latest"
    container(args.containerName) {
        return docker.withRegistry(
                urlRegistry,
                login
            ) {
            docker.image("${args.imageName}:${args.version}")
        }
    }
}

def pullImage(Map args=[:]) {
    if (!args.containerName) args.containerName = containerName
    container(args.containerName) {
        return docker.withRegistry(
                urlRegistry,
                login
        ) {
            docker.image("${args.imageName}:${args.version}").pull()
        }
    }
}

return this