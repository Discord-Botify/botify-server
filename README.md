# botify-server
The back-end controlling a Discord Bot and serving a RESTful API to interact with our app functionality

## Give me the details.
Every internet application needs an API to provide functionality. That's the code in this repository. Our server code handles API requests for our user interfaces, which currently include a website and a text-controlled Discord bot. Also, it manages our application's database

## Do you use any dependecies?
Well I'm not trying to reinvent the wheel! Our Discord bot uses Discord's own bot API, called [JDA](https://github.com/DV8FromTheWorld/JDA). Our API is made using [spark-java](http://sparkjava.com/). The last big one is an [API wrapper for Spotify](https://github.com/thelinmichael/spotify-web-api-java). Of course, there's a bunch of other dependencies sprinkled throughout.

## Sounds about normal, what's cool about it though?
If you're here to learn something new about Discord bots or the Spotify API, take a look through our code. If you're here as an employer (*wink, wink*) I'm glad to have you as well. Either way, keep reading for a more in-depth look at how it all works

# Purpose
This application started as a Discord bot which would notify you when an artist you like uploaded an album. It uses Spotify's database and API to accomplish this. **Spotify does not do this themselves.** So what does that mean for us? We have to patch together what Spotify *does* provide us to make it all work. In essence, we have to:
1. Save a user's list of artists they want to follow
2. Every few hours, reach out to the Spotify API and get the number of albums for each artist they follow
3. Check that number against our current number of albums that we've saved
4. If the number we pulled from Spotify is greater than the one we previously had, the artist uploaded something new! We notify the user acordingly


Of course, it's a lot more complicated than that, but that's how it works generally. Visualizations of how the application works can be found in the [project-diagrams repository](https://github.com/Discord-Botify/project-diagrams).

