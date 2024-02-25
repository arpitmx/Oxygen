
# <img width="60" alt="Frame 1000002619" src="https://github.com/arpitmx/Oxygen/assets/59350776/9a051ee8-1633-4b1f-b61e-1a418d736175"> NCS Oxygen (O2)
 
[![Repository](https://img.shields.io/badge/GitHub-Repository-brightgreen)](https://github.com/arpitmx/Oxygen)
[![License](https://img.shields.io/badge/license-MIT-blue.svg)](https://github.com/your-username/ncs-oxygen/blob/main/LICENSE)
![Android](https://img.shields.io/badge/Platform-Android-green.svg)
![Kotlin](https://img.shields.io/badge/Language-Kotlin-orange.svg) 
![Reactive](https://img.shields.io/badge/Reactive-coroutines-blue.svg)

NCS Oxygen (O2) is a project management Android app designed to streamline and optimize task and project management using modern project management techniques. With support for methodologies like sprints, kanban maps, and more, Oxygen empowers teams to efficiently plan, organize, and track their projects.

<p float="left" height="auto">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/5de16190-256d-466a-ac96-fcbf06486e32" width="250" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/90caaf73-42fd-4395-9ae3-0639dbcbb821" width="250" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/635bfbbb-6a90-4c96-947c-d4a675eed491" width="250" />
</p>

---
<br>

# Table of Contents
* [Technologies](#technologies)
* [Sections](#sections)
   * [Project Page](#project)
   * [Workspace](#work)
   * [Channels](#channel)
   * [Today](#today)
   * [O2 Ai](#o2mate)
   * [Tasks](#tasks)
   * [Code Viewer](#code)
* [Setup](#Installation)
* [Module/Package structure](#module_structure)
* [Testing](#testing)
* [Other](#other)
    * [Dependencies](#dependencies)
    * [ktlint](#ktlint)
    * [Resource Naming Conventions](#resource_naming_conventions)
* [Recommended Reading](#recommended_reading)
* [License](#license)


# Technologies <a name="technologies"></a>
* Android SDK
* Kotlin Coroutine for Asynchronous tasks
* Workmanager Api
* AndroidX Jetpack for navigation
* Dagger/Hilt for dependency injection
* Retrofit/OkHttp/Gson for networking
* MVVM as architectural pattern
* SQLite/Room for local data storage
* Firebase Firestore, MongoDB as Cloud Databases
* Firebase storage, dynamic links
* Firebase cloud functions
* Django rest for Mailing system, webhooks
* Paging 3 for pagination



# Key Features

<img width="400" alt="Frame 1000002619" src="https://github.com/arpitmx/Oxygen/assets/59350776/85e48c6b-56ee-4426-ae8c-22f07886fcaa">
<br><br>

- **Agile project management** : Leverage sprint planning, backlog management, and task tracking to effectively manage projects using agile methodologies.
- **Kanban boards** : Visualize project progress and optimize workflow using customizable kanban boards.
- **Task management** : Create, assign, and track tasks for team members, ensuring clear accountability and progress monitoring.
- **Collaborative workspace** : Foster collaboration within teams by providing a shared platform for communication, file sharing, and discussions.
- **Reporting and analytics** : Generate insightful reports and analyze project performance to make data-driven decisions.

# Sections <a name="sections"></a> 

### 1. Project Overview ✦ <a name="project"></a> 

Task detail page provides comprehensive details about any task, including issue type, priority, duration, difficulty, status, position in kanban, fix in build version, story points, issue title, extensive issue summary with Markdown support, moderator list, and resource links for files, images, and video support.

**Highlights:**
1. Issue Type, Priority, Duration, Difficulty, Status, Position in Kanban, Fix in Build Version, Story Points
2. Issue Title
3. Extensive Issue Summary with Markdown Support
4. Moderator List
5. Resource Links (Files, Images, Video)

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/2edb6f3f-02bc-4823-a711-2b3352eaffd7" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/80a6818e-b744-43b7-93a2-822adba6fd3e" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/6a5266d7-52fc-4b84-be56-6e2aa60b1136" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/6c4e6ed1-00b4-401b-a256-a13f0687d7f0" width="200" />
</p>


---

### 2. Clean, Segregated Workspace ✦ <a name="work"></a> 

Keep your workspace tidy and organized with our clean and segregated interface. Easily navigate between projects, tasks, and files without clutter.

**Highlight:**  
- Enhance productivity by providing a clutter-free environment.
- Segregate tasks with filters for better focus and organization.
- Streamline workflow by reducing distractions, focus on schedules.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/b2182d0f-218a-45a0-a873-5f7ee64ed3ee" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/25af984c-56b1-4079-8093-6089f4e567be" width="200" />
</p>


---

### 3. Share updates with Channels ✦  <a name="channel"></a> 

Effortlessly share project updates and communicate with team members through dedicated channel chats. Keep everyone informed and engaged in real-time discussions.

**Highlight:**  
- Foster transparent communication by sharing updates in dedicated channels.
- Facilitate collaboration and idea sharing among team members.
- Ensure important information reaches relevant stakeholders instantly.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/b0b3557c-7e48-4578-b4b3-9ac77c56a9ae" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/2893be2c-5ba2-482b-9ca5-4b75f8e38f6d" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/da468def-4dc1-48f2-8c7e-5a80abf71052" width="200" />
</p>

---


### 4. Schedule tasks for your Today ✦  <a name="today"></a> 

Efficiently plan your day by scheduling tasks for today. Keep track of all the tasks you need to accomplish, ensuring no deadlines are missed.

**Highlight:**  
- Organize your daily workflow by prioritizing tasks for today.
- Stay focused and productive with a clear list of tasks to complete.
- Easily adjust and update your schedule as priorities change throughout the day.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/5f5d95e3-6292-47c3-9299-be0cc6ad104e" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/5e3d6cd8-b4c4-4170-89e1-c1057be61e73" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/5e7707f9-e08b-484f-94b4-4b42b2f5a30b" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/99172573-afe2-4fa6-aa87-34f3557adea5" width="200" />
</p>

---

### 5. Swiftly automate tasks creation with O2Mate AI ✦ <a name="o2mate"></a> 

Harness the power of O2Mate AI to automate the creation of tasks swiftly. Streamline your workflow by leveraging intelligent task generation.

**Highlight:**  
- Save time and effort by automating the creation of tasks.
- Improve productivity by eliminating manual task creation processes.
- Leverage AI-driven insights to optimize task creation and allocation.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/688b44a4-72e3-49f3-8f6b-3e5df89c6465" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d21a6ca4-3643-420c-bde8-fd01c5ab8f1d" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/58b8ab95-87a7-4ea7-925c-6f8f0d0e6c5a" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/b4197375-2524-47b7-a59b-8a7e242e18e5" width="200" />
</p>

---

### 6. Detailed Tasks ✦  <a name="tasks"></a> 

Task detail page provides comprehensive details about any task, issues which includes : 
 1. Issue `Type` ,`Priority`, `Duration`, `Difficulty` , `Status`, `Position in kanban`, `Fix in build version`, `Story points`
 2. Issue Title
 3. Extensive Issue summary with Markdown support
 4. Moderator list
 5. Resource links -> Files, Images, Video support

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/1b639810-8bbf-46a8-81ac-b1fb201e787f" width="200" height="auto" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/a6981108-39af-4132-9b5c-9fdac6fc4206" width="200" height="auto"/>
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/ff543e7f-50ad-4f89-9da6-9fefcd2fe275" width="200" height="auto"/>
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/ccab4142-89de-4666-98ae-cc0adb5de466" width="200" height="auto"/>
</p>

---

### 7. Syntax highlighting, Code viewer ✦ <a name="code"></a> 

- **Clear Code Presentation**: The code viewer provides a clear and structured presentation of code snippets, enabling users to easily view, understand, and analyze code within the project management app.
- **Syntax Highlighting**: With syntax highlighting, the code viewer emphasizes different elements of the code, such as keywords, strings, and comments, enhancing readability and aiding in code comprehension. 
- **Interactive Features**: The code viewer may include interactive features like line numbering, code folding, and search functionality, allowing users to navigate through code efficiently and locate specific sections with ease.


<br>


<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d37dcf7f-4ea5-47fa-b8f8-235e38b15ef8" width="600"/>
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/6b5b22d4-8d4e-4f84-8834-8f51b32173ee" width="200" />
</p>

---

### 8. Flexible Task Chat system ✦  

Utilize our flexible real-time chat system within tasks, enabling seamless communication among team members. Share links, files, and code snippets with support for syntax highlighting.

**Highlight:**  
- Enhance collaboration by discussing tasks in real-time within the platform.
- Share relevant links and files directly within task discussions for easy reference.
- Collaborate on code snippets with syntax highlighting for improved readability and comprehension.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/8309b23e-2ce5-47a6-9895-efec3373cfb6" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/26e3a509-35ea-4122-9288-728e79455ff4" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/dbf9fcb1-74bd-4f8c-8357-cc33ca636b94" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/239ba210-0d0c-4ed5-ad53-b2c6ae06af6c" width="200" />
</p>

---

### 9. Add checks with Checklists ✦  

Enhance task management by adding checklists to tasks. Break down tasks into actionable steps and track progress more effectively.

**Highlight:**  
- Improve task clarity by breaking them down into smaller, manageable steps.
- Ensure thoroughness and completeness by checking off items as they are completed.
- Increase accountability and transparency within the team by monitoring checklist progress.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/5114eb11-2351-4bd5-8009-aedb1dd675c9" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d8315154-dc26-4a67-accc-fb820db87ddb" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/7f4ef800-d5cc-48df-b310-4ead34978bd3" width="200" />
</p>


---


### 10. Convinient Kanban board ✦  

Experience the convenience of our Kanban board for visualizing and managing your project workflow. Easily track tasks as they progress through different stages.

**Highlight:**  
- Streamline project management with a visually intuitive Kanban board interface.
- Easy task management and reordering.
- Customize columns and labels to match your team's workflow for maximum convenience.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/6f33fa42-4aa2-4974-b968-488d650d6c5f" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/920af06c-ce49-430c-85fa-c1e9900b44c3" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/7b7ec28f-234d-4c4b-ab2c-084092ec222f" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d491ae85-edbf-4b83-8a9c-926dbf26422b" width="200" />
</p>


---

### 11. Notification tab keep you updated ✦ 

Stay updated with our notification tab, which keeps you informed of real-time updates, including task assignments, comments, and mentions.

**Highlight:**  
- Stay informed of important activities and changes within your projects.
- Receive notifications for task assignments, comments, and mentions to stay engaged.
- Customize notification settings to control the type and frequency of updates you receive.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/707665ec-63b7-4357-81b1-63091e04d9aa" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d8599231-1266-4b6b-8914-9d36d630dfa1" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/ae4c765a-f54c-4b09-89a3-682ce110eaeb" width="200" />
</p>


---

### 12. Push Notifications ✦  

Receive push notifications directly to your devices for instant updates on important events and activities within your projects.

**Highlight:**  
- Stay connected and informed even when you're not actively using the app.
- Receive instant alerts for task assignments, comments, and mentions.
- Enable/disable push notifications based on your preferences for optimal productivity.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/14e4c8fb-8efe-490c-8b70-aad989a9ee73" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/b6f4a060-d480-407c-a02d-4fbf869fe57a" width="200" />
</p>

---

### 13. Flexible Search System ✦ 

Utilize our flexible search system to find tasks with atomic-level precision. Filter your search queries to narrow down results and locate specific tasks efficiently.

**Highlight:**  
- Conduct granular searches to find tasks based on various criteria such as title, description, assignee, due date, and more.
- Utilize advanced filtering options to refine search results and pinpoint specific tasks.
- Enhance productivity by quickly locating relevant tasks without sifting through irrelevant information.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/693cd8b2-724e-4b95-9aae-63becf47b77d" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/f4ad5eaf-c0a1-4df5-9799-589b7800bb32" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/3793ec7e-4a9c-46be-8dc1-d78f4a2a6fef" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/ef64ba7d-3490-4f13-802e-36115d5c6ea2" width="200" />
</p>

---

### 14. Team ✦ 

**Description:**  
Easily access and view details of team members within the app. Get insights into their roles, responsibilities, contact information, and more.

**Highlight:**  
- Access comprehensive profiles of team members to understand their expertise and contributions.
- View contact details such as email addresses or phone numbers for seamless communication.
- Stay informed about team dynamics and roles to facilitate collaboration and coordination.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/fa21fa25-d498-4cf3-9943-146a3bc652c3" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d8263d3f-456a-42d6-b7bd-513c00a3a636" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/d2a42d4e-ccfb-4b4a-a558-8d19a11335fa" width="200" />
</p>

---

### 15. Offline Mode ✦ 

Stay productive even without an internet connection with our offline mode feature. Access and work on your tasks seamlessly, even when offline.

**Highlight:**  
- Continue working on tasks and projects without interruption, regardless of internet connectivity.
- Access cached data and synchronize changes once you're back online.
- Ensure productivity and efficiency, even in remote or low-connectivity environments.

<br>

<p float="left">
   <img src="https://github.com/arpitmx/Oxygen/assets/59350776/80a0fd56-ace7-419a-b911-4b44ac2d9f08" width="200" />
   <img src="https://github.com/arpitmx/Oxygen/assets/59350776/fc222562-66c9-46f2-befa-a202ca619c12" width="200" />
</p>

---

### 16. Shake to Report ✦ 

Report bugs quickly and conveniently by shaking your device. Provide instant feedback to our development team for swift resolution of issues.

**Highlight:**  
- Streamline the bug reporting process with a simple gesture.
- Encourage user participation in improving app stability and performance.
- Expedite bug fixes by providing timely and actionable feedback.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/1261bea1-c176-4cb8-bc4b-4ea33e0e8325" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/f4cd0cb3-06f2-43a2-b15e-719bc0c0f282" width="200" />
</p>


---

### 17. Custom Crash Reporting System ✦ 

Oxygen has its own custom made crash reporting system called `Crashman` which listens for unhandled exception and crashes, wraps them into an Oxygen issue and opens a thread under Issues for faster bug recovery by the Oxygen Team.

<br>

<p float="left">
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/82c68123-03aa-4567-bb93-b5d353bb34e0" width="200" />
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/a6981108-39af-4132-9b5c-9fdac6fc4206" width="200"/>
  <img src="https://github.com/arpitmx/Oxygen/assets/59350776/24ff257d-b03a-47aa-a3b9-7ce30b00e0f0" width="200" />
</p>

---

 


## Module/Package structure <a name="module_structure"></a>
* Model classes should be located in a `model` package.
* Network related classes and interfaces (e.g. networking api's) are located in a `remote` package.
* Local storage related classes (e.g. databases or dao's) are located in a `local` package.
* Classes that do not correspond to a certain feature, should either be located in an `all` package, an `util` package or at the top level of the module.

These rules can be applied to either whole *modules* or *packages* depending on if you have feature modules or feature packages. An example for such a module/package structure can be found [here](https://github.com/tailoredmedia/AndroidAppTemplateExample).


## Installation <a name="Installation"></a>

To get started with NCS Oxygen (O2), follow these steps:

1. Clone the repository: `git clone https://github.com/arpitmx/oxygen.git`
2. Open the project in Android Studio.
3. Build and run the app on an Android device or emulator.


## Other <a name="other"></a>

### Dependencies <a name="dependencies"></a>

**All** dependencies are located in the `Libs.kt` file in the `buildSrc` folder. To implement them use `implementation Libs.XXX`.

Checking whether dependencies are ready to be updated, use `./gradlew refreshVersions`. Afterwards the newer version is added as comments to the `versions.properties` file. 

## Testing <a name="testing"></a>
Every module should contain tests for its use cases:

* `test`: Write unit tests for every `ViewModel` or `Service`/`Repository`. Mockito or PowerMock can be used to mock objects and verify correct behaviour.
* `androidTest`: Write UI tests for common actions in your app. Use JUnit 4 Tests with Espresso. Some helper methods are available in EspressoUtils.

The dependencies for testing are located in the `gradle/test-dependencies-android.gradle` and `gradle/test-dependencies.gradle` files. If your `module` already implements `gradle/library-module-android.gradle` or `gradle/library-module.gradle`, then these dependencies are automatically added to the `module`.

If your module does not implement these standard library gradle files, add the test dependencies with:

``` groovy
apply from: rootProject.file("gradle/XXX.gradle")
```

### ktlint <a name="ktlint"></a>
[ktlint](https://ktlint.github.io/) is a *Kotlin* linter and formatter. Using it is required to keep the code base clean and readable.

Use `./gradlew ktlintCheck` to lint your code.

To conform to the rules either:

* configure AndroidStudio [accordingly](https://github.com/pinterest/ktlint#-with-intellij-idea).
* use `./gradlew ktlintApplyToIdea` to overwrite IDE style files. Read more [here](https://github.com/JLLeitschuh/ktlint-gradle).


### Resource Naming Conventions <a name="resource_naming_conventions"></a>

The goal of these conventions is to reduce the effort needed to read and understand code and also enable reviews to focus on more important issues than arguing over syntax.

**Bold** rules should be applied. *Italic* rules are optional.

| Component        | Rule             | Example                   |
| ---------------- | ---------------------- | ----------------------------- |
| Layouts | **\<what\>**\_**\<where\>**.xml | `activity_main.xml`, `item_detail.xml` |
| Sub-Layouts | **\<what\>**\_**\<where\>**\_**\<description\>**.xml | `activity_main_appbar.xml` |
| Strings | **\<where\>**\_**\<what\>**\_**\<description\>** | `detail_tv_location` |
| Drawables | **\<what\>**\_**\<where\>**\_**\<description\>** | `btn_detail_background`, `card_overview_background` |
| Icons | ic_**\<description\>**\_**\<where\>**.xml | `ic_close.xml`, `ic_location_pin_detail.xml` |
| Dimensions | *\<where\>*\_**\<what\>**\_*\<description\>*\_*\<size\>* | `margin`, `detail_height_card`, `textsize_small` |
| Styles | **\<What\>**\.**\<Description\>** | `Text.Bold`, `Ratingbar.Preview` |
| Component Ids | **\<what\>\<Description\>** | `btnOpen`, `tvTitle` |


## Recommended Reading <a name="recommended_reading"></a>
* [Kotlin](https://kotlinlang.org/docs/reference/)
* [Kotlin Coroutines](https://kotlinlang.org/docs/reference/coroutines/basics.html)
* [Kotlin Flow](https://kotlinlang.org/docs/reference/coroutines/flow.html)
* [Navigation Architecture Component](https://developer.android.com/topic/libraries/architecture/navigation/)
* [ViewBinding](https://developer.android.com/topic/libraries/view-binding)
* [control](https://github.com/floschu/control/)
* [Koin](https://insert-koin.io/)
* [Retrofit](http://www.vogella.com/tutorials/Retrofit/article.html)
* [Room](http://www.vogella.com/tutorials/AndroidSQLite/article.html)


## License <a name="license"></a>
```
Copyright 2023 Hackncs

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```
