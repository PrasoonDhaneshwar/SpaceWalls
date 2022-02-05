# Astronomy Picture of the Day
https://apod.nasa.gov/apod/astropix.html

## NASA APIs ##
This app generates requests based on NASA's APOD service:
https://api.nasa.gov/

### *Sign up for your API key at:* ###
https://api.nasa.gov/index.html#apply-for-an-api-key

### *An example query:* ###
- Default request: https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY

- For any given date: https://api.nasa.gov/planetary/apod?api_key=DEMO_KEY&date=YYYY-MM-DD

## Download ##
Download it [here](https://raw.githubusercontent.com/PrasoonDhaneshwar/Astronomy-Photo-Of-The-Day-Android/main/Astronomy-Picture-of-the-Day-v1.0.apk) to run the application in your Android phone.

#### App Features: ####
- Search a picture/video for date of their choice.
- Display date, explanation, Title and the image / video of the day.
- Create/manage a list of "favorites".
- Wallpaper Scheduler from their favorites or picture of current day.

#### Extras: ####
- Dark mode.
- Support for different screen sizes and orientations.

## Screenshots ##
#### *Select any date and fetch it's corresponding picture information.* ####

<!-- ![](images/MainPage.jpg) -->

#### *"Add" it to "Favorites" and save the list.* ####

<!-- ![](images/ListOfImages.jpg) -->

#### *Support for Landscape orientation:* ####

<!-- ![](images/LandscapeFlow.jpg) -->

#### *Detailed view of picture:* ####

<!-- ![](images/DetailImage.jpg) -->

#### *Support for orientation:* ####

<!-- ![](images/MainPageLandscape.jpg) -->

#### Dependencies ####

- *[Retrofit](https://square.github.io/retrofit/).* A type-safe HTTP client for Android and Java.
- *[Glide](https://github.com/bumptech/glide).* Fast and efficient open source media management and image loading framework for Android that wraps media decoding, memory and disk caching, and resource pooling into a simple and easy to use interface.
- *[Gson](https://github.com/google/gson).* Convert Java Objects into their JSON representation.
- *[Secrets Gradle Plugin for Android](https://github.com/google/secrets-gradle-plugin).* Plugin for providing your secrets securely to your Android project.

- This app follows MVVM architecture. Dependencies like Room, Navigation Components and Kotlin Coroutines are the essential components of the application.