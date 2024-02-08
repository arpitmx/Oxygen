
# <img width="60" alt="Frame 1000002619" src="https://github.com/arpitmx/Oxygen/assets/59350776/9a051ee8-1633-4b1f-b61e-1a418d736175"> NCS Oxygen (O2)
 

[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/your-username/ncs-oxygen/blob/main/LICENSE)
![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg) 
![Reactive](https://img.shields.io/badge/Reactive-coroutines-blue.svg)

NCS Oxygen (O2) is a project management Android app designed to streamline and optimize task and project management using modern project management techniques. With support for methodologies like sprints, kanban maps, and more, Oxygen empowers teams to efficiently plan, organize, and track their projects.

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/5de16190-256d-466a-ac96-fcbf06486e32" width="300" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/90caaf73-42fd-4395-9ae3-0639dbcbb821" width="300" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/635bfbbb-6a90-4c96-947c-d4a675eed491" width="300" />
</p>

---
<br>

### Table of Contents
* [Technologies](#technologies)
* [Setup](#setup)
* [App structure](#app_structure)
* [Predefined Modules](#predefined_structure)
* [Module/Package structure](#module_structure)
* [Testing](#testing)
* [Other](#other)
    * [Dependencies](#dependencies)
        * [How to add a dependency](#adddependencies)
    * [ktlint](#ktlint)
    * [Resource Naming Conventions](#resource_naming_conventions)
* [Recommended Reading](#recommended_reading)
* [License](#license)


## Technologies <a name="technologies"></a>
* Android SDK
* Kotlin Coroutine for Asynchronous tasks
* AndroidX Jetpack for navigation
* Dagger2 for dependency injection
* Retrofit/OkHttp/Gson for networking
* MVVM as architectural pattern
* SQLite/Room for local data storage
* Firestore as Cloud Database
* Firebase cloud functions
* Django database for Mailing system, webhooks



## Key Features

<img width="400" alt="Frame 1000002619" src="https://github.com/arpitmx/Oxygen/assets/59350776/85e48c6b-56ee-4426-ae8c-22f07886fcaa">
<br><br>

- Agile project management: Leverage sprint planning, backlog management, and task tracking to effectively manage projects using agile methodologies.
- Kanban boards: Visualize project progress and optimize workflow using customizable kanban boards.
- Task management: Create, assign, and track tasks for team members, ensuring clear accountability and progress monitoring.
- Collaborative workspace: Foster collaboration within teams by providing a shared platform for communication, file sharing, and discussions.
- Reporting and analytics: Generate insightful reports and analyze project performance to make data-driven decisions.

## Sections 



## Testing <a name="testing"></a>
Every module should contain tests for its use cases:

* `test`: Write unit tests for every `ViewModel` or `Service`/`Repository`. Mockito or PowerMock can be used to mock objects and verify correct behaviour.
* `androidTest`: Write UI tests for common actions in your app. Use JUnit 4 Tests with Espresso. Some helper methods are available in EspressoUtils.

The dependencies for testing are located in the `gradle/test-dependencies-android.gradle` and `gradle/test-dependencies.gradle` files. If your `module` already implements `gradle/library-module-android.gradle` or `gradle/library-module.gradle`, then these dependencies are automatically added to the `module`.

If your module does not implement these standard library gradle files, add the test dependencies with:

``` groovy
apply from: rootProject.file("gradle/XXX.gradle")
```


## Installation

To get started with NCS Oxygen (O2), follow these steps:

1. Clone the repository: `git clone https://github.com/arpitmx/oxygen.git`
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator.

## License

This project is licensed under the MIT License. See the [LICENSE](https://github.com/your-username/ncs-oxygen/blob/main/LICENSE) file for more information.
