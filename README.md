# Docker+Spring Graceful Shutdown

This is a sample project to demonstrate how the `CMD` and `ENTRYPOINT` dockerfile instructions interfere in the graceful
shutdown process with spring/java.

## Motivation

The [docker documentation](https://docs.docker.com/reference/dockerfile/#entrypoint) states the following:

> The shell form of ENTRYPOINT prevents any CMD command line arguments from being used. It also starts your ENTRYPOINT as a subcommand of /bin/sh -c, which does not pass signals. This means that the executable will not be the container's PID 1, and will not receive Unix signals. In this case, your executable doesn't receive a SIGTERM from docker stop <container>.

In other words, using the shell form of the entrypoint has almost the same effect as using `CMD` instead `ENTRYPOINT`.

So, according to the documentation, using `CMD` should prevent your spring application from receiving the SIGTERM command and therefore, the graceful shutdown process will not work.

## How this project works

This project creates a custom thread pool (`SchedulingConfiguration`) to be used by the spring @Scheduled. This thread pool also has the properties `setAwaitTerminationSeconds` and `setWaitForTasksToCompleteOnShutdown`
so it can affect the graceful shutdown process.

In our `HelloController` we use a scheduled task that holds one thread for 15 seconds.

If the graceful shutdown is working as expected, the spring application should wait for the task to finish before exiting.

And indeed, you can confirm that this works by running `./gradlew bootRun` followed by `kill PID`.

### Testing inside docker

To create the docker images needed to test this project as docker containers, you can use the following command:

```shell
docker build -t sample-graceful-shutdown:cmd -f Dockerfile_CMD . && docker build -t sample-graceful-shutdown:entrypoint -f Dockerfile_ENTRYPOINT .
```

This will create 2 docker images, one that uses the `CMD` instruction to start the .jar and the other using the `ENTRYPOINT`.

To test these images, first confirm that your docker is in swarm mode (this will also help to simulate the behavior inside kubernetes ): `docker swarm init`

Then, you can start both containers at same time using the compose file in the root of this project:

```shell
docker stack deploy -c docker-compose-deploy.yml mystack
```

Now, you will need 2 terminals to see what happens.

In the first one, run this command to see the logs inside the first container: `docker service logs -f mystack_cmd_service`.

Confirm that our hold task is printing the logs and then use this to trigger a container restart `docker service update --force mystack_cmd_service`.

You will see that the container will hang for 30 seconds (this time comes from docker stop_grace_period in the compose file) before exiting without
graceful shutdown. What happened was that docker sent the `SIGTERM` to the `/bin/sh -c` that is behind the `CMD` instruction and our .jar never received it.
After the `stop_grace_period`, docker sent a `SIGKILL` killing the process and the container.

If we do the same, but this time with the other service named `mystack_entrypoint_service` we will see the same behavior as when we run the .jar with `./gradlew bootRun`.
As soon as we sent the update command, spring will print something like `Commencing graceful shutdown.` and as soon as our hold task finishes, the .jar will exit and the container will be
shut down.

If you do not want to use docker swarm for testing, you cant do the same as before, but use `docker run [IMAGE_NAME]` to run the container and `docker stop [CONTAINER_ID]` to send the `SIGTERM` signal.
You'll see that the stop command for the CMD image will do nothing, the container will not stop, it only will stop with a `docker kill` command. For the ENTRYPOINT image, it will work just like the test
using docker swarm.