**RuneLite - Extended**

This is a forked runelite client of [here](https://github.com/runelite/runelite).
In this repository I have commited some personal plugins that I have been working on/am working on.
So far this is the list of custom plugins compared to the original client:

- **StatsStalker** - This plugin enables to track your progress vs your friends or another players on runescape. It displays a widget with level information compared to yours and shows the XP differences.

* [cache](cache/src/main/java/net/runelite/cache) - Libraries used for reading/writing cache files, as well as the data in it
* [http-api](http-api/src/main/java/net/runelite/http/api) - API for api.runelite.net
* [http-service](http-service/src/main/java/net/runelite/http/service) - Service for api.runelite.net
* [runelite-api](runelite-api/src/main/java/net/runelite/api) - RuneLite API, interfaces for accessing the client
* [runelite-client](runelite-client/src/main/java/net/runelite/client) - Game client with plugins

## Usage

Open the project in your IDE as a Maven project, build the root module and then run the RuneLite class in runelite-client.  
For more information visit the [RuneLite Wiki](https://github.com/runelite/runelite/wiki).

### License

RuneLite is licensed under the BSD 2-clause license. See the license header in the respective file to be sure.

## Contribute and Develop

We've set up a separate document for our [contribution guidelines](https://github.com/runelite/runelite/blob/master/.github/CONTRIBUTING.md).
